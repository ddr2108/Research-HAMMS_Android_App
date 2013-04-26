package edu.gt.vestibular.assitant;

import java.util.Calendar;

import edu.gt.vestibular.assitant.R;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.app.PendingIntent;


public class AlarmReceiver extends BroadcastReceiver  {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		//Get time
	      Calendar calendar = Calendar.getInstance();
	      //Create notification managers
	      NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	      final Notification notifyDetails = new Notification(R.drawable.icon, "Myapp",calendar.getTimeInMillis());
	      //Create intent to go to
	      Intent notifyIntent = new Intent(context, Main.class);
	      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
	      //Create notification
	      notifyDetails.setLatestEventInfo(context, "Vestibular Rehab", "Time to do your Exercises", pendingIntent);
	      notifyDetails.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
	      //Display notification
	      mNotificationManager.notify(0, notifyDetails);

	}

}
