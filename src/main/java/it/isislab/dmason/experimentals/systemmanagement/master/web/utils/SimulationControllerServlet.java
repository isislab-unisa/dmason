package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MultiServerInterface;

public class SimulationControllerServlet extends HttpServlet {

	MultiServerInterface masterServer=null;
	
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
	if(req.getServletContext().getAttribute("masterServer")==null)
		return;

	masterServer =(MultiServerInterface) req.getServletContext().getAttribute("masterServer");
	
	String id = (String)req.getParameter("id");
    String op = (String)req.getParameter("op");
    
    System.out.println(id);
    System.out.println(op);
    
    if(id != null && op!=null){
        int i = Integer.parseInt(id);
        if(op.equals("play"))
            masterServer.start(i);
        else
            if(op.equals("stop"))
                masterServer.stop(i);
            else
                if(op.equals("stop"))
                   masterServer.pause(i);
    }else
    	System.out.println("non sono entrato");
		
}


@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		doGet(req, resp);
	}
}
