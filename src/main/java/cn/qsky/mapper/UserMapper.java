package cn.qsky.mapper;

import cn.qsky.aop.EnDecryptMapperAnnotation;
import cn.qsky.aop.EnDecryptMapperMethod;
import cn.qsky.aop.EncryptField;
import cn.qsky.entity.User;
import java.util.List;
import org.springframework.stereotype.Repository;

@EnDecryptMapperAnnotation
@Repository
public interface UserMapper {

  @EnDecryptMapperMethod
  User getUserByUid(String uid);

  List<User> getAll(User user);

  @EnDecryptMapperMethod
  int insertOne(User user);

  @EnDecryptMapperMethod
  int insertBatch(List<User> userList);
}
