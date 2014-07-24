/*
 * Author: Marcus Feeney, SolvedIt Ltd
 * File: MainActivity.java
 * 
 * Main class which handles user touch events to pass off to the Sender class.
 * Shows the UI as well as the saving and loading of user set preferences.
 * 
 */

package nz.co.solvedit.androidtouchmouse;

import nz.co.solvedit.androidtouchmouse.Sender.OnMessageReceived;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {
	
	private final String PREF_SENSITIVITY = "pref_sensitivity";
	private final String PREF_ACCELERATION = "pref_acceleration";
	
	private float sensitivity;
	private float acceleration;
	private float movePreviousX;
	private float movePreviousY;
	private float moveResultX;
	private float moveResultY;
	private GestureDetectorCompat touchDetector;
	private SharedPreferences prefs;
	private OnSharedPreferenceChangeListener prefsListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		touchDetector = new GestureDetectorCompat(this, this);	
		setPrefs();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SetPreferenceActivity.class);
		startActivityForResult(intent, 0);
		return true;
	}
	
	public void onButtonLeftClicked(View v) {
		new ConnectTask(prefs, this).execute("0x10|LEFT|DOWN|;");
	}
	
	public void onButtonRightClicked(View v) {
		new ConnectTask(prefs, this).execute("0x10|RIGHT|DOWN|;");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		
		switch (e.getAction()) {
		
		case MotionEvent.ACTION_MOVE:
			handleTouchMove(e);
			break;
			
		case MotionEvent.ACTION_DOWN:
			handleTouchDown(e);
			break;
			
		default:
			break;
		}
		touchDetector.onTouchEvent(e);
		return super.onTouchEvent(e);
	}
	
	/*
	 * Handle touch input of type ACTION_MOVE
	 */
	private void handleTouchMove(MotionEvent e) {			
		float moveDistanceRawX = e.getRawX() - movePreviousX;
        float moveDistanceRawY = e.getRawY() - movePreviousY;   
        moveDistanceRawX *= sensitivity;
        moveDistanceRawY *= sensitivity;
        float accelerationX = (float) ((Math.pow(Math.abs(moveDistanceRawX), acceleration) * Math.signum(moveDistanceRawX)));
        float accelerationY = (float) ((Math.pow(Math.abs(moveDistanceRawY), acceleration) * Math.signum(moveDistanceRawY)));
        moveDistanceRawX = accelerationX;
        moveDistanceRawY = accelerationY;
        moveDistanceRawX += moveResultX;
        moveDistanceRawY += moveResultY;    
        int moveDistanceXFinal = Math.round(moveDistanceRawX);
        int moveDistanceYFinal = Math.round(moveDistanceRawY);    
        if (moveDistanceXFinal != 0 || moveDistanceYFinal != 0)
        	new ConnectTask(prefs, this).execute("0x00|" + moveDistanceXFinal + "|" + moveDistanceYFinal + "|;");
        moveResultX = moveDistanceRawX - moveDistanceXFinal;
        moveResultY = moveDistanceRawY - moveDistanceYFinal;
        movePreviousX = e.getRawX();
        movePreviousY = e.getRawY();
	}
	
	/*
	 * Handle touch input of type ACTION_DOWN
	 */
	private void handleTouchDown(MotionEvent e) {
		moveResultX = 0;
		moveResultY = 0;
		movePreviousX = e.getRawX();
		movePreviousY = e.getRawY();
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		new ConnectTask(prefs, this).execute("0x10|LEFT|DOWN|;");
		return false;
	}
	
	private void setPrefs() {
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {		
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if(key.equals(PREF_SENSITIVITY)) {
					sensitivity = Float.parseFloat(sharedPreferences.getString(key, "0.7"));
				} else if (key.equals(PREF_ACCELERATION)) {
					acceleration = Float.parseFloat(sharedPreferences.getString(key, "1.5"));
				}
				
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(prefsListener);
		sensitivity = Float.parseFloat(prefs.getString("pref_sensitivity", "0.7"));
		acceleration = Float.parseFloat(prefs.getString("pref_acceleration", "1.5"));
	}
	
	@Override
	public boolean onDown(MotionEvent e) {return false;}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {return false;}
	@Override
	public void onLongPress(MotionEvent e) {}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
	@Override
	public void onShowPress(MotionEvent e) {}
	
	/*
	 * AsyncTask to send x,y coordinates to TCP server
	 */
	public class ConnectTask extends AsyncTask<String,String,Sender> implements OnMessageReceived {
		
		Sender client;
		SharedPreferences prefs;
		MainActivity parentActivity; //unused at this stage
		
		public ConnectTask(SharedPreferences prefs, MainActivity parentActivity) {
			this.prefs = prefs;
			this.parentActivity = parentActivity;
		}

        @Override
        protected Sender doInBackground(String... message) {
        	client = new Sender(message[0], prefs, this);
        	client.run();
            return null;
        }

		@Override
		public void messageReceived(String message) {
			
		}
    }

}
