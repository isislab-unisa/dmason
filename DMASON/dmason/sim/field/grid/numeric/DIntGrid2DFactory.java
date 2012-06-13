package dmason.sim.field.grid.numeric;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.DistributedField;
import dmason.util.exception.DMasonException;
import sim.engine.SimState;
import sim.util.Int2D;

/**
 * A Factory class to create the right distribution field according to two parameters
 *  HORIZONTAL_DISTRIBUTION_MODE and SQUARE_DISTRIBUTION_MODE.
 */
public class DIntGrid2DFactory {

	public static final int HORIZONTAL_DISTRIBUTION_MODE=0;
	public static final int SQUARE_DISTRIBUTION_MODE=1;
	public static final int SQUARE_BALANCED_DISTRIBUTION_MODE=2;

	/**
	 * 
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param num_peers The number of peers
	 * @param MODE The mode of simulation (orizontal or squared)
	 * @return The right DSparseGrid2D
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DIntGrid2D createDIntGrid2D(int width, int height,SimState sm,int max_distance,int i,int j,int num_peers,int MODE, 
			int initialGridValue, boolean fixed, String name)
		throws DMasonException
	{
		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{
			if(width%num_peers == 0)
			{
				DIntGrid2D field = new DIntGrid2DY(width, height,sm, max_distance, i, j, num_peers, initialGridValue, name);
				
				if(!fixed)
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
				
				return (DIntGrid2D)field;
			}
			else
					throw new DMasonException("Illegal width dimension for NUM_PEERS:"+num_peers);
		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
				if((width% Math.sqrt(num_peers) == 0) && (height% Math.sqrt(num_peers) == 0))
				{
					
					DIntGrid2D field = new DIntGrid2DXY(width, height,sm, max_distance, i, j, num_peers, initialGridValue, name); 
					
					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);	
					
					return field;
				}
				else
					throw new DMasonException("Illegal width or height dimension for NUM_PEERS:"+num_peers);
			}
			else if (MODE==SQUARE_BALANCED_DISTRIBUTION_MODE){
				if(((width% Math.sqrt(num_peers) == 0) && (height% Math.sqrt(num_peers) == 0)) && 
						(((width/ Math.sqrt(num_peers))%3 == 0) && ((height/ Math.sqrt(num_peers))%3 == 0)))
				{
					
					DIntGrid2D field = new DIntGrid2DXYLB(width, height,sm, max_distance, i, j, num_peers, initialGridValue, name); 
					
					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField((DistributedField<Int2D>)field);	
					
					return field;
				}
				else
					throw new DMasonException("Illegal width or height dimension for NUM_PEERS:"+num_peers);
			}
			else 
			{
				throw new DMasonException("Illegal Distribution Mode");
			}
	}
}
