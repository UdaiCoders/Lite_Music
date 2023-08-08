package app.UDC.music.data;

public class AppConfig {

    // flag for display ads
    public static final boolean ENABLE_ADS = true;

    public static boolean ADS_MAIN_INTERSTITIAL = ENABLE_ADS && true;

    public static long ADS_INTERSTITIAL_INTERVAL = 5; // in action

    public static boolean ADS_MAIN_BANNER = ENABLE_ADS && true;
}
