package edu.gt.vestibular.assitant;

import java.util.Calendar;
import java.util.TimeZone;
import edu.gt.vestibular.assitant.R;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

public class Options extends Activity implements OnClickListener{
    
	//Name of Settings
	public static final String Preferences = "Prefs";
    public static final String Preferences_Name = "Name";
    public static final String Preferences_Email="Email";
    public static final String Preferences_Device="Device";
    public static final String Preferences_Bluetooth="BLuetooth";
    public static final String Preferences_Notification="Notification";
    public static final String Preferences_TimeHour = "Hour";
    public static final String Preferences_TimeMin = "Min";
    
    //Variable to hold Settings
    SharedPreferences mSettings;

	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Set view to options page
        setContentView(R.layout.options);
        
        View emailButton = this.findViewById(R.id.emailButton);
        emailButton.setOnClickListener(this);

        //Get the desired preferences
        mSettings = getSharedPreferences(Preferences, Context.MODE_PRIVATE);

        //Get the data entry boxes
        EditText nameInputBox = (EditText) findViewById(R.id.name);   
        EditText emailInputBox = (EditText) findViewById(R.id.email);
        EditText deviceInputBox = (EditText) findViewById(R.id.device);
        CheckBox bluetoothCheck = (CheckBox) findViewById(R.id.bluetooth);
        CheckBox notificationCheck = (CheckBox) findViewById(R.id.notifications);
        TimePicker notificationTime = (TimePicker) findViewById(R.id.timeNot);
        
        String pref;
        Boolean checkPref;
        int timePref; 
        //Get the stored preference for the Name
        pref = mSettings.getString(Preferences_Name, "John Doe").toString();
        //Store the name in the text box
        nameInputBox.setText(pref);
        
        //Get the stored preference for the email
        pref = mSettings.getString(Preferences_Email, "johndoe@gmail.com").toString();
        //Store the email in the text box
        emailInputBox.setText(pref);
        
      //Get the stored preference for the device
        pref = mSettings.getString(Preferences_Device, "0:0:0:0:0:0").toString();
        //Store the email in the text box
        deviceInputBox.setText(pref);
        
        //Get the stored preference for the device
        checkPref = mSettings.getBoolean(Preferences_Bluetooth, false);
        //Store the email in the text box
        bluetoothCheck.setChecked(checkPref);

      //Get the stored preference for the device
        checkPref = mSettings.getBoolean(Preferences_Notification, false);
        //Store the email in the text box
        notificationCheck.setChecked(checkPref);
        
        //Get the stored preference for the device
        timePref = mSettings.getInt(Preferences_TimeHour, 12);
        notificationTime.setCurrentHour(timePref);
        timePref = mSettings.getInt(Preferences_TimeMin, 00);
        notificationTime.setCurrentMinute(timePref);

        
	}
	
	protected void onPause() 
	{
	  super.onPause();
	  
	  //Get correct settings
      mSettings = getSharedPreferences(Preferences, Context.MODE_PRIVATE);
      //Edit Settings
      Editor settings = mSettings.edit();

      //Get all of the boxes for User input
      EditText nameInputBox = (EditText) findViewById(R.id.name);
      EditText emailInputBox = (EditText) findViewById(R.id.email);
      EditText deviceInputBox = (EditText) findViewById(R.id.device);
      CheckBox bluetoothCheck = (CheckBox) findViewById(R.id.bluetooth);
      CheckBox notificationCheck = (CheckBox) findViewById(R.id.notifications);
      TimePicker notificationTime = (TimePicker) findViewById(R.id.timeNot);
      
      //Get the user input
      String nameInput = nameInputBox.getText().toString();
      String emailInput = emailInputBox.getText().toString();
      String deviceInput = deviceInputBox.getText().toString();
      Boolean bluetoothPref = bluetoothCheck.isChecked();
      Boolean notificationPref = notificationCheck.isChecked();
      int hour = notificationTime.getCurrentHour();
      int min = notificationTime.getCurrentMinute();
    		  
      //If no name is put, defualt to me
      if (nameInput == "" || nameInput == null)
    	  nameInput = "John Doe";
      //If not email put, default to me@gmail.com
      if (emailInput == "" || emailInput == null)
    	  emailInput = "johndoe@gmail.com";
      if (deviceInput == "" || deviceInput == null)
    	  deviceInput = "0:0:0:0:0:0";
      if (bluetoothPref == null)
    	  bluetoothPref = false;
      if (notificationPref == null)
    	  notificationPref = false;
      
      //Store the preferences
      settings.putString(Preferences_Name, nameInput);
      settings.putString(Preferences_Email, emailInput);
      settings.putString(Preferences_Device, deviceInput);
      settings.putBoolean(Preferences_Bluetooth, bluetoothPref);
      settings.putBoolean(Preferences_Notification, notificationPref);
      settings.putInt(Preferences_TimeHour, hour);
      settings.putInt(Preferences_TimeMin, min);
      
      settings.commit();
      
      
      
      if (notificationPref){
    	  setRecurringAlarm();
      }else{
    	  cancelRecurringAlarm();
      }
	}

	private void setRecurringAlarm() {
		//Set up time of notification
	    Calendar notifyTime = Calendar.getInstance();
	    notifyTime.setTimeZone(TimeZone.getDefault());
	    notifyTime.set(Calendar.HOUR_OF_DAY, mSettings.getInt(Preferences_TimeHour, 12));
	    notifyTime.set(Calendar.MINUTE, mSettings.getInt(Preferences_TimeMin, 00));
	    //Set up intent
	    Intent alert = new Intent(this, AlarmReceiver.class);
	    PendingIntent recurringAlert = PendingIntent.getBroadcast(this,
	            0, alert, PendingIntent.FLAG_CANCEL_CURRENT);
	    AlarmManager alarms = (AlarmManager) this.getSystemService(
	            Context.ALARM_SERVICE);
	    //Cancel any existing
	    alarms.cancel(recurringAlert);
	    //Set alarm
	    alarms.setRepeating(AlarmManager.RTC_WAKEUP,
	    		notifyTime.getTimeInMillis(),
	            AlarmManager.INTERVAL_DAY, recurringAlert);
	}
	private void cancelRecurringAlarm() {
		//Cancel notification
		Intent alert = new Intent(this, AlarmReceiver.class);
    	PendingIntent recurringAlert = PendingIntent.getBroadcast(this,
  	            0, alert, PendingIntent.FLAG_CANCEL_CURRENT);
  	    AlarmManager alarms = (AlarmManager) this.getSystemService(
  	            Context.ALARM_SERVICE);
  	    alarms.cancel(recurringAlert);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent nextTask;
    	switch(v.getId()){
    		//Begin button pressed
    		case R.id.emailButton:
    			//Begin testing
    			nextTask = new Intent(Intent.ACTION_SEND);
    			nextTask.setType("text/plain");
    			//Put Recipient
    			String[] recipients = new String[]{((EditText) findViewById(R.id.email)).getText().toString(), "",};
    			nextTask.putExtra(android.content.Intent.EXTRA_EMAIL  ,recipients );
    			//Add subject
    			nextTask.putExtra(Intent.EXTRA_SUBJECT, "Patient: " + ((EditText) findViewById(R.id.name)).getText().toString());
    			//Create an email chooser
          		startActivity(Intent.createChooser(nextTask, "Send mail..."));
    			break;

    	}
	}
}
