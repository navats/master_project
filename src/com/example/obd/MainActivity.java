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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	
	Boolean con_status = false; // !!!!!! create database variable
	ImageView conLight;
	TextView mactext;
	String deviceAddress = "00:00:00:00:00:00";
	
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
		if(con_status){
	        Intent j = new Intent(MainActivity.this, scanDisplay.class);
	        j.putExtra("MAC",deviceAddress);
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
		
		 
		
		if(con_status){

	        Intent j = new Intent(MainActivity.this, gaugeDisplay.class);
	        j.putExtra("MAC",deviceAddress);
	        startActivity(j);
		}
		else{
			Toast.makeText(getApplicationContext(), "Please connect OBD2 device",Toast.LENGTH_LONG).show();
		}
	}
	
	
}// end Main Activity
