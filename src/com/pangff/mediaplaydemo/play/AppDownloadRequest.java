package com.pangff.mediaplaydemo.play;

/**
 * 网络文件下载请求
 */
public class AppDownloadRequest {
  /**
   * 下载文件的网络地址
   */
  public String downloadUrl;
  /**
   * 下载文件保存路径
   */
  public String appFile;
  /**
   * 下载文件大小
   */
  public long appSize;
  /**
   * 是否支持断点续传
   */
  public boolean acceptRanges;
  /**
   * 已经接收的文件大小
   */
  public long receivedCount = 0;
  /**
   * 当前的下载速度
   */
  public float speed = 0f;
}
