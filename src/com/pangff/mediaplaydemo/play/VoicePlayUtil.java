package com.pangff.mediaplaydemo.play;

import android.media.MediaPlayer;

import com.pangff.mediaplaydemo.play.IPlayVoiceProgressListener.VoiceProgressChangedEvent;

public class VoicePlayUtil {
  
  

  public VoicePlayUtil() {
  }


  // 每次延迟100毫秒再启动线程
  public String voiceId;
  // 音频相关
  public MediaPlayer mediaPlay = new MediaPlayer();

  public MediaPlayer createMediaPlayer() {
    if (mediaPlay == null) {
      mediaPlay = new MediaPlayer();
    }
    return mediaPlay;
  }

  public void release() {
      VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
      event.voiceId = voiceId;
      event.playing = false;
      event.progess = 0;
      event.state = PlaySate.STATE_PLAY_RELEASE;
      voiceChangedPublisher.notifyDataChanged(event);
    if(mediaPlay!=null){
      mediaPlay.stop();
      mediaPlay.release();
      mediaPlay = null;
    }
    task.stop();
    voiceId = null;
  }

  public EventPublish<IPlayVoiceProgressListener, VoiceProgressChangedEvent> voiceChangedPublisher =
      new EventPublish<IPlayVoiceProgressListener, VoiceProgressChangedEvent>() {

        @Override
        public void notifyEvent(IPlayVoiceProgressListener listener, VoiceProgressChangedEvent event) {
          listener.onVoiceProgressChanged(event);
        }
  };
  
  public RepeatTaskUtil task = new RepeatTaskUtil(new Runnable() {
    int count = 0;

    @Override
    public void run() {

      VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
      event.voiceId = voiceId;
      if (mediaPlay.isPlaying()) {
        event.state = PlaySate.STATE_PLAY_ON;
        event.playing = true;
        event.progess = mediaPlay.getCurrentPosition();
        //Log.e("ddd", "event.progess:"+event.progess);
      } else {
        event.state = PlaySate.STATE_PLAY_OVER;
        event.playing = false;
        event.progess = 0;
        count++;
        if (count > 3) {
          task.stop();
          count = 0;
        }
      }
      voiceChangedPublisher.notifyDataChanged(event);
    }
  }, 100);
}
