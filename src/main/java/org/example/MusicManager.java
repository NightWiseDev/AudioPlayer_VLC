package org.example;

import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.music.MusicItem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class MusicManager {

    private final List<File> musicFiles;
    private final ListView<MusicItem> playListView;

    public void loadSounds() {
        musicFiles.clear();
        playListView.getItems().clear();
        try {
            String userHome = System.getProperty("user.home");
            Path musicPath = Paths.get(userHome, ".nxAudioPlayer", "music");
            Files.createDirectories(musicPath); // если нет — создаст

            File[] files = musicPath.toFile().listFiles();
            if (files != null) {
                for (File f : files) {
                    musicFiles.add(f);
                    playListView.getItems().add(new MusicItem(f,new Label(f.getName()), new Label("")));
                }
            }
        } catch (Exception e) {
            System.out.println("Возникла ошибка: " + e.getMessage());
        }
    }
    public void saveSounds() {
        try {
            String userHome = System.getProperty("user.home");
            Path destinationDir = Paths.get(userHome, ".nxAudioPlayer", "music");
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
