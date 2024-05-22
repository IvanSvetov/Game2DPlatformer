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
import java.util.List;

public class Main extends Application {

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -13;
    private static final double MOVE_SPEED = 5;
    private static final double GROUND_LEVEL = 600; // Новая высота земли

    private boolean isLeftPressed = false;
    private boolean isRightPressed = false;
    private boolean isUpPressed = false;

    private double velocityY = 0;
    private boolean canJump = true;

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

        Image backgroundImage = new Image(getClass().getResourceAsStream("/background.png"));
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(WINDOW_WIDTH);
        backgroundView.setFitHeight(WINDOW_HEIGHT + 100);
        root.getChildren().add(backgroundView);

        Image characterImage = new Image(getClass().getResourceAsStream("/character.png"));
        ImageView characterView = new ImageView(characterImage);
        characterView.setFitWidth(60);  // Увеличение ширины персонажа
        characterView.setFitHeight(84); // Увеличение высоты персонажа
        characterView.setLayoutX(100);
        characterView.setLayoutY(GROUND_LEVEL - characterView.getFitHeight()); // Устанавливаем начальную позицию чуть выше уровня земли
        root.getChildren().add(characterView);

        // Добавляем платформы
        platforms.add(new Platform(200, 450, 100, 20));
        platforms.add(new Platform(400, 350, 100, 20));
        platforms.add(new Platform(600, 250, 100, 20));

        for (ImageView platform : platforms) {
            root.getChildren().add(platform);
        }

        // Добавляем врагов
        enemies.add(new Enemy(300, 420, 70, 50, -2)); // Враг движется влево
        enemies.add(new Enemy(500, 320, 70, 50, 2)); // Враг движется вправо

        for (Enemy enemy : enemies) {
            root.getChildren().add(enemy);
        }

        // Добавляем предметы (сундуки)
        items.add(new Item(350, 400, 50, 50));
        items.add(new Item(550, 300, 50, 50));

        for (Item item : items) {
            root.getChildren().add(item);
        }

        // Добавляем текст для отображения очков и здоровья
        Text scoreText = new Text(10, 20, "Score: 0");
        Text healthText = new Text(10, 40, "Health: 3");
        root.getChildren().addAll(scoreText, healthText);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                isLeftPressed = true;
            } else if (event.getCode() == KeyCode.RIGHT) {
                isRightPressed = true;
            } else if (event.getCode() == KeyCode.UP && canJump) {
                isUpPressed = true;
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                isLeftPressed = false;
            } else if (event.getCode() == KeyCode.RIGHT) {
                isRightPressed = false;
            } else if (event.getCode() == KeyCode.UP) {
                isUpPressed = false;
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
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

                // Ограничение, чтобы персонаж не падал ниже "земли"
                if (characterView.getLayoutY() >= GROUND_LEVEL - characterView.getFitHeight()) {
                    characterView.setLayoutY(GROUND_LEVEL - characterView.getFitHeight());
                    velocityY = 0;
                    canJump = true;
                }

                // Проверка на столкновение с платформами
                boolean onPlatform = false;
                for (ImageView platform : platforms) {
                    if (characterView.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                        double characterBottom = characterView.getBoundsInParent().getMaxY();
                        double platformTop = platform.getBoundsInParent().getMinY();
                        if (characterBottom <= platformTop + 10 && characterBottom >= platformTop - 10) {
                            characterView.setLayoutY(platform.getLayoutY() - characterView.getFitHeight());
                            // Персонаж касается платформы
                            double newY = platform.getLayoutY() - characterView.getFitHeight() + 5; // Новая позиция персонажа
                            characterView.setLayoutY(newY);
                            velocityY = 0;
                            canJump = true;
                            onPlatform = true;
                        }
                    }
                }
                if (!onPlatform && y < GROUND_LEVEL - characterView.getFitHeight()) {
                    canJump = false;
                }

                // Движение врагов
                for (Enemy enemy : enemies) {
                    enemy.update();
                }

                // Проверка на столкновение с врагами
                for (Enemy enemy : enemies) {
                    if (characterView.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                        if (y + characterView.getFitHeight() <= enemy.getY() + 10 &&
                                y + characterView.getFitHeight() >= enemy.getY() - 10) {
                            root.getChildren().remove(enemy);
                            enemies.remove(enemy);
                            score += 10;
                            scoreText.setText("Score: " + score);
                            break;
                        } else {
                            health -= 1;
                            healthText.setText("Health: " + health);
                            if (health <= 0) {
                                stop();
                                System.out.println("Game Over!");
                            }
                        }
                    }
                }

                // Проверка на сбор предметов
                for (Item item : items) {
                    if (characterView.getBoundsInParent().intersects(item.getBoundsInParent())) {
                        root.getChildren().remove(item);
                        items.remove(item);
                        score += 5;
                        scoreText.setText("Score: " + score);
                        break;
                    }
                }
            }
        };
        timer.start();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
