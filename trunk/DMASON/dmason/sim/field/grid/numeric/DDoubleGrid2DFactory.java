/**
 * Copyright 2012 Università degli Studi di Salerno


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

package dmason.sim.field.grid.numeric;

import sim.engine.SimState;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedMultiSchedule;
import dmason.sim.engine.DistributedState;
import dmason.sim.field.DistributedField;
import dmason.sim.field.grid.DSparseGrid2D;
import dmason.sim.field.grid.DSparseGrid2DYLB;
import dmason.util.exception.DMasonException;

/**
 * A Factory class to create the right distribution field according to two parameters
 *  HORIZONTAL_DISTRIBUTION_MODE and SQUARE_DISTRIBUTION_MODE.
 */
public class DDoubleGrid2DFactory {

	public static final int HORIZONTAL_DISTRIBUTION_MODE=0;
	public static final int SQUARE_DISTRIBUTION_MODE=1;
	public static final int SQUARE_BALANCED_DISTRIBUTION_MODE=2;
	public static final int HORIZONTAL_BALANCED_DISTRIBUTION_MODE=3;

	/**
	 * 
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param num_peers The number of peers
	 * @param MODE The mode of simulation (horizontal or squared)
	 * @param name is a value that we give at the topic, for the connection, for distinguish, updates from other region
	 * @param initialGridValue  is the value that we want to set at begin simulation for all the grid
	 * @return The right DDoubleGrid2D
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DDoubleGrid2D createDDoubleGrid2D(int width, int height,SimState sm,int max_distance,int i,int j,int rows, int columns,int MODE, 
			double initialGridValue, boolean fixed, String name, String topicPrefix)
		throws DMasonException
	{
		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{
			
					DistributedField field = new DDoubleGrid2DY(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix);

					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DDoubleGrid2D)field;
			
		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
					DistributedField field = new DDoubleGrid2DXY(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix);

					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					return (DDoubleGrid2D)field;
				
			}
			else if (MODE==SQUARE_BALANCED_DISTRIBUTION_MODE){
				if(((width% columns == 0) && (height% rows == 0)) && 
						(((width/ columns)%3 == 0) && ((height/ rows)%3 == 0)))
				{
					DistributedField field = new DDoubleGrid2DXYLB(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name, topicPrefix);
					
					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					return (DDoubleGrid2D)field;
				}
				else
					throw new DMasonException("Illegal width or height dimension for NUM_PEERS:"+(rows*columns));
			}
			else
				if(MODE==HORIZONTAL_BALANCED_DISTRIBUTION_MODE)
			{
					DistributedField field = new DDoubleGrid2DYLB(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name, topicPrefix);
					
					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DDoubleGrid2D)field;
						
			
			}
			else 
			{
				throw new DMasonException("Illegal Distribution Mode");
			}
	}
	public static final DDoubleGrid2DThin createDDoubleGrid2DThin(int width, int height,SimState sm,int max_distance,int i,int j,int rows, int columns,int MODE, 
			double initialGridValue, boolean fixed, String name, String topicPrefix)
		throws DMasonException
	{
		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{
			int field_width,field_height;
			
				//upper left corner's coordinates
				
				// own width and height
				if(j<(width%columns))
					field_width=(int) Math.floor(width/columns+1)+4*max_distance;
				else
					field_width=(int) Math.floor(width/columns)+4*max_distance;
				field_height=height;
			
					DistributedField field = new DDoubleGrid2DYThin(width, height,field_width,field_height,sm, max_distance, i, j, rows,columns, initialGridValue, name, topicPrefix);

					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DDoubleGrid2DThin)field;
			
		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
				int field_width,field_height;

					
					// own width and height
					if(j<(width%columns))
						field_width=(int) Math.floor(width/columns+1)+4*max_distance;
					else
						field_width=(int) Math.floor(width/columns)+4*max_distance;
					
					if(i<(height%rows))
						field_height=(int) Math.floor(height/rows+1)+4*max_distance;
					else
						field_height=(int) Math.floor(height/rows)+4*max_distance;
				
					DistributedField field = new DDoubleGrid2DXYThin(width, height,field_width,field_height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix);

					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					return (DDoubleGrid2DThin)field;
				
			}
			else 
			{
				throw new DMasonException("Illegal Distribution Mode");
			}
	}
}
