package org.freeland.wildscanlaos;

import android.app.Application;

//import com.firebase.*;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by nor on 4/11/2016.
 */
public class App extends Application {
    private static Application instance;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        instance = this;
//        DebugLogConfig.enable();
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }
}
