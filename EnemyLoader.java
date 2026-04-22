import java.io.*;
import java.util.*;

public class EnemyLoader {
    public static void main(String[] args) {
    HashMap<String, Enemy> db = loadEnemies("enemylist.csv");
    System.out.println("Loaded enemies: " + db.size());
}

    public static HashMap<String, Enemy> loadEnemies(String fileName) {
        HashMap<String, Enemy> enemyDB = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                String name = parts[0].trim();
                int str = Integer.parseInt(parts[1].trim());
                int mag = Integer.parseInt(parts[2].trim());
                int vit = Integer.parseInt(parts[3].trim());
                int agi = Integer.parseInt(parts[4].trim());
                int luck = Integer.parseInt(parts[5].trim());

                enemyDB.put(name, new Enemy(name, str, mag, vit, agi, luck));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return enemyDB;
    }
}