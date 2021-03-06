package com.pangff.mediaplaydemo.play;


public interface IPlayVoiceProgressListener extends IEventListener {

  public static class VoiceProgressChangedEvent {
    public String voiceId;
    public int progess;
    public int state;
    public ISoundBean soundBean;
    public boolean playing;
  }

  public void onVoiceProgressChanged(VoiceProgressChangedEvent event);

}
