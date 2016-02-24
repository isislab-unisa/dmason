package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.io.file.tfile.Utils;

import com.lowagie.text.Utilities;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.Util;
import it.isislab.dmason.util.connection.ConnectionType;

public class SubmitSimulationServlet extends HttpServlet {


	MasterServer server=null;
	final static String ACTIVEMQ="ActiveMQ";
	HashMap<String,String> listParams = null;




	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if(req.getServletContext().getAttribute("masterServer")==null)
			return;

		server =(MasterServer)  req.getServletContext().getAttribute("masterServer");
		listParams = new HashMap<>();
		
		FileItem jarSim = null;
		
		if(!ServletFileUpload.isMultipartContent(req)){
			System.out.println("nothing to do");
			return;
		}
		else{
			FileItemFactory itemFact = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(itemFact);
			try{
				List<FileItem> items = upload.parseRequest(req);
			
				for(FileItem item : items) {
					if(!item.isFormField()){
						System.out.println("visto "+item.getName());
						jarSim = item;
						//item.write(file);
					}else{
						System.out.println(item.getFieldName()+" "+item.getString());
						listParams.put(item.getFieldName(), item.getString());
					}
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		//RECEIVE PARAMETER FROM CLIENT		
		String simName= listParams.get("simName");
		String rows=	listParams.get("rows");
		String columns=	listParams.get("cols");
		String aoi=		listParams.get("aoi");
		String width=	listParams.get("width");
		String height=	listParams.get("heigth");
		String numAgent=listParams.get("numAgents");
		String conType= listParams.get("connectionType");
		int mode = DistributedField2D.UNIFORM_PARTITIONING_MODE;
		if(req.getParameter("non-uniform")!=null)
			mode= DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;
		
		//connection
		
		int connection=0;
		if(conType.equalsIgnoreCase(ACTIVEMQ))
			connection=ConnectionType.pureActiveMQ;

		//topics

		String topics[] =((String)req.getParameter("workers")).split(",");
		ArrayList< String> topicList=new ArrayList<String>();
		for(String x: topics) 
			topicList.add(x);

		String simPath=server.getSimulationsDirectories()+File.separator+simName;
		server.createSimulationDirectoryByID(simName);
		File dir = new File(simPath);
		File file = new File(dir, jarSim.getName());
		try {
			jarSim.write(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Simulation sim =new Simulation(simName, simPath, rows, columns, aoi, width, height, numAgent, mode, connection) ;

		sim.setTopicList(topicList);
		sim.setSimID(server.getKeySim().incrementAndGet());
		

		//upload jar in sim.getSimulationFolder() 

		server.submitSimulation(sim);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);

	}

}
