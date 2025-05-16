package game;

import java.awt.*;

public class Bullet {
    public int x, y;
    private final int speed;
    private final String direction;

    public Bullet(int x, int y, String direction, int speed) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
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
        g.setColor(Color.CYAN); // 队友子弹颜色
        g.fillOval(x, y, 10, 10);
    }

    public String getDirection() {
        return direction;
    }
}
