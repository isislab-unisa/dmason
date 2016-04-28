package it.isislab.dmason.sim.app.DCircles;

import java.awt.Color;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedState;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Double2D;

public class DCircle extends RemoteCircle<Double2D> {

	protected Color agentColor;
	/**
	 * The attractive damping term. Increasing this term will encourage agents to attract.
	 */
	double kAttr = 0;
	/**
	 * The repulsive damping term. Increasing this value will encourage agents to separate.
	 */
	double kRep = 1;
	/**
	 * The interaction radius of the circle agents. Increasing this value will increase the communication between agents (assuming constant density)
	 */
	static double RADIUS = 2;
	//Bag neighbors = null;

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
		// get last position
		pos = st.circles.getObjectLocation(this);
		
		//calculating neighbors from my position within distance 3R in toroidal field.
		//Note: getNeighborsWithinDistance method for efficiency 
		//		includes also extra agents which are not necessary in the specific range
		Bag neighbors = st.circles.getNeighborsWithinDistance(pos, 3*RADIUS,st.circles.isToroidal());
		//for all neighbors calculates forces exerted on me
		for(Object b : neighbors){
			DCircle other = (DCircle) b;
			if(other.id.equalsIgnoreCase(id)) //getNeighborsWithinDistance includes also me
				continue;
			//calculating euclidean discance from me to all neighbors
			distance = euclideanDistance(pos, other.pos);
			
			separation_distance = distance - (2*RADIUS);
			//exclude
			if(separation_distance < RADIUS){
				
				//in according to model, we set the appropriate force exerted (attractive or repulsive)  
				if(separation_distance<0)
					force = kRep;
				else
					force = kAttr;
				// calculating the shift on x,y coordinates in according to model
				x +=(distance==0)?0:(force * separation_distance * (other.pos.x - pos.x))/distance;

				y +=(distance==0)?0:(force * separation_distance * (other.pos.y - pos.y))/distance;
				if(Double.isNaN(x) || Double.isNaN(y))
					System.err.println("ollooooooc");
			}
		}
		Double2D oldpos = pos;
		if(st.circles.isToroidal())
			pos = new Double2D(st.circles.stx(pos.x + x), st.circles.sty(pos.y +y));
		else{
			
				//Note: for non toroidal field
				// 		if shift exceeds AOI it will be truncated
				if(pos.x +x > st.circles.my_width+st.circles.own_x)
					x =  (st.circles.my_width+st.circles.own_x) - pos.x-1;
				
				if(pos.y + y > st.circles.my_height+st.circles.own_y)
					y=(st.circles.my_height+st.circles.own_y) - pos.y-1;
				
				if(pos.x +x < st.circles.own_x)
					x= pos.x - st.circles.own_x;
				
				if(pos.y + y < st.circles.own_y)
					y = pos.y - st.circles.own_y;
				
				pos = new Double2D(pos.x + x,pos.y +y);
			}
		try {
			//setting the agent at new position
			if(!st.circles.setDistributedObjectLocation(pos,this, state))
				//it's for debugging
				System.out.println(x+" "+y+" mypos "+oldpos);
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}

	}

	public static double euclideanDistance(Double2D me_location, Double2D other_location){

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
