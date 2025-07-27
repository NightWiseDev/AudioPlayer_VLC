package org.example.threads;

import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.MusicManager;
import org.example.platforms.SoundCloudDownloader;

import java.nio.file.Paths;

@RequiredArgsConstructor
public class DownloaderThread extends Thread {

    private final SoundCloudDownloader soundCloudDownloader;
    private final MusicManager musicManager;
    private final String url;

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        System.out.println("Запущен поток за " + start);
        String userHome = System.getProperty("user.home");
        String outputDir = Paths.get(userHome, ".nxAudioPlayer", "music").toString();
        try {
            soundCloudDownloader.download(url,outputDir);
            System.out.println("-> Загружена песня по url: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            musicManager.loadSounds();
            System.out.println("-> Обновлены файлы");
        });
        long end = System.currentTimeMillis() - start;
        System.out.println("Установили песню за: " + end);
    }
}
