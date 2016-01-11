package it.isislab.dmason.sim.app.openAB.circle;

import java.awt.Color;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.CellType;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

public class DCircle extends RemoteCircle<Double2D> {

	private static final long serialVersionUID = 1L;
	protected Color agentColor;
	double kAttr = 1;
	double kRep = 1;
	static double RADIUS = 2;
	Bag neighbors = null;

	public DCircle(){}
	
	public DCircle(DistributedState<Double2D> sm, Double2D location){
		super(sm,DCircles.DIAMETER);
		pos = location;
		
	}

	@Override
	public void step(SimState state) {
		final DCircles st = (DCircles)state;
		double distance = 0.0;
		double force = 0.0;
		double separation_distance = 0.0;
		double x=0.0, y = 0.0;
		pos = st.circles.getObjectLocation(this);
		neighbors = st.circles.getNeighborsWithinDistance(pos, 3*RADIUS,true);
		for(Object b : neighbors){
			DCircle other = (DCircle) b;
			//System.out.println("me "+id+ "and you "+other.id);
			if(other.id.equalsIgnoreCase(id))
				continue;
			distance = euclideanDistance(pos, other.pos);
			separation_distance = distance - (2*RADIUS);
			if(separation_distance < RADIUS){
				if(separation_distance<0)
					force = kRep;
				else
					force = kAttr;

				x +=(force * separation_distance * (other.pos.x - pos.x))/distance;

				y +=(force * separation_distance * (other.pos.y - pos.y))/distance;
			}
		}
		pos = new Double2D(st.circles.stx(pos.x + x), st.circles.sty(pos.y +y));
		try {
			st.circles.setDistributedObjectLocation(pos,this, state);
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private double euclideanDistance(Double2D me_location, Double2D other_location){

		return Math.sqrt(Math.pow((me_location.x - other_location.x),2) + Math.pow((me_location.y - other_location.y),2));
	}
	
	public void setColor(Color color) 
	{
		this.agentColor = color;
		paint = agentColor;
	}

	public Color getColor() {
		return agentColor;
	}
	
	

}
