package edu.gt.vestibular.assitant;

import edu.gt.vestibular.assitant.R;

import android.app.Activity;
import android.os.Bundle;

public class Instruct extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set view to instructions page
        setContentView(R.layout.intruct);
    }
}