/**
 * Copyright 2016 Universita' degli Studi di Salerno


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