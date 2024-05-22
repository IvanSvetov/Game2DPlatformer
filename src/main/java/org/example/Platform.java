package org.example;

import javafx.scene.shape.Rectangle;

public class Platform extends Rectangle {
    public Platform(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.setStyle("-fx-fill: gray;");
    }
}