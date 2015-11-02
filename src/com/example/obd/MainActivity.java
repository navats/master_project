package com.example.obd;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import pt.lighthouselabs.obd.commands.SpeedObdCommand;
import pt.lighthouselabs.obd.commands.engine.EngineRPMObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.commands.protocol.TimeoutObdCommand;
import pt.lighthouselabs.obd.commands.temperature.AirIntakeTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import pt.lighthouselabs.obd.commands.temperature.TemperatureObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	
	Boolean con_status = false; // !!!!!! create database variable
	ImageView conLight;
	TextView mactext;
	
	String deviceAddress = "00:00:00:00:00:00";
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		setContentView(R.layout.activity_main);
		
		conLight = (ImageView)findViewById(R.id.imageView1);
		mactext = (TextView)findViewById(R.id.text_status);
		
		
		
	}// end onCreate
	
	public void connectionStatus(View v){
		
		
		
		if(!con_status){
			
			//Toast.makeText(getApplicationContext(), "Connected",Toast.LENGTH_LONG).show();
						
			getDeviceAddress();
			
			//Toast.makeText(getApplicationContext(), deviceAddress,Toast.LENGTH_LONG).show();
			Log.d("ADDRESS:", deviceAddress);
			
			if(deviceAddress != "00:00:00:00:00:00"){
			conLight.setImageResource(R.drawable.blue_link);
			con_status = true;
			mactext.setText(deviceAddress);
			}
			
		}
		else{
			
			//Thread.interrupted();
			
			mactext.setText("");
			Toast.makeText(getApplicationContext(), "Socket closed",Toast.LENGTH_LONG).show();
			conLight.setImageResource(R.drawable.red_link);
			//Toast.makeText(getApplicationContext(), "Disconnected",Toast.LENGTH_LONG).show();
			con_status = false;
			Log.d("STATUS", "set to false");
		}
	
		
		
	} // end connectionStatus
	
	
	
	//getDeviceList: Show a list of available BT device, return selected device's MAC address
	public void getDeviceAddress(){
		
		ArrayList deviceStrs = new ArrayList();
	    final ArrayList devices = new ArrayList();
	    //String Address;
	    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	    Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
	     
	    if (pairedDevices.size() > 0)
	    {
	           for (BluetoothDevice device : pairedDevices)
	           {
	        	   deviceStrs.add(device.getName() + "\n" + device.getAddress());
	        	   devices.add(device.getAddress());
	            }
	     }
	            
	         // show list
	            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

	            @SuppressWarnings({ "unchecked", "rawtypes" })
				ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[deviceStrs.size()]));

	            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener()
	            {
	                	@Override
	                	public void onClick(DialogInterface dialog, int which)
	                	{
	                		
	                		int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
	                		deviceAddress = (String) devices.get(position);
	                		dialog.dismiss();
	                		// TODO save deviceAddress
	                	}
	             });

	            alertDialog.setTitle("Choose Bluetooth device");
	            alertDialog.show();
	            
	            
	}
	
	public void showScan(View v){
		
		Log.d("ShowSacn", "entered");
		
		CheckBox sim = (CheckBox)findViewById(R.id.cb1);
		String simMode = "0";
		if(sim.isChecked()){
			simMode = "1";
		}
		
		Log.d("SimMode", simMode);
		
		if(con_status){
	        Intent j = new Intent(MainActivity.this, scanDisplay.class);
	        j.putExtra("MAC",deviceAddress);
	        j.putExtra("simMode", simMode);
	        startActivity(j);
		}
		else{
			Toast.makeText(getApplicationContext(), "Please connect OBD2 device",Toast.LENGTH_LONG).show();
		}
}
	public void showMap(View v){
				
		
		
		Intent j = new Intent(MainActivity.this, mapDisplay.class);
		startActivity(j);
	}

	public void showGauges(View v){
		
		Log.d("ShowGauages", "entered");
		
		if(con_status){
			CheckBox sim = (CheckBox)findViewById(R.id.cb1);
			String simMode = "0";
			if(sim.isChecked()){
				simMode = "1";
			}
			
			Log.d("SimMode", simMode);
			
	        Intent j = new Intent(MainActivity.this, gaugeDisplay.class);
	        j.putExtra("MAC",deviceAddress);
	        j.putExtra("simMode", simMode);
	        startActivity(j);
		}
		else{
			Toast.makeText(getApplicationContext(), "Please connect OBD2 device",Toast.LENGTH_LONG).show();
		}
	}
	
	public void speak(View view) {
		  Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		  // Specify the calling package to identify your application
		  intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

		  // Given an hint to the recognizer about what the user is going to say
		  //There are two form of language model available
		  //1.LANGUAGE_MODEL_WEB_SEARCH : For short phrases
		  //2.LANGUAGE_MODEL_FREE_FORM  : If not sure about the words or phrases and its domain.
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		  // If number of Matches is not selected then return show toast message
//		  if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
//		   Toast.makeText(this, "Please select No. of Matches from spinner",
//		     Toast.LENGTH_SHORT).show();
//		   return;
//		  }

		  int noOfMatches = 1;
		 
		  // Specify how many results you want to receive. The results will be
		  // sorted where the first result is the one with higher confidence.
		  intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);
		  //Start the Voice recognizer activity for the result.
		  startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
		 }
	
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)

	   //If Voice recognition is successful then it returns RESULT_OK
	   if(resultCode == RESULT_OK) {

	    ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

	    if (!textMatchList.isEmpty()) {
		     // If first Match contains the 'search' word
		     // Then start web search.
		     if (textMatchList.get(0).contains("search")) {
	
		        String searchQuery = textMatchList.get(0);
		        searchQuery = searchQuery.replace("search","");
		        Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
		        search.putExtra(SearchManager.QUERY, searchQuery);
		        startActivity(search);
	     } else {
	         
	    	 //Toast.makeText(getApplicationContext(), textMatchList.get(0),Toast.LENGTH_LONG).show();
	    	 if(textMatchList.get(0).equals("show the map")){
	    		 Toast.makeText(getApplicationContext(), "Opening the map ...",Toast.LENGTH_LONG).show();
	    		 Button bt_map = (Button) findViewById(R.id.map);
	    		 bt_map.performClick();
	    	 }
	    	 else if (textMatchList.get(0).equals("show data")){
	    		 Toast.makeText(getApplicationContext(), "Retriving ECU data ...",Toast.LENGTH_LONG).show();
	    		 Button bt_gauge = (Button) findViewById(R.id.gauge);
	    		 bt_gauge.performClick();
	    	 }
	    	 else if(textMatchList.get(0).equals("scan")){
	    		 Toast.makeText(getApplicationContext(), "Scanning DTC code ...",Toast.LENGTH_LONG).show();
	    		 Button bt_scan = (Button) findViewById(R.id.scan);
	    		 bt_scan.performClick();
	    	 }
	    	 else{
	    		 Log.d("command", textMatchList.get(0));
	    		 Toast.makeText(getApplicationContext(), "Command not found",Toast.LENGTH_LONG).show();
	    	 }
	     }

	    }
	   //Result code for various error.
	   }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
		Toast.makeText(getApplicationContext(), "Audio Error",Toast.LENGTH_LONG).show();
	   }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
		   Toast.makeText(getApplicationContext(), "Client Error",Toast.LENGTH_LONG).show();
	   }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
		   Toast.makeText(getApplicationContext(), "Network Error",Toast.LENGTH_LONG).show();
	   }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
		   Toast.makeText(getApplicationContext(), "Command not found",Toast.LENGTH_LONG).show();
	   }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
		   Toast.makeText(getApplicationContext(), "Server Error",Toast.LENGTH_LONG).show();
	   }
	  super.onActivityResult(requestCode, resultCode, data);
	 }
}// end Main Activity
