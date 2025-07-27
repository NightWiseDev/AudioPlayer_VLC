package org.example;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import org.example.music.MusicItem;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class Timer {

    // Храним Timeline в поле класса, чтобы можно было его останавливать и запускать заново
    private Timeline timeline;

    public void start(int index, MediaPlayer mediaPlayer, ListView<MusicItem> playListView) {
        // Останавливаем предыдущий таймер, если он есть
        if (timeline != null) {
            timeline.stop();
        }

        // Получаем текущий трек
        MusicItem musicItem = playListView.getItems().get(index);

        // Создаём новый таймер с KeyFrame, который будет срабатывать каждую секунду
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (index == -1) return;

                    // Получаем актуальное время из mediaPlayer
                    String time = Utils.getRemainingTime(mediaPlayer);

                    // Обновляем label на UI потоке
                    Platform.runLater(() -> {
                        musicItem.getTimeLabel().setText(time);
                        playListView.refresh();  // Обновляем отображение
                    });
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void stop(ListView<MusicItem> playListView) {
        // Останавливаем таймер
        if (timeline != null) {
            timeline.stop();
        }

        // Сбрасываем время у всех треков
        for (MusicItem item : playListView.getItems()) {
            item.getTimeLabel().setText("0:00");
        }
        playListView.refresh();
    }
}
