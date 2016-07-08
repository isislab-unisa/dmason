package it.isislab.dmason.test.sim.field.continuous;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import sim.engine.SimState;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2D;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DFactory;
import it.isislab.dmason.sim.field.continuous.DContinuousGrid2DXY;
import it.isislab.dmason.sim.field.continuous.DContinuousNonUniform;
import it.isislab.dmason.test.sim.engine.util.StubDistributedState;

// TODO: Auto-generated Javadoc
/**
 * Test the Class DContinuous2DFactory.
 * 
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class DContinuousGrid2DFactoryTester {

	/** The distributed continuous. */
	DContinuousGrid2D dcon;


	String name, topicPrefix;
	boolean isToroidal;
	double discretization, width, height;
	int max_distance, i, j, rows, columns, MODE, id, P;
	SimState sm;

	@Before
	public void setUP(){
		discretization=2;
		width=210;
		height=210;
		name="name";
		topicPrefix="prefix";
		max_distance=2;
		i=2;
		j=2;
		rows=2;
		columns=2;
		MODE=DistributedField2D.UNIFORM_PARTITIONING_MODE;
		id=3;
		P=23;
		sm = new StubDistributedState<>();
		isToroidal=false;
	}

	//DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal)

	@Test
	public void test_discretizationValueCreateDContinuous2DUniform(){

		try{
			discretization = Double.MIN_VALUE; //Discretization min double value

			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("FAIL Illegal value : discretization exceeds Double MIN value");
		}catch(DMasonException minvalueException){
			assertTrue("Illegal value : discretization exceeds Double MIN value",minvalueException.getMessage().equals("Illegal value : discretization exceeds Double MIN value"));
			try{
				discretization = Double.MAX_VALUE; //Discretization max double value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("FAIL Illegal value : discretization value exceeds Double MAX value");

			}catch(DMasonException maxvalueException){
				assertTrue("Illegal value : discretization value exceeds Double MAX value",maxvalueException.getMessage().equals("Illegal value : discretization value exceeds Double MAX value"));
				try{
					discretization = -1; //Discretization min double value
					DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
					fail("FAIL Illegal value : discretization exceeds Double MIN value");
				}catch(DMasonException negativeException){
					assertTrue("Illegal value : discretization exceeds Double MIN value",negativeException.getMessage().equals("Illegal value : discretization exceeds Double MIN value"));
				}
			}
		}
	}

	@Test
	public void test_widthValueCreateDContinuous2DUniform(){

		try{
			width = Integer.MIN_VALUE; //width negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value: Field exceeds Double MIN value");
		}catch(DMasonException negativeWidthException){
			assertTrue("Illegal value: Field exceeds Double MIN value",negativeWidthException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
			try{
				width = Double.MIN_VALUE; //width min double value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value: Field exceeds Double MIN value");
			}catch(DMasonException minDoubleWidthException){
				assertTrue("Illegal value: Field exceeds Double MIN value",minDoubleWidthException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
				try{
					width = Double.MAX_VALUE; //width max double value
					DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
					fail("Fail Illegal value : width value exceeds Double MAX value");
				}catch(DMasonException maxDoubleWidthException){
					assertTrue("Illegal value : width value exceeds Double MAX value",maxDoubleWidthException.getMessage().equals("Illegal value : width value exceeds Double MAX value"));
					try{
						width = 0; //width zero value
						DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
						fail("Fail Illegal value: Field exceeds Double MIN value");
					}catch(DMasonException zeroWidthException){
						assertTrue("Illegal value: Field exceeds Double MIN value",zeroWidthException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
					}
				}
			}
		}
	}

	@Test
	public void test_heightValueCreateDContinuous2DUniform(){


		try{
			height =Double.MIN_VALUE;//height min double value
			DContinuousGrid2DFactory.createDContinuous2D(1,210 ,Double.MIN_VALUE, null, 10, 29, 34, 3, 3,DistributedField2D.UNIFORM_PARTITIONING_MODE, "name", "topix", false);
			fail("Fail Illegal value: Field exceeds Double MIN value");
		}catch(DMasonException minDoubleHeightException){
			assertTrue("Illegal value: Field exceeds Double MIN value",minDoubleHeightException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
			try{
				height =Double.MAX_VALUE;//height max double value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : height value exceeds Double MAX value");
			}catch(DMasonException maxDoubleHeightException){
				assertTrue("Illegal value : height value exceeds Double MAX value",maxDoubleHeightException.getMessage().equals("Illegal value : height value exceeds Double MAX value"));
				try{
					height =0;//height zero value
					DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
					fail("Fail Illegal value: Field exceeds Double MIN value");
				}catch(DMasonException zeroHeightException){
					assertTrue("Illegal value: Field exceeds Double MIN value",zeroHeightException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
				}
			}
		}
	}
	@Test
	public void test_simStateValueCreateDContinuous2DUniform(){
		try{
			sm=null;
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : SimState is null");
		}catch(DMasonException nullSimStateException){

			assertTrue("Illegal value : SimState is null",nullSimStateException.getMessage().equals("Illegal value : SimState is null"));
		}
	}

	@Test
	public void test_maxDistanceValueCreateDContinuous2DUniform(){

		try{
			max_distance = Integer.MIN_VALUE;//max_distance negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value, max_distance value must be greater than 0");
		}catch(DMasonException negativeAOIException){
			assertTrue("Illegal value, max_distance value must be greater than 0",negativeAOIException.getMessage().equals("Illegal value, max_distance value must be greater than 0"));
			try{
				max_distance = 0;//max_distance zero value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value, max_distance value must be greater than 0");
			}catch(DMasonException zeroAOIException){
				assertTrue("Illegal value, max_distance value must be greater than 0",zeroAOIException.getMessage().equals("Illegal value, max_distance value must be greater than 0"));
				try{
					max_distance = (int)width+1;//max_distance greater than width value
					DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
					fail("Fail "+String.format("Illegal value : max_distance (%d) value exceded width(%f) value",max_distance,width));
				}catch(DMasonException outOfWidthAOIException){
					String message = String.format("Illegal value : max_distance (%d) value exceded width(%f) value",max_distance,width);
					assertTrue(message,outOfWidthAOIException.getMessage().equals(message));
				}
			}
		}
	}

	@Test
	public void test_iValueCreateDContinuous2DUniform(){

		try{
			i = Integer.MIN_VALUE;//celltype_i negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : celltype_i value should not be negative");
		}catch(DMasonException negativeCelltype_iException){
			assertTrue("Illegal value : celltype_i value should not be negative",negativeCelltype_iException.getMessage().equals("Illegal value : celltype_i value should not be negative"));
			try{
				i = Integer.MAX_VALUE;//celltype_i max int value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : celltype_i exceeds Integer MAX value");
			}catch(DMasonException maxIntCelltype_iException){
				assertTrue("Illegal value : celltype_i exceeds Integer MAX value",maxIntCelltype_iException.getMessage().equals("Illegal value : celltype_i exceeds Integer MAX value"));
			}
		}
	}

	@Test
	public void test_jValueCreateDContinuous2DUniform(){

		try{
			j = Integer.MIN_VALUE;//celltype_j negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : celltype_j value should not be negative");
		}catch(DMasonException negativeCelltype_jException){
			assertTrue("Illegal value : celltype_j value should not be negative",negativeCelltype_jException.getMessage().equals("Illegal value : celltype_j value should not be negative"));
			try{
				j = Integer.MAX_VALUE;//celltype_j max int value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : celltype_j exceeds Integer MAX value");
			}catch(DMasonException maxIntCelltype_jException){
				assertTrue("Illegal value : celltype_j exceeds Integer MAX value",maxIntCelltype_jException.getMessage().equals("Illegal value : celltype_j exceeds Integer MAX value"));
			}
		}
	}


	@Test
	public void test_rowsValueCreateDContinuous2DUniform(){

		try{
			rows = Integer.MIN_VALUE;//rows negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : rows value must be greater than 0");
		}catch(DMasonException negativeRowsException){
			assertTrue("Illegal value : rows value must be greater than 0",negativeRowsException.getMessage().equals("Illegal value : rows value must be greater than 0"));
			try{
				rows = 0;//rows zero value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : rows value must be greater than 0");
			}catch(DMasonException zeroRowsException){
				assertTrue("Illegal value : rows value must be greater than 0",zeroRowsException.getMessage().equals("Illegal value : rows value must be greater than 0"));
				try{
					rows = Integer.MAX_VALUE;//rows max int value
					DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
					fail("Fail Illegal value : rows exceeds Integer MAX value");
				}catch(DMasonException maxIntRowsException){
					assertTrue("Illegal value : rows exceeds Integer MAX value",maxIntRowsException.getMessage().equals("Illegal value : rows exceeds Integer MAX value"));
				}
			}
		}
	}


	@Test
	public void test_columnsValueCreateDContinuous2DUniform(){

		try{
			columns = Integer.MIN_VALUE;//rows negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : columns value must be greater than 0");
		}catch(DMasonException negativeColsException){
			assertTrue("Illegal value : columns value must be greater than 0",negativeColsException.getMessage().equals("Illegal value : columns value must be greater than 0"));
			try{
				columns = 0;//rows zero value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : columns value must be greater than 0");
			}catch(DMasonException zeroColsException){
				assertTrue("Illegal value : columns value must be greater than 0",zeroColsException.getMessage().equals("Illegal value : columns value must be greater than 0"));
				try{
					columns = Integer.MAX_VALUE;//rows max int value
					DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
					fail("Fail Illegal value : columns exceeds Integer MAX value");
				}catch(DMasonException maxIntColsException){
					assertTrue("Illegal value : columns exceeds Integer MAX value",maxIntColsException.getMessage().equals("Illegal value : columns exceeds Integer MAX value"));
				}
			}
		}
	}


	@Test
	public void test_rows_columnsValueCreateDContinuous2DUniform(){

		try{
			rows = 1;//rows negative value
			columns=1;
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : field partitioning with one row and one column is not defined");
		}catch(DMasonException oneRCException){
			assertTrue("Illegal value : field partitioning with one row and one column is not defined",oneRCException.getMessage().equals("Illegal value : field partitioning with one row and one column is not defined"));
		}
	}


	@Test
	public void test_modeValueCreateDContinuous2DUniform(){

		try{
			MODE = Integer.MIN_VALUE;//MODE negative value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal Distribution Mode");
		}catch(DMasonException negativeModeException){
			assertTrue("Illegal Distribution Mode",negativeModeException.getMessage().equals("Illegal Distribution Mode"));
			try{
				MODE = Integer.MAX_VALUE;//MODE max int value
				DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal Distribution Mode");
			}catch(DMasonException maxIntModeException){
				assertTrue("Illegal Distribution Mode",maxIntModeException.getMessage().equals("Illegal Distribution Mode"));
			}
		}
	}


	@Test
	public void test_nameValueCreateDContinuous2DUniform(){

		try{
			name = null;//name null value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : name should not be null");
		}catch(DMasonException nullNameException){
			assertTrue("Illegal value : name should not be null",nullNameException.getMessage().equals("Illegal value : name should not be null"));
		}
	}

	@Test
	public void test_topicPrefixValueCreateDContinuous2DUniform(){

		try{
			topicPrefix = null;//name null value
			DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : topicPrefix should not be null");
		}catch(DMasonException nullTopicPrefixException){
			assertTrue("Illegal value : topicPrefix should not be null",nullTopicPrefixException.getMessage().equals("Illegal value : topicPrefix should not be null"));
		}
	}

	@Test
	public void test_CreateDContinuous2DUniform(){
		try{
			dcon = DContinuousGrid2DFactory.createDContinuous2D(discretization, width, height, sm, max_distance, i, j, rows, columns, MODE, name, topicPrefix, isToroidal);
			DContinuousGrid2DXY dconxy = (DContinuousGrid2DXY)dcon;
			assertTrue((dcon instanceof DContinuousGrid2DXY));
			assertArrayEquals(new Object[]{discretization, width, height,sm, max_distance, i, j, rows, columns, isToroidal},
					new Object[]{dconxy.discretization, dconxy.width, dconxy.height,dconxy.sm, dconxy.AOI, dconxy.cellType.pos_i, dconxy.cellType.pos_j, dconxy.rows, dconxy.columns, isToroidal});
		}catch(DMasonException e){
			fail("");
		}
	}

	@Test
	public void test_idValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		id=Integer.MIN_VALUE;
		try{
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Illegal value : id should not be negative");
		}catch(DMasonException negativeIDException){
			assertTrue("Illegal value : id should not be negative",negativeIDException.getMessage().equals("Illegal value : id should not be negative"));
			try{
				id=Integer.MAX_VALUE;
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("Illegal value : id exceeds Integer MAX value");
			}catch(DMasonException maxIDValueException){
				assertTrue("Illegal value : id exceeds Integer MAX value",maxIDValueException.getMessage().equals("Illegal value : id exceeds Integer MAX value"));
			}
		}
	}


	@Test
	public void test_pValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		P=Integer.MIN_VALUE;
		try{
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Illegal value : id should not be negative");
		}catch(DMasonException negativePException){
			assertTrue("Illegal value : P should not be negative",negativePException.getMessage().equals("Illegal value : P should not be negative"));
			try{
				P=Integer.MAX_VALUE;
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("Illegal value : P exceeds Integer MAX value");
			}catch(DMasonException maxPValueException){
				assertTrue("Illegal value : P exceeds Integer MAX value",maxPValueException.getMessage().equals("Illegal value : P exceeds Integer MAX value"));
			}
		}
	}


	@Test
	public void test_discretizationValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		try{
			discretization = Double.MIN_VALUE; //Discretization min double value

			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("FAIL Illegal value : discretization exceeds Double MIN value");
		}catch(DMasonException minvalueException){
			assertTrue("Illegal value : discretization exceeds Double MIN value",minvalueException.getMessage().equals("Illegal value : discretization exceeds Double MIN value"));
			try{
				discretization = Double.MAX_VALUE; //Discretization max double value
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("FAIL Illegal value : discretization value exceeds Double MAX value");

			}catch(DMasonException maxvalueException){
				assertTrue("Illegal value : discretization value exceeds Double MAX value",maxvalueException.getMessage().equals("Illegal value : discretization value exceeds Double MAX value"));
				try{
					discretization = -1; //Discretization min double value
					DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
					fail("FAIL Illegal value : discretization exceeds Double MIN value");
				}catch(DMasonException negativeException){
					assertTrue("Illegal value : discretization exceeds Double MIN value",negativeException.getMessage().equals("Illegal value : discretization exceeds Double MIN value"));
				}
			}
		}
	}

	@Test
	public void test_widthValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;

		try{
			width = Double.MIN_VALUE; //width min double value
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value: Field exceeds Double MIN value");
		}catch(DMasonException minDoubleWidthException){
			assertTrue("Illegal value: Field exceeds Double MIN value",minDoubleWidthException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
			try{
				width = Double.MAX_VALUE; //width max double value
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : width value exceeds Double MAX value");
			}catch(DMasonException maxDoubleWidthException){
				assertTrue("Illegal value : width value exceeds Double MAX value",maxDoubleWidthException.getMessage().equals("Illegal value : width value exceeds Double MAX value"));
				try{
					width = 0; //width zero value
					DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
					fail("Fail Illegal value: Field exceeds Double MIN value");
				}catch(DMasonException zeroWidthException){
					assertTrue("Illegal value: Field exceeds Double MIN value",zeroWidthException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
				}
			}
		}

	}

	@Test
	public void test_heightValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;

		try{
			height =Double.MIN_VALUE;//height min double value
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value: Field exceeds Double MIN value");
		}catch(DMasonException minDoubleHeightException){
			assertTrue("Illegal value: Field exceeds Double MIN value",minDoubleHeightException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
			try{
				height =Double.MAX_VALUE;//height max double value
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value : height value exceeds Double MAX value");
			}catch(DMasonException maxDoubleHeightException){
				assertTrue("Illegal value : height value exceeds Double MAX value",maxDoubleHeightException.getMessage().equals("Illegal value : height value exceeds Double MAX value"));
				try{
					height =0;//height zero value
					DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
					fail("Fail Illegal value: Field exceeds Double MIN value");
				}catch(DMasonException zeroHeightException){
					assertTrue("Illegal value: Field exceeds Double MIN value",zeroHeightException.getMessage().equals("Illegal value: Field exceeds Double MIN value"));
				}
			}
		}
	}
	
	@Test
	public void test_simStateValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		try{
			sm=null;
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : SimState is null");
		}catch(DMasonException nullSimStateException){

			assertTrue("Illegal value : SimState is null",nullSimStateException.getMessage().equals("Illegal value : SimState is null"));
		}
	}

	@Test
	public void test_maxDistanceValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;

		try{
			max_distance = Integer.MIN_VALUE;//max_distance negative value
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value, max_distance value must be greater than 0");
		}catch(DMasonException negativeAOIException){
			assertTrue("Illegal value, max_distance value must be greater than 0",negativeAOIException.getMessage().equals("Illegal value, max_distance value must be greater than 0"));
			try{
				max_distance = 0;//max_distance zero value
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal value, max_distance value must be greater than 0");
			}catch(DMasonException minIntAOIException){
				assertTrue("Illegal value, max_distance value must be greater than 0",minIntAOIException.getMessage().equals("Illegal value, max_distance value must be greater than 0"));
			}
		}
	}

	@Test
	public void test_modeValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		try{
			MODE = Integer.MIN_VALUE;//MODE negative value
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal Distribution Mode");
		}catch(DMasonException negativeModeException){
			assertTrue("Illegal Distribution Mode",negativeModeException.getMessage().equals("Illegal Distribution Mode"));
			try{
				MODE = Integer.MAX_VALUE;//MODE max int value
				DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
				fail("Fail Illegal Distribution Mode");
			}catch(DMasonException maxIntModeException){
				assertTrue("Illegal Distribution Mode",maxIntModeException.getMessage().equals("Illegal Distribution Mode"));
			}
		}
	}


	@Test
	public void test_nameValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		try{
			name = null;//name null value
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : name should not be null");
		}catch(DMasonException nullNameException){
			assertTrue("Illegal value : name should not be null",nullNameException.getMessage().equals("Illegal value : name should not be null"));
		}
	}

	@Test
	public void test_topicPrefixValueCreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		try{
			topicPrefix = null;//name null value
			DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			fail("Fail Illegal value : topicPrefix should not be null");
		}catch(DMasonException nullTopicPrefixException){
			assertTrue("Illegal value : topicPrefix should not be null",nullTopicPrefixException.getMessage().equals("Illegal value : topicPrefix should not be null"));
		}
	}

	@Test
	public void test_CreateDContinuous2DNonUniform(){
		MODE = DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		try{
			dcon = DContinuousGrid2DFactory.createDContinuous2DNonUniform(discretization, width, height, sm, max_distance, id,P, MODE, name, topicPrefix, isToroidal);
			assertTrue("Class cast exception",(dcon instanceof DContinuousNonUniform));
			DContinuousNonUniform dconUni = (DContinuousNonUniform)dcon;
			assertArrayEquals("fields don't match",new Object[]{discretization, width, height,sm, max_distance, isToroidal},
					new Object[]{dconUni.discretization, dconUni.width, dconUni.height,dconUni.sm, dconUni.AOI, isToroidal});
		}catch(DMasonException e){
			fail("");
		}
	}
}
