package dmason.util.visualization;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import dmason.sim.field.CellType;

public class RemoteSnap implements Serializable{

	public byte[] image;
	
	public RemoteSnap(CellType type, long step, byte[] image) {
		
		this.image = image;
		this.i = (short)type.pos_i;
		this.j = (short)type.pos_j;
		this.step = step;
	}
	
	public short i, j;
	public long step;
	
}
