package it.isislab.dmason.sim.field.network.kway.algo.jabeja;

import edu.cmu.graphchi.*;
import edu.cmu.graphchi.engine.GraphChiEngine;
import edu.cmu.graphchi.engine.VertexInterval;
import edu.cmu.graphchi.preprocessing.EdgeProcessor;
import edu.cmu.graphchi.preprocessing.FastSharder;
import edu.cmu.graphchi.preprocessing.VertexProcessor;
import it.isislab.dmason.sim.field.network.kway.algo.interfaces.PartitioningAlgorithm;
import it.isislab.dmason.sim.field.network.kway.graph.tools.VertexParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;


/**
 * Example application: JabeJa
 * Iteratively computes a balanced partitioning of a given graph
 * by performing local operations at each vertex.
 * 
 * You can find this code at https://github.com/fatemehr/jabeja.
 * 
 * This code has been edited by Alessia Antelmi and Carmine Spagnuolo.
 * Changes made in the method update():
 * 		- ghost vertices (added by GraphChi) will be not processed
 * 		- control over the initialization of the partitions
 * 		- prevent a node to swap with a ghost one
 * Other changes:
 * 		- all initializations to start the GraphChiEngine take place in the method partitioning()
 * 
 * @author Fatemeh Rahimian
 */
public class JabeJa implements GraphChiProgram<Integer[], Integer[]>, PartitioningAlgorithm {
	private  Random rnd = new Random(4242);
	private  TreeMap<Integer, String> originalIds;
	private  Logger logger = ChiLogger.getLogger("jabeja");
	public  int numPartitions = 0;	
	public float TEMPERATURE = (float) 2;			// if set to 1, it means no simulated annealing is applied
	public float TEMPERATUREDelta = (float) 0.003;

	private static enum MType {
		INFO, ACK, NACK, SWAP
	}
	private  final int ISWAITING = 0;
	private  final int SENDER = 0;
	private  final int COLOR = 1;
	private  final int TYPE = 2;

	public  Integer[] colorInventory ;

	private static AtomicInteger cut;
	private static MessageRelay mail = new MessageRelay();

	protected float getTEMPERATURE() {
		return TEMPERATURE;
	}

	protected void setTEMPERATURE(float tEMPERATURE) {
		TEMPERATURE = tEMPERATURE;
	}

	protected float getTEMPERATUREDelta() {
		return TEMPERATUREDelta;
	}

	protected void setTEMPERATUREDelta(float tEMPERATUREDelta) {
		TEMPERATUREDelta = tEMPERATUREDelta;
	}

