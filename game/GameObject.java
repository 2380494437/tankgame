package game;

import java.awt.*;
import java.awt.Image;

public class GameObject {
    public int x, y;
    public Image image;
    public int type;
    public int width, height;

    public GameObject(int x, int y, Image image, int type, int width, int height) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.type = type;
        this.width = width;
        this.height = height;
    }

    public boolean canBulletPass() {
        return type == 3 || type == 4;
    }

    public boolean canPlayerPass() {
        return type == 4;
    }

    public boolean canBeDestroyedByBullet() {
        return type == 1;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 2, y + 2, width - 4, height - 4);
    }
}
