package game;

import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import javax.imageio.ImageIO;

import enemyai.Enemy;
import enemyai.EnemyBullet;
import enemyai.AStarPathFinder;
import enemyai.Node;
import java.awt.Font;


public class Ally {
    public int x, y;
    private final int width = 31, height = 31;
    private final int speed = 4;
    private String direction = "down";
    private BufferedImage image;
    private final Point spawnPoint;
    private boolean alive = true;
    private int respawnTimer = 0;
    private int pathCooldown = 0;
    private int shootCooldown = 0;
    private int stuckCounter = 0;
    private final List<Bullet> bullets = new ArrayList<>();
    private List<Node> path = new ArrayList<>();
    public int pathStep = 1;
    private Node lastTarget = null;
    private final int maxBullets = 50; // ✅ 队友最多允许存在的子弹数
    private Runnable fireSoundCallback = null;
    private String label = "AI"; // 默认内容
    private int labelTimer = 0;
    private int deathCount = 0;
    private int killCount = 0;
    public void setLabel(String text, int durationFrames) {
        this.label = text;
        this.labelTimer = durationFrames;
    }
    public int getDeathCount() {
        return deathCount;
    }
    
    public int getKillCount() {
        return killCount;
    }
    public void updateLabelTimer() {
        if (labelTimer > 0) {
            labelTimer--;
            if (labelTimer == 0) {
                label = "AI"; // 重置为默认
            }
        }
    }
    public Ally(int x, int y) {
        this.x = x;
        this.y = y;
        this.spawnPoint = new Point(x, y);
        loadImage("assets/tile_351.png");
    }
    public void setFireSoundCallback(Runnable callback) {
        this.fireSoundCallback = callback;
    }
    private void loadImage(String path) {
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean canShoot() {
        return shootCooldown-- <= 0 && bullets.size() < maxBullets;
    }
    public void draw(Graphics g) {
        g.setColor(Color.CYAN); // 队友子弹颜色
        g.fillOval(x, y, 10, 10);
    }
    
    public void update(List<Enemy> enemies, List<EnemyBullet> enemyBullets, int[][] logicMap, List<GameObject> objects, int mapWidth, int mapHeight, Runnable fireSoundCallback) {
        if (!alive) {
            if (respawnTimer-- <= 0) {
                x = spawnPoint.x;
                y = spawnPoint.y;
                alive = true;
                pathStep = 1;
                System.out.println("🪖 队友重生于: (" + x + ", " + y + ")");
                setLabel("复活继续干", 60);
            }
            return;
        }
    
        if (isThreatened(enemyBullets)) {
            evade(objects, enemyBullets);
            tryInterceptBullet(enemyBullets); // ✅ 新增防御性开火逻辑
            return;
        }
    
        if (enemies.isEmpty()) return;
    
        Enemy target = findClosestEnemy(enemies);
        int ex = x / 32;
        int ey = y / 32;
        int tx = target.x / 32;
        int ty = target.y / 32;
    
        // 路径更新
        if (path.isEmpty() || pathStep >= path.size() || pathCooldown <= 0) {
            AStarPathFinder finder = new AStarPathFinder(logicMap);
    
            // 找到敌人上下左右的空位
            List<Node> candidatePositions = new ArrayList<>();

            // 水平方向
            for (int x = 0; x < logicMap.length; x++) {
                if (x == tx) continue;
                if (logicMap[x][ty] != 0) continue;

                boolean blocked = false;
                for (int k = Math.min(x, tx) + 1; k < Math.max(x, tx); k++) {
                    if (logicMap[k][ty] != 0) {
                        blocked = true;
                        break;
                    }
                }

                if (!blocked) {
                    candidatePositions.add(new Node(x, ty));
                }
            }

            // 垂直方向
            for (int y = 0; y < logicMap[0].length; y++) {
                if (y == ty) continue;
                if (logicMap[tx][y] != 0) continue;

                boolean blocked = false;
                for (int k = Math.min(y, ty) + 1; k < Math.max(y, ty); k++) {
                    if (logicMap[tx][k] != 0) {
                        blocked = true;
                        break;
                    }
                }

                if (!blocked) {
                    candidatePositions.add(new Node(tx, y));
                }
            }

    
            // 距离自己最近的目标
            Node bestTarget = null;
            int minDist = Integer.MAX_VALUE;
            for (Node pos : candidatePositions) {
                if (lastTarget != null && pos.x == lastTarget.x && pos.y == lastTarget.y) {
                    continue; // ⚠️ 跳过和上次一样的目标，避免死循环
                }
            
                int dist = Math.abs(pos.x - ex) + Math.abs(pos.y - ey);
                if (dist < minDist) {
                    minDist = dist;
                    bestTarget = pos;
                }
            }
            
    
            if (bestTarget != null) {
                //System.out.println("🎯 追踪敌人: (" + target.x + ", " + target.y + ")，目标格: (" + bestTarget.x + ", " + bestTarget.y + ")");
                setLabel("🎯 追击敌人中...", 60);
                path = finder.findPath(ex, ey, bestTarget.x, bestTarget.y);
                pathStep = 1;
                pathCooldown = 40;
                lastTarget = bestTarget;
            }
        } else {
            pathCooldown--;
        }
        // ✅ 监视四个方向，如果有敌人出现并可直线命中，就开火
        for (Enemy e : enemies) {
            if (!e.isAlive()) continue;

            int dx = e.x - x;
            int dy = e.y - y;

            // 同一行或同一列
            if ((Math.abs(dx) < 16 && Math.abs(dy) < 800) || (Math.abs(dy) < 16 && Math.abs(dx) < 800)) {

                String fireDir = null;
                if (Math.abs(dx) < 16) {
                    fireDir = dy > 0 ? "down" : "up";
                } else if (Math.abs(dy) < 16) {
                    fireDir = dx > 0 ? "right" : "left";
                }

                if (fireDir != null && canShoot()) {
                    // 检查中间是否有障碍物
                    boolean blocked = false;
                    Rectangle line = switch (fireDir) {
                        case "up" -> new Rectangle(x + width / 2 - 5, e.y, 10, y - e.y);
                        case "down" -> new Rectangle(x + width / 2 - 5, y, 10, e.y - y);
                        case "left" -> new Rectangle(e.x, y + height / 2 - 5, x - e.x, 10);
                        case "right" -> new Rectangle(x, y + height / 2 - 5, e.x - x, 10);
                        default -> null;
                    };

                    if (line != null) {
                        for (GameObject obj : objects) {
                            if (!obj.canBulletPass() && obj.getBounds().intersects(line)) {
                                blocked = true;
                                break;
                            }
                        }
                    }

                    if (!blocked) {
                        direction = fireDir;
                        Point p = getGunPoint();
                        this.bullets.add(new Bullet(p.x, p.y, fireDir, 15));

                        shootCooldown = 30;
                        if (fireSoundCallback != null) fireSoundCallback.run();
                        System.out.println("🎯 队友自动开火命中敌人方向: " + direction);
                        setLabel("💥 远程开炮！", 60);
                        break; // 只开一枪，防止重复开火
                    }
                }
            }
        }

        // 移动执行
        /*if (pathStep < path.size()) {
            Node node = path.get(pathStep);
            moveTo(node.x * 32, node.y * 32, objects, enemies);
        }*/
        if (pathStep < path.size()) {
            Node node = path.get(pathStep);
            boolean moved = moveTo(node.x * 32, node.y * 32, objects, enemies);
        
            if (!moved) {
                stuckCounter++;
                //System.out.println("⛔ 被阻挡，卡住计数: " + stuckCounter);
            
                // ✅ 检查前方是否是敌人
                Rectangle ahead = switch (direction) {
                    case "up" -> new Rectangle(x + 2, y - 5, width - 4, 5);
                    case "down" -> new Rectangle(x + 2, y + height, width - 4, 5);
                    case "left" -> new Rectangle(x - 5, y + 2, 5, height - 4);
                    case "right" -> new Rectangle(x + width, y + 2, 5, height - 4);
                    default -> null;
                };
            
                boolean enemyInFront = false;
                if (ahead != null) {
                    for (Enemy enemy : enemies) {
                        if (ahead.intersects(enemy.getBounds())) {
                            enemyInFront = true;
                            break;
                        }
                    }
                }
            
                if (enemyInFront && canShoot()) {
                    Point p = getGunPoint();
                    bullets.add(new Bullet(p.x, p.y, direction, 15));
                    shootCooldown = 30;
                    stuckCounter = 0;
                    System.out.println("💥 前方是敌人，立即开火，方向: " + direction);
                    setLabel("💥 开炮！", 60);

                    if (fireSoundCallback != null) fireSoundCallback.run();
                }else if (stuckCounter >= 5) {
                    // 🔁 随机开火或探索
                    int action = new Random().nextInt(2);
                    if (action == 0) {
                        String[] dirs = { "up", "down", "left", "right" };
                        boolean fired = false;
                    
                        for (String dir : dirs) {
                            this.direction = dir;
                            Point p = getGunPoint();
                    
                            // 构建朝前的区域，用于检测砖块/子弹
                            Rectangle forward = switch (dir) {
                                case "up"    -> new Rectangle(x + 2, y - 5, width - 4, 10);
                                case "down"  -> new Rectangle(x + 2, y + height - 5, width - 4, 10);
                                case "left"  -> new Rectangle(x - 5, y + 2, 10, height - 4);
                                case "right" -> new Rectangle(x + width - 5, y + 2, 10, height - 4);
                                default -> null;
                            };
                    
                            boolean shouldFire = false;
                    
                            // ✅ 检查是否是砖块 type == 1
                            for (GameObject obj : objects) {
                                if (obj.type == 1 && forward.intersects(obj.getBounds())) {
                                    shouldFire = true;
                                    //System.out.println("💥 前方是砖块，方向: " + dir);
                                    break;
                                }
                            }
                    
                            // ✅ 检查是否有敌人子弹靠近（贴脸）
                            if (!shouldFire) {
                                for (EnemyBullet eb : enemyBullets) {
                                    Rectangle bulletRect = new Rectangle(eb.x, eb.y, 10, 10);
                                    if (forward.intersects(bulletRect)) {
                                        shouldFire = true;
                                        System.out.println("🛡️ 前方有敌方子弹，方向: " + dir);
                                        setLabel(dir + "方向有危险", 60);
                                        break;
                                    }
                                }
                            }
                    
                            if (shouldFire && canShoot()) {
                                bullets.add(new Bullet(p.x, p.y, dir, 15));
                                shootCooldown = 30;
                                if (fireSoundCallback != null) fireSoundCallback.run();
                                fired = true;
                                break;
                            }
                        }
                    
                        if (fired) {
                            stuckCounter = 0;
                        } else {
                            setLabel("💥 打不开！再试试", 60);
                            //System.out.println("🧱 被卡住但四周无砖块也无子弹，保持等待");
                        }
                    }                    
                     else {
                        Node randomNearby = findRandomNearbyPosition(logicMap, ex, ey);
                        if (randomNearby != null) {
                            AStarPathFinder finder = new AStarPathFinder(logicMap);
                            path = finder.findPath(ex, ey, randomNearby.x, randomNearby.y);
                            pathStep = 1;
                            stuckCounter = 0;
                            setLabel("看导航中...", 60);
                            //System.out.println("🌀 被卡住，切换临时探索路径 -> " + randomNearby.x + "," + randomNearby.y);
                        } else {
                            System.out.println("⚠️ 被卡住但未找到可探索路径，保持等待");
                        }
                    }
                }
            
            } else {
                stuckCounter = 0;
            }
            updateLabelTimer();

            
        }
        
    
        // 🔫 判断是否正对敌人 + 射线中间无障碍
        if (canShoot() && isAlignedAndClear(target, objects)) {
            // 自动调整朝向
            if (Math.abs(target.x - x) > Math.abs(target.y - y)) {
                direction = target.x > x ? "right" : "left";
            } else {
                direction = target.y > y ? "down" : "up";
            }
    
            Point p = getGunPoint();
            bullets.add(new Bullet(p.x, p.y, direction, 15));
            shootCooldown = 30;
            System.out.println("🔫 队友开火 -> 方向: " + direction + "，位置: (" + x + ", " + y + ")");
            setLabel("开火！", 60);
            if (fireSoundCallback != null) fireSoundCallback.run();
            // 如果前方仍被墙挡住，重新寻路
            if (isBlockedAhead(objects)) {
                path.clear();
                pathStep = 0;
            }
        }
    
        bullets.removeIf(b -> b.x < 0 || b.y < 0 || b.x > mapWidth || b.y > mapHeight);
        for (Bullet b : bullets) b.move();
        
    }
    
    private void tryInterceptBullet(List<EnemyBullet> enemyBullets) {
        for (EnemyBullet b : enemyBullets) {
            Rectangle bulletBox = new Rectangle(b.x, b.y, 10, 10);
            Rectangle interceptZone = new Rectangle(x - 50, y - 50, width + 100, height + 100); // ✅ 50px 四周
    
            if (interceptZone.intersects(bulletBox)) {
                String bulletDir = b.getDirection();
                String fireDir = switch (bulletDir) {
                    case "up"    -> "down";
                    case "down"  -> "up";
                    case "left"  -> "right";
                    case "right" -> "left";
                    default -> null;
                };
    
                if (fireDir != null && canShoot()) {
                    direction = fireDir;
                    Point p = getGunPoint();
                    bullets.add(new Bullet(p.x, p.y, direction, 15));
                    shootCooldown = 30;
                    System.out.println("🛡️ 队友主动拦截子弹，方向: " + direction);
                    setLabel("拦截子弹", 60);
                    if (fireSoundCallback != null) fireSoundCallback.run();
                    break;
                }
            }
        }
    }
    
    
    private boolean isAlignedAndClear(Enemy target, List<GameObject> objects) {
        Rectangle self = getBounds();
        Rectangle targetRect = target.getBounds();
    
        if (self.x == targetRect.x) {
            int y1 = Math.min(self.y, targetRect.y);
            int y2 = Math.max(self.y, targetRect.y);
            for (GameObject obj : objects) {
                if (!obj.canBulletPass()) {
                    Rectangle r = obj.getBounds();
                    if (r.x == self.x && r.y > y1 && r.y < y2) {
                        return false;
                    }
                }
            }
            return true;
        }
    
        if (self.y == targetRect.y) {
            int x1 = Math.min(self.x, targetRect.x);
            int x2 = Math.max(self.x, targetRect.x);
            for (GameObject obj : objects) {
                if (!obj.canBulletPass()) {
                    Rectangle r = obj.getBounds();
                    if (r.y == self.y && r.x > x1 && r.x < x2) {
                        return false;
                    }
                }
            }
            return true;
        }
    
        return false;
    }
    
    private boolean isBlockedAhead(List<GameObject> objects) {
        Rectangle ahead = switch (direction) {
            case "up" -> new Rectangle(x, y - 5, width, 5);
            case "down" -> new Rectangle(x, y + height, width, 5);
            case "left" -> new Rectangle(x - 5, y, 5, height);
            case "right" -> new Rectangle(x + width, y, 5, height);
            default -> new Rectangle(x, y, width, height);
        };
    
        for (GameObject obj : objects) {
            if (!obj.canBulletPass() && ahead.intersects(obj.getBounds())) {
                return true;
            }
        }
        return false;
    }
    
    

    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy closest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            double d = Point.distance(x, y, e.x, e.y);
            if (d < minDist) {
                minDist = d;
                closest = e;
            }
        }
        return closest;
    }

    private boolean isThreatened(List<EnemyBullet> bullets) {
        Rectangle self = getBounds();
        for (EnemyBullet b : bullets) {
            Rectangle future = new Rectangle(b.x, b.y, 10, 10);
            for (int i = 0; i < 10; i++) {
                switch (b.getDirection()) {
                    case "up" -> future.y -= 10;
                    case "down" -> future.y += 10;
                    case "left" -> future.x -= 10;
                    case "right" -> future.x += 10;
                }
                if (self.intersects(future)) return true;
            }
        }
        return false;
    }

    private void evade(List<GameObject> objects, List<EnemyBullet> enemyBullets) {
        // ✅ 先主动防御 - 尝试拦截贴脸子弹
        for (EnemyBullet b : enemyBullets) {
            Rectangle bulletRect = new Rectangle(b.x, b.y, 10, 10);
            Rectangle self = getBounds();
            if (self.intersects(bulletRect)) {
                String fireDir = switch (b.getDirection()) {
                    case "up" -> "down";
                    case "down" -> "up";
                    case "left" -> "right";
                    case "right" -> "left";
                    default -> null;
                };
        
                if (fireDir != null && canShoot()) {
                    direction = fireDir;
                    Point p = getGunPoint();
                    this.bullets.add(new Bullet(p.x, p.y, fireDir, 15)); // ✅ 修复点
                    shootCooldown = 30;
                    System.out.println("🛡️ 贴脸子弹 -> 主动拦截方向: " + fireDir);
                    setLabel("拦截", 60);
                    if (fireSoundCallback != null) fireSoundCallback.run();
                }
            }
        }
        
    
        // ✅ 清除原来的寻路，避免移动路径和躲避冲突
        path.clear();
        pathStep = 0;
    
        // ✅ 开始智能闪避
        int[][] dirs = { {0, -speed}, {0, speed}, {-speed, 0}, {speed, 0} };
        String[] dirNames = { "up", "down", "left", "right" };
    
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
    
        for (int i = 0; i < dirs.length; i++) {
            int[] d = dirs[i];
            Rectangle future = new Rectangle(x + d[0] + 2, y + d[1] + 2, width - 4, height - 4);
    
            // ✅ 是否被障碍挡住
            boolean blocked = false;
            for (GameObject obj : objects) {
                if (!obj.canPlayerPass() && future.intersects(obj.getBounds())) {
                    blocked = true;
                    break;
                }
            }
            if (blocked) continue;
    
            // ✅ 计算危险评分
            int dangerScore = 0;
            for (EnemyBullet b : enemyBullets) {
                Rectangle bulletRect = new Rectangle(b.x, b.y, 10, 10);
                for (int t = 0; t < 5; t++) {
                    switch (b.getDirection()) {
                        case "up" -> bulletRect.y -= 10;
                        case "down" -> bulletRect.y += 10;
                        case "left" -> bulletRect.x -= 10;
                        case "right" -> bulletRect.x += 10;
                    }
                    if (bulletRect.intersects(future)) {
                        dangerScore -= (5 - t); // 越近越危险
                        break;
                    }
                }
            }
    
            if (dangerScore > bestScore) {
                bestScore = dangerScore;
                bestMove = d;
            }
        }
    
        if (bestMove != null) {
            x += bestMove[0];
            y += bestMove[1];
            updateImage();
            System.out.println("🌀 闪避成功，方向: (" + bestMove[0] + ", " + bestMove[1] + ")");
            setLabel("闪避", 60);
        } else {
            System.out.println("🚨 所有方向都有风险，原地停留防守");
            setLabel("躲不掉啦", 60);
        }
    }
    
    

    private boolean moveTo(int tx, int ty, List<GameObject> objects, List<Enemy> enemies) {
        int newX = x, newY = y;
        if (tx > x) { newX += speed; direction = "right"; }
        else if (tx < x) { newX -= speed; direction = "left"; }
        else if (ty > y) { newY += speed; direction = "down"; }
        else if (ty < y) { newY -= speed; direction = "up"; }
    
        Rectangle future = new Rectangle(newX + 2, newY + 2, width - 4, height - 4);
    
        boolean blocked = false;
    
        // 与障碍物碰撞检测
        for (GameObject obj : objects) {
            if (!obj.canPlayerPass() && future.intersects(obj.getBounds())) {
                blocked = true;
                break;
            }
        }
    
        // ✅ 与敌人碰撞检测（新增）
        if (!blocked) {
            for (Enemy enemy : enemies) {
                if (future.intersects(enemy.getBounds())) {
                    blocked = true;
                    break;
                }
            }
        }
    
        /*if (!blocked) {
            x = newX;
            y = newY;
            Rectangle currentBounds = getBounds();
            Rectangle targetArea = new Rectangle(tx + 8, ty + 8, 16, 16);
            if (currentBounds.intersects(targetArea)) {
                pathStep++;
            }
        }*/
        if (!blocked) {
            x = newX;
            y = newY;
            stuckCounter = 0; // ✅ 移动成功，清零卡住计数
            //System.out.println("🚶 移动到: (" + x + ", " + y + ")");
            Rectangle currentBounds = getBounds();
            Rectangle targetArea = new Rectangle(tx + 8, ty + 8, 16, 16);
            if (currentBounds.intersects(targetArea)) {
                pathStep++;
            }
            updateImage();
            return true;
        } else {
            return false;
        }
        
    
        
    }
    
    private Node findRandomNearbyPosition(int[][] map, int cx, int cy) {
        List<Node> options = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int nx = cx + dx;
                int ny = cy + dy;
                if (nx >= 0 && ny >= 0 && nx < map.length && ny < map[0].length) {
                    if (map[nx][ny] == 0) {
                        options.add(new Node(nx, ny));
                    }
                }
            }
        }
        if (!options.isEmpty()) {
            Collections.shuffle(options);
            return options.get(0);
        }
        return null;
    }
    
    private void updateImage() {
        try {
            switch (direction) {
                case "up" -> image = ImageIO.read(new File("assets/tile_351.png"));
                case "down" -> image = ImageIO.read(new File("assets/tile_354.png"));
                case "left" -> image = ImageIO.read(new File("assets/tile_352.png"));
                case "right" -> image = ImageIO.read(new File("assets/tile_356.png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 2, y + 2, width - 4, height - 4);
    }

    public Point getGunPoint() {
        return new Point(x + width / 2 - 5, y + height / 2 - 5);
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void draw(Graphics g, Component observer) {
        if (!alive) return;
        g.drawImage(image, x, y, width, height, observer);
        for (Bullet b : bullets) b.draw(g);
        drawLabel(g);
    }

    private void drawLabel(Graphics g) {
        String text = label; // ✅ 使用动态内容
        Font originalFont = g.getFont();
        Font font = new Font("SansSerif", Font.PLAIN, 12);
        g.setFont(font);
    
        int textWidth = g.getFontMetrics().stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y - 8;
    
        g.setColor(Color.WHITE);
        g.fillRoundRect(textX - 4, textY - 12, textWidth + 8, 16, 10, 10);
    
        g.setColor(Color.GRAY);
        g.drawRoundRect(textX - 4, textY - 12, textWidth + 8, 16, 10, 10);
    
        g.setColor(Color.BLACK);
        g.drawString(text, textX, textY);
    
        g.setFont(originalFont);

        g.setColor(Color.YELLOW);
        g.drawString("击杀:" + killCount + " 死亡:" + deathCount, x, y + height + 12);
    }
    
    
    public void kill() {
        alive = false;
        respawnTimer = 0; // 3秒后重生
        bullets.clear();
        System.out.println("💀 队友死亡 -> 即将重生");
        deathCount++; // ✅ 记录死亡次数
        setLabel("💀 我死了...", 60);
    }
    public void incrementKill() {
        killCount++;
        setLabel("🎯 击杀 +1", 50);
    }
    
    public boolean isAlive() {
        return alive;
    }
}
