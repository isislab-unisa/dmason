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
package it.isislab.dmason.experimentals.sim.field.grid.object;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.DistributedField2D;
import sim.engine.SimState;

/**
 *  
 * Factory for ObjectGrid2D 
 *  
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
*/
public class DObjectGrid2DFactory {

	
	public static final DObjectGrid2D createDObjectGrid2D(int width, int height,SimState sm,int max_distance,int i,int j,int rows,int columns,int MODE, 
			Object initialGridValue, String name, String topicPrefix, boolean isToroidal)
					throws DMasonException
					{
		//general parameters check
		if(columns<=0 || rows <=0){throw new DMasonException("Illegal value : columns value and rows value must be greater than 0");}
		if(width<=0) {throw new DMasonException("Illegal value: Field width <= 0 is not defined");}
		if(height<=0) {throw new DMasonException("Illegal value: Field height <= 0 is not defined");}
		if(width>=Integer.MAX_VALUE) {throw new DMasonException("Illegal value : width value exceeds Integer max value");}
		if(height>=Integer.MAX_VALUE) {throw new DMasonException("Illegal value : height value exceeds Integer max value");}
		if(max_distance<=0){throw new DMasonException("Illegal value, max_distance value must be greater than 0");}
		if(max_distance>=Integer.MAX_VALUE ){throw new DMasonException("Illegal value : max_distance value exceded Integer max value");}
		if(rows==1 && columns==1){throw new DMasonException("Illegal value : field partitioning with one row and one column is not defined");}
		
		if(MODE==DistributedField2D.UNIFORM_PARTITIONING_MODE)
		{
		
			  
			DObjectGrid2D field = new DObjectGrid2DXY(width, height,sm, max_distance, i, j, rows,columns, initialGridValue, name,topicPrefix,isToroidal);
			

			return field;

		}
		return null;
		
		
					}	
}
