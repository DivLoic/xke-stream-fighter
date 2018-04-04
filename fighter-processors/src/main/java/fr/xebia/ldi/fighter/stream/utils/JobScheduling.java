package fr.xebia.ldi.fighter.stream.utils;

import java.util.concurrent.TimeUnit;

/**
 * Created by loicmdivad.
 */
public class JobScheduling {

    public static void delayProcessing(long timeout){
        try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
