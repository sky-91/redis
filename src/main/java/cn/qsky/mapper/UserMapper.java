package cn.qsky.mapper;

import cn.qsky.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

  User getUserByUid(String uid);
}
