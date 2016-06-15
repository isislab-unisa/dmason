package it.isislab.dmason.test.sim.engine;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import sim.util.Double2D;
import it.isislab.dmason.sim.engine.DistributedStateConnectionJMS;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.test.sim.engine.util.DistributedStateConnectionJMSFake;
import it.isislab.dmason.test.sim.engine.util.FakeUpdaterThreadForListener;
import it.isislab.dmason.test.sim.engine.util.StubDistributedState;
import it.isislab.dmason.test.sim.field.utils.StubDistributedField2D;
import it.isislab.dmason.test.util.connection.StubConnectionJMS;
import it.isislab.dmason.test.util.connection.VirtualConnection;
import it.isislab.dmason.test.util.connection.VirtualConnectionNFieldsWithVirtualJMS;
import it.isislab.dmason.util.connection.jms.ConnectionJMS;

/**
 * This is the DistributedStateConnectionJMS's tester.
 *
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 */

public class DistributedStateConnectionJMSTester {


	/** The variable to test. */
	DistributedStateConnectionJMS<Double2D> toTest;

	/** The connection. */
	StubConnectionJMS stConnection;
	StubDistributedState<Double2D> dm;

	/** The num of loop of tests. */
	int numLoop = 8;

	/**
	 * Set the dimension of field, if the mode is square or horizontal and if
	 * the space is toroidal.
	 * 
	 * @param rows
	 *            the number of rows
	 * @param colums
	 *            the number of colums
	 * @param mode
	 *            the modality that we use for the space: horizontal or square
	 * @param posiqqq
	 *            the rows's index of cell under test
	 * @param posj
	 *            the colum's index of cell under test
	 * @param isToroidal
	 *            set if the space is toroidal or not
	 * */
	public void setEnviroment(int rows, int colums, int mode, int posi,
			int posj, boolean isToroidal) {
		stConnection = new StubConnectionJMS();

		DistributedField2D<Double2D> field = new StubDistributedField2D<Double2D>();

		field.setToroidal(isToroidal);
		dm = new StubDistributedState<Double2D>(field);
		dm.rows = rows;
		dm.columns = colums;
		dm.NUMPEERS = 1; // if numpeers == 0 is throwed an exeption for a
							// division by 0
		dm.MODE = mode;
		dm.TYPE = new CellType(posi, posj);

		toTest = new DistributedStateConnectionJMSFake<Double2D>(dm, "127.0.0.1",
				"61616", stConnection);
		toTest.init_connection();
	}

	private ArrayList<String> getTopicMappingForNonToroidalTopicsCreation(int i, int j,int rows, int columns){
		CellType c = new CellType(i, j);
		ArrayList<String> result=new ArrayList<>();
		
		if(rows>1 && columns==1){
			if(i==0) result.add(c.toString()+"S");
			else if(i == rows-1) result.add(c.toString()+"N");
			else{
				result.add(c.toString()+"S");
				
				result.add(c.toString()+"N");
			}	
		}
		else if(rows==1 && columns > 1){
			if(j < columns)	result.add(c.toString()+"E");
			if(j > 0) result.add(c.toString()+"W");

		}else{
			//N rows and N columns
			if(j > 0)					
				result.add(c.toString()+"W");
			if(j < columns)
				result.add(c.toString()+"E");
			if(i > 0)
				result.add(c.toString()+"N");
			if(i < rows)
				result.add(c.toString()+"S");
			if(i < rows && j < columns)
				result.add(c.toString()+"SE");
			if(i > 0 && j < columns)
				result.add(c.toString()+"NE");
			if(i < rows && j > 0)
				result.add(c.toString()+"SW");
			if(i > 0 && j > 0)
				result.add(c.toString()+"NW");
		}
		return (result.size()==0)?null:result;
	}
	
	private ArrayList<String> getTopicMappingForToroidalTopicsCreation(int i, int j, int rows, int columns){
		CellType c = new CellType(i, j);
		ArrayList<String> topics = new ArrayList<String>();
		if(rows > 1 && columns == 1){
			topics.add(c.toString()+"N");
			topics.add(c.toString()+"S");
			topics.add(c.toString()+"NW");
			topics.add(c.toString()+"NE");
			topics.add(c.toString()+"SW");
			topics.add(c.toString()+"SE");

		}
		//one rows and N columns
		else if(rows==1 && columns>1){
			topics.add(c.toString()+"W");
			topics.add(c.toString()+"E");
			topics.add(c.toString()+"NW");
			topics.add(c.toString()+"NE");
			topics.add(c.toString()+"SW");
			topics.add(c.toString()+"SE");
		}else{
			// M rows and N columns
			topics.add(c.toString()+"W");
			topics.add(c.toString()+"E");
			topics.add(c.toString()+"S");
			topics.add(c.toString()+"N");
			topics.add(c.toString()+"NW");
			topics.add(c.toString()+"NE");
			topics.add(c.toString()+"SW");
			topics.add(c.toString()+"SE");

		}
		return (topics.size()>0)?topics:null;
	}

