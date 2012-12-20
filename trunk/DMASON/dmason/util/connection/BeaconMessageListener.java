package dmason.util.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JComboBox;

import com.google.gson.Gson;

public class BeaconMessageListener extends Thread 
{
	private static final int BEACON_PORT = 5555;
	
	JComboBox ip;
	JComboBox port;
	
	
	
	public BeaconMessageListener(JComboBox ip, JComboBox port) {
		super();
		this.ip = ip;
		this.port = port;
	}



	@Override
	public void run() {
		byte[] receiveData = new byte[100];
		DatagramSocket clientSocket;
		try {
			clientSocket = new DatagramSocket(BEACON_PORT);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,receiveData.length);

			clientSocket.receive(receivePacket);
			
			String data = getRidOfAnnoyingChar(receivePacket);
			System.out.println("From "+receivePacket.getAddress()+" received "+ data);
			
			Gson json = new Gson();
			
			BeaconMessage message = json.fromJson(data, BeaconMessage.class);
			
			System.out.println("IP: "+message.getIp()+" Port: "+message.getPort());
			
			ip.addItem(message.getIp());
			port.addItem(message.getPort());
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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
