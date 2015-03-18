package it.isislab.dmason.sim.field.network.partitioning.interfaces;

public interface LabelVertex<V> {
	
	public V getLabel();
	public int getCommunity();
	public void setLabel(V newLabel);
	public void setCommunity(int newComm);
	
}
