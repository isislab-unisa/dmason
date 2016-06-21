package it.isislab.dmason.test.sim.engine;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.test.sim.engine.util.DistributedStateConnectionJMSFake;
import it.isislab.dmason.test.sim.engine.util.StubDistributedState;
import it.isislab.dmason.util.connection.ConnectionType;

public class DistributedStateTest {
	
	GeneralParam params;
	DistributedMultiSchedule schedule;
	String prefix;
	int typeOfConnection;
	DistributedStateConnectionJMSFake conjms;
	
	int width, height, aoi, rows, columns, numAgents, mode; 
	
	@Before
	public void setUp(){
		
		typeOfConnection=ConnectionType.fakeUnitTestJMS;
		width=300; height=400; aoi=10; rows=3; columns=11; numAgents=3000000; mode=DistributedField2D.UNIFORM_PARTITIONING_MODE;
		params = new GeneralParam(width, height, aoi, rows, columns, numAgents, mode, typeOfConnection);
		schedule = new DistributedMultiSchedule<>();
		prefix = new String("tester");
	}
	
	@Test
	public void testConstructor(){
		DistributedState ds = new StubDistributedState<>(params, schedule, prefix, typeOfConnection);
		conjms = new DistributedStateConnectionJMSFake<>(ds);
		assertNotNull(ds);

	}
	
	@Test
	public void testFields(){
		DistributedState ds = new StubDistributedState<>(params, schedule, prefix, typeOfConnection);
		conjms = new DistributedStateConnectionJMSFake<>(ds);
		Object[] expecteds={aoi, columns, mode, numAgents,(params.getRows() * params.getColumns()),rows*columns,rows,schedule,prefix,new CellType(params.getI(), params.getJ())};
		Object[] actuals={ds.AOI,ds.columns,ds.MODE,ds.NUMAGENTS,ds.NUMPEERS,ds.P,ds.rows,ds.schedule,ds.topicPrefix,ds.TYPE};
		assertArrayEquals(expecteds, actuals);
	}
	
}
