package cn.qsky.controller;

import cn.qsky.entity.User;
import cn.qsky.service.UserService;
import cn.qsky.vo.UserVo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

  @Resource
  UserService userService;

  @GetMapping("/getUser/{uid}")
  public UserVo getUserByUid(@PathVariable String uid) {
    Assert.hasText(uid, "Uid must have value!");
    UserVo vo = new UserVo();
    BeanUtils.copyProperties(userService.getUserByUid(uid), vo);
    return vo;
  }

  @GetMapping("/getAll")
  public List<UserVo> getAll() {
    List<User> userList = userService.getAll(new User());
    List<UserVo> voList = new ArrayList<>();
    userList.forEach(user -> {
      UserVo vo = new UserVo();
      BeanUtils.copyProperties(user, vo);
      voList.add(vo);
    });
    return voList;
  }

  @RequestMapping(value = "/insertOne", method = RequestMethod.POST)
  @ResponseBody
  public int insertOne(@RequestBody UserVo vo) {
    User user = new User();
    BeanUtils.copyProperties(vo, user);
    return userService.insertOne(user);
  }

  @RequestMapping(value = "/insertBatch", method = RequestMethod.POST)
  @ResponseBody
  public int insertBatch(@RequestBody List<UserVo> voList) {
    List<User> userList = new ArrayList<>();
    voList.forEach(vo -> {
      User user = new User();
      BeanUtils.copyProperties(vo, user);
      userList.add(user);
    });
    return userService.insertBatch(userList);
  }
}
