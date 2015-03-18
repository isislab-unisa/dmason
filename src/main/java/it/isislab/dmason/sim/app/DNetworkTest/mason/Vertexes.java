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

import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util.GraphMLImpoter;
import it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util.ImportException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.network.Network;
import sim.util.Double2D;


public class Vertexes extends SimState{

	private static final long serialVersionUID = -7594558050842065632L;
	public static String topicPrefix = "Vertexes";
	public Network network;
	public Continuous2D yard = new Continuous2D(1.0,100,100);
	public int vertexCount;
	public long startTime;
	private HashMap<String, Vertex> myVertexes;

	public int colored=0;


	public Vertexes(long seed, String graph_path)
	{    	
		super(seed);

		GraphMLImpoter<String, DefaultWeightedEdge> gImp = null;
		try {
			gImp = new GraphMLImpoter<String, DefaultWeightedEdge>(graph_path);
		} catch (ImportException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		network = new Network();

		// clear the yard
		yard.clear();

		// clear the network
		network.clear();

		//int commID = (TYPE.pos_i*rows)+TYPE.pos_j;
		SimpleGraph<String, DefaultWeightedEdge> g = (SimpleGraph<String, DefaultWeightedEdge>) gImp.getGraph();
//		System.out.println(g.toString());
		Set<DefaultWeightedEdge> edgeSet = g.edgeSet();
		Set<String> vertexSet = g.vertexSet();
		myVertexes = new HashMap<String,Vertex>();
		System.out.println(edgeSet.size());
		for (DefaultWeightedEdge e : edgeSet) {


			Vertex source = myVertexes.get( ""+((int)Double.parseDouble(g.getEdgeSource(e))));
			Vertex target = myVertexes.get( ""+(int)Double.parseDouble(g.getEdgeTarget(e)));
			Double2D pos=null;
			if(source==null)
			{
				pos=new Double2D(this.random.nextDouble()*yard.getWidth(), this.random.nextDouble()*yard.getHeight());
				source =new Vertex(this, -1, this.random.nextBoolean(),""+((int)Double.parseDouble(g.getEdgeSource(e))) ,pos);
				myVertexes.put(source.getLabel(), source);
				network.addNode(source);
				yard.setObjectLocation(source, pos);
				//System.out.println("CREO SRC "+source.getLabel());
			}
			if(target==null)
			{
				pos=new Double2D(this.random.nextDouble()*yard.getWidth(), this.random.nextDouble()*yard.getHeight());
				target = new Vertex(this, -1, this.random.nextBoolean(),""+((int)Double.parseDouble(g.getEdgeTarget(e))) ,pos);
				myVertexes.put(target.getLabel(), target);
				network.addNode(target);
				yard.setObjectLocation(target, pos);
				//System.out.println("CREO TR "+target.getLabel());
			}
			network.addEdge(source, target, "");
			//network.addEdge(target, source, "");

		}
		vertexCount = vertexSet.size();
		
		System.out.println("vertici : "+network.getAllNodes().size());
	}



	@Override
	public void start()
	{
		startTime=System.currentTimeMillis();
		super.start();

		for(String v : myVertexes.keySet())
		{
			schedule.scheduleRepeating(myVertexes.get(v));
		}
	}


	public static void main(String[] args) throws Exception
	{
		//		doLoop(Vertexes.class, args);
		//		System.exit(0);

		/*Debug: you can omit this code (2) */
		long start_time=0;
		String file=null;
		FileOutputStream out=null;
		PrintStream print=null;

		String graphName = args[1].split("/")[args[1].split("/").length - 1];
		
		file="MASON-SIM-TIME-graph-"+ graphName +"-STEPS-"+args[0]+".txt";
		start_time=System.currentTimeMillis();
		out=new FileOutputStream(new File(file));
		print=new PrintStream(out);

		System.out.println("Worker 0 FILE:"+file+" simulation started..");
		System.out.println("STEP:");

		Vertexes state=new Vertexes(0,args[1]);

		state.start();
		/*End (2)*/
		//MPI.COMM_WORLD.barrier();

		int STEP=Integer.parseInt(args[0]);
		int NUM_STEP=0;
		while(NUM_STEP< STEP)
		{
			/*Debug: you can omit this code (3) */

			System.out.print(NUM_STEP+" . ");
			
			/*End (3)*/
			state.schedule.step(state);
			NUM_STEP++;

		}

		print.print(System.currentTimeMillis()-start_time);
		System.out.println("end Simulation");
		try{
			print.close();
			out.close();
		}catch (Exception e) {
			System.out.println("Error file writing: "+file);
		}


	}  

	public Network getField() {
		// TODO Auto-generated method stub
		return network;
	}

	public SimState getState() {
		// TODO Auto-generated method stub
		return null;
	}
}
