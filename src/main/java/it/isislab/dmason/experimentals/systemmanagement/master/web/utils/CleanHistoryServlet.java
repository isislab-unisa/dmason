package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

/**
 * Delete all files in the History folder
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class CleanHistoryServlet extends HttpServlet {


	private static final long serialVersionUID = 1L;
	MasterServer myServer =null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return; 	
		}
		
		myServer = (MasterServer) req.getServletContext().getAttribute("masterServer");
		String hitory_pathName = myServer.getMasterHistory();
		File f_history = new File(hitory_pathName);
		if(!f_history.exists() || !f_history.isDirectory()){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return; 	
		}
		
			
		myServer.deleteHistoryFolder();
		
		resp.setStatus(HttpServletResponse.SC_OK);
		
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}
}
