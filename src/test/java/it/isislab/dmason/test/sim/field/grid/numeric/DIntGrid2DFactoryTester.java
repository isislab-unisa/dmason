package it.isislab.dmason.test.sim.field.grid.numeric;

import static org.junit.Assert.*;

import org.junit.Test;

import sim.engine.SimState;
import sim.util.Double2D;
import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemotePositionedAgent;
import it.isislab.dmason.sim.field.DistributedField;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2D;
import it.isislab.dmason.sim.field.grid.numeric.DIntGrid2DFactory;
import it.isislab.dmason.sim.field.grid.sparse.DSparseGrid2DFactory;
import it.isislab.dmason.tools.batch.data.GeneralParam;
import it.isislab.dmason.util.connection.ConnectionType;


// TODO: Auto-generated Javadoc
/**
 * Test the Class DIntGrid2DFactory.
 * @author Michele Carillo
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Mario Capuozzo
 */
public class DIntGrid2DFactoryTester {

	/** The grid. */
	DIntGrid2D dint;

	/** The num loop. */
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
	 *  TESTS FOR createDIntGrid2D.
	 */

	/**
	 * Test for the horizontal distribution mode with different width and height
	 */

	@Test
	public void testHorizontalDistributionModeWidthHeight() {

		for (int i = 1; i < numLoop; i++) {
			for (int j = 1; j < numLoop; j++) {
				try {

					dint = DIntGrid2DFactory.createDIntGrid2D(/* width */i, /* height */
							j, /* SimState */new StubDistributedState(),/* max_distance */
							1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
							DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
							1, /* fixed */true, /* name */"test",/* topicPrefix */
							"",/* isToroidal */false);

					assertEquals("error for width=" + i, i, dint.getWidth());
					assertEquals("error for height=" + j, j, dint.getHeight());

				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					fail(e.getMessage());
				}
			}
		}
	}

