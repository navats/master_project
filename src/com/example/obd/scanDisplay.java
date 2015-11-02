package com.example.obd;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


import pt.lighthouselabs.obd.commands.control.DtcNumberObdCommand;
import pt.lighthouselabs.obd.commands.control.TroubleCodesObdCommand;
import pt.lighthouselabs.obd.commands.protocol.EchoOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.LineFeedOffObdCommand;
import pt.lighthouselabs.obd.commands.protocol.SelectProtocolObdCommand;
import pt.lighthouselabs.obd.enums.ObdProtocols;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class scanDisplay extends FragmentActivity{

	final Handler myHandler = new Handler();
	final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	BluetoothAdapter btAdapter = null;
	BluetoothDevice device = null;
	BluetoothSocket socket;
	String deviceAddress;
	
	TroubleCodesObdCommand troubleCodesCommand = new TroubleCodesObdCommand(0);
	DtcNumberObdCommand dtcnum = new DtcNumberObdCommand();
	TextView dtcs;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {


		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.scanner);
        
        
        
        dtcs = (TextView)findViewById(R.id.dtcs);
        
        deviceAddress = getIntent().getExtras().getString("MAC");
        Log.d("scanDisplay_Init", "OK");

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
	}
	
public void start(){
	
	int start = 2;
	int end = 6;
	String Raw;
	String code = "";
	
	try {
		dtcnum.run(socket.getInputStream(), socket.getOutputStream());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	//Log.d("DTCNUM", String.valueOf(dtcnum.getTotalAvailableCodes()));
	
	if(dtcnum.getTotalAvailableCodes() > 0){
			try {
					troubleCodesCommand.run(socket.getInputStream(), socket.getOutputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			
			Raw = troubleCodesCommand.getFormattedResult();
			Log.d("RAW_CODE", Raw);
			
			if(!Raw.equals(null)){
				
				while(!(start+1 >= Raw.length()) && !Raw.substring(start, end).equals("0000")){
					
					Log.d("RawSub", Raw.substring(start, end));
					int pid = Integer.parseInt(Raw.substring(start, end));
					Log.d("PID", String.valueOf(pid));
//					code += "P" + Raw.substring(start, end) + " "+ getCodeName(pid) + "\n";
					code += "P" + Raw.substring(start, end) + "\n";
					start += 4;
					end += 4;
					
				}
				
		//		code += "P" + Raw.substring(start, end);
				
				dtcs.setText(code);
				
				Log.d("RAW", Raw);
			}
			else{
				dtcs.setText("No DTC found.");
			}
	}
	else{
		dtcs.setText("No DTC found.");
	}	
		
	}//end start

	
	
	@Override
	public void onBackPressed() {
		
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finish();
	}

	public void obdInit(){

		try {
			new EchoOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());
			Log.d("EchoOFF", "OK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			new LineFeedOffObdCommand().run(socket.getInputStream(), socket.getOutputStream());
			Log.d("LineFeedOFF", "OK");
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
			Log.d("SelectProtocol", "OK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			socket.getOutputStream();
			Log.d("GetOutput", "OK");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String getCodeName(int pid){
		switch(pid){
		case 103:
			return "MAF Circuit High Input";
		case 300:
			return "Cylinder Misfire Detected";
		default:
			return "";
				
				
			
		}
	}
}
