package com.pangff.mediaplaydemo.play;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.util.LruCache;

import com.pangff.mediaplaydemo.BaseApplication;

public class DownloadProgressCache {
  private DownloadProgressCache() {
    loadCache();
  }

  private static class SingletonHolder {
    static DownloadProgressCache instance = new DownloadProgressCache();
  }

  public static DownloadProgressCache getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * key: apk url <br>
   * value: apk file size
   */
  LruCache<String, Long> cache = new LruCache<String, Long>(20);

  public void saveCache() {
    SharedPreferences sp =
        BaseApplication.self.getSharedPreferences("DownloadProgressCache", Context.MODE_PRIVATE);
    Editor edit = sp.edit();
    edit.clear();
    Map<String, Long> all = cache.snapshot();
    for (Iterator<Entry<String, Long>> iterator = all.entrySet().iterator(); iterator.hasNext();) {
      Entry<String, Long> entry = iterator.next();
      edit.putLong(entry.getKey(), entry.getValue());
    }
    edit.commit();
  }

  @SuppressWarnings("unchecked")
  public void loadCache() {
    SharedPreferences sp =
        BaseApplication.self.getSharedPreferences("DownloadProgressCache", Context.MODE_PRIVATE);

    Map<String, Long> all = (Map<String, Long>) sp.getAll();
    for (Iterator<Entry<String, Long>> iterator = all.entrySet().iterator(); iterator.hasNext();) {
      Entry<String, Long> entry = iterator.next();
      cache.put(entry.getKey(), entry.getValue());
    }
  }

  public Long get(String key) {
    Long l = cache.get(key);
    return l == null ? Long.valueOf(0L) : l;
  }

  public void put(String key, Long value) {
    cache.put(key, value);
  }
}
