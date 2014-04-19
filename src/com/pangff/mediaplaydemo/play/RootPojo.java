package com.pangff.mediaplaydemo.play;


/**
 * 通过网络访问得到的类的基类
 * 
 * @author Administrator
 * 
 */
public class RootPojo  {
  public static final String STATUS = "status";
  public static final String MESSAGE = "message";
  public static final String RESLUT_SUCCEED = "0000";
  public static final String RESLUT_BAN = "0603";

  /**
   * 状态码
   */
  private String status;

  /**
   * 返回描述或者错误信息
   */
  private String message;

  public String getStatus() {

    return status;
  }

  public void setStatus(String status) {

    this.status = status;
  }

  public String getMessage() {

    return message;
  }

  public void setMessage(String message) {

    this.message = message;
  }


}
