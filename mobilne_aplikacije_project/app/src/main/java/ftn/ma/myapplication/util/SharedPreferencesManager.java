package ftn.ma.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREFS_NAME = "MyApplicationPrefs";
    private static final String KEY_USER_LEVEL = "user_level";
    private static final String KEY_USER_XP = "user_xp";
    private static final String KEY_USER_PP = "user_pp";
    private static final String KEY_USER_COINS = "user_coins";

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

    public static void saveUserPp(Context context, int pp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_PP, pp);
        editor.apply();
    }

    public static int getUserPp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Vraćamo 40 PP (početna vrednost za nivo 1) ako ništa nije sačuvano
        return prefs.getInt(KEY_USER_PP, 40);
    }

    // --- NOVE METODE za novčiće ---
    public static void saveUserCoins(Context context, int coins) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_COINS, coins);
        editor.apply();
    }

    public static int getUserCoins(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_USER_COINS, 0); // Početna vrednost 0
    }
}