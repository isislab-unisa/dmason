package it.isislab.dmason.sim.field.network.kway.algo.social.util;



import it.isislab.dmason.sim.field.network.kway.algo.social.GraphWDispersion;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.TreeMap;

public class Txt2JGraphWDispersion {

	/**
	 * @param args
	 */
	public static String s = "taroexhange"; //Grafo da generare 
	public static String  p;


	public static GraphWDispersion graph;

	public static void main(String[] args) {
		try {
			p = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String n=p.concat(File.separator+"graphs"+File.separator).concat(s).concat(".txt");
		graph= new GraphWDispersion();
		String strLine=null;
		

		try{
			FileInputStream fstream = new FileInputStream(n);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String[]  strSplit;
			TreeMap<Integer,Vertex> vertexes= new TreeMap<Integer,Vertex>();
			while ((strLine = br.readLine()) != null)   {
					if(!strLine.startsWith("%")){
					try{
						Vertex u,v;

						//System.out.println(strLine);

						//  strSplit= new String[2];
						strSplit=strLine.split(" ");

						if(strSplit.length>=2){
							Integer ui=Integer.parseInt(strSplit[0]);
							u=vertexes.get(ui);
							if(u==null){
								u=new Vertex(ui);
								graph.addVertex(u);
								vertexes.put(ui,u);
							}

							Integer vi=Integer.parseInt(strSplit[1]);
							v=vertexes.get(vi);
							if(v==null){
								v=new Vertex(vi);
								graph.addVertex(v);
								vertexes.put(vi,v);
							}

							graph.addEdge(u, v);
						}
					}catch (Exception e){
						System.out.println(strLine);
						e.printStackTrace();
					}
				}

			}
			in.close();			
		}catch (Exception e){
			e.printStackTrace();
		}

		//System.out.println(graph);

		
		String fileS=null;
		try {
			fileS = new java.io.File( "." ).getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fileS=fileS.concat(File.separator+"graphs"+File.separator).concat(s);
		fileS=fileS.concat("WD.graph");
		saveDiagram(fileS);
		System.out.println("Done "+ graph.vertexSet().size());
	}
	 


	public static void saveDiagram(String s) {
		FileOutputStream fis = null;
		ObjectOutputStream out = null;
		try {
			fis = new FileOutputStream(s);
			out = new ObjectOutputStream(fis);
			out.writeObject(graph);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Saving default diagram failed");
		}
	}


}
