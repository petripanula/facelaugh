package com.babylaugh;


import java.util.Random;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import com.babylaugh.util.IabHelper;
import com.babylaugh.util.IabResult;
import com.babylaugh.util.Inventory;
import com.babylaugh.util.Purchase;
import com.facelaugh.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameActivity;


public class BabyMain extends BaseGameActivity implements NumberPicker.OnValueChangeListener  {
	
	private MediaPlayer mp;
	
	private EasyTracker easyTracker = null;
	
	//static Dialog d;
	
	long myRemainingTime = 0;
			
	//public static final String GAME_SAVINGS = "GAME_SAVINGS";
	
	String[] GameTypeValues = { "Touch Mode", "Music Box Mode"};
	
	String PlayerName="NoName";
	
	 //TODO for release builds set to false
	public static final boolean ENABLE_LOGS = false;

	static final String TAG = "Pete";
	
	public static boolean MyisSignedIn = false;
	public static boolean OnPause = false;
	
	//IInAppBillingService mService;
	//ServiceConnection mServiceConn;
	
	ImageButton imgButton;
	TextView viewlaughs;
	
	CountDownTimer MyCountDownTimer;
	CountDownTimer MyCountDownTimer2;
	 
	public static boolean SleeperOngoing = false;

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteLaugh = false;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
    static final String SKU_GAS = "gas";

    //TODO
    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_LAUGH = "infinite_laughs"; 
    //static final String SKU_INFINITE_LAUGH = "android.test.purchased";  
    
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 2;

    // Graphics for the gas gauge
    //static int[] TANK_RES_IDS = { R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,R.drawable.gas3, R.drawable.gas4 };

    // How many units (1/4 tank is our unit) fill in the tank.
    static final int LAUGHS_MAX = 50;
    
    //Max time in seconds until purchase is requested
    //TODO
    static final int MAX_SLEEPER_TIME = 3600;
    
    int laughs;
    
    int Gametype;
    
    int FreeSleeperRunningtime;
    
    int InfiniteLaughsBought = 0;
    
    int TimeToRunSleeper,TimeToRunSleeper_tmp;
    
    // The helper object
    IabHelper mHelper;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                
        setContentView(R.layout.activity_baby_main);

        easyTracker = EasyTracker.getInstance(BabyMain.this);
        
        // load game data
        loadData();
        
        if(InfiniteLaughsBought==1){
        	if(ENABLE_LOGS) Log.d(TAG, "InfiniteLaughsBought==1");
        	mSubscribedToInfiniteLaugh=true;
        }

