package ftn.ma.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ftn.ma.myapplication.data.model.User;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserStorage {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_LIST = "user_list";
    private static final String KEY_CURRENT_USER = "current_user_email";
    private static final Gson gson = new Gson();

    // Dodaj korisnika u listu (za registraciju)
    public static void saveUser(Context context, User user) {
        List<User> userList = getUserList(context);
        
        // Proveri da li korisnik već postoji (po email-u)
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getEmail().equals(user.getEmail())) {
                userList.set(i, user); // Ažuriraj postojećeg korisnika
                saveUserList(context, userList);
                return;
            }
        }
        
        // Dodeli novi jedinstveni ID ako ga nema
        if (user.getId() == 0) {
            int maxId = 0;
            for (User existingUser : userList) {
                if (existingUser.getId() > maxId) {
                    maxId = existingUser.getId();
                }
            }
            // Koristim Reflection da postavim ID jer nema setter
            try {
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.setInt(user, maxId + 1);
            } catch (Exception e) {
                // Fallback: koristi hash od email-a
                int id = Math.abs(user.getEmail().hashCode() % 10000) + 1;
                try {
                    java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.setInt(user, id);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
        
        // Dodaj novog korisnika
        userList.add(user);
        saveUserList(context, userList);
    }

    // Dobij listu svih korisnika
    public static List<User> getUserList(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_USER_LIST, null);
        if (json == null) return new ArrayList<>();
        
        Type listType = new TypeToken<ArrayList<User>>(){}.getType();
        return gson.fromJson(json, listType);
    }

    // Sačuvaj listu korisnika
    private static void saveUserList(Context context, List<User> userList) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_LIST, gson.toJson(userList)).apply();
    }

    // Pronađi korisnika po email-u
    public static User findUserByEmail(Context context, String email) {
        List<User> userList = getUserList(context);
        for (User user : userList) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    // Stare metode za kompatibilnost
    public static User getUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentEmail = prefs.getString(KEY_CURRENT_USER, null);
        if (currentEmail == null) {
            // Ako nema trenutnog korisnika, vrati prvog iz liste
            List<User> users = getUserList(context);
            return users.isEmpty() ? null : users.get(0);
        }
        return findUserByEmail(context, currentEmail);
    }

    // Postavi trenutnog korisnika
    public static void setCurrentUser(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENT_USER, email).apply();
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_USER_LIST).remove(KEY_CURRENT_USER).apply();
    }
}
