import java.io.*;
import java.util.*;

public class ItemLoader {
    public static HashMap<String, Item> loadItems(String fileName) {
        HashMap<String, Item> itemDB = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                Item item = new Item(data[0], data[1], Integer.parseInt(data[2]), Integer.parseInt(data[3]), data[4]);
                itemDB.put(item.name, item);
            }
        } catch (IOException e) {
            System.out.println("Error loading items: " + e.getMessage());
        }
        return itemDB;
    }
}