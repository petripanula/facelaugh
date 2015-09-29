package com.babylaugh;

import com.facelaugh.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;

import android.app.ActionBar.LayoutParams;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

import static java.lang.Math.sqrt;

import com.google.android.gms.games.achievement.Achievements;
import com.google.android.gms.games.achievement.Achievements.UpdateAchievementResult;
import com.google.android.gms.common.api.ResultCallback;

public class MemoryGameActivity extends BaseGameActivity implements NumberPicker.OnValueChangeListener {

    private EasyTracker easyTracker = null;

    private MediaPlayer mp_click;
    private MediaPlayer mp_fan;
    String[] AllowedValues;
    public static int SamePictures,oldposition,OpenPictures,NbrOfPictures,OpenPicureID,NbrOfPictures_tmp,DefaultValue, DefaultValue_tmp;
    public static int NbrOfPictures_oncreate,DefaultValue_oncreate;
    public static final int FreeVersionLimit = 4;
    public static int NextLevelArray;
    ImageView imageView_old;
    CountDownTimer MyCountDownTimer;
    CountDownTimer MyCountDownTimer2;
    ImageView imageView;
    Boolean TimerRunning = false;
    Boolean TimerRunning2 = false;
    int picturewidth;
    public static boolean MyisSignedIn = false;
    String PlayerName="NoName";
    int child_mode;
    String[] AchievementString = new String[10];
    public static boolean[] AchievementOnServer = new boolean[10];
    public static boolean[] Achievement = new boolean[10];

    public static int[] NewArray;
    public static int[][] SeenPictures;
    SecurePreferences preferences;
    public static Boolean ShouldKnowNow=false;
    public static Boolean GameOver=false;
    public static int KnownID = -1;
    public static int KnownPos = -1;


    public static int AchievementAck = -1;
    public static boolean AchievementSent = false;
    public static int MemoryLevel = -1;
    // request codes we use when invoking an external activity
    final int RC_UNUSED = 5001;

    public static final boolean ENABLE_MEM_LOGS = false;

    PopupWindow popupWindow;
    public String[] MyStringBuffer;

    String PopUpmessage="NA";

    //BabyMain BabyMainActivity = new BabyMain();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        AllowedValues = new String[10];
        AllowedValues[0] = "4";
        AllowedValues[1] = "6";
        AllowedValues[2] = "8";
        AllowedValues[3] = "12";
        AllowedValues[4] = "16";
        AllowedValues[5] = "20";
        AllowedValues[6] = "24";
        AllowedValues[7] = "30";
        AllowedValues[8] = "36";
        AllowedValues[9] = "42";

        DefaultValue = 0;
        DefaultValue_tmp = 0;

        loadData();
        SetStringsArrays();
        loadLocal();
        CheckHighestLevel();

        easyTracker = EasyTracker.getInstance(MemoryGameActivity.this);
        easyTracker.send(MapBuilder.createEvent("MemoryGameActivity", "onCreate", "1", null).build());

        int TableIndex = Pictures.MEMORY_IDS_TEST.length*2+1;
        /// Array to test if picture is seen or not...
        SeenPictures = new int[TableIndex][TableIndex];

        //And Init for it...
        for(int i=0; i<TableIndex; i++) {
            for(int m=0; m<TableIndex; m++) {
                SeenPictures[i][m]=0;
            }
        }

        Random random_pic = new Random();

        for(int l=0; l<AllowedValues.length; l++) {
            //if (ENABLE_MEM_LOGS) Log.d("Pete", "Integer.valueOf(AllowedValues[l]): " + Integer.valueOf(AllowedValues[l]));

            if (Integer.valueOf(AllowedValues[l]) == NbrOfPictures){
                //if (ENABLE_MEM_LOGS) Log.d("Pete", "BabyMain.NbrOfPictures: " + BabyMain.NbrOfPictures);
                DefaultValue = l;
                DefaultValue_tmp = DefaultValue + 1;
                MemoryLevel = l;
                break;
             }
        }

        NbrOfPictures_tmp = NbrOfPictures;

        NbrOfPictures_oncreate  = NbrOfPictures;
        DefaultValue_oncreate = DefaultValue;


        mp_click = MediaPlayer.create(this, R.raw.click);
        mp_fan = MediaPlayer.create(this, R.raw.fanfare);

