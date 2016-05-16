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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class ZipDirectory {

	static List<String> fileList;
	
	private static  String SOURCE_FOLDER = "";

	public static boolean createZipDirectory(String zipFileToCreate, String folderToZip){
		SOURCE_FOLDER=folderToZip;
		fileList = new ArrayList<String>();
		generateFileList(new File(SOURCE_FOLDER));
		zipDirectory(zipFileToCreate);
		return true;
	}



	/**
	 * Zip it
	 * @param zipFile output ZIP file location
	 */
	private static void zipDirectory(String zipFile){

		byte[] buffer = new byte[1024];

		try{

			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);

			for(String file : fileList){

				System.out.println("File Added : " + file);
				ZipEntry ze= new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = 
						new FileInputStream(SOURCE_FOLDER + File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			//remember close it
			zos.close();

			System.out.println("Done");
		}catch(IOException ex){
			ex.printStackTrace();   
		}
	}

	/**
	 * Traverse a directory and get all files,
	 * and add the file into fileList  
	 * @param node file or directory
	 */
	private static void generateFileList(File node){

		//add file only
		if(node.isFile()){
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}

		if(node.isDirectory()){
			
			String[] subNote = node.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return name.endsWith(".out");
				}
			});
			for(String filename : subNote){
				generateFileList(new File(node, filename));
			}
		}

	}

	/**
	 * Format the file path for zip
	 * @param file file path
	 * @return Formatted file path
	 */
	private static  String generateZipEntry(String file){
		return file.substring(SOURCE_FOLDER.length()+1, file.length());
	}



	public static boolean unZipDirectory(String zipFile, String outputFolder){

		byte[] buffer = new byte[1024];

		try{

			//create output directory is not exists
			File folder = new File(outputFolder);
			if(!folder.exists()){
				folder.mkdir();
			}

			System.out.println("Start exstrazione "+zipFile);
			//get the zip file content
			ZipInputStream zis = 
					new ZipInputStream(new FileInputStream(zipFile));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				System.out.println("file unzip : "+ newFile.getAbsoluteFile());

				//create all non exists folders
				//else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);             

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();   
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done unzip");
            return true;
            
		}catch(IOException ex){
			ex.printStackTrace(); 
		}
		return false;
	}    


	//TESTING
	public static void main(String[] args) {
		String folderToZip="/home/user/Desktop/worker/04-03-16-15_33/simulations/gvsbhscbhd1/out";
		String pathZip="/home/user/git/dmason/dmason/master/simulations/miasim/runs/-1043054413.zip";
		String pathUnzip="/home/user/Desktop/outputZip";

		//if(ZipDirectory.createZipDirectory(pathZip, folderToZip)) System.out.println("finished");
		if(ZipDirectory.unZipDirectory(pathZip, pathUnzip)) System.out.println("finished");
		//ZipDirectory zipp=new ZipDirectory(pathZip, pathTozip);
		//zipp.zipDirectory(pathZip);
		//File file=new File(pathTozip);
		//ZipDirectory.zipDirectory(file, pathZip);
		//ZipDirectory.unZip(pathZip, pathUnzip);
	}

}

