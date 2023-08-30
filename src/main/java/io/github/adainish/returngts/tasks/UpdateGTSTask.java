package io.github.adainish.returngts.tasks;

import io.github.adainish.returngts.ReturnGTS;

public class UpdateGTSTask implements Runnable{
    @Override
    public void run() {
        if (ReturnGTS.gts != null)
        {
            ReturnGTS.gts.wipeExpiredItems();
        }
    }
}
