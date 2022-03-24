package cn.qsky.service.impl;

import cn.qsky.entity.User;
import cn.qsky.mapper.UserMapper;
import cn.qsky.service.UserService;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service(value = "userService")
public class UserServiceImpl implements UserService {

  @Resource
  UserMapper userMapper;

  @Override
  public User getUserByUid(String uid) {
    return userMapper.getUserByUid(uid);
  }

  @Override
  public List<User> getAll(User user) {
    return userMapper.getAll(user);
  }

  @Override
  public int insertOne(User user) {
    return userMapper.insertOne(user);
  }

  @Override
  public int insertBatch(List<User> userList) {
    return userMapper.insertBatch(userList);
  }
}
