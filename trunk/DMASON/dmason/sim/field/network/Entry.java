package dmason.sim.field.network;

import java.util.HashMap;

public class Entry {
	
	private HashMap<Integer,Integer> map;
	private NetworkUpdaterThreadForListener listener;
	
	public Entry(){
		this.listener=null;
		map=new HashMap<Integer, Integer>();
	}
	
	public void setThread(NetworkUpdaterThreadForListener listener){
		this.listener=listener;
	}

	public HashMap<Integer, Integer> getMap() {
		return map;
	}

	public NetworkUpdaterThreadForListener getListener() {
		return listener;
	}
	
	//-------------------------vecchia implentanzione---------------------------------------------------
	
	/*private int counter;
	private NetworkUpdaterThreadForListener listener;
	
	public Entry(){
		this.counter=1;
		this.listener=null;
	}
	
	public void addCounter(){
		this.counter++;
	}
	
	public void subCounter(){
		this.counter--;
	}
	
	public int getCounter(){
		return this.counter;
	}
	
	public NetworkUpdaterThreadForListener getListener(){
		return this.listener;
	}
	
	public void setCounter(int i){
		this.counter=i;
	}
	
	public void setListener(NetworkUpdaterThreadForListener l){
		this.listener=l;
	}*/

}
