package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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

public class Main extends Application {

    private List<File> musicFiles = new ArrayList<>();
    private MediaPlayerFactory mediaPlayerFactory;
    private MediaPlayer mediaPlayer;
    private ListView<String> playListView;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Аудио-плеер на VLCJ");

        playListView = new ListView<>();

        Button addButton = new Button("Добавить музыку");
        Button playButton = new Button("▶ Воспроизвести");
        Button pauseButton = new Button("⏸ Пауза");
        Button stopButton = new Button("⏹ Остановить");
        Button addMusicInPlatforms = new Button("Добавить музыку из SoundCloud");

        // Инициализация VLCJ
        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();

        addButton.setOnAction(event -> handleAddButton(stage));

        playButton.setOnAction(event -> {
            int index = playListView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                playMusic(index);
            }
        });

        pauseButton.setOnAction(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.controls().pause();
            }
        });

        stopButton.setOnAction(event -> {
            if (mediaPlayer != null) {
                mediaPlayer.controls().stop();
            }
        });

        HBox controls = new HBox(10, playButton, pauseButton, stopButton);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(10, addButton, playListView, controls);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 400, 500);

        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        loadSounds();
    }

    private void loadSounds() {
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

    private void handleAddButton(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите песню");
        // Можно расширить список форматов
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Аудио файлы", "*.mp3", "*.wav", "*.ogg")
        );
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            musicFiles.addAll(selectedFiles);
            for (File file : selectedFiles) {
                playListView.getItems().add(file.getName());
            }
        }
        saveSounds();
    }

    private void saveSounds() {
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


    private void playMusic(int index) {
        if (mediaPlayer != null && !musicFiles.isEmpty()) {
            String mediaPath = musicFiles.get(index).getAbsolutePath();
            System.out.println("Playing media: " + mediaPath);

            mediaPlayer.controls().stop();

            mediaPlayer.media().play(mediaPath);
        }
    }

    @Override
    public void stop() throws Exception {
        // Освобождаем ресурсы VLCJ при закрытии
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