	// -----------------------------------------------------------------
	public void update(ChiVertex<Integer[], Integer[]> vertex, GraphChiContext context)  {

		/* ghost vertex are not processed */
		if(!originalIds.containsKey(context.getVertexIdTranslate().backward(vertex.getId())))
			return;
			
		if (context.getIteration() == 0) {		/* Initialize on first iteration */

			int vertexColor;
			int isVertexWaiting;
			vertexColor = rnd.nextInt(numPartitions);
			long numRealVertices = originalIds.size();
			
			synchronized(colorInventory){
				
				if(numRealVertices%numPartitions == 0){
					while (colorInventory[vertexColor] >= numRealVertices /numPartitions)
						vertexColor = (vertexColor + 1) % numPartitions;
					
					colorInventory[vertexColor]++;	
				}
				else{
					while (colorInventory[vertexColor] > numRealVertices /numPartitions)
						vertexColor = (vertexColor + 1) % numPartitions;
					
					colorInventory[vertexColor]++;
				}
			}

			isVertexWaiting = 0;
			Integer[] nodeValue = { isVertexWaiting, vertexColor };
			vertex.setValue(nodeValue);

			// format [sender, sender color, MType, color distribution]
			Integer[] info = new Integer[3 + numPartitions];
			Arrays.fill(info, -1);
			info[SENDER] = vertex.getId();
			info[TYPE] = MType.INFO.ordinal();
			info[COLOR] = vertexColor;

			for(int i=0; i<vertex.numOutEdges(); i++) {
				vertex.outEdge(i).setValue(info);
			}

		} else {	// for all other iterations
			int inDegree = vertex.numInEdges();
			int outDegree = vertex.numOutEdges();

			// node to inEdge/outEdge mapping <VertexID , edgeIndex>
			HashMap<Integer, Integer> inNeighborsMap = new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> outNeighborsMap = new HashMap<Integer, Integer>();

			HashMap<Integer, Integer[]> inEdges = new HashMap<Integer, Integer[]>();
			HashMap<Integer, Integer[]> outEdges = new HashMap<Integer, Integer[]>();

			// STEP 1: read all the in/out values
			Integer[] nodeValue = vertex.getValue();

			int[] population = new int[numPartitions];
			Arrays.fill(population, 0);

			for (int i = 0; i < inDegree; i++) {
				inNeighborsMap.put(vertex.inEdge(i).getVertexId(), i);
				inEdges.put(i, vertex.inEdge(i).getValue());
				population[vertex.inEdge(i).getValue()[COLOR]]++;
			}
			for (int i = 0; i < outDegree; i++) {
				outNeighborsMap.put(vertex.getOutEdgeId(i), i);
				outEdges.put(i, vertex.getOutEdgeValue(i));
			}

			String logString = "";
			logString += "node id:" + vertex.getId() + "\tcolor:" + nodeValue[COLOR] + "\tisWaiting:" + nodeValue[ISWAITING] + "\tinNeighbors:";
			for (Integer i: inNeighborsMap.keySet())
				logString += i + " ";
			logString += "\toutNeighbors:";
			for (Integer i: outNeighborsMap.keySet())
				logString += i + " ";

			logString += "\tpopulation:[ ";
			for (int i= 0; i < numPartitions; ++i)
				logString += population[i] + " ";
			logString += "]";
			int c = inDegree - population[vertex.getValue()[COLOR]];
			logString += "\tcut:" + c;
			logger.info(logString);

			// read from mail (this does not update the population)
			ArrayList<Integer[]> messages = new ArrayList<Integer[]>(mail.get(vertex.getId()));

			// STEP 2: process / update state
			ArrayList<Integer[]> swapRequests = new ArrayList<Integer[]>();
			Integer[] inValue;
			Integer[] outValue;
			int outIndex = 0;

			for (Integer[] mailreq : messages) {

				logger.info("received a mail from node:" + mailreq[SENDER] + " with type:" + mailreq[TYPE]);


				MType mType = MType.values()[mailreq[TYPE]];


				if (mType == MType.ACK) {
					nodeValue[COLOR] = mailreq[COLOR];
					nodeValue[ISWAITING] = 0;			// value 0 indicates that isVertexWaiting = false;
					vertex.setValue(nodeValue);
				} else if (mType == MType.NACK) {
					nodeValue[ISWAITING] = 0;			// value 0 indicates that isVertexWaiting = false;
					vertex.setValue(nodeValue);
				}
				else if (mType == MType.SWAP) {
					swapRequests.add(mailreq);
				}
			}

			for (int i = 0; i < inDegree; i++) {

				inValue = inEdges.get(i);
				MType inType = MType.values()[inValue[TYPE]];

				logger.info("invalue[SENDER] --> " + inValue[SENDER]);
				logger.info("map --> " + outNeighborsMap);

				outIndex = outNeighborsMap.get(inValue[SENDER]);

				if (inType == MType.INFO) {
					// no state update, just reply with INFO
					outValue = outEdges.get(outIndex);
					outValue[TYPE] = MType.INFO.ordinal();
					outEdges.put(outIndex, outValue);
				}
				else if (inType == MType.ACK) {
					// change your color, but before that make corrections to population array
					population[nodeValue[COLOR]]++;
					population[inValue[COLOR]]--;

					nodeValue[COLOR] = inValue[COLOR];
					nodeValue[ISWAITING] = 0;	// value 0 indicates that isVertexWaiting = false;
					vertex.setValue(nodeValue);

					outValue = outEdges.get(outIndex);
					outValue[TYPE] = MType.INFO.ordinal();
					outEdges.put(outIndex, outValue);
				}
				else if (inType == MType.NACK) {
					nodeValue[ISWAITING] = 0;
					vertex.setValue(nodeValue);

					outValue = outEdges.get(outIndex);
					outValue[TYPE] = MType.INFO.ordinal();
					outEdges.put(outIndex, outValue);
				}
				else if (inType == MType.SWAP) {
					if (nodeValue[ISWAITING] == 0) {
						swapRequests.add(inValue);
					}
					else {
						outValue = outEdges.get(outIndex);
						outValue[TYPE] = MType.NACK.ordinal();
						outEdges.put(outIndex, outValue);
					}
				}
			}

			boolean colorChanged = false;
			// process the swap requests, if any
			if (nodeValue[ISWAITING] == 0 && swapRequests.size() > 0) {
				logger.info(vertex.getId() + " is proccessing " + swapRequests.size() + " swap request(s).");
				Collections.shuffle(swapRequests, rnd);			// shuffle all the incoming requests
				for (Integer[] req: swapRequests) {

					if (inNeighborsMap.containsKey(req[SENDER])) { // the request is sent by a neighbor, so update the edge value

						if (colorChanged == false && swap(vertex.getValue()[COLOR], population, req, true) > 0) {

							outIndex = outNeighborsMap.get(req[SENDER]);
							Integer[] edgeValue = vertex.outEdge(outIndex).getValue();
							edgeValue[TYPE] = MType.ACK.ordinal();
							edgeValue[COLOR] = nodeValue[COLOR];
							outEdges.put(outIndex, edgeValue);

							nodeValue[COLOR] = req[COLOR];
							nodeValue[ISWAITING] = 0;
							vertex.setValue(nodeValue);

							logger.info("****** Swap request is accepted");

							colorChanged = true;	
						} 
						else {
							outIndex = outNeighborsMap.get(req[SENDER]);
							Integer[] edgeValue = vertex.outEdge(outIndex).getValue();
							edgeValue[TYPE] = MType.NACK.ordinal();
							outEdges.put(outIndex, edgeValue);

						}
					}
					else {	// the request is sent by a random node
						if (colorChanged == false && swap(vertex.getValue()[COLOR], population, req, false) > 0) {

							Integer[] response = new Integer[3 + numPartitions];
							Arrays.fill(response, -1);
							response[SENDER] = vertex.getId();
							response[TYPE] = MType.ACK.ordinal();	 
							response[COLOR] = nodeValue[COLOR];
							mail.send(req[SENDER], response);
							logger.info("sending an ACK response to node " + req[SENDER]);

							nodeValue[COLOR] = req[COLOR];
							nodeValue[ISWAITING] = 0;
							vertex.setValue(nodeValue);

							colorChanged = true;
						}
						else {
							Integer[] response = new Integer[3 + numPartitions];
							Arrays.fill(response, -1);
							response[SENDER] = vertex.getId();
							response[TYPE] = MType.NACK.ordinal();
							mail.send(req[SENDER], response);
							logger.info("sending a NACK response to node " + req[SENDER]);

						}
					}

				}
			}
			else if (swapRequests.size() > 0) {
				logger.info(vertex.getId() + " has to reject " + swapRequests.size() + " swap request(s).");

				for (Integer[] req: swapRequests) {
					if (inNeighborsMap.containsKey(req[SENDER])) { // the request is sent by a neighbor, so update the edge value
						outIndex = outNeighborsMap.get(req[SENDER]);
						Integer[] edgeValue = vertex.outEdge(outIndex).getValue();
						edgeValue[TYPE] = MType.NACK.ordinal();
						outEdges.put(outIndex, edgeValue);
					}
					else {
						Integer[] response = new Integer[3 + numPartitions];
						Arrays.fill(response, -1);
						response[SENDER] = vertex.getId();
						response[TYPE] = MType.NACK.ordinal();
						mail.send(req[SENDER], response);
						logger.info("sending a NACK response to node " + req[SENDER]);
					}
				}
			}


			logger.info("node id:" + vertex.getId() + "\tcolor:" + nodeValue[COLOR] + "\tisWaiting:" + nodeValue[ISWAITING]);

			// Send a swap request, either to a neighbor or to a random node in the graph
			// first, try to find the best neighbor to swap with, if no good local swap is possible select a random node
			if (nodeValue[ISWAITING] == 0) {
				int bestNeighbor;
				if (outDegree > 0 && (bestNeighbor = selectBestNeighbor(vertex.getValue()[COLOR], population, outEdges)) != -1 ) {
					Integer[] edgeValue = vertex.getOutEdgeValue(bestNeighbor);
					edgeValue[SENDER] = vertex.getId();
					edgeValue[COLOR] = nodeValue[COLOR];
					edgeValue[TYPE] = MType.SWAP.ordinal();
					for (int i = 0; i < numPartitions; i++)
						edgeValue[3 + i] = population[i];

					logger.info("sending swap request... to neighbor:" + bestNeighbor);
					outEdges.put(bestNeighbor, edgeValue);
					nodeValue[ISWAITING] = 1;

				}
				else {
					Integer[] req = new Integer[3 + numPartitions];
					req[SENDER] = vertex.getId();
					req[COLOR] = nodeValue[COLOR];
					req[TYPE] = MType.SWAP.ordinal();
					for (int i = 0; i < numPartitions; i++)
						req[3 + i] = population[i];

					Long N = context.getNumVertices(); //- numG
					Integer randomNodeId = rnd.nextInt(N.intValue());
					/* nodes can't swap with ghost nodes */
					if (vertex.getId() != randomNodeId && !inNeighborsMap.containsKey(randomNodeId) && originalIds.containsKey(context.getVertexIdTranslate().backward(randomNodeId))) {
						mail.send(randomNodeId, req);
						nodeValue[ISWAITING] = 1;
					}
					logger.info("sending swap request... to random node id:" + randomNodeId);
				}
				vertex.setValue(nodeValue);
			}


			// write the corresponding out-values
			for (int i = 0; i < outDegree; i++) {
				Integer[] edgeValue = outEdges.get(i);

				for (int j= 0; j < numPartitions; j++)
					edgeValue[3 + j] = population[j];
				if (edgeValue[TYPE] != MType.ACK.ordinal()) // if you are not sending an ACK send your current color to the neighbor, in case of ACK let it be the old color
					edgeValue[COLOR] = nodeValue[COLOR];

				vertex.outEdge(i).setValue(edgeValue);

				logger.info("outedgeValue for neighbor:" + i + " is [sender:" + edgeValue[SENDER] + " ,type:" + edgeValue[TYPE] + "]");
			}

			cut.addAndGet(inDegree - population[vertex.getValue()[COLOR]]);
		}
	}

