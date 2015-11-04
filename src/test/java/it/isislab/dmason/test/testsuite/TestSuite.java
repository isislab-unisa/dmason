package it.isislab.dmason.test.testsuite;

import it.isislab.dmason.test.sim.app.DAntsForage.TestDAntsForage;
import it.isislab.dmason.test.sim.app.DFlockers.TestDFlockers;
import it.isislab.dmason.test.sim.app.DParticles.TestDParticles;
import it.isislab.dmason.test.sim.engine.DistributedStateConnectionJMSTester;
import it.isislab.dmason.test.sim.field.CellTypeTester;
import it.isislab.dmason.test.sim.field.UpdateCellTester;
import it.isislab.dmason.test.sim.field.continuous.DContinuous2DFactoryTester;
import it.isislab.dmason.test.sim.field.continuous.DContinuous2DXYTester;
import it.isislab.dmason.test.sim.field.continuous.loadbalanced.DContinuous2DXYLBTester;
import it.isislab.dmason.test.sim.field.continuous.region.RegionDoubleLBTester;
import it.isislab.dmason.test.sim.field.continuous.region.RegionDoubleTester;
import it.isislab.dmason.test.sim.field.continuous.thin.DContinuous2DXYThinTester;
import it.isislab.dmason.test.sim.field.grid.numeric.DDoubleGrid2DFactoryTester;
import it.isislab.dmason.test.sim.field.grid.numeric.DDoubleGrid2DXYTester;
import it.isislab.dmason.test.sim.field.grid.numeric.DIntGrid2DFactoryTester;
import it.isislab.dmason.test.sim.field.grid.numeric.DIntGrid2DXYTester;
import it.isislab.dmason.test.sim.field.grid.numeric.loadbalanced.DDoubleGrid2DXYLBTester;
import it.isislab.dmason.test.sim.field.grid.numeric.loadbalanced.DIntGrid2DXYLBTester;
import it.isislab.dmason.test.sim.field.grid.numeric.region.RegionDoubleNumericTester;
import it.isislab.dmason.test.sim.field.grid.numeric.region.RegionIntegerNumericTester;
import it.isislab.dmason.test.sim.field.grid.numeric.thin.DDoubleGrid2DXYThinTester;
import it.isislab.dmason.test.sim.field.grid.numeric.thin.DIntGrid2DXYThinTester;
import it.isislab.dmason.test.sim.field.grid.region.RegionIntegerLBTester;
import it.isislab.dmason.test.sim.field.grid.region.RegionIntegerTester;
import it.isislab.dmason.test.sim.field.grid.sparse.DSparse2DFactoryTester;
import it.isislab.dmason.test.sim.field.grid.sparse.DSparseGrid2DXYTester;
import it.isislab.dmason.test.sim.field.grid.sparse.loadbalanced.DSparseGrid2DXYLBTester;
import it.isislab.dmason.test.sim.field.grid.sparse.thin.DSparseGrid2DXYThinTester;
import it.isislab.dmason.test.sim.field.support.field2D.UpdateMapTester;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The Class TestSuite. Call all classes of tests.
 * 
 * @author Mario Capuozzo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 * @author Michele Carillo
 */
