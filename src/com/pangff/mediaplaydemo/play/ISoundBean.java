package com.pangff.mediaplaydemo.play;



public interface ISoundBean {

  public abstract int getTimelen();

  public abstract void setTimelen(int timelen);

  public abstract String getUrl();

  public abstract void setUrl(String url);

  
  public abstract boolean isDiskCache();
}
