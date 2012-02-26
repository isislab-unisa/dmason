package dmason.util.garbagecollector;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.util.Scanner;

public class Start {

	private String start="start\n";
	private String stop="stop\n";
	private String restart="restart\n";
	private Socket socket;
	private BufferedOutputStream out;
	private BufferedInputStream in;
	private Scanner console;
	private boolean r = false;
	
	public Start() {}
	
	public boolean connect(String ip,String port){
		try{
			socket = new Socket(ip,Integer.parseInt(port));
			in = new BufferedInputStream(socket.getInputStream());
			console = new Scanner(in);
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
			}
	}
	
	public boolean isConnected()
	{
		if(socket == null)
			return false;
		return socket.isConnected();
		
	}
	
	public void execute(String cmd){
		try{
			out = new BufferedOutputStream(socket.getOutputStream());
			if(cmd.equals("start")){
				out.write(start.getBytes());
				out.flush();
			}
			if(cmd.equals("stop")){
				out.write(stop.getBytes());
				out.flush();
			}
			if(cmd.equals("restart")){
				r = true;
				out.write(restart.getBytes());
				out.flush();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String receive(){
		return console.nextLine();
	}
	
	public static void main(String[] args){
		Start s = new Start();
		s.connect("127.0.0.1","3333");
		Scanner input = new Scanner(System.in);
		String x="";
		while(input.hasNext())
		{
			x = input.nextLine();
			s.execute(x);
			System.out.println(s.receive());
		}
	}
}
