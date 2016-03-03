package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.util.connection.ConnectionType;

public class SubmitSimulationServlet extends HttpServlet {


	MasterServer server=null;
	final static String ACTIVEMQ="ActiveMQ";
	final static String MPI="MPI";
	HashMap<String,String> listParams = null;




	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if(req.getServletContext().getAttribute("masterServer")==null)
			return;

		server =(MasterServer)  req.getServletContext().getAttribute("masterServer");
		listParams = new HashMap<>();

		FileItem jarSim = null;

		if(!ServletFileUpload.isMultipartContent(req)){
			return;
		}
		else{
			FileItemFactory itemFact = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(itemFact);
			try{
				List<FileItem> items = upload.parseRequest(req);

				for(FileItem item : items) {
					if(!item.isFormField()){
						jarSim = item;
						//item.write(file);
					}else{
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
		String numStep= listParams.get("step");
		String conType= listParams.get("connectionType");
		String modeType = listParams.get("partitioning");
		String cells = listParams.get("cells");
		int mode = (modeType.equals("uniform"))?DistributedField2D.UNIFORM_PARTITIONING_MODE: DistributedField2D.NON_UNIFORM_PARTITIONING_MODE;

		//connection

		int connection=0;
		if(conType.equalsIgnoreCase(ACTIVEMQ))
			connection=ConnectionType.pureActiveMQ;

		if(conType.equalsIgnoreCase(MPI))
			System.out.println("STAI USANDO MPI ");
		
		//topics

		String topics[] =listParams.get("workers").split(",");
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

		Simulation sim=null;
		if(mode==DistributedField2D.UNIFORM_PARTITIONING_MODE)
		sim =new Simulation(simName, simPath,jarSim.getName() ,rows, columns, aoi, width, height, numAgent, numStep, mode, connection) ;
		else
		sim=new Simulation(simName, simPath, jarSim.getName(), cells, aoi, width, height, numAgent, numStep, mode, connection);	
		
	
		sim.setTopicList(topicList);
		int simId=server.getKeySim().incrementAndGet();
		sim.setSimID(simId);
		sim.setTopicPrefix(simName+"-"+simId);


		if(server.submitSimulation(sim)){
			resp.setStatus(HttpServletResponse.SC_OK);
		}

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

}
