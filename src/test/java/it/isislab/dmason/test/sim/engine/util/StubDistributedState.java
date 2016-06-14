package it.isislab.dmason.test.sim.engine.util;

import ec.util.MersenneTwisterFast;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.DistributedStateConnectionJMS;
import it.isislab.dmason.sim.engine.DistributedStateConnectionMPI;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.engine.SimState;

public class StubDistributedState<E> extends DistributedState<E> {
	
	public StubDistributedState() {
		super();
	}
	
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
	
	public StubDistributedState(GeneralParam params,
			DistributedMultiSchedule<E> sched, String prefix,
			int typeOfConnection) {
		super(sched);

		P=params.getP();
		long randomizer = 0;
		if (prefix.startsWith("Batch"))
			randomizer = System.currentTimeMillis();

		this.TYPE = new CellType(params.getI(), params.getJ());
		this.random = new MersenneTwisterFast(randomizer
				+ this.TYPE.getInitialValue());
		this.AOI = params.getAoi();
		this.NUMPEERS = params.getRows() * params.getColumns();
		this.rows = params.getRows();
		this.columns = params.getColumns();
		this.NUMAGENTS = params.getNumAgents();
		this.count_id = NUMAGENTS * TYPE.getInitialValue();
		this.MODE = params.getMode();
		this.topicPrefix = prefix;

		
		switch (typeOfConnection) {
		case ConnectionType.fakeUnitTestJMS:
			serviceJMS = new DistributedStateConnectionFake(this);
			isPureAMQ = true;
			break;
//		case ConnectionType.hybridActiveMQMPIBcast:
//			serviceJMS = new DistributedStateConnectionJMS(this,
//					params.getIp(), params.getPort());
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isHybrid = true;
//			break;
//		case ConnectionType.hybridActiveMQMPIGather:
//			serviceJMS = new DistributedStateConnectionJMS(this,
//					params.getIp(), params.getPort());
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isHybrid = true;
//			break;
//		case ConnectionType.hybridActiveMQMPIParallel:
//			serviceJMS = new DistributedStateConnectionJMS(this,
//					params.getIp(), params.getPort());
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isHybrid = true;
//			break;
//		case ConnectionType.hybridMPIMultipleThreads:
//			serviceJMS = new DistributedStateConnectionJMS(this,
//					params.getIp(), params.getPort());
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isHybrid = true;
//			break;
//		case ConnectionType.pureMPIBcast:
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isPureMPI = true;
//			break;
//		case ConnectionType.pureMPIGather:
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isPureMPI = true;
//			break;
//		case ConnectionType.pureMPIParallel:
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isPureMPI = true;
//			break;
//		case ConnectionType.pureMPIMultipleThreads:
//			serviceMPI = new DistributedStateConnectionMPI(this,
//					typeOfConnection);
//			isPureMPI = true;
//			break;
//		case ConnectionType.fakeUnitTestJMS:
//
//			break;
		default:
			break;
		}

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
