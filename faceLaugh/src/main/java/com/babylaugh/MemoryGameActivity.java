package com.babylaugh;

import com.facelaugh.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Random;

import static java.lang.Math.sqrt;

public class MemoryGameActivity extends Activity implements NumberPicker.OnValueChangeListener {

    private MediaPlayer mp_click;
    private MediaPlayer mp_fan;
    String[] AllowedValues;
    public static int SamePictures,oldposition,OpenPictures,NbrOfPictures,OpenPicureID,NbrOfPictures_tmp,DefaultValue, DefaultValue_tmp;
    ImageView imageView_old;
    CountDownTimer MyCountDownTimer;
    ImageView imageView;
    Boolean TimerRunning = false;
    int picturewidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        AllowedValues = new String[9];
        AllowedValues[0] = "4";
        AllowedValues[1] = "6";
        AllowedValues[2] = "8";
        AllowedValues[3] = "12";
        AllowedValues[4] = "16";
        AllowedValues[5] = "20";
        AllowedValues[6] = "24";
        AllowedValues[7] = "30";
        AllowedValues[8] = "36";

        Random random_pic = new Random();

        for(int l=0; l<AllowedValues.length; l++) {
            //Log.v("Pete", "Integer.valueOf(AllowedValues[l]): " + Integer.valueOf(AllowedValues[l]));

            if (Integer.valueOf(AllowedValues[l]) == BabyMain.NbrOfPictures){
                //Log.v("Pete", "BabyMain.NbrOfPictures: " + BabyMain.NbrOfPictures);
                DefaultValue = l;
                break;
             }
        }

        mp_click = MediaPlayer.create(this, R.raw.click);
        mp_fan = MediaPlayer.create(this, R.raw.fanfare);
        OpenPicureID = 999;
        SamePictures = 0;
        oldposition = -1;
        OpenPictures = 0;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_memory_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        int nbr_of_pictures = Pictures.BACKGROUND_IDS.length;
        int random_range = nbr_of_pictures - 1;
        int pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;

        LinearLayout rLayout = (LinearLayout) findViewById (R.id.memory_activity);
        Resources res = getResources(); //resource handle
        Drawable drawable = res.getDrawable(Pictures.BACKGROUND_IDS[pic_id]); //new Image that was added to the res folder
        rLayout.setBackground(drawable);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        //NbrOfPictures = 12;
        NbrOfPictures = BabyMain.NbrOfPictures;
        int sizeofcubeside = (int)sqrt(NbrOfPictures);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int windowWidth = size.x;
        int windowHeight = size.y;
        //How this should be calculated (weigth 7, weight 1)
        int HeightOfGridArea = windowHeight*6/7;

        Log.v("Pete", "windowWidth: " + windowWidth);
        Log.v("Pete", "windowHeight: " + windowHeight);
        Log.v("Pete", "HeightOfGridArea: " + HeightOfGridArea);

        gridview.setNumColumns(sizeofcubeside);

        int NbrOfVerticalPictures = NbrOfPictures/sizeofcubeside;
        int TotalPadding = 16*NbrOfVerticalPictures;
        Log.v("Pete", "NbrOfVerticalPictures: " + NbrOfVerticalPictures);
        Log.v("Pete", "TotalPadding: " + TotalPadding);

        picturewidth = (windowWidth / sizeofcubeside);

        Log.v("Pete", "picturewidth: " + picturewidth);

        if((NbrOfVerticalPictures*picturewidth+TotalPadding)>HeightOfGridArea) {
            Log.v("Pete", "Pictures does't fit to Y: " + windowHeight);

            picturewidth = HeightOfGridArea/NbrOfVerticalPictures - TotalPadding;
            Log.v("Pete", "new picturewidth: " + picturewidth);
        }else{
            picturewidth = picturewidth - 50;
        }

        gridview.setAdapter(new ImageAdapter(this, NbrOfPictures, picturewidth));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(MemoryGameActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                //Toast.makeText(MemoryGameActivity.this, "" + v.getId(), Toast.LENGTH_SHORT).show();
                //ImageView imageView;

                if (TimerRunning)
                    return;

                imageView = (ImageView) v;
                imageView.clearColorFilter();

                if(v.getId()==OpenPicureID)
                    return;

                if(SamePictures==0){
                    PlayClick();
                    //First new picture checked
                    oldposition = position;
                    SamePictures=v.getId();
                    imageView_old = (ImageView) v;

                    OpenPictures++;
                }else{
                    //Cliecked different picture than first one...
                    if(position!=oldposition) {

                        //Pictures are same
                        if (SamePictures == v.getId()) {
                            PlayFan();
                            OpenPictures++;
                            imageView.setId(OpenPicureID);
                            imageView_old.setId(OpenPicureID);
                        } else {
                            PlayClick();
                            SetCountDownTimer(1500);

                            OpenPictures--;
                        }

                        //Now 2 pictures checked - those are closed or left open..
                        SamePictures = 0;
                    }

                }

                if(OpenPictures==NbrOfPictures)
                    Toast.makeText(MemoryGameActivity.this, "All pictures open!!!!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    public void Restart(View arg0){
        Toast.makeText(MemoryGameActivity.this, "Restart!!!!", Toast.LENGTH_SHORT).show();

        Start();
    }

    public void Start(){

        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            overridePendingTransition(0, 0);

            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        NbrOfPictures_tmp = Integer.valueOf(AllowedValues[newVal-1]);
        Log.v("Pete", "onValueChange - newVal: " + newVal);
        Log.v("Pete", "onValueChange - NbrOfPictures_tmp: " + NbrOfPictures_tmp);
        DefaultValue_tmp = newVal;
        PlayClick();
        //NbrOfPictures_tmp = newVal;
    }

    public void show(View arg0)
    {
        PlayClick();
        final Dialog d;
        d = new Dialog(MemoryGameActivity.this);
        d.setTitle("Set Number of Pictures");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.MynumberPicker);
        np.setMaxValue(AllowedValues.length); // max value 100
        np.setMinValue(1);   // min value 0
        np.setDisplayedValues(AllowedValues);

        //Should be set from Array...
        np.setValue(DefaultValue+1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                PlayClick();
                BabyMain.NbrOfPictures = NbrOfPictures_tmp;
                DefaultValue = DefaultValue_tmp - 1;
                d.dismiss();
                Start();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                PlayClick();
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();

    }

    public void PlayClick() {

        if (mp_fan.isPlaying()){
            mp_fan.seekTo(0);
            mp_fan.pause();
        }

        if (mp_click.isPlaying()){
            mp_click.seekTo(0);
        }

        mp_click.start();
    }

    public void PlayFan() {

        if (mp_click.isPlaying()){
            mp_click.seekTo(0);
            mp_click.pause();
        }

        if (mp_fan.isPlaying()){
            mp_fan.seekTo(0);
        }
        mp_fan.start();
    }

    public void SetCountDownTimer(long startfromthis_ms) {

        TimerRunning = true;

        MyCountDownTimer = new CountDownTimer(startfromthis_ms, 1000) {

            public void onTick(long millisUntilFinished) {

                //myRemainingTime = millisUntilFinished;
                //FreeSleeperRunningtime++;
            }

            public void onFinish() {

                int color = Color.parseColor("#FFFFFF");
                imageView.setColorFilter(color);

                if (imageView_old != null)
                    imageView_old.setColorFilter(color);

                TimerRunning = false;
            }
        }.start();
    }

}
