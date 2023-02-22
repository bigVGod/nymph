package com.navigatorTB_Nymph.Timer;

import java.util.TimerTask;

public class GuessTimer extends TimerTask {
    public static int count = 5;

    @Override
    public void run() {
        if (count < 10) {
            count += 2;
        }
        if (count > 10) {
            count = 10;
        }
    }
}
