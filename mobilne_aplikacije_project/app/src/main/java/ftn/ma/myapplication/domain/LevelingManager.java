package ftn.ma.myapplication.domain;

import android.content.Context;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class LevelingManager {

    /**
     * Metoda koja računa ukupan broj XP poena potreban da bi se dostigao određeni nivo.
     * Npr. za Nivo 1 treba 200 XP, za Nivo 2 treba 500 XP ukupno, itd.
     * @param level Nivo za koji tražimo prag
     * @return Ukupan broj XP poena potreban za taj nivo
     */
    public static int getXpNeededForLevel(int level) {
        if (level <= 1) {
            return 200;
        }

        // Počinjemo od praga za Nivo 1
        int xpThreshold = 200;
        // Računamo pragove za svaki nivo do onog koji tražimo
        for (int i = 2; i <= level; i++) {
            int previousThreshold = xpThreshold;
            // Formula: Prag prethodnog * 2 + Prag prethodnog / 2
            xpThreshold = previousThreshold * 2 + previousThreshold / 2;
            // Zaokruživanje na prvu narednu stotinu
            if (xpThreshold % 100 != 0) {
                xpThreshold = ((xpThreshold / 100) + 1) * 100;
            }
        }
        return xpThreshold;
    }


    public static int calculateTotalPpForLevel(int level) {
        if (level <= 1) {
            return 40;
        }
        double currentPp = 40.0;
        for (int i = 2; i <= level; i++) {
            currentPp = currentPp + (0.75 * currentPp);
        }
        return (int) Math.round(currentPp);
    }

    public static int calculateBossHpForLevel(int level) {
        if (level <= 1) {
            return 200;
        }
        int currentHp = 200;
        for (int i = 2; i <= level; i++) {
            currentHp = currentHp * 2 + currentHp / 2;
        }
        return currentHp;
    }

    // --- NOVA METODA za računanje nagrade u novčićima ---
    // Specifikacija: 200 za prvog bosa, +20% za svakog sledećeg
    public static int calculateCoinReward(int level) {
        if (level <= 1) {
            return 200;
        }
        double coinReward = 200.0;
        // Počinjemo od nivoa 2
        for (int i = 2; i <= level; i++) {
            // Povećavamo za 20%
            coinReward *= 1.20;
        }
        return (int) Math.round(coinReward);
    }

    public static int getNextBossToFight(Context context, int currentUserLevel) {
        // Borba se dešava nakon level up-a, tako da je minimalni nivo korisnika 2.
        if (currentUserLevel <= 1) {
            // Vraćamo 1 kao podrazumevanu vrednost u slučaju greške
            return 1;
        }

        // Proveravamo sve prethodne nivoe, počevši od nivoa 1
        for (int level = 1; level < currentUserLevel; level++) {
            if (!SharedPreferencesManager.isBossDefeated(context, level)) {
                // Pronašli smo prvog neporaženog bosa, njega treba napasti!
                return level;
            }
        }

        // Ako su svi prethodni bosovi pobeđeni, borimo se sa bosom poslednjeg završenog nivoa.
        // Npr. ako je korisnik nivo 3, poslednji završeni je nivo 2.
        return currentUserLevel - 1;
    }

    /**
     * Računa XP vrednost za Težinu zadatka na osnovu nivoa korisnika.
     * @param difficulty Težina zadatka.
     * @param userLevel Trenutni nivo korisnika.
     * @return Izračunata XP vrednost.
     */
    public static int getDynamicXpForDifficulty(Task.Difficulty difficulty, int userLevel) {
        double baseXp;
        switch (difficulty) {
            case VEOMA_LAK: baseXp = 1; break;
            case LAK:       baseXp = 3; break;
            case TEZAK:     baseXp = 7; break;
            case EKSTREMNO_TEZAK: baseXp = 20; break;
            default: return 0;
        }

        // Formula se primenjuje za svaki nivo IZNAD prvog
        for (int i = 1; i < userLevel; i++) {
            baseXp = baseXp + baseXp / 2;
        }
        return (int) Math.round(baseXp);
    }

    /**
     * Računa XP vrednost za Bitnost zadatka na osnovu nivoa korisnika.
     * @param importance Bitnost zadatka.
     * @param userLevel Trenutni nivo korisnika.
     * @return Izračunata XP vrednost.
     */
    public static int getDynamicXpForImportance(Task.Importance importance, int userLevel) {
        double baseXp;
        switch (importance) {
            case NORMALAN:        baseXp = 1; break;
            case VAZAN:           baseXp = 3; break;
            case EKSTREMNO_VAZAN: baseXp = 10; break;
            case SPECIJALAN:      baseXp = 100; break;
            default: return 0;
        }

        // Formula: XP bitnosti za prethodni nivo + XP bitnosti za prethodni nivo / 2 [cite: 143]
        // Primenjuje se za svaki nivo IZNAD prvog
        for (int i = 1; i < userLevel; i++) {
            baseXp = baseXp + baseXp / 2;
        }
        // Zaokružiti dobijenu vrednost [cite: 144]
        return (int) Math.round(baseXp);
    }
}