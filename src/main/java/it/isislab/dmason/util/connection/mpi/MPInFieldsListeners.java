package it.isislab.dmason.util.connection.mpi;



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
				
				DistributedRegionInterface obj = (DistributedRegionInterface)bo.get(field.getID());
				
				field.getUpdates().put(obj.getStep(), obj);
				
				
			}
			
		}
		
	}


