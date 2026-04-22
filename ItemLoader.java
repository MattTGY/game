import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ItemLoader {

    public static HashMap<String, Item> loadItems(String filePath) {
        HashMap<String, Item> itemMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                // Skip the first line (ID, Name, Type...)
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // 1. Split by "," to handle the quotes in your CSV
                // This regex handles: "Value","Value","Value"
                String[] data = line.split("\",\"");

                // 2. Clean up the surrounding quotes from the split
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "");
                }

                // 3. Map the columns based on your file structure:
                // Index 0: ID (Skip)
                // Index 1: Name
                // Index 2: Type
                // Index 3: Value
                // Index 4: Price
                // Index 5: Description
                
                String name = data[1];
                String type = data[2];
                int value = Integer.parseInt(data[3]);
                int price = Integer.parseInt(data[4]);
                String description = data[5];

                // 4. Create the Item object and put it in the map
                Item newItem = new Item(name, type, value, price, description);
                itemMap.put(name, newItem);
            }
        } catch (IOException e) {
            System.out.println("Error loading file: " + filePath);
            e.printStackTrace();
        }

        return itemMap;
    }
}