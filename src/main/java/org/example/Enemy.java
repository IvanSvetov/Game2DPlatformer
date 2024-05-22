package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Enemy extends ImageView {
    private double velocityX;

    public Enemy(double x, double y, double width, double height, double velocityX) {
        super(new Image(Enemy.class.getResourceAsStream("/enemy.png")));
        this.velocityX = velocityX;
        this.setFitWidth(width);
        this.setFitHeight(height);
        this.setLayoutX(x);
        this.setLayoutY(y);
    }

    public void update() {
        this.setLayoutX(this.getLayoutX() + velocityX);

        // Меняем направление при достижении границы экрана
        if (this.getLayoutX() <= 0 || this.getLayoutX() >= Main.WINDOW_WIDTH - this.getFitWidth()) {
            velocityX = -velocityX;
        }
    }
}
