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
package it.isislab.dmason.sim.app.DNetworkSIR;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.engine.DistributedAgentFactory;
import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.DistributedState;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.ObjectStreamException;

import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

@AuthorAnnotation(
		author = {"Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
public class Vertex15 extends RemoteVertex15<Double2D> implements DVertexState15{

	private static final long serialVersionUID = 2456052404584928634L;
	private int community;
	private boolean isVisited;

	public Vertex15(){}
	public Vertex15(String id,Integer community, Boolean isVisited, String label,Double2D pos) throws IOException
	{
		super.label=label;  
		super.id=id;
		this.community=community;
		this.isVisited=isVisited;
		super.pos=pos;
	}

	public Vertex15(SimState state,Integer community, Boolean isVisited, String label,Double2D pos) throws IOException
	{

		super((DistributedState)state,label);
		this.community=community;
		this.isVisited=isVisited;
		super.pos=pos;
	}

	@Override
	public String getId() {
		return super.id;
	}
	@Override
	public void setId(String id) {
		this.id=id;

	}
	public boolean isVisited() {
		return isVisited;
	}

	public void setVisited(boolean isVisited) {
		this.isVisited = isVisited;
	}

	@Override
	public int getCommunityId() {
		return community;
	}

	@Override
	public void setCommunityId(int community) {
		this.community=community;
	}

	@Override
	public void step(SimState state) {

		int sani = 0, malati = 0;

		//		if(isVisited!=true)
		//		{
		Bag out=null;

		out = ((Vertexes15)state).network.getEdges(this, null);

		//computazione farlocca
		try {
			Thread.sleep(0, 50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int len = out.size();
		for(int buddy = 0 ; buddy < len; buddy++)
		{	
			Edge e = (Edge)(out.get(buddy));
			if (((Vertex15)e.getFrom()).getVal((DistributedMultiSchedule) state.schedule)
					|| ((Vertex15)e.getTo()).getVal((DistributedMultiSchedule) state.schedule)) {
				malati++;
			} else {
				sani++;
			}
			//System.out.println("nodo " + label + " sani: " + sani + " malati: " + malati);
			if(isVisited==true) 
			{
				if(sani == 0) {
					if(state.random.nextDouble() >= 0.2) {
						setVisited(false);
					}
				} else if(len/sani + state.random.nextDouble() >= 1) {
					setVisited(false);
				}

			} else {
				if(malati == 0) {
					if(state.random.nextDouble() >= 0.2) {
						setVisited(true);
					}
				} else if(len/malati + state.random.nextDouble() >= 1) {
					setVisited(true);
				}
			}
		}
		//		}
		((Vertexes15)state).network.updateNode(this, state);
	}


	@Override
	public String toString() {

		return "[ "+label +" is visited? "+isVisited+"]";
	}

	@Override
	public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		double diamx = info.draw.width*2;
		double diamy = info.draw.height*2;

		graphics.setFont(new Font(Font.SERIF, Font.ITALIC, 16));
		graphics.setColor( (this.isVisited)?Color.RED : Color.GREEN);
		graphics.fillOval((int)(info.draw.x-diamx/2),(int)(info.draw.y-diamy/2),(int)(diamx),(int)(diamy));
		//graphics.drawString(label,(int)(info.draw.x-2*diamx),(int)(info.draw.y-0.01*diamy));
	}

	@Override
	public boolean getVal(DistributedMultiSchedule schedule) {
		return isVisited;
	}

	@Override
	public void setVal(DistributedMultiSchedule schedule, boolean isVisited) {
		this.isVisited=isVisited;
	}

	public Object writeReplace() throws ObjectStreamException{

		try {
			return new Vertex15(id, community, isVisited, label, pos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}return null;

	}
	public Object readResolve() throws ObjectStreamException{
		//(String id,int community, boolean isVisited, String label,Double2D pos)
		return DistributedAgentFactory.newIstance(
				Vertex15.class,
				new Class[]{String.class,Integer.class,Boolean.class,String.class,Double2D.class},
				new Object[]{id,community,isVisited,label,pos},
				DVertexState15.class);
	}
}
