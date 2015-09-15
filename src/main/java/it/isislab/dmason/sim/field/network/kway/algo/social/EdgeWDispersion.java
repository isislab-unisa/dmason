package it.isislab.dmason.sim.field.network.kway.algo.social;

import it.isislab.dmason.sim.field.network.kway.graph.Edge;

public class EdgeWDispersion extends Edge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected double dispersion;
	protected int dv;
	protected int embedness;

	public int getDv() {
		return dv;
	}

	public void setDv(int dv) {
		this.dv = dv;
	}

	public int getEmbedness() {
		return embedness;
	}

	public void setEmbedness(int embedeness) {
		this.embedness = embedeness;
	}

	public double getDispersion() {
		return dispersion;
	}

	public void setDispersion(double dispersion) {
		this.dispersion = dispersion;
	}
	

}
