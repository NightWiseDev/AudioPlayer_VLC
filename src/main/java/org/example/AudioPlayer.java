package org.example;

import javafx.application.Application;
import javafx.scene.control.ListView;
import lombok.RequiredArgsConstructor;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import java.io.File;
import java.util.List;

public class AudioPlayer {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final MediaPlayer mediaPlayer;
    private final MusicManager musicManager;

    private final List<File> musicFiles;

    public AudioPlayer(MediaPlayerFactory factory, MediaPlayer player, MusicManager manager) {
        this.mediaPlayerFactory = factory;
        this.mediaPlayer = player;
        this.musicManager = manager;
        this.musicFiles = manager.getMusicFiles();
    }

    public void playMusic(int index) {
        if (mediaPlayer != null && !musicFiles.isEmpty()) {
            String mediaPath = musicFiles.get(index).getAbsolutePath();
            System.out.println("Playing media: " + mediaPath);

            mediaPlayer.controls().stop();

            mediaPlayer.media().play(mediaPath);
        }
    }

    public void stop(Application application) throws Exception {
        // Освобождаем ресурсы VLCJ при закрытии
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
        }
        if (mediaPlayerFactory != null) {
            mediaPlayerFactory.release();
        }
        application.stop();
    }
}
