package it.isislab.dmason.experimentals.systemmanagement.master;

import java.io.File;

public class MyFileSystem {

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



	public static void delete(File file) {

		if(file.isDirectory()){

			//directory is empty, then delete it
			if(file.list().length==0){
				file.delete();
				System.out.println("Directory is deleted : " + file.getAbsolutePath());

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
					System.out.println("Directory is deleted : " 
							+ file.getAbsolutePath());
				}
			}

		}else{
			//if file, then delete it
			file.delete();
			System.out.println("File is deleted : " + file.getAbsolutePath());
		}
	}

}
