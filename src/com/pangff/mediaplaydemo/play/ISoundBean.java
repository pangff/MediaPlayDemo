package com.pangff.mediaplaydemo.play;

import android.os.Parcelable;



public interface ISoundBean extends Parcelable{
  
  public abstract boolean isVoice();
  
  public abstract String getText();

  public abstract int getTimelen();

  public abstract void setTimelen(int timelen);

  public abstract String getUrl();

  public abstract void setUrl(String url);

  
  public abstract boolean isDiskCache();
}
