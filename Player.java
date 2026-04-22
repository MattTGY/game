import java.util.ArrayList;

public class Player {
    public int level;
    public int strength, magic, vitality, agility, luck, macca, exp;
    public int currentHp, currentSp;
    public ArrayList<Item> inventory = new ArrayList<>();

    // Helper to use an item
    public void useItem(Item item) {
        if (item.type.equals("HealHP")) {
            this.currentHp = Math.min(getMaxHp(), this.currentHp + item.value);
        } else if (item.type.equals("HealSP")) {
            this.currentSp = Math.min(getMaxSp(), this.currentSp + item.value);
        }
        inventory.remove(item);
    }

    public Player() {
        this.level = 5;
        this.strength = 5;
        this.magic = 5;
        this.vitality = 5;
        this.agility = 5;
        this.luck = 5;

        // Initialize current pools to max at start
        this.currentHp = getMaxHp();
        this.currentSp = getMaxSp();
    }

    public void addStat(int choice) {
        switch(choice) {
            case 1: strength++; break;
            case 2: magic++; break;
            case 3: vitality++; break;
            case 4: agility++; break;
            case 5: luck++; break;
        }
        // Recalculate HP/SP if Vitality or Magic changed
        this.currentHp = getMaxHp();
        this.currentSp = getMaxSp();
    }

    // (Lvl + Vit) * 6 = HP
    public int getMaxHp() {
        int hp = (level + vitality) * 6;
        return Math.min(hp, 999); // Hard cap at 999
    }

    // (Lvl + Mag) * 3 = MP
    public int getMaxSp() {
        int sp = (level + magic) * 3;
        return Math.min(sp, 999); // Hard cap at 999
    }

    public void takeDamage(int damage) {
        this.currentHp -= damage;
        if (this.currentHp < 0) this.currentHp = 0;
    }

    public void levelUp() {
        this.level++;
        // We don't refill HP, but we might want to increase currentHp 
        // by the 6 points they just gained so they don't feel cheated.
        this.currentHp += 6;
        this.currentSp += 3;
    }

    boolean isAlive() {
        return this.currentHp > 0;
    }
}