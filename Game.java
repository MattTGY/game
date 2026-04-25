import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

class Game {
    Player player = new Player();
    int playerX = 5;
    int playerY = 5;
    int floor = 1;
    MoonCycle moonCycle = new MoonCycle();
    HashMap<String, Enemy> enemyDB;
    HashMap<String, Item> chestItemDB;

    Map map;
    Scanner sc = new Scanner(System.in);

    public Game() {
        this.enemyDB = EnemyLoader.loadEnemies("enemylist.csv");
        this.chestItemDB = ItemLoader.loadItems("ChestItems.csv");
        this.map = new Map(playerX, playerY, enemyDB);
        this.map.spawnChests(3);
    }

    public void initializePlayer() {
        clearScreen();
        System.out.println("--- WELCOME TO THE LABYRINTH ---");
        System.out.print("Load previous save? (y/n): ");
        if (sc.nextLine().toLowerCase().equals("y")) {
            loadGame();
            return; 
        }

        int bonusPoints = 5;
        while (bonusPoints > 0) {
            clearScreen();
            System.out.println("--- CHARACTER CREATION ---");
            System.out.println("Points remaining: " + bonusPoints);
            System.out.println("1. Strength: " + player.strength);
            System.out.println("2. Vitality: " + player.vitality);
            System.out.println("3. Luck:     " + player.luck);
            System.out.print("Select stat (1-3): ");

            try {
                int choice = Integer.parseInt(sc.nextLine());
                if (choice >= 1 && choice <= 3) {
                    player.addStat(choice);
                    bonusPoints--;
                }
            } catch (Exception e) { System.out.println("Invalid input!"); }
        }
        player.currentHp = player.getMaxHp();
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
            if (input.equals("m")) openMainMenu();
            else move(input);

            if (!player.isAlive()) { resetGame(); continue; }
            if (map.isExit(playerX, playerY)) nextFloor();
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
            Enemy enemy = map.getEnemyAt(playerX, playerY);
            if (enemy != null) startBattle(enemy);
        }
    }

    private void startBattle(Enemy enemy) {
        int scalingTier = floor / 2; 
        int enemyMaxHP = ((1 + enemy.vitality) * 6) + (scalingTier * 20);
        int enemyCurrentHP = enemyMaxHP;
        int scaledStr = enemy.strength + (scalingTier * 2);

        enemy.burnTicks = 0;
        enemy.skipTicks = 0;

        while (enemyCurrentHP > 0 && player.isAlive()) {
            clearScreen();
            System.out.println("BATTLE: " + enemy.name + " | FLOOR: " + floor);
            System.out.println(String.format("%-20s HP: %d/%d", enemy.name, enemyCurrentHP, enemyMaxHP));
            System.out.println(String.format("%-20s HP: %d/%d", "PLAYER", player.currentHp, player.getMaxHp()));
            System.out.println("----------------------------------------");
            System.out.println("1. Attack  2. Magic  3. Items  4. Talk");
            System.out.print("Action: ");

            String choice = sc.nextLine();
            if (choice.equals("1")) {
                int damage = (player.level + player.strength) * 40 / 15;
                enemyCurrentHP -= damage;
                System.out.println("> You hit for " + damage + "!");
            } else if (choice.equals("2")) {
                Item card = selectSkillCard();
                if (card != null) {
                    enemyCurrentHP -= card.value;
                    if (card.name.toLowerCase().contains("agi")) enemy.burnTicks = 3;
                    if (card.name.toLowerCase().contains("zio") || card.name.toLowerCase().contains("bufu")) enemy.skipTicks = 1;
                    player.inventory.remove(card);
                } else continue;
            } else if (choice.equals("3")) {
                if (handleInBattleHealing()) continue;
            } else if (choice.equals("4")) {
                if (handleNegotiation(enemy)) return;
            } else continue;

            if (enemyCurrentHP <= 0) break;

            // ENEMY TURN
            if (enemy.burnTicks > 0) {
                enemyCurrentHP -= 10;
                System.out.println(">> Burn damage: -10");
                enemy.burnTicks--;
                if (enemyCurrentHP <= 0) break;
            }

            if (enemy.skipTicks > 0) {
                System.out.println(">> " + enemy.name + " is stunned!");
                enemy.skipTicks--;
            } else {
                int enemyDamage = (1 + scaledStr) * 40 / 15;
                player.takeDamage(enemyDamage);
                System.out.println("> " + enemy.name + " deals " + enemyDamage + " damage!");
            }
            pause();
        }
        handleBattleEnd(enemy);
    }

    private boolean handleInBattleHealing() {
        List<Item> heals = new ArrayList<>();
        for (Item i : player.inventory) if (i.type.equals("HealHP")) heals.add(i);
        if (heals.isEmpty()) { System.out.println("> No healing items!"); pause(); return true; }

        System.out.println("--- USE ITEM ---");
        for (int i = 0; i < heals.size(); i++) System.out.println((i+1) + ". " + heals.get(i).name);
        System.out.print("Choice (0 for Back): ");
        try {
            int h = Integer.parseInt(sc.nextLine());
            if (h == 0) return true;
            player.useItem(heals.get(h - 1));
            pause();
            return false; 
        } catch (Exception e) { return true; }
    }

    private boolean handleNegotiation(Enemy enemy) {
        MoonCycle.Phase ph = moonCycle.getCurrentPhase();
        
        if (ph == MoonCycle.Phase.FULL) { 
            System.out.println("> The enemy is blinded by moon-rage! They won't talk!"); 
            pause(); 
            return false; 
        }
        if (ph == MoonCycle.Phase.NEW) { 
            processNegotiationSuccess(enemy); 
            return true; 
        }
        
        int chance = 20 + (player.luck * 2);
        if (new Random().nextInt(100) < chance) { 
            processNegotiationSuccess(enemy); 
            return true; 
        } else { 
            System.out.println("> Negotiation failed!"); 
            pause(); 
            return false; 
        }
    }

    private void processNegotiationSuccess(Enemy enemy) {
        List<Item> loot = new ArrayList<>(chestItemDB.values());
        Item gift = loot.get(new Random().nextInt(loot.size()));
        player.inventory.add(gift);
        System.out.println("> Received " + gift.name + "!");
        map.removeEnemy(playerX, playerY);
        pause();
    }

    private void handleBattleEnd(Enemy enemy) {
        if (player.isAlive()) {
            int gainedExp = (enemy.strength + enemy.vitality) * 2;
            int gainedMacca = (enemy.luck * 20); // Balanced to use Luck only
            player.exp += gainedExp; player.macca += gainedMacca;
            System.out.println("\nVictory! Gained " + gainedExp + " EXP.");
            while (player.exp >= 100) { player.exp -= 100; handleLevelUpMenu(); }
            map.removeEnemy(playerX, playerY);
            pause();
        }
    }

    private void saveGame() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("savegame.txt"))) {
            writer.println(floor);
            writer.println(player.level);
            writer.println(player.strength);
            writer.println(player.vitality);
            writer.println(player.luck);
            writer.println(player.macca);
            writer.println(player.exp);
            System.out.println("Save successful!");
            pause();
        } catch (IOException e) { System.out.println("Save failed."); }
    }

    private void loadGame() {
        try (Scanner fs = new Scanner(new File("savegame.txt"))) {
            floor = fs.nextInt();
            player.level = fs.nextInt();
            player.strength = fs.nextInt();
            player.vitality = fs.nextInt();
            player.luck = fs.nextInt();
            player.macca = fs.nextInt();
            player.exp = fs.nextInt();
            player.currentHp = player.getMaxHp();
            System.out.println("Load successful!");
            pause();
        } catch (Exception e) { System.out.println("No save found."); pause(); }
    }

    private void openMainMenu() {
        boolean inMenu = true;
        while (inMenu) {
            clearScreen();
            System.out.println("--- CAMP MENU ---");
            System.out.println("Floor: " + floor + " | HP: " + player.currentHp + "/" + player.getMaxHp());
            System.out.println("1. Status  2. Inventory  3. Save  4. Exit");
            String choice = sc.nextLine();
            switch (choice) {
                case "1": showDetailedStatus(); break;
                case "2": openInventoryMenu(); break;
                case "3": saveGame(); break;
                case "4": inMenu = false; break;
            }
        }
    }

    private void showDetailedStatus() {
        clearScreen();
        System.out.println("--- STATUS ---");
        System.out.println("St: " + player.strength + " | Vi: " + player.vitality + " | Lu: " + player.luck);
        System.out.println("EXP: " + player.exp + "/100");
        pause();
    }

    private void nextFloor() {
        floor++; playerX = 5; playerY = 5;
        map = new Map(playerX, playerY, enemyDB);
        map.spawnChests(3);
        System.out.println("Descending to Floor " + floor + "...");
        pause();
    }

    private void clearScreen() { System.out.print("\033[H\033[2J"); System.out.flush(); }
    private void pause() { System.out.println("Press Enter..."); sc.nextLine(); }

    private void openInventoryMenu() {
        clearScreen();
        for (int i = 0; i < player.inventory.size(); i++) System.out.println((i+1) + ". " + player.inventory.get(i).name);
        System.out.println("0. Back");
        try {
            int c = Integer.parseInt(sc.nextLine()) - 1;
            if (c != -1) player.useItem(player.inventory.get(c));
        } catch (Exception e) {}
    }

    private Item selectSkillCard() {
        List<Item> cards = new ArrayList<>();
        for (Item i : player.inventory) if (i.type.equals("Skill")) cards.add(i);
        if (cards.isEmpty()) { System.out.println("No skill cards!"); pause(); return null; }
        for (int i = 0; i < cards.size(); i++) System.out.println((i+1) + ". " + cards.get(i).name);
        try { return cards.get(Integer.parseInt(sc.nextLine()) - 1); } catch (Exception e) { return null; }
    }

    private void handleLevelUpMenu() {
        player.levelUp();
        System.out.println("Level Up! Level " + player.level);
        System.out.println("1. St  2. Vi  3. Lu");
        try { player.addStat(Integer.parseInt(sc.nextLine())); } catch (Exception e) {}
    }

    private void resetGame() {
        System.out.println("GAME OVER");
        player = new Player(); floor = 1;
        playerX = 5; playerY = 5;
        map = new Map(playerX, playerY, enemyDB);
        initializePlayer();
    }
}