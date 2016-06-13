package br.unb.frc;

import java.io.File;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import edu.pdx.cs410.wifi.direct.file.frc.R;

public class MainActivity extends Activity {
	public final int fileRequestID = 55;
	public final int port = 7950;
	
	private WifiP2pManager wifiManager;
	private Channel wifichannel;
	private BroadcastReceiver wifiServerReceiver;

	private IntentFilter wifiServerReceiverIntentFilter;
	
	private String path;
	private File downloadTarget;
	
	private Intent serverServiceIntent; 
	
	private boolean serverThreadActive;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
               
        wifiManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        wifichannel = wifiManager.initialize(this, getMainLooper(), null);
        wifiServerReceiver = new WiFiServerBroadcastReceiver(wifiManager, wifichannel, this);
              
        wifiServerReceiverIntentFilter = new IntentFilter();;
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiServerReceiverIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        

    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
    	serverServiceStatus.setText(R.string.server_stopped);
        
    	path = "/";
    	downloadTarget = new File(path);
    	
    	serverServiceIntent = null; 
    	serverThreadActive = false;
    	
    	setServerFileTransferStatus("Nenhum arquivo sendo transferido.");
    	
        registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
       
    public void startFileBrowseActivity(View view) {
        Intent clientStartIntent = new Intent(this, FileBrowser.class);
        startActivityForResult(clientStartIntent, fileRequestID);
    }
      
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
    		File targetDir = (File) data.getExtras().get("file");
    		
    		if(targetDir.isDirectory()) {
    			if(targetDir.canWrite()) {
    				downloadTarget = targetDir;
	    	    	TextView filePath = (TextView) findViewById(R.id.server_file_path);
	    	    	filePath.setText(targetDir.getPath());
	    			setServerFileTransferStatus("Diretório de download " + targetDir.getName());
    			}
    			else {
	    			setServerFileTransferStatus("Você não possui permissão para escrever " + targetDir.getName());
    			}
    		}
    		else {
    			setServerFileTransferStatus("Não foi selecionado um diretório válido. Favor, selecionar um diretório válido.");
    		}
        }
    }
    
    public void startServer(View view) {
    	if(!serverThreadActive) {
	
	    	serverServiceIntent = new Intent(this, ServerService.class);
	    	serverServiceIntent.putExtra("saveLocation", downloadTarget);
	    	serverServiceIntent.putExtra("port", new Integer(port));
	    	serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
	    	    @Override
	    	    protected void onReceiveResult(int resultCode, final Bundle resultData) {
	    	    	
	    	    	if(resultCode == port ) {
		    	        if (resultData == null) {
		    	        	serverThreadActive = false;
		    	        			    	        	
		    	        	final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
		    	        	server_status_text.post(new Runnable() {
		    	                public void run() {
				    	        	server_status_text.setText(R.string.server_stopped);
		    	                }
		    	        	});
		    	        }
		    	        else {
		    	        	final TextView server_file_status_text = (TextView) findViewById(R.id.server_file_transfer_status);

		    	        	server_file_status_text.post(new Runnable() {
		    	                public void run() {
		    	                	server_file_status_text.setText((String)resultData.get("message"));
		    	                }
		    	        	});		    	   		    	        	
		    	        }
	    	    	}
	    	    }
	    	});
	    		    		
	    	serverThreadActive = true;
	        startService(serverServiceIntent);

	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
	    	serverServiceStatus.setText(R.string.server_running);
	    	
	    }
    	else {
	    	TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
	    	serverServiceStatus.setText("Servidor já está rodando.");
    	}
    }
    
    public void stopServer(View view) {
    	if(serverServiceIntent != null) {
    		stopService(serverServiceIntent);
    	}
    }
    
    public void startClientActivity(View view) {
    	stopServer(null);
        Intent clientStartIntent = new Intent(this, ClientActivity.class);
        startActivity(clientStartIntent);    		
    }   
    
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        stopServer(null);

		try {
			unregisterReceiver(wifiServerReceiver);
		} catch (IllegalArgumentException e) {

		}      
    }

    public void setServerWifiStatus(String message) {
    	TextView server_wifi_status_text = (TextView) findViewById(R.id.server_wifi_status_text);
    	server_wifi_status_text.setText(message);	
    }
    
    public void setServerStatus(String message) {
    	TextView server_status_text = (TextView) findViewById(R.id.server_status_text_2);
    	server_status_text.setText(message);	
    }
    
    public void setServerFileTransferStatus(String message) {
    	TextView server_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
    	server_status_text.setText(message);	
    }
}