        //TODO
        //Just remove this
       // laughs = 0;
       // FreeSleeperRunningtime = 0;
        
        
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0H8ToRVuOEnB6S02/ODLGP85IR+V9M6lH7WysSWbl64gPE32/OxtiNMyeMrppabt9Ywp4R0O620CJVzXowRc/WXzKbC8B5PwzRexqjitGir2dlHYQIWxWKzQXfuh4mCBciLiiAis8e+6Pxt/0hEKqv1J3yKfidc79Wc5z8FSgPKCD62S7MQB5rly3dMJEUJNqRcMrmdiPjuOPnyhMC7zcFHyrve/UV2UFDR2UEs8yObiizIgW+cjcWzi45V2iMu8TGa54goqaeKRF3ZFz5mRIdjoTBllC+B5dMq8wncqopHSYB3bP9GgG2GmtjlbAc67igm/kwQBvvNW4bL6/RluLQIDAQAB";

        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please put your app's public key in MainActivity.java. See README.");
        }
        if (getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        // Create the helper, passing it our context and the public key to verify signatures with
        if(ENABLE_LOGS) Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        if(ENABLE_LOGS) Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if(ENABLE_LOGS) Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                if(ENABLE_LOGS) Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        addButtonListener();
        
		Spinner GameTypeSpinner = (Spinner) findViewById(R.id.game_type_spinner);
		GameTypeSpinner.setAdapter(new MyAdapterGameType(this, R.layout.my_spinner, GameTypeValues));
		GameTypeSpinner.setSelection(Gametype);

		GameTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			 
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v,int position, long id) {
                // On selecting a spinner item
                String item = adapter.getItemAtPosition(position).toString();
 
                if(ENABLE_LOGS) Log.v("Pete", "GameTypeSpinner position: " + position);
                if(ENABLE_LOGS) Log.v("Pete", "GameTypeSpinner onItemSelected: " + item);
        	    
 			   if (mp != null) {
 			      mp.release();
 			      mp = null;
 			   }
 			   
 		       if(MyCountDownTimer!=null)
 				  MyCountDownTimer.cancel();
 			   
                switch(position) {
	    	        case 0:
		            	if(ENABLE_LOGS) Log.v("Pete", "spinner - clicked touch");
		            	Gametype = 0;
		            	saveData();
		         	   	findViewById(R.id.StartButton).setVisibility(View.GONE);
		         	   	findViewById(R.id.StopButton).setVisibility(View.GONE);
		         	    findViewById(R.id.ConsumedLaughs).setVisibility(View.VISIBLE);		         	    
		         	    findViewById(R.id.settings).setVisibility(View.GONE);

		         	    if(laughs >= LAUGHS_MAX && !mSubscribedToInfiniteLaugh)
		         		    updateEndofFreeStuff();
		         	    else
		         	    	updateUiDefault();
			         	   
		            	break;
	    	        case 1:
		            	if(ENABLE_LOGS) Log.v("Pete", "spinner - clicked sleeper");
		            	Gametype = 1;
		            	saveData(); 
		         	   	findViewById(R.id.StartButton).setVisibility(View.VISIBLE);
		         	    findViewById(R.id.settings).setVisibility(View.VISIBLE);
		         	   	findViewById(R.id.StopButton).setVisibility(View.GONE);
		         	    findViewById(R.id.ConsumedLaughs).setVisibility(View.GONE);
		         	       
		         	   if(FreeSleeperRunningtime >= MAX_SLEEPER_TIME && !mSubscribedToInfiniteLaugh)
		         		   updateEndofFreeStuff();
		         	   else
		         		   updateUiDefault();
		         	   
	    	            break;
	    	        default:
	    	        	if(ENABLE_LOGS) Log.v("Pete", "position is not set so we use 0 as default...");
	
	    	            break;   	
                }
            }
                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                   
     
                }
            });
		
		updateUi(true);
		
    }
    
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if(ENABLE_LOGS) Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            if(ENABLE_LOGS) Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            /*
            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            if(ENABLE_LOGS) Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
             */
            
            // Do we have the infinite laugh plan?
            Purchase infiniteLaughPurchase = inventory.getPurchase(SKU_INFINITE_LAUGH);
            mSubscribedToInfiniteLaugh = (infiniteLaughPurchase != null &&
                    verifyDeveloperPayload(infiniteLaughPurchase));
            if(ENABLE_LOGS) Log.d(TAG, "User " + (mSubscribedToInfiniteLaugh ? "HAS" : "DOES NOT HAVE")
                        + " infinite laugh subscription.");
            //if (mSubscribedToInfiniteLaugh) laughs = LAUGHS_MAX;

            /*	
            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                if(ENABLE_LOGS) Log.d(TAG, "We have gas. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
                return;
            }
            */

            updateUi(true);
            setWaitScreen(false);
            if(ENABLE_LOGS) Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    
    // updates UI to reflect model
    public void updateUi(boolean start) {

    	boolean thistypelocked = false;
    	
    	int nbr_of_pictures,random_range,pic_id,windowWidth,windowHeight;
    	Random random_pic = new Random();
    	
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	windowWidth = size.x;
    	windowHeight = size.y;
    	
    	int PicNumberWaidth = (int) ( windowWidth/1.2);
    	int PicNumberHeight = windowHeight/2;
    	
    	if(laughs >= LAUGHS_MAX && Gametype==0)
    		thistypelocked = true;
    	
    	if(FreeSleeperRunningtime >= MAX_SLEEPER_TIME && Gametype==1)
    		thistypelocked = true;
    	
    	
    	if (!thistypelocked || mSubscribedToInfiniteLaugh) {
    		
    		findViewById(R.id.screen_main).setBackgroundColor(Color.RED);
    		
	    	nbr_of_pictures = Pictures.BACKGROUND_IDS.length;
	    	random_range = nbr_of_pictures - 1;
	    	pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;
	    	
    		RelativeLayout rLayout = (RelativeLayout) findViewById (R.id.screen_main);
    		Resources res = getResources(); //resource handle
    		Drawable drawable = res.getDrawable(Pictures.BACKGROUND_IDS[pic_id]); //new Image that was added to the res folder
    		//Drawable drawable = res.getDrawable(R.drawable.checkerboard); //new Image that was added to the res folder
  		    rLayout.setBackground(drawable);
    		
    		findViewById(R.id.BuyButton).setVisibility(View.GONE);
    	
		   	//TextView viewlaughs;
	    	viewlaughs = (TextView) findViewById(R.id.ConsumedLaughs); 
	    	//viewlaughs.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
	    	viewlaughs.setTypeface(viewlaughs.getTypeface(), Typeface.BOLD);
	    	
	    	if(mSubscribedToInfiniteLaugh){
	    		viewlaughs.setText("Infinite Laughs Purchased!");
	    		laughs = 0;
	    	}
	    	else
	    		viewlaughs.setText("Laughs: " + laughs + "/" + LAUGHS_MAX);
	    		
	    	
	    	nbr_of_pictures = Pictures.SMILEY_IDS.length;
	    	
	    	random_range = nbr_of_pictures - 1;
	    	
	    	pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;
	    	
	    	if(start) pic_id=1;
	    		
	    	ImageButton picture;
	    	picture = (ImageButton) findViewById(R.id.imageButton);
	    	picture.setImageBitmap(decodeSampledBitmapFromResource(getResources(),Pictures.SMILEY_IDS[pic_id], PicNumberWaidth, PicNumberHeight));
	    	picture.getLayoutParams().height=PicNumberHeight;
	    	picture.getLayoutParams().width=PicNumberWaidth;
	    	picture.setScaleType(ImageView.ScaleType.FIT_XY);
	    	
	    	if(!start && Gametype==0){
	    		mp = MediaPlayer.create(this, Pictures.SOUND_IDS[pic_id]);
	    		mp.start();
	    	}
	    	
	    	if(!start && Gametype==1){
		    	nbr_of_pictures = Pictures.SOUND_BOX_IDS.length;
		    	random_range = nbr_of_pictures - 1;
		    	pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;
		    	
		    	if (mp != null) {
		    			if(mp.isPlaying()){
		    				if(ENABLE_LOGS) Log.d(TAG, "mp.isPlaying()...");
		    			}else{
		    				if(ENABLE_LOGS) Log.d(TAG, "mp.isNotPlaying()...");
		    				mp.release();
		    				mp = null;
				    		mp = MediaPlayer.create(this, Pictures.SOUND_BOX_IDS[pic_id]);
				    		mp.start();	
		    			}
		    	}else{
		    		mp = MediaPlayer.create(this, Pictures.SOUND_BOX_IDS[pic_id]);
		    		mp.start();	    		
		    	}
		    		
	    	}
	    	
	    	if(Gametype==1 && !start && SleeperOngoing) {
	    		SetCountDownTimer(10000);
	    	}
	    	
	    	if(Gametype==1 && !start && !SleeperOngoing) {
	    		StopButtonClicked(picture) ;
	    		turnOffScreen();
	    	}
	    	
    	}
    	else{
    		
    		updateEndofFreeStuff();
	    	
    	}
    	
    	
    }
    
    // updates UI to reflect model
    public void updateUiDefault() {
    	
    	int nbr_of_pictures,random_range,pic_id,windowWidth,windowHeight;
    	Random random_pic = new Random();
    	
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	windowWidth = size.x;
    	windowHeight = size.y;
    	
    	int PicNumberWaidth = (int) ( windowWidth/1.2);
    	int PicNumberHeight = windowHeight/2;
    	
   		
		findViewById(R.id.screen_main).setBackgroundColor(Color.RED);
		
    	nbr_of_pictures = Pictures.BACKGROUND_IDS.length;
    	random_range = nbr_of_pictures - 1;
    	pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;
    	
		RelativeLayout rLayout = (RelativeLayout) findViewById (R.id.screen_main);
		Resources res = getResources(); //resource handle
		Drawable drawable = res.getDrawable(Pictures.BACKGROUND_IDS[pic_id]); //new Image that was added to the res folder
		//Drawable drawable = res.getDrawable(R.drawable.checkerboard); //new Image that was added to the res folder
	    rLayout.setBackground(drawable);
		
		findViewById(R.id.BuyButton).setVisibility(View.GONE);
	
	   	//TextView viewlaughs;
    	viewlaughs = (TextView) findViewById(R.id.ConsumedLaughs); 
    	//viewlaughs.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
    	viewlaughs.setTypeface(viewlaughs.getTypeface(), Typeface.BOLD);
    	
    	if(mSubscribedToInfiniteLaugh){
    		viewlaughs.setText("Infinite Laughs Purchased!");
    		laughs = 0;
    	}
    	else
    		viewlaughs.setText("Laughs: " + laughs + "/" + LAUGHS_MAX);
    		
    	
    	//nbr_of_pictures = Pictures.SMILEY_IDS.length;
    	
    	//random_range = nbr_of_pictures - 1;
    	
    	//pic_id = random_pic.nextInt(random_range - 1 + 1) + 1;
    	
    	pic_id=1;
    		
    	ImageButton picture;
    	picture = (ImageButton) findViewById(R.id.imageButton);
    	picture.setImageBitmap(decodeSampledBitmapFromResource(getResources(),Pictures.SMILEY_IDS[pic_id], PicNumberWaidth, PicNumberHeight));
    	picture.getLayoutParams().height=PicNumberHeight;
    	picture.getLayoutParams().width=PicNumberWaidth;
    	picture.setScaleType(ImageView.ScaleType.FIT_XY);
    	    	
    }
    
    void updateEndofFreeStuff(){
		
    	int pic_id,windowWidth,windowHeight;
    	
    	Display display = getWindowManager().getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	windowWidth = size.x;
    	windowHeight = size.y;
    	
    	int PicNumberWaidth = (int) ( windowWidth/1.2);
    	int PicNumberHeight = windowHeight/2;
    	
 	   	findViewById(R.id.StartButton).setVisibility(View.GONE);
 	   	findViewById(R.id.StopButton).setVisibility(View.GONE);
 	   findViewById(R.id.settings).setVisibility(View.GONE); 
 	   
		findViewById(R.id.BuyButton).setVisibility(View.VISIBLE);
		
		findViewById(R.id.screen_main).setBackgroundColor(Color.GRAY);
		
	   	//TextView viewlaughs;
   		findViewById(R.id.ConsumedLaughs).setVisibility(View.VISIBLE);  
    	viewlaughs = (TextView) findViewById(R.id.ConsumedLaughs); 
    	//viewlaughs.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
    	viewlaughs.setTypeface(viewlaughs.getTypeface(), Typeface.BOLD);
    	
    	if(Gametype==0){
    		viewlaughs.setText("End of Laughs -  " + LAUGHS_MAX + "/" + LAUGHS_MAX);    	
    	}
    	else{
    		viewlaughs.setText("End of free sleeper time!"); 
    	}
    	
    	pic_id = 1;
    	
    	ImageButton picture;
    	picture = (ImageButton) findViewById(R.id.imageButton);
    	picture.setImageBitmap(decodeSampledBitmapFromResource(getResources(),Pictures.SMILEY_SAD_IDS[pic_id], PicNumberWaidth, PicNumberHeight));
    	picture.getLayoutParams().height=PicNumberHeight;
    	picture.getLayoutParams().width=PicNumberWaidth;
    	picture.setScaleType(ImageView.ScaleType.FIT_XY);
    	
    	mp = MediaPlayer.create(this, Pictures.SOUND_CRY_IDS[pic_id]);
    	mp.start();
    }

   
    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
    	if(ENABLE_LOGS) Log.d(TAG, "in setWaitScreen");
        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }
    
	public void addButtonListener() {

		imgButton = (ImageButton) findViewById(R.id.imageButton);
		imgButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				
			   if(Gametype==1) return;
					
			   laughs++;
			   //Toast.makeText(BabyMain.this,"ImageButton is working. laughts: " + laughs, Toast.LENGTH_SHORT).show();  
			   
			   // Release any resources from previous MediaPlayer
			   if (mp != null) {
			      mp.release();
			      mp = null;
			   }
			   
			   updateUi(false);
			}
		});
	}

	/*
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }
	 */

   /*
   public void onServiceDisconnected(ComponentName name) {
	   if(ENABLE_LOGS) Log.d("Pete", "Billing service Disconnected");
       mService = null;
   }

   public void onServiceConnected(ComponentName name,
      IBinder service) {
       mService = IInAppBillingService.Stub.asInterface(service);
       if(ENABLE_LOGS) Log.d("Pete", "Billing service Connected");
   }
   */
    
   void saveData() {
       /*
        * WARNING: on a real application, we recommend you save data in a secure way to
        * prevent tampering. For simplicity in this sample, we simply store the data using a
        * SharedPreferences.
        */
       SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
       spe.putInt("laughs", laughs);
       spe.putInt("test", InfiniteLaughsBought);
       spe.putInt("Gametype", Gametype);
       spe.putInt("FreeSleeperRunningtime", FreeSleeperRunningtime);
       spe.putInt("TimeToRunSleeper", TimeToRunSleeper);
       //spe.commit();
	   spe.apply();
       if(ENABLE_LOGS) Log.d("Pete", "Saved data: laughs = " + String.valueOf(laughs));
       if(ENABLE_LOGS) Log.d("Pete", "Saved data: test = " + String.valueOf(InfiniteLaughsBought));
       if(ENABLE_LOGS) Log.d("Pete", "Saved data: Gametype = " + String.valueOf(Gametype));
       if(ENABLE_LOGS) Log.d("Pete", "Saved data: FreeSleeperRunningtime = " + String.valueOf(FreeSleeperRunningtime));
   }

   void loadData() {
       SharedPreferences sp = getPreferences(MODE_PRIVATE);
       laughs = sp.getInt("laughs", 0);
       InfiniteLaughsBought = sp.getInt("test", 0);
       Gametype = sp.getInt("Gametype", 0);
       FreeSleeperRunningtime = sp.getInt("FreeSleeperRunningtime", 60);     
       TimeToRunSleeper = sp.getInt("TimeToRunSleeper", 5);
       
       if(ENABLE_LOGS) Log.d("Pete", "Loaded data: laughs = " + String.valueOf(laughs));
       if(ENABLE_LOGS) Log.d("Pete", "Loaded data: InfiniteLaughsBought = " + String.valueOf(InfiniteLaughsBought));
       if(ENABLE_LOGS) Log.d("Pete", "Loaded data: Gametype = " + String.valueOf(Gametype));
       if(ENABLE_LOGS) Log.d("Pete", "Loaded data: FreeSleeperRunningtime = " + String.valueOf(FreeSleeperRunningtime));
   }
   
   void complain(String message) {
       Log.e(TAG, "**** TrivialDrive Error: " + message);
       alert("Error: " + message);
   }
   
   void alert(String message) {
       AlertDialog.Builder bld = new AlertDialog.Builder(this);
       bld.setMessage(message);
       bld.setNeutralButton("OK", null);
       if(ENABLE_LOGS) Log.d(TAG, "Showing alert dialog: " + message);
       bld.create().show();
   }
   
   // Called when consumption is complete
   IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
       public void onConsumeFinished(Purchase purchase, IabResult result) {
           if(ENABLE_LOGS) Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

           // if we were disposed of in the meantime, quit.
           if (mHelper == null) return;

           // We know this is the "gas" sku because it's the only one we consume,
           // so we don't check which sku was consumed. If you have more than one
           // sku, you probably should check...
           if (result.isSuccess()) {
               // successfully consumed, so we apply the effects of the item in our
               // game world's logic, which in our case means filling the gas tank a bit
               if(ENABLE_LOGS) Log.d(TAG, "Consumption successful. Provisioning.");
               laughs = laughs == LAUGHS_MAX ? LAUGHS_MAX : laughs + 1;
               saveData();
               alert("You filled 1/4 tank. Your tank is now " + String.valueOf(laughs) + "/4 full!");
           }
           else {
               complain("Error while consuming: " + result);
           }
           updateUi(false);
           setWaitScreen(false);
           if(ENABLE_LOGS) Log.d(TAG, "End consumption flow.");
       }
   };
   
   /** Verifies the developer payload of a purchase. */
   boolean verifyDeveloperPayload(Purchase p) {
        //String payload = p.getDeveloperPayload();

       /*
        * TO-DO: verify that the developer payload of the purchase is correct. It will be
        * the same one that you sent when initiating the purchase.
        *
        * WARNING: Locally generating a random string when starting a purchase and
        * verifying it here might seem like a good approach, but this will fail in the
        * case where the user purchases an item on one device and then uses your app on
        * a different device, because on the other device you will not have access to the
        * random string you originally generated.
        *
        * So a good developer payload has these characteristics:
        *
        * 1. If two different users purchase an item, the payload is different between them,
        *    so that one user's purchase can't be replayed to another user.
        *
        * 2. The payload must be such that you can verify it even when the app wasn't the
        *    one who initiated the purchase flow (so that items purchased by the user on
        *    one device work on other devices owned by the user).
        *
        * Using your own server to store and verify developer payloads across app
        * installations is recommended.
        */

       return true;
   }
 
   // Callback for when a purchase is finished
   IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
       public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
           if(ENABLE_LOGS) Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

           // if we were disposed of in the meantime, quit.
           if (mHelper == null) return;

           if (result.isFailure()) {
               complain("Error purchasing: " + result);
               setWaitScreen(false);
               return;
           }
           if (!verifyDeveloperPayload(purchase)) {
               complain("Error purchasing. Authenticity verification failed.");
               setWaitScreen(false);
               return;
           }

           if(ENABLE_LOGS) Log.d(TAG, "Purchase successful.");

           if (purchase.getSku().equals(SKU_GAS)) {
               // bought 1/4 tank of gas. So consume it.
               if(ENABLE_LOGS) Log.d(TAG, "Purchase is gas. Starting gas consumption.");
               mHelper.consumeAsync(purchase, mConsumeFinishedListener);
           }
           else if (purchase.getSku().equals(SKU_PREMIUM)) {
               // bought the premium upgrade!
               if(ENABLE_LOGS) Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
               alert("Thank you for upgrading to premium!");
               mIsPremium = true;
               updateUi(false);
               setWaitScreen(false);
           }
           else if (purchase.getSku().equals(SKU_INFINITE_LAUGH)) {
               // bought the infinite gas subscription
               if(ENABLE_LOGS) Log.d(TAG, "Infinite gas subscription purchased.");
               alert("Thank you for subscribing to infinite laughs and sleeper time!");
               mSubscribedToInfiniteLaugh = true;
               laughs = LAUGHS_MAX;
               InfiniteLaughsBought = 1;
               saveData();
               updateUi(false);
               setWaitScreen(false);
           }
       }
   };
   
   // User clicked the "Buy Gas" button
   //Never used with this APP!!!!!
   public void onBuyGasButtonClicked(View arg0) {
	   
	   Toast.makeText(BabyMain.this,"onBuyGasButtonClicked", Toast.LENGTH_SHORT).show();
	   
       if(ENABLE_LOGS) Log.d(TAG, "Buy gas button clicked.");

       if (mSubscribedToInfiniteLaugh) {
           complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
           return;
       }

       if (laughs >=  LAUGHS_MAX) {
           complain("Your tank is full. Drive around a bit!");
           return;
       }

       // launch the gas purchase UI flow.
       // We will be notified of completion via mPurchaseFinishedListener
       setWaitScreen(true);
       if(ENABLE_LOGS) Log.d(TAG, "Launching purchase flow for gas.");

       /* TO-DO: for security, generate your payload here for verification. See the comments on
        *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
        *        an empty string, but on a production app you should carefully generate this. */
       String payload = "";

       mHelper.launchPurchaseFlow(this, SKU_GAS, RC_REQUEST,
               mPurchaseFinishedListener, payload);
   }
   
   public void StartButtonClicked(View arg0) {
	   if(ENABLE_LOGS) Log.d(TAG, "StartButtonClicked");
	   
	   findViewById(R.id.StartButton).setVisibility(View.GONE);
	   findViewById(R.id.StopButton).setVisibility(View.VISIBLE);
	   findViewById(R.id.settings).setVisibility(View.GONE);

	   SetSleeperTimer(TimeToRunSleeper);
	   updateUi(false);
	   
	   easyTracker.send(MapBuilder.createEvent("StartButtonClicked","Sleeper", "1", null).build());
   }
   
   public void StopButtonClicked(View arg0) {
	   if(ENABLE_LOGS) Log.d(TAG, "StopButtonClicked");
	   
	   findViewById(R.id.StartButton).setVisibility(View.VISIBLE);
	   findViewById(R.id.settings).setVisibility(View.VISIBLE);
	   findViewById(R.id.StopButton).setVisibility(View.GONE);

	   // Release any resources from previous MediaPlayer
	   if (mp != null) {
		  if(ENABLE_LOGS) Log.d(TAG, "mp.release");
	      mp.release();
	      mp = null;
	   }
	   
       if(MyCountDownTimer!=null)
		  MyCountDownTimer.cancel();
   }
   
   public void onInfiniteLaughButtonClicked(View arg0) {
	   
	   Toast.makeText(BabyMain.this,"onInfiniteGasButtonClicked", Toast.LENGTH_SHORT).show();
	   
       if (!mHelper.subscriptionsSupported()) {
           complain("Subscriptions not supported on your device yet. Sorry!");
           return;
       }
              
       if (mSubscribedToInfiniteLaugh) {
    	   Toast.makeText(BabyMain.this,"Allready owned " + SKU_INFINITE_LAUGH, Toast.LENGTH_SHORT).show();
    	   return;
       }

       /* TO-DO: for security, generate your payload here for verification. See the comments on
        *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
        *        an empty string, but on a production app you should carefully generate this. */
       String payload = "";

       setWaitScreen(true);
       if(ENABLE_LOGS) Log.d(TAG, "Launching purchase flow for infinite gas subscription. SKU_INFINITE_GAS: " + SKU_INFINITE_LAUGH + " RC_REQUEST: " + RC_REQUEST);
       /* mHelper.launchPurchaseFlow(this,
               SKU_INFINITE_GAS, IabHelper.ITEM_TYPE_SUBS,
               RC_REQUEST, mPurchaseFinishedListener, payload);
       */
       mHelper.launchPurchaseFlow(this,
               SKU_INFINITE_LAUGH, IabHelper.ITEM_TYPE_INAPP,
               RC_REQUEST, mPurchaseFinishedListener, payload);
   }
   
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	   if(ENABLE_LOGS) Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
	
	   // Pass on the activity result to the helper for handling
	   if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	       // not handled, so handle it ourselves (here's where you'd
	       // perform any handling of activity results not related to in-app
	       // billing...
	       super.onActivityResult(requestCode, resultCode, data);
	   }
	   else {
	       if(ENABLE_LOGS) Log.d(TAG, "onActivityResult handled by IABUtil.");
	   }
   }

   @Override
   public void onSignInFailed() {
   	if(ENABLE_LOGS) Log.v("Pete", "In onSignInFailed...");
   	MyisSignedIn = false;
   	
   	if(ENABLE_LOGS) Log.v("Pete", "isTaskRoot(): " + isTaskRoot());
   	
   	if(ENABLE_LOGS) Log.v("Pete", "OnPause: " + OnPause);
   	
   	if(!OnPause){
	    	if(ENABLE_LOGS) Log.v("Pete", "In removing sign out button...");
	    	
	        View b_out = findViewById(R.id.button_sign_out);
	        
	        if(b_out==null){
	        	if(ENABLE_LOGS) Log.v("Pete", "b_out==null");
	        }
	        else{
	        	b_out.setVisibility(View.GONE);
	        }
	   
	        if(ENABLE_LOGS) Log.v("Pete", "In adding sign in button...");
	        
	        View b_in = findViewById(R.id.button_sign_in);
	        
	        if(b_in==null){
	        	if(ENABLE_LOGS) Log.v("Pete", "b_in==null");
	        }
	        else{
	        	b_in.setVisibility(View.VISIBLE);  	
	        }
   	}
   }
   
   @Override
   public void onSignInSucceeded() {
   	if(ENABLE_LOGS) Log.v("Pete", "In onSignInSucceeded...");
   	MyisSignedIn = true;
   	
   	if(ENABLE_LOGS) Log.v("Pete", "isTaskRoot(): " + isTaskRoot());
   	
   	if(ENABLE_LOGS) Log.v("Pete", "OnPause: " + OnPause);
  
   	if(!OnPause){
	        View b_in = findViewById(R.id.button_sign_in);
	        
	        if(b_in==null){
	        	if(ENABLE_LOGS) Log.v("Pete", "In onSignInSucceeded - b_in==null");
	        }
	        else{
	        	b_in.setVisibility(View.GONE);
	        }
	        
	        View b_out = findViewById(R.id.button_sign_out);
	        
	        if(b_out==null){
	        	if(ENABLE_LOGS) Log.v("Pete", "In onSignInSucceeded - b_out==null");
	        }
	        else{
	        	b_out.setVisibility(View.VISIBLE);
	        }
	        
	        // Set the greeting appropriately on main menu
	        Player p = Games.Players.getCurrentPlayer(getApiClient());
	        String displayName;
	        if (p == null) {
	            if(ENABLE_LOGS) Log.v("Pete", "getCurrentPlayer() is NULL!");
	            //displayName = "???";
	        } else {
	            displayName = p.getDisplayName();
	            if(ENABLE_LOGS) Log.v("Pete", "getCurrentPlayer() is " + displayName);
	            
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
   }
   
   //@Override
   public void onSignInButtonClicked(View view) {
   	if(ENABLE_LOGS) Log.v("Pete", "In onSignInButtonClicked...");
       // start the sign-in flow
       beginUserInitiatedSignIn();
   }
   
   public void CheckIfSignedInAndSetButtons(){
   	 if(ENABLE_LOGS) Log.d("Pete", "CheckIfSignedInAndSetButtons....");
   	
   	if(isSignedIn()){
   		    if(ENABLE_LOGS) Log.d("Pete", "isSignedIn....");
   		
	        View b_in = findViewById(R.id.button_sign_in);
	        b_in.setVisibility(View.GONE);
	        
	        View b_out = findViewById(R.id.button_sign_out);
	        b_out.setVisibility(View.VISIBLE); 			
   	}else {
   		   if(ENABLE_LOGS) Log.d("Pete", "isSignedOut....");
   		   
           View b_out = findViewById(R.id.button_sign_out);
           b_out.setVisibility(View.GONE);
      
           View b_in = findViewById(R.id.button_sign_in);
           b_in.setVisibility(View.VISIBLE);    		
   	}
   	
   }
   
   //@Override
   public void onSignOutButtonClicked(View view) {
   	if(ENABLE_LOGS) Log.d("Pete", "In onSignOutButtonClicked...");
   	MyisSignedIn = false;
   	
   	if(isSignedIn()){
   		if(ENABLE_LOGS) Log.d("Pete", "lets sign out...");
        signOut();
   	}
       
       View b_out = findViewById(R.id.button_sign_out);
       b_out.setVisibility(View.GONE);
  
       View b_in = findViewById(R.id.button_sign_in);
       b_in.setVisibility(View.VISIBLE);
       
   }
   
   public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
           int reqWidth, int reqHeight) {

       // First decode with inJustDecodeBounds=true to check dimensions
       final BitmapFactory.Options options = new BitmapFactory.Options();
       options.inJustDecodeBounds = true;
       BitmapFactory.decodeResource(res, resId, options);

       // Calculate inSampleSize
       options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

       // Decode bitmap with inSampleSize set
       options.inJustDecodeBounds = false;
       return BitmapFactory.decodeResource(res, resId, options);
   }
   
   public static int calculateInSampleSize(
	   BitmapFactory.Options options, int reqWidth, int reqHeight) {
	   // Raw height and width of image
	   final int height = options.outHeight;
	   final int width = options.outWidth;
	   int inSampleSize = 1;
	
	   if (height > reqHeight || width > reqWidth) {
	
	       final int halfHeight = height / 2;
	       final int halfWidth = width / 2;
	
	       // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	       // height and width larger than the requested height and width.
	       while ((halfHeight / inSampleSize) > reqHeight
	               && (halfWidth / inSampleSize) > reqWidth) {
	           inSampleSize *= 2;
	       }
	   }
	
	   return inSampleSize;
   }
   
   public class MyAdapterGameType extends ArrayAdapter<String> { 
   	public MyAdapterGameType(Context ctx, int txtViewResourceId, String[] objects) { 
   		super(ctx, txtViewResourceId, objects); 
   	} 
   	
   	@Override public View getDropDownView(int position, View cnvtView, ViewGroup prnt) { 
   		return getCustomView(position, cnvtView, prnt); 
   	} 
   	
   	@Override public View getView(int pos, View cnvtView, ViewGroup prnt) { 
   		return getCustomView(pos, cnvtView, prnt); 
   	} 
   	
   	public View getCustomView(int position, View convertView, ViewGroup parent) { 
   		 LayoutInflater inflater = getLayoutInflater(); 
   		 View mySpinner = inflater.inflate(R.layout.my_spinner, parent, false); 
   		 TextView main_text = (TextView) mySpinner .findViewById(R.id.my_spinner_text); 
   		 main_text.setText(GameTypeValues[position]); 
   		return mySpinner; 
   		} 
   }

   
   public void SetCountDownTimer(long startfromthis_ms) {
	 	  
	    MyCountDownTimer = new CountDownTimer(startfromthis_ms, 1000) {
   		
  	    public void onTick(long millisUntilFinished) {

  	         myRemainingTime = millisUntilFinished;
  	         FreeSleeperRunningtime++;
  	    }

  	    public void onFinish() {
  	    	updateUi(false);
  	     }
  	    }.start();
   }
   
   public void turnOffScreen(){
       // turn off screen
	   if(ENABLE_LOGS) Log.d ("Pete", "turnOffScreen...");
	   getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

  }
   
   public void SetSleeperTimer(int startfromthis_ms) {
	 	  
	    if(ENABLE_LOGS) Log.d("Pete", "SetSleeperTimer..."); 
	    SleeperOngoing = true;
	   
	    MyCountDownTimer2 = new CountDownTimer(startfromthis_ms*60*1000, 1000) {
	    	
 	    public void onTick(long millisUntilFinished) {

 	    }

 	    public void onFinish() {
 	    	SleeperOngoing = false; 	 
 	    	if(ENABLE_LOGS) Log.v("Pete", "SetSleeperTimer - onFinish..."); 
 	     }
 	    }.start();
   }
   
   
   
   @Override
   public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

		TimeToRunSleeper_tmp = newVal;
		if(ENABLE_LOGS) Log.v("Pete", "onValueChange - newVal: " + newVal);
		if(ENABLE_LOGS) Log.v("Pete", "onValueChange - oldVal: " + oldVal);
    }

   public void show(View arg0)
   {

        final Dialog d = new Dialog(BabyMain.this);
        d.setTitle("Set Time For Music Box (min)");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.MynumberPicker);
        np.setMaxValue(60); // max value 100
        np.setMinValue(0);   // min value 0
        np.setValue(TimeToRunSleeper);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new OnClickListener()
        {
         @Override
         public void onClick(View v) {
        	 TimeToRunSleeper = TimeToRunSleeper_tmp;
        	 saveData();
             d.dismiss();
          }    
         });
        b2.setOnClickListener(new OnClickListener()
        {
         @Override
         public void onClick(View v) {
             d.dismiss(); // dismiss the dialog
          }    
         });
      d.show();

   }
   
   @Override
   public void onStart() {
   	if(ENABLE_LOGS) Log.v("Pete", "BabyMain onStart...");
     super.onStart();
     // The rest of your onStart() code.
     EasyTracker.getInstance(this).activityStart(this);  // Add this method.
   }

   @Override
   public void onStop() {
   	if(ENABLE_LOGS) Log.v("Pete", "BabyMain onStop...");
     super.onStop();
     // The rest of your onStop() code.
     EasyTracker.getInstance(this).activityStop(this);  // Add this method.
   }
   
  
   @Override
   public void onPause() {
       super.onPause();
       if(ENABLE_LOGS) Log.v("Pete", "MainActivity onPause...");
       
       if(MyCountDownTimer!=null)
		  MyCountDownTimer.cancel();
 
       if(MyCountDownTimer2!=null)
		  MyCountDownTimer2.cancel();
       
	   // Release any resources from previous MediaPlayer
	   if (mp != null) {
		   if(ENABLE_LOGS) Log.d(TAG, "onPause() - mp.release()");
	      mp.release();
	      mp = null;
	   }
       
       OnPause = true;
       saveData();
   }
   
   @Override
   public void onResume() {
       super.onResume();
       if(ENABLE_LOGS) Log.v("Pete", "MainActivity onResume...");
       OnPause = false;
       setWaitScreen(false);
       CheckIfSignedInAndSetButtons();
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       
       if(Gametype==1){
    	   findViewById(R.id.StartButton).setVisibility(View.VISIBLE);
    	   findViewById(R.id.settings).setVisibility(View.VISIBLE);
    	   findViewById(R.id.StopButton).setVisibility(View.GONE);
    	   findViewById(R.id.ConsumedLaughs).setVisibility(View.GONE);
       }else{
    	   findViewById(R.id.StartButton).setVisibility(View.GONE);
    	   findViewById(R.id.settings).setVisibility(View.GONE);
    	   findViewById(R.id.StopButton).setVisibility(View.GONE);
    	   findViewById(R.id.ConsumedLaughs).setVisibility(View.VISIBLE);   	   
       }
   }
   
   // We're being destroyed. It's important to dispose of the helper here!
   @Override
   public void onDestroy() {
       super.onDestroy();

       if(ENABLE_LOGS) Log.d(TAG, "onDestroy()...");
       
       saveData();
       
       if(MyCountDownTimer!=null)
		  MyCountDownTimer.cancel();
       
       if(MyCountDownTimer2!=null)
		  MyCountDownTimer2.cancel();
		
	   // Release any resources from previous MediaPlayer
	   if (mp != null) {
		  if(ENABLE_LOGS) Log.d(TAG, "onDestroy() - mp.release()");
	      mp.release();
	      mp = null;
	   }
	   
       // very important:
       if(ENABLE_LOGS) Log.d(TAG, "Destroying helper.");
       if (mHelper != null) {
           mHelper.dispose();
           mHelper = null;
       }
   }
}
