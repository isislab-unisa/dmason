package it.isislab.dmason.sim.field.network.kway.graph.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;


public class VertexParser {

	//private static Logger logger = Logger.getLogger("vertex-parser");
	
	/**
	 * Reads a graph described as edgelist 
	 * and returns all the vertices of the graph
	 * with their id
	 * @param filepath - file.edgelist describing a graph
	 * @return ids - TreeMap containing vertices id, inserted as keys
	 */
	
	public static TreeMap<Integer, String> getOriginalIdFromEdgelist(String filepath){
		TreeMap<Integer, String> ids = new TreeMap<Integer, String>();
		String edge, separator;
		String vertices[] = new String[2];
	
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			
			while((edge = in.readLine()) != null){
				
				separator = findSeparator(edge);
				vertices = edge.split(separator);
				
				int firstId = Integer.parseInt(vertices[0]);
				int secondId = Integer.parseInt(vertices[1]);
				
				if(!ids.containsKey(firstId))
					ids.put(firstId, null);
				
				if(!ids.containsKey(secondId))
					ids.put(secondId, null);
				
			}
	
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
//		
//		List<Integer> c=new ArrayList<Integer>(ids.keySet());
//		Collections.sort(c);
//		for (int i = 1; i < c.size(); i++) {
//			if(c.get(i)!=(c.get(i-1)+1)) {
//				System.out.println(c.get(i-1)+" "+c.get(i));
//				//System.exit(-1);
//			}
//		}
		return ids;
	}
	
	private static String findSeparator(String info){
		String separator = "";
		
		for(int i=0; i<info.length(); i++){
			char c = info.charAt(i);
			
			if(!Character.isDigit(c)){
				separator = c + "";
				break;
			}	
		}
		
		return separator;
	}
}
