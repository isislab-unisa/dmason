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

package dmason.sim.loadbalancing;

import java.util.ArrayList;
import dmason.sim.field.DistributedField;
import dmason.sim.field.DistributedField;

/**
 * Interface for all class of load balancing
 * 
 *
 */
public interface LoadBalancingInterface {

	
	/**
	 * Provide to generate the Cells
	 * @param x is the field which must divide
	 * @param superWidth is the width of field which must divide
	 * @param superHeight is the height field which must divide
	 * @param MAX_DISTANCE is the MaxDistance of simulation
	 * @param superOwnX is the X coordinates of corner up left of field which must divide
	 * @param superOwnY is the Y coordinates of corner up left of field which must divide
	 * @param numPeer number of peer
	 * @return ArrayList<MyCellInterface> list of all cells that compose the field
	 */
	public ArrayList<MyCellInterface> createRegions(DistributedField x, Object superWidth,Object superHeight,int MAX_DISTANCE,Object superOwnX,Object superOwnY, int numPeer);
	
	/**
	 * Calculates the right position of the cell in arrival.
	 * This number is the specular of the position of Cell that arrived
	 * @param pos the position of Cell that arrived
	 * @return the specular of the position of Cell that arrived
	 */
	public int calculatePositionForBalance(int pos);
	
	/**
	 * Add the value or agent in the specific region of myCell for the split
	 * @param location location of value or agent to add
	 * @param element the value or agent to add
	 * @param myCell the cell that contains the value or agent
	 * @return true is the add is done, false otherwise
	 */
	public boolean addForBalance(Object location, Object element, Object myCell);
	
	/**
	 * 
	 * Add the value or agent in the specific region of myCell for the union
	 * @param location location of value or agent to add
	 * @param element the value or agent to add
	 * @param myCell the cell that contains the value or agent
	 * @return
	 */
	public boolean addForUnion(Object location, Object element, Object myCell);

}
