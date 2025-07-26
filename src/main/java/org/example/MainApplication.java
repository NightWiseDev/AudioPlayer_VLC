package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.platforms.SoundCloudDownloader;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainApplication extends Application {

    private MusicManager musicManager;
    private AudioPlayer audioPlayer;
    private SoundCloudDownloader soundCloudDownloader;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        List<File> musicFiles;
        ListView<String> playListView;
        MediaPlayerFactory mediaPlayerFactory;
        MediaPlayer mediaPlayer;


        stage.setTitle("Аудио-плеер на VLCJ");

        playListView = new ListView<>();
        musicFiles = new ArrayList<>();

        Button addButton = new Button("Добавить музыку");
        Button playButton = new Button("▶ Воспроизвести");
        Button pauseButton = new Button("⏸ Пауза");
        Button stopButton = new Button("⏹ Остановить");
        Button addMusicInPlatforms = new Button("Добавить музыку из SoundCloud");

        // Инициализация VLCJ
        mediaPlayerFactory = new MediaPlayerFactory();
        mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer();

        initManagers(musicFiles,playListView);
        initAudioPlayer(mediaPlayerFactory,mediaPlayer);
        soundCloudDownloader = new SoundCloudDownloader();

        addButton.setOnAction(event -> handleAddButton(stage,musicFiles, playListView));

        playButton.setOnAction(event -> {
            int index = playListView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                audioPlayer.playMusic(index);
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
        addMusicInPlatforms.setOnAction(event -> handleAddMusicInPlatforms(stage));

        HBox controls = new HBox(10, playButton, pauseButton, stopButton,addMusicInPlatforms);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(10, addButton, playListView, controls);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 400, 500);

        scene.getStylesheets().add(getClass().getResource("/dark-theme.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        musicManager.loadSounds();
    }
    private void handleAddButton(Stage stage,List<File> musicFiles,ListView<String> playListView) {
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
        musicManager.saveSounds();
    }
    private void handleAddMusicInPlatforms(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить трек с soundcloud");
        dialog.setHeaderText("Введите ссылку на трек");
        dialog.setContentText("URL:");

        dialog.showAndWait().ifPresent(url -> {
               String outputDir = "src/main/resources/music";
                try {
                    soundCloudDownloader.download(url,outputDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    musicManager.loadSounds();
            });
        });
    }
    private void initManagers(List<File> musicFiles, ListView<String> listView) {
        musicManager = new MusicManager(musicFiles, listView);
    }
    private void initAudioPlayer(MediaPlayerFactory mediaPlayerFactory, MediaPlayer mediaPlayer) {
        audioPlayer = new AudioPlayer(mediaPlayerFactory,mediaPlayer, musicManager);
    }
}
