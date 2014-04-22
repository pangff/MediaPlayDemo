package com.pangff.mediaplaydemo.play;

public class PlaySate {
  /**
   * 下载
   */
  public final static int STATE_DOWNLOAD_START = -1;
  public final static int STATE_DOWNLOAD_ON = -2;
  public final static int STATE_DOWNLOAD_FINISHED = -3;
  
  public final static int STATE_READY=0;
  /**
   * 播放
   */
  public final static int STATE_PLAY_START= 1;
  public final static int STATE_PLAY_ON = 2;
  public final static int STATE_PLAY_OVER = 3;
  public final static int STATE_PLAY_RELEASE = 4;
  
  
  public final static String ACTION_PLAY_START = "ACTION_PLAY_START";
  public final static String ACTION_PLAY_RELEASE = "ACTION_PLAY_RELEASE";
  public final static String ACTION_PLAY_FINISHED = "ACTION_PLAY_FINISHED";
}
