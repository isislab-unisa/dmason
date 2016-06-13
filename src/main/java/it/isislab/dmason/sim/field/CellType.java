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


package it.isislab.dmason.sim.field;

import java.io.Serializable;

/**
 * A Class for a generic type of a Cell in a field 2D.
 * 
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * 
 */
public class CellType implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * i position in a 2D field
	 */
	public int pos_i;
	
	/**
	 * j position in a 2D field
	 */
	public int pos_j;
	
	/**
	 * @param pos_i i position in a 2D field
	 * @param pos_j j position in a 2D field
	 */
	public CellType(int pos_i, int pos_j) 
	{
		this.pos_i = pos_i;
		this.pos_j = pos_j;
	}
	
	public long getId(int i) { return (Long.parseLong(pos_i+""+pos_j)+i); }
	public int getInitialValue() { return Integer.parseInt(pos_i+""+pos_j); }
	
	//method to know the i and j positions of neighbors
	public String getNeighbourLeft() {	return (pos_i)+"-"+(pos_j-1); }
	public String getNeighbourDiagLeftUp() { return (pos_i-1)+"-"+(pos_j-1); }
	public String getNeighbourUp() { return (pos_i-1)+"-"+(pos_j); }
	public String getNeighbourDiagRightUp() { return (pos_i-1)+"-"+(pos_j+1); }
	public String getNeighbourRight() {	return (pos_i)+"-"+(pos_j+1); }
	public String getNeighbourDiagRightDown() {	return (pos_i+1)+"-"+(pos_j+1); }
	public String getNeighbourDown() { return (pos_i+1)+"-"+(pos_j); }
	public String getNeighbourDiagLeftDown() { return (pos_i+1)+"-"+(pos_j-1); }
	
	
	@Override
	public String toString() { return pos_i+"-"+pos_j; }
}