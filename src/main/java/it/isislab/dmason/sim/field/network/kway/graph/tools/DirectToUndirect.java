package it.isislab.dmason.sim.field.network.kway.graph.tools;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * 
 * @author aleant
 */

public class DirectToUndirect {

	private static Logger logger = Logger.getLogger("dir2und");
	
	public static void main(String[] args) {
		
		if(args.length < 2){
			logger.info("Insert filepath and output filename..");
			System.exit(1);
		}
		
		String filepath = args[0];
		String outFilename = args[1];
		
		//directToUndirect(filepath, outFilename);
		undirecTodirect(filepath, outFilename);
	}

	/**
	 * Reads a graph described as an edgelist
	 * and transforms it into an undirect graph
	 * @param filepath - edgelist file, direct
	 * @param outFilename - edgelist file, undirect
	 */
	public static void directToUndirect(String filepath, String outFilename){
	
		String edge, separator;
		String invEdge;
		String info;
		String[] vertices = new String[2];
		HashMap<String, String> edges = new HashMap<String, String>();
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));
			
			while((edge = in.readLine()) != null){
					
				if(edges.containsKey(edge))
					continue;
				
				edges.put(edge, null);
				
				info = edge + "\n";
				out.write(info.getBytes());
				
				separator = findSeparator(edge);
				vertices = edge.split(separator);
				
				invEdge = vertices[1] + separator + vertices[0];
				
				edges.put(invEdge, null);
				
				info = invEdge + "\n";
				out.write(info.getBytes());
			}
			
			in.close();
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}//end convertFile

	/**
	 * Reads a graph described as an edgelist
	 * and transforms it into an undirect graph
	 * @param filepath - edgelist file, undirect
	 * @param outFilename - edgelist file, direct
	 */
	public static void undirecTodirect(String filepath, String outFilename){
		String edge, separator;
		String invEdge;
		String info;
		String[] vertices = new String[2];
		HashMap<String, String> edges = new HashMap<String, String>();
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));
			
			while((edge = in.readLine()) != null){
					
				if(edges.containsKey(edge))
					continue;
				
				separator = findSeparator(edge);
				vertices = edge.split(separator);
				
				invEdge = vertices[1] + separator + vertices[0];
				
				if(edges.containsKey(invEdge))
					continue;
				
				edges.put(edge, null);
				
				info = edge + "\n";
				out.write(info.getBytes());
				
			}
			
			in.close();
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
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



















