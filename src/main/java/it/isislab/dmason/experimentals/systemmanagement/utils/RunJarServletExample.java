package it.isislab.dmason.experimentals.systemmanagement.utils;


import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class RunJarServletExample extends HttpServlet{


	private static final long serialVersionUID = 1L;
	private String pathJar="";


	public RunJarServletExample() {}
	public RunJarServletExample(String path) {
		this.pathJar=path;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		System.out.println("request"+pathJar.hashCode());    
	
		this.excuteSimulationByJar();
		ServletOutputStream stream=response.getOutputStream();
		stream.write(pathJar.getBytes());
		
		stream.close();
		


	}




	private void excuteSimulationByJar(){

		try {
			int start=0;
			int last=pathJar.lastIndexOf("/");
			String dirPath=pathJar.substring(start, last);
			String jarName=pathJar.substring(last+1, pathJar.length());
			//System.out.println("|"+dirPath+"|");
			//System.out.println("|"+jarName+"|");

			File f=new File(pathJar);
			f.setExecutable(true);
			ProcessBuilder pb = new ProcessBuilder(pathJar, "-jar", jarName);

			pb.directory(new File(dirPath));

			pb.redirectOutput(new File(dirPath+"/flockers.txt"));
			Process process = pb.start();
			//if(process.waitFor()==0){System.out.println("OK");}

			/*
			  String line=null;
		String output=null;
		BufferedReader in; 
			  InputStreamReader input= new InputStreamReader(process.getInputStream());
			in = new BufferedReader(input) ;

			while ((line = in.readLine()) != null  ) {

				output+=line;
			}
			input.close();
			in.close();	*/



		} catch (IOException e) {e.printStackTrace();

		} 

	}}
