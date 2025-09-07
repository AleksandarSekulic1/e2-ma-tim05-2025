// RewardManager.java
package ftn.ma.myapplication.util;

import android.content.Context;

public class RewardManager {

    /**
     * Računa 50% nagrade u novčićima od sledećeg regularnog bosa.
     * Primer 8: Ako si nivo 1, računa se 50% od nagrade za bosa nivoa 2. [cite: 308]
     */
    public static int calculateMissionCoinReward(Context context) {
        int currentUserLevel = SharedPreferencesManager.getUserLevel(context);
        int nextBossLevel = currentUserLevel + 1;

        // Nagrada za prvog bosa je 200 novčića, a za svakog sledećeg 20% više. [cite: 170]
        double nextBossReward = 200; // Nagrada za bosa nivoa 1

        // Računamo nagradu za bosa na sledećem nivou
        for (int i = 2; i <= nextBossLevel; i++) {
            nextBossReward *= 1.20;
        }

        // Nagrada iz misije je 50% od toga
        return (int) (nextBossReward / 2);
    }
}
