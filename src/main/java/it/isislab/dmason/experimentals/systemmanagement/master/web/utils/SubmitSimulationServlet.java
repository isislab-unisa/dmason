package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;


public class SubmitSimulationServlet extends HttpServlet {


	MasterServer server=null;



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if(req.getSession().getAttribute("masterServer")==null) return; 
		else server= (MasterServer)req.getSession().getAttribute("masterServer");





		//RECEIVE PARAMETER FROM CLIENT
		String simName=	(String) req.getParameter("simName");
		String rows=(String)	req.getParameter("rows");
		String columns=(String)	req.getParameter("cols");
		String aoi=	(String) req.getParameter("aoi");
		String	width=	(String)req.getParameter("width");
		String	height=	(String)req.getParameter("height");
		String	numAgent=	(String)req.getParameter("numAgents");
		String	mode=	(String)((req.getParameter("uniform")==null)?req.getParameter("non-uniform"):req.getParameter("uniform"));
		String	connection=	(String)req.getParameter("connectionType");
		
    	///i topic 
		
		String simPath=server.getSimulationsDirectories()+File.separator+simName+"_";
		server.createSimulationDirectoryByID(simPath);
	     	
		Simulation sim =new Simulation(simName, simPath, rows, columns, aoi, width, height, numAgent, mode, connection) ;
        ArrayList< String> topicList=new ArrayList<>();
		sim.setTopicList(topicList);
		sim.setSimID(server.getKeySim().incrementAndGet());
		server.submitSimulation(sim);
		
		
			
	}
}
