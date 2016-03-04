package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MultiServerInterface;

public class GetConnectedWorkersServlet extends HttpServlet {

	MultiServerInterface myServer =null;


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null)
			return; 	
		
		myServer = (MultiServerInterface) req.getServletContext().getAttribute("masterServer");
		String message = "{\"workers\":[";
		myServer.checkAllConnectedWorkers();
		PrintWriter p = resp.getWriter();
		
		int startMessageSize = message.length();
		
		for(String info : myServer.getInfoWorkers().values()){
			message+=info+",";
		}
		if(message.length() > startMessageSize)
			message=message.substring(0, message.length()-1)+"]}";
		else
			message="";

		p.print(message);
//		req.setAttribute("message", message);
//        req.getRequestDispatcher("/index.jsp").forward(req, resp);

        p.close();
	
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

}
