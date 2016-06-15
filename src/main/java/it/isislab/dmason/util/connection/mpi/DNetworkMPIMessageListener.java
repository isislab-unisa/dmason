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

package it.isislab.dmason.util.connection.mpi;
import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.DistributedFieldNetwork;
import it.isislab.dmason.sim.field.support.network.DNetworkRegion;
import it.isislab.dmason.util.connection.MyHashMap;

import java.util.ArrayList;
/**
 *	A Listener for the messages swapped among the peers.
 */
//@param <E> the type of coordinates
//@param <F> the type of locations
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class DNetworkMPIMessageListener implements MPIMessageListener
{	


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String topic;
	public ArrayList<DistributedFieldNetwork> fields;


    /**
     * Constructor 
     * @param fields the fields 
     * @param topic name of topic
     */
	public DNetworkMPIMessageListener(ArrayList<DistributedFieldNetwork> fields,String topic) 
	{
		super();

		this.fields = fields;

		this.topic=topic;

	}


    /**
     * Return the topic name
     * @return the topic name
     */
	public String getTopic(){
		return topic;
	}

	/**
	 *	It's called when a message is listen 
	 */
	@Override
	public void onMessage(Object message) throws Exception {
		try
		{

			MyHashMap bo = (MyHashMap)message;

			for (DistributedFieldNetwork field : fields) {	
				DNetworkRegion obj = (DNetworkRegion)bo.get(field.getDistributedFieldID());
				field.getNetworkUpdates().put(obj.getStep(), obj);

			}

		} catch (Exception e) { 

			e.printStackTrace(); 
		}				

	}

}