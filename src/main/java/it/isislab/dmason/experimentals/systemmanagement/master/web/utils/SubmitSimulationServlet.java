package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.io.Files;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.master.MultiServerInterface;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;


public class SubmitSimulationServlet extends HttpServlet {


	MasterServer server=null;
	final static String ACTIVEMQ="ActiveMQ";


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;

		server =(MasterServer)  req.getServletContext().getAttribute("masterServer");





		//RECEIVE PARAMETER FROM CLIENT
		String simName=	(String) req.getParameter("simName");
		String rows=(String)	req.getParameter("rows");
		String columns=(String)	req.getParameter("cols");
		String aoi=	(String) req.getParameter("aoi");
		String	width=	(String)req.getParameter("width");
		String	height=	(String)req.getParameter("heigth");
		String	numAgent=	(String)req.getParameter("numAgents");
		int mode = DistributedField2D.UNIFORM_PARTITIONING_MODE;
		if(req.getParameter("non-uniform")!=null)
			mode= DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;


		///connction
		String	conType=	(String)req.getParameter("connectionType");
		int connection=0;
		if(conType.equalsIgnoreCase(ACTIVEMQ))
			connection=ConnectionType.pureActiveMQ;

		//topics

		String topics[] =((String)req.getParameter("workers")).split(",");





		ArrayList< String> topicList=new ArrayList<String>();

		for(String x: topics) 
			topicList.add(x);


		//// prova
		//		String simName=	"flockers";
		//		String rows="1";
		//		String columns="2";
		//		String aoi="1";
		//		String	width=	"200";
		//		String	height=	"200";
		//		String	numAgent="190";
		//		String	mode= "0";
		//
		//		ArrayList<String> topicList=new ArrayList<>();
		//		for(String x :server.infoWorkers.keySet()){
		//			topicList.add(x);
		//		}
		//		
		//		int connection=0;
		/////

		String simPath=server.getSimulationsDirectories()+File.separator+simName;
		//server.createSimulationDirectoryByID(simPath);
		server.createSimulationDirectoryByID(simName);
		
		if(ServletFileUpload.isMultipartContent(req)){
			System.out.println("nothing to do");
		}
		else{
			FileItemFactory itemFact = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(itemFact);
			try{
				List<FileItem> items = upload.parseRequest(req);
				for(FileItem item : items){
					File dir = new File(simPath);
					File file = File.createTempFile("SIM", ".jar", dir);
					item.write(file);
				}
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
		
		

		Simulation sim =new Simulation(simName, simPath, rows, columns, aoi, width, height, numAgent, mode, connection) ;

		sim.setTopicList(topicList);
		sim.setSimID(server.getKeySim().incrementAndGet());


		//upload jar in sim.getSimulationFolder() 

		server.submitSimulation(sim);



	}
}
