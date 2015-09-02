package it.isislab.dmason.sim.field.network.partitioning;

import org.jgrapht.Graph;
@Deprecated
public class  SuperGraphStats<V, E> {
	private long weigth, noe;
	public Graph<V, E> g;
	public SuperGraphStats(long weigth, long noe, Graph<V, E> g) {
		super();
		this.weigth = weigth;
		this.noe = noe;
		this.g=g;
	}

	public long getWeigth() {
		return weigth;
	}

	public void setWeigth(long weigth) {
		this.weigth = weigth;
	}

	public long getNoe() {
		return noe;
	}

	public void setNoe(long noe) {
		this.noe = noe;
	}
	
	
}
