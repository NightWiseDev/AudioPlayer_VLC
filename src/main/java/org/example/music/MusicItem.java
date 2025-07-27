package org.example.music;

import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class MusicItem {

    private File file;
    private Label nameLabel;
    private Label timeLabel;

    public MusicItem(File file,Label nameLabel,Label timeLabel) {
        this.file = file;
        this.nameLabel = nameLabel;
        this.timeLabel = timeLabel;
    }
    public String getName() {
        return file.getName().replace(".mp3", "");
    }
}
