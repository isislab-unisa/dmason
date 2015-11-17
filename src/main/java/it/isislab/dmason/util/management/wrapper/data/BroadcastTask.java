/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


package it.isislab.dmason.util.management.wrapper.data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TimerTask;

import com.google.gson.Gson;


public class BroadcastTask extends TimerTask
{

	private static final int PORT = 5555;
	private static final byte[] ADDRESS = new byte[] { (byte) 255,(byte) 255, (byte) 255, (byte) 255 };
	
	private String activeMQPort;
	private String activeMQIP;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private byte[] beacon;
	
	public BroadcastTask(String ip, String port) 
	{
		this.activeMQIP = ip;
		this.activeMQPort = port;
		
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        beacon = new byte[100];
        packet = new DatagramPacket(beacon, beacon.length);
        try {
			packet.setAddress(InetAddress.getByAddress(ADDRESS));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        packet.setPort(PORT);

        BeaconMessage message = new BeaconMessage(activeMQIP, activeMQPort);
        Gson json = new Gson();
        
        
        //message = "ActiveMQ beacon: {IP: "+activeMQIP+", PORT: "+activeMQPort+"}";
            
        beacon = json.toJson(message).getBytes();
        packet.setData(beacon);
            
	}


	@Override
	public void run() {
		System.out.println("Send beacon...");
		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