	// -----------------------------------------------------------------
	private int selectBestNeighbor(int selfColor, int[] localPopulation, HashMap<Integer, Integer[]> outEdges) {
		int bestId = -1;
		double best = 0;
		double benefit;

		for (int id : outEdges.keySet()) {
			benefit = swap(selfColor, localPopulation, outEdges.get(id), true);
			if (benefit > best) {
				best = benefit;
				bestId = id;
			}
		}

		return bestId;
	}

	// -----------------------------------------------------------------
	private double swap(int selfColor, int[] localPopulation, Integer[] inValue, boolean isNeighbor) {
		float coefficient;

		int tempNodeColor = inValue[COLOR];

		if (tempNodeColor == selfColor)
			return 0;

		int c1SelfNodeBenefit = localPopulation[selfColor];
		int c1TempNodeBenefit = inValue[3+tempNodeColor];	// the population of current color

		int c2SelfNodeBenefit = localPopulation[tempNodeColor];
		int c2TempNodeBenefit = inValue[3+selfColor];		// the population of (possibly) future color

		if (isNeighbor == true) {	// if the swap of two neighbors are considered, the expected populations are overestimated by one for each node
			c2SelfNodeBenefit--;
			c2TempNodeBenefit--;
			coefficient = 1;
		}
		else
			coefficient = TEMPERATURE;

		double oldBenefit = Math.pow(c1SelfNodeBenefit, 2) + Math.pow(c1TempNodeBenefit, 2);
		double newBenefit = Math.pow(c2SelfNodeBenefit, 2) + Math.pow(c2TempNodeBenefit, 2);


		return newBenefit * coefficient - oldBenefit;
	}

