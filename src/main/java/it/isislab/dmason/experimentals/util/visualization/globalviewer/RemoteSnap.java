/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.experimentals.util.visualization.globalviewer;

import it.isislab.dmason.sim.field.CellType;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class holds the informations that every region sends
 * to a possibly active viewer.
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Luca Vicidomini
 *
 */
public class RemoteSnap implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * Coordinates of the region that generated this snap 
	 */
	public short i, j;
	
	/**
	 * Top and left coordinates of the image.
	 */
	/*
	 * When the Global Inspector collects all the bitmap from the fields, it
	 * have to compose the global image. Previously, every bitmap was shifted
	 * by x*field_width and y*field_height, assuming that every field had same
	 * dimensions. When using load balancing, field width and/or height may
	 * vary, so we must explicitly set (x, y) coordinates in order to compose
	 * the global image.
	 */
	public int x, y; 
	
	/**
	 * Simulation's step these informations are referring to.
	 */
	public long step;
	
	/**
	 * Simulation's time these informations are referring to.
	 */
	public double time;
	
	/**
	 * A visual snapshot of agents' positions in current region
	 * (if requested by the viewer)
	 */
	public byte[] image;
	
	/**
	 * A list of statistics read from simulations (as requested by
	 * the viewer). These are in the form (key, value). Suppose that
	 * the simulation instance has a method called getNumAgents
	 * returning and <code>int</code>. Then a valid entry for
	 * <code>stats</code> may be ("NumAgents", 200). 
	 */
	public HashMap<String, Object> stats;
	
	/**
	 * Constructor
	 * @param type Identifies the cell generating this snap
	 * @param step Simulation's step
	 * @param time Simulation's time (needed for graphs)
	 */
	public RemoteSnap(CellType type, long step, double time)
	{
		this.i = (short)type.pos_i;
		this.j = (short)type.pos_j;
		this.step = step;
		this.time = time;
		this.image = null;
		this.stats = null;
	}

}
