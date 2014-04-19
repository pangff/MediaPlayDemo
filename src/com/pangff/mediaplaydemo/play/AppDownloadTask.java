package com.pangff.mediaplaydemo.play;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

/**
 * 应用下载类，支持断点续传、普通下载，断点续传优先；<br>
 * 支持1对N的事件通知
 * 
 * @author wangmoulin
 */
public class AppDownloadTask extends AsyncTask<AppDownloadRequest, Integer, Integer> {
  private static final int MAX_RETRY_TIMES = 3;
  private static final String TAG = "AppDownloadTask";
  public static final int ERROR = -1; // 未知错误
  public static final int ERROR_INVALID_INPUT = -2; // 非法输入参数
  public static final int ERROR_NETWORK = -3; // 无网络错误
  public static final int ERROR_NO_SD = -4; // 没有SD卡
  public static final int ERROR_NOT_ENOUGH_DISK = -5; // SD卡空间不足
  public static final int ERROR_FAILED_GOT_HEAD = -6; // 无法获取下载信息
  public static final int USER_CANCEL = -7; // 用户取消下载
  public static final int OK = 0; // 下载完成
  public static final int PROGRESS = 1; // 下载中


  private HttpClient httpClient = null;

  // 通常服务器都支持GET方法,此处使用HttpGet
  private HttpGet httpGet = null;
  private HttpResponse response = null;

  AppDownloadRequest request;

  AtomicBoolean abort = new AtomicBoolean(false);

  public void abort() {
    Log.d(TAG, "user abort");
    abort.set(true);
    onCancelled();
    publisher.notifyDataChanged(new DownloadProgressEvent(USER_CANCEL, request));
  }

  public void publishProgressNotice() {
    publishProgress(PROGRESS);
  }

  @Override
  protected Integer doInBackground(AppDownloadRequest... params) {
    publishProgress(PROGRESS);
    Log.d(TAG, "begin");

    request = params[0];
    if (request == null || TextUtils.isEmpty(request.downloadUrl)) {
      return ERROR_INVALID_INPUT;
    }

    if (!PhoneUtils.isSDMounted()) {
      return ERROR_NO_SD;
    }

    httpClient = new DefaultHttpClient();
    httpClient.getParams().setIntParameter("http.connection.timeout", 15000);
    httpGet = new HttpGet(request.downloadUrl);

    // 检查是否支持断点续传，失败后多次尝试
    Log.d(TAG, "begin isAcceptRange");
    boolean acceptRange = false;
    int times = 0;
    do {
      try {
        acceptRange = isAcceptRange(httpClient, request);
        break;
      } catch (Exception e) {
        Log.e(TAG, "", e);
        times++;
      }
      if (abort.get()) {
        return USER_CANCEL;
      }
    } while (times < MAX_RETRY_TIMES);
    
    Log.d(TAG, "end isAcceptRange");


    if (times == MAX_RETRY_TIMES) {
      return ERROR_FAILED_GOT_HEAD;
    }



    // 磁盘空间不足
    if (!PhoneUtils.isSDFreeSpaceLarger(request.appSize)) {
      return ERROR_NOT_ENOUGH_DISK;
    }



    File tmpDir = new File(PhoneUtils.getVoiceTmpOnSDPath());

    // 检查安装文件是否已经存在
    File appFile = new File(request.appFile);
    appFile.getParentFile().mkdirs();

    if (appFile.exists() && appFile.length() == request.appSize) {
      // 已经存在
      request.receivedCount = request.appSize;
      return OK;
    } else {
      // 不符合条件的安装文件，删除
      if (appFile.exists()) {
        appFile.delete();
      }
    }

    File tmpFile = new File(tmpDir, appFile.getName() + ".tmp");

    boolean exists = tmpFile.exists();
    if (acceptRange) {
      long start = exists ? tmpFile.length() : 0;
      request.receivedCount = start;
      StringBuilder sb = new StringBuilder();
      sb.append("bytes=").append(start).append('-').append(request.appSize);
      httpGet.addHeader("RANGE", sb.toString());
    } else {
      // 不支持断点续传的情况下，删除已下载的历史文件
      if (exists) {
        tmpFile.delete();
      }
    }

    try {
      Log.d(TAG, "begin getting data");
      response = httpClient.execute(httpGet);
      int statusCode = response.getStatusLine().getStatusCode();

      // log日志，用于debug
      Header[] allHeaders = response.getAllHeaders();
      Log.d(TAG, "statusCode:" + statusCode);
      for (Header header : allHeaders) {
        Log.d(TAG, header.getName() + ":" + header.getValue());
      }


      if (statusCode == 206 || (statusCode == 200 && !acceptRange)) {// 下载数据中
        InputStream inputStream = response.getEntity().getContent();

        // 创建输出文件
        FileOutputStream outputStream = null;
        try {
          outputStream = new FileOutputStream(tmpFile, true);

          int count = 0;
          byte[] buffer = new byte[1024 * 8];
          long start = System.currentTimeMillis();
          while ((count = inputStream.read(buffer, 0, buffer.length)) > 0) {

            // 此循环内操作时间最长，每次读取后检查cancel标识，及时推出
            if (abort.get()) {
              publisher.notifyDataChanged(new DownloadProgressEvent(USER_CANCEL, request));
              break;
            }
            // 写文件，更新统计，发送事件
            outputStream.write(buffer, 0, count);
            long end = System.currentTimeMillis();
            request.speed = end - start == 0 ? 0 : count * 1000.0f / (end - start);
            start = end;
            request.receivedCount += count;
            publishProgress(PROGRESS);
          }
          if (abort.get()) {
            return USER_CANCEL;
          } else {
            // 重命名临时文件
            tmpFile.renameTo(appFile);
            return OK;
          }
        } finally {
          IOUtils.close(outputStream);
        }

      } else if (statusCode == 416) { // 下载数据完成
        publisher.notifyDataChanged(new DownloadProgressEvent(PROGRESS, request));
        // 重命名临时文件
        tmpFile.renameTo(appFile);
        return OK;
      }
      if (abort.get()) {
        return USER_CANCEL;
      } else {
        return ERROR_NETWORK;
      }
    } catch (Exception e) {
      Log.e(TAG, "", e);
      if (abort.get()) {
        return USER_CANCEL;
      } else {
        return ERROR_NETWORK;
      }
    } finally {
      releaseHttpResource();
    }

  }

