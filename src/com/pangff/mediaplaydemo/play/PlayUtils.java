package com.pangff.mediaplaydemo.play;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask.Status;
import android.util.Log;

import com.pangff.mediaplaydemo.play.AppDownloadTask.DownloadProgressListener;
import com.pangff.mediaplaydemo.play.IPlayVoiceProgressListener.VoiceProgressChangedEvent;


public class PlayUtils {

  private static PlayUtils playUtils;
  private PlayStateListener playStateListener;
  private IPlayVoiceProgressListener listener;
  private int currentVoicePosition = -1;
  private VoicePlayUtil voicePlayUtil;
  
  
  public void setPlayStateListener(PlayStateListener playStateListener){
    this.playStateListener = playStateListener;
  }


  public static PlayUtils getInstance() {
    if (playUtils == null) {
      playUtils = new PlayUtils();
    }
    return playUtils;
  }

  /**
   * 播放列表
   */
  public List<ISoundBean> soundBeanList = new ArrayList<ISoundBean>();

  /**
   * 追加
   * 
   * @param soundBean
   */
  public void addSound(ISoundBean soundBean) {
    soundBeanList.add(soundBean);
    if (currentVoicePosition == -1 || !voicePlayUtil.getMediaPlayer().isPlaying()) {
      currentVoicePosition = 0;
      startVoice(soundBeanList.get(0));
    }
  }
  
  /**
   * 播放选中声音
   * 
   * @param soundBean
   */
  public void playSelectedSound(ISoundBean soundBean) {
    clearSoundList();
    addSound(soundBean);
  }
  
  /**
   * 释放播放器
   */
  public void releasPlayer(){
    voicePlayUtil.release();
    clearSoundList();
  }

  /**
   * 清空
   */
  private void clearSoundList() {
    stopVoice();// 停止当前声音
    soundBeanList.clear();// 清除列表
    currentVoicePosition = -1;// 当前id置空
  }
  
  /**
   * 获取当前id
   * @return
   */
  private String getCurrentId() {
    if(currentVoicePosition==-1){
      return "";
    }else{
      return soundBeanList.get(currentVoicePosition).getUrl();
    }
  }

  /**
   * 初始化
   */
  private void init(){
    voicePlayUtil = new VoicePlayUtil();
    listener = new IPlayVoiceProgressListener() {
      @Override
      public void onVoiceProgressChanged(VoiceProgressChangedEvent event) {
        Log.e("dd", "!!!!!");
        if (event == null || event.voiceId == null) {
          return;
        }
        if (event.voiceId.equals(getCurrentId())) {
          if (event.progess < 0) {
            // 下载事件接收
          } else if (event.progess == 0) {
            // 进度为0有2种情况：
            if (event.playing) {// 播放中；
            } else { // 或者，播放完毕；
              playNext();
            }
          } else {
            if(playStateListener!=null){
              playStateListener.onProgress(soundBeanList.get(currentVoicePosition), event.progess);
            }
          }
        }
      }
    };
    voicePlayUtil.voiceChangedPublisher.register(listener);
  }

  private PlayUtils() {
    init();
  }
  
  
  /**
   * 播放下一个
   */
  private void playNext(){
    if(currentVoicePosition+1<soundBeanList.size()){
      currentVoicePosition++;
      startVoice(soundBeanList.get(currentVoicePosition));
      if(playStateListener!=null){
        playStateListener.onNextPlay(soundBeanList.get(currentVoicePosition));
      }
    }else{
      //发通知播放完毕
      if(playStateListener!=null && soundBeanList.size()>0){
        playStateListener.onFinishAllPlay();
      }
      voicePlayUtil.release();
      clearSoundList();
    }
  }
  
  
 


