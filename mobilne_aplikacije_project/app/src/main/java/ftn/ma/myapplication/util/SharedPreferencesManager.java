package ftn.ma.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import ftn.ma.myapplication.data.model.SpecialMission;

public class SharedPreferencesManager {

    private static final String PREFS_NAME = "MyApplicationPrefs";
    // NOVI KLJUČEVI
    private static final String KEY_COMPLETED_MISSIONS = "completed_missions";
    private static final String PREFIX_MISSION_REWARD_CLAIMED = "mission_reward_claimed_";

    private static final String KEY_USER_LEVEL = "user_level";
    private static final String KEY_USER_XP = "user_xp";
    private static final String KEY_USER_PP = "user_pp";
    private static final String KEY_USER_COINS = "user_coins";
    private static final String KEY_BOSS_DEFEATED_PREFIX = "boss_defeated_";
    private static final String KEY_PERMANENT_PP_BONUS = "permanent_pp_bonus";
    // --- NOVI KLJUČ ---
    private static final String KEY_LAST_LEVEL_UP_DATE = "last_level_up_date";
    private static final String KEY_BOSS_CURRENT_HP_PREFIX = "boss_current_hp_";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    //private static final String KEY_BOSS_DEFEATED_PREFIX = "boss_defeated_";
    //private static final String KEY_BOSS_CURRENT_HP_PREFIX = "boss_current_hp_";

    // --- NOVI PREFIKSI ZA MISIJE (SA ID-JEM) ---
    private static final String PREFIX_MISSION_MEMBERS = "mission_members_";
    private static final String PREFIX_MISSION_HP = "mission_hp_";
    private static final String PREFIX_MISSION_MAX_HP = "mission_max_hp_";
    private static final String KEY_SIMULATED_DATE = "simulated_date";
    private static final String PREFIX_MISSION_START_DATE = "mission_start_date_"; // NOVO
    private static final String PREFIX_MISSION_EXPIRED = "mission_expired_"; // NOVO
    private static final String PREFIX_MISSION_ACTION_COUNT = "mission_action_count_"; // NOVO
    private static final String KEY_ACTIVE_MISSION_ID = "active_mission_id"; // NOVO
    private static final String PREFIX_MISSION_ACTION_COUNT_DAILY = "mission_action_daily_"; // Za dnevne
    private static final String PREFIX_MISSION_ACTION_COUNT_TOTAL = "mission_action_total_"; // Za ukupne
    private static final String KEY_ALL_MISSIONS = "all_special_missions";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

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

