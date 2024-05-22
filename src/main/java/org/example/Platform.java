package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class Platform extends ImageView {
    public Platform(double x, double y, double width, double height) {
        super(new Image(Platform.class.getResourceAsStream("/platform.png")));
        this.setFitWidth(width);
        this.setFitHeight(height);
        this.setLayoutX(x);
        this.setLayoutY(y);
    }
}