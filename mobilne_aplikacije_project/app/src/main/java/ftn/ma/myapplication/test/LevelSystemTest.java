package ftn.ma.myapplication.test;

import ftn.ma.myapplication.data.model.User;

/**
 * Test klasa za demonstraciju level sistema sa PP formulama
 */
public class LevelSystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== LEVEL SYSTEM TEST SA PP FORMULAMA ===\n");
        
        // Test XP potrebnih za nivoe
        System.out.println("XP potreban za svaki nivo:");
        for (int level = 1; level <= 5; level++) {
            int xpRequired = User.getXPRequiredForLevel(level);
            int totalXP = User.getTotalXPForLevel(level);
            System.out.println("Nivo " + level + ": " + xpRequired + " XP (ukupno: " + totalXP + " XP)");
        }
        
        System.out.println("\n=== PP FORMULA VALIDACIJA ===");
        
        // Test PP formula prema specifikaciji
        System.out.println("PP za svaki nivo (prema formuli PP_prethodni + 3/4 * PP_prethodni):");
        for (int level = 1; level <= 5; level++) {
            int ppForLevel = User.getPPForLevel(level);
            int totalPP = User.getTotalPPForLevel(level);
            System.out.println("Nivo " + level + ": +" + ppForLevel + " PP (ukupno: " + totalPP + " PP)");
        }
        
        // Validacija protiv specifikacije
        System.out.println("\n=== VALIDACIJA PROTIV SPECIFIKACIJE ===");
        
        // Nivo 1: 0 PP (počinje sa 0)
        // Nivo 2: 40 PP (prvi level up)
        // Nivo 3: 40 + 3/4*40 = 40 + 30 = 70 PP
        // Nivo 4: 70 + 3/4*70 = 70 + 52.5 = 122.5 ≈ 123 PP
        
        int[] expectedPP = {0, 40, 70, 123}; // Očekivane vrednosti iz specifikacije
        System.out.println("Poređenje sa specifikacijom:");
        for (int i = 1; i <= 4; i++) {
            int calculated = User.getPPForLevel(i);
            int expected = expectedPP[i-1];
            String status = (calculated == expected) ? "✅ TAČNO" : "❌ GREŠKA";
            System.out.println("Nivo " + i + ": Računato=" + calculated + ", Očekivano=" + expected + " " + status);
        }
        
        System.out.println("\n=== XP MULTIPLIER TEST ===");
        
        // Test XP multiplikatora kroz nivoe
        System.out.println("XP multiplikatori kroz nivoe:");
        for (int level = 1; level <= 5; level++) {
            User testUser = new User("test@test.com", "testuser", "hash", 0, true, 0);
            testUser.setLevel(level);
            int importance = testUser.getImportanceXPMultiplier();
            int difficulty = testUser.getDifficultyXPMultiplier();
            System.out.println("Nivo " + level + " - Bitnost: +" + importance + " XP, Težina: +" + difficulty + " XP");
        }
        
        System.out.println("\n=== LEVEL UP SIMULATION SA PP ===");
        
        // Simulacija levelovanja sa PP
        User player = new User("player@test.com", "player", "hash", 0, true, 0);
        player.setXp(0);
        player.setLevel(1);
        player.setCoins(0);
        player.setPowerPoints(0);
        
        System.out.println("Početno stanje: Nivo " + player.getLevel() + ", XP: " + player.getXp() + 
                          ", PP: " + player.getPowerPoints() + ", Coins: " + player.getCoins());
        
        // Simuliraj dodavanje XP-a za level up-ove
        int[] xpAdds = {200, 500, 750, 1000}; // Dovoljno za prve nivoe
        
        for (int xp : xpAdds) {
            int oldLevel = player.getLevel();
            int oldPP = player.getPowerPoints();
            boolean leveledUp = player.addXP(xp);
            
            System.out.println("\nDodato " + xp + " XP:");
            System.out.println("  - Novo stanje: Nivo " + player.getLevel() + ", XP: " + player.getXp() + 
                              ", PP: " + player.getPowerPoints() + ", Coins: " + player.getCoins());
            System.out.println("  - Do sledećeg nivoa: " + player.getXPToNextLevel() + " XP");
            System.out.println("  - Titula: " + player.getTitle());
            
            if (leveledUp) {
                int ppGained = player.getPowerPoints() - oldPP;
                System.out.println("  🎉 LEVEL UP! Nivo " + oldLevel + " → " + player.getLevel() + 
                                  " (+PP: " + ppGained + ")");
            }
        }
        
        System.out.println("\n=== FINALNI PP TEST ===");
        System.out.println("Očekivani PP za nivo " + player.getLevel() + ": " + player.getExpectedPPForCurrentLevel());
        System.out.println("Stvarni PP: " + player.getPowerPoints());
        String ppStatus = (player.getPowerPoints() == player.getExpectedPPForCurrentLevel()) ? "✅ ISPRAVNO" : "❌ NEISPRAVNO";
        System.out.println("Status: " + ppStatus);
    }
}