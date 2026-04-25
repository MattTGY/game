import java.util.ArrayList;

public class Player {
    public int level = 5;
    public int strength = 5, vitality = 5, luck = 5; // Removed Magic and Agility
    public int macca = 0, exp = 0;
    public int currentHp;
    public ArrayList<Item> inventory = new ArrayList<>();

    public Player() {
        this.currentHp = getMaxHp();
    }

    public int getMaxHp() {
        return (level + vitality) * 6;
    }

    // Updated to only allow 1 (St), 2 (Vi), or 3 (Lu)
    public void addStat(int choice) {
        switch(choice) {
            case 1: strength++; break;
            case 2: vitality++; break;
            case 3: luck++; break;
        }
        this.currentHp = getMaxHp(); // Heal on stat increase
    }

    public void takeDamage(int dmg) {
        this.currentHp = Math.max(0, this.currentHp - dmg);
    }

    public boolean isAlive() { return this.currentHp > 0; }

    public void useItem(Item item) {
        if (item.type.equals("HealHP")) {
            this.currentHp = Math.min(getMaxHp(), this.currentHp + item.value);
            System.out.println("Restored HP!");
        }
        inventory.remove(item);
    }

    public void levelUp() {
        this.level++;
        this.currentHp = getMaxHp();
    }
}