    public static void saveBossDefeatedStatus(Context context, int bossLevel, boolean isDefeated) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Kreiramo dinamički ključ, npr. "boss_defeated_1"
        editor.putBoolean(KEY_BOSS_DEFEATED_PREFIX + bossLevel, isDefeated);
        editor.apply();
    }

    public static boolean isBossDefeated(Context context, int bossLevel) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Ako ključ ne postoji, podrazumevana vrednost je false (nije pobeđen)
        return prefs.getBoolean(KEY_BOSS_DEFEATED_PREFIX + bossLevel, false);
    }

    public static void savePermanentPpBonus(Context context, int bonus) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PERMANENT_PP_BONUS, bonus);
        editor.apply();
    }

    public static int getPermanentPpBonus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_PERMANENT_PP_BONUS, 0); // Početni bonus je 0
    }

    public static void saveLastLevelUpDate(Context context, long timestamp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_LAST_LEVEL_UP_DATE, timestamp);
        editor.apply();
    }

    public static long getLastLevelUpDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_LAST_LEVEL_UP_DATE, 0);
    }
    // --- NOVE METODE ZA PAMĆENJE HP-A BOSA ---
    public static void saveBossCurrentHp(Context context, int bossLevel, int hp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BOSS_CURRENT_HP_PREFIX + bossLevel, hp);
        editor.apply();
    }

    public static int getBossCurrentHp(Context context, int bossLevel) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Vraćamo -1 ako HP nije sačuvan, da bismo znali da treba da uzmemo max HP
        return prefs.getInt(KEY_BOSS_CURRENT_HP_PREFIX + bossLevel, -1);
    }

    // --- NOVE METODE za status prijave ---
    public static void setUserLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // --- NOVA METODA ZA RESET ---
    public static void resetAllUserData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Čuvamo samo login status, sve ostalo brišemo
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        editor.clear(); // Briše SVE podatke
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn); // Vraćamo samo login status

        editor.apply();
    }

    // --- NOVE METODE ZA SPECIJALNU MISIJU ---

    // =================================================================================
    // METODE ZA UPRAVLJANJE VIŠE SPECIJALNIH MISIJA (NOVA LOGIKA)
    // =================================================================================

    /**
     * Čuva broj članova saveza za određenu misiju.
     */
    public static void saveMissionAllianceMembers(Context context, int missionId, int members) {
        getPrefs(context).edit().putInt(PREFIX_MISSION_MEMBERS + missionId, members).apply();
    }

    /**
     * Vraća broj članova saveza za određenu misiju. Vraća 0 ako nije sačuvano.
     */
    public static int getMissionAllianceMembers(Context context, int missionId) {
        return getPrefs(context).getInt(PREFIX_MISSION_MEMBERS + missionId, 0);
    }

    /**
     * Čuva trenutni HP bosa za određenu misiju.
     */
    public static void saveMissionBossHp(Context context, int missionId, int hp) {
        getPrefs(context).edit().putInt(PREFIX_MISSION_HP + missionId, hp).apply();
    }

    /**
     * Vraća trenutni HP bosa za određenu misiju. Vraća -1 ako nije sačuvano.
     */
    public static int getMissionBossHp(Context context, int missionId) {
        return getPrefs(context).getInt(PREFIX_MISSION_HP + missionId, -1);
    }

    /**
     * Čuva maksimalni HP bosa za određenu misiju. Korisno za praćenje promena broja članova.
     */
    public static void saveMissionMaxHp(Context context, int missionId, int maxHp) {
        getPrefs(context).edit().putInt(PREFIX_MISSION_MAX_HP + missionId, maxHp).apply();
    }

    /**
     * Vraća maksimalni HP bosa za određenu misiju. Vraća -1 ako nije sačuvano.
     */
    public static int getMissionMaxHp(Context context, int missionId) {
        return getPrefs(context).getInt(PREFIX_MISSION_MAX_HP + missionId, -1);
    }

    /** Čuva ID trenutno aktivne misije. -1 znači da nijedna nije aktivna. */
    public static void setActiveMissionId(Context context, int missionId) {
        getPrefs(context).edit().putInt(KEY_ACTIVE_MISSION_ID, missionId).apply();
    }

    public static int getActiveMissionId(Context context) {
        return getPrefs(context).getInt(KEY_ACTIVE_MISSION_ID, -1);
    }

    /** Čuva i čita da li je misija trajno istekla. */
    public static void saveMissionExpiredStatus(Context context, int missionId, boolean hasExpired) {
        getPrefs(context).edit().putBoolean(PREFIX_MISSION_EXPIRED + missionId, hasExpired).apply();
    }

    public static boolean getMissionExpiredStatus(Context context, int missionId) {
        return getPrefs(context).getBoolean(PREFIX_MISSION_EXPIRED + missionId, false);
    }

    /** Čuva i čita datum početka za svaku misiju posebno. */
    public static void saveMissionStartDate(Context context, int missionId, long startDate) {
        getPrefs(context).edit().putLong(PREFIX_MISSION_START_DATE + missionId, startDate).apply();
    }

    public static long getMissionStartDate(Context context, int missionId) {
        return getPrefs(context).getLong(PREFIX_MISSION_START_DATE + missionId, 0);
    }

    /** Čuva i čita broj izvršenja akcije za određeni dan. */
    public static void saveDailyActionCount(Context context, int missionId, String memberName, String actionKey, long date, int count) {
        String dateString = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(date));
        String key = PREFIX_MISSION_ACTION_COUNT_DAILY + missionId + "_" + memberName + "_" + actionKey + "_" + dateString;
        getPrefs(context).edit().putInt(key, count).apply();
    }

    public static int getDailyActionCount(Context context, int missionId, String memberName, String actionKey, long date) {
        String dateString = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(date));
        String key = PREFIX_MISSION_ACTION_COUNT_DAILY + missionId + "_" + memberName + "_" + actionKey + "_" + dateString;
        return getPrefs(context).getInt(key, 0);
    }

    /**
     * Čuva broj izvršenja određene akcije za određenog člana u misiji.
     * Primer ključa: "contribution_1_student_shop"
     */
    public static void saveMemberActionCount(Context context, int missionId, String memberName, String actionKey, int count) {
        String key = PREFIX_MISSION_ACTION_COUNT_TOTAL + missionId + "_" + memberName + "_" + actionKey;
        getPrefs(context).edit().putInt(key, count).apply();
    }

    public static int getMemberActionCount(Context context, int missionId, String memberName, String actionKey) {
        String key = PREFIX_MISSION_ACTION_COUNT_TOTAL + missionId + "_" + memberName + "_" + actionKey;
        return getPrefs(context).getInt(key, 0);
    }

    // =================================================================================
    // METODE ZA SIMULACIJU DATUMA
    // =================================================================================

    public static void saveSimulatedDate(Context context, long timestamp) {
        getPrefs(context).edit().putLong(KEY_SIMULATED_DATE, timestamp).apply();
    }

    public static long getSimulatedDate(Context context) {
        return getPrefs(context).getLong(KEY_SIMULATED_DATE, 0L);
    }

    public static void clearSimulatedDate(Context context) {
        getPrefs(context).edit().remove(KEY_SIMULATED_DATE).apply();
    }

    // NOVO: Generičke metode koje su nedostajale
    public static void saveLong(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return getPrefs(context).getLong(key, defaultValue);
    }

    // =================================================================================
    // NOVE METODE ZA NAGRADE I BEDŽEVE
    // =================================================================================

    /** Čuva i čita da li je nagrada za misiju preuzeta. */
    public static void saveMissionRewardClaimed(Context context, int missionId, boolean isClaimed) {
        getPrefs(context).edit().putBoolean(PREFIX_MISSION_REWARD_CLAIMED + missionId, isClaimed).apply();
    }

    public static boolean isMissionRewardClaimed(Context context, int missionId) {
        return getPrefs(context).getBoolean(PREFIX_MISSION_REWARD_CLAIMED + missionId, false);
    }

    /** Čuva i čita broj završenih misija (za bedževe). */
    public static void saveCompletedMissionsCount(Context context, int count) {
        getPrefs(context).edit().putInt(KEY_COMPLETED_MISSIONS, count).apply();
    }

    public static int getCompletedMissionsCount(Context context) {
        return getPrefs(context).getInt(KEY_COMPLETED_MISSIONS, 0);
    }

    public static void saveMissions(Context context, List<SpecialMission> missions) {
        Gson gson = new Gson();
        String json = gson.toJson(missions);
        getPrefs(context).edit().putString(KEY_ALL_MISSIONS, json).apply();
    }

    public static List<SpecialMission> loadMissions(Context context) {
        String json = getPrefs(context).getString(KEY_ALL_MISSIONS, null);
        if (json == null) {
            return new ArrayList<>(); // Vrati praznu listu ako ništa nije sačuvano
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<SpecialMission>>() {}.getType();
        return gson.fromJson(json, type);
    }
}