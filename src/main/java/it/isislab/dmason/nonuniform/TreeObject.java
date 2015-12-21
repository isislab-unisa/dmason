package it.isislab.dmason.nonuniform;


import java.io.Serializable;

public class TreeObject{
	public Serializable obj;
	double x;
	double y;
	public TreeObject(Serializable obj, double x, double y) {
		super();
		this.obj = obj;
		this.x = x;
		this.y = y;
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof TreeObject)
		{
			TreeObject t=(TreeObject)obj;
			return x==t.x && y==t.y && t.obj.equals(obj);
		}
		return false;
	}

}