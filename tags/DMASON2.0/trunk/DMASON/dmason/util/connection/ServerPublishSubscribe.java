package dmason.util.connection;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ada Mancuso
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
		System.out.println("Server in ascolto");
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
	




