package it.isislab.dmason.sim.app.SociallyDamagingBehavior;

import java.io.Serializable;

public class EntrySocialAgent <D, H> implements Serializable{

	private D fitSum;
	private H h;
	
	public D getFitSum() {
		return fitSum;
	}

	public void setFitSum(D fitSum) {
		this.fitSum = fitSum;
	}

	public H getH() {
		return h;
	}

	public void setH(H h) {
		this.h = h;
	}

	public EntrySocialAgent(D fitsum, H h) {

		this.fitSum = fitsum;
		this.h = h;
	}
}
