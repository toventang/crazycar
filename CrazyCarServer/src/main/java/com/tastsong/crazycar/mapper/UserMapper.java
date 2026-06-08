package com.tastsong.crazycar.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tastsong.crazycar.model.UserModel;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<UserModel> {

    /**
     * 原子累加用户星币，避免 read-modify-write 在并发下丢失更新。
     * delta 可正(发奖励)可负(扣星币购买道具)。
     * @return 受影响行数；1 表示成功，0 表示该 uid 不存在
     */
    @Update("UPDATE `user` SET star = star + #{delta} WHERE uid = #{uid}")
    int incrUserStar(@Param("uid") int uid, @Param("delta") int delta);

    /**
     * 原子扣减用户星币，并在 SQL 层用 star >= needStar 防止超扣。
     * 高并发下两次购买不会同时成功，余额不足时受影响行数为 0。
     * @return 受影响行数；1 表示扣减成功，0 表示余额不足或 uid 不存在
     */
    @Update("UPDATE `user` SET star = star - #{needStar} WHERE uid = #{uid} AND star >= #{needStar}")
    int deductUserStar(@Param("uid") int uid, @Param("needStar") int needStar);
}