	private ArrayList<String> getSubscriberTopicsForNoToroidal(int i, int j, int rows, int columns){
		CellType c = new CellType(i, j);
		ArrayList<String> subscribers = new ArrayList<>();
		
		if(rows>1 && columns==1){
			if(i==0)					subscribers.add(c.getNeighbourDown() + "N");
			else if(i == rows-1) 		subscribers.add(c.getNeighbourUp() + "S");
			else{
										subscribers.add(c.getNeighbourDown() + "N");
										subscribers.add(c.getNeighbourUp() + "S");
			}	
		}
		else if(rows==1 && columns > 1){
			if(j < columns) 				subscribers.add(c.getNeighbourRight() + "W");
			if(j > 0) 						subscribers.add(c.getNeighbourLeft() + "E");
		}else{
			if(i > 0 && j > 0) 				subscribers.add(c.getNeighbourDiagLeftUp()+ "SE");
			if(i > 0 && j < columns) 		subscribers.add(c.getNeighbourDiagRightUp()+ "SW");
			if(i < rows && j > 0) 			subscribers.add(c.getNeighbourDiagLeftDown()+ "NE");
			if(i < rows && j < columns)		subscribers.add(c.getNeighbourDiagRightDown()+ "NW");
			if(j > 0)						subscribers.add(c.getNeighbourLeft() + "E");
			if(j < columns)					subscribers.add(c.getNeighbourRight() + "W");
			if(i > 0)						subscribers.add(c.getNeighbourUp() + "S");
			if(i < rows) 					subscribers.add(c.getNeighbourDown() + "N");
		}
		return (subscribers.size()>0)?subscribers:null;
	}
	
	
	public ArrayList<String> getSubscriberTopicsForToroidal(int i, int j, int rows, int columns){
		CellType c = new CellType(i, j);
		ArrayList<String> subscribers = new ArrayList<>();
		if(rows > 1 && columns == 1){
			subscribers.add(((i + 1 + rows) % rows) + "-"+ ((j + columns) % columns) + "N");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j + columns) % columns) + "S");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "SE");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "SW");
			subscribers.add(((i + 1 + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "NE");
			subscribers.add(+((i + 1 + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "NW");
		}
		else if(rows==1 && columns>1){
			
			subscribers.add(((i + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "W");
			subscribers.add(((i + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "E");
			subscribers.add(+((i - 1 + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "SE");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "SW");
			subscribers.add(+((i + 1 + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "NE");
			subscribers.add(((i + 1 + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "NW");
		}else{
			// M rows and N columns
			subscribers.add(((i + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "W");
			subscribers.add(((i + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "E");
			subscribers.add(((i + 1 + rows) % rows) + "-"+ ((j + columns) % columns) + "N");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j + columns) % columns) + "S");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "SE");
			subscribers.add(((i - 1 + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "SW");
			subscribers.add(((i + 1 + rows) % rows) + "-"+ ((j - 1 + columns) % columns) + "NE");
			subscribers.add(((i + 1 + rows) % rows) + "-"+ ((j + 1 + columns) % columns) + "NW");
		}
		
		return (subscribers.size()>0)?subscribers:null;
	}
	
	/**
	 * Verify if the topics are instantiated for a simply, static 3x3 non-toroidal configuration
	 * */
	@Test
	public void testInstantiatedTopicsNoToroidalSquareMode() { 
		int R=3,C=3;
		boolean isToroidal= false;
		for(int rows=1; rows <=R; rows++)
			for(int columns=2; columns <=C; columns++)
				for(int i=0; i < rows; i++)
					for(int j=0; j < columns; j++){
						setEnviroment(rows, columns, DistributedField2D.UNIFORM_PARTITIONING_MODE, i, j, isToroidal);
						assertNotNull(getTopicMappingForNonToroidalTopicsCreation(i,j,rows,columns));
						assertTrue(stConnection.topics.containsAll(getTopicMappingForNonToroidalTopicsCreation(i, j,rows,columns)));
					}
	}
	
	/**
	 * Verify if the topics are instantiated for a simply, static 3x3 toroidal configuration
	 * */
	@Test
	public void testInstantiatedTopicsToroidalSquareMode() { 
		int R=3,C=3;
		boolean isToroidal= true;
		for(int rows=1; rows <=R; rows++)
			for(int columns=2; columns <=C; columns++)
				for(int i=0; i < rows; i++)
					for(int j=0; j < columns; j++){
						setEnviroment(rows, columns, DistributedField2D.UNIFORM_PARTITIONING_MODE, i, j, isToroidal);
						assertNotNull(getTopicMappingForToroidalTopicsCreation(i,j,rows,columns));
						assertTrue(stConnection.topics.containsAll(getTopicMappingForToroidalTopicsCreation(i, j,rows,columns)));
			}
	}
	
	
	/**
	 * Verify if the topics are instantiated for a simply, static 3x3 non-toroidal configuration
	 * */
	@Test
	public void testSubscribeTopicsNoToroidal() { 
		int R=3,C=3;
		boolean isToroidal= false;
		for(int rows=1; rows <=R; rows++)
			for(int columns=2; columns <=C; columns++)
				for(int i=0; i < rows; i++)
					for(int j=0; j < columns; j++){
						setEnviroment(rows, columns, DistributedField2D.UNIFORM_PARTITIONING_MODE, i, j, isToroidal);
						assertNotNull(getSubscriberTopicsForNoToroidal(i,j,rows,columns));
						System.out.println(i+"-"+j+" "+getSubscriberTopicsForNoToroidal(i,j,rows,columns));
						assertTrue(stConnection.topicsSubscribed.containsAll(getSubscriberTopicsForNoToroidal(i, j,rows,columns)));
					}
	}
	
	/**
	 * Verify if the topics are instantiated for a simply, static 3x3 non-toroidal configuration
	 * */
	@Test
	public void testSubscribeTopicsToroidal() { 
		int R=3,C=3;
		boolean isToroidal= true;
		for(int rows=1; rows <=R; rows++)
			for(int columns=2; columns <=C; columns++)
				for(int i=0; i < rows; i++)
					for(int j=0; j < columns; j++){
						setEnviroment(rows, columns, DistributedField2D.UNIFORM_PARTITIONING_MODE, i, j, isToroidal);
						assertNotNull(getSubscriberTopicsForToroidal(i,j,rows,columns));
						assertTrue(stConnection.topicsSubscribed.containsAll(getSubscriberTopicsForToroidal(i, j,rows,columns)));
					}
	}

}