  /**
   * 关闭前一个播放指定的
   * @param sound
   * @param path
   */
  private void playOrStop(final ISoundBean sound, final String path) {
    boolean current = getCurrentId().equals(voicePlayUtil.voiceId);
    // self在playing
    if (current && voicePlayUtil.getMediaPlayer().isPlaying()) {
      return;
    }
    File file = new File(path);
    if (file != null && file.exists()) {
      // 其他项在playing
      if (voicePlayUtil.getMediaPlayer().isPlaying()) {
        voicePlayUtil.voiceId = null;
        voicePlayUtil.task.stop();
        voicePlayUtil.getMediaPlayer().stop();
      }
      voicePlayUtil.voiceId = sound.getUrl();
      try {
        voicePlayUtil.getMediaPlayer().reset();
        voicePlayUtil.getMediaPlayer().setDataSource(path);
        voicePlayUtil.getMediaPlayer().prepare();
        voicePlayUtil.getMediaPlayer().start();
        voicePlayUtil.task.start();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if(playStateListener!=null){
      playStateListener.onStartPlay(sound);
    }
  }

  /**
   * 播放语音
   * 
   * @param sound
   */
  private void startVoice(final ISoundBean sound) {
    boolean current = getCurrentId().equals(voicePlayUtil.voiceId);
    if(current){
      return;
    }
    if (!PhoneUtils.isSDMounted()) {
      ToastUtil.show("您的SD卡不可用");
      return;
    }
    // 获取服务器的语音信息
    int start = sound.getUrl().lastIndexOf("/") + 1;
    final String voicesName = sound.getUrl().substring(start, sound.getUrl().length());
    final String path;
    if (sound.isDiskCache()) {
      path = sound.getUrl();
    } else {
      path = PhoneUtils.getVoiceOnSDPath(voicesName);
    }

    File filePath = new File(path);
    if (filePath.exists()) {
      playOrStop(sound, path);
      return;
    }
    String downloadUrl = sound.getUrl();
    AppDownloadTask task = DownloadMgr.getTaskByUrl(downloadUrl);
    if (task == null || task.getStatus() == Status.FINISHED) {
      // 下载任务不存在或者已经结束的情况下，创建新的下载任务
      AppDownloadRequest request = new AppDownloadRequest();
      request.downloadUrl = downloadUrl;
      request.appFile = path;
      task = new AppDownloadTask();
      task.execute(request);
      // 发送下载事件
      if(playStateListener!=null){
        playStateListener.onStartDownload(sound);
      }
      DownloadMgr.cache(downloadUrl, task);

      task.addDownloadProgressListener(new DownloadProgressListener() {
        @Override
        public void onStatusChanged(int status, AppDownloadRequest result) {
          if (status == AppDownloadTask.PROGRESS) {
            // 发布进度信息
            VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
            event.voiceId = getCurrentId();
            event.progess = -1;
            event.playing = false;
            voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
          } else if (status == AppDownloadTask.OK) {
            if(playStateListener!=null){
              playStateListener.onDownloadFinished(sound);
            }
            // 下载完毕
            if (getCurrentId().equals(voicePlayUtil.voiceId)) {
              playOrStop(sound, path);
            } else {
              // 进入就绪状态
              VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
              event.voiceId = getCurrentId();
              event.progess = 0;
              event.playing = false;
              voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
            }
          } else {
            // 错误处理，进入就绪状态
            VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
            event.voiceId = getCurrentId();
            event.progess = 0;
            event.playing = false;
            voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
          }
        }
      });
    }


    // 其他项在playing
    if (voicePlayUtil.getMediaPlayer().isPlaying()) {
      voicePlayUtil.voiceId = null;
      voicePlayUtil.task.stop();
      voicePlayUtil.getMediaPlayer().stop();
    }
    voicePlayUtil.voiceId = getCurrentId();
  }


  /**
   * 停止当前语音
   */
  private void stopVoice() {
    VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
    event.voiceId = getCurrentId();
    event.playing = false;
    event.progess = 0;
    voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
    voicePlayUtil.voiceId = null;
    voicePlayUtil.task.stop();
    voicePlayUtil.getMediaPlayer().stop();
  }

}
