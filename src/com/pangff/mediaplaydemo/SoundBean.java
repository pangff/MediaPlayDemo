package com.pangff.mediaplaydemo;

import com.pangff.mediaplaydemo.play.ISoundBean;
import com.pangff.mediaplaydemo.play.RootPojo;



public class SoundBean extends RootPojo implements ISoundBean  {
  
  private int timelen;//语音时长
  private String url;//语音地址
  private boolean isDiskCache = false;
  private boolean isVoice;
  private String text;
  private String id;
  

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public boolean isVoice() {
    return isVoice;
  }

  public void setVoice(boolean isVoice) {
    this.isVoice = isVoice;
  }

  @Override
  public int getTimelen() {
    return timelen;
  }

  @Override
  public void setTimelen(int timelen) {
    this.timelen = timelen;
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isDiskCache() {
    return isDiskCache;
  }

  public void setDiskCache(boolean isDiskCache) {
    this.isDiskCache = isDiskCache;
  }

  

}
