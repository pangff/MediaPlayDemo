package com.pangff.mediaplaydemo.play;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * A class used to perform periodical tasks, specified inside a runnable object. A task interval may
 * be specified
 */
public class RepeatTaskUtil {
  private static final String TAG = "RepeatTaskUtil";
  // Create a Handler that uses the Main Looper to run in
  private Handler mHandler = new Handler(Looper.getMainLooper());

  private Runnable mStatusChecker;
  private int updateInterval;
  // 用于互斥访问，当一个task进入时，其他task不能执行
  ReentrantLock lock = new ReentrantLock();
  // 停止标识，
  AtomicBoolean stop = new AtomicBoolean(false);


  public RepeatTaskUtil(final Runnable task, int interval) {
    this.updateInterval = interval;
    mStatusChecker = new Runnable() {
      long lastStartTimestamp = -1;

      @Override
      public void run() {
        if (stop.get()) return; // 检查停止标识，退出定时任务


        lock.lock();
        try {
          long current = System.currentTimeMillis();
          if (lastStartTimestamp != -1 && current - lastStartTimestamp < updateInterval) { // 异常情况：未到定时时间，延时执行
            Log.d(TAG, "task is delayed");
            mHandler.removeCallbacks(mStatusChecker);
            mHandler.postDelayed(this, current - lastStartTimestamp);
            return;
          } else {
            lastStartTimestamp = System.currentTimeMillis(); // 设置最新的开始时间
          }
          // 执行前确保消息队列中无候选任务，结束的时候再添加
          mHandler.removeCallbacks(mStatusChecker);

          // 执行传入的任务
          task.run();
        } catch (Exception e) {
          Log.e(TAG, "", e); // 记录异常
        } finally {
          lock.unlock();
          // 添加前确保消息队列中无候选任务
          mHandler.removeCallbacks(mStatusChecker);
          if (!stop.get()) { // 检查停止标识，添加定时任务
            mHandler.postDelayed(this, updateInterval);
          }
        }
      }
    };
  }


  /**
   * 启动定时任务，可多次启动，不影响定时任务的结果
   */
  public synchronized void start() {
    stop.getAndSet(false);
    /**
     * 情况0： 首次启动，A能够获取锁，A执行，A的后继A2执行剩下的循环；
     * 情况1： 任务A在执行，启动任务B无法获取锁，B被忽略；A的后继A2执行剩下的循环；<br>
     * 情况2： 任务A已经释放锁，A的后继A2存在于消息队列中；启动任务B，A2被删除；<br>
     *      情况2.1：B任务执行时未到定时时间，B被延时执行；<br>
     *      情况2.2：B任务执行时到定时时间，B被执行，B的后继B2执行剩下的循环<br>
     */
    if (lock.tryLock()) {
      mHandler.removeCallbacks(mStatusChecker);
      mHandler.postDelayed(mStatusChecker, 0);
      lock.unlock();
    }
  }

  /**
   * 启动定时任务，可设置时间间隔，可多次启动，不影响定时任务的结果
   * @param interval int
   */
  public synchronized void start(int interval) {
    this.updateInterval = interval;
    start();
  }


  /**
   * 停止定时任务
   */
  public synchronized void stop() {
    stop.getAndSet(true); //设置停止标识
    mHandler.removeCallbacks(mStatusChecker); //移除消息队列中的候选任务
  }
}
