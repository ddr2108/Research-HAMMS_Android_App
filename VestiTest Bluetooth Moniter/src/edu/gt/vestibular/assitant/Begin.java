package edu.gt.vestibular.assitant;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import edu.gt.vestibular.assitant.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class Begin extends Activity implements OnClickListener, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Called when the activity is first created. */
	
	//Variables to hold Preferences Items
    public static final String Preferences = "Prefs";
    public static final String Preferences_Name = "Name";
    public static final String Preferences_Email="Email";
    public static final String Preferences_Device="Device";
        
    SharedPreferences mSettings;
    
    ArrayList<Integer> minute;
    ArrayList<Integer> second;
    ArrayList<String> exercise;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.begin);
        
        //Get Preferenes
        mSettings = getSharedPreferences(Preferences, Context.MODE_PRIVATE);

        //Set up click listeners for all the buttons
        View startButton = this.findViewById(R.id.start);
        startButton.setOnClickListener(this);
        
        //Get Exercise information
        exerciseInfo();
        
    }
    
    public void exerciseInfo(){
    	//Set up exercieses info
        TextView exercises = (TextView) findViewById(R.id.exerciseInfo);
        
        //Get name of user
        String name = mSettings.getString(Preferences_Name, "John Doe");
        name = name.replace(" ","%20");
        
        //Get date for file
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        String date =  "" + String.format("%02d",(today.get(Calendar.MONTH)+1)) + String.format("%02d",today.get(Calendar.DATE)) + today.get(Calendar.YEAR);
       
        //Create URL
        String URLdata = "http://www.deepdattaroy.com/projects/HAMMS/" + name + "/exercises/" + date;
        
        //Store data recieved
        String data = "";
        
        //Arrays to hold split data
        minute = new ArrayList<Integer>();
        second = new ArrayList<Integer>();
        exercise = new ArrayList<String>();

        try {
            // Create a URL for the desired page
            URL url = new URL(URLdata);

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            
            String str;
            String tokens[];
            while ((str = in.readLine()) != null) {
            	//Split data
            	tokens = str.split(":|-");
            	//Put data in array
            	minute.add(Integer.parseInt(tokens[0]));
            	second.add(Integer.parseInt(tokens[1]));
            	exercise.add(tokens[2]);
            	//Put data in string to display
            	data = data + "\n" + tokens[0]+":"+tokens[1]+"\t\t\t\t"+tokens[2];
            }
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        
        //Put string in textbox
        if (!data.equals("")){
        	data = "Time" + "\t\t\t" + "Exercise\n" + data;
        	exercises.setText(data);

        }
    }
    
    //Button clicked
    public void onClick(View v){
    	Intent nextTask;
    	switch(v.getId()){
    		//Begin button pressed
    		case R.id.start:
    			if (exercise.size()>0){
	    			//Begin testing
	    			nextTask = new Intent(this,Monitor.class);
	    			//Put extra info
	    			nextTask.putExtra("min", minute);
	    			nextTask.putExtra("sec", second);
	    			nextTask.putExtra("exercise", exercise);
	    			//Start new activity
	    			startActivity(nextTask);
	    			break;
    			}else{
    				
    				AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		       //Check if internet connected 
    			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

    				if (activeNetworkInfo != null){
    					builder.setMessage("Can not start exercises. No exercise information obtained from server.")
 		               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 		                   public void onClick(DialogInterface dialog, int id) {}
 		               })
 		        	    .setTitle("Error");
    					//Display message
        				AlertDialog dialog = builder.create();
        		        dialog.show();
    				}else{
    					builder.setMessage("Can not start exercises. No internet connection. Please Connect.")
 		               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 		                   public void onClick(DialogInterface dialog, int id) {
 		                   }})
 		        	    .setTitle("Error");
		                //Display message
		                AlertDialog dialog = builder.create();
        		        dialog.show();
		               

    				}
    			}
    	}
    }
    
    public void warning(){
    	
    }
}

