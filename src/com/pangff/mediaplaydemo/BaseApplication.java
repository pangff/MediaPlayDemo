package com.pangff.mediaplaydemo;
import android.app.Application;
import android.os.Handler;

public class BaseApplication extends Application {

  final static String TAG = "BaseApplication";
  public boolean homekeyClicked = false;
  public static BaseApplication self;
  public Handler handlerCommon = new Handler();
  /**
   * 是否调用过追踪权限的接口
   */
  public boolean hasRequestTraceRight = false;
  private String currentViewName;


  public String getCurrentViewName() {
    return currentViewName;
  }

  public void setCurrentViewName(String currentViewName) {
    this.currentViewName = currentViewName;
  }


  public void onCreate() {
    super.onCreate();
    setApplication(this);
  }

  public static void setApplication(BaseApplication application) {
    self = application;
  }

  public static BaseApplication getBaseApplication() {
    return self;
  }

}
