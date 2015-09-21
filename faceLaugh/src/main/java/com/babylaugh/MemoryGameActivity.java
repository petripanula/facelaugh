package com.babylaugh;

import com.facelaugh.R;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Random;

import static java.lang.Math.sqrt;

import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.achievement.Achievements.UpdateAchievementResult;
import com.google.android.gms.common.api.ResultCallback;

public class MemoryGameActivity extends BaseGameActivity implements NumberPicker.OnValueChangeListener {

    private MediaPlayer mp_click;
    private MediaPlayer mp_fan;
    String[] AllowedValues;
    public static int SamePictures,oldposition,OpenPictures,NbrOfPictures,OpenPicureID,NbrOfPictures_tmp,DefaultValue, DefaultValue_tmp;
    ImageView imageView_old;
    CountDownTimer MyCountDownTimer;
    ImageView imageView;
    Boolean TimerRunning = false;
    int picturewidth;
    public static boolean MyisSignedIn = false;
    String PlayerName="NoName";
    int child_mode;
    String[] AchievementString = new String[9];

    SecurePreferences preferences;

    public static boolean[] AchievementOnServer = new boolean[9];
    public static boolean[] Achievement = new boolean[9];
    public static int AchievementAck = -1;
    public static boolean AchievementSent = false;
    public static int MemoryLevel = -1;
    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        loadData();
        SetStringsArrays();
        loadLocal();

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

            if (Integer.valueOf(AllowedValues[l]) == NbrOfPictures){
                //Log.v("Pete", "BabyMain.NbrOfPictures: " + BabyMain.NbrOfPictures);
                DefaultValue = l;
                MemoryLevel = l;
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

                Log.v("Pete", "onItemClick...");

                if (TimerRunning)
                    return;

                imageView = (ImageView) v;
                imageView.clearColorFilter();

                if (v.getId() == OpenPicureID)
                    return;

                if (SamePictures == 0) {
                    PlayClick();
                    //First new picture checked
                    oldposition = position;
                    SamePictures = v.getId();
                    imageView_old = (ImageView) v;

                    OpenPictures++;
                } else {
                    //Cliecked different picture than first one...
                    if (position != oldposition) {

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

                if (OpenPictures == NbrOfPictures) {
                    Toast.makeText(MemoryGameActivity.this, "All pictures open!!!!", Toast.LENGTH_SHORT).show();
                    SetAchievement();
                }

            }

        });

