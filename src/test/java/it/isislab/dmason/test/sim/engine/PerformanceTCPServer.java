package it.isislab.dmason.test.sim.engine;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class PerformanceTCPServer {
	 public static void main(String argv[]) throws Exception {
		  String sentence;
		  String modifiedSentence;
		  //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		  
		  for(int i =1;i<100;i++) {
			  Socket clientSocket = new Socket("172.16.15.82", 6666);
			  DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			  BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			  //sentence = inFromUser.readLine();
			  StringBuilder sb = new StringBuilder();
			  sb.append("test-75");
			  sb.append(",");
			  sb.append(i);
			  outToServer.writeBytes(sb.toString() + '\n');
			  modifiedSentence = inFromServer.readLine();
			  System.out.println("step"+ i +":  " + modifiedSentence);
			  clientSocket.close();
		  }
		  
		 }
}
