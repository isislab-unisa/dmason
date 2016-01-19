package it.isislab.dmason.experimentals.sim.field.grid.object;

import java.io.Serializable;

public class EntryObject<F> implements Serializable {

	public Object r;
	public F      l;
	
	
	public EntryObject(Object r, F location) {
		this.r=r;
		this.l=location;
	}
	
	
	
}
