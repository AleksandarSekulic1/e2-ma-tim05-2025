package ftn.ma.myapplication.domain;

public class LevelingManager {

    // Metoda koja računa koliko je XP poena potrebno za sledeći nivo
    // na osnovu formula iz specifikacije
    public static int calculateXpForNextLevel(int currentLevel) {
        if (currentLevel < 1) {
            // Početni uslov, pre nivoa 1
            return 200;
        }

        // Prvo izračunavamo XP potreban za TRENUTNI nivo
        int xpForCurrentLevel = 200;
        // Počinjemo od nivoa 2 jer je za nivo 1 fiksno 200
        for (int i = 2; i <= currentLevel; i++) {
            // Formula: XP prethodnog * 2 + XP prethodnog / 2
            int previousXp = xpForCurrentLevel;
            xpForCurrentLevel = previousXp * 2 + previousXp / 2;
            // Zaokruživanje na prvu narednu stotinu
            if (xpForCurrentLevel % 100 != 0) {
                xpForCurrentLevel = ((xpForCurrentLevel / 100) + 1) * 100;
            }
        }

        // Sada kada znamo XP za trenutni nivo, računamo za SLEDEĆI
        int xpForNextLevel = xpForCurrentLevel * 2 + xpForCurrentLevel / 2;
        // I opet zaokružujemo
        if (xpForNextLevel % 100 != 0) {
            xpForNextLevel = ((xpForNextLevel / 100) + 1) * 100;
        }

        return xpForNextLevel;
    }
}
