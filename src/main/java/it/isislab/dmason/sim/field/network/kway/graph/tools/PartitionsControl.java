package it.isislab.dmason.sim.field.network.kway.graph.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class PartitionsControl {

	public static void main(String[] args) {
		
	//	checkPartitions("/home/aleant/workspace/kway/jabeja-output/add20U.graph.part.2");
		checkPartitions("/home/aleant/workspace/kway/jabeja-output/add20U.graph.part.4");

	}
	
	public static void checkPartitions(String filepath){
		String p = "";
		HashMap<String, Integer> numPart = new HashMap<String, Integer>();
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			
			while((p=in.readLine()) != null){
				
				if(!numPart.containsKey(p)){
					numPart.put(p, 1);
				}
				else{
					int numElem = numPart.get(p) + 1;
					numPart.put(p, numElem);
				}
			}
			
			System.out.println(numPart);
			
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
