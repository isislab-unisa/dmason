package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public class CreateSimulationFolderServlet extends HttpServlet {

	MasterServer server;
	public CreateSimulationFolderServlet(MasterServer master) {
	server=master;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	PrintWriter out=resp.getWriter();
	String folder=req.getParameter("simname");
	server.createSimulationDirectoryByID(folder);	
	resp.setStatus(HttpServletResponse.SC_OK);
	out.println("<h1>Directory Creata<h1>");
	}
}
