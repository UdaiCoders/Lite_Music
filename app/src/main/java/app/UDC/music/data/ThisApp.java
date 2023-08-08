package app.UDC.music.data;

import android.app.Application;

import app.UDC.music.advertise.AdNetworkHelper;

public class ThisApp extends Application {

    private static ThisApp mInstance;

    public static synchronized ThisApp get() {
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        AdNetworkHelper.init(this);
    }

}
