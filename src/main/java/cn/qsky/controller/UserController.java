package cn.qsky.controller;

import cn.qsky.service.UserService;
import cn.qsky.vo.UserVo;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