@RunWith(Suite.class)
@SuiteClasses({
	
	/*******Simulations**************/
	//TestDFlockers.class,
	//TestDParticles.class,
	//TestDAntsForage.class,
	
	/********Grid Factory*************/
	/*DSparse2DFactoryTester.class,		
	DContinuous2DFactoryTester.class,
	DIntGrid2DFactoryTester.class,
	DDoubleGrid2DFactoryTester.class,
	*/
	/*********************/
	/*CellTypeTester.class,
	UpdateCellTester.class, 		
	UpdateMapTester.class,
*/
	/*********Connection************/
	//DistributedStateConnectionJMSTester.class,
	
	/*****Distributed Fields****************/
	//DSparseGrid2DXYTester.class,
	//DContinuous2DXYTester.class,
	//DIntGrid2DXYTester.class,
	//DDoubleGrid2DXYTester.class,

	/*****Distrubuted Fields Load Balancing ****************/
    //DSparseGrid2DXYLBTester.class,		
	//DContinuous2DXYLBTester.class,		
	//DIntGrid2DXYLBTester.class,
	//DDoubleGrid2DXYLBTester.class,
	
	/*******Regions**************/
	//RegionIntegerTester.class,		
	//RegionDoubleTester.class,
    //RegionDoubleNumericTester.class,
	//RegionIntegerNumericTester.class,
	
	/*******Load Balancing Regions**************/
	//RegionIntegerLBTester.class,
	//RegionDoubleLBTester.class,		
	
	/*******Thin Distributed Fields**************/
	//DSparseGrid2DXYThinTester.class,
	//DContinuous2DXYThinTester.class,
	//DIntGrid2DXYThinTester.class,
	//DDoubleGrid2DXYThinTester.class		
})
public class TestSuite{

	//"	 ****************************************************************************"
	//"	 *  The following classes will be test:										*"
	//"	 * 																			*"
	//"	 * 																			*"
	//"	 * - UpdateCell (dmason.sim.field)											*"
	//"	 * - CellType (dmason.sim.field)											*"
	//"	 * - UpdateMap (dmason.sim.field.support.field2D)							*"
	//"	 * 																			*"
	//"	 * - RegionDouble (dmason.sim.field.continuous.region)						*"
	//"	 * - RegionDoubleLB (dmason.sim.field.continuous.region)					*"
	//"	 * - RegionDoubleNumeric (dmason.sim.field.grid.numeric.region)				*"
	//"	 * - RegionIntNumeric (dmason.sim.field.grid.numeric.region)				*"
	//"	 * - RegionInteger (dmason.sim.field.grid.region)							*"
	//"	 * - RegionIntegerLB (dmason.sim.field.grid.region)							*"
	//"	 * 																			*"
	//"	 * - Dcontinuous2DFactory (dmason.sim.field.continuous)						*"
	//"	 * - DContinuous2DXY (dmason.sim.field.continuous)							*"
	//"	 * - DContinuous2DXYLB (dmason.sim.field.continuous.loadbalanced)			*"
	//"	 * - DContinuous2DXYThin (dmason.sim.field.continuous.thin)					*"
	//"	 * 																			*"
	//"	 * - DDoubleGrid2DFactory (dmason.sim.field.grid.numeric)					*"
	//"	 * - DIntGrid2DFactory (dmason.sim.field.grid.numeric)						*"
	//"	 * - DDoubleGrid2DXY (dmason.sim.field.grid.numeric)						*"
	//"	 * - DIntGrid2DXY (dmason.sim.field.grid.numeric)							*"
	//"	 * - DDoubleGrid2DXYLB (dmason.sim.field.grid.numeric.loadbalanced)			*"
	//"	 * - DIntGrid2DXYLB (dmason.sim.field.grid.numeric.loadbalanced)			*"
	//"	 * - DDoubleGrid2DXYThin (dmason.sim.field.grid.numeric.thin)				*"
	//"	 * - DIntGrid2DXYThin (dmason.sim.field.grid.numeric.thin)					*"
	//"	 * 																			*"
	//"	 * - DSparseGrid2DXY (dmason.sim.field.grid.sparse) 						*"
	//"	 * - DSparseGrid2DXYLB (dmason.sim.field.grid.sparse.loadbalanced)			*"
	//"	 * - DSparseGrid2DXYThin (dmason.sim.field.grid.sparse.thin)				*"
	//"	 * 																			*"
	//"	 * - DistributedStateConnectionJMS(dmason.sim.engine)						*"
	//"	 * 																			*"
	//"	 * - DFlockers (dmason.sim.app.dFlokers)									*"
	//"	 * 																			*"
	//"	 ****************************************************************************"
}
