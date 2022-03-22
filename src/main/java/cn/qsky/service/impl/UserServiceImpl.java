package cn.qsky.service.impl;

import cn.qsky.entity.User;
import cn.qsky.mapper.UserMapper;
import cn.qsky.service.UserService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Resource
  UserMapper userMapper;

  @Override
  public User getUserByUid(String uid) {
    return userMapper.getUserByUid(uid);
  }
}
