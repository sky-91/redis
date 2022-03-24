package cn.qsky.service;

import cn.qsky.entity.User;
import java.util.List;

public interface UserService {

  User getUserByUid(String uid);

  List<User> getAll(User user);

  int insertOne(User user);

  int insertBatch(List<User> userList);
}
