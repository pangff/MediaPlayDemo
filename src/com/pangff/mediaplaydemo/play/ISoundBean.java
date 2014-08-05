package com.pangff.mediaplaydemo.play;

import java.io.Serializable;



public interface ISoundBean extends Serializable{
  
  public abstract boolean isVoice();
  
  public abstract String getText();

  public abstract int getTimelen();

  public abstract void setTimelen(int timelen);

  public abstract String getUrl();

  public abstract void setUrl(String url);
  public abstract String getId();
  
  public abstract boolean isDiskCache();
  
  public abstract boolean isHasPrefixVoice();
  
  public abstract String getPrefixText();
}
