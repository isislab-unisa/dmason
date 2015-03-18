/**
 * Copyright 2012 Universita' degli Studi di Salerno


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
package it.isislab.dmason.sim.app.DNetworkTest.mason;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;


public class Vertex extends SimplePortrayal2D implements Steppable{

	private static final long serialVersionUID = 2456052404584928634L;
	private int community;
	private boolean isInfected;
	private String label;
	private boolean privateState=false;
	private int ID;

	public Vertex(SimState state,int community, boolean isVisited, String label,Double2D pos)
	{
		this.community=community;
		this.isInfected=isVisited;
		this.label=label;

	}

	//	public String getId() {
	//		return super.id;
	//	}

	//	public void setId(String id) {
	//		this.id=id;
	//
	//	}
	public boolean isInfected() {
		return isInfected;
	}

	public void setInfected(boolean isVisited) {
		this.isInfected = isVisited;
	}

	public int getCommunityId() {
		return community;
	}

	public void setCommunityId(int community) {
		this.community=community;
	}
	public boolean getValAtStep(long step)
	{
		if(nextStep==step) return privateState;
		else return isInfected;
	}
	long nextStep=1;
	public void step(SimState state) {

		nextStep= state.schedule.getSteps();

		//		if(privateState){
		//			isVisited=privateState;
		//			((Vertexes)state).vertexCount--;
		//			if(1 == ((Vertexes)state).vertexCount)
		//			{
		//				System.err.println("Simulation ended in "+(System.currentTimeMillis()-((Vertexes)state).startTime)+" at steps: "+(state.schedule.getSteps()-1));
		//			}
		//			privateState=false;
		//		}
		isInfected=privateState;
		Bag out= new Bag();
		int infected = 0, sani=0;

		out = ((Vertexes)state).network.getEdgesIn(this);
		//out.addAll(((Vertexes)state).network.getEdgesOut(this));
		int len = out.numObjs;
		//		System.out.println("ID "+label+" "+len);
		for(int buddy = 0 ; buddy < len; buddy++)
		{	

			Edge e = (Edge)(out.get(buddy));
			privateState = ((Vertex)e.getFrom()).getValAtStep(state.schedule.getSteps()-1) ||
					((Vertex)e.getTo()).getValAtStep(state.schedule.getSteps()-1);
			if(privateState==true) 
			{
				infected++;
			}else sani++;
		}

		double r=state.random.nextDouble(false, true);

		if(isInfected!=true)
		{	
			//System.out.println("I'm " + label + " state: not infected; infected neighbors: " + infected);
			if(infected == 0){
				if(r >= 0.02) 
					this.isInfected = true;
			}

			else if((infected/len) + r  >= 1) {
				this.isInfected = true;
			}
		} else {
			//System.out.println("I'm " + label + "state: infected; not infected neighbors: " + sani);
			if(sani == 0) {
				if(r >= 0.02) 
					this.isInfected = false;
			}

			else if((sani/len) + r >= 1) {
				this.isInfected = false;
			}

			//			if(infected == 0) {
			//				this.isInfected = false; 
			//				System.out.println("I'm " + label + " - infected: " + infected + "and I'm NOT gonna be INFECTED");
			//			}else if((len/infected)*r <= 1) {
			//				this.isInfected = true; 
			//				System.out.println("I'm " + label + " - infected: " + infected + "and I'm gonna be INFECTED");
			//			}
			//			
			//		}else{
			//			if(sani == 0) {
			//				this.isInfected = true; 
			//				System.out.println("I'm " + label + " - infected: " + infected + "and I'm gonna be INFECTED");
			//			}else if((len/sani)*r <= 1) {
			//				this.isInfected = false; 
			//				System.out.println("I'm " + label + " - infected: " + infected + "and I'm NOT gonna be INFECTED");
			//			}
		}
		//((Vertexes)state).yard.setObjectLocation(this, new Double2D());
		//((Vertexes)state).network.updateNode(this, state);
	}



	@Override
	public String toString() {

		return "[ "+label +" is visited? "+isInfected+"]";
	}


	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		double diamx = info.draw.width*1.5;
		double diamy = info.draw.height*1.5;

		graphics.setFont(new Font(Font.SERIF, Font.ITALIC, 16));
		graphics.setColor( (this.isInfected)?Color.RED : Color.GREEN);
		graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
		//		graphics.drawString(label,(int)(info.draw.x-2*diamx),(int)(info.draw.y-0.01*diamy));
	}

	public int getCommunity() {
		return community;
	}

	public void setCommunity(int community) {
		this.community = community;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isPrivateState() {
		return privateState;
	}

	public void setPrivateState(boolean privateState) {
		this.privateState = privateState;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public long getNextStep() {
		return nextStep;
	}

	public void setNextStep(long nextStep) {
		this.nextStep = nextStep;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
