import java.util.HashMap;
import java.util.Scanner;

class Game {
    //tw
    Player player = new Player();
    int playerX = 5;
    int playerY = 5;
    int floor = 1;
    MoonCycle moonCycle = new MoonCycle();
    HashMap<String, Enemy> enemyDB;

    Map map;
    Scanner sc = new Scanner(System.in);

    public Game() {
        this.enemyDB = EnemyLoader.loadEnemies("enemylist.csv");
        this.map = new Map(playerX, playerY, enemyDB);
        this.sc = new Scanner(System.in);
    }

    public void initializePlayer() {
        int bonusPoints = 5; // Give them 5 extra points to start
    
        while (bonusPoints > 0) {
            clearScreen();
            System.out.println("--- CHARACTER CREATION ---");
            System.out.println("Level: " + player.level);
            System.out.println("Points remaining: " + bonusPoints);
            System.out.println("1. Strength: " + player.strength);
            System.out.println("2. Magic:    " + player.magic);
            System.out.println("3. Vitality: " + player.vitality);
            System.out.println("4. Agility:  " + player.agility);
            System.out.println("5. Luck:     " + player.luck);
            System.out.print("Select a stat to increase (1-5): ");
        
            try {
                int choice = Integer.parseInt(sc.nextLine());
                if (choice >= 1 && choice <= 5) {
                player.addStat(choice);
                bonusPoints--;
                }
            } catch (Exception e) {
                System.out.println("Invalid input!");
            }
        }
        // Set initial HP/SP to full after stats are finalized
        player.currentHp = player.getMaxHp();
        player.currentSp = player.getMaxSp();
    }

    public void start() {
        initializePlayer();

        while (true) {

            clearScreen();

            System.out.println("Floor: " + floor + " | Macca: " + player.macca);
            System.out.println("Moon: " + moonCycle.getCurrentPhase());
            map.draw(playerX, playerY);

            System.out.print("Move (WASD) or open (M)enu: ");
            String input = sc.nextLine().toLowerCase();

            if(input.equals("m")){
                openMainMenu();
            }else{
                move(input);
            }

            // Check if player died during move/battle
            if (!player.isAlive()) {
                // This happens if startBattle returned false (Player chose "Try Again")
                resetGame();
                continue; 
            }

            if (map.isExit(playerX, playerY)) {
                nextFloor();
            }
        }
    }

    private void move(String input) {
        boolean moved = false;

        switch (input) {
        case "w": if (playerY > 0) { playerY--; moved = true; } break;
        case "s": if (playerY < 9) { playerY++; moved = true; } break;
        case "a": if (playerX > 0) { playerX--; moved = true; } break;
        case "d": if (playerX < 9) { playerX++; moved = true; } break;
    }

    if (moved) {
        moonCycle.playerMoved();
    }

        Enemy enemy = map.getEnemyAt(playerX, playerY);
        if (enemy != null) {
            startBattle(enemy);
        }

    }

