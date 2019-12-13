package com.example.moneycount;


import android.content.Context;
import android.media.MediaPlayer;

public class PlayAudio {

    public int getStringIdentifier(Context context, String name) {
        return context.getResources().getIdentifier(name, "raw", context.getPackageName());
    }
    public static void convertText(String numberWord){
        String[] currencies = numberWord.split(" ");
        // Array a = Arrays.toString(currencies);
        // System.out.println(a);

        for (String denom : currencies) {
            System.out.println(denom);
//            MediaPlayer welcome = MediaPlayer.create(MainActivity.PlayAudio.class, R.raw.welcome);
        }

    }
}
