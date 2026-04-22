public class Enemy {
    public int x, y; // position on map
    public String name;
    public int strength, magic, vitality, agility, luck;

    // Constructor from CSV / stats
    public Enemy(String name, int strength, int magic, int vitality, int agility, int luck) {
        this.name = name;
        this.strength = strength;
        this.magic = magic;
        this.vitality = vitality;
        this.agility = agility;
        this.luck = luck;
        this.x = -1; // default until placed on map
        this.y = -1;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}