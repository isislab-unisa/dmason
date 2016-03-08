package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RemoveSimulationServlet extends HttpServlet {

	MasterServer masterServer =null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;
		
		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		
		String id= req.getParameter("id");
		masterServer.removeSimulationProcessByID(id);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}

}
