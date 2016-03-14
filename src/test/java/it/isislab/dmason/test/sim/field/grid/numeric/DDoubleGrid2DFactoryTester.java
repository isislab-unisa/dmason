package it.isislab.dmason.test.sim.field.grid.numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DDoubleGrid2DFactory;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.engine.SimState;
import sim.util.Double2D;

// TODO: Auto-generated Javadoc
/**
 * Test the Class DDoubleGrid2DFactory.
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuoz
 */
public class DDoubleGrid2DFactoryTester {

	/** The double grid. */
	DDoubleGrid2D df;

	/** The num of loop of tests. */
	int numLoop = 8;

	/**
	 * The Class StubDistributedState.
	 */
	public class StubDistributedState extends DistributedState<Double2D> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/**
		 * Instantiates a new stub distributed state.
		 *
		 * @param params the params
		 */
		public StubDistributedState(GeneralParam params) {
			super(params, new DistributedMultiSchedule<Double2D>(), "stub",
					params.getConnectionType());

			this.MODE = params.getMode();

		}

		/**
		 * Instantiates a new stub distributed state.
		 */
		public StubDistributedState() {
			super();
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#getField()
		 */
		@Override
		public DistributedField<Double2D> getField() {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#addToField(it.isislab.dmason.sim.engine.RemotePositionedAgent, java.lang.Object)
		 */
		@Override
		public void addToField(RemotePositionedAgent<Double2D> rm, Double2D loc) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#getState()
		 */
		@Override
		public SimState getState() {
			// TODO Auto-generated method stub
			return null;
		}
	}



	/**
	 * Test for horizontal distribution mode with different width and height
	 */

	@Test
	public void testHorizontalDistributionModeWidthHeight() {

		int rows =1; //it is fixed for HM
		int width =241;
		int height =109;

		DDoubleGrid2D[][] ddg =null;
		int effectiveWidth=0,effectiveHeight =0;

		for(int columns=1; columns <= 11; columns++){
			ddg = new DDoubleGrid2D[rows][columns];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
					try {

						df = DDoubleGrid2DFactory.createDDoubleGrid2D(width,height,
								/* simState */new StubDistributedState(),
								/* max_distance */1,
								i ,j,rows,columns,
								DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
								1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
								"", /* isToroidal */false);
						ddg[i][j]=df;
					} catch (DMasonException e) {
						// TODO Auto-generated catch block
						fail(e.getMessage());
					}
				}
			}

			effectiveWidth=0;
			effectiveHeight =0;

			for(int i=0; i<columns; i++)
				effectiveWidth+=ddg[0][i].my_width;

			for(int i=0; i<rows; i++)
				effectiveHeight+=ddg[i][0].my_height;

			assertEquals("error for height=" + height, height, effectiveHeight);
			assertEquals("error for width=" + width, width, effectiveWidth);
		}
	}

	/**
	 * Test horizontal distribution mode with 0 width.
	 */
	@Test
	public void testHorizontalDistributionMode0Width() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */0, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception for width=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode 0 height.
	 */
	@Test
	public void testHorizontalDistributionMode0Height() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					0, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception for height=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode negative height.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeHeight() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					-3, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("really you can have a height <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode negative width.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeWidth() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */-10, /* height */
					3, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("really you can have a width <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode width max int.
	 */
	@Test
	public void testHorizontalDistributionModeWidthMaxInt() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(
					/* width */Integer.MAX_VALUE, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode height max int.
	 */
	@Test
	public void testHorizontalDistributionModeHeightMaxInt() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					Integer.MAX_VALUE, /* simState */
					new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}
	}

	
	/**
	 * Test horizontal distribution mode max distance=0.
	 */
	@Test
	public void testHorizontalDistributionZeroMaxDistance() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					0, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a maxdistance=0???");
		} catch (DMasonException e) {
			// ok
		}
	}
	
	
	
	
	/**
	 * Test horizontal distribution mode negative max distance.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeMaxDistance() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					-1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative distance???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode rows columns.
	 */
	@Test
	public void testHorizontalDistributionModeRowsColumns() {

		try {
			for (int j = 1; j < numLoop; j++) {

				df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
						10, /* simState */new StubDistributedState(),/* max_distance */
						1, /* i */0,/* j */0, /* rows */1, /* columns */j,
						DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
						1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
						"", /* isToroidal */false);

				assertEquals(1, df.rows);
				assertEquals(j, df.columns);
			}

		} catch (DMasonException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test horizontal distribution mode negative rows.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeRows() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */-10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative rows???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode negative columns.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeColumns() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */-10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative columns???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode 0 columns.
	 */
	@Test
	public void testHorizontalDistributionMode0Columns() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */0,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have 0 columns???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode 0 rows.
	 */
	@Test
	public void testHorizontalDistributionMode0Rows() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */0, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have 0 rows???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test horizontal distribution mode not fixed.
	 */
	@Test
	public void testHorizontalDistributionModeNotFixed() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */false, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			assertEquals("width", 10, df.getWidth());
			assertEquals("height", 10, df.getHeight());
			assertEquals("rows", 1, df.rows);
			assertEquals("columns", 10, df.columns);
		} catch (DMasonException e) {
			fail(e.getMessage());
		}
	}

	/**
	 *  Test for square distribution mode not fixed.
	 */
	@Test
	public void testSDMNotFixed() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */false, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			assertEquals("width", 10, df.getWidth());
			assertEquals("height", 10, df.getHeight());
			assertEquals("rows", 10, df.rows);
			assertEquals("columns", 10, df.columns);
		} catch (DMasonException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test for square distribution mode with width height.
	 */
	@Test
	public void testSDMWidthHeight() {
		int width = 125;
		int height = 100;

		/* Celltype
		 * 00 - 01 - 02
		 * 10 - 11 - 12
		 */
		int rows=3;
		int columns=3;

		DDoubleGrid2D[][] regions = new DDoubleGrid2D[rows][columns];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				try {

					df = DDoubleGrid2DFactory.createDDoubleGrid2D( width , height,
							/* simState */new StubDistributedState(),
							/* max_distance */1, /* Celltype i */i,/*Celltype j */j, 
							/* rows */rows, /* columns */columns,
							DistributedField2D.UNIFORM_PARTITIONING_MODE, 
							/* initialGridValue */ 1.0, /* fixed */true, /* name */"testGrid",
							/* topicPrefix */"", /* isToroidal */false);
					regions[i][j] = df;
				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					fail(e.getMessage());
				}
			}
		}
		int effectiveWidth=0;
		int effectiveHeight =0;

		int fixedRow =0;
		for(int j=0; j<columns; j++ )
			effectiveWidth+=regions[fixedRow][j].my_width;

		int fixedColumn =0;
		for(int i=0; i<rows; i++ )
			effectiveHeight+=regions[i][fixedColumn].my_height;


		assertEquals("error for height=" + height, height, effectiveHeight);
		assertEquals("error for width=" + width, width, effectiveWidth);
	}


	
	/**
	 * Test for square distribution mode with 0 width.
	 */
	@Test
	public void testSDM0Width() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */0, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception for width=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode with 0 height.
	 */
	@Test
	public void testSDM0Height() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					0, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception for width=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode with negative height.
	 */
	@Test
	public void testSDMNegativeHeight() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					-3, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("really you can have a height <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode with negative width.
	 */
	@Test
	public void testSDMNegativeWidth() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */-10, /* height */
					3, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("really you can have a width <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for square distribution mode with width max int.
	 */
	@Test
	public void testSDMWidthMaxInt() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(
					/* width */Integer.MAX_VALUE, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for square distribution mode with height max int.
	 */
	@Test
	public void testSDMHeightMaxInt() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					Integer.MAX_VALUE, /* simState */
					new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for square distribution mode with max distance0.
	 */
	@Test
	public void testSDMMaxDistance0() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					0, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("maxdistance=0 is an error");
		} catch (DMasonException e) {
			
		}
	}

	/**
	 * Test for square distribution mode with negative max distance.
	 */
	@Test
	public void testSDMNegativeMaxDistance() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					-1, /* i */0,/* j */1, /* rows */10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative distance???");
		} catch (DMasonException e) {
			// ok
		}
	}


	/**
	 * Test for square distribution mode with negative rows.
	 */
	@Test
	public void testSDMNegativeRows() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */-10, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative rows???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for square distribution mode with negative columns.
	 */
	@Test
	public void testSDMNegativeColumns() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */-10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative columns???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for square distribution mode with 0 columns.
	 */
	@Test
	public void testSDM0Columns() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */10, /* columns */0,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have 0 columns???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for square distribution mode with 0 rows.
	 */
	@Test
	public void testSDM0Rows() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */0, /* columns */10,
					DistributedField2D.UNIFORM_PARTITIONING_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have 0 rows???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 *  test for horizontal balanced distribution mode not fixed.
	 */

	@Test
	public void testHBDMNotFixed() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */false, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			assertEquals("width", 10, df.getWidth());
			assertEquals("height", 10, df.getHeight());
			assertEquals("rows", 1, df.rows);
			assertEquals("columns", 10, df.columns);
		} catch (DMasonException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test for horizontal balanced distribution mode with variable Columns.
	 */
	@Test
	public void testHBDMVariableColoumns() {
		int rows =1; //it is fixed for HM
		int width =241;
		int height =109;

		DDoubleGrid2D[][] ddg =null;
		int effectiveWidth=0,effectiveHeight =0;

		for(int columns=1; columns <= 11; columns++){
			ddg = new DDoubleGrid2D[rows][columns];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++) {
					try {

						df = DDoubleGrid2DFactory
								.createDDoubleGrid2D(
										width,
										height,
										/* simState */new StubDistributedState(),
										/* max_distance */1,
										i,
										j,
										rows,
										columns,
										DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE,
										/* initialGridValue */1.0, /* fixed */true, /* name */"testGrid",
										/* topicPrefix */"", /* isToroidal */false);

						ddg[i][j]=df;
					} catch (DMasonException e) {
						// TODO Auto-generated catch block
						fail(e.getMessage());
					}
				}
			}

			effectiveWidth=0;
			effectiveHeight =0;

			for(int i=0; i<columns; i++)
				effectiveWidth+=ddg[0][i].my_width;

			for(int i=0; i<rows; i++)
				effectiveHeight+=ddg[i][0].my_height;

			assertEquals("error for height=" + height, height, effectiveHeight);
			assertEquals("error for width=" + width, width, effectiveWidth);
		}



	}

	/**
	 * Test for horizontal balanced distribution mode with 0 width.
	 */
	@Test
	public void testHBDM0Width() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */0, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception for width=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with 0 height.
	 */
	@Test
	public void testHBDM0Height() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					0, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception for width=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with negative height.
	 */
	@Test
	public void testHBDMNegativeHeight() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					-3, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("really you can have a height <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with negative width.
	 */
	@Test
	public void testHBDMNegativeWidth() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */-10, /* height */
					3, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("really you can have a width <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal balanced distribution mode with width max int.
	 */
	@Test
	public void testHBDMWidthMaxInt() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(
					/* width */Integer.MAX_VALUE, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal balanced distribution mode with height max int.
	 */
	@Test
	public void testHBDMHeightMaxInt() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					Integer.MAX_VALUE, /* simState */
					new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);
			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal balanced distribution mode with max distance0.
	 */
	@Test
	public void testHBDMMaxDistance0() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					0, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

		fail("maxdistance=0 is an error");
		} catch (DMasonException e) {
		
		}
	}

	/**
	 * Test horizontal distribution mode more of 1 row.
	 */
	@Test
	public void testHorizontalDistributionModeMoreOf1Row() {

		try {

			GeneralParam genParam = new GeneralParam(
					/* width */10,
					/* height */10,
					/* maxDistance */10,
					/* rows */2,
					/* columns */2,
					/* numAgents */1,
					/* mode */DistributedField2D.UNIFORM_PARTITIONING_MODE,
					ConnectionType.pureActiveMQ);

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */12, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("horizontal mode can not have more than one column");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal distribution mode with negative max distance.
	 */
	@Test
	public void testHBDMNegativeMaxDistance() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					-1, /* i */0,/* j */1, /* rows */1, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative distance???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal distribution mode with rows columns.
	 */
	@Test
	public void testHBDMRowsColumns() {

		try {

			for (int j = 1; j < numLoop; j++) {

				df = DDoubleGrid2DFactory
						.createDDoubleGrid2D(
								/* width */10, /* height */
								10, /* simState */
								new StubDistributedState(),/* max_distance */
								1, /* i */
								0,/* j */
								0, /* rows */
								1, /* columns */
								j,
								DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
								1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
								"", /* isToroidal */false);

			}

		} catch (DMasonException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test for horizontal distribution mode with negative rows.
	 */
	@Test
	public void testHBDMNegativeRows() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */-10, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative rows???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal distribution mode with negative columns.
	 */
	@Test
	public void testHBDMNegativeColumns() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */-10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have a negative columns???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal distribution mode with 0 columns.
	 */
	@Test
	public void testHBDM0Columns() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */1, /* columns */0,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have 0 columns???");
		} catch (DMasonException e) {
			// ok
		}
	}

	/**
	 * Test for horizontal distribution mode with 0 rows.
	 */
	@Test
	public void testHBDM0Rows() {

		try {

			df = DDoubleGrid2DFactory.createDDoubleGrid2D(/* width */10, /* height */
					10, /* simState */new StubDistributedState(),/* max_distance */
					1, /* i */0,/* j */1, /* rows */0, /* columns */10,
					DistributedField2D.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1.0, /* fixed */true, /* name */"testGrid", /* topicPrefix */
					"", /* isToroidal */false);

			fail("really you can have 0 rows???");
		} catch (DMasonException e) {
			// ok
		}
	}

}
