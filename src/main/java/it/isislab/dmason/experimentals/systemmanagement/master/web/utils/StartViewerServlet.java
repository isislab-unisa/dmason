package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;

public class StartViewerServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MasterServer masterServer=null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String idSimulation = (String)req.getParameter("id");
		String command = (String)req.getParameter("cmd");
		if(idSimulation==null || command == null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Simulation s = masterServer.getSimulationsList().get(Integer.parseInt(idSimulation));

		if (s==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		masterServer.showImage(Integer.parseInt(idSimulation),command);
	}

	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
		
	};
}
