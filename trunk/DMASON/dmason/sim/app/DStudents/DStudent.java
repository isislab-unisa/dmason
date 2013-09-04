package dmason.sim.app.DStudents;

import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.sim.field.network.CellDimension;
import dmason.sim.field.network.DNetwork;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.MutableDouble2D;

public class DStudent extends RemoteStudent<Double2D> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1474708405645852144L;
	public static final double MAX_FORCE = 3.0;
	public Double2D loc,loc2;
	public int step=0;
	double x,y;
	boolean  xflag=true;
	boolean  yflag=true;
	double xStep=0;
	double xt=100,yt=100;
	Double2D target;
	Double2D speed;
	public DStudent() {}

	public DStudent(DistributedState<Double2D> sm, Double2D location){
		super(sm);
		loc = location;
		target = new Double2D(loc.x, loc.y);
	}

	public void step(SimState state) {

		final DStudents students = (DStudents) state;

		DNetwork net=(DNetwork) students.network;
		
		loc = students.yard.getObjectLocation(this);
		
		MutableDouble2D sumForces = new MutableDouble2D();
		
		if (target.distanceSq(loc) < 0.01)
		{
			while (target.distanceSq(loc) < 0.01) {
				target = new Double2D(students.random.nextDouble()*students.gridWidth,students.random.nextDouble()*students.gridHeight);
			}
			speed = target.subtract(loc).resize(.1);
		}
		
		sumForces.addIn(speed);
		sumForces.addIn(new Double2D(loc.x, loc.y));
	
		
		/*--------------------------------------------*/
		
		/*if (this.getId().equals("0-0-1")){
			if (state.schedule.getSteps()>2200 )
				sumForces.addIn(new Double2D(loc.x, loc.y ));
			else sumForces.addIn(new Double2D(loc.x+0.1, loc.y+0.1));
		}
		else if (this.getId().equals("1-0-82")){
			if (state.schedule.getSteps()>2200)
				sumForces.addIn(new Double2D(loc.x, loc.y ));
			else sumForces.addIn(new Double2D(loc.x+0.1, loc.y-0.1));
		}
		else if (this.getId().equals("0-1-12")){
			if (state.schedule.getSteps()>1500)
				sumForces.addIn(new Double2D(loc.x, loc.y));
			else sumForces.addIn(new Double2D(loc.x-0.1, loc.y ));
		}
		else if (this.getId().equals("0-1-16")){
			if (state.schedule.getSteps()>1500)
				sumForces.addIn(new Double2D(loc.x, loc.y ));
			else sumForces.addIn(new Double2D(loc.x-0.1, loc.y));
		}
		else if (this.getId().equals("0-0-7")){
			if (state.schedule.getSteps()>110)
				sumForces.addIn(new Double2D(loc.x, loc.y ));
			else sumForces.addIn(new Double2D(loc.x+0.1, loc.y));
		}
		else  if (this.getId().equals("1-0-86")){
			if (state.schedule.getSteps()>=2000 && state.schedule.getSteps()<=3000)
				sumForces.addIn(new Double2D(loc.x-0.1, loc.y));
			else if (state.schedule.getSteps()<2000)
				sumForces.addIn(new Double2D(loc.x+0.1, loc.y));
			else sumForces.addIn(new Double2D(loc.x, loc.y));
		}
		else  if (this.getId().equals("1-1-93")){
			if (state.schedule.getSteps()>1800 && (state.schedule.getSteps()<3200))
				sumForces.addIn(new Double2D(loc.x, loc.y+0.1));
			else if (state.schedule.getSteps()>3210) sumForces.addIn(new Double2D(loc.x, loc.y));
			else sumForces.addIn(new Double2D(loc.x, loc.y-0.1));
		}
		else {
			sumForces.addIn(new Double2D(loc.x, loc.y));
		}
		
		
		//----------------------------------------------------------------*/

		loc = new Double2D(sumForces);
		students.yard.setDistributedObjectLocation(loc, this, state);
		
		
		students.network.setDistributedObjectLocation(loc, this, state);
		
		

	}
	


}