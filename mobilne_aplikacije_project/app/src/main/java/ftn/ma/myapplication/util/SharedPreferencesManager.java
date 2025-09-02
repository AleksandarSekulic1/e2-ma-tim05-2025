package ftn.ma.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREFS_NAME = "MyApplicationPrefs";
    private static final String KEY_USER_LEVEL = "user_level";
    private static final String KEY_USER_XP = "user_xp";

    // Metoda za čuvanje nivoa korisnika
    public static void saveUserLevel(Context context, int level) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_LEVEL, level);
        editor.apply();
    }

    // Metoda za čitanje nivoa korisnika
    public static int getUserLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Vraćamo nivo 1 ako još ništa nije sačuvano
        return prefs.getInt(KEY_USER_LEVEL, 1);
    }

    // Metoda za čuvanje XP-a korisnika
    public static void saveUserXp(Context context, int xp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_XP, xp);
        editor.apply();
    }

    // Metoda za čitanje XP-a korisnika
    public static int getUserXp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Vraćamo 0 XP ako još ništa nije sačuvano
        return prefs.getInt(KEY_USER_XP, 0);
    }
}