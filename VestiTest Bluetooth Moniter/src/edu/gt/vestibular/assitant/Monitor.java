package edu.gt.vestibular.assitant;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import edu.gt.vestibular.assitant.R;


public class Monitor extends Activity implements OnClickListener {
    
    TextView secondsLeftText;
    TextView minutesLeftText;

    StringBuilder builder = new StringBuilder();
    
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
	
    //Bluetooth adapter of phone
    private BluetoothAdapter bluetooth;
    //Socket for transferring data
    private BluetoothSocket socket;
    //Mac address of bluetooth component
    private String address;
    //UUID of phone
    private UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  //Buffer to hold data from bluetooth head piece
	byte[] buffer = new byte[750000];

	String dataBluetooth;
	
    //List from server
    ArrayList<Integer> minute;
    ArrayList<Integer> second;
    ArrayList<String> exercise;
    public int exerSize;
    public int exerIndex;
    boolean start = false;
    
    //For measuring time in each direction
    public long startTime = 2;
    public int[] time = {0, 0};
    public int dirCurrent = 0;
    
    //Timer
    CountDownTimer task;
    public int minutesLeft = 0;
    public int secondsLeft = 5;
    View doneButton;
    View saveButton;
    
    
  //Variable to hold if test has begun
    int testState;
    boolean connected;
    //Possible States
    public static final int TEST_ON = 1; 
    public static final int TEST_OFF = 0; 
    
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get labels for timer
        secondsLeftText = (TextView) findViewById(R.id.seconds);
        minutesLeftText = (TextView) findViewById(R.id.minutes);

        //Set up buttons
        doneButton = this.findViewById(R.id.done);
        doneButton.setOnClickListener(this);
        saveButton = this.findViewById(R.id.save);
        saveButton.setOnClickListener(this);
        saveButton.setVisibility(View.GONE);
        
        //Set up initial time left
        minutesLeftText.setText(String.format("%02d", minutesLeft));
        secondsLeftText.setText(String.format("%02d", secondsLeft));
        
        //Get extras from intent
        Bundle extras = getIntent().getExtras();
        minute = (ArrayList<Integer>) extras.getSerializable("min");
        second = (ArrayList<Integer>) extras.getSerializable("sec");
        exercise = (ArrayList<String>) extras.getSerializable("exercise");
        
        //Number of exercises
        exerSize = exercise.size();
        exerIndex = 0;
        
        //Get Preferenes
        mSettings = getSharedPreferences(Preferences, Context.MODE_PRIVATE);
        //Check if recieving data via bluetooth
        Boolean bluetoothData = mSettings.getBoolean(Preferences_Bluetooth, false);
        if (bluetoothData){
        	//Say test has begun
            testState = TEST_ON;
        	bluetoothSetup();
        }
        
        
        
