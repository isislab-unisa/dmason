package dmason.sim.field.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Mappa {
	
	private HashMap<String,Entry> mappa;
	
	public Mappa(){
		this.mappa=new HashMap<String, Entry>();
	}
	
	public boolean findKey(String key){
		return mappa.containsKey(key);
	}
	
	public boolean putKey(String key,Integer id){
		if (findKey(key))
			return false;
		Entry e=new Entry();
		e.getMap().put(id, 1);
		mappa.put(key, e);
		return true;
	}
	
	public boolean putDummyKey(String key,Integer id){
		if (findKey(key))
			return false;
		Entry e=new Entry();
		e.getMap().put(id, -1);
		mappa.put(key, e);
		return true;
	}
	
	public boolean findNode(String key, Integer id){
		if (!mappa.containsKey(key))
			return false;
		Entry e=mappa.get(key);
		if (e.getMap().containsKey(id))
			return true;
		return false;
	}
	
	public int getCounter(String key, Integer id){
		if (!mappa.containsKey(key))
			return -1;
		Entry e=mappa.get(key);
		if (!e.getMap().containsKey(id))
			return -1;
		return e.getMap().get(id);
	}
	
	public boolean addCounter(String key, Integer id){
		if (!mappa.containsKey(key))
			return false;
		Entry e=mappa.get(key);
		if (!e.getMap().containsKey(id))
			return false;
		Integer i=e.getMap().get(id);
		i=i+1;
		e.getMap().put(id, i);
		return true;
	}
	
	public boolean putNode(String key,Integer id){
		if (!findKey(key))
			return false;
		Entry e=mappa.get(key);
		e.getMap().put(id,1);
		return true;
	}
	
	public boolean putDummyNode(String key,Integer id){
		if (!findKey(key))
			return false;
		Entry e=mappa.get(key);
		e.getMap().put(id,-1);
		return true;
	}
	
	public Set<String> getKeySet(){
		return mappa.keySet();
	}
	
	public boolean addListener(String key, NetworkUpdaterThreadForListener l){
		if (!mappa.containsKey(key))
			return false;
		Entry e=mappa.get(key);
		e.setThread(l);
		return true;
	}
	
	public int size(){
		if (mappa.isEmpty())
			return 0;
		return mappa.keySet().size();
	}
	
	public boolean subCounter(String key, Integer id){
		if (!mappa.containsKey(key))
			return false;
		Entry e=mappa.get(key);
		if (!e.getMap().containsKey(id))
			return false;
		Integer i=e.getMap().get(id);
		i=i-1;
		e.getMap().put(id,i);
		return true;
	}
	
	public int counterSize(String key,Integer id){
		if (!mappa.containsKey(key))
			return -1;
		Entry e=mappa.get(key);
		if (!e.getMap().containsKey(id))
			return -1;
		int i=e.getMap().get(id);
		return i;
	}
	
	public NetworkUpdaterThreadForListener returnListener(String key){
		Entry e=mappa.get(key);
		return e.getListener();
	}
	
	public boolean removeNode(String key, Integer id){
		if (!mappa.containsKey(key))
			return  false;
		if (!mappa.get(key).getMap().containsKey(id))
			return false;
		mappa.get(key).getMap().remove(id);
		return true;
	}
	
	public int sizeForCell(String key){
		if (mappa.isEmpty())
			return 0;
		if (!mappa.containsKey(key))
			return 0;
		return mappa.get(key).getMap().keySet().size();
	}
	
	public boolean removeKey(String key){
		if (!mappa.containsKey(key))
			return false;
		mappa.remove(key);
		return true;
	}
	
	public boolean setCounter(String key,Integer id,int counter){
		if (!mappa.containsKey(key))
			return false;
		Entry e=mappa.get(key);
		if (!e.getMap().containsKey(id))
			return false;
		Integer i=e.getMap().get(id);
		i=counter;
		e.getMap().put(id,i);
		return true;
	}
	
	public Set<Integer> getNodeSet(String key){
		if (findKey(key)){
			return mappa.get(key).getMap().keySet();
		}
		return null;
	}
	
	public int getCounterByNode(int identificatore){
		Set<String> setKey=mappa.keySet();
		Iterator<String> i=setKey.iterator();
		String s;
		Entry e;
		Integer id;
		HashMap<Integer,Integer> m;
		while(i.hasNext()){
			s=i.next();
			e=mappa.get(s);
			m=e.getMap();
			Set<Integer> setNode=m.keySet();
			Iterator<Integer> iter=setNode.iterator();
			while (iter.hasNext()){
				id=iter.next();
				if (id==identificatore)
					return m.get(id);
			}
		}
		return -2;
	}
	
	public String keyByNode(int identificatore){
		Set<String> setKey=mappa.keySet();
		Iterator<String> i=setKey.iterator();
		String s;
		Entry e;
		Integer id;
		HashMap<Integer,Integer> m;
		while(i.hasNext()){
			s=i.next();
			e=mappa.get(s);
			m=e.getMap();
			Set<Integer> setNode=m.keySet();
			Iterator<Integer> iter=setNode.iterator();
			while (iter.hasNext()){
				id=iter.next();
				if (id==identificatore)
					return s;
			}
		}
		return null;
	}
	
	//metodo di debug per visualizzare il contenuto della hashMap
		public void stampa(){
			System.out.println("-----------------------------DebugHashMap------------------------------------------------");
			Set<String> setKey=mappa.keySet();
			Iterator<String> i=setKey.iterator();
			String s;
			Entry e;
			HashMap<Integer,Integer> m;
			NetworkUpdaterThreadForListener l;
			Integer id;
			while(i.hasNext()){
				s=i.next();
				System.out.println("Chiave di CellType: "+s);
				e=mappa.get(s);
				l=e.getListener();
				if (l!=null)
					System.out.println("il listener è: "+e.getListener());
				else System.out.println(" listener non settato...");
				m=e.getMap();
				Set<Integer> setNode=m.keySet();
				Iterator<Integer> iter=setNode.iterator();
				while (iter.hasNext()){
					id=iter.next();
					System.out.println("Id del nodo è: "+id);
					int counter=m.get(id);
					System.out.println("il counter associato è: "+counter);
				}
			}
			System.out.println("--------------------------NuovaHashMap FINE------------------------------------------------");
		}
		
	
}
