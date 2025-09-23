package ftn.ma.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import ftn.ma.myapplication.data.model.User;

public class UserStorage {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER = "user_data";
    private static final Gson gson = new Gson();

    public static void saveUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER, gson.toJson(user)).apply();
    }

    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return gson.fromJson(json, User.class);
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER).apply();
    }
}