        //Set up counter for initialization to start exercises
        task = new CountDownTimer(5*1000, 980) {
    		
    		//Update every second
    	     public void onTick(long millisUntilFinished) {
    	    	 //Decrement time left
    	    	 if (secondsLeft==0 && minutesLeft!=0){
    	    		 secondsLeft = 59;
    	    		 minutesLeft--;
    	    	 }else if(!(secondsLeft==0 && minutesLeft==0)){
    	    		 secondsLeft--;
    	    	 }
    	    	 
    	    	//Post time left
    	         minutesLeftText.setText(String.format("%02d", minutesLeft));
    	         secondsLeftText.setText(String.format("%02d", secondsLeft));
    	    	 
    	     }
    	     
    	     //after finish go update timer
    	     public void onFinish() {
    	    	 //Update time to 0
    	         minutesLeftText.setText(String.format("%02d", 0));
    	         secondsLeftText.setText(String.format("%02d", 0));
    	         minutesLeft = 0;
    	         secondsLeft = 0;
    	         
    	        if (mSettings.getBoolean(Preferences_Bluetooth, false)&&!connected){
    	        	waitConnect();
    	         }
    	         
    	         
    	         
    	       //Time Exercises
    	         timerUpdate();
    	     }
    	  }.start();

        
    }
    
    public void waitConnect(){
    	// prepare the alert box
	        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
	        // set the message to display message according to plotted elements
	        alertbox.setMessage("Could not connect. Please try again.");
	        // add a neutral button to the alert box
	        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface arg0, int arg1) {
	                launchIntent();
	            }
	        });
	        // show it
	        alertbox.show();
    }
    
    public void timerUpdate(){
    	start = true;
    	task.cancel();
    	final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
		 tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        //Set up time info
        TextView timeTitle = (TextView) findViewById(R.id.timeTitle);
        timeTitle.setText("Time Remaining");
    	
        //Set up image
        ImageView arrows = (ImageView) findViewById(R.id.dirView);
        
    	//Set up timer for updating display
    	minutesLeft = minute.get(exerIndex);
        secondsLeft = second.get(exerIndex);
        
        //Find the direction
        String direction = exercise.get(exerIndex);
        if (direction.equals("vertical")){
        	dirCurrent = 0;
        	arrows.setImageResource(R.drawable.vertical);
        }else{
        	dirCurrent = 1;
        	arrows.setImageResource(R.drawable.horizontal);
        }
        
        exerIndex++;
        
        //Figure home much time to set timer
        int timeLimit = minutesLeft*60+secondsLeft;
        //Set timer
        startTime = System.currentTimeMillis();
        task = new CountDownTimer(timeLimit*1000, 980) {
        	
    		//Update every second
    	     public void onTick(long millisUntilFinished) {
    	    	 //Decrement time left

    	    	 if (secondsLeft==0 && minutesLeft!=0){
    	    		 secondsLeft = 59;
    	    		 minutesLeft--;
    	    	 }else if(!(secondsLeft==0 && minutesLeft==0)){
    	    		 if (secondsLeft%20==0){
    	    			 tg.startTone(ToneGenerator.TONE_PROP_BEEP);
    	    		 }
    	    		 secondsLeft--;
    	    	 }
    	    	 
    	    	 //Beep if close to end
    	    	 if(secondsLeft<=10 && minutesLeft==0){
    	    	     tg.startTone(ToneGenerator.TONE_PROP_BEEP);
    	    	 }
    	    	 
    	    	//Post time left
    	         minutesLeftText.setText(String.format("%02d", minutesLeft));
    	         secondsLeftText.setText(String.format("%02d", secondsLeft));
    	    	 
    	     }
    	     
    	     //after finish go update timer
    	     public void onFinish() {
    	    	 //Update time to 0
    	         minutesLeftText.setText(String.format("%02d", 0));
    	         secondsLeftText.setText(String.format("%02d", 0));
    	         minutesLeft = 0;
    	         secondsLeft = 0;
    	     }
    	  }.start();
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			//Done button pressed
			case R.id.done:
				//Go to next exercise
   	         	if (exerIndex<exerSize && start){
   	        	 	time[dirCurrent]= time[dirCurrent] + (int)((System.currentTimeMillis()-startTime)/1000);
   	         		timerUpdate();
   	         	}else if (start){
   	         		//Cancel rest of time
   	         		time[dirCurrent]= time[dirCurrent] + (int)((System.currentTimeMillis()-startTime)/1000);
   	         		task.cancel();
   	         		minutesLeftText.setText(String.format("%02d", 0));
   	         		secondsLeftText.setText(String.format("%02d", 0));
   	         		dirCurrent = 2;
   	         		start = false;
   	         		//Display correct buttons
   	         		saveButton.setVisibility(View.VISIBLE);
   	         		doneButton.setVisibility(View.GONE);
   	         		//Say test has ended
   	         		testState = TEST_ON;
   	         		//Save file
   	         		saveFile();
   	         	}
				break;
	    	//Save button pressed	
			case R.id.save:
				if (exerIndex==exerSize && !start){
					//Send to stats page
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch(Exception e){
						e.printStackTrace();
					}
					Intent newTask = new Intent(this, Save.class);
					newTask.putExtra("times", time);
					newTask.putExtra("data", dataBluetooth);
					startActivity(newTask);
				}
		      break;
		  }
	}
	
	@Override
	public void onPause(){
			super.onPause();
			task.cancel();
	}

	@Override
	public void onRestart(){
			super.onRestart();
			//Restart Program
			Intent newTask = new Intent(this, Main.class);
			startActivity(newTask);
	}
	
    public void bluetoothSetup(){
    	
    	//Load blutooth status from preferences and set it in the textbox
       address = mSettings.getString(Preferences_Device, "0:0:0:0:0:0");            
            
	    	//Find the adapter of device
	    	bluetooth = BluetoothAdapter.getDefaultAdapter();
	    	if (bluetooth != null) {
		    	//Enable bluetooth
		    	bluetooth.enable();
		    	//While adapter turning on, go on infinite loop
		    	while(bluetooth.getState()!=bluetooth.STATE_ON){}
		
		        //Create the socket for data connections
		        BluetoothSocket tmp = null;
		        
		        // Get a BluetoothSocket to connect with the given BluetoothDevice
		        try {
			        //Get remote bluetooth sensors
		        	BluetoothDevice device = bluetooth.getRemoteDevice(address);
		        	device.getBondState();
		        	// MY_UUID is the app's UUID string, also used by the server code
		            tmp = device.createRfcommSocketToServiceRecord(myUUID);
		            //cancel discovery
		            bluetooth.cancelDiscovery();
		            //Create the socket and connect
		            socket = tmp;
		            socket.connect();
		            //Set up so that asynchronous data can be recieved
		            MotionSensorTask bluetoothData = new MotionSensorTask(socket);
		            connected = true;
		            bluetoothData.execute();
		        } catch (IllegalArgumentException iae){
		        	 // prepare the alert box
			        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			        // set the message to display message according to plotted elements
			        alertbox.setMessage("Please make sure head motion sensor is turned on.");
			        // add a neutral button to the alert box
			        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			            public void onClick(DialogInterface arg0, int arg1) {
			                launchIntent();
			            }
			        });
			        // show it
			        alertbox.show();
		        } catch (IOException e) { 
		        	 // prepare the alert box
			        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
			        // set the message to display message according to plotted elements
			        alertbox.setMessage("Connection Error. Please make sure head motion sensor is turned on.");
			        // add a neutral button to the alert box
			        alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			            public void onClick(DialogInterface arg0, int arg1) {
			                launchIntent();
			            }
			        });
			        // show it
			        alertbox.show();
		        }
	    	}
        }
    
   public void launchIntent(){
	 //Restart Program
	   task.cancel();
		Intent newTask = new Intent(this, Main.class);
		startActivity(newTask);
   }
    
    
    public void saveFile(){
    	//Get dates
	    Calendar today = Calendar.getInstance();
	    String date =  "" + String.format("%02d",(today.get(Calendar.MONTH)+1)) + String.format("%02d",today.get(Calendar.DATE)) + today.get(Calendar.YEAR);
	    String date2 =  "" + String.format("%02d",(today.get(Calendar.MONTH)+1)) + "/" + String.format("%02d",today.get(Calendar.DATE)) + "/" + today.get(Calendar.YEAR) + " " + String.format("%02d",today.get(Calendar.HOUR)) + ":" + String.format("%02d",today.get(Calendar.MINUTE));

	    //Save the Bluetooth data to file
    	try { // catches IOException below
    		//Get the file
    		// create a File object for the parent directory
    		File root = new File("/sdcard/TestFiles/");
    		// have the object build the directory structure, if needed.
    		root.mkdirs();
    		File outputFile = new File(root, date);
			FileOutputStream fos = new FileOutputStream(outputFile );
            //Create Writer
			OutputStreamWriter osw = new OutputStreamWriter(fos);
            
			//Write the data
			if(mSettings.getBoolean(Preferences_Bluetooth, false)){
				dataBluetooth = (new String(buffer).trim());
				osw.write(date2);
				osw.write("\n");
				osw.write(dataBluetooth);
			}else{
				osw.write(date2);
				osw.write("\n");
				osw.write("No data obtained.");
			}
				
				
            osw.flush();
            osw.close();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
    }
   
    public class MotionSensorTask extends AsyncTask<Void, Integer, Void> {

    	//Bluetooth socket
    	BluetoothSocket socket;
        
    	//Constructor
    	public MotionSensorTask(BluetoothSocket socket) {
    		this.socket = socket;
		}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
        	//data for each time
			int data[] = new int[10];
			for (int i=0;i<10;i++){
	        	data[i]=0;
	        }

			try {

            	//get insput stream from socket
                InputStream is = socket.getInputStream();
                //get next character being sent
                int readStream = 0;
                //keep on reading data until no more data
                while(testState != TEST_OFF){
    				//Read in a value
                	buffer[readStream++] = (byte) is.read();
                 }

                //close input stream
                is.close();
                //close socket
                socket.close();
                
                saveFile();
              

            } catch (Exception e) {
                e.printStackTrace();
            }
			return null;		
		}

    }
}
	
