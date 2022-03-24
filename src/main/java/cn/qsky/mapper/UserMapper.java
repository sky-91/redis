package cn.qsky.mapper;

import cn.qsky.aop.EncryptField;
import cn.qsky.entity.User;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

  User getUserByUid(@EncryptField String uid);

  List<User> getAll(User user);

  int insertOne(User user);

  int insertBatch(List<User> userList);
}
