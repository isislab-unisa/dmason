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


/**
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *
 */
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.support.field2D.DistributedRegionInterface;
import it.isislab.dmason.util.connection.MyHashMap;

import java.util.ArrayList;
import java.util.List;

	public class MPInFieldsListeners<E> implements MPIMessageListener
	{
		List<DistributedField<E>> fields;
		public MPInFieldsListeners(ArrayList<DistributedField<E>> fields)
		{
			this.fields=fields;
		}
		@Override
		public void onMessage(Object message) throws Exception {
		
			
			MyHashMap bo = (MyHashMap)message;

			for (DistributedField field : fields) {
				
				DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getDistributedFieldID());
				
				field.getUpdates().put(obj.getStep(), obj);
				
				
			}
			
		}
		
	}


