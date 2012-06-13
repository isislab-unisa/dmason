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