        OpenPicureID = 999;
        SamePictures = 0;
        oldposition = -1;
        OpenPictures = 0;
        GameOver = false;
        ShouldKnowNow=false;

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_memory_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int nbr_of_pictures = Pictures.MEMORY_BACKGROUND_IDS.length;
        int random_range = nbr_of_pictures - 1;
        int pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int windowWidth = size.x;
        int windowHeight = size.y;


        imageView = new ImageView(this);
        BitmapDrawable ob = new BitmapDrawable(getResources(), decodeSampledBitmapFromResource(imageView.getResources(), Pictures.MEMORY_BACKGROUND_IDS[pic_id], windowWidth, windowHeight));

        LinearLayout rLayout = (LinearLayout) findViewById (R.id.memory_activity);
        rLayout.setBackground(ob);


        GridView gridview = (GridView) findViewById(R.id.gridview);

        int sizeofcubeside = (int)sqrt(NbrOfPictures);

        /*
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int windowWidth = size.x;
        int windowHeight = size.y;
        */
        //How this should be calculated (weigth 7, weight 1)
        int HeightOfGridArea = windowHeight*6/7;

        if (ENABLE_MEM_LOGS) Log.d("Pete", "windowWidth: " + windowWidth);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "windowHeight: " + windowHeight);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "HeightOfGridArea: " + HeightOfGridArea);

        gridview.setNumColumns(sizeofcubeside);

        int NbrOfVerticalPictures = NbrOfPictures/sizeofcubeside;
        int TotalPadding = 16*NbrOfVerticalPictures;
        if (ENABLE_MEM_LOGS) Log.d("Pete", "NbrOfVerticalPictures: " + NbrOfVerticalPictures);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "TotalPadding: " + TotalPadding);

        picturewidth = (windowWidth / sizeofcubeside);

        if (ENABLE_MEM_LOGS) Log.d("Pete", "picturewidth: " + picturewidth);

        if((NbrOfVerticalPictures*picturewidth+TotalPadding)>HeightOfGridArea) {
            if (ENABLE_MEM_LOGS) Log.d("Pete", "Pictures does't fit to Y: " + windowHeight);

            picturewidth = HeightOfGridArea/NbrOfVerticalPictures - TotalPadding;
            if (ENABLE_MEM_LOGS) Log.d("Pete", "new picturewidth: " + picturewidth);
        }else{
            picturewidth = picturewidth - 50;
        }

        ShuffleArray(Pictures.MEMORY_IDS_TEST);

        NewArray = new int[NbrOfPictures];

        int j=0,k=0;
        for(int l=0; l<NbrOfPictures; l++) {
            NewArray[l] = Pictures.MEMORY_IDS_TEST[k];
            j++;
            if(j==2){
                j=0;
                k++;
            }
        }

        ShuffleArray(NewArray);

        if (ENABLE_MEM_LOGS) Log.d("Pete", "NewArray: " + Arrays.toString(NewArray));

        gridview.setAdapter(new ImageAdapter(this, NbrOfPictures, picturewidth));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if (ENABLE_MEM_LOGS) Log.d("Pete", "onItemClick - v.getId(): " + v.getId());

                if (TimerRunning)
                    return;

                if(GameOver)
                    return;

                if(child_mode==0) {
                    if(MyCountDownTimer2!=null)
                        MyCountDownTimer2.cancel();

                    SetCountDownTimer2(7000);
                }

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

                    SeenPictures[v.getId()][position]++;

                    if(child_mode==0)
                        CheckIfYouShouldKnowTheNext(v.getId(), position);
                } else {
                    //Cliecked different picture than first one...
                    if (position != oldposition) {

                        SeenPictures[v.getId()][position]++;

                        //Pictures are same
                        if (SamePictures == v.getId()) {
                            PlayFan();
                            OpenPictures++;
                            imageView.setId(OpenPicureID);
                            imageView_old.setId(OpenPicureID);

                            if(ShouldKnowNow)
                                ShouldKnowNow=false;
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

                    if(MyCountDownTimer2!=null)
                        MyCountDownTimer2.cancel();

                    easyTracker.send(MapBuilder.createEvent("MemoryGameActivity", "All pictures open", Integer.toString(MemoryLevel), null).build());
                    //Toast.makeText(MemoryGameActivity.this, "All pictures open!!!!", Toast.LENGTH_SHORT).show();
                    SetAchievement();
                    PlayNextLevel();
                }

            }

        });

        UpdateUi();
    }

    public void Restart(View arg0){
        //Toast.makeText(MemoryGameActivity.this, "Restart!!!!", Toast.LENGTH_SHORT).show();

        MyStringBuffer = new String[3];

        for(int i=0; i < MyStringBuffer.length; i++)
        {
            MyStringBuffer[i] = "Huaaa";
        }

        //ShowPopUp();
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

    public int RetunHigestLevelPossible(){

        for (int i = AchievementString.length - 1; i >= 0; i--) {

            if (Achievement[i] || AchievementOnServer[i]) {

                if (ENABLE_MEM_LOGS) Log.d("Pete", "RetunHigestLevelPossible - i: " + i);
                return i;

            }
        }

        return 0;
    }

    public void CheckHighestLevel(){

        if (ENABLE_MEM_LOGS) Log.d("Pete", "CheckHighestLevel - NbrOfPictures: " + NbrOfPictures);

        if(child_mode==1)
            return;

        int Index;
        int NbrOfPictures_current;

        NbrOfPictures_current = NbrOfPictures;

        NbrOfPictures = 4;

        for (int i = AchievementString.length - 1; i >= 0; i--) {

            if (Achievement[i] || AchievementOnServer[i]) {
                if (ENABLE_MEM_LOGS) Log.d("Pete", "CheckHighestLevel - this is first level with achievement: " + i);
                if (ENABLE_MEM_LOGS) Log.d("Pete", "CheckHighestLevel - DefaultValue: " + DefaultValue);
                Index = i + 1;

                if(Index>=AchievementString.length)
                    Index = i;

                NbrOfPictures = Integer.valueOf(AllowedValues[Index]);
                if (ENABLE_MEM_LOGS) Log.d("Pete", "CheckHighestLevel - NbrOfPictures: " + NbrOfPictures);

                break;
            }
        }


        if(NbrOfPictures_current<NbrOfPictures)
            NbrOfPictures = NbrOfPictures_current;

        saveData();
    }

    public void Mode(View arg0){
        ImageButton ib = (ImageButton) findViewById(R.id.mode);
        Boolean RestartNeeded;
        int Index;

        if(child_mode==0) {
            child_mode=1;

            if(MyCountDownTimer2!=null)
                MyCountDownTimer2.cancel();

            ib.setImageResource(Pictures.IMAGEBUTTON_IDS[0]);
            Toast.makeText(MemoryGameActivity.this, "Child Mode Activated!", Toast.LENGTH_SHORT).show();
        }
        else{
            child_mode=0;
            ib.setImageResource(Pictures.IMAGEBUTTON_IDS[1]);
            Toast.makeText(MemoryGameActivity.this, "Child Mode DeActivated!", Toast.LENGTH_SHORT).show();
        }

        if(child_mode==0) {
            RestartNeeded=true;
            for (int i = AchievementString.length - 1; i >= 0; i--) {

                if (Achievement[i] || AchievementOnServer[i]) {
                    RestartNeeded=false;
                    if (ENABLE_MEM_LOGS) Log.d("Pete", "Mode - this is first level with achievement: " + i);
                    if (ENABLE_MEM_LOGS) Log.d("Pete", "Mode - DefaultValue: " + DefaultValue);
                    Index = i + 1;

                    if(Index>=AchievementString.length) {
                        Index = i;
                    }

                    NbrOfPictures = Integer.valueOf(AllowedValues[Index]);
                    if (ENABLE_MEM_LOGS) Log.d("Pete", "Mode - NbrOfPictures: " + NbrOfPictures);

                    if(DefaultValue>Index)
                        RestartNeeded=true;

                    break;
                }

            }

            if(OpenPictures>0)
                RestartNeeded=true;

            if(RestartNeeded) {
                saveData();
                Start();
            }
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
        if (ENABLE_MEM_LOGS) Log.d("Pete", "onValueChange - newVal: " + newVal);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "onValueChange - oldVal: " + oldVal);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "onValueChange - NbrOfPictures_tmp: " + NbrOfPictures_tmp);
        DefaultValue_tmp = newVal;
        PlayClick();
        //NbrOfPictures_tmp = newVal;
    }

    public void called_show(){
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

                //DefaultValue = DefaultValue_tmp - 1;
                DefaultValue = DefaultValue_tmp;
                NbrOfPictures = NbrOfPictures_tmp;

                if (ENABLE_MEM_LOGS) Log.d("Pete", "onClick - DefaultValue: " + DefaultValue);
                if (ENABLE_MEM_LOGS) Log.d("Pete", "onClick - NbrOfPictures: " + NbrOfPictures);

                if(DefaultValue<0)
                    DefaultValue=0;

                int Index;
                if(DefaultValue<=1)
                    Index = 0;
                else
                    Index = DefaultValue - 2;

                if (ENABLE_MEM_LOGS) Log.d("Pete", "onClick - Index: " + Index);

                if(!BabyMain.mSubscribedToInfiniteLaugh && child_mode==0 && DefaultValue>FreeVersionLimit) {
                    //showAlert(getString(R.string.level_not_available_in_free));
                    d.dismiss();

                    int addthis;
                    addthis = 0;

                    if(!Achievement[0])
                        addthis = 0;
                    else
                        addthis = 1;


                    NbrOfPictures_tmp = Integer.valueOf(AllowedValues[RetunHigestLevelPossible()+addthis]);
                    DefaultValue = RetunHigestLevelPossible() + addthis;
                    DefaultValue_tmp = RetunHigestLevelPossible() + addthis;

                    ShowPopUp_Buy();
                }else {

                    if (Achievement[Index] || AchievementOnServer[Index] || Index == 0 || child_mode == 1) {

                        d.dismiss();

                        if(Index == 0 && NbrOfPictures==6 && !Achievement[Index]){

                            NbrOfPictures_tmp = Integer.valueOf(AllowedValues[RetunHigestLevelPossible()]);
                            DefaultValue = RetunHigestLevelPossible();
                            DefaultValue_tmp = RetunHigestLevelPossible();

                            PopUpmessage = getString(R.string.level_not_available);
                            ShowPopUp_OK(true);
                        }else {
                            Start();
                        }

                    } else {
                        PopUpmessage = getString(R.string.level_not_available);
                        d.dismiss();

                        int addthis;

                        if(!Achievement[0])
                            addthis = 0;
                        else
                            addthis = 1;

                        NbrOfPictures_tmp = Integer.valueOf(AllowedValues[RetunHigestLevelPossible()+addthis]);
                        DefaultValue = RetunHigestLevelPossible() + addthis;
                        DefaultValue_tmp = RetunHigestLevelPossible() + addthis;

                        if (ENABLE_MEM_LOGS) Log.d("Pete", "Here onClick - DefaultValue: " + DefaultValue);
                        if (ENABLE_MEM_LOGS) Log.d("Pete", "Here onClick - NbrOfPictures_tmp: " + NbrOfPictures_tmp);

                        ShowPopUp_OK(true);
                        //showAlert(getString(R.string.level_not_available));
                    }
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                PlayClick();
                d.dismiss(); // dismiss the dialog

                if(NbrOfPictures_oncreate  != NbrOfPictures || DefaultValue_oncreate != DefaultValue) {
                    NbrOfPictures = NbrOfPictures_oncreate;
                    DefaultValue = DefaultValue_oncreate;
                    saveData();
                    Start();
                }
            }
        });
        d.show();

    }


    public void show(View arg0)
    {
        called_show();
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

                if(ShouldKnowNow){
                    GameOver = true;


                    //Get all childs from Gridview...
                    GridView gv = (GridView) findViewById(R.id.gridview);

                    int childcount = gv.getChildCount();
                    for (int i=0; i < childcount; i++){
                        View v = gv.getChildAt(i);
                        //if (ENABLE_MEM_LOGS) Log.d("Pete", "v.getId(): " + v.getId());

                        if(v.getId()==KnownID){
                            imageView = (ImageView) v;
                            imageView.clearColorFilter();

                            imageView.setImageAlpha(100);
                        }
                    }

                    if(MyCountDownTimer2!=null)
                        MyCountDownTimer2.cancel();

                    PopUpmessage = getString(R.string.game_over);
                    ShowPopUp_OK(false);
                    //showAlert(getString(R.string.game_over));
                }else {
                    int color = Color.parseColor("#FFFFFF");
                    imageView.setColorFilter(color);

                    if (imageView_old != null)
                        imageView_old.setColorFilter(color);
                }

                TimerRunning = false;
            }
        }.start();
    }

    public void SetCountDownTimer2(long startfromthis_ms) {

        TimerRunning2 = true;

        MyCountDownTimer2 = new CountDownTimer(startfromthis_ms, 1000) {

            public void onTick(long millisUntilFinished) {

                //myRemainingTime = millisUntilFinished;
                //FreeSleeperRunningtime++;
            }

            public void onFinish() {
                PopUpmessage = getString(R.string.game_over_timeout);
                ShowPopUp_OK(false);
                //showAlert(getString(R.string.game_over_timeout));
                TimerRunning2 = false;

                GameOver = true;
            }
        }.start();
    }

    void loadData() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        NbrOfPictures = sp.getInt("NbrOfPictures", 4);
        child_mode = sp.getInt("child_mode", 0);

        if (NbrOfPictures == 0) NbrOfPictures = 4;
        if (ENABLE_MEM_LOGS) Log.d("Pete", "Loaded data: NbrOfPictures = " + String.valueOf(NbrOfPictures));

    }

    void saveData() {
       /*
        * WARNING: on a real application, we recommend you save data in a secure way to
        * prevent tampering. For simplicity in this sample, we simply store the data using a
        * SharedPreferences.
        */
        SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
        spe.putInt("NbrOfPictures", NbrOfPictures);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "saveData: NbrOfPictures = " + String.valueOf(NbrOfPictures));
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
        AchievementString[9] = getString(R.string.achievement_memory_with_42_pictures);
    }

    protected boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    @Override
    public void onSignInFailed() {
        if (ENABLE_MEM_LOGS) Log.d("Pete", "In onSignInFailed...");

        MyisSignedIn = false;

        if (ENABLE_MEM_LOGS) Log.d("Pete", "isTaskRoot(): " + isTaskRoot());

    }

    @Override
    public void onSignInSucceeded() {
        if (ENABLE_MEM_LOGS) Log.d("Pete", "In onSignInSucceeded...");
        MyisSignedIn = true;

        if (ENABLE_MEM_LOGS) Log.d("Pete", "isTaskRoot(): " + isTaskRoot());


        // Set the greeting appropriately on main menu
        Player p = Games.Players.getCurrentPlayer(getApiClient());
        String displayName;
        if (p == null) {
            if (ENABLE_MEM_LOGS) Log.d("Pete", "getCurrentPlayer() is NULL!");
            //displayName = "???";
        } else {
            displayName = p.getDisplayName();
            if (ENABLE_MEM_LOGS) Log.d("Pete", "getCurrentPlayer() is " + displayName);

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
        //if(ENABLE_LOGS) if (ENABLE_MEM_LOGS) Log.d("Pete", "AccomplishmentsOutbox saveLocal...");

        preferences = new SecurePreferences(context, "my-preferences", "SometopSecretKey1235", true);

        for(int i=0;i<AchievementString.length;i++) {
            preferences.put("AchievementOnServer" + "_" + i, String.valueOf(AchievementOnServer[i]));
            preferences.put("Achievement" + "_" + i, String.valueOf(Achievement[i]));
        }

    }

    public void loadLocal() {
        if (ENABLE_MEM_LOGS) Log.d("Pete", "AccomplishmentsOutbox loadLocal...");

        //Init
        preferences = new SecurePreferences(this, "my-preferences", "SometopSecretKey1235", true);

        for(int i=0;i<AchievementString.length;i++) {
            AchievementOnServer[i] = Boolean.valueOf(preferences.getBooleanString("AchievementOnServer" + "_" + i));
            if (ENABLE_MEM_LOGS) Log.d("Pete", "loadLocal i: "+ i + " AchievementOnServer[i]: " + AchievementOnServer[i]);

            Achievement[i] = Boolean.valueOf(preferences.getBooleanString("Achievement" + "_" + i));
            if (ENABLE_MEM_LOGS) Log.d("Pete", "loadLocal i: " + i + " Achievement[i]: " + Achievement[i]);

        }

    }

    public void SetAchievement(){
        if (ENABLE_MEM_LOGS) Log.d("Pete", "SetAchievement - MemoryLevel: " + MemoryLevel);

        if(child_mode==1)
            return;

        if(!GetIsAchievemented(MemoryLevel)) {
            Toast.makeText(this, this.getString(R.string.achievement) + ": " + this.getString(R.string.level_achievement_reached), Toast.LENGTH_LONG).show();
            easyTracker.send(MapBuilder.createEvent(getString(R.string.achievement), getString(R.string.level_achievement_reached), Integer.toString(MemoryLevel), null).build());
        }
        if (ENABLE_MEM_LOGS) Log.d("Pete", "Call PicSetAchievement...");

        Achievement[MemoryLevel] = true;

        if (ENABLE_MEM_LOGS) Log.d("Pete", "Call saveLocal...");
        saveLocal(this);

        pushAccomplishments();
        //String GameAndLevel = "PicGame " + GetGameLevel(GameLevel) + " " + GetGameTime(GameTime);
        //easyTracker.send(MapBuilder.createEvent("user action","achievement",GameAndLevel,null).build())
    }

    public boolean GetIsAchievemented(int level){

        if (ENABLE_MEM_LOGS) Log.d("Pete", "GetIsThisPicLevelAchievemented - level: " + level);
        if (ENABLE_MEM_LOGS) Log.d("Pete", "AchievementOnServer[level]: " + AchievementOnServer[level]);

        return Achievement[level] || AchievementOnServer[level];

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

        if (ENABLE_MEM_LOGS) Log.d("Pete", "pushAccomplishments.....");

        if (!isSignedIn()) {
            // can't push to the cloud, so save locally
            //if(ENABLE_LOGS) if (ENABLE_MEM_LOGS) Log.d("Pete", "pushAccomplishments - not isSignedIn");
            saveLocal(this);
            return;
        }

        if (ENABLE_MEM_LOGS) Log.d("Pete", "pushAccomplishments - isSignedIn");


        for(int i=0;i<AchievementString.length;i++) {

            //For Pic level Achievements
            if (Achievement[i] && !AchievementSent && !AchievementOnServer[i]) {
                if (ENABLE_MEM_LOGS) Log.d("Pete", "pushing Achievement: " + i);

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
                if (ENABLE_MEM_LOGS) Log.d("Pete", "Achievement delivered to server!!!!");

                AchievementSent = false;
                AchievementOnServer[AchievementAck] = true;
                AchievementAck = -1;

            }
        }
    }

    public void CheckIfYouShouldKnowTheNext(int Id, int pos){

        for(int i=0; i<Pictures.MEMORY_IDS_TEST.length + 1; i++) {
            if(i!=pos) {
                if (SeenPictures[Id][i] >= 1) {
                    ShouldKnowNow = true;
                    KnownID = Id;
                    KnownPos = i;
                }
            }
        }

        if(ShouldKnowNow)
            Toast.makeText(MemoryGameActivity.this, "You Should Know Now Where the Picture Is!!!", Toast.LENGTH_SHORT).show();

    }

    private void ShuffleArray(int[] array)
    {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            if (index != i)
            {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }

    public void ShowPopUp_Buy(){
        if (ENABLE_MEM_LOGS) Log.d("Pete", "ShowPopUp_Buy....");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //int windowWidth = size.x;
        //int windowHeight = size.y;

        int FontSize = 16;

        ImageButton justfind;

        // POPUP WINDOW STARTS //
        LayoutInflater layoutInflater  = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);

        // final PopupWindow popupWindow;
        popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        //popupWindow.setWidth(windowWidth * 2 / 3);
        //popupWindow.setHeight(windowWidth*2/3);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        //popupWindow.setBackgroundDrawable(new BitmapDrawable());

        //Log.v("Pete", "MyStringBuffer.length: " + MyStringBuffer.length);

        String message = "This level is not available in free version!\n\n Buy infinite laughs?\n";

        LinearLayout ll = (LinearLayout)popupView.findViewById(R.id.popup_ll);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final TextView rowTextView = new TextView(this);
        rowTextView.setText(message);

        rowTextView.setGravity(Gravity.CENTER);
        rowTextView.setTextColor(Color.WHITE);
        rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        rowTextView.setTypeface(rowTextView.getTypeface(), Typeface.BOLD);
        // add the textview to the linearlayout
        ll.addView(rowTextView,params);

        /*
        for(int i = 0; i < MyStringBuffer.length; i++){
            // create a new textview
            final TextView rowTextView_loop = new TextView(this);
            rowTextView_loop.setText(MyStringBuffer[i]);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowTextView_loop.setGravity(Gravity.CENTER);
            rowTextView_loop.setTextColor(Color.WHITE);
            // add the textview to the linearlayout
            ll.addView(rowTextView_loop,params1);
        }
        */

        Button btnDismiss = new Button(this);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.setMargins(5, 5, 5, 10);
        //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        //params.addRule(RelativeLayout.LEFT_OF, R.id.id_to_be_left_of);
        btnDismiss.setLayoutParams(params2);
        btnDismiss.setText("CANCEL");
        btnDismiss.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        btnDismiss.setBackgroundResource(R.drawable.button_info_page);
        btnDismiss.setTextColor(Color.WHITE);

        ll.addView(btnDismiss, params2);

        btnDismiss.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - CANCEL....");
                popupWindow.dismiss();
                called_show();
            }
        });

        Button buy = new Button(this);
        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params3.setMargins(5,5,5,5);
        buy.setLayoutParams(params3);
        buy.setText("BUY");
        buy.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        buy.setBackgroundResource(R.drawable.button_info_page);
        buy.setTextColor(Color.WHITE);

        ll.addView(buy, params3);

        buy.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - buy....");
                BabyMain.SetBuyInWanted();
                popupWindow.dismiss();
                CloseThisActivity();
                //BabyMainActivity.InfiniteLaughButtonClicked(MemoryGameActivity.this);

            }
        });

        //Just find some view where we can refer....
        justfind = (ImageButton)findViewById(R.id.settings);
        popupWindow.showAtLocation(justfind, Gravity.CENTER, 0, 0);
        // POPUP WINDOW ENDS //

    }

    public void ShowPopUp_OK(final boolean callShow){
        if (ENABLE_MEM_LOGS) Log.d("Pete", "ShowPopUp_OK....");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //int windowWidth = size.x;
        //int windowHeight = size.y;

        int FontSize = 16;

        ImageButton justfind;

        // POPUP WINDOW STARTS //
        LayoutInflater layoutInflater  = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup2, null);

        // final PopupWindow popupWindow;
        popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        //popupWindow.setWidth(windowWidth * 2 / 3);
        //popupWindow.setHeight(windowWidth*2/3);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(LayoutParams.WRAP_CONTENT);

        String message = "\n" + PopUpmessage + "\n\n";

        LinearLayout ll = (LinearLayout)popupView.findViewById(R.id.popup_ll);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final TextView rowTextView = new TextView(this);
        rowTextView.setText(message);

        rowTextView.setGravity(Gravity.CENTER);
        rowTextView.setTextColor(Color.WHITE);
        rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        rowTextView.setTypeface(rowTextView.getTypeface(), Typeface.BOLD);
        // add the textview to the linearlayout
        ll.addView(rowTextView,params);

        Button btnDismiss = new Button(this);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.setMargins(5, 5, 5, 20);
        btnDismiss.setLayoutParams(params2);
        btnDismiss.setText("OK");
        btnDismiss.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        btnDismiss.setBackgroundResource(R.drawable.button_info_page);
        btnDismiss.setTextColor(Color.WHITE);

        ll.addView(btnDismiss, params2);

        btnDismiss.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - OK....");
                popupWindow.dismiss();

                if(callShow)
                    called_show();
            }
        });

          //Just find some view where we can refer....
        justfind = (ImageButton)findViewById(R.id.settings);
        popupWindow.showAtLocation(justfind, Gravity.CENTER, 0, 0);
        // POPUP WINDOW ENDS //

    }

    public void PlayNextLevel(){
        if (ENABLE_MEM_LOGS) Log.d("Pete", "PlayNextLevel....");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        //int windowWidth = size.x;
        //int windowHeight = size.y;

        int FontSize = 20;

        ImageButton justfind;

        // POPUP WINDOW STARTS //
        LayoutInflater layoutInflater  = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup3, null);

        // final PopupWindow popupWindow;
        popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        //popupWindow.setWidth(windowWidth * 2 / 3);
        //popupWindow.setHeight(windowWidth*2/3);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(LayoutParams.WRAP_CONTENT);

        String message = "Congratulations! Level Passed!";
        String message2 = "Try next level?";

        //RelativeLayout rl = (RelativeLayout)popupView.findViewById(R.id.popup_rl);
        LinearLayout rl = (LinearLayout)popupView.findViewById(R.id.popup_rl);

        TextView titleView = (TextView) rl.findViewById(R.id.header3);
        TextView textview = (TextView) rl.findViewById(R.id.text3);
        Button btnDismiss = (Button) rl.findViewById(R.id.button_cancel);
        Button btnYes = (Button) rl.findViewById(R.id.button_yes);

        titleView.setText(message);
        titleView.setTextColor(Color.RED);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

        textview.setText(message2);
        textview.setTextColor(Color.RED);
        textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        textview.setTypeface(titleView.getTypeface(), Typeface.BOLD);

        btnDismiss.setText("NO");
        btnDismiss.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        btnDismiss.setBackgroundResource(R.drawable.button_info_page);
        btnDismiss.setTextColor(Color.WHITE);

        btnDismiss.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - NO....");
                popupWindow.dismiss();
            }
        });

        btnYes.setText("YES");
        btnYes.setTextSize(TypedValue.COMPLEX_UNIT_SP, FontSize);
        btnYes.setBackgroundResource(R.drawable.button_info_page);
        btnYes.setTextColor(Color.WHITE);

        btnYes.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - YES....");
                popupWindow.dismiss();
                NextLevelArray = DefaultValue_oncreate + 1;

                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - NextLevelArray: " + NextLevelArray);
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - FreeVersionLimit: " + FreeVersionLimit);
                if (ENABLE_MEM_LOGS) Log.v("Pete", "ShowPopUp onClick - BabyMain.mSubscribedToInfiniteLaugh: " + BabyMain.mSubscribedToInfiniteLaugh);

                if (!BabyMain.mSubscribedToInfiniteLaugh && NextLevelArray > FreeVersionLimit){
                    ShowPopUp_Buy();
                }else {
                    NbrOfPictures = Integer.valueOf(AllowedValues[NextLevelArray]);
                    saveData();
                    Start();
                }

            }
        });

        //Just find some view where we can refer....
        justfind = (ImageButton)findViewById(R.id.settings);
        popupWindow.showAtLocation(justfind, Gravity.CENTER, 0, 0);
        // POPUP WINDOW ENDS //

    }


    public void CloseThisActivity(){
        finish();
    }

    @Override
    public void onStart() {
        if(ENABLE_MEM_LOGS) Log.v("Pete", "BabyMain onStart...");
        super.onStart();
        // The rest of your onStart() code.
        EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        if(ENABLE_MEM_LOGS) Log.v("Pete", "BabyMain onStop...");
        super.onStop();
        // The rest of your onStop() code.
        EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }


    @Override
    public void onPause() {
        super.onPause();
        if (ENABLE_MEM_LOGS) Log.d("Pete", "MemoryGameActivity onPause...");

        saveData();

        if(MyCountDownTimer!=null)
            MyCountDownTimer.cancel();

        if(MyCountDownTimer2!=null)
            MyCountDownTimer2.cancel();

        TimerRunning = false;

        // Release any resources from previous MediaPlayer
        if (mp_click != null) {
            if (ENABLE_MEM_LOGS) Log.d("Pete", "onPause() - mp_click.release()");
            mp_click.release();
            mp_click = null;
        }

        if (mp_fan != null) {
            if (ENABLE_MEM_LOGS) Log.d("Pete", "onPause() - mp_fan.release()");
            mp_fan.release();
            mp_fan = null;
        }

        saveData();
        saveLocal(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ENABLE_MEM_LOGS) Log.d("Pete", "MemoryGameActivity onResume...");

        loadData();

        NbrOfPictures_tmp = NbrOfPictures;
        OpenPictures=0;
        SamePictures=0;

        if( mp_fan == null)
            mp_fan = MediaPlayer.create(this, R.raw.fanfare);

        if( mp_click == null)
            mp_click = MediaPlayer.create(this, R.raw.click);


        if(!GameOver) {
            //Get all childs from Gridview...
            GridView gv = (GridView) findViewById(R.id.gridview);

            int childcount = gv.getChildCount();
            for (int i = 0; i < childcount; i++) {
                View v = gv.getChildAt(i);
                //if (ENABLE_MEM_LOGS) Log.d("Pete", "v.getId(): " + v.getId());

                if (v.getId() != OpenPicureID) {
                    imageView = (ImageView) v;
                    int color = Color.parseColor("#FFFFFF");
                    imageView.setColorFilter(color);
                } else {
                    OpenPictures++;
                }

            }
        }

        UpdateUi();

    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (ENABLE_MEM_LOGS) Log.d("Pete", "MemoryGameActivity onDestroy()...");

        saveData();

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
}