	// -----------------------------------------------------------------
	public void beginIteration(GraphChiContext ctx) {
		TEMPERATURE -= TEMPERATUREDelta;
		if (TEMPERATURE < 1)
			TEMPERATURE = 1;

		cut = new AtomicInteger(0);
	}

	public void endIteration(GraphChiContext ctx) {
		logger.setLevel(Level.OFF);
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><<<<<<<<>>>>>>>>>>>>>>>>><<>>> iteration: " + ctx.getIteration() + " edge cut = " + cut.intValue() / 2);
		//logger.setLevel(Level.OFF);

	}

	public void beginInterval(GraphChiContext ctx, VertexInterval interval) {}

	public void endInterval(GraphChiContext ctx, VertexInterval interval) {}

	public void beginSubInterval(GraphChiContext ctx, VertexInterval interval) {}

	public void endSubInterval(GraphChiContext ctx, VertexInterval interval) {}

	/**
	 * Initialize the sharder-program.
	 * @param graphName
	 * @param numShards
	 * @return
	 * @throws java.io.IOException
	 */
	protected static FastSharder<Integer[], Integer[]> createSharder(String graphName, int numShards,final int _k) throws IOException {

		return new FastSharder<Integer[], Integer[]>(graphName, numShards, new VertexProcessor<Integer[]>() {

			public Integer[] receiveVertexValue(int vertexId, String token) {
				return new Integer[2];
			}
		},

		new EdgeProcessor<Integer[]>() {
			public Integer[] receiveEdge(int from, int to, String token) {
			
				Integer[] val = new Integer[3 + _k];
				for (int i = 0; i < 3 + _k; i++)
					val[i] = 0;
				return val;
			}
		}, new IntArrayConverter(2), new IntArrayConverter(_k + 3));
	}

