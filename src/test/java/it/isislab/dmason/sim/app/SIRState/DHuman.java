/**

 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.sim.app.SIRState;

import it.isislab.dmason.exception.DMasonException;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.engine.RemoteAgentStateMethodHandler;
import it.isislab.dmason.sim.engine.StateVariable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

public class DHuman  extends RemoteHuman<Double2D> {



	//Wandering
	public Double2D targetLocation = null;
	private Double2D desired_velocity = null;
	private Double2D steering = null;
	protected Color agentColor;
	static double MAX_VELOCITY = 0.5;
	static double MAX_FORCE = 0.1;
	private Double2D tmp = null;
	private double mass = 1;
	private int next_decision = 0;
	private Double2D velocity = new Double2D(0,0); //2D field
	//End Wandering

	private int timer =0;

	//CONSISTENCY MANAGER
	static ArrayList<StateVariable> statevariables=new ArrayList<StateVariable>();

	static{
		
		statevariables.add(new StateVariable("IsInfected",boolean.class));
	}
     final static RemoteAgentStateMethodHandler memory=new RemoteAgentStateMethodHandler(DHuman.class,statevariables);

	public boolean IsInfected =false;
	public boolean isResistent =false;

	private static int AOI= 10;

	private static Color SUSCEPTIBLE = new Color(0, 100, 0); //green
	private static Color RESISTENT = new Color(0, 0, 255);//blue
	private static Color INFECTED = new Color(255,0,0);//red

	private static double VIRUS_SPREAD_CHANCE= 10;//[0,10]
	private static double VIRUS_CHECK_FREQUENCY= 20; //every 20 steps [1,20]
	private static double RECOVERY_CHANCE= 10; //[0,10]
	private static double GAIN_RESISTANCE_CHANCE= 50; //[0,100]

	private Bag neighbors = null;


	//consinstent
	public Double2D lastp = new Double2D(0,0);

	public DHuman() {}

	public DHuman(SimState sm,Double2D location, Boolean isInfected){ 
		super((DistributedState)sm);
		pos = location;
		this.IsInfected=isInfected;
		

	}

	public DHuman(String idAgent, Double2D location, Boolean isInfected,Integer next_decision,Double2D lastpos, Double2D targpos) throws IOException{ 
		this.id=idAgent;
		pos = location;
		this.IsInfected=isInfected;
		this.isResistent = false;
		this.next_decision = next_decision;
		this.lastp=lastpos;
		this.targetLocation = targpos;
		
	}



	@Override
	public void step(SimState state) {
		DPeople st = (DPeople)state;
		double x=0,y=0;
		Double2D normVect = null;
		pos = st.environment.getObjectLocation(this);
		lastp = pos;
		/*targetLocation = new Double2D(st.random.nextDouble()*st.environment.getWidth(),
				st.random.nextDouble()*st.environment.getHeight());*/


		timer++;
		if(timer >= VIRUS_CHECK_FREQUENCY)
			timer=0;

		spreadVirus(st);
		doVirusCheck(st);

		if(state.schedule.getSteps()==next_decision){
			/*targetLocation = new Double2D(st.random.nextDouble()*st.environment.getWidth(),
					st.random.nextDouble()*st.environment.getHeight());*/
			//targetLocation = st.environment.getAvailableRandomLocation();
			targetLocation = new Double2D(st.random.nextDouble() *st.environment.getWidth(),st.random.nextDouble()*st.environment.getHeight());  
			next_decision = (int) state.schedule.getSteps() + (int) Math.floor((state.random.nextDouble()*10)+100);
		}

		//tmp= subVector(targetLocation,agentLocation);
		//normVect =normalize(tmp);
		//velocity =  new Double2D(normVect.getX()* MAX_VELOCITY,normVect.getX()*MAX_VELOCITY);

		tmp = seek(targetLocation,pos);


		steering = truncate(tmp,MAX_FORCE);
		steering = new Double2D(steering.getX()*(1/mass),steering.getY()*(1/mass));


		tmp = sumVector(velocity, steering);
		velocity = truncate (tmp , MAX_VELOCITY);

		pos = new Double2D(pos.x + velocity.x, pos.y +velocity.y);
		//pos = truncate(pos, st.environment.getWidth(),st.environment.getHeight());
		pos = truncate(pos, st.environment.getWidth()-1,st.environment.getHeight()-1);
		try {
			st.environment.setDistributedObjectLocation(pos,this,state);
		} catch (DMasonException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void spreadVirus(DPeople st) {

		try{
			Bag neighbors = st.environment.getNeighborsWithinDistance(pos, AOI);

			for(Object o: neighbors){
				DHuman p = (DHuman)o;
				Boolean _isInfected=(Boolean)
						memory.getState((DistributedMultiSchedule)st.schedule, p, "IsInfected");
				if(_isInfected
						&&
						!isResistent ){
					if(st.random.nextInt(100) < VIRUS_SPREAD_CHANCE){
						//agentColor = INFECTED;
						memory.setState((DistributedMultiSchedule)st.schedule,this, "IsInfected", true);
						break;
					}
				}
			} 
		}catch ( Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
		}


	}

	private void doVirusCheck(DPeople st) {
		try{
			if(IsInfected && timer==0){
				if(st.random.nextInt(100) < RECOVERY_CHANCE){
					memory.setState((DistributedMultiSchedule)st.schedule,this, "IsInfected",false);
					if(st.random.nextInt(100) < GAIN_RESISTANCE_CHANCE){
						isResistent=true;
						//agentColor = RESISTENT;
					}

				}


			}
		}catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	private Double2D seek(Double2D target, Double2D agent) {
		Double2D normVect;
		tmp= subVector(target,agent);
		normVect =normalize(tmp);
		desired_velocity = new Double2D(normVect.getX()* MAX_VELOCITY,normVect.getY()*MAX_VELOCITY);

		tmp = subVector(desired_velocity, velocity);
		return tmp;
	}

	//public void setColor(Color c){ this.agentColor = c; paint = agentColor;}



	private Double2D truncate(Double2D pos, double value){
		double x,y;
		x=(pos.x>value)?value:pos.x;
		y=(pos.y>value)?value:pos.y;
		return new Double2D(x,y);
	}

	private Double2D truncate(Double2D pos, double width, double height){
		double x = pos.x,y=pos.y;
		if(x<0) x=0;
		if(y<0) y=0;
		if(x>width) x=width;
		if(y>height) y=height;

		return new Double2D(x,y);
	}

	private Double2D sumVector(Double2D a, Double2D b)
	{ 
		double x,y;
		x=a.x+b.x;
		y=a.y+b.y;
		return new Double2D(x,y);
	}

	private Double2D subVector(Double2D a, Double2D b)
	{ 
		double x,y;
		if(a==null || b==null){
			System.out.println("tps "+a);
			System.out.println("ps "+b);
		}
		x=a.x-b.x;
		y=a.y-b.y;
		return new Double2D(x,y);
	}

	private Double2D normalize(Double2D vector){
		double vector_lenght =Math.sqrt((Math.pow(vector.x, 2)+Math.pow(vector.y, 2)));

		return new Double2D(vector.x/vector_lenght,vector.y/vector_lenght);
	}

	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		double diamx = info.draw.width*8;
		double diamy = info.draw.height*8;

		if (IsInfected) graphics.setColor( INFECTED );
		else if(isResistent) graphics.setColor ( RESISTENT );
		else graphics.setColor ( SUSCEPTIBLE );
		graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
	}


	public boolean getIsInfected() {
		return IsInfected;
	}
	public void setIsInfected(boolean val) {
		IsInfected=val;

	}


//	public Object writeReplace() throws ObjectStreamException{
//
//		try {
//			return new DHuman(id,pos,isInfected,next_decision,lastp,targetLocation);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}return null;
//
//	}
//	public Object readResolve() throws ObjectStreamException{
//
//		return DistributedAgentFactory.newIstance(
//				DHuman.class,
//				new Class[]{String.class,Double2D.class,Boolean.class,Integer.class,Double2D.class,Double2D.class},
//				new Object[]{id,pos,isInfected,next_decision,lastp,targetLocation},
//				DHumanState.class);
//	}

}
