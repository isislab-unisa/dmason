package it.isislab.dmason.sim.app.SociallyDamagingBehavior;
import java.io.Serializable;

public class PastData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public double numNeighPunished = 0;
	public double numNeighDamager = 0;
	public double dna = 0;
	
	public PastData(double numNeighPunished, double numNeighDamager,
			double dna) {
		super();
		this.numNeighPunished = numNeighPunished;
		this.numNeighDamager = numNeighDamager;
		this.dna = dna;
	}

	public double getNumNeighPunished() {
		return numNeighPunished;
	}

	public void setNumNeighPunished(double numNeighPunished) {
		this.numNeighPunished = numNeighPunished;
	}

	public double getNumNeighDamager() {
		return numNeighDamager;
	}

	public void setNumNeighDamager(double numNeighDamager) {
		this.numNeighDamager = numNeighDamager;
	}

	public double getDna() {
		return dna;
	}

	public void setDna(double dna) {
		this.dna = dna;
	}
}