  long lastUpdateTimeMillis;

  @Override
  protected void onProgressUpdate(Integer... progress) {
    if (lastUpdateTimeMillis == -1 || System.currentTimeMillis() - lastUpdateTimeMillis > 1000) {
      publisher.notifyDataChanged(new DownloadProgressEvent(PROGRESS, request));
      lastUpdateTimeMillis = System.currentTimeMillis();
    }
  }

  HttpHead httpHead = null;
  /**
   * 获取下载文件信息：文件大小以及是否支持断点续传
   */
  private boolean isAcceptRange(HttpClient httpClient, AppDownloadRequest request)
      throws IOException, ClientProtocolException, Exception {
    
    try {
      httpHead = new HttpHead(request.downloadUrl);
      HttpResponse response = httpClient.execute(httpHead);
      // 获取HTTP状态码
      int statusCode = response.getStatusLine().getStatusCode();

      if (statusCode != 200) throw new Exception("资源不存在!");

      for (Header header : response.getAllHeaders()) {
        Log.d(TAG, header.getName() + ":" + header.getValue());
      }

      // 获取文件长度
      Header[] headers = response.getHeaders("Content-Length");
      if (headers.length > 0) {
        request.appSize = Long.valueOf(headers[0].getValue());
        DownloadProgressCache.getInstance().put(request.downloadUrl, request.appSize);
      }

      httpHead.abort();

      // 尝试断点续传
      httpHead = new HttpHead(request.downloadUrl);
      httpHead.addHeader("Range", "bytes=0-" + request.appSize);
      response = httpClient.execute(httpHead);
      if (response.getStatusLine().getStatusCode() == 206) {
        request.acceptRanges = true;
      } else {
        request.acceptRanges = false;
      }
      httpHead.abort();
      return request.acceptRanges;
    } finally {
      if (httpHead != null) {
        httpHead.abort();
      }
    }
  }

  @Override
  protected final void onCancelled() {
    releaseHttpResource();
    // Log.d(TAG,"onCancelled()");
  }

  /**
   * 回收资源
   */
  private void releaseHttpResource() {
    Log.d(TAG, "releaseHttpResource");
    if (httpHead != null) {
      httpHead.abort();
    }
    
    if (httpGet != null) {
      httpGet.abort();
    }
    
    if (httpClient != null) {
      httpClient.getConnectionManager().shutdown();
    }
  }

  protected final void onPostExecute(Integer status) {
    publisher.notifyDataChanged(new DownloadProgressEvent(status, request));
  }

  DownloadProgressListener lastListener;

  /**
   * 增加事件的订阅者
   * 
   * @param listener DownloadProgressListener
   */
  public void addDownloadProgressListener(DownloadProgressListener listener) {
    publisher.register(listener);
    this.lastListener = listener;
  }

  /**
   * 1对N的事件发布
   */
  private EventPublish<DownloadProgressListener, DownloadProgressEvent> publisher =
      new EventPublish<DownloadProgressListener, DownloadProgressEvent>() {

        @Override
        public void notifyEvent(DownloadProgressListener listener, DownloadProgressEvent event) {
          listener.onStatusChanged(event.status, event.result);
        }
      };

  /**
   * 下载进度事件
   */
  public static class DownloadProgressEvent {
    int status;
    AppDownloadRequest result;

    public DownloadProgressEvent(int status, AppDownloadRequest result) {
      this.status = status;
      this.result = result;
    }
  }

  /**
   * 下载进度事件的订阅者
   */
  public interface DownloadProgressListener {
    public void onStatusChanged(int status, AppDownloadRequest result);
  }

}
