package ftn.ma.myapplication.domain;

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
}