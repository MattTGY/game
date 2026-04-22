public class Item {
    public String name;
    public String type; // HealHP, HealSP, Cure
    public int value;   // Amount to heal
    public int price;
    public String description;

    public Item(String name, String type, int value, int price, String description) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.price = price;
        this.description = description;
    }
}