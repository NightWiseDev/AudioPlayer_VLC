package org.example.music;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;


public class MusicCeil extends ListCell<MusicItem> {

    @Override
    protected void updateItem(MusicItem item,boolean empty) {
        super.updateItem(item,empty);

        if(empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {

            Label nameLabel = new Label(item.getName());

            HBox layout = new HBox(10,nameLabel,item.getTimeLabel());
            layout.setPadding(new Insets(5));
            layout.setSpacing(30);

            setGraphic(layout);
        }
    }
}
