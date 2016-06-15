package br.unb.frc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;

import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;

public class ClientService extends IntentService {

	private boolean serviceEnabled;
	
	private int port;
	private File fileToSend;
	private ResultReceiver clientResult;
	private WifiP2pDevice targetDevice;
	private WifiP2pInfo wifiInfo;
	
	public ClientService() {
		super("ClientService");
		serviceEnabled = true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		port = ((Integer) intent.getExtras().get("port")).intValue();	
		fileToSend = (File) intent.getExtras().get("fileToSend");
		clientResult = (ResultReceiver) intent.getExtras().get("clientResult");
		wifiInfo = (WifiP2pInfo) intent.getExtras().get("wifiInfo");	
		
		if(!wifiInfo.isGroupOwner) {
			InetAddress targetIP = wifiInfo.groupOwnerAddress;
			
			Socket clientSocket = null;
			OutputStream os = null;
			 
			try {
				clientSocket = new Socket(targetIP, port);
				os = clientSocket.getOutputStream();
				PrintWriter pw = new PrintWriter(os);
				
				InputStream is = clientSocket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);			
				
				signalActivity("Início - handshake.");
				
			    byte[] buffer = new byte[4096];
			    
			    FileInputStream fis = new FileInputStream(fileToSend);
			    BufferedInputStream bis = new BufferedInputStream(fis);
			    			   			    			  		    
			    while(true) {
				    int bytesRead = bis.read(buffer, 0, buffer.length);
				    
				    if(bytesRead == -1) {
				    	break;
				    }

				    os.write(buffer,0, bytesRead);
				    os.flush();			    
			    }
			    
			    fis.close();
			    bis.close();
			    
			    br.close();
			    isr.close();
			    is.close();
			    
			    pw.close();		    
			    os.close();
			    			   			    
			    clientSocket.close();
			    			    
			    signalActivity("Transferência de arquivo completa, arquivo enviado: " + fileToSend.getName());
			} catch (IOException e) {
				signalActivity(e.getMessage());
			}
			catch(Exception e) {
				signalActivity(e.getMessage());
			}
		}
		else {
			signalActivity("Este dispositivo é dono do grupo, portanto o endereço de IP do " +
					"dispositivo target não foi determinado. Transferência não pode continuar.");
		}

		clientResult.send(port, null);
	}
	

	public void signalActivity(String message) {
		Bundle b = new Bundle();
		b.putString("message", message);
		clientResult.send(port, b);
	}
	
	
	public void onDestroy() {
		serviceEnabled = false;
		stopSelf();
	}
}