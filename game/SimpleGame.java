package game;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import enemyai.*;
import game.GameObject;
import java.util.List;
import java.util.Random;
//编译 javac game/*.java enemyai/*.java
//运行 java -cp . game.SimpleGame
public class SimpleGame extends JPanel implements ActionListener {
    private int playerX = 496;//玩家初始坐标
    private int playerY = 799;//玩家初始坐标
    private final int playerSpeed = 4;
    private Image playerImage;
    private final int playerWidth = 31;//玩家体积为31*31，这样比32*32小，才可以走过打出来的过道
    private final int playerHeight = 31;
    private final int mapWidth = 1248; // 增加地图的宽度416
    private final int mapHeight = 832; // 保持地图的高度为832
    private final Set<Integer> keysPressed = new HashSet<>();
    private final ArrayList<Bullet> bulletList = new ArrayList<>();
    private final ArrayList<GameObject> objects = new ArrayList<>(); // 用于存放地图上的物体
    private String playerDirection = "up";
    private Timer timer;
    private int lastKeyPressed = -1;
    private final int maxBullets = 1; // 最多允许的子弹数量
    private final int bulletSpeed = 15; // 子弹速度
    private final ArrayList<Explosion> explosions = new ArrayList<>(); // 存放爆炸效果
    private Clip moveClip; // 用于循环播放坦克移动音效
    private boolean isMoving = false; // 记录坦克是否在移动
    private final EnemyManager enemyManager = new EnemyManager(mapWidth, mapHeight);
    private final Point playerSpawn = new Point(496, 799); // 玩家出生点
    private final int maxBricks = 100;
    private int brickSpawnCooldown = 0;
    private Ally ally = new Ally(624, 799); // 队友出生点，可调


    public SimpleGame() {
        try {
            playerImage = ImageIO.read(new File("assets/tile_50.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 播放初始化音频
        playSound("wav/开始游戏.wav");

       // 创建物体作为示例
       
       createObjectAtPosition(100, 10, "assets/tile_16.png", 1, 32, 32); // 砖块
       createObjectAtPosition(150, 10, "assets/tile_41.png", 2, 32, 32); // 铁块
       createObjectAtPosition(200, 10, "assets/tile_91.png", 3, 32, 32); // 水体
       createObjectAtPosition(250, 10, "assets/tile_67.png", 4, 32, 32); // 草丛
       
       
       createObjectAtPosition(530, 799, "assets/tile_41.png", 2, 32, 32); // 铁块
       createObjectAtPosition(530, 799-32, "assets/tile_41.png", 2, 32, 32); // 铁块
       createObjectAtPosition(530+32, 799-32, "assets/tile_41.png", 2, 32, 32); // 铁块
       createObjectAtPosition(530+32+32, 799-32, "assets/tile_41.png", 2, 32, 32); // 铁块
       createObjectAtPosition(530+32+32, 799, "assets/tile_41.png", 2, 32, 32); // 铁块
       createObjectAtPosition(530+32, 799, "assets/tile_69.png", 2, 32, 32); // Boss


       /*
       createObjectAtPosition(100, 420, "assets/tile_91.png", 3, 32, 32); // 水体
       createObjectAtPosition(100+32, 420, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32, 420, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32, 420, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32, 420, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32, 420, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32+32, 420, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32+32+32, 420, "assets/tile_91.png", 3, 32, 32); // 水体

       createObjectAtPosition(100+32+32, 420+32+32, "assets/tile_91.png", 3, 32, 32); // 水体
       createObjectAtPosition(100+32+32+32, 420+32+32, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32, 420+32+32, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32, 420+32+32, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32+32, 420+32+32, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32+32+32, 420+32+32, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32+32+32+32, 420+32+32, "assets/tile_67.png", 4, 32, 32); // 草丛
       createObjectAtPosition(100+32+32+32+32+32+32+32+32+32, 420+32+32, "assets/tile_91.png", 3, 32, 32); // 水体
        */











        // 地图宽度为 1248px，居中点是 1248 / 2 = 624
        int centerX = 624;

        // 行间距约 176px，砖块高度为 32px × 5 行 = 160，建议行距 16~32px
        drawTextCentered("HELLO", centerX, 64);       // 第一行
        drawTextCentered("WORLD", centerX, 256);      // 第二行
        drawTextCentered("2380494437", centerX, 448); // 第三行



       enemyManager.updateLogicMap(objects); // 初始逻辑图
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    shootBullet();
                } else {
                    lastKeyPressed = e.getKeyCode();
                }
                keysPressed.add(e.getKeyCode());
                // 开始播放坦克移动音效
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keysPressed.remove(e.getKeyCode());
                if (e.getKeyCode() == lastKeyPressed) {
                    lastKeyPressed = -1;
                }
                // 如果没有按下方向键，停止播放移动音效，并播放停止音效
                if (keysPressed.isEmpty()) {
                    isMoving = false;
                    stopLoopSound();
                    //loopSound("wav/坦克停止.wav");
                    playSound("wav/坦克停止.wav");
                }
            }
        });

