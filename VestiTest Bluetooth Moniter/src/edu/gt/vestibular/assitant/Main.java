package edu.gt.vestibular.assitant;


import edu.gt.vestibular.assitant.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;

public class Main extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	
	//Variables to hold Preferences Items
    public static final String Preferences = "Prefs";
    public static final String Preferences_Name = "Name";
    public static final String Preferences_Email="Email";
    public static final String Preferences_Device="Device";
    public static final String Preferences_Bluetooth="BLuetooth";
    public static final String Preferences_Notification="Notification";
    public static final String Preferences_TimeHour = "Hour";
    public static final String Preferences_TimeMin = "Min";
    
    SharedPreferences mSettings;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Set up click listeners for all the buttons
        View beginButton = this.findViewById(R.id.begin);
        beginButton.setOnClickListener(this);
        View aboutButton = this.findViewById(R.id.about);
        aboutButton.setOnClickListener(this);
        View instructButton = this.findViewById(R.id.instruct);
        instructButton.setOnClickListener(this);
        View optionButton = this.findViewById(R.id.option);
        optionButton.setOnClickListener(this);
        
        //Get Preferenes
        mSettings = getSharedPreferences(Preferences, Context.MODE_PRIVATE);
        //Set default preferences
        Editor settings = mSettings.edit();
        if (mSettings.getString(Preferences_Name,null) == "" || mSettings.getString(Preferences_Name,null) == null)
        	settings.putString(Preferences_Name, "John Doe");
        if(mSettings.getString(Preferences_Email,null) == "" || mSettings.getString(Preferences_Email,null) == null)
        	settings.putString(Preferences_Email, "johndoe@gmail.com");
        if(mSettings.getString(Preferences_Device,null) == "" || mSettings.getString(Preferences_Device,null) == null)
        	settings.putString(Preferences_Device, "0:0:0:0:0:0");

        settings.commit();
        
        connect();
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	//Connect as necessary
    	connect();
    }
    
    public void connect(){
        //Check internet connection
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    if (activeNetworkInfo == null){
	        //Turn on wifi if no connection to internet
	        WifiManager wifiManager;
	        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	        if(!wifiManager.isWifiEnabled()){
	        	wifiManager.setWifiEnabled(true);
	        	//Alert user wifi is turning on
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage("Turning on Wifi.")
	               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                   }})
	        	    .setTitle("Warning");
	                //Display message
	                AlertDialog dialog = builder.create();
 		        dialog.show();
	        }
	        
	    }
	    
	    //Check if bluetooth desired
	    if (mSettings.getBoolean(Preferences_Bluetooth, false)){
		    //If bluetooth off turn on
		    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		    if (!mBluetoothAdapter.isEnabled()) {
		    	mBluetoothAdapter.enable();
		    	//Alert user bluetooth is turning on
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setMessage("Turning on Bluetooth.")
	               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                   }})
	        	    .setTitle("Warning");
	                //Display message
	                AlertDialog dialog = builder.create();
 		        dialog.show();

		    }
	    }

    }
    
    //Button clicked
    public void onClick(View v){
    	Intent nextTask;
    	switch(v.getId()){
    		//Begin button pressed
    		case R.id.begin:
    			//Begin testing
    			nextTask = new Intent(this,Begin.class);
    			startActivity(nextTask);
    			break;
    		case R.id.instruct:
    			//Begin testing
    			nextTask = new Intent(this,Instruct.class);
    			startActivity(nextTask);
    			break;
    		case R.id.option:
    			//Begin testing
    			nextTask = new Intent(this,Options.class);
    			startActivity(nextTask);
    			break;
    		case R.id.about:
    			//Begin testing
    			nextTask = new Intent(this,About.class);
    			startActivity(nextTask);
    			break;
    	}
    }
}

