package babu.com.lockscreentest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LockscreenReceiver extends BroadcastReceiver  {


    public static boolean wasScreenOn = true;
    public static int prevCredits = 3592;
    public static int credits = 3592;
    public static int lastSeenAd = 0;


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

            Intent lockScreenAppIntent = new Intent(context,LockScreenAppActivity.class);
            lockScreenAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            wasScreenOn = false;
            context.startActivity(lockScreenAppIntent);


        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

            Intent lockScreenAppIntent = new Intent(context,LockScreenAppActivity.class);
            lockScreenAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            wasScreenOn = true;
            prevCredits = credits;
            credits += 73;
            context.startActivity(lockScreenAppIntent);
        }
        else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent lockScreenAppIntent = new Intent(context, LockScreenAppActivity.class);

            lockScreenAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(lockScreenAppIntent);
        }

    }


}
