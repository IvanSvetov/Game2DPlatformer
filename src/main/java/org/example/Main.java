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


        backgroundView.setFitWidth(WINDOW_WIDTH);
        backgroundView.setFitHeight(WINDOW_HEIGHT + 100);
        root.getChildren().add(backgroundView);


        characterView.setFitWidth(60);  // Увеличение ширины персонажа
        characterView.setFitHeight(84); // Увеличение высоты персонажа
        characterView.setLayoutX(100);
        characterView.setLayoutY(GROUND_LEVEL - characterView.getFitHeight()); // Устанавливаем начальную позицию чуть выше уровня земли
        root.getChildren().add(characterView);


        gameOverImageView.setFitWidth(300); // Устанавливаем ширину кнопки
        gameOverImageView.setFitHeight(300); // Устанавливаем высоту кнопки
        gameOverImageView.setLayoutX(250); // Устанавливаем положение по оси X
        gameOverImageView.setLayoutY(20); // Устанавливаем положение по оси Y
        gameOverImageView.setOnMouseClicked(event -> resetGame()); // Обработчик события для кнопки
        gameOverImageView.setVisible(false); // Начально кнопка не видна
        root.getChildren().add(gameOverImageView);


        resetImageView.setFitWidth(100); // Устанавливаем ширину кнопки
        resetImageView.setFitHeight(50); // Устанавливаем высоту кнопки
        resetImageView.setLayoutX(350); // Устанавливаем положение по оси X
        resetImageView.setLayoutY(250); // Устанавливаем положение по оси Y
        resetImageView.setOnMouseClicked(event -> resetGame()); // Обработчик события для кнопки
        resetImageView.setVisible(false); // Начально кнопка не видна
        root.getChildren().add(resetImageView);

        // Добавляем платформы
        platforms.add(new Platform(200, 450, 100, 20));
        platforms.add(new Platform(400, 350, 100, 20));
        platforms.add(new Platform(600, 250, 100, 20));

        for (ImageView platform : platforms) {
            root.getChildren().add(platform);
        }

        // Добавляем врагов
        enemies.add(new Enemy(300, 420, 90, 70, -2)); // Враг движется влево
        enemies.add(new Enemy(500, 320, 90, 70, 2)); // Враг движется вправо

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

        timer = new AnimationTimer() {
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
                                gameOverImageView.setVisible(true);
                                resetImageView.setVisible(true);
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

    private void resetGame() {
        // Возвращаем персонажа на начальную позицию
        characterView.setLayoutX(100);
        characterView.setLayoutY(530);

        // Инициализируем списки заново
        platforms = new ArrayList<>();
        enemies = new ArrayList<>();
        items = new ArrayList<>();

        // Добавляем платформы
        platforms.add(new Platform(200, 450, 100, 20));
        platforms.add(new Platform(400, 350, 100, 20));
        platforms.add(new Platform(600, 250, 100, 20));


        // Добавляем врагов
        enemies.add(new Enemy(300, 420, 90, 70, -2)); // Враг движется влево
        enemies.add(new Enemy(500, 320, 90, 70, 2)); // Враг движется вправо


        // Добавляем предметы (сундуки)
        items.add(new Item(350, 400, 50, 50));
        items.add(new Item(550, 300, 50, 50));



        // Сбрасываем значения переменных, связанных с игровым процессом
        score = 0;
        health = 3;

        // Обновляем отображение счета и здоровья
        scoreText.setText("Score: " + score);
        healthText.setText("Health: " + health);

        // Скрываем изображение "Game Over" и кнопку "Reset"
        gameOverImageView.setVisible(false);
        resetImageView.setVisible(false);

        // Запускаем игру заново
        timer.start();
    }



    public static void main(String[] args) {

        launch(args);
    }
}