	/**
	 * Test for the horizontal distribution mode with 0 width.
	 */
	@Test
	public void testHorizontalDistributionMode0Width() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */0, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception for width=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with 0 height.
	 */
	@Test
	public void testHorizontalDistributionMode0Height() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					0, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception for height=0");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with negative height.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeHeight() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					-3, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a height <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with negative width.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeWidth() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */-10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a width <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode width max int.
	 */
	@Test
	public void testHorizontalDistributionModeWidthMaxInt() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(
					/* width */Integer.MAX_VALUE, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with height max int.
	 */
	@Test
	public void testHorizontalDistributionModeHeightMaxInt() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					Integer.MAX_VALUE, /* SimState */
					new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with negative max distance.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeMaxDistance() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					-1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a negative distance???");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with more of1 row.
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
					/* mode */DistributedField2D.HORIZONTAL_DISTRIBUTION_MODE,
					ConnectionType.pureActiveMQ);

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */2, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("horizontal mode can not have more than one column");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with rows columns.
	 */
	@Test
	public void testHorizontalDistributionModeRowsColumns() {

		try {

			for (int j = 1; j < numLoop; j++) {

				dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
						10, /* SimState */new StubDistributedState(),/* max_distance */
						1, /* i */0, /* j */0, /* rows */1, /* columns */j, /* MODE */
						DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
						1, /* fixed */true, /* name */"test",/* topicPrefix */
						"",/* isToroidal */false);

				assertEquals(j, dint.columns);

			}
		} catch (DMasonException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Test for the horizontal distribution mode with negative rows.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeRows() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */-10, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have a negative rows???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with negative columns.
	 */
	@Test
	public void testHorizontalDistributionModeNegativeColumns() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */-10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have a negative columns???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with 0 columns.
	 */
	@Test
	public void testHorizontalDistributionMode0Columns() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */0, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have 0 columns???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for the horizontal distribution mode with 0 rows.
	 */
	@Test
	public void testHorizontalDistributionMode0Rows() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */0, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */false, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			assertEquals("width", 10, dint.getWidth());
			assertEquals("height", 10, dint.getHeight());
			assertEquals("rows", 1, dint.rows);
			assertEquals("columns", 10, dint.columns);

		} catch (DMasonException e) {
			fail(e.getMessage());
		}

	}

	/**
	 *  Test for square distribution mode with different width and height.
	 */

	@Test
	public void testSDMWidthHeight() {

		for (int i = 1; i < numLoop; i++) {
			for (int j = 1; j < numLoop; j++) {
				try {

					dint = DIntGrid2DFactory.createDIntGrid2D(/* width */i, /* height */
							j, /* SimState */new StubDistributedState(),/* max_distance */
							1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
							DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
							1, /* fixed */true, /* name */"test",/* topicPrefix */
							"",/* isToroidal */false);

					assertEquals("error for width=" + i, i, dint.getWidth());
					assertEquals("error for height=" + j, j, dint.getHeight());

				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					fail(e.getMessage());
				}
			}
		}
	}

	/**
	 * Test for square distribution mode with 0 width.
	 */
	@Test
	public void testSDM0Width() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */0, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					0, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception for height=0");

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					-3, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */-10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a width <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode width max int.
	 */
	@Test
	public void testSDMWidthMaxInt() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(
					/* width */Integer.MAX_VALUE, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					Integer.MAX_VALUE, /* SimState */
					new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode with negative max distance.
	 */
	@Test
	public void testSDMNegativeMaxDistance() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					-1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a negative distance???");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode with different rows columns.
	 */
	@Test
	public void testSDMRowsColumns() {

		try {

			for (int i = 1; i < numLoop; i++) {
				int j=i;
				//for (int j = 1; j < numLoop; j++) {

					dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
							10, /* SimState */new StubDistributedState(),/* max_distance */
							1, /* i */0, /* j */0, /* rows */i, /* columns */j, /* MODE */
							DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
							1, /* fixed */true, /* name */"test",/* topicPrefix */
							"",/* isToroidal */false);
					assertEquals(i, dint.rows);
					assertEquals(j, dint.columns);
				//}
			}
		} catch (DMasonException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Test for square distribution mode with negative rows.
	 */
	@Test
	public void testSDMNegativeRows() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */-10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */-10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */0, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */0, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have 0 rows???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for square distribution mode not fixed.
	 */
	@Test
	public void testSDMNotFixed() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.SQUARE_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */false, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			assertEquals("width", 10, dint.getWidth());
			assertEquals("height", 10, dint.getHeight());
			assertEquals("rows", 10, dint.rows);
			assertEquals("columns", 10, dint.columns);

		} catch (DMasonException e) {
			fail(e.getMessage());
		}

	}

	/**
	 *  Test for horizontal balanced distribution mode with differents width and height.
	 */

	@Test
	public void testHBDMWidthHeight() {

		for (int i = 1; i < numLoop; i++) {
			for (int j = 1; j < numLoop; j++) {
				try {

					dint = DIntGrid2DFactory
							.createDIntGrid2D(
									/* width */i, /* height */
									j, /* SimState */
									new StubDistributedState(),/* max_distance */
									1, /* i */
									0, /* j */
									0, /* rows */
									1, /* columns */
									10, /* MODE */
									DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
									1, /* fixed */true, /* name */"test",/* topicPrefix */
									"",/* isToroidal */false);

					assertEquals("error for width=" + i, i, dint.getWidth());
					assertEquals("error for height=" + j, j, dint.getHeight());

				} catch (DMasonException e) {
					// TODO Auto-generated catch block
					fail(e.getMessage());
				}
			}
		}
	}

	/**
	 * Test for horizontal balanced distribution mode with 0 width.
	 */
	@Test
	public void testHBDM0Width() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */0, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					0, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception for height=0");

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					-3, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */-10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a width <0?!?!?");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with max int.
	 */
	@Test
	public void testHBDMWidthMaxInt() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(
					/* width */Integer.MAX_VALUE, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

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

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					Integer.MAX_VALUE, /* SimState */
					new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("should throw an exception");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with negative max distance.
	 */
	@Test
	public void testHBDMNegativeMaxDistance() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					-1, /* i */0, /* j */0, /* rows */1, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);

			fail("really you can have a negative distance???");

		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with rows columns.
	 */
	@Test
	public void testHBDMRowsColumns() {

		try {

			for (int j = 1; j < numLoop; j++) {

				dint = DIntGrid2DFactory
						.createDIntGrid2D(
								/* width */10, /* height */
								10, /* SimState */
								new StubDistributedState(),/* max_distance */
								1, /* i */
								0, /* j */
								0, /* rows */
								1, /* columns */
								j, /* MODE */
								DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
								1, /* fixed */true, /* name */"test",/* topicPrefix */
								"",/* isToroidal */false);

				assertEquals(j, dint.columns);

			}
		} catch (DMasonException e) {
			fail(e.getMessage());
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with negative rows.
	 */
	@Test
	public void testHBDMNegativeRows() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */-10, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have a negative rows???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with negative columns.
	 */
	@Test
	public void testHBDMNegativeColumns() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */-10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have a negative columns???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with 0 columns.
	 */
	@Test
	public void testHBDM0Columns() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */1, /* columns */0, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have 0 columns???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode with 0 rows.
	 */
	@Test
	public void testHBDM0Rows() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */0, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */true, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			fail("really you can have 0 rows???");
		} catch (DMasonException e) {
			// ok
		}

	}

	/**
	 * Test for horizontal balanced distribution mode not fixed.
	 */
	@Test
	public void testHBDMNotFixed() {

		try {

			dint = DIntGrid2DFactory.createDIntGrid2D(/* width */10, /* height */
					10, /* SimState */new StubDistributedState(),/* max_distance */
					1, /* i */0, /* j */0, /* rows */10, /* columns */10, /* MODE */
					DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
					1, /* fixed */false, /* name */"test",/* topicPrefix */
					"",/* isToroidal */false);
			assertEquals("width", 10, dint.getWidth());
			assertEquals("height", 10, dint.getHeight());
			assertEquals("rows", 10, dint.rows);
			assertEquals("columns", 10, dint.columns);

		} catch (DMasonException e) {
			fail(e.getMessage());
		}

	}

	/**
	 *  Test for square balanced distribution mode width and height multiple of 6 colums and rows.
	 */

	@Test
	public void testSquareBalancedDistributionModeX6() {

		for (int col = 10; col < numLoop; col++) {
			for (int row = 10; row < numLoop; row++) {
				try {

					dint = DIntGrid2DFactory
							.createDIntGrid2D(
									/* width */6 * col, /* height */
									6 * row, /* SimState */
									new StubDistributedState(),/* max_distance */
									1, /* i */
									0, /* j */
									0, /* rows */
									1, /* columns */
									10, /* MODE */
									DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
									1, /* fixed */false, /* name */"test",/* topicPrefix */
									"",/* isToroidal */false);

				} catch (DMasonException e) {
					fail(e.getMessage());
				}
			}
		}
	}

	/**
	 * Test square balanced distribution mode width and height multiple of 3 colums and rows.
	 */
	@Test
	public void testSquareBalancedDistributionModeX3() {

		for (int col = 10; col < numLoop; col++) {
			for (int row = 10; row < numLoop; row++) {
				try {

					dint = DIntGrid2DFactory
							.createDIntGrid2D(
									/* width */3 * col, /* height */
									3 * row, /* SimState */
									new StubDistributedState(),/* max_distance */
									1, /* i */
									0, /* j */
									0, /* rows */
									1, /* columns */
									10, /* MODE */
									DIntGrid2DFactory.HORIZONTAL_BALANCED_DISTRIBUTION_MODE, /* initialGridValue */
									1, /* fixed */false, /* name */"test",/* topicPrefix */
									"",/* isToroidal */false);

				} catch (DMasonException e) {
					fail(e.getMessage());
				}
			}
		}
	}

}
