/**
 * Copyright 2016 Universita' degli Studi di Salerno


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


package it.isislab.dmason.util.connection.jms;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Observable;

import com.google.gson.Gson;
/**
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
public class BeaconMessageListener extends Observable implements Runnable 
{
	private static final int BEACON_PORT = 5555;
	
	String ip;
	String port;
	
	
	
	public BeaconMessageListener() {
		super();
		
	}

	@Override
	public void run() {
		byte[] receiveData = new byte[100];
		DatagramSocket clientSocket = null;
		try {
			clientSocket = new DatagramSocket(BEACON_PORT);
		} catch (SocketException e) {
			return;
		}
		try{
			if( clientSocket != null)
			{
				DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);

				clientSocket.receive(receivePacket);
				
				String data = getRidOfAnnoyingChar(receivePacket);
				System.out.println("From "+receivePacket.getAddress()+" received "+ data);
				
				Gson json = new Gson();
				
				BeaconMessage message = json.fromJson(data, BeaconMessage.class);
				
				//System.out.println("IP: "+message.getIp()+" Port: "+message.getPort());
				
				//ip.addItem(message.getIp());
				//port.addItem(message.getPort());
				ip = message.getIp();
				port = message.getPort();
				
				setChanged();
				notifyObservers("Beacon");
				
			}
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	private  String getRidOfAnnoyingChar(DatagramPacket packet){
        String result = new String(packet.getData());
        char[] annoyingchar = new char[1];
        char[] charresult = result.toCharArray();
        result = "";
        for(int i=0;i<charresult.length;i++){
            if(charresult[i]==annoyingchar[0]){
                break;
            }
            result+=charresult[i];
        }
        return result;
    }
}
