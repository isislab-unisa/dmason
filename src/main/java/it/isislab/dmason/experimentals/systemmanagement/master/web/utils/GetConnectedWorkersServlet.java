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
	
	public GetConnectedWorkersServlet(MultiServerInterface server){
		myServer = server;
	}
	
		
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		  resp.setContentType("application/json");
		  PrintWriter out = resp.getWriter();
		  myServer.checkAllConnectedWorkers();
		  try{
			  Thread.sleep(5000);
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		  for(String info : myServer.getInfoWorkers().values()){
			  out.println(info);
		  }
			  
			  
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

}
