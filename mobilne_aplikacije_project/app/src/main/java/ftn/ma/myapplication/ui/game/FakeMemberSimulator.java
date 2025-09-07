// FakeMemberSimulator.java
package ftn.ma.myapplication.ui.game;

import android.content.Context;
import java.util.List;
import java.util.Random;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class FakeMemberSimulator {

    private static final Random random = new Random();

    /**
     * Simulira dnevni napredak za listu lažnih članova.
     * Metoda sada samo beleži akcije, ne vraća štetu.
     */
    public static void simulateDailyProgress(Context context, int missionId, List<String> fakeMemberNames, long simulatedDate) {
        for (String memberName : fakeMemberNames) {
            // ISPRAVKA: Povećana šansa da bot bude aktivan na 90%
            if (random.nextInt(100) < 90) {

                // Slanje poruke (max 1 dnevno) - šansa ostaje 90%
                int messageDailyCount = SharedPreferencesManager.getDailyActionCount(context, missionId, memberName, "message", simulatedDate);
                if (messageDailyCount == 0 && random.nextInt(100) < 90) {
                    SharedPreferencesManager.saveDailyActionCount(context, missionId, memberName, "message", simulatedDate, 1);
                }

                // Bonus bez greške (10% šanse po misiji)
                int bonusCount = SharedPreferencesManager.getMemberActionCount(context, missionId, memberName, "bonus");
                if (bonusCount == 0 && random.nextInt(100) < 10) {
                    SharedPreferencesManager.saveMemberActionCount(context, missionId, memberName, "bonus", 1);
                }

                // Ostale akcije
                performAction(context, missionId, memberName, "shop", 5);
                performAction(context, missionId, memberName, "hit", 10);
                performAction(context, missionId, memberName, "easy_task", 10);
                performAction(context, missionId, memberName, "hard_task", 6);
            }
        }
    }

    private static void performAction(Context context, int missionId, String memberName, String actionKey, int maxTotalActions) {
        int currentTotalCount = SharedPreferencesManager.getMemberActionCount(context, missionId, memberName, actionKey);
        if (currentTotalCount >= maxTotalActions) {
            return;
        }

        // ISPRAVKA: Bot će sada uraditi 1, 2 ili 3 akcije, umesto 0-2.
        // Ovo osigurava mnogo brži napredak.
        int actionsToDoToday = 1 + random.nextInt(3);

        if (actionsToDoToday > 0) {
            int newTotalCount = Math.min(currentTotalCount + actionsToDoToday, maxTotalActions);
            if (newTotalCount > currentTotalCount) {
                SharedPreferencesManager.saveMemberActionCount(context, missionId, memberName, actionKey, newTotalCount);
            }
        }
    }
}