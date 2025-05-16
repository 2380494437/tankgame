package enemyai;

import game.Ally;
import game.GameObject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class EnemyManager {
    private final List<Enemy> enemies = new ArrayList<>();
    private final int[][] logicMap;
    private final int cols, rows;
    private final List<Point> spawnPoints = new ArrayList<>();
    private final int maxEnemies = 50;
    private final int mapWidth, mapHeight;

    public EnemyManager(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.cols = mapWidth / 32;
        this.rows = mapHeight / 32;
        this.logicMap = new int[cols][rows];
    
        int centerX = cols / 2;
    
        spawnPoints.add(new Point(1, 1));               // 左上
        spawnPoints.add(new Point(cols - 2, 1));        // 右上
        spawnPoints.add(new Point(centerX, 1));         // 中上
    }
    

    public void updateLogicMap(List<GameObject> objects) {
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                logicMap[x][y] = 0;

        for (GameObject obj : objects) {
            if (!obj.canPlayerPass()) {
                int cx = obj.x / 32;
                int cy = obj.y / 32;
                logicMap[cx][cy] = 1;
            }
        }
    }

    
    public void updateEnemies(List<GameObject> objects, Ally ally) {
        while (enemies.size() < maxEnemies) {
            List<Point> shuffled = new ArrayList<>(spawnPoints);
            Collections.shuffle(shuffled); // 打乱顺序，随机选择空位
        
            boolean spawned = false;
            for (Point spawn : shuffled) {
                Rectangle spawnRect = new Rectangle(spawn.x * 32 + 2, spawn.y * 32 + 2, 31 - 4, 31 - 4); // 缩小边界
                boolean occupied = false;
        
                for (Enemy e : enemies) {
                    if (spawnRect.intersects(e.getBounds())) {
                        occupied = true;
                        break;
                    }
                }
        
                if (!occupied) {
                    enemies.add(new Enemy(spawn.x * 32, spawn.y * 32));
                    spawned = true;
                    break;
                }
            }
        
            if (!spawned) {
                break; // 所有出生点都被占用，退出生成循环
            }
        }
        
    
        AStarPathFinder pathFinder = new AStarPathFinder(logicMap);
    
        for (Enemy e : enemies) {
            int ex = e.x / 32, ey = e.y / 32;
    
            // 每隔一定时间（或走完路径）选新目标
            if (e.pathStep >= e.path.size() || e.pathCooldown <= 0) {
                Node dest = getRandomWalkableTile();
                if (dest != null) {
                    List<Node> path = pathFinder.findPath(ex, ey, dest.x, dest.y);
                    if (!path.isEmpty()) {
                        e.updatePath(path);
                        e.pathCooldown = 100 + new Random().nextInt(100); // 随机下一次更新间隔
                    }
                }
            } else {
                e.pathCooldown--;
            }
    
            // 收集阻挡物（地图+敌人）
            List<Rectangle> allBlocks = new ArrayList<>();
            for (GameObject obj : objects) {
                if (!obj.canPlayerPass()) {
                    allBlocks.add(obj.getBounds());
                }
            }
            for (Enemy other : enemies) {
                if (other != e) {
                    allBlocks.add(other.getBounds());
                }
            }
            if (ally != null && ally.isAlive()) {
                allBlocks.add(ally.getBounds());
            }
            Rectangle[] blockRects = allBlocks.toArray(new Rectangle[0]);
            boolean moved = e.move(blockRects);

            if (!moved) {
                // 立即清除当前路径
                e.clearPath();

                // 尝试切换临时探索路径
                Node randomNearby = getRandomWalkableTileNear(e.x / 32, e.y / 32);
                if (randomNearby != null) {
                    List<Node> newPath = pathFinder.findPath(e.x / 32, e.y / 32, randomNearby.x, randomNearby.y);
                    if (!newPath.isEmpty()) {
                        e.updatePath(newPath);
                        //System.out.println("🧭 敌人被挡住，立即切换临时路径 -> " + randomNearby.x + "," + randomNearby.y);
                    }
                } else {
                    System.out.println("⚠️ 敌人被挡住但找不到附近空位");
                }
            }

            if (e.canShoot()) e.shoot();
            e.updateBullets(mapWidth, mapHeight);
        }
    }
    
    private Node getRandomWalkableTileNear(int cx, int cy) {
        List<Node> candidates = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int nx = cx + dx;
                int ny = cy + dy;
                if (nx >= 0 && ny >= 0 && nx < logicMap.length && ny < logicMap[0].length) {
                    if (logicMap[nx][ny] == 0) {
                        candidates.add(new Node(nx, ny));
                    }
                }
            }
        }
        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            return candidates.get(0);
        }
        return null;
    }
    
    private Node getRandomWalkableTile() {
        Random rand = new Random();
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = rand.nextInt(cols);
            int y = rand.nextInt(rows);
            if (logicMap[x][y] == 0) {
                return new Node(x, y);
            }
        }
        return null; // 失败
    }
    

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<EnemyBullet> getAllBullets() {
        List<EnemyBullet> all = new ArrayList<>();
        for (Enemy e : enemies) all.addAll(e.getBullets());
        return all;
    }

    public void removeBullet(EnemyBullet b) {
        for (Enemy e : enemies) {
            if (e.getBullets().contains(b)) {
                e.removeBullet(b);
                break;
            }
        }
    }

    public void removeEnemy(Enemy e) {
        enemies.remove(e);
    }

    public void draw(Graphics g, Component observer) {
        for (Enemy e : enemies) e.draw(g, observer);
    }
}
