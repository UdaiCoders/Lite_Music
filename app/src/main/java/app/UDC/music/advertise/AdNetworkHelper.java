package app.UDC.music.advertise;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import app.UDC.music.R;
import app.UDC.music.data.AppConfig;
import app.UDC.music.data.GDPR;
import app.UDC.music.data.SharedPref;

public class AdNetworkHelper {

    private static final String TAG = AdNetworkHelper.class.getSimpleName();

    private final Activity activity;
    private final SharedPref sharedPref;

    //Interstitial
    private InterstitialAd adMobInterstitialAd;

    public AdNetworkHelper(Activity activity) {
        this.activity = activity;
        sharedPref = new SharedPref(activity);
    }

    @SuppressLint("MissingPermission")
    public static void init(Context context) {
        MobileAds.initialize(context);
    }

    public void showGDPR() {
        GDPR.updateConsentStatus(activity);
    }

    @SuppressLint("MissingPermission")
    public void loadBannerAd(boolean enable) {
        if (!enable) return;
        LinearLayout ad_container = activity.findViewById(R.id.ad_container);
        ad_container.removeAllViews();
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(activity)).build();
        ad_container.setVisibility(View.GONE);
        AdView adView = new AdView(activity);
        adView.setAdUnitId(activity.getString(R.string.banner_ad_unit_id));
        ad_container.addView(adView);
        adView.setAdSize(getAdmobBannerSize());
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                ad_container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Code to be executed when an ad request fails.
                ad_container.setVisibility(View.GONE);
            }
        });
    }

    public void loadInterstitialAd(boolean enable) {
        if (!enable) return;
        InterstitialAd.load(activity, activity.getString(R.string.interstitial_ad_unit_id), new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                adMobInterstitialAd = interstitialAd;
                adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        adMobInterstitialAd = null;
                        loadInterstitialAd(enable);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.d(TAG, "The ad was shown.");
                        sharedPref.setIntersCounter(0);
                    }
                });
                Log.i(TAG, "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.i(TAG, loadAdError.getMessage());
                adMobInterstitialAd = null;
                Log.d(TAG, "Failed load AdMob Interstitial Ad");
            }
        });
    }

    public boolean showInterstitialAd(boolean enable) {
        if (!enable) return false;
        int counter = new SharedPref(activity).getIntersCounter();
        if (counter > AppConfig.ADS_INTERSTITIAL_INTERVAL) {
            if (adMobInterstitialAd == null) return false;
            adMobInterstitialAd.show(activity);
            return true;
        } else {
            sharedPref.setIntersCounter(sharedPref.getIntersCounter() + 1);
        }
        return false;
    }

    private AdSize getAdmobBannerSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }


}
