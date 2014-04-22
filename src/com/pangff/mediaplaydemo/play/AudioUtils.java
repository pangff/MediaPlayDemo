package com.pangff.mediaplaydemo.play;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.pangff.mediaplaydemo.BaseApplication;
import com.pangff.mediaplaydemo.R;

/**
 * 语音工具类
 * 
 * @author Mr.Right
 * @version 1.0
 */
public class AudioUtils {

  public final static int MSG_AMRFILECOMPLETE = 1;
  public final static int MSG_AMRFILECANCEL = -1;
  public final static int MSG_SHOW_RECORDPLAY = 2;

  public final static String RECORD_SAVE_PATH = PhoneUtils.getSDPath() + "/callme/record/";
  private final static String TAG = AudioUtils.class.getSimpleName();

  public static void skipAmrHead(DataInputStream dataInput) {
    final byte[] AMR_HEAD = new byte[] {0x23, 0x21, 0x41, 0x4D, 0x52, 0x0A};
    int result = -1;
    int state = 0;
    try {
      while (-1 != (result = dataInput.readByte())) {
        if (AMR_HEAD[0] == result) {
          state = (0 == state) ? 1 : 0;
        } else if (AMR_HEAD[1] == result) {
          state = (1 == state) ? 2 : 0;
        } else if (AMR_HEAD[2] == result) {
          state = (2 == state) ? 3 : 0;
        } else if (AMR_HEAD[3] == result) {
          state = (3 == state) ? 4 : 0;
        } else if (AMR_HEAD[4] == result) {
          state = (4 == state) ? 5 : 0;
        } else if (AMR_HEAD[5] == result) {
          state = (5 == state) ? 6 : 0;
        }
        if (6 == state) {
          break;
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "read mdat error...");
    }
  }

  /**
   * 音频前缀写入sdcard
   * @param path
   */
  public static boolean writeVoicePrefixToSdcard(String path) {
    InputStream in = BaseApplication.self.getResources().openRawResource(R.raw.send);
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(path);
      byte[] buff = new byte[1024];
      int read = 0;
      while ((read = in.read(buff)) > 0) {
        out.write(buff, 0, read);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      try {
        in.close();
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  /**
   * 合并AMR文件
   * 
   * @param tempAmrFiles
   * @return
   */
  public static File buildAmrFile(File tempFile, File amrFile) {
    List<File> tempAmrFiles = new ArrayList<File>();
    
    File prefixVoice = new File(PhoneUtils.getVoicePrefixOnSDPath("send.arm"));
    if(prefixVoice.exists()){
      tempAmrFiles.add(prefixVoice);
    }else{
      if(writeVoicePrefixToSdcard(PhoneUtils.getVoicePrefixOnSDPath("send.arm"))){
        tempAmrFiles.add(prefixVoice);
      }
    }
    tempAmrFiles.add(tempFile);

    if (tempAmrFiles == null || tempAmrFiles.isEmpty()) return null;
    // File amrFile = new File(RECORD_SAVE_PATH, UUID.randomUUID().toString() + ".amr");
    boolean hasError = false;
    FileOutputStream fos = null;
    RandomAccessFile ra = null;
    try {
      fos = new FileOutputStream(amrFile);
      for (int i = 0; i < tempAmrFiles.size(); i++) {
        ra = new RandomAccessFile(tempAmrFiles.get(i), "r");
        if (i != 0) {
          ra.seek(6);
        }
        byte[] buffer = new byte[1024 * 8];
        int len = 0;
        while ((len = ra.read(buffer)) != -1) {
          fos.write(buffer, 0, len);
        }
      }
    } catch (Exception e) {
      hasError = true;
    } finally {
      IOUtils.close(fos);
      IOUtils.close(ra);
    }
    return hasError ? null : amrFile;
  }

}