    private void nextFloor() {
        System.out.println("You found the exit!\n");

        floor++;
        playerX = 5;
        playerY = 5;

        map = new Map(playerX, playerY, enemyDB);
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private boolean startBattle(Enemy enemy) {
        // Enemy HP using your formula: (Lvl + Vit) * 6
    // Assuming level 1 for basic enemies for now
    int enemyMaxHP = (1 + enemy.vitality) * 6;
    int enemyCurrentHP = enemyMaxHP;

    while (enemyCurrentHP > 0 && player.isAlive()) {
        clearScreen();
        
        // --- BATTLE UI ---
        System.out.println("========================================");
        System.out.println("            BATTLE: " + enemy.name);
        System.out.println("========================================");
        System.out.println(String.format("  %-20s HP: %d/%d", enemy.name, enemyCurrentHP, enemyMaxHP));
        System.out.println("\n");
        System.out.println(String.format("  %-20s HP: %d/%d  SP: %d/%d", "PLAYER", player.currentHp, player.getMaxHp(), player.currentSp, player.getMaxSp()));
        System.out.println("========================================");
        System.out.println("  1. Attack    2. Magic    3. Items   4.Talk");
        System.out.println("========================================");
        System.out.print("Choose an action: ");

        String choice = sc.nextLine();
        
        // --- PLAYER TURN ---
        if (choice.equals("1")) {
            // Physical Formula: (Lvl + Str) * Power / 15 (Using Power 40 for Lunge)
            int damage = (player.level + player.strength) * 40 / 15;
            enemyCurrentHP -= damage;
            System.out.println("\n> You lunged at " + enemy.name + " for " + damage + " damage!");
        } else if (choice.equals("2")) {
            System.out.println("\n> You haven't learned any skills yet!");
            continue; // Skip to next iteration without taking an enemy hit
        }else if (choice.equals("3")) {
    
            System.out.println("> You have no items to use!");
            pause();
            continue; // Don't let the enemy attack if you just looked at an empty bag
        } else if (choice.equals("4")) {
            System.out.println("\n> " + enemy.name + " stares at you intensely...");
            // Negotiation logic goes here later!
        } else {
            System.out.println("\n> Invalid command!");
            continue;
        }

        if (enemyCurrentHP <= 0) break;

        // --- ENEMY TURN ---
        // Enemy Physical Formula: (Lvl + Str) * Power / 15
        int enemyDamage = (1 + enemy.strength) * 40 / 15;
        player.takeDamage(enemyDamage);
        System.out.println("> " + enemy.name + " attacks! You took " + enemyDamage + " damage.");
        
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

        // --- POST BATTLE ---
        if (player.isAlive()) {
            // Reward calculation based on enemy stats
            int gainedExp = (enemy.strength + enemy.vitality) * 2;
            int gainedMacca = (enemy.luck + enemy.agility) * 10;

            player.exp += gainedExp;
            player.macca += gainedMacca;

            System.out.println("\nVictory!");
            System.out.println("Gained " + gainedExp + " EXP and " + gainedMacca + " Macca.");

            // Check for Level Up
            while (player.exp >= 100) {
            player.exp -= 100;
                handleLevelUpMenu();
            }

            map.removeEnemy(playerX, playerY);
            System.out.println("Press Enter to return to map...");
            sc.nextLine();
            return true;
            }else{
                clearScreen();
                System.out.println("========================================");
                System.out.println("           YOU ARE DEAD");
                System.out.println("========================================");
                System.out.println("Your journey has reached its end...");
                System.out.println("\n1. Try Again");
                System.out.println("2. Quit");
                System.out.print("\nChoice: ");
        
                String choice = sc.nextLine();
                return !choice.equals("1"); // Returns false if they want to play again
            }
        }

        private void handleLevelUpMenu() {
            player.levelUp();
            boolean pointSpent = false;

            while (!pointSpent) {
                clearScreen();
                System.out.println("========================================");
                System.out.println("            LEVEL UP! (Lv. " + player.level + ")");
                System.out.println("========================================");
                System.out.println("Assign 1 Stat Point:");
                System.out.println("1. St: " + player.strength + " (Physical Damage)");
                System.out.println("2. Ma: " + player.magic    + " (MP & Magic Power)");
                System.out.println("3. Vi: " + player.vitality + " (Max HP)");
                System.out.println("4. Ag: " + player.agility  + " (Accuracy/Evasion)");
                System.out.println("5. Lu: " + player.luck     + " (Magic Evasion/Recovery)");
                System.out.print("\nChoice: ");

                try {
                    int choice = Integer.parseInt(sc.nextLine());
                    if (choice >= 1 && choice <= 5) {
                        player.addStat(choice);
                        pointSpent = true;
                        System.out.println("\nStat point allocated!");
                        try { Thread.sleep(1000); } catch (Exception e) {}
                    }
                } catch (Exception e) {
                    System.out.println("Invalid choice.");
                }
            }
        }

    public void resetGame() {
        this.player = new Player(); // Fresh stats
        this.playerX = 5;
        this.playerY = 5;
        this.floor = 1;
        this.moonCycle = new MoonCycle(); // Reset moon phases
        this.map = new Map(playerX, playerY, enemyDB); // Fresh map generation
        initializePlayer(); // Let them re-allocate their starting level 5 points
    }

    private void openMainMenu() {
    boolean inMenu = true;
    while (inMenu) {
        clearScreen();
        System.out.println("========================================");
        System.out.println("             CAMP MENU");
        System.out.println("========================================");
        System.out.println(String.format(" Lv. %-2d  HP: %d/%d  SP: %d/%d", 
            player.level, player.currentHp, player.getMaxHp(), player.currentSp, player.getMaxSp()));
        System.out.println(" Macca: " + player.macca);
        System.out.println("----------------------------------------");
        System.out.println(" 1. Status (Detailed Stats)");
        System.out.println(" 2. Inventory (Items)");
        System.out.println(" 3. Return to Map");
        System.out.print("\nChoice: ");

        String choice = sc.nextLine();
        switch (choice) {
            case "1": showDetailedStatus(); break;
            case "2": System.out.println("\nInventory is empty..."); pause(); break;
            case "3": inMenu = false; break;
        }
    }
}

    private void showDetailedStatus() {
        clearScreen();
        System.out.println("========================================");
        System.out.println("           PLAYER STATUS");
        System.out.println("========================================");
        System.out.println(" Strength:  " + player.strength);
        System.out.println(" Magic:     " + player.magic);
        System.out.println(" Vitality:  " + player.vitality);
        System.out.println(" Agility:   " + player.agility);
        System.out.println(" Luck:      " + player.luck);
        System.out.println("----------------------------------------");
        System.out.println(" EXP: " + player.exp + " / 100");
        System.out.println("\nPress Enter to return...");
        sc.nextLine();
    }

    private void pause() {
        System.out.println("Press Enter to continue...");
        sc.nextLine();
    }
}