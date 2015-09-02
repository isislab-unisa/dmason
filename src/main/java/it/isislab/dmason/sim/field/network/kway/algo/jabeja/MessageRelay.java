package it.isislab.dmason.sim.field.network.kway.algo.jabeja;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
/**
 * @author martin
 *	
 * message relay using a multimap
 * 
 * NOTE this is the memory only solution fast but not scalable 
 *
 */

public class MessageRelay {
	
	private Multimap<Integer, Integer[]> msgStor;
	
	public MessageRelay() {
		Multimap<Integer, Integer[]> map = HashMultimap.create();
		msgStor = Multimaps.synchronizedMultimap(map);
	}
		
	//format of the msg [sender, receiver, msgtype, color, colordistribution]
	public void send(Integer to, Integer[] msg){
		msgStor.put(to, msg);
	}
	
	public Collection<Integer[]> get(int id){
		return msgStor.removeAll(id);
	}

}