	@Override
	public String partitioning() throws IOException, InterruptedException {
		
		JabeJa j = new JabeJa();
		j.originalIds = VertexParser.getOriginalIdFromEdgelist(baseFilename);
	
		j.colorInventory = new Integer[numP];	
		j.numPartitions = numP;
		j.TEMPERATURE = temp;
		j.TEMPERATUREDelta = tempDelta;
		
		logger.setLevel(Level.OFF);
		Logger log = Logger.getLogger("jabeja-test");
		
		log.info("start test on " + baseFilename + " with niter " + niter + " numP " + numP +" temp " + temp + " tempDelta " + tempDelta);
		
		long startTime = System.currentTimeMillis();

		try{
			logger.addHandler(new FileHandler(baseFilename + "_log.txt"));

			/* Create shards */
			FastSharder<Integer[], Integer[]> sharder = createSharder(baseFilename, nShards, numP);
			if (baseFilename.equals("pipein")) {     // Allow piping graph in
				sharder.shard(System.in, fileType);
			} else {
				if (!new File(ChiFilenames.getFilenameIntervals(baseFilename, nShards)).exists()) {
					sharder.shard(new FileInputStream(new File(baseFilename)), fileType);
				} else {
					logger.info("Found shards -- no need to preprocess");
				}
			}
	
			Arrays.fill(j.colorInventory, 0);

			/* Run GraphChi ... */
			GraphChiEngine<Integer[], Integer[]> engine = new GraphChiEngine<Integer[], Integer[]>(baseFilename, nShards);

			ChiLogger.getLogger("engine").setLevel(Level.OFF);
			ChiLogger.getLogger("vertex-data").setLevel(Level.OFF);
			ChiLogger.getLogger("fast-sharder").setLevel(Level.OFF);
		
			engine.setEdataConverter(new IntArrayConverter(numP + 3));
			engine.setVertexDataConverter(new IntArrayConverter(2));
			engine.setModifiesInedges(false); // Important optimization
			engine.run(j, niter);	
		
			logger.info(" engine.numVertices " + engine.numVertices());

			logger.info("Ready. Going to output...");
		
			log.info("Finished in " + (System.currentTimeMillis() - startTime) + "ms");
		
			String[] w = baseFilename.split(File.separator);
			String completeGraphName = w[w.length -1];
			String[] graphName = completeGraphName.split("\\.");
				
			File output = new File(System.getProperty("user.dir") + File.separator + "jabeja-output");
			
			if(!output.exists())
				output.mkdir();
			
			String outputFilename = output.getAbsolutePath() + File.separator + graphName[0] + ".graph.part." + numP; 
									
			/* Process output */
			PartitionAnalysis.writeVertexPartitions(logger, baseFilename, outputFilename, engine.numVertices(), numP, engine.getVertexIdTranslate(), vertex_names);
			
			log.info("Finished. See file: " + baseFilename + ".graph.part." + numP);	
			
			return outputFilename;
		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public JabeJa(String baseFilename, int nShards, String fileType, 
					int niter, int numP, float temp, float tempDelta, Integer[] vertex_names){
		this.baseFilename = baseFilename;
		this.nShards = nShards;
		this.fileType = fileType;
		this.niter = niter;
		this.numP = numP;
		this.temp = temp;
		this.tempDelta = tempDelta;
		this.vertex_names = vertex_names;
	}
	
	public JabeJa(){}
	
 private String baseFilename;
 private int nShards;
 private String fileType;
 private int niter;
 private int numP;
 private float temp;
 private float tempDelta;
 private Integer[] vertex_names;
}














