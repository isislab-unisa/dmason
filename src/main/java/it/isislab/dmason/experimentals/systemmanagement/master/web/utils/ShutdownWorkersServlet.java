package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public class ShutdownWorkersServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MasterServer masterServer =null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null)
			return;
		
		masterServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		masterServer.shutdownAllWorkers();
		
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
	}
	
}
