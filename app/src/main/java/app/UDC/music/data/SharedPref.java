package app.UDC.music.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import app.UDC.music.R;
import app.UDC.music.utils.Tools;


public class SharedPref {
    private Context context;
    private SharedPreferences sharedPreferences;

    public static final String THEME_COLOR_KEY = "app.lite.music.THEME_COLOR_KEY";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
    }

    /**
     * For theme color
     */
    public void setThemeColor(String color) {
        sharedPreferences.edit().putString(THEME_COLOR_KEY, color).commit();
    }

    public String getThemeColor() {
        return sharedPreferences.getString(THEME_COLOR_KEY, "");
    }

    public int getThemeColorInt() {
        if (getThemeColor().equals("")) {
            return context.getResources().getColor(R.color.colorPrimary);
        }
        return Color.parseColor(getThemeColor());
    }

    public int getThemeColorIntDarker() {
        if (getThemeColor().equals("")) {
            return Tools.colorDarker(context.getResources().getColor(R.color.colorPrimary));
        }
        return Tools.colorDarker(Color.parseColor(getThemeColor()));
    }

    /**
     * To save dialog permission state
     */
    public void setNeverAskAgain(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getNeverAskAgain(String key) {
        return sharedPreferences.getBoolean(key, false);
    }


    // Preference for first launch
    public void setIntersCounter(int counter) {
        sharedPreferences.edit().putInt("INTERS_COUNT", counter).apply();
    }

    public int getIntersCounter() {
        return sharedPreferences.getInt("INTERS_COUNT", 0);
    }

    public void clearIntersCounter() {
        sharedPreferences.edit().putInt("INTERS_COUNT", 0).apply();
    }
}
