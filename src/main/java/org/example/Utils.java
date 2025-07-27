package org.example;

import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class Utils {

    public static String getRemainingTime(MediaPlayer mediaPlayer) {
        long total = mediaPlayer.status().length();
        long current = mediaPlayer.status().time();
        long remaining = Math.max(total - current,0);

        long minutes = (remaining / 1000) / 60;
        long seconds = (remaining / 1000) % 60;

        return String.format("%d:%02d",minutes,seconds);
    }
}
