package com.pangff.mediaplaydemo.play;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import android.os.Environment;
import android.os.StatFs;

public class PhoneUtils {

  /**
   * 得到sd卡的路径
   * 
   * @return
   */
  public static String getSDPath() {
    File sdDir = null;
    boolean sdCardExist =
        Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
    if (sdCardExist) {
      sdDir = Environment.getExternalStorageDirectory();
    } else {
      StringBuilder sbPhone = new StringBuilder();
      sbPhone.append(Environment.getDataDirectory());
      sbPhone.append("/data/com.witmob.callme");
      return sbPhone.toString();
    }
    return sdDir.toString();
  }
  
  /**
   * 得到sd卡的路径
   * 
   * @return
   */
  public static String getVoiceOnSDPath(String voiceName) {
    createDir(getSDPath() + "/callme");
    createDir(getSDPath() + "/callme/voices");
    return getSDPath() + "/callme/voices/"+voiceName;
  }
  
  /**
   * 创建路径
   * @param path
   */
  public static void createDir(String path){
    File file = new File(path);
    if(!file.exists()){
      file.mkdir();
    }
  }
  
  /**
   * 得到sd卡的路径
   * 
   * @return
   */
  public static String getVoiceTmpOnSDPath() {
    createDir(getSDPath() + "/callme");
    createDir(getSDPath() + "/callme/tmp");
    return getSDPath() + "/callme/tmp";
  }


  public static boolean existsSDCard() {
    boolean sdCardExist =
        Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    return sdCardExist;
  }

  public static long getSDFreeSize() {
    File sdDir = null;
    boolean sdCardExist =
        Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
    if (sdCardExist) {
      sdDir = Environment.getExternalStorageDirectory();
    } else {
      sdDir = Environment.getDataDirectory();
    }
    StatFs sf = new StatFs(sdDir.getPath());
    long blockSize = sf.getBlockSize();
    long freeBlocks = sf.getAvailableBlocks();
    // 返回SD卡空闲大小
    // return freeBlocks * blockSize; //单位Byte
    // return (freeBlocks * blockSize)/1024; //单位KB
    return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
  }

  /**
   * 通过linux命令获取mac地址 若wifi正在使用 则无法获取
   * 
   * @return
   */
  public static String getMacByLinuxCode() {
    String macSerial = null;
    String str = "";
    try {
      Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
      InputStreamReader ir = new InputStreamReader(pp.getInputStream());
      LineNumberReader input = new LineNumberReader(ir);
      for (; null != str;) {
        str = input.readLine();
        if (str != null) {
          macSerial = str.trim();// 去空格
          break;
        }
      }
    } catch (IOException ex) {
      // 赋予默认值
      ex.printStackTrace();
      macSerial = "";
    }
    return macSerial;
  }


  /**
   * SD卡是否加载中
   * 
   * @return booolean
   */
  public static boolean isSDMounted() {
    return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
  }

  public static boolean isSDFreeSpaceLarger(long freeSpace) {
    boolean flag = false;
    if (isSDMounted()) {
      StatFs statfs = new StatFs(Environment.getExternalStorageDirectory().getPath());
      long blockCount = statfs.getBlockCount();
      long availableBlocks = statfs.getAvailableBlocks();
      if (blockCount > 0L && blockCount - availableBlocks >= 0L
          && (long) statfs.getBlockSize() * (long) statfs.getFreeBlocks() >= freeSpace) {
        flag = true;
      }
    }
    return flag;
  }

}
