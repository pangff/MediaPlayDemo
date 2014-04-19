package com.pangff.mediaplaydemo.play;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask.Status;

public class DownloadMgr {

  /**
   * 下载任务的缓存，支持弱引用
   */
  static Map<String, WeakReference<AppDownloadTask>> downloadTaskCache =
      new HashMap<String, WeakReference<AppDownloadTask>>();

  /**
   * 缓存下载任务
   * 
   * @param url 下载地址
   * @param task AppDownloadTask
   */
  public static void cache(String url, AppDownloadTask task) {
    downloadTaskCache.put(url, new WeakReference<AppDownloadTask>(task));
  }

  /**
   * 通过下载地址，获取下载任务
   * 
   * @param url 下载地址
   * @return AppDownloadTask
   */
  public static AppDownloadTask getTaskByUrl(String url) {
    WeakReference<AppDownloadTask> wr = downloadTaskCache.get(url);
    AppDownloadTask task = wr == null ? null : wr.get();
    if (task != null && task.getStatus() == Status.FINISHED) {
      downloadTaskCache.remove(url);
      return null;
    }

    return task;
  }

  /**
   * 通过下载地址，移除下载任务
   * 
   * @param url 下载地址
   */
  public static void removeTaskByUrl(String url) {
    downloadTaskCache.remove(url);
  }
}
