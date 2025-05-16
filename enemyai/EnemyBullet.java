package enemyai;

import java.awt.*;

public class EnemyBullet {
    public int x, y;
    private final int speed = 10;
    private final String direction;

    public EnemyBullet(int x, int y, String direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public void move() {
        switch (direction) {
            case "up" -> y -= speed;
            case "down" -> y += speed;
            case "left" -> x -= speed;
            case "right" -> x += speed;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 10, 10);
    }

    public void draw(Graphics g) {
        g.setColor(Color.ORANGE);
        g.fillOval(x, y, 10, 10);
    }

    public String getDirection() {
        return direction;
    }
}
