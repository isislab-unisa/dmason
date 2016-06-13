package it.isislab.dmason.test.sim.engine.util;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import sim.engine.SimState;

public class StubDistributedState<E> extends DistributedState<E> {
	
	/**
	 * Instantiates a new stub distributed state.
	 *
	 * @param field
	 *            the field
	 */
	public StubDistributedState(DistributedField2D<E> field) {

		super();
		((DistributedMultiSchedule<E>) schedule).addField(field);

	}

	/**
	 * Instantiates a new stub distributed state.
	 *
	 * @param params
	 *            the params
	 */
	public StubDistributedState(GeneralParam params) {
		super(params, new DistributedMultiSchedule<E>(), "stub",
				params.getConnectionType());

		this.MODE = params.getMode();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.engine.DistributedState#getField()
	 */
	@Override
	public DistributedField<E> getField() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.isislab.dmason.sim.engine.DistributedState#addToField(it.isislab
	 * .dmason.sim.engine.RemotePositionedAgent, java.lang.Object)
	 */
	@Override
	public void addToField(RemotePositionedAgent<E> rm, E loc) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.sim.engine.DistributedState#getState()
	 */
	@Override
	public SimState getState() {
		// TODO Auto-generated method stub
		return null;
	}

}
