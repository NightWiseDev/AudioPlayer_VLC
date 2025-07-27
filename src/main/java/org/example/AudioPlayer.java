package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import java.io.File;
import java.util.List;

@Getter
public class AudioPlayer {

    private final MediaPlayerFactory mediaPlayerFactory;
    private final MediaPlayer mediaPlayer;
    private final MusicManager musicManager;

    private final List<File> musicFiles;

    private int currentIndex = -1;

    public AudioPlayer(MediaPlayerFactory factory, MediaPlayer player, MusicManager manager) {
        this.mediaPlayerFactory = factory;
        this.mediaPlayer = player;
        this.musicManager = manager;
        this.musicFiles = manager.getMusicFiles();

        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
           @Override
            public void finished(MediaPlayer mediaPlayer) {
               Platform.runLater(() -> {
                   if(currentIndex >= 0) {
                       playMusic(currentIndex);
                   }
               });
           }
        });
    }

    public void playMusic(int index) {
        if (mediaPlayer != null && !musicFiles.isEmpty() && index >= 0 && index < musicFiles.size()) {
            currentIndex = index;
            String mediaPath = musicFiles.get(index).getAbsolutePath();
            System.out.println("Playing media: " + mediaPath);

            mediaPlayer.controls().stop();

            mediaPlayer.media().play(mediaPath);
        }
    }
}
