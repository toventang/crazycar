package com.tastsong.crazycar.service;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tastsong.crazycar.dto.resp.RespMatchRank;
import com.tastsong.crazycar.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastsong.crazycar.mapper.MatchRecordMapper;

@Service
public class MatchRecordService {
    @Autowired
    private UserService userService;
    @Autowired
    private MatchRecordMapper matchRecordMapper;
    @Autowired
    private MatchClassService matchClassService;

    public boolean isBreakRecord(MatchRecordModel recordModel) {
        if (recordModel.getComplete_time() == -1) {
			return false;
		} 
        int minTime = getMiniCompleteTime(recordModel.getUid(), recordModel.getCid());
		if (minTime == -1 && recordModel.getComplete_time() != -1){
			return true;
		}		
		
		return recordModel.getComplete_time() < minTime;
    }

    // 获取记录中自己最好的成绩
    public int getMiniCompleteTime(int uid, int cid){
        QueryWrapper<MatchRecordModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MatchRecordModel::getUid, uid);
        queryWrapper.lambda().eq(MatchRecordModel::getCid, cid);
        queryWrapper.lambda().ne(MatchRecordModel::getComplete_time, -1);
        queryWrapper.lambda().orderByAsc(MatchRecordModel::getComplete_time);
        MatchRecordModel matchRecordModel = matchRecordMapper.selectOne(queryWrapper, false);
        if (ObjUtil.isEmpty(matchRecordModel)) {
            return -1;
        } else {
            return matchRecordModel.getComplete_time();
        }
    }

//    @Select("SELECT COUNT(*) as count, record_time as timestamp " +
//            "FROM match_record " +
//            "WHERE record_time > (UNIX_TIMESTAMP(CAST(SYSDATE() AS DATE)) - 60 * 60 * 24 * #{offsetTime}) " +
//            "GROUP BY FROM_UNIXTIME(record_time, '%y-%m-%d'), record_time " +
//            "ORDER BY record_time LIMIT #{offsetTime}")


    public void insertRecord(MatchRecordModel recordModel) {
        matchRecordMapper.insert(recordModel);
    }

    /**
     * 结算一次匹配赛成绩：判定破纪录 -> 发奖励 -> 写入记录
     * 三步在同一事务内执行，避免奖励已下发但记录未落库(或反之)造成的星币-账本不一致。
     * @param recordModel 待写入的比赛记录(uid/cid/complete_time/record_time 必填)
     * @return 是否破纪录，由上层用于构造响应(reward / rank)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean settleResult(MatchRecordModel recordModel) {
        boolean isBreakRecord = isBreakRecord(recordModel);
        if (isBreakRecord) {
            matchClassService.giveReward(recordModel.getUid(), recordModel.getCid());
        }
        insertRecord(recordModel);
        return isBreakRecord;
    }

    // 获取排行榜
    public List<RespMatchRank> getMatchRankListByCid(int cid){
        QueryWrapper<MatchRecordModel> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("uid", "MIN(complete_time) AS complete_time");
        queryWrapper.lambda().eq(MatchRecordModel::getCid, cid);
        queryWrapper.lambda().ne(MatchRecordModel::getComplete_time, -1);
        queryWrapper.lambda().groupBy(MatchRecordModel::getUid);
        queryWrapper.lambda().orderByAsc(MatchRecordModel::getComplete_time);
        queryWrapper.lambda().orderByAsc(MatchRecordModel::getUid);
        List<MatchRecordModel> matchRecordModels = matchRecordMapper.selectList(queryWrapper);
        List<RespMatchRank> rankModels = new ArrayList<>();
        for (int i = 0; i < matchRecordModels.size(); i++) {
            rankModels.add(toRespMatchRank(matchRecordModels.get(i), i + 1));
        }
        return rankModels;
    }

    private RespMatchRank toRespMatchRank(MatchRecordModel matchRecordModel, int rank) {
    	RespMatchRank respMatchRank = new RespMatchRank();
        respMatchRank.setUid(matchRecordModel.getUid());
        UserModel userModel = userService.getUserByUid(matchRecordModel.getUid());
        if (!ObjUtil.isEmpty(userModel)) {
            respMatchRank.setAid(userModel.getAid());
            respMatchRank.setUser_name(userModel.getUser_name());
        }
        respMatchRank.setRank_num(rank);
        respMatchRank.setComplete_time(matchRecordModel.getComplete_time());

    	return respMatchRank;
    }
}
