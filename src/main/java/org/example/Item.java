package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Item extends ImageView {
    public Item(double x, double y, double width, double height) {
        super(new Image(Item.class.getResourceAsStream("/chest.png")));
        this.setFitWidth(width);
        this.setFitHeight(height);
        this.setLayoutX(x);
        this.setLayoutY(y);
    }
}
