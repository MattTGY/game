import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

class Map {
    int size = 10;
    char[][] grid;
    int exitX, exitY;

    ArrayList<Enemy> enemies = new ArrayList<>();
    Random rand = new Random();
    HashMap<String, Enemy> enemyDB;
    HashMap<String, Item> itemDB;

    public Map(int playerStartX, int playerStartY, HashMap<String, Enemy> enemyDB) {
        this.enemyDB = enemyDB;
        generate(playerStartX, playerStartY);
    }

    public void generate(int playerStartX, int playerStartY) {
        grid = new char[size][size];

        // fill with empty tiles
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                grid[y][x] = '.';
            }
        }

        placeExit(playerStartX, playerStartY);
        spawnEnemies(playerStartX, playerStartY);
    }

    private void placeExit(int playerStartX, int playerStartY) {
        while (true) {
            int side = rand.nextInt(4);

            switch (side) {
                case 0: // top
                    exitX = rand.nextInt(size);
                    exitY = 0;
                    break;
                case 1: // bottom
                    exitX = rand.nextInt(size);
                    exitY = size - 1;
                    break;
                case 2: // left
                    exitX = 0;
                    exitY = rand.nextInt(size);
                    break;
                case 3: // right
                    exitX = size - 1;
                    exitY = rand.nextInt(size);
                    break;
            }

            // make sure it's NOT where player starts
            if (exitX != playerStartX || exitY != playerStartY) {
                grid[exitY][exitX] = 'X';
                break;
            }
        }
    }

    public void draw(int playerX, int playerY) {
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {

                if (x == playerX && y == playerY)
                    System.out.print("P ");
                else if(isEnemy(x,y)){
                    System.out.print("E ");
                }else if (grid[y][x] ==('C')) {
                    System.out.print("C ");
                }
                else
                    System.out.print(grid[y][x] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public boolean isExit(int x, int y) {
        return x == exitX && y == exitY;
    }

    private void spawnEnemies(int playerStartX, int playerStartY) {

        int numEnemies = 3 + rand.nextInt(3);

        ArrayList<String> enemyNames = new ArrayList<>(enemyDB.keySet());

        for (int i = 0; i < numEnemies; i++) {

            int x, y;

            while (true) {
                x = rand.nextInt(size);
                y = rand.nextInt(size);

                if ((x != playerStartX || y != playerStartY) &&
                    (x != exitX || y != exitY) &&
                    !isEnemy(x, y)) {
                    break;
                }
            }

        String randomName = enemyNames.get(rand.nextInt(enemyNames.size()));
        Enemy base = enemyDB.get(randomName);

        Enemy newEnemy = new Enemy(
                base.name,
                base.strength,
                base.magic,
                base.vitality,
                base.agility,
                base.luck
            );

            newEnemy.setPosition(x, y);
            enemies.add(newEnemy);
        }
    }

    public boolean isEnemy(int x, int y) {
        for (Enemy e : enemies) {
            if (e.x == x && e.y == y) return true;
        }
        return false;
    }

    public void removeEnemy(int x, int y) {
        enemies.removeIf(e -> e.x == x && e.y == y);   
    }

    public Enemy getEnemyAt(int x, int y) {
    for (Enemy e : enemies) {
        if (e.x == x && e.y == y) return e;
    }
    return null;
}
    // Inside Map.java
    public void spawnChests(int count) {
        for (int i = 0; i < count; i++) {
            int rx = rand.nextInt(size);
            int ry = rand.nextInt(size);
            if (grid[ry][rx]==('.')) {
                grid[ry][rx] = 'C'; // C for Chest
                spawned++;
            }
        }
    }

    public boolean isChest(int x, int y) {
        return grid[y][x] == 'C';
    }

    public void clearTile(int x, int y) {
        grid[y][x] = '.';
    }

    
}