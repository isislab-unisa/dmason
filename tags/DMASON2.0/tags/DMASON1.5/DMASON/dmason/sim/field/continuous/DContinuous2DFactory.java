package dmason.sim.field.continuous;

import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.DistributedField;
import dmason.util.exception.DMasonException;
import sim.engine.SimState;


/**
*  A Factory class to create the right distribution field according to two parameters
*  HORIZONTAL_DISTRIBUTION_MODE and SQUARE_DISTRIBUTION_MODE.
*/
public final class DContinuous2DFactory 
{	
	public static final int HORIZONTAL_DISTRIBUTION_MODE=0;
	public static final int SQUARE_DISTRIBUTION_MODE=1;
	
	/** 
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param num_peers The number of peers
	 * @param MODE The mode of simulation (horizontal or squared)
	 * @return The right DSparseGrid2D
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DContinuous2D createDContinuous2D(double discretization,double width, double height,SimState sm,int max_distance,int i,int j,int num_peers,int MODE,String name)
		throws DMasonException
	{		
		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{
			int my_width=(int) (width/num_peers);
			if(width%num_peers == 0 && (max_distance < my_width/2 )){
				DistributedField field = new DContinuous2DY(discretization,width, height,sm, max_distance, i, j, num_peers,name);	
				
				((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
				
				return (DContinuous2D)field;
			}
			else
					throw new DMasonException("Illegal width dimension for NUM_PEERS:"+num_peers);
		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
				int my_width=(int) (width/Math.sqrt(num_peers));
				int my_height=(int) (height/Math.sqrt(num_peers));
				if((width% Math.sqrt(num_peers) == 0) && (height% Math.sqrt(num_peers) == 0) && (max_distance < my_width/2) && (max_distance < my_height)){

					DistributedField field = new DContinuous2DXY(discretization,width, height,sm, max_distance, i, j, num_peers,name);
					
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DContinuous2D)field;
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