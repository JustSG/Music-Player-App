package com.example.odtwarzaczmuzyki;

import android.media.MediaPlayer;

public class Odtwarzacz {
    static MediaPlayer intance;

    public static MediaPlayer getInstance(){
        if(intance == null){
            intance = new MediaPlayer();
        }
        return intance;
    }

    public static int index = -1;
}