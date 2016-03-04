package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetInfoForLogServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	MasterServer masterServer=null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;

		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		
		String idSimulation = (String)req.getParameter("id");
	    masterServer.logForSimulationByID(Integer.parseInt(idSimulation));
	   
			
	}

	@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
			doGet(req, resp);
		}
}
