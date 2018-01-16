package it.isislab.dmason.test.sim.field.utils;

import java.util.HashMap;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.nonuniform.QuadTree;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

public class StubDistributedField2D<E> implements DistributedField2D<E>{

	/** The toroidal. */
	boolean toroidal = false;

	/**
	 * Instantiates a new stub distributed field2 d.
	 */
	public StubDistributedField2D() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField#synchro()
	 */
	@Override
	public boolean synchro() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField#getState()
	 */
	@Override
	public DistributedState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.isislab.dmason.sim.field.DistributedField#setTable(java.util.HashMap
	 * )
	 */
	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField#getID()
	 */
	@Override
	public String getDistributedFieldID() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField#getUpdates()
	 */
	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return null;
	}

	


	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField#getGlobals()
	 */
	@Override
	public VisualizationUpdateMap getGlobals() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField2D#isToroidal()
	 */
	@Override
	public boolean isToroidal() {

		// TODO Auto-generated method stub
		return toroidal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.isislab.dmason.sim.field.DistributedField2D#setToroidal(boolean)
	 */
	@Override
	public void setToroidal(boolean isToroidal) {
		// TODO Auto-generated method stub
		toroidal = isToroidal;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.field.DistributedField2D#
	 * setDistributedObjectLocation(java.lang.Object,
	 * it.isislab.dmason.sim.engine.RemotePositionedAgent,
	 * sim.engine.SimState)
	 */
	@Override
	public boolean setDistributedObjectLocation(E location, Object RemoteObject, SimState sm) throws DMasonException{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.isislab.dmason.sim.field.DistributedField2D#getAvailableRandomLocation
	 * ()
	 */
	@Override
	public E getAvailableRandomLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verifyPosition(E pos) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Bag clear() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean createRegions(QuadTree... cell) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getCommunicationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

}
