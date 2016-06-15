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
package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An utils class for file system in DMason 
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class DMasonFileSystem {

	public static void make(String path){
		//modify 
		File c=new File(path);
		if(!c.exists()){	
			if(c.mkdirs())
			System.out.println("Directory is created : "+ c.getAbsolutePath());
			else System.out.println("Directory not created : "+ c.getAbsolutePath());;
		 }else {
			 System.out.println("Directory already exists : "+ c.getAbsolutePath());
		}
		

	}


    /**
     * Remove the folder from the file system
     * @param file file to delete
     */
	public static void delete(File file) {

		if(file.isDirectory()){

			//directory is empty, then delete it
			if(file.list().length==0){
				file.delete();
				//System.out.println("Directory is deleted : " + file.getAbsolutePath());

			}else{
				//list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					//construct the file structure
					File fileDelete = new File(file, temp);

					//recursive delete
					delete(fileDelete);
				}

				//check the directory again, if empty then delete it
				if(file.list().length==0){
					file.delete();
					System.out.println("Directory is deleted : "+ file.getAbsolutePath());
				}
			}

		}else{
			//if file, then delete it
			file.delete();
			//System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}
	
	
    /**
     * Copy a directory in another directory 
     *  
     * @param src  directory to copy
     * @param dest destination path
     * @throws IOException the exception
     */
    public static void copyFolder(File src, File dest)
    	throws IOException{
    	
    	if(src.isDirectory()){
    		
    		//if directory not exists, create it
    		if(!dest.exists()){
    		   dest.mkdir();
    		   //System.out.println("Directory copied from " + src + "  to " + dest);
    		}
    		
    		//list all the directory contents
    		String files[] = src.list();
    		
    		for (String file : files) {
    		   //construct the src and dest file structure
    		   File srcFile = new File(src, file);
    		   File destFile = new File(dest, file);
    		   //recursive copy
    		   copyFolder(srcFile,destFile);
    		}
    	   
    	}else{
    		//if file, then copy it
    		//Use bytes stream to support all file types
    		InputStream in = new FileInputStream(src);
    	        OutputStream out = new FileOutputStream(dest); 
    	                     
    	        byte[] buffer = new byte[1024];
    	    
    	        int length;
    	        //copy the file content in bytes 
    	        while ((length = in.read(buffer)) > 0){
    	    	   out.write(buffer, 0, length);
    	        }
 
    	        in.close();
    	        out.close();
    	        //System.out.println("File copied from " + src + " to " + dest);
    	}
    }
		
}
