package it.isislab.dmason.experimentals.systemmanagement.bashConsole;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasterShell {

	public static final Logger err_log = Logger.getLogger(MasterShell.class.getName());
	private static final String MASTER_URL ="http://localhost:8080";
	private final String FAKE_USER_AGENT = "Mozilla/5.0";
	String charset = "UTF-8";
	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
	String CRLF = "\r\n"; // Line separator required by multipart/form-data.

	URL obj= null;
	HttpURLConnection con =null;
	public MasterShell() {}

	//String url = "http://www.google.com/search?q=mkyong";
	private String[] getWorkers(){
		try{
			String url_req = MASTER_URL.concat("/getWorkers");

			obj = new URL(url_req);
			con = (HttpURLConnection) obj.openConnection();	
			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", FAKE_USER_AGENT);

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//print result
			JSONParser parser = new JSONParser();
			JSONObject j_workers = (JSONObject)parser.parse(response.toString());
			JSONArray worker = (JSONArray)j_workers.get("workers");

			if(worker !=null){
				String[] W_IDs = new String[worker.size()]; 
				for(int i=0; i< worker.size(); i++){
					JSONObject o = (JSONObject) worker.get(i);
					W_IDs[i]=(String)o.get("workerID");
				}
				return W_IDs; 
			}



		}catch(MalformedURLException e){
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}catch(IOException e){
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		} catch (ParseException e) {
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}
		return null;
	}

	public void submitSimulation(String[] args){
		String url_req = MASTER_URL.concat("/submitSimulation");

		//simName rows cols aoi width height numAgents step connectionType="activemq" partitioning="uniform" exampleSimulation workers[]
		HashMap<String, String> m_args = new HashMap<>();
		m_args.put("exampleSimulation", args[0]);
		m_args.put("simName", args[1]);
		m_args.put("rows", args[2]);
		m_args.put("cols", args[3]);
		m_args.put("aoi", args[4]);
		m_args.put("width", args[5]);
		m_args.put("height", args[6]);
		m_args.put("numAgents", args[7]);
		m_args.put("step", args[8]);
		//default value
		m_args.put("connectionType", "activemq");
		m_args.put("partitioning", "uniform");

		String req_arg = "";
		for(String k: m_args.keySet()){
			req_arg = req_arg.concat(k+"="+m_args.get(k)+"&");
		}
		String [] workers = getWorkers();
		for (int i = 0; i < workers.length; i++) {
			req_arg = req_arg.concat("workers="+workers[i]+"&");
		}
		req_arg=req_arg.substring(0, req_arg.length()-1);





		try {
			//obj = new URL(url_req.concat(req_arg));
			URL url = new URL(url_req);
			con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);

			con.setDoOutput(true); // indicates POST method
			con.setDoInput(true);
			con.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);

			OutputStream output = con.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

			File uploadFile = new File(m_args.get("exampleSimulation"));
			String fileName = uploadFile.getName();
			writer.append("--" + boundary).append(CRLF);
			writer.append(
					"Content-Disposition: form-data; name=\"" + "simExe"
							+ "\"; filename=\"" + fileName + "\"")
			.append(CRLF);
			writer.append(
					"Content-Type: "
							+ URLConnection.guessContentTypeFromName(fileName))
			.append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF);
			writer.flush();

			FileInputStream inputStream = new FileInputStream(uploadFile);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			output.flush();
			inputStream.close();

			writer.append(CRLF);
			writer.flush();   

			// Send normal param.
			for(String k: m_args.keySet()){

				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\""+k+"\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
				writer.append(CRLF).append(m_args.get(k)).append(CRLF).flush();
			}

			for (int i = 0; i < workers.length; i++) {
				writer.append("--" + boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"workers\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
				writer.append(CRLF).append(workers[i]).append(CRLF).flush();
			}


			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF).flush();
			writer.close();
			// checks server's status code first
			List<String> response = new ArrayList<String>();
			int status = con.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String line = null;
				while ((line = reader.readLine()) != null) {
					response.add(line);
				}
				reader.close();
				con.disconnect();
			} else {
				throw new IOException("Server returned non-OK status: " + status);
			}

		} catch (MalformedURLException e) {
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}catch (IOException e) {
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}

	}

	private String getSimulationList(){
		String url_req = MASTER_URL.concat("/simulationList");


		try {
			obj = new URL(url_req);

			con = (HttpURLConnection) obj.openConnection();	
			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", FAKE_USER_AGENT);

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();


			return response.toString();
		} catch (MalformedURLException e) {
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		} catch (IOException e) {
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}
		return null;


	}


	private JSONArray getSimulationListasJson(){

		String str_list = getSimulationList();
		if(str_list.equals(""))
			return null;
		//print result
		JSONParser parser = new JSONParser();
		JSONObject sims_list;
		try {
			sims_list = (JSONObject)parser.parse(str_list);
			JSONArray sims = (JSONArray)sims_list.get("simulations");

			return sims;
		} catch (ParseException e) {
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}
		return null;
		
	}
	
	private void testing_StartFirstSimulation(){
		JSONArray sims = getSimulationListasJson();
		int sim_id;
		if(sims!=null){
			sim_id = Integer.parseInt((String)((JSONObject)sims.get(0)).get("id"));
			startSimulation(sim_id);
		}else{
			err_log.log(Level.SEVERE, "Someting was wrong");
			System.exit(-1);
		}
		
	}

	private void startSimulation(int SIM_ID) {
		
		try{
			String url_req = MASTER_URL.concat(String.format("/simulationController?id=%d&op=play",SIM_ID));


			obj = new URL(url_req);
			con = (HttpURLConnection) obj.openConnection();	
			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", FAKE_USER_AGENT);

			int responseCode = con.getResponseCode();
		
		}catch(MalformedURLException e){
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}catch(IOException e){
			err_log.log(Level.SEVERE, e.getMessage(), e);
			System.exit(-1);
		}

	}
	
	private String testing_getFirstSimulationStatus() {
		JSONArray sims = getSimulationListasJson();
		if(sims == null)
			return "";
		JSONObject sim = (JSONObject)sims.get(0);
		int id = Integer.parseInt((String)sim.get("id"));
		return getSimulationStatus(id);
	}


	private String getSimulationStatus(int id) {
		JSONArray sims = getSimulationListasJson();
		if(sims == null)
			return "";
		int sim_id=-1;
		JSONObject sim =null;
		for (int i = 0; i < sims.size(); i++) {
			 sim = (JSONObject)sims.get(i);
			sim_id = Integer.parseInt((String)sim.get("id"));
			if(sim_id == id)
				break;
		}
		
		return (sim!=null)?(String)sim.get("status"):"";
		
		
	}

	public static void main(String[] args) {
		String cmd = args[0].replace("-", "");
		String[] cmd_args = null;
		MasterShell ms = new MasterShell();
		if (args.length >1){
			cmd_args = new String[args.length-1];
			for (int i = 0; i < args.length-1; i++) {
				cmd_args[i]=args[i+1];
			}
		}
		switch (cmd) {
		case "submitSim":
			if(cmd_args.length > 0)
				//jarFile SimName R C AOI W H numAgents Steps
				ms.submitSimulation(cmd_args);
			break;
		case "startSim":
			ms.testing_StartFirstSimulation();
			break;
		case "simStatus":
			System.out.println(ms.testing_getFirstSimulationStatus());
			break;
		default:
			break;
		}
	}

	



}
