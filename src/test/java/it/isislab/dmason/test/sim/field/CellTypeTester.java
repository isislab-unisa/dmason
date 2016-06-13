package it.isislab.dmason.test.sim.field;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;
import it.isislab.dmason.sim.field.CellType;

// TODO: Auto-generated Javadoc
/**
 * Test the Class CellType.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class CellTypeTester{

	/** The cell type. */
	CellType[] list_ct=null;
	//CellType ct;
	
	/** The number of loop of the iteration of tests. */
	int testLimit; // number of for's cycle

	/**
	 * Set the enviroment.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		testLimit = 3;
		list_ct = new CellType[9];
		int cnt=0;
		for(int i=0; i<testLimit; i++)
			for(int j=0; j<testLimit; j++){
				list_ct[cnt++] = new CellType(i, j);
		}
		
	}

	// getInitialValue
	/**
	 * Test get initial value.
	 */
	@Test
	public void testGetInitialValue() {
		int cnt=0;
		for(int i=0; i<testLimit; i++)
			for(int j=0; j<testLimit; j++){
				assertEquals((i*10)+j, list_ct[cnt++].getInitialValue());
		}
	}

	// getId
	@Test
	public void testGetId(){
		int cnt=0,x;
		
		for(int i=0; i<testLimit; i++)
			for(int j=0; j<testLimit; j++){
				x=cnt++;
				assertEquals(((i*10)+j)+x, list_ct[x].getId(x));
		}
	}

	// getNeighbourLeft

	/**
	 * Test get neighbour left x_ y.
	 */
	@Test
	public void testGetNeighbours() {
		int cnt=0,x;
		for (int i = 0; i < testLimit; i++) {
			for (int j = 0; j < testLimit; j++) {
				x=cnt++;
				assertEquals("CellType:"+i+""+j+" error", i + "-" + (j - 1), list_ct[x].getNeighbourLeft());
				assertEquals("CellType:"+i+""+j+" error",(i - 1) + "-" + (j - 1), list_ct[x].getNeighbourDiagLeftUp());
				assertEquals("CellType:"+i+""+j+" error",(i - 1) + "-" + (j), list_ct[x].getNeighbourUp());
				assertEquals("CellType:"+i+""+j+" error",(i - 1) + "-" + (j + 1), list_ct[x].getNeighbourDiagRightUp());
				assertEquals("CellType:"+i+""+j+" error",(i) + "-" + (j + 1), list_ct[x].getNeighbourRight());
				assertEquals("CellType:"+i+""+j+" error",(i + 1) + "-" + (j + 1), list_ct[x].getNeighbourDiagRightDown());
				assertEquals("CellType:"+i+""+j+" error",(i + 1) + "-" + (j), list_ct[x].getNeighbourDown());
				assertEquals("CellType:"+i+""+j+" error",(i + 1) + "-" + (j - 1), list_ct[x].getNeighbourDiagLeftDown());
			}
		}

	}

	// toString
	/**
	 * Test to string x_ y.
	 */
	@Test
	public void testToStringX_Y() {
		int cnt=0,x;
		for (int i = 0; i < testLimit; i++) {
			for (int j = 0; j < testLimit; j++) {
				x=cnt++;
				assertEquals(i + "-" + j, list_ct[x].toString());
			}
		}

	}
}
