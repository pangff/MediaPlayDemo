MediaPlayDemo
=============
列表自动播放demo

代码使用方法


com.pangff.mediaplaydemo.play包全部拷贝到自己项目中

音频对象实现ISoundBean 类似SoundBean

Activity中实现PlayStateListener，来获取播放的各种状态类似MainActivity


该demo提供了PlayUtils.getInstance().addSound(添加一个)、
PlayUtils.getInstance().playSelectedSound（播放指定文件）、
PlayUtils.getInstance().releasPlayer(Activity销毁时释放资源)

支持顺序播放
