package com.example.windowremote;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final String TAG = "WindowRemote";
	
	// Max override time in hours
	private int mMaxTime = 8;
	private int progressPosition;
	private int progressTime;
	
	private TextView epoch;
	private SeekBar positionControl;
	private SeekBar timeControl;
	private TextView positionCounter;
	private TextView timeCounter;
	private Button buttonGo;
	
	
	private float convertTimeProgressToHours(float progress) {
		return progress / 100 * (float)mMaxTime;
	}
	
	protected class SendOverrideTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected Void doInBackground(Void... params) {
			
			long curTime = System.currentTimeMillis() / 1000;
			long overrideLength = (long)((double)progressTime / 100.0 * mMaxTime * 3600);
			long overrideEnd = curTime + overrideLength;
			double position = (double)progressPosition / 100.0;
			String message = position + "," + overrideEnd;
			try {
				DatagramSocket sock = new DatagramSocket();
				InetAddress serverAddr = InetAddress.getByName("pi.fanto.local");
				DatagramPacket dp;
				dp = new DatagramPacket(message.getBytes(), message.length(), serverAddr, 8181);
				sock.send(dp);
				} catch (Exception e){
					Log.d(TAG, e.toString());
				}
			return null;
		}
		
	}
	
	
	private void sendOverride(){
		new SendOverrideTask().execute();
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		epoch = (TextView)findViewById(R.id.epoch);
		
		positionCounter = (TextView)findViewById(R.id.textViewPositionCounter);
		timeCounter = (TextView)findViewById(R.id.textViewTimeCounter);
		
		
		positionControl = (SeekBar)findViewById(R.id.seekBarPosition);
		positionControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progressCounter;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
				progressCounter = progress;
				progressPosition = progress;
				onStopTrackingTouch(seekBar);
			}
			public void onStartTrackingTouch(SeekBar seekBar) { }
			public void onStopTrackingTouch(SeekBar seekBar) {
				positionCounter.setText(progressCounter+"% open");
			}
		});

				
		timeControl = (SeekBar)findViewById(R.id.seekBarTime);
		timeControl.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progressPos;
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { 
				progressPos = progress;
				progressTime = progress;
				onStopTrackingTouch(seekBar);
			}
			public void onStartTrackingTouch(SeekBar seekBar) { }
			public void onStopTrackingTouch(SeekBar seekBar) {
				updateProgressCounter();
			}
			
			private void updateProgressCounter() {
				if (progressPos != 0)
				{
					int minutes = (int)Math.round(60 * convertTimeProgressToHours(progressPos) % 60);
					int hours = (int)((double)progressPos / 100.0 * (double)mMaxTime);
					timeCounter.setText(hours+" hours "+minutes+" minutes");
				}
				else {
					timeCounter.setText("Auto mode");
				}
			}
				
		});
		
		long epochTime = System.currentTimeMillis() / 1000;
		epoch.setText("epochTime is: " + epochTime);
		
		buttonGo = (Button)findViewById(R.id.buttonGo);
		buttonGo.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				sendOverride();
			}
		});

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
