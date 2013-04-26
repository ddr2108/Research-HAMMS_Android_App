package edu.gt.vestibular.assitant;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import edu.gt.vestibular.assitant.R;


public class Save extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    
	//Name of Settings
	public static final String Preferences = "Prefs";
    public static final String Preferences_Name = "Name";
    public static final String Preferences_Email="Email";
    public static final String Preferences_Device="Device";
    public static final String Preferences_Bluetooth="BLuetooth";
    public static final String Preferences_Notification="Notification";
    public static final String Preferences_TimeHour = "Hour";
    public static final String Preferences_TimeMin = "Min";
    
	SharedPreferences mSettings;

	TextView verticalFreq;
    TextView horizontalFreq;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set view to save page
        setContentView(R.layout.save);
       
        //Get Preferenes
        mSettings = getSharedPreferences(Preferences, Context.MODE_PRIVATE);
        
        //Send file to server
        sendFile();
        
      //Update the stats
        updateStats();
        	 
    }
	
	public void updateStats(){
		//Get labels for timer
        TextView verticalTime = (TextView) findViewById(R.id.verticalLabel);
        TextView horizontalTime = (TextView) findViewById(R.id.horizontalLabel);
        verticalFreq = (TextView) findViewById(R.id.verticalFreqLabel);
        horizontalFreq = (TextView) findViewById(R.id.horizontalFreqLabel);

        
        //Get extras from intent
        Bundle extras = getIntent().getExtras();
        int time[] = extras.getIntArray("times");
        
        //Print times to screen
        verticalTime.setText(String.format("%03d sec", time[0]));
        horizontalTime.setText(String.format("%03d sec", time[1]));

        checkDates();
      
        if (!mSettings.getBoolean(Preferences_Bluetooth, false)){
        	verticalFreq.setText("Unknown");
        	horizontalFreq.setText("Unknown");
        }else{
        	getData();
        }
        
	}
	
	public void getData(){
		/*
		String readString = "";
		char[] inputBuffer = new char[750000];
        //Get date for file
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        String date =  "" + (today.get(Calendar.MONTH)+1) + today.get(Calendar.DATE) + today.get(Calendar.YEAR);

		File root = new File("/sdcard/TestFiles/");
		// have the object build the directory structure, if needed.
		File outputFile = new File(root, date);

		
		try { // catches IOException below          
            //Create file input stream for the file
            FileInputStream fIn = new FileInputStream(outputFile);
            //Create an input stream reader
	        InputStreamReader isr = new InputStreamReader(fIn);
	        //Read the file into buffer
	        isr.read(inputBuffer);
	        //Make the buffer into string
	        readString =  new String(inputBuffer);
	        readString = readString.trim();
	        //Close buffer
	    	isr.close();
	      } catch (IOException ioe) {
	          ioe.printStackTrace();
	      } */
		
        //Get extras from intent
        Bundle extras = getIntent().getExtras();
        String readString =  extras.getString("data");

		
		//Split String
		String[] splitString = readString.split("\n|\t|\r");
		
		if (splitString.length > 50){
			int start = 0;
			int rows = 2;
			for (int i = 1; i < 5; i++){
				try{
					Integer.parseInt(splitString[i]);
				}catch (NumberFormatException e){
					start = i;
					rows = (int)(splitString.length - (i +1) )/4;
					break;
				}
				
			}

			long pitch=0;
			long yaw=0;
			for (int i = (start+1+4*2); i<(rows*4-12); i=i+4){
				//.w("asd", String.valueOf(index));
				yaw += Integer.parseInt(splitString[i]);
				//Log.w("asd", String.valueOf(yaw[index]));
				pitch += Integer.parseInt(splitString[i+1]);
				//Log.w("asd", String.valueOf(pitch[index] ));
			}
			
			//Format values
			DecimalFormat myFormatter = new DecimalFormat("###.##");

			verticalFreq.setText("" + myFormatter.format((double)pitch*(0.0175)/360) + " °/s");
        	horizontalFreq.setText("" +  myFormatter.format((double)yaw*(0.0175)/360) + " °/s");
		}else{
			verticalFreq.setText("Unknown");
        	horizontalFreq.setText("Unknown");
		}
	}
	
	public void checkDates(){
		//Get labels for date
        TextView numSkip = (TextView) findViewById(R.id.numSkip);

        int daysSupposed = 0;
        int daysDid = 0;
        
        //Get name of user
        String name = mSettings.getString(Preferences_Name, "John Doe");
        name = name.replace(" ","%20");
        
        //Get date for file
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        
        URL url;
        String URLdata;
        //Check for existance of each
        for (int i = 0;i<7; i++){
        try {
        	//Get date
            today.add(Calendar.DAY_OF_YEAR, -i);
            String date =  "" + String.format("%02d",(today.get(Calendar.MONTH)+1)) + String.format("%02d",today.get(Calendar.DATE)) + today.get(Calendar.YEAR);
                   
            //Create URL
            URLdata = "http://www.deepdattaroy.com/projects/HAMMS/" + name + "/exercises/" + date;
            url = new URL(URLdata);
            //Check if exercise file exists
            url.openStream();
            //increment if it does
            daysSupposed++;
            
            //Create URL
            URLdata = "http://www.deepdattaroy.com/projects/HAMMS/" + name + "/results/" + date;
            url = new URL(URLdata);
            //Check if results file exiats
            url.openStream();
            daysDid++;
            
         } catch (Exception ex) {}        
        }
        
        //Post number of days skipped
        numSkip.setText(""+(daysSupposed-daysDid));
	}
	
	
	public void sendFile(){
		//Get name of user
        String name = mSettings.getString(Preferences_Name, "John Doe");
        
        //Get date for file
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        String date =  "" + String.format("%02d",(today.get(Calendar.MONTH)+1)) + String.format("%02d",today.get(Calendar.DATE)) + today.get(Calendar.YEAR);

        
        View mainMenu = this.findViewById(R.id.home);
        mainMenu.setOnClickListener(this);
        
        //Client object
        FTPClient con = null;
		//String to connect to server
        String ftpserver = "www.deepdattaroy.com";
        String un = "deep";
        String pw = "siddhartha";
        String data = "/sdcard/TestFiles/" + date;
        String filenameStore = "Documents/Web/projects/HAMMS/" + name + "/results/" + date;
        
        try
        {
        	//Create client and connect to server
            con = new FTPClient();
            con.connect(ftpserver);                
            if (con.login(un, pw))
            {
            	//Set up conection
                con.enterLocalPassiveMode();                   // Very Important
                con.setFileType(FTP.BINARY_FILE_TYPE);        //  Very Important
                //Create stream of data to send
                FileInputStream in = new FileInputStream(new File(data));
                //Send file
                con.storeFile(filenameStore, in);
                //Close connection
                in.close();
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        } 
	}
    
    public void onClick(View v) {
		switch(v.getId()){
			case R.id.home:
				Intent newTask = new Intent(this, Main.class);
				startActivity(newTask);
				break;
		  }
	}
}