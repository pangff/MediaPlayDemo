package com.pangff.mediaplaydemo.play;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask.Status;
import android.util.Log;

import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SynthesizerListener;
import com.pangff.mediaplaydemo.BaseApplication;
import com.pangff.mediaplaydemo.play.AppDownloadTask.DownloadProgressListener;
import com.pangff.mediaplaydemo.play.IPlayVoiceProgressListener.VoiceProgressChangedEvent;

public class PlayUtils implements SynthesizerListener {

	private static PlayUtils playUtils;
	private PlayStateListener playStateListener;
	private IPlayVoiceProgressListener listener;
	private int currentVoicePosition = -1;
	private VoicePlayUtil voicePlayUtil;

	// 合成对象.
	private static SpeechSynthesizer mSpeechSynthesizer;

	public void setPlayStateListener(PlayStateListener playStateListener) {
		this.playStateListener = playStateListener;
	}

	public static PlayUtils getInstance() {
		if (playUtils == null) {
			playUtils = new PlayUtils();
			// 初始化合成对象.
			mSpeechSynthesizer = SpeechSynthesizer
					.createSynthesizer(BaseApplication.self);
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
		if (currentVoicePosition == -1 || voicePlayUtil.mediaPlay == null
				|| !voicePlayUtil.mediaPlay.isPlaying()) {
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
	public void releasPlayer() {
		voicePlayUtil.release();
		clearSoundList();
		if (null != mSpeechSynthesizer) {
			mSpeechSynthesizer.stopSpeaking();
		}
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
	 * 
	 * @return
	 */
	private String getCurrentId() {
		if (currentVoicePosition == -1) {
			return "";
		} else {
			return soundBeanList.get(currentVoicePosition).getUrl();
		}
	}

	/**
	 * 获取当前sound
	 * 
	 * @return
	 */
	private ISoundBean getCurrentSound() {
		if (currentVoicePosition == -1) {
			return null;
		} else {
			return soundBeanList.get(currentVoicePosition);
		}
	}

	/**
	 * 初始化
	 */
	private void init() {
		voicePlayUtil = new VoicePlayUtil();
		listener = new IPlayVoiceProgressListener() {
			@Override
			public void onVoiceProgressChanged(VoiceProgressChangedEvent event) {
				if (event == null || event.voiceId == null) {
					return;
				}
				if (event.voiceId.equals(getCurrentId())) {
					if (event.state == PlaySate.STATE_DOWNLOAD_START) {
						if (playStateListener != null) {
						}
					}
					if (event.state == PlaySate.STATE_DOWNLOAD_ON) {
					}
					if (event.state == PlaySate.STATE_DOWNLOAD_FINISHED) {
						if (playStateListener != null) {
							playStateListener
									.onDownloadFinished(event.soundBean);
						}
					}
					if (event.state == PlaySate.STATE_PLAY_ON) {
						if (playStateListener != null) {
							if (event.progess > 0) {
								playStateListener
										.onProgress(soundBeanList
												.get(currentVoicePosition),
												event.progess);
							}
						}
					}
					if (event.state == PlaySate.STATE_PLAY_OVER) {
						playNext();
					}
					if (event.state == PlaySate.STATE_PLAY_RELEASE) {
						Intent intent = new Intent();
						intent.setAction(PlaySate.ACTION_PLAY_RELEASE);
						BaseApplication.self.sendBroadcast(intent);
					}
					if (event.state == PlaySate.STATE_PLAY_START) {
						Intent intent = new Intent();
						intent.putExtra("soundBean", event.soundBean);
						intent.setAction(PlaySate.ACTION_PLAY_START);
						BaseApplication.self.sendBroadcast(intent);
						if (playStateListener != null) {
							playStateListener.onStartPlay(event.soundBean);
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
	private void playNext() {
		Intent intent = new Intent();
		intent.setAction(PlaySate.ACTION_PLAY_FINISHED);
		intent.putExtra("soundBean", getCurrentSound());
		BaseApplication.self.sendBroadcast(intent);

		if (currentVoicePosition + 1 < soundBeanList.size()) {
			currentVoicePosition++;
			startVoice(soundBeanList.get(currentVoicePosition));
			if (playStateListener != null) {
				playStateListener.onNextPlay(soundBeanList
						.get(currentVoicePosition));
			}
		} else {
			// 发通知播放完毕
			releasPlayer();
			if (playStateListener != null) {
				playStateListener.onFinishAllPlay();
			}
		}
	}

	/**
	 * 关闭前一个播放指定的
	 * 
	 * @param sound
	 * @param path
	 */
	private void playOrStop(final ISoundBean sound, final String path,boolean isPlayingPrefix) {
		Log.e("dddd", "@@@@@@@@@@@@@@@@@@@@:"+(sound.isHasPrefixVoice() && isPlayingPrefix));
		if (sound.isHasPrefixVoice() && isPlayingPrefix) {
			startPrefixVoice(sound.getPrefixText());
			return;
		}
		if (voicePlayUtil.mediaPlay == null) {
			voicePlayUtil.createMediaPlayer();
		}
		boolean current = getCurrentId().equals(voicePlayUtil.voiceId);
		// self在playing
		if (current && voicePlayUtil.mediaPlay.isPlaying()) {
			return;
		}
		File file = new File(path);
		if (file != null && file.exists()) {
			// 其他项在playing
			if (voicePlayUtil.mediaPlay.isPlaying()) {
				voicePlayUtil.voiceId = null;
				voicePlayUtil.task.stop();
				voicePlayUtil.mediaPlay.stop();
			}
			voicePlayUtil.voiceId = sound.getUrl();
			try {
				voicePlayUtil.mediaPlay.reset();
				voicePlayUtil.mediaPlay.setDataSource(path);
				voicePlayUtil.mediaPlay.prepare();
				voicePlayUtil.mediaPlay.start();
				voicePlayUtil.task.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
		event.voiceId = getCurrentId();
		event.progess = -1;
		event.soundBean = sound;
		event.state = PlaySate.STATE_PLAY_START;
		event.playing = false;
		voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
	}

	private void startTextVoice(ISoundBean sound) {
		if (null == mSpeechSynthesizer) {
			// 创建合成对象.
			mSpeechSynthesizer = SpeechSynthesizer
					.createSynthesizer(BaseApplication.self);
		}
		// 设置合成发音人.
		String role = "xiaoyan";
		// 设置发音人
		mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, role);
		// 获取语速
		int speed = 50;
		// 设置语速
		mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "" + speed);
		// 获取音量.
		int volume = 50;
		// 设置音量
		mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "" + volume);
		// 获取语调
		int pitch = 50;
		// 设置语调
		mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "" + pitch);
		// 获取合成文本.

		final String source;
		if (null != sound.getText()) {
			source = sound.getText().toString();
		} else {
			source = null;
		}
		mSpeechSynthesizer.startSpeaking(source, PlayUtils.this);
	}

	/**
	 * 播放前缀
	 */
	boolean playedPrefixVoice;

	private void playPrefixVoice() {
		if (!playedPrefixVoice) {
			if (voicePlayUtil.mediaPlay == null) {
				voicePlayUtil.createMediaPlayer();
			}
			voicePlayUtil.mediaPlay.reset();
			try {
				voicePlayUtil.mediaPlay.setDataSource(AudioUtils
						.getPrefixVoice().getAbsolutePath());
				voicePlayUtil.mediaPlay.prepare();
				voicePlayUtil.mediaPlay.start();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				playedPrefixVoice = true;
			}
		}
	}

	private void startRelVoice(final ISoundBean sound,final boolean isPlayingPrefix) {

		boolean current = getCurrentId().equals(voicePlayUtil.voiceId);
		if (current) {
			return;
		}
		if (!PhoneUtils.isSDMounted()) {
			ToastUtil.show("您的SD卡不可用");
			return;
		}
		// 获取服务器的语音信息
		int start = sound.getUrl().lastIndexOf("/") + 1;
		final String voicesName = sound.getUrl().substring(start,
				sound.getUrl().length());
		final String path;
		if (sound.isDiskCache()) {
			path = sound.getUrl();
		} else {
			path = PhoneUtils.getVoiceOnSDPath(voicesName);
		}

		File filePath = new File(path);
		if (filePath.exists()) {
			playOrStop(sound, path,isPlayingPrefix);
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
			VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
			event.voiceId = getCurrentId();
			event.progess = -1;
			event.state = PlaySate.STATE_DOWNLOAD_START;
			event.playing = false;
			event.soundBean = sound;
			voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);

			DownloadMgr.cache(downloadUrl, task);

			task.addDownloadProgressListener(new DownloadProgressListener() {
				@Override
				public void onStatusChanged(int status,
						AppDownloadRequest result) {
					if (status == AppDownloadTask.PROGRESS) {
						// 发布进度信息
						VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
						event.voiceId = getCurrentId();
						event.progess = -1;
						event.state = PlaySate.STATE_DOWNLOAD_ON;
						event.playing = false;
						event.soundBean = sound;
						voicePlayUtil.voiceChangedPublisher
								.notifyDataChanged(event);
					} else if (status == AppDownloadTask.OK) {
						VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
						event.voiceId = getCurrentId();
						event.progess = -1;
						event.state = PlaySate.STATE_DOWNLOAD_FINISHED;
						event.playing = false;
						event.soundBean = sound;
						voicePlayUtil.voiceChangedPublisher
								.notifyDataChanged(event);

						// 下载完毕
						if (getCurrentId().equals(voicePlayUtil.voiceId)) {
							playOrStop(sound, path,isPlayingPrefix);
						} else {
							// 进入就绪状态
							VoiceProgressChangedEvent event2 = new VoiceProgressChangedEvent();
							event2.voiceId = getCurrentId();
							event2.progess = 0;
							event2.soundBean = sound;
							event2.state = PlaySate.STATE_READY;
							event2.playing = false;
							voicePlayUtil.voiceChangedPublisher
									.notifyDataChanged(event);
						}
					} else {
						// 错误处理，进入就绪状态
						VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
						event.voiceId = getCurrentId();
						event.progess = 0;
						event.soundBean = sound;
						event.state = PlaySate.STATE_READY;
						event.playing = false;
						voicePlayUtil.voiceChangedPublisher
								.notifyDataChanged(event);
					}
				}
			});
		}

		// 其他项在playing
		if (voicePlayUtil.mediaPlay == null) {
			voicePlayUtil.createMediaPlayer();
		}
		if (voicePlayUtil.mediaPlay.isPlaying()) {
			voicePlayUtil.voiceId = null;
			voicePlayUtil.task.stop();
			voicePlayUtil.mediaPlay.stop();
		}
		voicePlayUtil.voiceId = getCurrentId();

	}

	/**
	 * 播放语音
	 * @param sound
	 */
	private void startVoice(final ISoundBean sound) {
		if (sound.isVoice()) {
			startRelVoice(sound,sound.isHasPrefixVoice());
		} else {
			if (sound.isHasPrefixVoice()) {
				startPrefixVoice(sound.getPrefixText());
			}else{
				startTextVoice(sound);
			}
		}
	}

	boolean isPlayingPrefix = false;
	private void startPrefixVoice(String prefixText) {
		if (null == mSpeechSynthesizer) {
			// 创建合成对象.
			mSpeechSynthesizer = SpeechSynthesizer
					.createSynthesizer(BaseApplication.self);
			// 设置合成发音人.
			String role = "xiaoyan";
			// 设置发音人
			mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, role);
			// 获取语速
			int speed = 50;
			// 设置语速
			mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "" + speed);
			// 获取音量.
			int volume = 50;
			// 设置音量
			mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "" + volume);
			// 获取语调
			int pitch = 50;
			// 设置语调
			mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "" + pitch);
			// 获取合成文本.
		}
		isPlayingPrefix = true;
		playedPrefixVoice = false;
		mSpeechSynthesizer.startSpeaking(prefixText, PlayUtils.this);
	}

	/**
	 * 停止当前语音
	 */
	private void stopVoice() {
		VoiceProgressChangedEvent event = new VoiceProgressChangedEvent();
		event.voiceId = getCurrentId();
		event.playing = false;
		event.progess = 0;
		event.soundBean = getCurrentSound();
		event.state = PlaySate.STATE_PLAY_RELEASE;
		voicePlayUtil.voiceChangedPublisher.notifyDataChanged(event);
		voicePlayUtil.voiceId = null;
		voicePlayUtil.task.stop();
		if (voicePlayUtil.mediaPlay != null) {
			voicePlayUtil.mediaPlay.stop();
		}
	}

	@Override
	public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
	}

	@Override
	public void onCompleted(SpeechError arg0) {
		if (isPlayingPrefix) {
			if(!playedPrefixVoice){
				
				playedPrefixVoice = true;
			}else{
				isPlayingPrefix = false;
				if(getCurrentSound().isVoice()){
					startRelVoice(getCurrentSound(), isPlayingPrefix);;
				}else{
					startTextVoice(getCurrentSound());
				}
			}
		}else  {
			playNext();
		}
	}

	@Override
	public void onSpeakBegin() {
		if (getCurrentSound() != null && getCurrentSound().isHasPrefixVoice()
				&& isPlayingPrefix) {
			Intent intent = new Intent();
			intent.putExtra("soundBean", getCurrentSound());
			intent.setAction(PlaySate.ACTION_PLAY_START);
			BaseApplication.self.sendBroadcast(intent);
			mSpeechSynthesizer.pauseSpeaking();
			playPrefixVoice();
			BaseApplication.self.handlerCommon.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSpeechSynthesizer.resumeSpeaking();
				}
			}, 800);
		}else if(getCurrentSound() != null && !getCurrentSound().isHasPrefixVoice()){
			Intent intent = new Intent();
			intent.putExtra("soundBean", getCurrentSound());
			intent.setAction(PlaySate.ACTION_PLAY_START);
			BaseApplication.self.sendBroadcast(intent);
		}
	}

	@Override
	public void onSpeakPaused() {

	}

	@Override
	public void onSpeakProgress(int arg0, int arg1, int arg2) {
	}

	@Override
	public void onSpeakResumed() {

	}

}
