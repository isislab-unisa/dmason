package it.isislab.dmason.experimentals.util.visualization.globalviewer;


/**
 * An utility class used to represent a simulation class as a
 * combobox entry.
 * @author Luca Vicidomini
 */
public class SimComboEntry
{
	/**
	 * A short name that will be shown to the user.
	 */
	String shortName;
	
	/**
	 * Qualified name of the class implementing the Zoom feature.
	 */
	String fullZoomName;
	
	/**
	 * Qualified name of the class implementing the proper simulation.
	 */
	String fullSimName;
	
	/**
	 * Creates a new entry for the combobox.
	 * @param shortName A short name that will be shown to the user.
	 * @param fullZoomName Qualified name of the class implementing the Zoom feature.
	 * @param fullSimName Qualified name of the class implementing the proper simulation.
	 */
	public SimComboEntry(String shortName, String fullZoomName, String fullSimName) { this.shortName = shortName; this.fullZoomName = fullZoomName; this.fullSimName = fullSimName; }
	@Override public String toString() { return shortName; }
}