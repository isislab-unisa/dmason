package it.isislab.dmason.sim.field.network.kway.graph.tools;

import java.io.File;
import java.io.FilenameFilter;

public class Cleaner {
		
	public static void cleanOutput(File path, final String graph_name){
		
		if(path.exists()){
			
			FilenameFilter nameFilter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					
					if(name.startsWith(graph_name))
						return true;
					
					return false;
				}
			};
			
			File[] files = path.listFiles(nameFilter);
			for(File f : files){
				if(f.isDirectory())
					cleanDir(f);
				else{
					
					f.delete();
				}
						
			}	
		}
	}
	
	
	private static void cleanDir(File dir){
		File[] files = dir.listFiles();
		for(File f : files){
			if(f.isDirectory())
				cleanDir(f);
			else{
				
				f.delete();
			}	
		}
		dir.delete();
	}
	
}
