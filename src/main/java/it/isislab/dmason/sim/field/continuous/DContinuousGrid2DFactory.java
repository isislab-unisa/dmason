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

package it.isislab.dmason.sim.field.continuous;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.sim.field.continuous.loadbalanced.DContinuousGrid2DXYLB;
import it.isislab.dmason.experimentals.sim.field.continuous.loadbalanced.DContinuousGrid2DYLB;
import it.isislab.dmason.experimentals.sim.field.continuous.thin.DContinuousGrid2DThin;
import it.isislab.dmason.experimentals.sim.field.continuous.thin.DContinuousGrid2DXYThin;
import it.isislab.dmason.experimentals.sim.field.continuous.thin.DContinuousGrid2DYThin;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import sim.engine.SimState;


/**
 *  A Factory class to create the right distribution field according to four parameters
 *  HORIZONTAL_DISTRIBUTION_MODE, SQUARE_DISTRIBUTION_MODE, SQUARE_BALANCED_DISTRIBUTION_MODE and HORIZONTAL_BALANCED_DISTRIBUTION_MODE.
 *  
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
 */
public final class DContinuousGrid2DFactory 
{	


	/**
	 * 
	 * @param discretization Parameter for the MASON discretization of a continuous field
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param MODE The mode of simulation (horizontal or squared, balanced or not)
	 * @param name ID of a region
	 * @param topicPrefix Prefix for the name of topics used only in Batch mode
	 * @param isToroidal true if the field is a toroidal
	 * @return The right DContinuous2D
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DContinuousGrid2D createDContinuous2D(double discretization,double width, double height,SimState sm,int max_distance,int i,int j,int rows,int columns, int MODE, String name, String topicPrefix, boolean isToroidal)
			throws DMasonException
	{	

		//general parameters check for value   

		if(width>=Double.MAX_VALUE) {throw new DMasonException("Illegal value : width value exceeds Double max value");}
		if(height<=0) {throw new DMasonException("Illegal value: Field height <= 0 is not defined");}
		if(height>=Double.MAX_VALUE) {throw new DMasonException("Illegal value : height value exceeds Double max value");}
		if(max_distance<=0){throw new DMasonException("Illegal value, max_distance value must be greater than 0");}
		if(max_distance>=Integer.MAX_VALUE ){throw new DMasonException("Illegal value : max_distance value exceded Integer max value");}
		if(max_distance>=width ){throw new DMasonException(String.format("Illegal value : max_distance (%d) value exceded width(%f) value",max_distance,width));}
		if(width<=0) {throw new DMasonException("Illegal value: Field width <= 0 is not defined");}
		if(columns<=0 || rows <=0){throw new DMasonException("Illegal value : columns value and rows value must be greater than 0");}
		if(rows==1 && columns==1){throw new DMasonException("Illegal value : field partitioning with one row and one column is not defined");}


		if(MODE==DistributedField2D.UNIFORM_PARTITIONING_MODE)
		{  
			DistributedField2D field = new DContinuousGrid2DXY(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix,isToroidal);
			((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

			return (DContinuousGrid2D)field;

		}
		else
			if(MODE==DistributedField2D.SQUARE_BALANCED_DISTRIBUTION_MODE)
			{
				int my_width=(int) (width/columns);
				double safezone = my_width /3;
				if(((width% columns == 0) && (height% rows == 0)) && 
						(((width/ columns)%3 == 0) && ((height/ rows)%3 == 0)) && max_distance < safezone/2 ){

					DistributedField2D field = new DContinuousGrid2DXYLB(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix,isToroidal);
					
					((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

					return (DContinuousGrid2D)field;
				}
				else
					throw new DMasonException("Illegal width or height dimension or MAXDISTANCE for NUM_PEERS:"+(rows*columns));
			}
			else if(MODE==DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE)
			{
				if(rows!=1){throw new DMasonException("Illegal rows dimension for horizontal balanced mode, it must have one row");}
				DistributedField2D field = new DContinuousGrid2DYLB(discretization,width, height,sm, max_distance, i, j, rows,columns,name,topicPrefix,isToroidal);	
				
				((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

				return (DContinuousGrid2D)field;
			}
			else 
			{
				throw new DMasonException("Illegal Distribution Mode");
			}
	}
	public static final DContinuousGrid2D createDContinuous2DNonUniform(double discretization,double width, double height,SimState sm,int max_distance, int id,int P, int MODE, String name, String topicPrefix, boolean isToroidal)
	{	
		if(MODE == DistributedField2D.NON_UNIFORM_PARTITIONING_MODE)
		{
			DistributedField2D field = new DContinuousNonUniform(discretization, width, height, sm, max_distance, id, P, name, topicPrefix);
			field.setToroidal(isToroidal);
			((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);
			return (DContinuousGrid2D)field;
		}return null;
	}

	/**
	 * Method used only for Thin simulations
	 * 
	 * @param discretization Parameter for the MASON discretization of a continuous field
	 * @param width The width of the simulated field
	 * @param height The height of the simulated field
	 * @param sm The SimState of simulation
	 * @param max_distance The maximum distance of shift of the agents
	 * @param i i position in the field
	 * @param j j position in the field
	 * @param rows number of rows in the division
	 * @param columns number of columns in the division
	 * @param MODE The mode of simulation (horizontal or squared, balanced or not)
	 * @param name ID of a region
	 * @param topicPrefix Prefix for the name of topics used only in Batch mode
	 * @return The right DContinuous2DThin
	 * @throws DMasonException if the ratio between field dimensions and the number of peers is not right
	 */
	public static final DContinuousGrid2DThin createDContinuous2DThin(double discretization,double width, double height,SimState sm,int max_distance,int i,int j,int rows,int columns, int MODE, String name, String topicPrefix, boolean isToroidal)
			throws DMasonException
	{
		
		
		if(MODE==DistributedField2D.THIN_MODE)
		{
			double field_width,field_height;

			//upper left corner's coordinates

			// own width and height
			if(j<(width%columns))
				field_width=(int) Math.floor(width/columns+1)+4*max_distance;
			else
				field_width=(int) Math.floor(width/columns)+4*max_distance;
			field_height=height;

			DistributedField2D field = new DContinuousGrid2DYThin(discretization,width, height,field_width,field_height,sm, max_distance, i, j, rows,columns,name,topicPrefix);	
			field.setToroidal(isToroidal);
			((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

			return (DContinuousGrid2DYThin)field;

		}
		else
			if(MODE==DistributedField2D.THIN_MODE)
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


				DistributedField2D field = new DContinuousGrid2DXYThin(discretization,width, height,field_width,field_height,sm, max_distance, i, j, rows,columns,name,topicPrefix);
				field.setToroidal(isToroidal);
				((DistributedMultiSchedule)((DistributedState)sm).schedule).addField(field);

				return (DContinuousGrid2DXYThin)field;
			}else 
			{
				throw new DMasonException("Illegal Distribution Mode");
			}

	}
}
