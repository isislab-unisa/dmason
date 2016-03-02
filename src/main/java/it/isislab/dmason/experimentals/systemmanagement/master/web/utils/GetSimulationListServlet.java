package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;

public class GetSimulationListServlet extends HttpServlet {

	MasterServer masterServer =null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;
		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		
		String message = "[";
        int startMessageSize = message.length();
        if(masterServer==null)
            return;
        for(Simulation s : masterServer.getSimulationsList().values()){
        	message+=s+",";
        }

        if(message.length() > startMessageSize)
            message=message.substring(0, message.length()-1)+"]";
        else
            message="[]";
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}
}
