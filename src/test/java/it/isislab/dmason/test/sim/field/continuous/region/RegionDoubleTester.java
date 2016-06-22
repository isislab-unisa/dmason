package it.isislab.dmason.test.sim.field.continuous.region;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;
import com.thoughtworks.xstream.io.xml.DocumentWriter;

import sim.util.Double2D;
import it.isislab.dmason.exception.DMasonException;
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
	double own_x,own_y,down_x,down_y;

	/*
	 * Sets the up.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() {
		
		own_x =	own_y=100;
		down_x = down_y = 300;
		
		rd = new RegionDouble(own_x,own_y,down_x,down_y);
	
	}

	@Test
	public void test_in_isMine(){
		for(double i=own_x; i<down_x; i+=0.1){
			for(double j=own_y; j<down_y; j+=0.1){

				assertTrue("in_isMine error for position "+i+"-"+j,rd.isMine(i, j));
			}
		}
	}
	
	@Test
	public void test_out_isMine(){
		for(double i=0; i<own_x; i+=0.1){
			for(double j=0; j<own_y; j+=0.1){

				assertFalse("out_isMine error for position "+i+"-"+j,rd.isMine(i, j));
			}
		}
		
		for(double i=down_x; i<down_x+100; i+=0.1){
			for(double j=down_y; j<down_y+100; j+=0.1){
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
