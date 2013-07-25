package dmason.sim.field.network;

/*Classe di supporto usata insieme al grafo ausiliario*/

public class Node {
	
	private double x;
	private double y;
	private int  id;
	
	private NetworkUpdaterThreadForListener listener;
	
	public NetworkUpdaterThreadForListener getListener() {
		return listener;
	}

	public void setListener(NetworkUpdaterThreadForListener listener) {
		this.listener = listener;
	}

	private int num=1;
	
	public int getNum() {
		return num;
	}
	
	public void addNum(){
		num++;
	}
	
	public void decNum(){
		num--;
	}
	
	public void setNum(int i){
		num=i;
	}
	
	public Node(double x,double y,int  id){
		this.x=x;
		this.y=y;
		this.id=id;
		num=1;
		listener=null;
	}

	public double getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getId() {
		return id;
	}

	public void setId(int  id) {
		this.id = id;
	}
	
	/*public boolean equals(Object obj) {
		//if (obj instanceof Node ==false) return false;
		Node x=(Node)obj;
		System.out.println("STAMPA: "+this.id+" "+x.id);
		if  (this.id==x.id){
			System.out.println("vero");
			return true;
		}
		return false;
	}*/
	
	
}
