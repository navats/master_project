package com.example.obd;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.engine.ThrottlePositionObdCommand;
import pt.lighthouselabs.obd.commands.engine.MassAirFlowObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import android.R.bool;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class gaugeDisplay extends FragmentActivity{

	final Handler myHandler = new Handler();
	final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	MainActivity m;
	
	BluetoothAdapter btAdapter = null;
	BluetoothDevice device = null;
	BluetoothSocket socket;
	String deviceAddress;
	TextView rpm_tx, sp_tx, temp_tx, maf_tx, throt_tx,limit,limit_number;
	SeekBar setLimit;
	MediaPlayer mp;
	Button pause;
	int isPause;
	
	EngineRPMObdCommand engineRpmCommand = new EngineRPMObdCommand();
	SpeedObdCommand speedCommand = new SpeedObdCommand();
	EngineCoolantTemperatureObdCommand temp = new EngineCoolantTemperatureObdCommand();
	MassAirFlowObdCommand maf = new MassAirFlowObdCommand();
	ThrottlePositionObdCommand throttle = new ThrottlePositionObdCommand();
	
	Thread th;
	Timer timer;
	
	ImageView sign;
	
	
	final Runnable myRunnable = new Runnable() {
		
		
		   public void run() {
			
			
			   
				try {
		   			engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
		   		} catch (IOException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		} catch (InterruptedException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
				
				rpm_tx.setText(engineRpmCommand.getFormattedResult());
				
				try {
					speedCommand.run(socket.getInputStream(), socket.getOutputStream());
		   		} catch (IOException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		} catch (InterruptedException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
				
				
				sp_tx.setText(speedCommand.getFormattedResult());
				String []limit_arr = speedCommand.getFormattedResult().split("[km/h]");
				Log.d("speed",limit_arr[0]);
				double mileSpeed_double = Double.parseDouble(limit_arr[0]);
				mileSpeed_double *= 0.621371;
				int mileSpeed_int = (int)mileSpeed_double;
				String mileSpeed_txt = String.valueOf(mileSpeed_int);
				
				mileSpeed_txt += " MPH";
				
				sp_tx.setText(mileSpeed_txt);
				
				if(Integer.valueOf(limit_arr[0]) > setLimit.getProgress()){
					
					sign.setVisibility(View.VISIBLE);
					setLimit.setVisibility(View.INVISIBLE);
					limit_number.setText(String.valueOf(setLimit.getProgress()));
					limit_number.setVisibility(View.VISIBLE);
					
					mp = MediaPlayer.create(gaugeDisplay.this, R.raw.beep);
					mp.start();
					mp.setOnCompletionListener(new OnCompletionListener() {
					    public void onCompletion(MediaPlayer mp) {
					        mp.release();

					    };
					});
					
				}
				else{
					
				
					
					sign.setVisibility(View.INVISIBLE);
					setLimit.setVisibility(View.VISIBLE);
					limit_number.setVisibility(View.INVISIBLE);
				}
				
				try {
					temp.run(socket.getInputStream(), socket.getOutputStream());
		   		} catch (IOException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		} catch (InterruptedException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
				
				
				temp_tx.setText(temp.getFormattedResult());
				

				try {
					maf.run(socket.getInputStream(), socket.getOutputStream());
		   		} catch (IOException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		} catch (InterruptedException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
				
				maf_tx.setText(maf.getFormattedResult());
				

				try {
					throttle.run(socket.getInputStream(), socket.getOutputStream());
		   		} catch (IOException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		} catch (InterruptedException e) {
		   			// TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
				
				throt_tx.setText(throttle.getFormattedResult());
				
				
		   }
		};

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//	// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}
//		
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle item selection
//	    switch (item.getItemId()) {
//	        case R.id.action_settings:
//	        	Toast.makeText(getApplicationContext(), "Setting clicked",Toast.LENGTH_LONG).show();
//	        	sign = (ImageView)findViewById(R.id.limitsign);
//	        	sign.setVisibility(1);
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}
//	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Log.d("OncreateGauge", "entered");
		
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gauges);

        rpm_tx = (TextView)findViewById(R.id.rpm_value);
        sp_tx = (TextView)findViewById(R.id.speed_value);
        temp_tx = (TextView)findViewById(R.id.coolant_value);
        maf_tx = (TextView)findViewById(R.id.maf_value);
        throt_tx = (TextView)findViewById(R.id.throt_value);
        limit = (TextView)findViewById(R.id.limit);
        limit_number = (TextView)findViewById(R.id.limit_number);
        setLimit = (SeekBar)findViewById(R.id.seekBar);
        sign = (ImageView)findViewById(R.id.limitsign);
        sign.setVisibility(View.INVISIBLE);
        pause = (Button)findViewById(R.id.bt_pause);
        isPause = 0;
        
        limit.setText(Integer.toString(setLimit.getProgress()));
        setLimit.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       

            @Override       
            public void onStopTrackingTouch(SeekBar seekBar) {      
                // TODO Auto-generated method stub      
            }       

            @Override       
            public void onStartTrackingTouch(SeekBar seekBar) {     
                // TODO Auto-generated method stub      
            }       

            @Override       
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {     
                // TODO Auto-generated method stub      

                limit.setText(Integer.toString(progress));

            }       
        });             
        
        deviceAddress = getIntent().getExtras().getString("MAC");




        Log.d("gaugeDisplay_Init", "OK");

        Log.d("deviceAddress ", deviceAddress);



		if(deviceAddress != null){

			Log.d("MAC","NOT null");

			btAdapter = BluetoothAdapter.getDefaultAdapter();
			Log.d("BTadaprt", "OK");

			device = btAdapter.getRemoteDevice(deviceAddress);
			Log.d("getRemoteDevice", "OK");

			try {
				socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
				Log.d("createInsecureSocket", "OK");

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				socket.connect();
				Log.d("Socket_Con","OK");
				
				if(socket.isConnected())
				{

					Toast.makeText(getApplicationContext(), "Socket created",Toast.LENGTH_LONG).show();
				}
				else{
					Toast.makeText(getApplicationContext(), "Warning Socket not openned",Toast.LENGTH_LONG).show();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String simMode = getIntent().getExtras().getString("simMode");
			
			if(simMode.equals("1")){
				Log.d("simMode", "ON");
				obdInit();
			}
			else{
				Log.d("simMode", "OFF");
			}
			
			start();
			
		}
		else{
			Log.d("DEVEICE_MAC","NULL");

		}
	}// end onCreate
	
	
	@Override
	public void onBackPressed() {
		this.timer.cancel();
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		finish();
	}

	public void start(){
		
		timer = new Timer();
	      timer.schedule(new TimerTask() {
	         @Override
	         public void run() {myHandler.post(myRunnable);}
	      }, 0, 1000);
	      
		
		
	}//end start

	public void stop(){
		
		this.timer.cancel();
		
	}

	public void obdInit(){

		try {
			new EchoOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			new LineFeedOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//new TimeoutObdCommand().run(socket.getInputStream(), socket.getOutputStream());

		try {
			new SelectProtocolObdCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void pauseScan(View v){
		if(isPause == 0){
			stop();
			isPause = 1;
			Toast.makeText(getApplicationContext(), "Pause",Toast.LENGTH_LONG).show();
		}
		else{
			start();
			isPause = 0;
			Toast.makeText(getApplicationContext(), "Resume",Toast.LENGTH_LONG).show();
		}

	}
	
}
