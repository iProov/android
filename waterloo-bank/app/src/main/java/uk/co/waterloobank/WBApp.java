package uk.co.waterloobank;

import android.app.Application;
import com.yariksoffice.lingver.Lingver;


public class WBApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Lingver.init(this, "en");
    }
}
