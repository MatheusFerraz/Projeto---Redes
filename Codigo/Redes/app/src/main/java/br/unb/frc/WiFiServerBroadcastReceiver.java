package br.unb.frc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class WiFiServerBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    public WiFiServerBroadcastReceiver(WifiP2pManager manager, Channel channel,MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
        
    	activity.setServerStatus("Servidor broadcast criado.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            	activity.setServerWifiStatus("Wifi Direct está habilitado.");
            } else {
            	activity.setServerWifiStatus("Wifi Direct não está habilitado.");
            }
            
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        	NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	
        	if(networkState.isConnected()) {
        		activity.setServerStatus("Status de conexão: Conectado.");
        	}
        	else {
        		activity.setServerStatus("Status de conexão: Desconectado.");
        		manager.cancelConnect(channel, null);

        	}
            
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}