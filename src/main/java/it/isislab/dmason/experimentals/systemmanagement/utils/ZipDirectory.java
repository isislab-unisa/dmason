package it.isislab.dmason.experimentals.systemmanagement.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipDirectory {

	static List<String> fileList;
	private static   String OUTPUT_ZIP_FILE = "";
	private  static  String SOURCE_FOLDER = "";

	public static boolean createZipDirectory(String zipFileToCreate, String folderToZip){
		OUTPUT_ZIP_FILE=zipFileToCreate;
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
			String[] subNote = node.list();
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


	public static void main(String[] args) {
		String folderToZip="/home/miccar/Scrivania/worker/04-03-16-15_33/simulations/gvsbhscbhd1/out";
		String pathZip="/home/miccar/git/dmason/dmason/master/simulations/miasim/runs/-1043054413.zip";
		String pathUnzip="/home/miccar/Scrivania/outputZip";

		//if(ZipDirectory.createZipDirectory(pathZip, folderToZip)) System.out.println("finished");
		if(ZipDirectory.unZipDirectory(pathZip, pathUnzip)) System.out.println("finished");
		//ZipDirectory zipp=new ZipDirectory(pathZip, pathTozip);
		//zipp.zipDirectory(pathZip);
		//File file=new File(pathTozip);
		//ZipDirectory.zipDirectory(file, pathZip);
		//ZipDirectory.unZip(pathZip, pathUnzip);
	}

}

