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
package it.isislab.dmason.sim.field.network.kway.algo.social;



import it.isislab.dmason.sim.field.network.kway.algo.social.util.ColorMap;
import it.isislab.dmason.sim.field.network.kway.algo.social.util.JGraphFrame;
import it.isislab.dmason.sim.field.network.kway.algo.social.util.Util;
import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;





import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.JFrame;

public class SandBox {
	static int K=2;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fileS=null;
		try {
			fileS = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileS=fileS.concat(File.separator+"graphs"+File.separator+"taroexhange"+"WD.graph");


		GraphWDispersion g= (GraphWDispersion) Util.loadDiagram(fileS);
		if(g==null){
			System.out.println("Loading  diagram failed");
			System.exit(0);
		}
		System.out.println(g.vertexSet().size()+ " "+ g.edgeSet().size());

		DispersionOnSocialTies.ComputeDispersion(g, K);
		

		ArrayList<Edge> ordedges=new ArrayList<Edge>(g.edgeSet());
		Collections.sort(ordedges,new Comparator<Edge>() {

			@Override
			public int compare(Edge o1, Edge o2) {
				return Double.compare(((EdgeWDispersion)o1).getDispersion(), ((EdgeWDispersion)o2).getDispersion());
			}
		});

		//		for(Edge ee : ordedges)
		//		{
		//			EdgeWDispersion e =(EdgeWDispersion)ee;
		//	
		//			System.out.println("Edge ("+ e.getU()+", "+ e.getV()+") "+ "dv= "+ e.getDv() + " Embedness= "+ e.getEmbedness()+ " normDisp "+ e.getDispersion());
		//		}
//		
		HashMap<Color, ArrayList<Vertex>> cTov=new HashMap<Color,ArrayList<Vertex>>();
		ArrayList<Vertex> order_vertex=new ArrayList<Vertex>();
		double vertex_MAX_DISPERSION=DispersionOnSocialTies.MAX_DISPERSION;
		for(Vertex v : g.vertexSet())
		{
			double v_dispersion=0;
			for(Edge ee:g.edgesOf(v))
			{
				EdgeWDispersion e=(EdgeWDispersion)ee;
				v_dispersion+=e.getDispersion();
			}
			v.setDispersion(v_dispersion);
			if(v_dispersion > vertex_MAX_DISPERSION)
				vertex_MAX_DISPERSION=v_dispersion;
			if(cTov.get(ColorMap.pickColor(v.getDispersion(),vertex_MAX_DISPERSION))==null) 
				cTov.put(ColorMap.pickColor(v.getDispersion(),vertex_MAX_DISPERSION), new ArrayList<Vertex>());
		
			cTov.get(ColorMap.pickColor(v.getDispersion(),vertex_MAX_DISPERSION)).add(v);
			order_vertex.add(v);
		}
		System.out.println("Max vertex dispersion "+vertex_MAX_DISPERSION);
		Collections.sort(order_vertex, new Comparator<Vertex>() {

			@Override
			public int compare(Vertex arg0, Vertex arg1) {
				// TODO Auto-generated method stub
				return Double.compare(arg0.getDispersion(), arg1.getDispersion());
			}
		});
		for(Vertex v:order_vertex)
		{int val=(int) ((255*v.getDispersion())/vertex_MAX_DISPERSION);
			System.out.println(v+" "+v.getDispersion()+" "+val);
		}
//		HashMap<Color, ArrayList<Vertex>> cTov=new HashMap<Color,ArrayList<Vertex>>();
//		for(Edge ee:g.edgeSet())
//		{
//			EdgeWDispersion e=(EdgeWDispersion)ee;
//			Vertex u=e.getU();
//			Vertex v=e.getV();
//
//			if(cTov.get(ColorMap.pickColor(e.getDispersion(),DispersionOnSocialTies.MAX_DISPERSION))==null) cTov.put(ColorMap.pickColor(e.getDispersion(),DispersionOnSocialTies.MAX_DISPERSION), new ArrayList<Vertex>());
//			cTov.get(ColorMap.pickColor(e.getDispersion(),DispersionOnSocialTies.MAX_DISPERSION)).add(u);
//			cTov.get(ColorMap.pickColor(e.getDispersion(),DispersionOnSocialTies.MAX_DISPERSION)).add(v);
//
//		}
		JGraphFrame frame = new JGraphFrame();
		frame.initWithDispersion(g,cTov);

		  frame.setTitle("JGraph ISISLab - "+fileS+" Network Dispersion for K="+K);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

}
