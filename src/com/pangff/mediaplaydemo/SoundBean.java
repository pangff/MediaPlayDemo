package com.pangff.mediaplaydemo;

import android.os.Parcel;

import com.pangff.mediaplaydemo.play.ISoundBean;
import com.pangff.mediaplaydemo.play.RootPojo;



public class SoundBean extends RootPojo implements ISoundBean  {
  
  private int timelen;//语音时长
  private String url;//语音地址
  private boolean isDiskCache = false;

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

  @Override
  public int describeContents() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    
  }

}
