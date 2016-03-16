package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public class GetWorkersInfoBySimIDServlet extends HttpServlet {

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
		if(idSimulation==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		PrintWriter p = resp.getWriter();
		int sim_id = Integer.parseInt(idSimulation);
		ArrayList<String> tp_list = masterServer.getSimulationsList().get(sim_id).getTopicList();
		HashMap<String, String> info_workers = masterServer.getInfoWorkers();
		if(info_workers==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String message = "{\"workers\":[";
		int startMessageSize = message.length();
		for(String w_tp: tp_list){
			message+=info_workers.get(w_tp).toString();
			message+=",";
		}
		if(message.length() > startMessageSize)
			message=message.substring(0, message.length()-1)+"]}";
		else
			message="";

		p.print(message);
		p.close();
	}
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException {
		doGet(req, resp);
	};
}
