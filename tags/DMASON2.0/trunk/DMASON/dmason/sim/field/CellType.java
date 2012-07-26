package dmason.sim.field;

import java.io.Serializable;

/**
 * A Class for a generic type of a Cell in a field 2D.
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
	
	
	public String toString() { return pos_i+"-"+pos_j; }
}