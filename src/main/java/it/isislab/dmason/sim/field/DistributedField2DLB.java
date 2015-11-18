package it.isislab.dmason.sim.field;

import it.isislab.dmason.sim.field.support.loadbalancing.MyCellInterface;

import java.util.HashMap;

public interface DistributedField2DLB<E> {


	//Methods for Load Balancing, if you don't need these methods you must "Add Unimplement Method"
	/**
	 * Return a list of MyCell
	 * @return list of cell  
	 */
	public HashMap<Integer, MyCellInterface> getToSendForBalance();

	/**
	 * Set parameter for split
	 * @param isSplitted true if cell will split   
	 */
	public void setIsSplitted(boolean isSplitted);
	
	/**
	 * get parameter on field for split
	 * @param isSplitted true if cell is splitted   
	 */
	public boolean isSplitted();
	
	/**
	 * get parameter on field for split
	 * @param isPrepareForBalance true if cell is splitting   
	 */
	public boolean isPrepareForBalance();
	
	/**
	 * get parameter on field for union
	 * @param isUnited true if cell is united   
	 */
	public boolean isUnited();

	/**
	 * Set setForBalance 
	 * @param setForBalance true for loadbalancing
	 */
	public void prepareForBalance(boolean prepareForBalance);
	
	public HashMap<Integer, MyCellInterface> getToSendForUnion();


	public void prepareForUnion(boolean prepareForUnion);
	
	/**
	 * Get number of agents of a region
	 * @return number of agents of a region
	 */
	/**USATI SOLO NELLE Y***/
	/**
	 * Get number of agents in the left mine
	 * @return number of agents in the left mine
	 */
	public int getLeftMineSize();
	
	
	/**
	 * Get number of agents in the right mine
	 * @return number of agents in the right mine
	 */
	public int getRightMineSize();
	
}
