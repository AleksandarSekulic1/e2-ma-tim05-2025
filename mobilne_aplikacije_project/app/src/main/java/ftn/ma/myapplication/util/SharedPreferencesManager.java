package ftn.ma.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

public class SharedPreferencesManager {

    private static final String PREFS_NAME = "MyApplicationPrefs";
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
    private static final String KEY_MISSION_BOSS_HP = "mission_boss_hp";
    private static final String KEY_MISSION_TASK_COUNT_PREFIX = "mission_task_"; // Npr. mission_task_shop_purchase
    private static final String KEY_SIMULATED_DATE = "simulated_date";

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

    public static void saveMissionBossHp(Context context, int hp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_MISSION_BOSS_HP, hp).apply();
    }

    public static int getMissionBossHp(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_MISSION_BOSS_HP, -1); // Vraćamo -1 ako nije postavljeno
    }

    /**
     * Čuva napredak za specijalni zadatak, uključujući broj izvršenja i vreme.
     * Podaci se čuvaju kao string u formatu "broj;timestamp".
     */
    public static void saveSpecialTaskProgress(Context context, String taskKey, int count, long timestamp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String valueToSave = count + ";" + timestamp;
        prefs.edit().putString(KEY_MISSION_TASK_COUNT_PREFIX + taskKey, valueToSave).apply();
    }

    /**
     * Čita napredak za specijalni zadatak.
     * @return Pair<Integer, Long> gde je prvi element broj, a drugi timestamp.
     */
    public static Pair<Integer, Long> getSpecialTaskProgress(Context context, String taskKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedValue = prefs.getString(KEY_MISSION_TASK_COUNT_PREFIX + taskKey, "0;0");

        try {
            String[] parts = savedValue.split(";");
            int count = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            return new Pair<>(count, timestamp);
        } catch (Exception e) {
            // U slučaju greške, vraća podrazumevane vrednosti
            return new Pair<>(0, 0L);
        }
    }


    // Metoda koja resetuje samo napredak misije
    public static void resetMissionProgress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Moramo ručno obrisati sve ključeve vezane za misiju
        editor.remove(KEY_MISSION_BOSS_HP);
        // Primer brisanja ključeva za specijalne zadatke (dodati sve ključeve)
        editor.remove(KEY_MISSION_TASK_COUNT_PREFIX + "shop");
        editor.remove(KEY_MISSION_TASK_COUNT_PREFIX + "regular_hit");
        editor.remove(KEY_MISSION_TASK_COUNT_PREFIX + "easy_task");
        editor.remove(KEY_MISSION_TASK_COUNT_PREFIX + "hard_task");
        // ... itd. za sve specijalne zadatke

        editor.apply();
    }

    // --- NOVE METODE ZA SIMULACIJU DATUMA ---

    /**
     * Čuva izabrani simulirani datum kao timestamp.
     * @param context Kontekst aplikacije.
     * @param timestamp Vreme u milisekundama za simulirani datum.
     */
    public static void saveSimulatedDate(Context context, long timestamp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_SIMULATED_DATE, timestamp);
        editor.apply();
    }

    /**
     * Vraća sačuvani simulirani datum.
     * @param context Kontekst aplikacije.
     * @return Timestamp sačuvanog datuma, ili 0L ako nije sačuvan.
     */
    public static long getSimulatedDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_SIMULATED_DATE, 0L);
    }

    /**
     * Briše sačuvani simulirani datum, vraćajući aplikaciju na korišćenje stvarnog vremena.
     * @param context Kontekst aplikacije.
     */
    public static void clearSimulatedDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_SIMULATED_DATE);
        editor.apply();
    }
}