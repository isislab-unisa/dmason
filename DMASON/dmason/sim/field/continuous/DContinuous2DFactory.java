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
	public static final int SQUARE_BALANCED_DISTRIBUTION_MODE=2;
	public static final int HORIZONTAL_BALANCED_DISTRIBUTION_MODE=3;


	/** 
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param num_peers The number of peers
	 * @param MODE The mode of simulation (horizontal or squared)
	 * @param mODE2 
	 * @return The right DSparseGrid2D
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DContinuous2D createDContinuous2D(double discretization,double width, double height,SimState sm,int max_distance,int i,int j,int rows,int columns, int MODE, String name, String topicPrefix)
		throws DMasonException
	{		
		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{
				DistributedField field = new DContinuous2DY(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix);	

				((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
				
				return (DContinuous2D)field;
			
		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
			
					DistributedField field = new DContinuous2DXY(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix);
					
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DContinuous2D)field;
				
			}
			else
				if(MODE==SQUARE_BALANCED_DISTRIBUTION_MODE)
				{
					int my_width=(int) (width/columns);
					int my_height=(int) (height/rows);
					int safezone = my_width /3;
					if(((width% columns == 0) && (height% rows == 0)) && 
							(((width/ columns)%3 == 0) && ((height/ rows)%3 == 0)) && max_distance < safezone/2 ){

						DistributedField field = new DContinuous2DXYLB(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix);
						
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
						
						return (DContinuous2D)field;
					}
					else
						throw new DMasonException("Illegal width or height dimension or MAXDISTANCE for NUM_PEERS:"+(rows*columns));
				}
				else if(MODE==HORIZONTAL_BALANCED_DISTRIBUTION_MODE)
				{

					DistributedField field = new DContinuous2DYLB(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix);	
					
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DContinuous2D)field;
				}
			else 
			{
				throw new DMasonException("Illegal Distribution Mode");
			}
	}
	
	
	public static final DContinuous2DThin createDContinuous2DThin(double discretization,double width, double height,SimState sm,int max_distance,int i,int j,int rows,int columns, int MODE, String name, String topicPrefix)
			throws DMasonException
		{
		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{
			double field_width,field_height;
		
				//upper left corner's coordinates
				
				// own width and height
				if(j<(width%columns))
					field_width=(int) Math.floor(width/columns+1)+4*max_distance;
				else
					field_width=(int) Math.floor(width/columns)+4*max_distance;
				field_height=height;
			
				DistributedField field = new DContinuous2DYThin(discretization,width, height,field_width,field_height,sm, max_distance, i, j, rows,columns,name,topicPrefix);	

				((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
				
				return (DContinuous2DYThin)field;
			
		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
				double field_width,field_height;

					
					// own width and height
					if(j<(width%columns))
						field_width=(int) Math.floor(width/columns+1)+4*max_distance;
					else
						field_width=(int) Math.floor(width/columns)+4*max_distance;
					
					if(i<(height%rows))
						field_height=(int) Math.floor(height/rows+1)+4*max_distance;
					else
						field_height=(int) Math.floor(height/rows)+4*max_distance;
				
				
					DistributedField field = new DContinuous2DXYThin(discretization,width, height,field_width,field_height,sm, max_distance, i, j, rows,columns,name,topicPrefix);
					
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
					
					return (DContinuous2DXYThin)field;
		}else 
		{
			throw new DMasonException("Illegal Distribution Mode");
		}
		
		}
}