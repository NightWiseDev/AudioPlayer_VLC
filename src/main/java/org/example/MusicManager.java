package org.example;

import javafx.application.Application;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class MusicManager {

    private final List<File> musicFiles;
    private final ListView<String> playListView;

    public void loadSounds() {
        try {
            URL musicUrl = getClass().getResource("/music");
            File folder;
            if (musicUrl == null) {
                folder = new File(musicUrl.toURI());
            } else {
                folder = new File("src/main/resources/music");
            }
            File[] files = folder.listFiles();
            if (folder.exists() && folder.isDirectory()) {
                if (files != null) {
                    for (File f : files) {
                        musicFiles.add(f);
                        playListView.getItems().add(f.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Возникла ошибка: " + e.getMessage());
        }
    }
    public void saveSounds() {
        try {
            Path destinationDir = Paths.get("src/main/resources/music");
            Files.createDirectories(destinationDir);

            for (File file : musicFiles) {
                Path source = file.toPath();
                Path destination = destinationDir.resolve(file.getName());

                Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.out.println("Возникла ошибка: " + e.getMessage());
        }
    }
}
