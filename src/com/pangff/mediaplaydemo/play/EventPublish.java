package com.pangff.mediaplaydemo.play;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class EventPublish<T, Event> {
  final List<WeakReference<T>> listeners = new ArrayList<WeakReference<T>>();

  public void register(T listener) {
    synchronized (listeners) {
      // 检查是否存在此Listener
      for (int i = listeners.size() - 1; i >= 0; i--) {
        WeakReference<T> wr = listeners.get(i);
        if (wr == null) {
          listeners.remove(i);
          continue;
        }
        T listenerInList = wr.get();
        if (listenerInList == null) {
          listeners.remove(i);
          continue;
        }
        if (listenerInList == listener) {
          return;
        }
      }
      //没有，则添加
      listeners.add(new WeakReference<T>(listener));
    }
  }

  public void unregister(T listener) {
    synchronized (listeners) {
      for (int i = listeners.size() - 1; i >= 0; i--) {
        WeakReference<T> wr = listeners.get(i);
        if (wr == null) {
          listeners.remove(i);
          continue;
        }
        T listenerInList = wr.get();
        if (listenerInList == null) {
          listeners.remove(i);
          continue;
        }
        if (listenerInList == listener) {
          listeners.remove(i);
        }
      }
    }
  }

  public void notifyDataChanged(Event event) {
    synchronized (listeners) {
      for (int i = listeners.size() - 1; i >= 0; i--) {
        WeakReference<T> wr = listeners.get(i);
        if (wr == null) {
          listeners.remove(i);
          continue;
        }
        T listener = wr.get();
        if (listener == null) {
          listeners.remove(i);
          continue;
        }
        notifyEvent(listener, event);
      }
    }
  }

  public abstract void notifyEvent(T listener, Event event);
}
