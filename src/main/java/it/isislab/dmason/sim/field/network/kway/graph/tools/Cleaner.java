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
package it.isislab.dmason.sim.field.network.kway.graph.tools;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.File;
import java.io.FilenameFilter;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "20/7/2015"
		)

/**
 * auxiliary class that removes all output files
 * written by executing algorithms 
 */
public class Cleaner {
		
	/**
	 * removes all output files in the path specified
	 * and with a specified name
	 * @param path - path where cleaning up
	 * @param graph_name - only files starting with this name will be deleted
	 */
	public static void cleanOutput(File path, final String graph_name){
		
		if(path.exists()){
			
			FilenameFilter nameFilter = new FilenameFilter() {
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
	
	
	/**
	 * removes all files in the specified path
	 * representing a vertex partitioning (file .part)
	 * @param path - path where cleaning up
	 */
	public static void cleanOutputPartition(File path){
		
		if(path.exists()){
				
			FilenameFilter nameFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					
					if((name.matches("^.*[0-9]$")))
						return true;
					
					return false;
				}
			};
			
			File[] files = path.listFiles(nameFilter);
			
			for(File f : files)
				f.delete();					
		}
	}
	
	
	/**
	 * removes a dir and all its content
	 * @param dir - the directory to remove
	 */
	public static void cleanDir(File dir){
		
		if(dir.exists()){
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
	
}













