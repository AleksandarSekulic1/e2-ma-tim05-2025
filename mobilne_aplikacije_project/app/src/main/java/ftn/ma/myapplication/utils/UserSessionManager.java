package ftn.ma.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Klasa za upravljanje korisničkim sesijama
 */
public class UserSessionManager {
    
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_TIME = "login_time";
    
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    
    public UserSessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }
    
    /**
     * Kreira korisničku sesiju
     */
    public void createSession(int userId, String username, String email) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }
    
    /**
     * Proverava da li je korisnik ulogovan
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Dobija ID trenutno ulogovanog korisnika
     */
    public int getCurrentUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Dobija username trenutno ulogovanog korisnika
     */
    public String getCurrentUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }
    
    /**
     * Dobija email trenutno ulogovanog korisnika
     */
    public String getCurrentUserEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }
    
    /**
     * Dobija vreme logovanja
     */
    public long getLoginTime() {
        return prefs.getLong(KEY_LOGIN_TIME, 0);
    }
    
    /**
     * Ažurira username korisnika
     */
    public void updateUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }
    
    /**
     * Ažurira email korisnika
     */
    public void updateEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }
    
    /**
     * Briše korisničku sesiju (logout)
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Proverava da li je sesija aktivna (nije istekla)
     */
    public boolean isSessionActive() {
        if (!isLoggedIn()) {
            return false;
        }
        
        long loginTime = getLoginTime();
        long currentTime = System.currentTimeMillis();
        long sessionDuration = currentTime - loginTime;
        
        // Sesija traje 30 dana (u milisekundama)
        long maxSessionDuration = 30L * 24 * 60 * 60 * 1000;
        
        return sessionDuration < maxSessionDuration;
    }
    
    /**
     * Dobija sve podatke o trenutnom korisniku
     */
    public UserSessionData getCurrentUserData() {
        if (!isLoggedIn()) {
            return null;
        }
        
        return new UserSessionData(
            getCurrentUserId(),
            getCurrentUsername(),
            getCurrentUserEmail(),
            getLoginTime()
        );
    }
    
    /**
     * Klasa za enkapsulaciju podataka o korisničkoj sesiji
     */
    public static class UserSessionData {
        private int userId;
        private String username;
        private String email;
        private long loginTime;
        
        public UserSessionData(int userId, String username, String email, long loginTime) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.loginTime = loginTime;
        }
        
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public long getLoginTime() { return loginTime; }
    }
}