package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public class LoadExampleSimulationsServlet extends HttpServlet{



	private static final long serialVersionUID = 1L;
	MasterServer masterServer=null;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain;charset=UTF-8");
		if(req.getServletContext().getAttribute("masterServer")==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		masterServer=(MasterServer) req.getServletContext().getAttribute("masterServer");
		JSONObject simjar;
		JSONArray examples_list = new JSONArray();
		JSONArray customs_list = new JSONArray();
		PrintWriter printer = resp.getWriter();
		
		
	   
	   HashMap<String , String> temp=masterServer.getListExampleSimulationsJars();
	   
	   for(String nameFile: temp.keySet()){
		   simjar=new JSONObject();
		   simjar.put("name", nameFile);
		   simjar.put("path", temp.get(nameFile));
		   examples_list.add(simjar);
	   }
	
	   JSONObject examples=new JSONObject();
	   examples.put("examples", examples_list);
	
	   
	   temp= masterServer.getListCustomSimulationsJars();
	   for(String nameFile: temp.keySet()){
		   simjar=new JSONObject();
		   simjar.put("name", nameFile);
		   simjar.put("path", temp.get(nameFile));
		   customs_list.add(simjar);
	   }
	
	   JSONObject customs=new JSONObject();
	   customs.put("customs", customs_list);
	   
	   
	   
	   JSONArray jars_list=new JSONArray();
	   jars_list.add(examples);
	   jars_list.add(customs);
	   
	   JSONObject jars=new JSONObject();
	   jars.put("jars", jars_list);
	   
	   
	   
	   StringWriter writer=new StringWriter();
	   jars.writeJSONString(writer);
	   String jSon=writer.toString();
	   printer.print(jSon);
	   printer.close();
	   
	   
	}



@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	// TODO Auto-generated method stub
	doGet(req, resp);
}
}
