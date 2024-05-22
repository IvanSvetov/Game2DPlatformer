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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Main extends Application {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -13;
    private static final double MOVE_SPEED = 5;
    private static final double GROUND_LEVEL = 600;

    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    private boolean isUpPressed = false;

    private double velocityY = 0;
    private boolean canJump = true;

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

    private List<ImageView> platforms = new ArrayList<>();
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

        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode(), true));
        scene.setOnKeyReleased(event -> handleKeyPress(event.getCode(), false));

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGame(root);
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
        platforms.add(new Platform(200, 450, 100, 20));
        platforms.add(new Platform(400, 350, 100, 20));
        platforms.add(new Platform(600, 250, 100, 20));

        for (ImageView platform : platforms) {
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

    private void handleKeyPress(KeyCode code, boolean isPressed) {
        switch (code) {
            case LEFT -> isLeftPressed = isPressed;
            case RIGHT -> isRightPressed = isPressed;
            case UP -> isUpPressed = isPressed;
        }
    }

    private void updateGame(Pane root) {
        double x = characterView.getLayoutX();
        double y = characterView.getLayoutY();

        if (isLeftPressed) {
            characterView.setLayoutX(x - MOVE_SPEED);
        }
        if (isRightPressed) {
            characterView.setLayoutX(x + MOVE_SPEED);
        }
        if (isUpPressed && canJump) {
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
        for (ImageView platform : platforms) {
            if (characterView.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                double characterBottom = characterView.getBoundsInParent().getMaxY();
                double platformTop = platform.getBoundsInParent().getMinY();
                if (characterBottom <= platformTop + 10 && characterBottom >= platformTop - 10) {
                    characterView.setLayoutY(platform.getLayoutY() - characterView.getFitHeight() + 5);
                    velocityY = 0;
                    canJump = true;
                    onPlatform = true;
                }
            }
        }
        if (!onPlatform && y < GROUND_LEVEL - characterView.getFitHeight()) {
            canJump = false;
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
                    if (health <= 0) {
                        gameOverImageView.setVisible(true);
                        resetImageView.setVisible(true);
                        timer.stop();
                        System.out.println("Game Over!");
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

    private void resetGame(Pane root) {
        root.getChildren().removeAll(platforms);
        root.getChildren().removeAll(enemies);
        root.getChildren().removeAll(items);

        characterView.setLayoutX(100);
        characterView.setLayoutY(GROUND_LEVEL - characterView.getFitHeight());

        setupPlatforms(root);
        setupEnemies(root);
        setupItems(root);

        score = 0;
        health = 3;

        scoreText.setText("Score: " + score);
        healthText.setText("Health: " + health);

        gameOverImageView.setVisible(false);
        resetImageView.setVisible(false);

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
