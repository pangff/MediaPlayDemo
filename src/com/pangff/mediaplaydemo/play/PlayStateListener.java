package com.pangff.mediaplaydemo.play;

public interface PlayStateListener {

  public void onStartPlay(ISoundBean soundBean);
  public void onNextPlay(ISoundBean soundBean);
  public void onStartDownload(ISoundBean soundBean);
  public void onDownloadFinished(ISoundBean soundBean);
  public void onFinishAllPlay();
  public void onProgress(ISoundBean soundBean,int progress);
  
}