        UpdateUi();
    }

    public void Restart(View arg0){
        Toast.makeText(MemoryGameActivity.this, "Restart!!!!", Toast.LENGTH_SHORT).show();

        Start();
    }

    public void UpdateUi(){
        ImageButton ib = (ImageButton) findViewById(R.id.mode);

        if(child_mode==0) {
            ib.setImageResource(Pictures.IMAGEBUTTON_IDS[1]);
        }
        else{
            ib.setImageResource(Pictures.IMAGEBUTTON_IDS[0]);
        }

    }

    //TODO
    public void Mode(View arg0){
        ImageButton ib = (ImageButton) findViewById(R.id.mode);

        if(child_mode==0) {
            child_mode=1;
            ib.setImageResource(Pictures.IMAGEBUTTON_IDS[0]);
            Toast.makeText(MemoryGameActivity.this, "Child Mode Activated!", Toast.LENGTH_SHORT).show();
        }
        else{
            child_mode=0;
            ib.setImageResource(Pictures.IMAGEBUTTON_IDS[1]);
            Toast.makeText(MemoryGameActivity.this, "Child Mode DeActivated!", Toast.LENGTH_SHORT).show();
        }
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


    //TODO only allow next level when current has been passed.....
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

                DefaultValue = DefaultValue_tmp - 1;
                NbrOfPictures = NbrOfPictures_tmp;

                int Index;
                if(DefaultValue==0)
                    Index = 0;
                else
                    Index = DefaultValue - 1;

                if(Achievement[Index] || AchievementOnServer[Index] || Index==0 || child_mode==1) {
                    d.dismiss();
                    Start();
                }else{
                    showAlert(getString(R.string.level_not_available));
                }
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

    void loadData() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        NbrOfPictures = sp.getInt("NbrOfPictures", 4);
        child_mode = sp.getInt("child_mode", 0);

        if (NbrOfPictures == 0) NbrOfPictures = 4;
        Log.d("Pete", "Loaded data: NbrOfPictures = " + String.valueOf(NbrOfPictures));

    }

    void saveData() {
       /*
        * WARNING: on a real application, we recommend you save data in a secure way to
        * prevent tampering. For simplicity in this sample, we simply store the data using a
        * SharedPreferences.
        */
        SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
        spe.putInt("NbrOfPictures", NbrOfPictures);
        spe.putInt("child_mode", child_mode);
        //spe.commit();
        spe.apply();

    }

    public void SetStringsArrays(){
        //Game Achievement Arrays
        AchievementString[0] = getString(R.string.achievement_memory_with_4_pictures);
        AchievementString[1] = getString(R.string.achievement_memory_with_6_pictures);
        AchievementString[2] = getString(R.string.achievement_memory_with_8_pictures);
        AchievementString[3] = getString(R.string.achievement_memory_with_12_pictures);
        AchievementString[4] = getString(R.string.achievement_memory_with_16_pictures);
        AchievementString[5] = getString(R.string.achievement_memory_with_20_pictures);
        AchievementString[6] = getString(R.string.achievement_memory_with_24_pictures);
        AchievementString[7] = getString(R.string.achievement_memory_with_30_pictures);
        AchievementString[8] = getString(R.string.achievement_memory_with_36_pictures);
        /*
        AchievementString[9] = getString(R.string.achievement_memory_with_36_pictures);
        AchievementString[10] = getString(R.string.achievement_memory_with_36_pictures);
        AchievementString[11] = getString(R.string.achievement_memory_with_36_pictures);
        */
    }

    protected boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    @Override
    public void onSignInFailed() {
        Log.v("Pete", "In onSignInFailed...");

        MyisSignedIn = false;

        Log.v("Pete", "isTaskRoot(): " + isTaskRoot());

    }

    @Override
    public void onSignInSucceeded() {
        Log.v("Pete", "In onSignInSucceeded...");
        MyisSignedIn = true;

        Log.v("Pete", "isTaskRoot(): " + isTaskRoot());


        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(getApiClient());
        String displayName;
        if (p == null) {
            Log.v("Pete", "getCurrentPlayer() is NULL!");
            //displayName = "???";
        } else {
            displayName = p.getDisplayName();
            Log.v("Pete", "getCurrentPlayer() is " + displayName);

            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            PlayerName = sp.getString("PlayerName", "NoName");


            if(PlayerName.equals("NoName")){

                SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
                spe.putString("PlayerName", displayName);
                spe.apply();
                //spe.commit();
            }
        }

    }

    public void saveLocal(Context context) {
        //if(ENABLE_LOGS) Log.v("Pete", "AccomplishmentsOutbox saveLocal...");

        preferences = new SecurePreferences(context, "my-preferences", "SometopSecretKey1235", true);

        for(int i=0;i<AchievementString.length;i++) {
            preferences.put("AchievementOnServer" + "_" + i, String.valueOf(AchievementOnServer[i]));
            preferences.put("Achievement" + "_" + i, String.valueOf(Achievement[i]));
        }

    }

    public void loadLocal() {
        Log.v("Pete", "AccomplishmentsOutbox loadLocal...");

        //Init
        preferences = new SecurePreferences(this, "my-preferences", "SometopSecretKey1235", true);

        for(int i=0;i<AchievementString.length;i++) {
            AchievementOnServer[i] = Boolean.valueOf(preferences.getBooleanString("AchievementOnServer" + "_" + i));
            Log.v("Pete", "loadLocal i: "+ i + " AchievementOnServer[i]: " + AchievementOnServer[i]);

            Achievement[i] = Boolean.valueOf(preferences.getBooleanString("Achievement" + "_" + i));
            Log.v("Pete", "loadLocal i: " + i + " Achievement[i]: " + Achievement[i]);

        }

    }

    public void SetAchievement(){
        Log.v("Pete", "SetAchievement - MemoryLevel: " + MemoryLevel);

        if(child_mode==1)
            return;

        if(!GetIsAchievemented(MemoryLevel))
            Toast.makeText(this, this.getString(R.string.achievement) + ": " + this.getString(R.string.level_achievement_reached), Toast.LENGTH_LONG).show();

        Log.v("Pete", "Call PicSetAchievement...");

        Achievement[MemoryLevel] = true;

        Log.v("Pete", "Call saveLocal...");
        saveLocal(this);

        pushAccomplishments();
        //String GameAndLevel = "PicGame " + GetGameLevel(GameLevel) + " " + GetGameTime(GameTime);
        //easyTracker.send(MapBuilder.createEvent("user action","achievement",GameAndLevel,null).build())
    }

    public boolean GetIsAchievemented(int level){

        Log.v("Pete", "GetIsThisPicLevelAchievemented - level: " + level);
        Log.v("Pete", "AchievementOnServer[level]: " + AchievementOnServer[level]);

        if(Achievement[level] || AchievementOnServer[level])
            return true;

        return false;
    }

    //@Override
    public void onShowAchievementsRequested(View view) {

        if(child_mode==1){
            showAlert(getString(R.string.achievements_not_available_child));
            return;
        }

        if (isSignedIn()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),RC_UNUSED);
        } else {
            showAlert(getString(R.string.achievements_not_available));
        }
    }


    void pushAccomplishments() {

        Log.v("Pete", "pushAccomplishments.....");

        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            //if(ENABLE_LOGS) Log.v("Pete", "pushAccomplishments - not isSignedIn");
            saveLocal(this);
            return;
        }

        Log.v("Pete", "pushAccomplishments - isSignedIn");


        for(int i=0;i<AchievementString.length;i++) {

            //TODO only push when not delivered to server!!
            //For Pic level Achievements
            if (Achievement[i] && !AchievementSent) {
                Log.v("Pete", "pushing Achievement: " + i);

                //This way we might get ack if this succeeds....
                Games.Achievements.unlockImmediate(mHelper.getApiClient(), AchievementString[i]).setResultCallback(new AchievementResultCallback());

                AchievementOnServer[i] = false;
                AchievementSent = true;
                AchievementAck = i;
            }

        }

        saveLocal(this);

        }



    class AchievementResultCallback implements ResultCallback<Achievements.UpdateAchievementResult> {

        @Override
        public void onResult(UpdateAchievementResult res) {
            if (res.getStatus().getStatusCode() == 0) {
                Log.v("Pete", "Achievement delivered to server!!!!");

                AchievementSent = false;
                AchievementOnServer[AchievementAck] = true;
                AchievementAck = -1;

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("Pete", "MemoryGameActivity onPause...");

        saveData();
        //MyCountDownTimer.onFinish();

        if(MyCountDownTimer!=null)
            MyCountDownTimer.cancel();

        TimerRunning = false;

        // Release any resources from previous MediaPlayer
        if (mp_click != null) {
            Log.d("Pete", "onPause() - mp_click.release()");
            mp_click.release();
            mp_click = null;
        }

        if (mp_fan != null) {
            Log.d("Pete", "onPause() - mp_fan.release()");
            mp_fan.release();
            mp_fan = null;
        }

        saveData();
        saveLocal(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("Pete", "MemoryGameActivity onResume...");

        loadData();

        OpenPictures=0;
        SamePictures=0;

        if( mp_fan == null)
            mp_fan = MediaPlayer.create(this, R.raw.fanfare);

        if( mp_click == null)
            mp_click = MediaPlayer.create(this, R.raw.click);


        //Get all childs from Gridview...
        GridView gv = (GridView) findViewById(R.id.gridview);

        int childcount = gv.getChildCount();
        for (int i=0; i < childcount; i++){
            View v = gv.getChildAt(i);
            //Log.d("Pete", "v.getId(): " + v.getId());

            if(v.getId()!=OpenPicureID){
                imageView = (ImageView) v;
                int color = Color.parseColor("#FFFFFF");
                imageView.setColorFilter(color);
            }else{
                OpenPictures++;
            }

        }

        UpdateUi();

    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("Pete", "MemoryGameActivity onDestroy()...");

        saveData();

    }

}
