package ftn.ma.myapplication.ui.game;

import android.content.Context;
import java.util.List;
import java.util.Random;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class FakeMemberSimulator {

    private static final Random random = new Random();

    /**
     * Simulira dnevni napredak za listu lažnih članova.
     * @return Ukupna šteta koju su naneli svi lažni članovi u ovoj simulaciji.
     */
    public static int simulateDailyProgress(Context context, int missionId, List<String> fakeMemberNames, long simulatedDate) {
        int totalDamageDone = 0;

        for (String memberName : fakeMemberNames) {
            // Svaki član ima 70% šanse da bude aktivan tog dana
            if (random.nextInt(100) < 70) {
                // Slanje poruke (max 1 dnevno)
                // Proveravamo da li je već poslao poruku ovog dana

                // --- ISPRAVKA 1 ---
                // Razdvojili smo 'memberName' i 'actionKey' ("message") u dva argumenta
                int messageCountToday = SharedPreferencesManager.getDailyActionCount(context, missionId, memberName, "message", simulatedDate);

                if (messageCountToday == 0) {
                    if (random.nextBoolean()) { // 50% šanse da pošalje poruku
                        // --- ISPRAVKA 2 ---
                        // I ovde smo razdvojili argumente
                        SharedPreferencesManager.saveDailyActionCount(context, missionId, memberName, "message", simulatedDate, 1);
                        totalDamageDone += 4;
                    }
                }

                // Ostale akcije
                totalDamageDone += performAction(context, missionId, memberName, "shop", 2, 5);
                totalDamageDone += performAction(context, missionId, memberName, "hit", 2, 10);
                totalDamageDone += performAction(context, missionId, memberName, "easy_task", 1, 10);
                totalDamageDone += performAction(context, missionId, memberName, "hard_task", 4, 6);
            }
        }
        return totalDamageDone;
    }

    /**
     * Pomoćna metoda koja simulira jednu vrstu akcije za jednog člana.
     */
    private static int performAction(Context context, int missionId, String memberName, String actionKey, int damagePerAction, int maxTotalActions) {
        int currentTotalCount = SharedPreferencesManager.getMemberActionCount(context, missionId, memberName, actionKey);
        if (currentTotalCount >= maxTotalActions) {
            return 0; // Već je ispunio ukupnu kvotu
        }

        // Bot će pokušati da uradi 0, 1 ili 2 akcije ovog tipa danas
        int actionsToDoToday = random.nextInt(3);
        int damageDone = 0;

        for (int i = 0; i < actionsToDoToday; i++) {
            if (currentTotalCount < maxTotalActions) {
                currentTotalCount++;
                damageDone += damagePerAction;
            } else {
                break; // Prekini ako je dostigao max u toku petlje
            }
        }

        if (damageDone > 0) {
            SharedPreferencesManager.saveMemberActionCount(context, missionId, memberName, actionKey, currentTotalCount);
        }
        return damageDone;
    }
}
