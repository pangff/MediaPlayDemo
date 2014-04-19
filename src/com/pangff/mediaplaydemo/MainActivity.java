package com.pangff.mediaplaydemo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.pangff.mediaplaydemo.play.ISoundBean;
import com.pangff.mediaplaydemo.play.PlayStateListener;
import com.pangff.mediaplaydemo.play.PlayUtils;

public class MainActivity extends Activity implements PlayStateListener{

  private TextView state;
  List<SoundBean> dataList = new ArrayList<SoundBean>();
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    state = (TextView) findViewById(R.id.state);
   
    PlayUtils.getInstance().setPlayStateListener(this);
    for(int i=0;i<2;i++){
      SoundBean soundBean = new SoundBean();
      if(i==0){
        soundBean.setUrl("http://fm111.img.xiaonei.com/tribe/20070613/10/52/A314269027058MUS.mp3");
      }else{
        soundBean.setUrl("http://xdong.0943.com.cn/music/%E6%97%A0%E6%B3%AA%E7%9A%84%E9%81%97%E6%86%BE.mp3");
      }
      dataList.add(soundBean);
      PlayUtils.getInstance().addSound(soundBean);
    }
  }
  
  public void myOnClick(View view){
    //模拟清空直接播放指定文件
    PlayUtils.getInstance().playSelectedSound(dataList.get(1));
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }


  @Override
  public void onStartPlay(ISoundBean soundBean) {
    Log.e("dddd", "开始播放");
    state.setText("开始播放");
  }


  @Override
  public void onStartDownload(ISoundBean soundBean) {
    Log.e("dddd", "开始下载");
    state.setText("开始下载");
  }


  @Override
  public void onDownloadFinished(ISoundBean soundBean) {
    Log.e("dddd", "下载完成");
    state.setText("下载完成");
  }


  @Override
  public void onFinishAllPlay() {
    Log.e("dddd", "播放完成");
    state.setText("播放完成");
  }


  @Override
  public void onNextPlay(ISoundBean soundBean) {
    Log.e("dddd", "播放下一个");
    state.setText("播放下一个");
  }
  
  @Override
  protected void onDestroy() {
    Log.e("dd","销毁");
    PlayUtils.getInstance().releasPlayer();
    super.onDestroy();
  }


  @Override
  public void onProgress(ISoundBean soundBean, int progress) {
    //Log.e("dddd", "播放"+(progress/1000)+"s");
    state.setText("播放"+(progress/1000)+"s");
  }

}
