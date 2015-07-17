package babu.com.lockscreentest;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.annotation.Target;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class LockScreenAppActivity extends FragmentActivity implements UnlockSliderFragment.OnFragmentInteractionListener, FullScreenAd.OnFragmentInteractionListener{

    
    int windowwidth;
    int windowheight;
    final int version = Build.VERSION.SDK_INT;
    final int seekBarMax = 100;
    DisplayMetrics displaymetrics;
    SeekBar unlockSlider;
    TextView tvTime, tvDate, tvCredits;
    DateFormat dateFormat;
    ImageView iv_ad;
    int[] ad_ids = {R.drawable.ad1, R.drawable.ad2, R.drawable.ad3, R.drawable.ad4,R.drawable.ad5,
            R.drawable.ad7, R.drawable.ad8, R.drawable.ad9};


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        setContentView(R.layout.main);

        //Randomly assign 1 of many ad posters
        assignAdvertisement();

        //Set Unlock Slider's Maximum Value; to be compared later
        unlockSlider = (SeekBar) findViewById(R.id.unlock_slider);
        unlockSlider.setMax(seekBarMax);

        //Set Time/Date
        setTimeDate();

        //Set Credits
        tvCredits = (TextView) findViewById(R.id.tv_credits);
        animateTextView(LockscreenReceiver.prevCredits, LockscreenReceiver.credits, tvCredits);

//        tvCredits.setText(String.valueOf(LockscreenReceiver.credits));

        if(getIntent()!=null &&
                getIntent().hasExtra("kill") &&
                    getIntent().getExtras().getInt("kill") == 1){

            finish();
        }

        try{

            startService(new Intent(this, LockScreenService.class));

            //Hides keyboard (keyboard will remain open when screen is turned off)
            hideKeyboard();

            StateListener phoneStateListener = new StateListener();
            TelephonyManager telephonyManager =(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CALL_STATE);

            displaymetrics = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            windowwidth = displaymetrics.widthPixels;
            windowheight = displaymetrics.heightPixels;


            unlockSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == seekBarMax) {

                        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                        final KeyguardManager.KeyguardLock kl = km .newKeyguardLock("MyKeyguardLock");
                        kl.disableKeyguard();

                        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
                        wakeLock.acquire();

                        finish();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    seekBar.setProgress(0);
                }
            });



        }catch (Exception e) {
            // TODO: handle exception
        }



    }

    public void animateTextView(int initialValue, int finalValue, final TextView textview) {
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(5.8f);
        int start = Math.min(initialValue, finalValue);
        int end = Math.max(initialValue, finalValue);
        int difference = Math.abs(finalValue - initialValue);
        Handler handler = new Handler();
        for (int count = start; count <= end; count++) {
            int time = Math.round(decelerateInterpolator.getInterpolation((((float) count) / difference)) * 100) * count;
            final int finalCount = ((initialValue > finalValue) ? initialValue - count : count);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    textview.setText(finalCount + "");
                }
            }, time);
        }
    }


    class StateListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch(state){

                case TelephonyManager.CALL_STATE_RINGING:
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:

                    finish();
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    private void setTimeDate(){
        //Set Time to Top TextView
        tvTime = (TextView) findViewById(R.id.tv_time);
        dateFormat = new SimpleDateFormat("hh:mm a");
        tvTime.setText(dateFormat.format(new Date()).toString());

        //Set Date to Top TextView
        tvDate = (TextView) findViewById(R.id.tv_date);
        dateFormat = new SimpleDateFormat("EEE, MMM d");
        tvDate.setText(dateFormat.format(new Date()).toString());
    }

    private void hideKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void assignAdvertisement(){
        Random r = new Random();
        int randomNum = r.nextInt(ad_ids.length);

        while (randomNum == LockscreenReceiver.lastSeenAd) {
           randomNum = r.nextInt(ad_ids.length);
        }

        iv_ad = (ImageView) findViewById(R.id.iv_advert);
        iv_ad.setImageResource(ad_ids[randomNum]);
    }


    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
        return;
    }

    //only used in lockdown mode
    @Override
    protected void onPause() {
        if (LockscreenReceiver.wasScreenOn){
            finish();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)||(keyCode == KeyEvent.KEYCODE_POWER)||(keyCode == KeyEvent.KEYCODE_VOLUME_UP)||(keyCode == KeyEvent.KEYCODE_CAMERA)) {
            return true;
        }
        if((keyCode == KeyEvent.KEYCODE_HOME)){

            return true;
        }

        return false;

    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_POWER ||(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)||(event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if((event.getKeyCode() == KeyEvent.KEYCODE_HOME)){

            return true;
        }
        return false;
    }

    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}