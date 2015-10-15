package it.isislab.dmason.test.sim.field.continuous;

import static org.junit.Assert.*;

import org.junit.Test;

import sim.engine.SimState;
import sim.util.Double2D;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.continuous.DContinuous2D;
import it.isislab.dmason.sim.field.continuous.DContinuous2DFactory;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;

// TODO: Auto-generated Javadoc
/**
 * Test the Class DContinuous2DFactory.
 * 
 * @author Mario Capuozzo
 */
public class DContinuous2DFactoryTester {

	/** The distributed continuous. */
	DContinuous2D dcon;
	
	/** The num of loop for the tests. */
	int numLoop = 100;

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

		/* (non-Javadoc)
		 * @see it.isislab.dmason.sim.engine.DistributedState#setPortrayalForObject(java.lang.Object)
		 */
		@Override
		public boolean setPortrayalForObject(Object o) {
			// TODO Auto-generated method stub
			return false;
		}

	}


	/**
	 * test for horizontal distribution mode with int width and height
	 */

	@Test
	public void testHorizontalDistributionModeIntWidthHeight() {

		for (int i = 1; i < numLoop; i++) {
			for (int j = 1; j < numLoop; j++) {
				try {

					GeneralParam genParam = new GeneralParam(
							/* width */i,
							/* height */j,
							/* maxDistance */10,
							/* rows */1,
							/* columns */2,
							/* numAgents */1,
							/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
							ConnectionType.pureActiveMQ);

					dcon = DContinuous2DFactory.createDContinuous2D(
					/* discretization */1.0 / 1.5,/* width */i,/* height */j,/*
																		 * DistributedState
																		 * but
																		 * is
																		 * SimState
																		 */
							new StubDistributedState(genParam),/* maxDistance */
							10, /* i */
							0, /* j */
							0,/* rows */1,/* colums */2,/* mode */
							DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
							"test", /* topicPrefix */"",/* isToroidal */true);

					assertEquals("error for height=" + j, j, (int) dcon.height);
					assertEquals("error for width=" + i, i, (int) dcon.width);
				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					fail(e.getMessage());
				}
			}
		}
	}

	/**
	 * Test horizontal distribution mode with 0 width.
	 */
	@Test
	public void testHorizontalDistributionMode0Width() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */0,
			/* height */10,
			/* maxDistance */10,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */0,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("should throw an exception for width=0");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with 0 height.
	 */
	@Test
	public void testHorizontalDistributionMode0Height() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */0,
			/* maxDistance */10,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */0,/*
																 * DistributedState
																 * but is
																 * SimState
																 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("should throw an exception for height=0");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with negative width.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeWidth() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */-10,
			/* height */10,
			/* maxDistance */10,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */-10,/* height */10,/*
																		 * DistributedState
																		 * but
																		 * is
																		 * SimState
																		 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("should throw an exception for width<0");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with negative height.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeHeight() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */-10,
			/* maxDistance */10,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */-10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("should throw an exception for width<0");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode width max double.
	 */
	@Test
	public void testHorizontalDistributionModeWidthMaxDouble() {

		double app = Double.MAX_VALUE;
		try {

			GeneralParam genParam = new GeneralParam(
			/* width */(int) app,
			/* height */10,
			/* maxDistance */10,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */app,/* height */10,/*
																		 * DistributedState
																		 * but
																		 * is
																		 * SimState
																		 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);
			fail("should throw an exception");
		} catch (DMasonException e) {

		}

	}

	/**
	 * Test horizontal distribution mode with height= max double.
	 */
	@Test
	public void testHorizontalDistributionModeHeightMaxDouble() {

		double app = Double.MAX_VALUE;
		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */(int) app,
			/* maxDistance */10,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */app,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);
			fail("should throw an exception");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode max distance.
	 */
	@Test
	public void testHorizontalDistributionModeMaxDistance() {

		int app = Integer.MAX_VALUE;
		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */app,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */app, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);
			fail("max_distance exceeds max integer value");
		} catch (DMasonException e) {
			//ok
		}

	}

	/**
	 * Test horizontal distribution mode with negative distance.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeDistance() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */-3,
			/* rows */1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */-3, /* i */
					0, /* j */
					0,/* rows */1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("really you can measure a distance with a negative number??");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with different rows and colums.
	 */
	@Test
	public void testHorizontalDistributionModeRowsColums() {

		for (int j = 1; j < numLoop; j++) {
			try {

				GeneralParam genParam = new GeneralParam(
				/* width */10,
				/* height */10,
				/* maxDistance */10,
				/* rows */1,
				/* columns */j,
				/* numAgents */1,
				/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
						ConnectionType.pureActiveMQ);

				dcon = DContinuous2DFactory.createDContinuous2D(
				/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																		 * DistributedState
																		 * but
																		 * is
																		 * SimState
																		 */
						new StubDistributedState(genParam),/* maxDistance */1, /* i */
						0, /* j */
						0,/* rows */1,/* colums */j,/* mode */
						DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
						"test", /* topicPrefix */"",/* isToroidal */true);

				assertEquals("rows value failure",1, dcon.rows);
				assertEquals("columns value failure",j, dcon.columns);
				
				
			} catch (DMasonException e) {
				fail(e.getMessage());
			}
		}

	}

	/**
	 * Test horizontal distribution mode with 0 rows.
	 */
	@Test
	public void testHorizontalDistributionMode0Rows() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */10,
			/* rows */0,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */1, /* i */
					0, /* j */
					0,/* rows */0,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("really you can have 0 rows??");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with more of 1 row.
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
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */1, /* i */
					0, /* j */
					0,/* rows */2,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("horizontal mode can not have more than one column");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with 0 colums.
	 */
	@Test
	public void testHorizontalDistributionMode0Colums() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */10,
			/* rows */1,
			/* columns */0,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */1, /* i */
					0, /* j */
					0,/* rows */1,/* colums */0,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("really you can have 0 colums??");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with negative rows.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeRows() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */10,
			/* rows */-1,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */1, /* i */
					0, /* j */
					0,/* rows */-1,/* colums */2,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("really you can have 0 rows??");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test horizontal distribution mode with negative colums.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeColums() {

		try {

			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */10,
			/* rows */1,
			/* columns */-1,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */1, /* i */
					0, /* j */
					0,/* rows */1,/* colums */-1,/* mode */
					DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			fail("really you can have negative colums??");
		} catch (DMasonException e) {
			// ok
		}

	}



	/**
	 *general test for square distribution mode
	 */

	@Test
	public void testSquareDistributionMode() {
		try {
			GeneralParam genParam = new GeneralParam(
			/* width */10,
			/* height */10,
			/* maxDistance */10,
			/* rows */2,
			/* columns */2,
			/* numAgents */1,
			/* mode */DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dcon = DContinuous2DFactory.createDContinuous2D(
			/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																	 * DistributedState
																	 * but is
																	 * SimState
																	 */
					new StubDistributedState(genParam),/* maxDistance */10, /* i */
					0, /* j */
					0,/* rows */2,/* colums */2,/* mode */
					DContinuous2DFactory.SQUARE_DISTRIBUTION_MODE, /* name */
					"test", /* topicPrefix */"",/* isToroidal */true);

			//fail("errore");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test for square balanced distribution mode with maxdistance = 0 and width and height multiple of 6 colums and rows.
	 */

	@Test
	public void testSquareBalancedDistributionModeX6() {
        
		//for (int col = 10; col < numLoop; col++) {
			for (int row = 10; row < numLoop; row++) {
				try {
                    int col=row;
					GeneralParam genParam = new GeneralParam(
							/* width */6 * col,
							/* height */6 * row,
							/* maxDistance */0,
							/* rows */row,
							/* columns */col,
							/* numAgents */1,
							/* mode */DContinuous2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE,
							ConnectionType.pureActiveMQ);

					dcon = DContinuous2DFactory
							.createDContinuous2D(
									/* discretization */1.0 / 1.5,/* width */
									6 * col,/* height */
									6 * row,/*
											 * DistributedState but is SimState
											 */
									new StubDistributedState(genParam),/* maxDistance */
									0, /* i */
									0, /* j */
									0,/* rows */
									row,/* colums */
									col,/* mode */
									DContinuous2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE, /* name */
									"test", /* topicPrefix */"",/* isToroidal */
									true);

				} catch (DMasonException e) {
					fail(e.getMessage());
				}
			}
		//}
	}

	/**
	 * Test square balanced distribution mode with max_distance = 0 and width and height multiple of 3 colums and rows.
	 */
	@Test
	public void testSquareBalancedDistributionModeX3() {

		// fail because safezone/2 return 0 but should be 0.5

		//for (int col = 10; col < numLoop; col++) {
			for (int row = 10; row < numLoop; row++) {
				try {
                    int col=row;
					GeneralParam genParam = new GeneralParam(
							/* width */3 * col,
							/* height */3 * row,
							/* maxDistance */0,
							/* rows */row,
							/* columns */col,
							/* numAgents */1,
							/* mode */DSparseGrid2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE,
							ConnectionType.pureActiveMQ);

					dcon = DContinuous2DFactory
							.createDContinuous2D(
									/* discretization */1.0 / 1.5,/* width */
									3 * col,/* height */
									3 * row,/*
											 * DistributedState but is SimState
											 */
									new StubDistributedState(genParam),/* maxDistance */
									0, /* i */
									0, /* j */
									0,/* rows */
									row,/* colums */
									col,/* mode */
									DContinuous2DFactory.SQUARE_BALANCED_DISTRIBUTION_MODE, /* name */
									"test", /* topicPrefix */"",/* isToroidal */
									true);

				} catch (DMasonException e) {
					fail(e.getMessage());
				}
			}
		//}
	}

	/**
	 * test for horizontal balanced distribution mode with different rows and colums.
	 */

	@Test
	public void testHorizontalBalancedDistributionModeRowsColums() {

		for (int j = 1; j < numLoop; j++) {
			try {

				GeneralParam genParam = new GeneralParam(
						/* width */10,
						/* height */10,
						/* maxDistance */1,
						/* rows */1,
						/* columns */j,
						/* numAgents */1,
						/* mode */DSparseGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE,
						ConnectionType.pureActiveMQ);
				
				dcon = DContinuous2DFactory
						.createDContinuous2D(
								/* discretization */1.0 / 1.5,/* width */
								10,/* height */
								10,/*
									 * DistributedState but is SimState
									 */
								new StubDistributedState(genParam),/* maxDistance */
								1, /* i */
								0, /* j */
								0,/* rows */
								1,/* colums */
								j,/* mode */
								DContinuous2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* name */
								"test", /* topicPrefix */"",/* isToroidal */
								true);

				assertEquals(1, dcon.rows);
				assertEquals(j, dcon.columns);

			} catch (DMasonException e) {
				fail(e.getMessage());
			}
		}

	}

	/**
	 * test for HORIZONTAL_DISTRIBUTION_MODE with different rows and colums
	 */

	@Test
	public void testThHorizontalDistributionModeRowsColums() {

		for (int j = 1; j < numLoop; j++) {
			try {

				GeneralParam genParam = new GeneralParam(
				/* width */10,
				/* height */10,
				/* maxDistance */1,
				/* rows */1,
				/* columns */j,
				/* numAgents */1,
				/* mode */DSparseGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE,
						ConnectionType.pureActiveMQ);

				dcon = DContinuous2DFactory.createDContinuous2DThin(
				/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																		 * DistributedState
																		 * but
																		 * is
																		 * SimState
																		 */
						new StubDistributedState(genParam),/* maxDistance */1, /* i */
						0, /* j */
						0,/* rows */1,/* colums */j,/* mode */
						DContinuous2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* name */
						"test", /* topicPrefix */"",/* isToroidal */true);

				assertEquals(1, dcon.rows);
				assertEquals(j, dcon.columns);

			} catch (DMasonException e) {
				fail(e.getMessage());
			}
		}

	}

	/**
	 * test for thin constructor SQUARE_DISTRIBUTION_MODE.
	 */

	@Test
	public void testThSquareDistributionModeRowsColums() {

		for (int i = 1; i < numLoop; i++) {
			for (int j = 1; j < numLoop; j++) {
				try {

					GeneralParam genParam = new GeneralParam(
					/* width */10,
					/* height */10,
					/* maxDistance */1,
					/* rows */i,
					/* columns */j,
					/* numAgents */1,
					/* mode */DSparseGrid2DFactory.SQUARE_DISTRIBUTION_MODE,
							ConnectionType.pureActiveMQ);

					dcon = DContinuous2DFactory.createDContinuous2DThin(
					/* discretization */1.0 / 1.5,/* width */10,/* height */10,/*
																			 * DistributedState
																			 * but
																			 * is
																			 * SimState
																			 */
							new StubDistributedState(genParam),/* maxDistance */
							1, /* i */
							0, /* j */
							0,/* rows */i,/* colums */j,/* mode */
							DContinuous2DFactory.SQUARE_DISTRIBUTION_MODE, /* name */
							"test", /* topicPrefix */"",/* isToroidal */true);

					assertEquals(i, dcon.rows);
					assertEquals(j, dcon.columns);

				} catch (DMasonException e) {
					fail(e.getMessage());
				}
			}
		}
	}

}