        timer = new Timer(30, this);
        timer.start();
    }

    public void drawTextCentered(String text, int centerX, int yStart) {
        text = text.toUpperCase();
        int spacing = 32; // 字符间距
        int totalWidth = 0;
    
        // 计算总宽度
        for (char c : text.toCharArray()) {
            int[][] matrix = getCharMatrix(c);
            if (matrix != null) {
                totalWidth += matrix[0].length * 32 + spacing;
            }
        }
    
        int startX = centerX - totalWidth / 2;
        for (char c : text.toCharArray()) {
            int[][] matrix = getCharMatrix(c);
            if (matrix == null) continue;
    
            for (int row = 0; row < matrix.length; row++) {
                for (int col = 0; col < matrix[row].length; col++) {
                    if (matrix[row][col] == 1) {
                        createObjectAtPosition(startX + col * 32, yStart + row * 32, "assets/tile_16.png", 1, 32, 32);
                    }
                }
            }
            startX += matrix[0].length * 32 + spacing;
        }
    }
    
    public int[][] getCharMatrix(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'H' -> new int[][] {
                {1,0,1},
                {1,0,1},
                {1,1,1},
                {1,0,1},
                {1,0,1}
            };
            case 'E' -> new int[][] {
                {1,1,1},
                {1,0,0},
                {1,1,0},
                {1,0,0},
                {1,1,1}
            };
            case 'L' -> new int[][] {
                {1,0,0},
                {1,0,0},
                {1,0,0},
                {1,0,0},
                {1,1,1}
            };
            case 'O' -> new int[][] {
                {0,1,0},
                {1,0,1},
                {1,0,1},
                {1,0,1},
                {0,1,0}
            };
            case 'W' -> new int[][] {
                {1,0,0,0,1},
                {1,0,0,0,1},
                {1,0,1,0,1},
                {1,0,1,0,1},
                {0,1,0,1,0}
            };
            case 'R' -> new int[][] {
                {1,1,0},
                {1,0,1},
                {1,1,0},
                {1,0,1},
                {1,0,1}
            };
            case 'D' -> new int[][] {
                {1,1,0},
                {1,0,1},
                {1,0,1},
                {1,0,1},
                {1,1,0}
            };
            case '0' -> new int[][] {
                {0,1,0},
                {1,0,1},
                {1,0,1},
                {1,0,1},
                {0,1,0}
            };
            case '1' -> new int[][] {
                {0,1,0},
                {1,1,0},
                {0,1,0},
                {0,1,0},
                {1,1,1}
            };
            case '2' -> new int[][] {
                {1,1,0},
                {0,0,1},
                {0,1,0},
                {1,0,0},
                {1,1,1}
            };
            case '3' -> new int[][] {
                {1,1,0},
                {0,0,1},
                {0,1,0},
                {0,0,1},
                {1,1,0}
            };
            case '4' -> new int[][] {
                {1,0,1},
                {1,0,1},
                {1,1,1},
                {0,0,1},
                {0,0,1}
            };
            case '5' -> new int[][] {
                {1,1,1},
                {1,0,0},
                {1,1,0},
                {0,0,1},
                {1,1,0}
            };
            case '6' -> new int[][] {
                {0,1,1},
                {1,0,0},
                {1,1,0},
                {1,0,1},
                {0,1,0}
            };
            case '7' -> new int[][] {
                {1,1,1},
                {0,0,1},
                {0,1,0},
                {0,1,0},
                {0,1,0}
            };
            case '8' -> new int[][] {
                {0,1,0},
                {1,0,1},
                {0,1,0},
                {1,0,1},
                {0,1,0}
            };
            case '9' -> new int[][] {
                {0,1,0},
                {1,0,1},
                {0,1,1},
                {0,0,1},
                {1,1,0}
            };
            default -> null;
        };
    }
    
    
    
    private void handleEnemyBullets() {
        List<EnemyBullet> bullets = enemyManager.getAllBullets();
        for (EnemyBullet b : new ArrayList<>(bullets)) {
            Rectangle bulletRect = b.getBounds();
    
            // 撞墙或障碍物
            for (GameObject obj : new ArrayList<>(objects)) {
                if (bulletRect.intersects(obj.getBounds())) {
                    if (!obj.canBulletPass()) {
                        if (obj.canBeDestroyedByBullet()) {
                            //playSound("wav/爆炸.wav");//敌人子弹打到砖块
                            createExplosion(obj.x, obj.y, 32, 32);
                            objects.remove(obj);
                        } else {
                            playSound("wav/子弹打到墙.wav");
                            createExplosion(b.x, b.y, 32, 32);
                        }
                        enemyManager.removeBullet(b);
                        break;
                    }
                }
            }
    
            // 撞玩家
            Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
            if (bulletRect.intersects(playerRect)) {
                playSound("wav/爆炸.wav");
                createExplosion(playerX, playerY, 32, 32);
                resetPlayer();
                enemyManager.removeBullet(b);
            }
        }
    }
    
    private void handlePlayerVsEnemies() {
        Iterator<Bullet> it = bulletList.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            for (Enemy e : new ArrayList<>(enemyManager.getEnemies())) {
                if (b.getBounds().intersects(e.getBounds())) {
                    playSound("wav/爆炸.wav");
                    createExplosion(e.x, e.y, 32, 32);
                    enemyManager.removeEnemy(e);
                    it.remove();
                    break;
                }
            }
        }
    }
    
    private void handleBulletCollisions() {
        List<EnemyBullet> eBullets = enemyManager.getAllBullets();
        Iterator<Bullet> playerIt = bulletList.iterator();
    
        while (playerIt.hasNext()) {
            Bullet pb = playerIt.next();
            Rectangle pbRect = pb.getBounds();
    
            for (EnemyBullet eb : new ArrayList<>(eBullets)) {
                if (pbRect.intersects(eb.getBounds())) {
                    createExplosion(pb.x, pb.y, 32, 32);
                    playSound("wav/子弹打到墙.wav");
                    playerIt.remove();
                    enemyManager.removeBullet(eb);
                    break;
                }
            }
        }
    }
    
    private void resetPlayer() {
        stopLoopSound();
        playerX = playerSpawn.x;
        playerY = playerSpawn.y;
        updatePlayerImage("up");
    }
    
    

    // 播放音频方法
    private void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // 播放音频
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * 循环播放音效（用于坦克移动）
     */
    private void loopSound(String filePath) {
        try {
            if (moveClip != null && moveClip.isRunning()) {
                return; // 如果已经在播放，不重复播放
            }
            
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            moveClip = AudioSystem.getClip();
            moveClip.open(audioStream);
            moveClip.loop(Clip.LOOP_CONTINUOUSLY); // 循环播放
            moveClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放循环音效（坦克停止时调用）
     */
    private void stopLoopSound() {
        if (moveClip != null) {
            moveClip.stop();
            moveClip.close();
            moveClip = null;
        }
    }

    // 修改createObjectAtPosition方法，增加宽度和高度参数
    private void createObjectAtPosition(int x, int y, String imagePath, int type, int width, int height) {
        try {
            Image objectImage = ImageIO.read(new File(imagePath));
            objects.add(new GameObject(x, y, objectImage, type, width, height));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shootBullet() {
        if (bulletList.size() < maxBullets) { // 限制最大子弹数量
            int bulletX = playerX + playerWidth / 2 - 5;
            int bulletY = playerY + playerHeight / 2 - 5;
            bulletList.add(new Bullet(bulletX, bulletY, playerDirection, bulletSpeed));
            playSound("wav/子弹发射.wav");
        }
    }

    private void updatePlayerPosition() {
        if (lastKeyPressed != -1) {
            int newX = playerX;
            int newY = playerY;
    
            /*switch (lastKeyPressed) {
                case KeyEvent.VK_W:
                    newY -= playerSpeed;
                    break;
                case KeyEvent.VK_S:
                    newY += playerSpeed;
                    break;
                case KeyEvent.VK_A:
                    newX -= playerSpeed;
                    break;
                case KeyEvent.VK_D:
                    newX += playerSpeed;
                    break;
            }*/
            switch (lastKeyPressed) {
                case KeyEvent.VK_UP:  // 上箭头
                    newY -= playerSpeed;
                    if (!isMoving) {
                        isMoving = true;
                        stopLoopSound();
                        loopSound("wav/坦克移动.wav");
                    }
                    break;
                case KeyEvent.VK_DOWN:  // 下箭头
                    newY += playerSpeed;
                    if (!isMoving) {
                        isMoving = true;
                        stopLoopSound();
                        loopSound("wav/坦克移动.wav");
                    }
                    break;
                case KeyEvent.VK_LEFT:  // 左箭头
                    newX -= playerSpeed;
                    if (!isMoving) {
                        isMoving = true;
                        stopLoopSound();
                        loopSound("wav/坦克移动.wav");
                    }
                    break;
                case KeyEvent.VK_RIGHT:  // 右箭头
                    newX += playerSpeed;
                    if (!isMoving) {
                        isMoving = true;
                        stopLoopSound();
                        loopSound("wav/坦克移动.wav");
                    }
                    break;
            }
            
            // 检查玩家是否可以移动到新的位置，且不能超出地图边界
            if (canPlayerMove(newX, newY)) {
                playerX = newX;
                playerY = newY;
            }
    
            updatePlayerImage(lastKeyPressed == KeyEvent.VK_UP ? "up" :
                    lastKeyPressed == KeyEvent.VK_DOWN ? "down" :
                            lastKeyPressed == KeyEvent.VK_LEFT ? "left" : "right");
    
            // 更新窗口标题显示玩家的坐标
            JFrame topLevelAncestor = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (topLevelAncestor != null) {
                topLevelAncestor.setTitle("坦克大战 - 菜鸟八哥 - QQ2380494437 - 群675644084 - 玩家坐标: (X：" + playerX + ", Y：" + playerY + ")");
            }
        }
    }
    

    // 检查玩家是否可以移动到新的位置，且不能超出地图边界
    /*private boolean canPlayerMove(int newX, int newY) {
        // 限制玩家不能移出地图
        if (newX < 0 || newX + playerWidth > mapWidth || newY < 0 || newY + playerHeight > mapHeight) {
            return false;
        }
        for (GameObject object : objects) {
            if (!object.canPlayerPass() && object.getBounds().intersects(new Rectangle(newX, newY, playerWidth, playerHeight))) {
                return false; // 如果碰到不可通过的物体，玩家无法移动
            }
        }
        return true;
    }*/
    private boolean canPlayerMove(int newX, int newY) {
        Rectangle future = new Rectangle(newX, newY, playerWidth, playerHeight);
    
        if (newX < 0 || newX + playerWidth > mapWidth || newY < 0 || newY + playerHeight > mapHeight) {
            return false;
        }
    
        for (GameObject object : objects) {
            if (!object.canPlayerPass() && object.getBounds().intersects(future)) {
                return false;
            }
        }
    
        for (Enemy enemy : enemyManager.getEnemies()) {
            if (enemy.getBounds().intersects(future)) {
                return false;
            }
        }
    
        return true;
    }
    

    private void updatePlayerImage(String direction) {
        try {
            playerDirection = direction;
            switch (direction) {
                case "up":
                    playerImage = ImageIO.read(new File("assets/tile_50.png"));
                    break;
                case "left":
                    playerImage = ImageIO.read(new File("assets/tile_52.png"));
                    break;
                case "down":
                    playerImage = ImageIO.read(new File("assets/tile_54.png"));
                    break;
                case "right":
                    playerImage = ImageIO.read(new File("assets/tile_56.png"));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, mapWidth, mapHeight); // 使用新的宽度和保持高度
        g.drawImage(playerImage, playerX, playerY, playerWidth, playerHeight, this);

        // 绘制所有物体
        for (GameObject object : objects) {
            g.drawImage(object.image, object.x, object.y, object.width, object.height, this); // 使用物体的宽高
        }

        g.setColor(Color.RED);
        for (Bullet bullet : bulletList) {
            g.fillOval(bullet.x, bullet.y, 10, 10);
        }

        // 绘制爆炸效果，爆炸图像的尺寸是由爆炸对象决定的
        for (Explosion explosion : explosions) {
            g.drawImage(explosion.image, explosion.x, explosion.y, explosion.width, explosion.height, this);
        }
        enemyManager.draw(g, this);
        ally.draw(g, this);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updatePlayerPosition();
        updateBullets();
        updateExplosions();
    
        // 更新队友逻辑
        ally.update(
            enemyManager.getEnemies(),
            enemyManager.getAllBullets(),
            logicMapFromObjects(),
            objects,
            mapWidth,
            mapHeight,
            () -> playSound("wav/子弹发射.wav")
        );
    
        repaint();
    
        // 敌人行为
        enemyManager.updateLogicMap(objects);
        //enemyManager.updateEnemies(objects);
        enemyManager.updateEnemies(objects, ally);
    
        // 各类碰撞检测
        handleEnemyBullets();             // 敌人打玩家
        handlePlayerVsEnemies();         // 玩家打敌人
        handleBulletCollisions();        // 玩家和敌人子弹互相抵消
        spawnRandomBrick();              // 自动生成砖块
        handleEnemyBulletHitAlly();      // 敌人打中队友
        handleAllyBulletsHitEnemies();   //队友打中敌人
        handleAllyBulletsVsEnemyBullets();//队友子弹打到墙
        handleAllyBulletsHitWalls(); // ✅ 队友子弹打砖块
    }
    private void handleAllyBulletsHitWalls() {
        Iterator<Bullet> it = ally.getBullets().iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            Rectangle bulletRect = bullet.getBounds();
    
            for (Iterator<GameObject> objIt = objects.iterator(); objIt.hasNext(); ) {
                GameObject obj = objIt.next();
    
                if (bulletRect.intersects(obj.getBounds())) {
                    if (!obj.canBulletPass()) {
                        // 如果可以被摧毁（type=1），就移除
                        if (obj.canBeDestroyedByBullet()) {
                            playSound("wav/爆炸.wav");
                            createExplosion(obj.x, obj.y, 32, 32);
                            objIt.remove();
                        } else {
                            playSound("wav/子弹打到墙.wav");
                            createExplosion(bullet.x, bullet.y, 32, 32);
                        }
                        it.remove(); // 移除子弹
                        break;
                    }
                }
            }
        }
    }
    
    private void handleEnemyBulletHitAlly() {
        if (!ally.isAlive()) return;
    
        Rectangle allyRect = ally.getBounds();
        for (EnemyBullet b : new ArrayList<>(enemyManager.getAllBullets())) {
            if (allyRect.intersects(b.getBounds())) {
                playSound("wav/爆炸.wav");
                createExplosion(ally.x, ally.y, 32, 32);
                enemyManager.removeBullet(b);
                ally.kill();
                break;
            }
        }
    }
    private void handleAllyBulletsHitEnemies() {
        Iterator<Bullet> it = ally.getBullets().iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            for (Enemy e : new ArrayList<>(enemyManager.getEnemies())) {
                if (b.getBounds().intersects(e.getBounds())) {
                    playSound("wav/爆炸.wav");
                    createExplosion(e.x, e.y, 32, 32);
                    enemyManager.removeEnemy(e);
                    it.remove();
                    ally.incrementKill();
                    break;
                }
            }
        }
    }
    
    private void handleAllyBulletsVsEnemyBullets() {
        Iterator<Bullet> aIt = ally.getBullets().iterator();
        List<EnemyBullet> eBullets = enemyManager.getAllBullets();
    
        while (aIt.hasNext()) {
            Bullet ab = aIt.next();
            Rectangle abRect = ab.getBounds();
    
            for (EnemyBullet eb : new ArrayList<>(eBullets)) {
                if (abRect.intersects(eb.getBounds())) {
                    createExplosion(ab.x, ab.y, 32, 32);
                    playSound("wav/子弹打到墙.wav");
                    aIt.remove();
                    enemyManager.removeBullet(eb);
                    break;
                }
            }
        }
    }
    
    private int[][] logicMapFromObjects() {
        int cols = mapWidth / 32;
        int rows = mapHeight / 32;
        int[][] map = new int[cols][rows];
    
        for (GameObject obj : objects) {
            if (!obj.canPlayerPass()) {
                int cx = obj.x / 32;
                int cy = obj.y / 32;
                map[cx][cy] = 1;
            }
        }
        return map;
    }
    
    /*private int[][] logicMapFromObjects() {
        int cols = mapWidth / 32;
        int rows = mapHeight / 32;
        int[][] map = new int[cols][rows];
    
        for (GameObject obj : objects) {
            if (!obj.canPlayerPass()) {
                int cx = obj.x / 32;
                int cy = obj.y / 32;
    
                // 向周围一圈扩展障碍，确保实体体积能避开
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = cx + dx;
                        int ny = cy + dy;
                        if (nx >= 0 && ny >= 0 && nx < cols && ny < rows) {
                            map[nx][ny] = 1;
                        }
                    }
                }
            }
        }
    
        return map;
    }*/
    
    private void spawnRandomBrick() {
        if (brickSpawnCooldown > 0) {
            brickSpawnCooldown--;
            return;
        }
    
        long currentBrickCount = objects.stream()
                .filter(obj -> obj.type == 1)
                .count();
    
        int bricksToSpawn = (int) (maxBricks - currentBrickCount);
        if (bricksToSpawn <= 0) return;
    
        Random rand = new Random();
        int attempts = 0;
        int spawned = 0;
    
        while (spawned < bricksToSpawn && attempts < 200) {
            int x = rand.nextInt(mapWidth / 32) * 32;
            int y = rand.nextInt(mapHeight / 32) * 32;
            Rectangle newBrick = new Rectangle(x, y, 32, 32);
    
            boolean overlap =
                    // 与玩家重叠
                    new Rectangle(playerX + 2, playerY + 2, playerWidth - 4, playerHeight - 4).intersects(newBrick) ||
                    // 与敌人重叠
                    enemyManager.getEnemies().stream().anyMatch(e -> newBrick.intersects(e.getBounds())) ||
                    // 与已有物体重叠
                    objects.stream().anyMatch(obj -> obj.getBounds().intersects(newBrick));
    
            if (!overlap) {
                createObjectAtPosition(x, y, "assets/tile_16.png", 1, 32, 32);
                spawned++;
            }
    
            attempts++;
        }
    
        brickSpawnCooldown = 200; // 再过若干帧才补下一轮
    }
    

    private void updateBullets() {
        Iterator<Bullet> iterator = bulletList.iterator();
        while (iterator.hasNext()) {
            Bullet bullet = iterator.next();
            bullet.move();

            // 检查子弹与物体的碰撞
            for (GameObject object : objects) {
                if (bullet.getBounds().intersects(object.getBounds())) {
                    // 如果物体不能被子弹摧毁，并且物体类型不允许子弹穿过，子弹应该消失
                    if (!object.canBeDestroyedByBullet() && !object.canBulletPass()) {
                        playSound("wav/子弹打到墙.wav");
                        createExplosion(bullet.x, bullet.y, 32, 32); // 自定义爆炸尺寸
                        iterator.remove();
                        break;
                    }
                    // 如果物体类型是1，表示被子弹摧毁，物体消失
                    if (object.canBeDestroyedByBullet()) {
                        playSound("wav/爆炸.wav");
                        createExplosion(object.x, object.y, 32, 32); // 自定义爆炸尺寸
                        objects.remove(object);
                        iterator.remove();
                        break;
                    }
                }
            }

            // 如果子弹超出屏幕边界，则移除
            if (bullet.x < 0 || bullet.x > mapWidth || bullet.y < 0 || bullet.y > mapHeight) {
                iterator.remove();
            }
        }
    }

    // 创建爆炸效果，并支持自定义尺寸
    private void createExplosion(int x, int y, int width, int height) {
        try {
            Image explosionImage = ImageIO.read(new File("assets/tile_217.png"));
            explosions.add(new Explosion(x-12, y-12, explosionImage, width, height));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 更新爆炸效果的生命周期
    private void updateExplosions() {
        Iterator<Explosion> iterator = explosions.iterator();
        while (iterator.hasNext()) {
            Explosion explosion = iterator.next();
            explosion.lifetime--;

            if (explosion.lifetime <= 0) {
                iterator.remove(); // 爆炸效果持续150毫秒后消失
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("坦克大战 - 菜鸟八哥 - QQ2380494437 - 群675644084");
        SimpleGame gamePanel = new SimpleGame();
        frame.add(gamePanel);
        frame.setSize(1264, 870); // 修改为新的窗口大小
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

// 用于表示游戏中的物体
/*class GameObject {
    int x, y;
    Image image;
    int type; // 物体的类型，决定其属性
    int width, height; // 新增的物体宽高

    // 物体的四种类型：1: 子弹不可穿过，玩家不可穿过，可被子弹摧毁
    // 2: 子弹不可穿过，玩家不可穿过，不可被子弹摧毁
    // 3: 子弹可穿过，玩家不可穿过，不可被子弹摧毁
    // 4: 子弹可穿过，玩家可穿过，不可被子弹摧毁

    public GameObject(int x, int y, Image image, int type, int width, int height) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.type = type;
        this.width = width;
        this.height = height;
    }

    // 检查是否允许子弹通过物体
    public boolean canBulletPass() {
        return type == 3 || type == 4; // 子弹可穿过的类型
    }

    // 检查玩家是否可以通过物体
    public boolean canPlayerPass() {
        return type == 4; // 玩家可穿过的类型
    }

    // 检查物体是否可以被子弹摧毁
    public boolean canBeDestroyedByBullet() {
        return type == 1; // 可以被子弹摧毁的类型
    }

    // 获取物体的边界（用于检测碰撞）
    public Rectangle getBounds() {
        //return new Rectangle(x, y, width, height); // 使用传入的宽高
        return new Rectangle(x + 2, y + 2, width - 4, height - 4);
    }
}*/

// 爆炸效果类，支持自定义尺寸
class Explosion {
    int x, y;
    Image image;
    int width, height;
    int lifetime; // 爆炸持续时间（150毫秒）

    public Explosion(int x, int y, Image image, int width, int height) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.width = width;
        this.height = height;
        this.lifetime = 5; // 设置爆炸的生命周期为5个更新周期（150毫秒）
    }
}
/*
class Bullet {
    int x, y;
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
            case "up":
                y -= speed;
                break;
            case "down":
                y += speed;
                break;
            case "left":
                x -= speed;
                break;
            case "right":
                x += speed;
                break;
        }
    }

    // 获取子弹的边界（用于检测碰撞）
    public Rectangle getBounds() {
        return new Rectangle(x, y, 10, 10);
    }
}*/
