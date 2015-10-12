/**
 * Copyright 2012 Universita' degli Studi di Salerno


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

package it.isislab.dmason.sim.field.grid.numeric;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.loadbalanced.DDoubleGrid2DXYLB;
import it.isislab.dmason.sim.field.grid.numeric.loadbalanced.DDoubleGrid2DYLB;
import it.isislab.dmason.sim.field.grid.numeric.thin.DDoubleGrid2DThin;
import it.isislab.dmason.sim.field.grid.numeric.thin.DDoubleGrid2DXYThin;
import it.isislab.dmason.sim.field.grid.numeric.thin.DDoubleGrid2DYThin;
import sim.engine.SimState;

/**
 *  A Factory class to create the right distribution field according to four parameters
 *  HORIZONTAL_DISTRIBUTION_MODE, SQUARE_DISTRIBUTION_MODE, SQUARE_BALANCED_DISTRIBUTION_MODE and HORIZONTAL_BALANCED_DISTRIBUTION_MODE.
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
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
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param MODE The mode of simulation (horizontal or squared, balanced or not)
	 * @param initialGridValue Starting value of the matrix
	 * @param fixed If it's true the field is read-only
	 * @param name ID of a region
	 * @param topicPrefix Prefix for the name of topics used only in Batch mode
	 * @return The right DSparseGrid2D
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DDoubleGrid2D createDDoubleGrid2D(int width, int height,SimState sm,int max_distance,int i,int j,int rows, int columns,int MODE, 
			double initialGridValue, boolean fixed, String name, String topicPrefix,boolean isToroidal)
					throws DMasonException
	{
		if(width <=0 ) throw new DMasonException("Width cannot be less than zero");
		if(height <=0 ) throw new DMasonException("Height cannot be less than zero");
		if(width >=Integer.MAX_VALUE ) throw new DMasonException("Width cannot exceed the maximum integer value");
		if(height >=Integer.MAX_VALUE ) throw new DMasonException("Height cannot exceed the maximum integer value");
		if(max_distance <0 ) throw new DMasonException("AOI (max_discante) cannot be negative");
		if(rows <=0 ) throw new DMasonException("Rows cannot be less than zero");
		if(columns <=0 ) throw new DMasonException("Columns cannot be less than zero");

		if(MODE==HORIZONTAL_DISTRIBUTION_MODE)
		{

			if(rows >1) throw new DMasonException("In HORIZONTAL MODE the rows must be 1");

			DistributedField2D field = new DDoubleGrid2DY(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix);
			field.setToroidal(isToroidal);
			if(!fixed)
				((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

			return (DDoubleGrid2D)field;

		}
		else
			if(MODE==SQUARE_DISTRIBUTION_MODE)
			{
				DistributedField2D field = new DDoubleGrid2DXY(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix);
				field.setToroidal(isToroidal);
				if(!fixed)
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
				return (DDoubleGrid2D)field;

			}
			else if (MODE==SQUARE_BALANCED_DISTRIBUTION_MODE){
				if(((width% columns == 0) && (height% rows == 0)) && 
						(((width/ columns)%3 == 0) && ((height/ rows)%3 == 0)))
				{
					DistributedField2D field = new DDoubleGrid2DXYLB(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name, topicPrefix);
					field.setToroidal(isToroidal);
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
					if(rows >1) throw new DMasonException("In HORIZONTAL MODE the rows must be 1");

					DistributedField2D field = new DDoubleGrid2DYLB(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name, topicPrefix);
					field.setToroidal(isToroidal);
					if(!fixed)
						((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

					return (DDoubleGrid2D)field;


				}
				else 
				{
					throw new DMasonException("Illegal Distribution Mode");
				}
	}


	/**
	 * Method used only for Thin simulations
	 * 
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param MODE The mode of simulation (horizontal or squared, balanced or not)
	 * @param initialGridValue Starting value of the matrix
	 * @param fixed If it's true the field is read-only
	 * @param name ID of a region
	 * @param topicPrefix Prefix for the name of topics used only in Batch mode
	 * @return The right DSparseGrid2DThin
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DDoubleGrid2DThin createDDoubleGrid2DThin(int width, int height,SimState sm,int max_distance,int i,int j,int rows, int columns,int MODE, 
			double initialGridValue, boolean fixed, String name, String topicPrefix, boolean isToroidal)
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

			DistributedField2D field = new DDoubleGrid2DYThin(width, height,field_width,field_height,sm, max_distance, i, j, rows,columns, initialGridValue, name, topicPrefix);
			field.setToroidal(isToroidal);
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

				DistributedField2D field = new DDoubleGrid2DXYThin(width, height,field_width,field_height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix);
				field.setToroidal(isToroidal);
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
