package it.isislab.dmason.experimentals.systemmanagement.master.web.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;
import it.isislab.dmason.experimentals.systemmanagement.utils.Simulation;
import it.isislab.dmason.experimentals.util.visualization.globalviewer.RemoteSnap;


public class ImageRequestServlet extends HttpServlet {

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
		String _step = (String)req.getParameter("step");
		if(idSimulation==null || _step == null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		Simulation s = masterServer.getSimulationsList().get(Integer.parseInt(idSimulation));

		if (s==null){
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		long key = Long.parseLong(_step);

		PrintWriter p = resp.getWriter();

		/**
		 * snapshot:{
		 * 		"step":{
		 *			"cellid": "base64code" 			
		 * 			.
		 * 			.	
		 * 			.
		 * 		}
		 * }
		 */

		
		JSONObject img = new JSONObject();
		JSONObject j_step = new JSONObject();

		ArrayList<RemoteSnap> list = s.getSnapshots().get(key);
		for(RemoteSnap rs : list){
			img.put(rs.i+"-"+rs.j,Base64.getEncoder().encodeToString(rs.image));
		}
		j_step = new JSONObject();

		j_step.put("step",img);
		

		JSONObject snapshot = new JSONObject();
		snapshot.put("snapshot", j_step);

		StringWriter out = new StringWriter();

		snapshot.writeJSONString(out);						

		String jsonText = out.toString();
		System.out.println(jsonText);
		p.print(jsonText);
		p.close();

	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

}
