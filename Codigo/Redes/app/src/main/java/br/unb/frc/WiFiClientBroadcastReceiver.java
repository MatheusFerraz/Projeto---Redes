package br.unb.frc;

import android.content.BroadcastReceiver;
import android.net.wifi.p2p.WifiP2pInfo;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class WiFiClientBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private ClientActivity activity;

    public WiFiClientBroadcastReceiver(WifiP2pManager manager, Channel channel,ClientActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    	activity.setClientStatus("Cliente broadcast criado.");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            	activity.setClientWifiStatus("Wifi Direct está habilitado.");
            } else {
            	activity.setClientWifiStatus("Wifi Direct não está habilitado.");
            }
            
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
        	
        	manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
				
				public void onPeersAvailable(WifiP2pDeviceList peers) {
					
					activity.displayPeers(peers);
					
				}
			});

        	
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        	
        	NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        	WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
        	WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        	
        	if(networkState.isConnected()) {
        		activity.setNetworkToReadyState(true, wifiInfo, device);
        		activity.setClientStatus("Status de conexão: Conectado.");
        	}
        	else {
        		activity.setTransferStatus(false);
        		activity.setClientStatus("Status de conexão: Desconectado.");
        		manager.cancelConnect(channel, null);
        	}

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

        }
    }
}