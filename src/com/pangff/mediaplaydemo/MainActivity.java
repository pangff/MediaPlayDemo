package com.pangff.mediaplaydemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechListener;
import com.iflytek.cloud.speech.SpeechUser;
import com.pangff.mediaplaydemo.play.PlaySate;
import com.pangff.mediaplaydemo.play.PlayUtils;
import com.pangff.mediaplaydemo.play.ToastUtil;

public class MainActivity extends Activity {// implements PlayStateListener

  private TextView state;
  List<SoundBean> dataList = new ArrayList<SoundBean>();
  BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(PlaySate.ACTION_PLAY_START)) {
        ToastUtil.show("播放开始URL:" + intent.getStringExtra("url"));
      }
      if (intent.getAction().equals(PlaySate.ACTION_PLAY_FINISHED)) {
        ToastUtil.show("播放结束");
      }
      if (intent.getAction().equals(PlaySate.ACTION_PLAY_RELEASE)) {
        ToastUtil.show("资源释放");
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    IntentFilter filter = new IntentFilter();
    filter.addAction(PlaySate.ACTION_PLAY_START);
    filter.addAction(PlaySate.ACTION_PLAY_RELEASE);
    filter.addAction(PlaySate.ACTION_PLAY_FINISHED);
    registerReceiver(broadcastReceiver, filter);

    SpeechUser.getUser().login(MainActivity.this, null, null,
        "appid=" + getString(R.string.app_id), listener);

    state = (TextView) findViewById(R.id.state);

    // PlayUtils.getInstance().setPlayStateListener(this);
    for (int i = 0; i < 2; i++) {
      SoundBean soundBean = new SoundBean();
      if (i == 0) {
        soundBean.setVoice(false);
        soundBean.setText("你好,哈哈哈");
      }
      if (i == 1) {
        soundBean.setVoice(true);
        soundBean.setUrl("http://lgyinterface.witmob.com/send2.amr");
      }
      dataList.add(soundBean);
      PlayUtils.getInstance().addSound(soundBean);
    }


  }

  public void myOnClick(View view) {
    // 模拟清空直接播放指定文件
    PlayUtils.getInstance().playSelectedSound(dataList.get(1));
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onDestroy() {
    Log.e("dd", "销毁");
    PlayUtils.getInstance().releasPlayer();
    unregisterReceiver(broadcastReceiver);
    super.onDestroy();
  }
  


  /**
   * 用户登录回调监听器.
   */
  private SpeechListener listener = new SpeechListener() {

    @Override
    public void onData(byte[] arg0) {}

    @Override
    public void onCompleted(SpeechError error) {
      if (error != null) {
        Toast.makeText(MainActivity.this, "用户登录失败", Toast.LENGTH_SHORT)
            .show();
      }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {}
  };


  // @Override
  // public void onStartPlay(ISoundBean soundBean) {
  // Log.e("dddd", "开始播放");
  // state.setText("开始播放");
  // }
  //
  //
  // @Override
  // public void onStartDownload(ISoundBean soundBean) {
  // Log.e("dddd", "开始下载");
  // state.setText("开始下载");
  // }
  //
  //
  // @Override
  // public void onDownloadFinished(ISoundBean soundBean) {
  // Log.e("dddd", "下载完成");
  // state.setText("下载完成");
  // }
  //
  //
  // @Override
  // public void onFinishAllPlay() {
  // Log.e("dddd", "播放完成");
  // state.setText("播放完成");
  // }
  //
  //
  // @Override
  // public void onNextPlay(ISoundBean soundBean) {
  // Log.e("dddd", "播放下一个");
  // state.setText("播放下一个");
  // }
  //
  //
  //
  // @Override
  // public void onProgress(ISoundBean soundBean, int progress) {
  // //Log.e("dddd", "播放"+(progress/1000)+"s");
  // state.setText("播放"+(progress/1000)+"s");
  // }

}
