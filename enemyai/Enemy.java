package enemyai;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

public class Enemy {
    public int x, y;
    private String direction = "down";
    private Image image;
    private final int width = 31, height = 31;
    private final int speed = 2;
    private final int maxBullets = 1;//敌人最大子弹数量
    private final List<EnemyBullet> bullets = new ArrayList<>();
    private final Random rand = new Random();
    private int shootCooldown = 0;
    private boolean alive = true;
    List<Node> path = new ArrayList<>();
    int pathStep = 1;
    int pathCooldown = 0;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        loadImage("assets/enemy_down.png");
    }
    public boolean isAlive() {
        return alive;
    }
    public void kill() {
        alive = false;
    }
    private void loadImage(String path) {
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePath(List<Node> newPath) {
        this.path = newPath;
        this.pathStep = 1;
    }

    public boolean move(Rectangle[] blockRects) {
        if (pathStep < path.size()) {
            Node target = path.get(pathStep);
            int tx = target.x * 32;
            int ty = target.y * 32;
    
            int newX = x, newY = y;
            if (tx > x) { newX += speed; direction = "right"; }
            else if (tx < x) { newX -= speed; direction = "left"; }
            else if (ty > y) { newY += speed; direction = "down"; }
            else if (ty < y) { newY -= speed; direction = "up"; }
    
            Rectangle future = new Rectangle(newX + 2, newY + 2, width - 4, height - 4);
            for (Rectangle r : blockRects) {
                if (r.intersects(future)) {
                    return false; // ❗ 被阻挡，移动失败
                }
            }
    
            // ✅ 移动成功
            x = newX;
            y = newY;
    
            Rectangle currentBounds = new Rectangle(x + 2, y + 2, width - 4, height - 4);
            Rectangle targetArea = new Rectangle(tx + 8, ty + 8, 16, 16);
            if (currentBounds.intersects(targetArea)) {
                pathStep++;
            }
    
            loadImage("assets/enemy_" + direction + ".png");
            return true;
        }
    
        return true; // 没有路径（已走完），默认成功
    }
    
    public void clearPath() {
        path.clear();
        pathStep = 0;
    }
    

    public boolean canShoot() {
        if (shootCooldown > 0) {
            shootCooldown--;
            return false;
        }
        if (bullets.size() >= maxBullets) return false;

        shootCooldown = 50 + rand.nextInt(30);
        return true;
    }

    public void shoot() {
        Point p = getGunPoint();
        bullets.add(new EnemyBullet(p.x, p.y, direction));
    }

    public void updateBullets(int mapWidth, int mapHeight) {
        bullets.removeIf(b -> b.x < 0 || b.y < 0 || b.x > mapWidth || b.y > mapHeight);
        for (EnemyBullet b : bullets) {
            b.move();
        }
    }

    public List<EnemyBullet> getBullets() {
        return bullets;
    }

    public void removeBullet(EnemyBullet b) {
        bullets.remove(b);
    }

    public Point getGunPoint() {
        return new Point(x + width / 2 - 5, y + height / 2 - 5);
    }

    public Rectangle getBounds() {
        //return new Rectangle(x, y, width, height);
        return new Rectangle(x + 2, y + 2, width - 4, height - 4);
    }

    public String getDirection() {
        return direction;
    }

    public void draw(Graphics g, Component observer) {
        g.drawImage(image, x, y, width, height, observer);
        for (EnemyBullet b : bullets) b.draw(g);
    }
}
