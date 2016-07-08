package it.isislab.dmason.test.sim.field.continuous.region;

import static org.junit.Assert.*;

import java.util.HashMap;


import org.junit.Before;
import org.junit.Test;


import sim.util.Double2D;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.EntryAgent;
import it.isislab.dmason.test.sim.app.DFlockersUnitTest.DFlocker;

// TODO: Auto-generated Javadoc
/**
 * Test the Class RegionDouble.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class RegionDoubleTester {

	/** The rd. */
	RegionDouble rd;
	double upl_xx,upl_yy,down_xx,down_yy;

	/*
	 * Sets the up.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() {
		
		upl_xx = upl_yy=100;
		down_xx = down_yy = 300;
		
		rd = new RegionDouble(upl_xx,upl_yy,down_xx,down_yy);
	
	}

	@Test
	public void test_constructor_fields(){
		assertTrue(upl_xx == rd.upl_xx);
		assertTrue(upl_yy == rd.upl_xx);
		assertTrue(down_xx == rd.down_xx);
		assertTrue(down_yy == rd.down_yy);
	}
	
	@Test
	public void test_in_isMine(){
		for(double i=upl_xx; i<down_xx; i+=0.1){
			for(double j=upl_yy; j<down_yy; j+=0.1){

				assertTrue("in_isMine error for position "+i+"-"+j,rd.isMine(i, j));
			}
		}
	}
	
	@Test
	public void test_out_isMine(){
		for(double i=0; i<upl_xx; i+=0.1){
			for(double j=0; j<upl_yy; j+=0.1){

				assertFalse("out_isMine error for position "+i+"-"+j,rd.isMine(i, j));
			}
		}
		
		for(double i=down_xx; i<down_xx+100; i+=0.1){
			for(double j=down_yy; j<down_yy+100; j+=0.1){
				assertFalse("out_isMine error for position "+i+"-"+j,rd.isMine(i, j));
			}
		}
	}
	
	
	@Test
	public void test_addAgents(){
		EntryAgent<Double2D> agent = null;
		assertFalse("addAgent error, you should not add a null agent",rd.addAgents(agent));
		agent = new EntryAgent<Double2D>(null, null);
		assertFalse("addAgent error, you should not add a null agent",rd.addAgents(agent));
		RemotePositionedAgent<Double2D> fakeAgent = new DFlocker();
		fakeAgent.setId("fakeID");
		agent = new EntryAgent<Double2D>(fakeAgent, null);
		assertFalse("addAgent error, you should not add a EntryAgent with null Position",rd.addAgents(agent));
		agent = new EntryAgent<Double2D>(null, new Double2D());
		assertFalse("addAgent error, you should not add a EntryAgent with null RemotePositionedAgent",rd.addAgents(agent));
		
		agent = new EntryAgent<Double2D>(fakeAgent, new Double2D());
		assertTrue("addAgent error, you should not add a EntryAgent with null RemotePositionedAgent",rd.addAgents(agent));
		
		//add the same agent to check regiondouble size (adding the same agent twice should not have effect)
		rd.addAgents(agent);
		rd.addAgents(agent);
		rd.addAgents(agent);
		assertTrue("AddAgent error: Add the same agent should not increase the regiondouble size",rd.size()==1);
		
		assertEquals("The agent added doesn't match",agent, rd.get(agent.r.getId()));
	}
	
	@Test
	public void test_add100Agents(){
		
		RemotePositionedAgent<Double2D> fakeAgent=null;
		EntryAgent<Double2D> agent = null;
		Double2D pos;
		for(int i=0; i<100; i++){
			fakeAgent = new DFlocker();
			fakeAgent.setId("fakeID-"+i);
			pos= new Double2D(i, i);
			agent = new EntryAgent<Double2D>(fakeAgent, pos);
			rd.addAgents(agent);
		}
		assertTrue("AddAgent error: the regiondouble size should be 100",rd.size()==100);
		
	}
	
	@Test
	public void test_clone(){
		int size= 100;
		RemotePositionedAgent<Double2D> fakeAgent=null;
		EntryAgent<Double2D> agent = null;
		Double2D pos;
		HashMap<String,EntryAgent> excpectesAgents= new HashMap<>();
		for(int i=0; i<size; i++){
			fakeAgent = new DFlocker();
			fakeAgent.setId("fakeID-"+i);
			pos= new Double2D(i, i);
			agent = new EntryAgent<Double2D>(fakeAgent, pos);
			rd.addAgents(agent);
			excpectesAgents.put(agent.r.getId(),agent);
		}
		
		RegionDouble cloned = (RegionDouble) rd.clone();
		for( EntryAgent e: cloned.values()){
			assertTrue(excpectesAgents.containsKey(e.r.getId()));
			assertTrue(excpectesAgents.get(e.r.getId()).equals(e));
		}
		
	}
	
}
