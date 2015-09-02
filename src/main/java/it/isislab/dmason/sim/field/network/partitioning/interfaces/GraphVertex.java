package it.isislab.dmason.sim.field.network.partitioning.interfaces;

@Deprecated
public class GraphVertex<V> implements LabelVertex<V>, Comparable<GraphVertex<V>>{

	private V id;
	private int label;

	public GraphVertex(V id, int label)
	{
		this.id=id;
		this.label=label;
	}
	
	@Override
	public V getLabel() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public int getCommunity() {
		// TODO Auto-generated method stub
		return label;
	}

	@Override
	public void setLabel(V newLabel) {
		id = newLabel;

	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[NID"+id+" CID"+label+"]";
	}
	
	@Override
	public int compareTo(GraphVertex<V> o) {
		return o.getLabel() == this.getLabel() ? 0 : 1;
	}

	@Override
	public void setCommunity(int newLabel) {
		this.label = newLabel;
		
	}

}
