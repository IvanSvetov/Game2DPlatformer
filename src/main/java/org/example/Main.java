package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class Main extends Application {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -13;
    private static final double MOVE_SPEED = 5;
    private static final double GROUND_LEVEL = 600;
    private static final long INVINCIBILITY_DURATION = 2000; // 2 seconds

    private Map<KeyCode, Boolean> keys = new HashMap<>();
    private double velocityY = 0;
    private boolean canJump = true;
    private long invincibilityTimer = 0;

    Image backgroundImage = new Image(getClass().getResourceAsStream("/background.png"));
    ImageView backgroundView = new ImageView(backgroundImage);

    Image characterImage = new Image(getClass().getResourceAsStream("/character.png"));
    ImageView characterView = new ImageView(characterImage);

    Image gameOverImage = new Image(getClass().getResourceAsStream("/gameover.png"));
    ImageView gameOverImageView = new ImageView(gameOverImage);

    Image resetImage = new Image(getClass().getResourceAsStream("/reset.png"));
    ImageView resetImageView = new ImageView(resetImage);

    Text scoreText = new Text(10, 20, "Score: 0");
    Text healthText = new Text(10, 40, "Health: 3");

    private AnimationTimer timer;

    private List<Platform> platforms = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Item> items = new ArrayList<>();

    private int score = 0;
    private int health = 3;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Pane root = new Pane();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle("Java Platformer Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        setupGame(root);

        scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
        scene.setOnKeyReleased(event -> keys.put(event.getCode(), false));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame(root, now);
            }
        };
        timer.start();
    }

    private void setupGame(Pane root) {
        root.getChildren().clear();

        backgroundView.setFitWidth(WINDOW_WIDTH);
        backgroundView.setFitHeight(WINDOW_HEIGHT + 100);
        root.getChildren().add(backgroundView);

        characterView.setFitWidth(60);
        characterView.setFitHeight(84);
        characterView.setLayoutX(100);
        characterView.setLayoutY(GROUND_LEVEL - characterView.getFitHeight());
        root.getChildren().add(characterView);

        gameOverImageView.setFitWidth(300);
        gameOverImageView.setFitHeight(300);
        gameOverImageView.setLayoutX(250);
        gameOverImageView.setLayoutY(20);
        gameOverImageView.setOnMouseClicked(event -> resetGame(root));
        gameOverImageView.setVisible(false);
        root.getChildren().add(gameOverImageView);

        resetImageView.setFitWidth(100);
        resetImageView.setFitHeight(50);
        resetImageView.setLayoutX(350);
        resetImageView.setLayoutY(250);
        resetImageView.setOnMouseClicked(event -> resetGame(root));
        resetImageView.setVisible(false);
        root.getChildren().add(resetImageView);

        setupPlatforms(root);
        setupEnemies(root);
        setupItems(root);

        root.getChildren().addAll(scoreText, healthText);
    }

    private void setupPlatforms(Pane root) {
        platforms.clear();
        platforms.add(new Platform(200, 450, 100, 50));
        platforms.add(new Platform(400, 350, 100, 50));
        platforms.add(new Platform(600, 250, 100, 50));

        for (Platform platform : platforms) {
            root.getChildren().add(platform);
        }
    }

    private void setupEnemies(Pane root) {
        enemies.clear();
        enemies.add(new Enemy(300, 420, 90, 70, -2));
        enemies.add(new Enemy(500, 320, 90, 70, 2));

        for (Enemy enemy : enemies) {
            root.getChildren().add(enemy);
        }
    }

    private void setupItems(Pane root) {
        items.clear();
        items.add(new Item(350, 400, 50, 50));
        items.add(new Item(550, 300, 50, 50));

        for (Item item : items) {
            root.getChildren().add(item);
        }
    }

    private void updateGame(Pane root, long now) {
        double x = characterView.getLayoutX();
        double y = characterView.getLayoutY();

        if (isPressed(KeyCode.LEFT)) {
            characterView.setLayoutX(x - MOVE_SPEED);
        }
        if (isPressed(KeyCode.RIGHT)) {
            characterView.setLayoutX(x + MOVE_SPEED);
        }
        if (isPressed(KeyCode.UP) && canJump) {
            velocityY = JUMP_FORCE;
            canJump = false;
        }

        velocityY += GRAVITY;
        characterView.setLayoutY(y + velocityY);

        if (characterView.getLayoutY() >= GROUND_LEVEL - characterView.getFitHeight()) {
            characterView.setLayoutY(GROUND_LEVEL - characterView.getFitHeight());
            velocityY = 0;
            canJump = true;
        }

        boolean onPlatform = false;
        for (Platform platform : platforms) {
            if (characterView.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                double characterBottom = characterView.getBoundsInParent().getMaxY();
                double platformTop = platform.getBoundsInParent().getMinY();
                if (characterBottom <= platformTop + 15 && characterBottom >= platformTop - 15) {
                    characterView.setLayoutY(platform.getLayoutY() - characterView.getFitHeight() + 10);
                    velocityY = 0;
                    canJump = true;
                    onPlatform = true;
                }
            }
        }
        if (!onPlatform && y < GROUND_LEVEL - characterView.getFitHeight()) {
            canJump = false;
        }

        if (invincibilityTimer > 0) {
            invincibilityTimer -= 16; // Assuming updateGame is called every ~16ms
        }

        updateEnemies(root);
        checkCollisions(root);
    }

    private void updateEnemies(Pane root) {
        for (Enemy enemy : enemies) {
            enemy.update();
        }
    }

    private void checkCollisions(Pane root) {
        checkEnemyCollisions(root);
        checkItemCollisions(root);
    }

    private void checkEnemyCollisions(Pane root) {
        if (invincibilityTimer > 0) {
            return; // Skip collision check if player is invincible
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (characterView.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                double y = characterView.getLayoutY();
                if (y + characterView.getFitHeight() <= enemy.getY() + 10 &&
                        y + characterView.getFitHeight() >= enemy.getY() - 10) {
                    root.getChildren().remove(enemy);
                    enemyIterator.remove();
                    score += 10;
                    scoreText.setText("Score: " + score);
                } else {
                    health -= 1;
                    healthText.setText("Health: " + health);
                    invincibilityTimer = INVINCIBILITY_DURATION;
                    if (health <= 0) {
                        gameOver();
                    }
                }
            }
        }
    }

    private void checkItemCollisions(Pane root) {
        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            if (characterView.getBoundsInParent().intersects(item.getBoundsInParent())) {
                root.getChildren().remove(item);
                itemIterator.remove();
                score += 5;
                scoreText.setText("Score: " + score);
            }
        }
    }

    private void gameOver() {
        gameOverImageView.setVisible(true);
        resetImageView.setVisible(true);
        timer.stop();
        System.out.println("Game Over!");
    }

    private void resetGame(Pane root) {
        health = 3;
        score = 0;
        healthText.setText("Health: " + health);
        scoreText.setText("Score: " + score);
        invincibilityTimer = 0;
        setupGame(root);
        timer.start();
    }

    private boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
