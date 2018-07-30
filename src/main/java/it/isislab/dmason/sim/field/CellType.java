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
 * A Class for a generic type of a Cell in a field.
 *
 * @author Michele Carillo
 * @author Ada Mancuso
 * @author Dario Mazzeo
 * @author Francesco Milone
 * @author Francesco Raia
 * @author Flavio Serrapica
 * @author Carmine Spagnuolo
 * @author Matteo D'Auria
 */
public class CellType implements Serializable
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * i position in a field
	 */
	public int pos_i;

	/**
	 * j position in a field
	 */
	public int pos_j;

	/**
	 * z position in a field
	 */
	public int pos_z;

	public boolean is3D;

	/**
	 * @param pos_i i position in a field
	 * @param pos_j j position in a field
	 */
	public CellType(int pos_i, int pos_j)
	{
		this.pos_i = pos_i;
		this.pos_j = pos_j;
		this.pos_z=0;
		this.is3D=false;
	}

	/**
	 * @param pos_i i position in a field
	 * @param pos_j j position in a field
	 * @param pos_z z position in a field
	 */
	public CellType(int pos_i,int pos_j,int pos_z){
		this.pos_i=pos_i;
		this.pos_j=pos_j;
		this.pos_z=pos_z;
		this.is3D=true;
	}

	public long getId(int i) {
		if(!this.is3D)
			return (Long.parseLong(pos_i+""+pos_j)+i);
		else
			return (Long.parseLong(pos_i+""+pos_j+""+pos_z)+i);
	}

	public int getInitialValue() {
		if(!this.is3D)
			return Integer.parseInt(pos_i+""+pos_j);
		else
			return Integer.parseInt(pos_i+""+pos_j+""+pos_z);
	}

	//method to know the i and j positions of neighbors
	public String getNeighbourLeft() {	return (pos_i)+"-"+(pos_j-1); }
	public String getNeighbourDiagLeftUp() { return (pos_i-1)+"-"+(pos_j-1); }
	public String getNeighbourUp() { return (pos_i-1)+"-"+(pos_j); }
	public String getNeighbourDiagRightUp() { return (pos_i-1)+"-"+(pos_j+1); }
	public String getNeighbourRight() {	return (pos_i)+"-"+(pos_j+1); }
	public String getNeighbourDiagRightDown() {	return (pos_i+1)+"-"+(pos_j+1); }
	public String getNeighbourDown() { return (pos_i+1)+"-"+(pos_j); }
	public String getNeighbourDiagLeftDown() { return (pos_i+1)+"-"+(pos_j-1); }

	//method to know the i,j and z positions of neighbors
	public String getNeighbour3DLeft() {	return (pos_i)+"-"+(pos_j-1)+"-"+(pos_z); }
	public String getNeighbour3DLeftRear() {	return (pos_i)+"-"+(pos_j-1)+"-"+(pos_z-1); }
	public String getNeighbour3DLeftFront() {	return (pos_i)+"-"+(pos_j-1)+"-"+(pos_z+1); }

	public String getNeighbour3DDiagLeftUp() { return (pos_i+1)+"-"+(pos_j-1)+"-"+(pos_z); }
	public String getNeighbour3DDiagLeftUpRear() { return (pos_i+1)+"-"+(pos_j-1)+"-"+(pos_z-1); }
	public String getNeighbour3DDiagLeftUpFront() { return (pos_i+1)+"-"+(pos_j-1)+"-"+(pos_z+1); }

	public String getNeighbour3DUp() { return (pos_i+1)+"-"+(pos_j)+"-"+(pos_z); }
	public String getNeighbour3DUpRear() { return (pos_i+1)+"-"+(pos_j)+"-"+(pos_z-1); }
	public String getNeighbour3DUpFront() { return (pos_i+1)+"-"+(pos_j)+"-"+(pos_z+1); }

	public String getNeighbour3DDiagRightUp() { return (pos_i+1)+"-"+(pos_j+1)+"-"+(pos_z); }
	public String getNeighbour3DDiagRightUpRear() { return (pos_i+1)+"-"+(pos_j+1)+"-"+(pos_z-1); }
	public String getNeighbour3DDiagRightUpFront() { return (pos_i+1)+"-"+(pos_j+1)+"-"+(pos_z+1); }

	public String getNeighbour3DRight() {	return (pos_i)+"-"+(pos_j+1)+"-"+(pos_z); }
	public String getNeighbour3DRightRear() {	return (pos_i)+"-"+(pos_j+1)+"-"+(pos_z-1); }
	public String getNeighbour3DRightFront() {	return (pos_i)+"-"+(pos_j+1)+"-"+(pos_z+1); }

	public String getNeighbour3DDiagRightDown() {	return (pos_i-1)+"-"+(pos_j+1)+"-"+(pos_z); }
	public String getNeighbour3DDiagRightDownRear() {	return (pos_i-1)+"-"+(pos_j+1)+"-"+(pos_z-1); }
	public String getNeighbour3DDiagRightDownFront() {	return (pos_i-1)+"-"+(pos_j+1)+"-"+(pos_z+1); }

	public String getNeighbour3DDown() { return (pos_i-1)+"-"+(pos_j)+"-"+(pos_z);  }
	public String getNeighbour3DDownRear() { return (pos_i-1)+"-"+(pos_j)+"-"+(pos_z-1);  }
	public String getNeighbour3DDownFront() { return (pos_i-1)+"-"+(pos_j)+"-"+(pos_z+1);  }

	public String getNeighbour3DDiagLeftDown() { return (pos_i-1)+"-"+(pos_j-1)+"-"+(pos_z); }
	public String getNeighbour3DDiagLeftDownRear() { return (pos_i-1)+"-"+(pos_j-1)+"-"+(pos_z-1); }
	public String getNeighbour3DDiagLeftDownFront() { return (pos_i-1)+"-"+(pos_j-1)+"-"+(pos_z+1); }

	public String getNeighbour3DRear(){return (pos_i)+"-"+(pos_j)+"-"+(pos_z-1);  };
	public String getNeighbour3DFront(){ return (pos_i)+"-"+(pos_j)+"-"+(pos_z+1);  };

	@Override
	public String toString() {
		if(!this.is3D)
			return pos_i+"-"+pos_j;
		else
			return pos_i+"-"+pos_j+"-"+pos_z;
	}

	@Override
	public boolean equals(Object obj) {
		CellType c;
		if (obj instanceof CellType){
			c = (CellType)obj;
			if(this.is3D != c.is3D) return false;
			if (!is3D)
				return (c.pos_i == this.pos_i) && (c.pos_j == this.pos_j);
			else
				return (c.pos_i == this.pos_i) && (c.pos_j == this.pos_j) && (c.pos_z == this.pos_z);
		}
		return false;
	}


}