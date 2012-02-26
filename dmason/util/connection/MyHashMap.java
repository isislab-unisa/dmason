package dmason.util.connection;

import java.io.Serializable;
import java.util.HashMap;

public class MyHashMap extends HashMap<String, Object> implements Serializable{
	
	private int full;
	public int NUMBER_FIELDS;
	
	public MyHashMap(int numFields){
		NUMBER_FIELDS = numFields;
		full = numFields;
	}
	
	public Object put(String key, Object value){
		
		Object obj = super.put(key, value);
		full--;
		return obj;
	}
	
	public boolean isFull(){
		
		return full==0;
	}
	
	public int getFull(){
		
		return full;
	}
}