package com.pangff.mediaplaydemo.play;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

public class IOUtils {

  public static void close(Cursor c) {
    if (null != c) {
      c.close();
    }
  }

  public static void close(Closeable c) {
    try {
      if (null != c) {
        c.close();
      }
    } catch (IOException ignore) {}
  }

  public static void close(SQLiteStatement stmt) {
    if (null != stmt) {
      stmt.close();
    }
  }

  public static void close(Socket socket) {
    try {
      if (null != socket) {
        socket.close();
      }
    } catch (IOException ignore) {}
  }


}
