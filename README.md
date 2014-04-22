MediaPlayDemo 列表自动播放demo
=============

代码使用方法
-----------------------------------

###com.pangff.mediaplaydemo.play包全部拷贝到自己项目中

###音频对象实现ISoundBean 类似SoundBean

###Activity中添加广播

  //广播
  BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(PlaySate.ACTION_PLAY_START)){
        ToastUtil.show("播放开始URL:"+intent.getStringExtra("url"));
      }
      if(intent.getAction().equals(PlaySate.ACTION_PLAY_FINISHED)){
        ToastUtil.show("播放结束");
      }
      if(intent.getAction().equals(PlaySate.ACTION_PLAY_RELEASE)){
        ToastUtil.show("资源释放");
      }
    }
  };

###onCreate注册广播
    IntentFilter filter = new IntentFilter();         
    filter.addAction(PlaySate.ACTION_PLAY_START);
    filter.addAction(PlaySate.ACTION_PLAY_RELEASE);
    filter.addAction(PlaySate.ACTION_PLAY_FINISHED);
    registerReceiver(broadcastReceiver, filter);     

###onDestroy注销广播
unregisterReceiver(broadcastReceiver);   

功能
------------

该demo提供了PlayUtils.getInstance().addSound(添加一个)、
PlayUtils.getInstance().playSelectedSound（播放指定文件）、
PlayUtils.getInstance().releasPlayer(Activity销毁时释放资源)

###支持顺序播放
