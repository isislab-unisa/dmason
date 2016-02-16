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
		String simName=	(String) req.getAttribute("name");
		String rows=(String)	req.getAttribute("rows");
		String columns=(String)	req.getAttribute("cols");
		
		String aoi=	(String) req.getAttribute("aoi");
		String	width=	(String)req.getAttribute("width");
		String	height=	(String)req.getAttribute("height");
		String	numAgent=	(String)req.getAttribute("numag");
		String	mode=	(String)req.getAttribute("mode");;
		String	connection=	(String)req.getAttribute("type");
		//topic
    	
		
		String simPath=server.getSimulationsDirectories()+File.separator+simName+"_";
		server.createSimulationDirectoryByID(simPath);
	     	
		Simulation sim =new Simulation(simName, simPath, rows, columns, aoi, width, height, numAgent, mode, connection) ;
        ArrayList< String> topicList=new ArrayList<>();
		sim.setTopicList(topicList);
		server.addSim(sim);
		
		//send a tutti i worker la simulazione 
		for(String topicName: sim.getTopicList())
		server.getConnection().publishToTopic(sim, topicName, "newsim");
		
			
	}
}
