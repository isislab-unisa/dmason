/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package it.isislab.dmason.sim.field.continuous;

import it.isislab.dmason.experimentals.sim.field.support.loadbalancing.MyCellInterface;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.VisualizationUpdateMap;
import it.isislab.dmason.sim.field.CellType;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field.continuous.region.RegionDouble;
import it.isislab.dmason.sim.field.support.field2D.UpdateMap;
import it.isislab.dmason.sim.field.support.field2D.region.Region;
import it.isislab.dmason.sim.field.support.field2D.region.RegionMap;

import java.util.ArrayList;
import java.util.HashMap;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/**
 *  Abstract class for Distributed Continuous 2D
 *  
 *  <h3>This Field extends Continuous2D, to be used in a distributed environment. 
 *  All the necessary informations for the distribution of simulation are wrapped in this class.</h3>
 *  It represents the field managed by a single peer.
 *  It adds to the superclass these following informations:
 *  <ul>
 *  <li> Upper Left corner's coordinates</li>
 *  <li> Width and Height of the Field of expertise</li>
 *  <li> The logic of synchronization among the peers, step by step</li>
 *  <li> The number of peers involved in the simulation</li>
 *  <li> The maximum distance of shift for the agents</li>
 *  <li> An arraylist for the neighborhoods topics</li>
 *  <li> A Region that represents the field to simulate</li>
 *  <li> A RegionMap that represents all the border Regions</li>
 *  <li> The simstate of sumulation</li>
 *  <li> An UpdateMap for all the updates</li>
 *  <li> A Connection object for an abstract connection</li>
 *  <li> A CellType object for differentiate the field</li>
 *  </ul>
 *  
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 *  
 *  
 */
public abstract class DContinuousGrid2D extends Continuous2D  implements DistributedField2D<Double2D>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Stores the coordinates of this distributed field relative to the
	 * global field. For instance, assume a global field sized 200x100 units
	 * (width * height). If we split the global field in two distributed field,
	 * we'll obtain a field with <code>cellType</code> 0-0 and a field with
	 * <code>cellType</code> 0-1, each sized 100x100 units. Please note that
	 * a <code>cellType</code> i-j means that the cell is at coordinates (i, j),
	 * where <i>i</i> is the row number, <i>j</i> is the column number.
	 */
	public CellType cellType;

	public int rows;

	public int columns;

	/**
	 * x coordinate of north-west corner.
	 */
	public double own_x;

	/**
	 * y coordinate of north-west corner.
	 */
	public double own_y;

	/**
	 * This field's width.
	 */
	public double my_width;

	/**
	 * This field's height.
	 */
	public double my_height;



	/** Will contain globals parameters */
	public VisualizationUpdateMap<String, Object> globals = new VisualizationUpdateMap<String, Object>();
	public RegionDouble myfield;
	public RegionMap<Double,Double2D> rmap=new RegionMap<Double,Double2D>();
	public ArrayList<Region<Double, Double2D>> updates_cache;
	public int AOI; //area of interest of an agent
	public UpdateMap<Double,Double2D> updates=new UpdateMap<Double,Double2D>();
	public HashMap<Integer,HashMap<CellType, MyCellInterface>> listGrid;
	public ArrayList<ArrayList<Region<Double, Double2D>>> updates_cacheLB;
	public SimState sm;
	public ArrayList<String> neighborhood=new ArrayList<String>();
	public boolean gui=true;
	private static boolean isToroidal;
	
	public DContinuousGrid2D(double discretization, double width, double height) 
	{
		super(discretization, width, height);
	}

	

	public boolean isToroidal()
	{
		return isToroidal;
	}

	public void setToroidal(boolean isToroidal)
	{	
		this.isToroidal=isToroidal;
	}

}