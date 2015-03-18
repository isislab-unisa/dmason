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
package it.isislab.dmason.util.connection.socket;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class perform the actions of a typical server publish and subscribe.
 * It receives two type of command : "publish" and "subscribe",both contained in a 
 * wrapper message PubSubMessage,containing also other two fields, "topic's name" and (if there's a publish message)
 * a Object .
 * The class use an HashMap to contain a sockets' list of subscribers indexed by topic.
 * In our model relationship between subscribers and topic is 1-to-1, so to each topic is associated one and only one 
 * subscriber. I've used an HashMap for its efficiency but if someone has to change the model (more clients subscribe themeselves
 * to the same topic),it's necessary to use a different data structure,like an HashMap that associates more than one value to a key.
 * I use the the java.util.concurrent package because of each client is served by a different thread-managed ServerSocket.
 * So casual accesses to the data structure by threads in the same moment can generate a ConcurrentModificationException.
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
public class ServerPublishSubscribe{
	
	private ServerSocket server;
	public ConcurrentHashMap<String,Socket> addresses;
	
	public ServerPublishSubscribe() {
		super();
		try{
			addresses = new ConcurrentHashMap<String,Socket>();
			server = new ServerSocket(5555);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Socket work() throws IOException{
		System.out.println("Server listening...");
		return server.accept();
	}
	
public static void main(String[] args) throws IOException{
	ServerPublishSubscribe s = new ServerPublishSubscribe();
	while(true){
		commandLine cmd = new commandLine(s);
		cmd.start();
		}
	}
}	

class commandLine extends Thread{
		
		private ServerPublishSubscribe ser=null;
		private Socket socket=null;
		private ObjectInputStream in=null;
		private PubSubMessage  msg = null;
		
		public commandLine(ServerPublishSubscribe ser){
			super();
			this.ser = ser;
			try{
				this.socket = ser.work();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			
				try{  
					in = new ObjectInputStream(socket.getInputStream());
					while(true){
					msg = (PubSubMessage) in.readObject();
					String cmd = msg.getPart1();
					if(cmd.equalsIgnoreCase("publish")){
							if ( ser.addresses.containsKey(msg.getPart2()) ) {
								Socket sock = ser.addresses.get(msg.getPart2());
								ObjectOutputStream o = new ObjectOutputStream(sock.getOutputStream());
								o.writeObject(msg.getPart3());
								o.flush();
							}	
							}
					if(cmd.equals("subscribe")){
						ser.addresses.put(msg.getPart2(), socket);
					}
				}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	




