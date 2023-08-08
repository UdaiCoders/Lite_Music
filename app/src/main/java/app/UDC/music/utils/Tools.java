package app.UDC.music.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import androidx.appcompat.app.ActionBar;
import android.view.Window;
import android.view.WindowManager;

import app.UDC.music.ActivityMain;
import app.UDC.music.R;
import app.UDC.music.data.SharedPref;

public class Tools {

    public static String[] ALL_REQUIRED_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static boolean isLolipopOrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public static void systemBarLolipop(Activity act) {
        if (isLolipopOrHigher()) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Tools.colorDarker(new SharedPref(act).getThemeColorInt()));
        }
    }

    public static void setActionBarColor(Context ctx, ActionBar actionbar) {
        ColorDrawable colordrw = new ColorDrawable(new SharedPref(ctx).getThemeColorInt());
        actionbar.setBackgroundDrawable(colordrw);
    }

    public static int colorDarker(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static boolean cekConnection(Context context) {
        return Tools.isConnectingToInternet(context);
    }

    private static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    public static void rateAction(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    public static void restartApplication(Activity activity) {
        activity.finish();
        Intent i = new Intent(activity, ActivityMain.class);
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        activity.startActivity(i);
    }

    public static boolean needRequestPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

}
