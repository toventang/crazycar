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
}
