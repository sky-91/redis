package cn.qsky.entity;

import cn.qsky.aop.DecryptField;
import cn.qsky.aop.EnDecryptMapperType;

import cn.qsky.aop.EncryptField;
import java.util.Date;

@EnDecryptMapperType
public class User {

  @DecryptField
  @EncryptField
  private String uid;

  @DecryptField
  @EncryptField
  private String userName;

  private Integer age;

  private Date registerTime;

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public Date getRegisterTime() {
    return registerTime;
  }

  public void setRegisterTime(Date registerTime) {
    this.registerTime = registerTime;
  }

  @Override
  public String toString() {
    return "User{" +
        "uid='" + uid + '\'' +
        ", userName='" + userName + '\'' +
        ", age=" + age +
        ", registerTime=" + registerTime +
        '}';
  }
}
