package dmason.sim.field.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.apache.activemq.broker.SslBrokerService;
import org.apache.commons.codec.language.Nysiis;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Double2D;
import sun.security.x509.DeltaCRLIndicatorExtension;
import dmason.sim.engine.DistributedState;
import dmason.sim.engine.RemoteAgent;
import dmason.sim.field.CellType;
import dmason.sim.field.Entry;
import dmason.sim.field.Region;
import dmason.sim.field.UpdateMap;
import dmason.sim.field.continuous.DContinuous2D;
import dmason.util.connection.Connection;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;
import dmason.util.visualization.VisualizationUpdateMap;

public class DNetwork<E> extends DNetworkAbstract<E> {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(DNetwork.class
			.getCanonicalName());

	// classe che gestisce le sottoscrizioni
	private Mappa mappa;
	private ArrayList<OutEntry> listaOut;
	private ArrayList<OutEntry> listaOutCache;
	public UpdateMapNet<Bag> updates = new UpdateMapNet<Bag>();
	private ArrayList<NetworkMessageListener> listeners = new ArrayList<NetworkMessageListener>();
	private ArrayList<RemoteAgent<Double2D>> outAgent;
	private ArrayList<RemoteAgent<Double2D>> rmExternal;
	private String name;
	private String topicPrefix = "";
	private CellType cellType;
	private Bag myNode;
	private ArrayList<AgentCell> outList;
	private AuxiliaryGraph auxiliaryGraph;
	private double width, height;
	private double own_x, own_y, my_width, my_height, end_x, end_y;
	private int MODE;
	private boolean flag = false;
	private boolean outFlag = false;

	private ArrayList<Object> listaAgentCache;

	// ----------struttra in fase di inizializzazione non usata in fase di
	// simulazione----------
	private ArrayList<Node> listVertex = new ArrayList<Node>();
	// -----------------------
	private int rows, columns;
	private DContinuous2D field;
	private Boolean lock = true;
	private Boolean secondLock = false;
	
	private DistributedRegionNetwork drn;
	private Object object;

	public DNetwork(boolean flag, int i, int j, String name,
			String topicPrefix, SimState sm, AuxiliaryGraph supGraph, int MODE,
			double gridWidth, double gridHeight, int rows, int columns,
			DContinuous2D field) {
		super(flag);
		this.cellType = new CellType(i, j);
		this.sm = sm;
		this.name = name;
		this.topicPrefix = topicPrefix;
		setConnection(((DistributedState) sm).getConnection());
		this.myNode = new Bag();
		this.outList = new ArrayList<AgentCell>();
		this.auxiliaryGraph = supGraph;
		this.MODE = MODE;
		this.width = gridWidth;
		this.height = gridHeight;
		this.rows = rows;
		this.columns = columns;
		this.field = field;
		createNetworkRegin();

		listaAgentCache = new ArrayList<Object>();

		mappa = new Mappa();

		listaOut = new ArrayList<OutEntry>();
		listaOutCache = new ArrayList<OutEntry>();
		outAgent = new ArrayList<RemoteAgent<Double2D>>();
		rmExternal = new ArrayList<RemoteAgent<Double2D>>();

		/*System.out.println("rows=" + rows + " colums=" + columns + " width="
				+ width + " height=" + height + " own_x=" + own_x + " own_y="
				+ own_y + " my_width=" + my_width + " my_height" + my_height
				+ " end_x=" + end_x + " end_y=" + end_y);*/
		

	}

	// cotruisce l'oggetto che indentifica le cordinate della cella gestita
	private void createNetworkRegin() {

		// upper left corner's coordinates
		if (cellType.pos_j < (width % columns))
			own_x = (int) Math.floor(width / columns + 1) * cellType.pos_j;
		else
			own_x = (int) Math.floor(width / columns + 1) * ((width % columns))
			+ (int) Math.floor(width / columns)
			* (cellType.pos_j - ((width % columns)));

		if (cellType.pos_i < (height % rows))
			own_y = (int) Math.floor(height / rows + 1) * cellType.pos_i;
		else
			own_y = (int) Math.floor(height / rows + 1) * ((height % rows))
			+ (int) Math.floor(height / rows)
			* (cellType.pos_i - ((height % rows)));

		// own width and height
		if (cellType.pos_j < (width % columns))
			my_width = (int) Math.floor(width / columns + 1);
		else
			my_width = (int) Math.floor(width / columns);

		if (cellType.pos_i < (height % rows))
			my_height = (int) Math.floor(height / rows + 1);
		else
			my_height = (int) Math.floor(height / rows);

		// end_x and end_y
		
		end_x = (my_width * cellType.pos_j) + my_width;
		end_y = (my_height * cellType.pos_i) + my_height;
	}

	// metodo che aggiunge i nodi alla network in pase alla loro posizione
	public void addNode(Object node, Node v) {
		if (isMine(v)) {
			//System.out.println("inserisco il vertice " + v.getX() +" "+ v.getY() + " l'id: " + ((RemoteAgent) node).getId()+" id net: "+((RemoteNetwork) node).getNetworkId());
			super.addNode(node);
			myNode.add(node);
			listVertex.add(v);
		} else {
			//System.out.println("non lo inserisco " + v.getX() + " " + v.getY());
			listaAgentCache.add(node);
			RemoteNetwork x = (RemoteNetwork) node;
			x.startInContinuos = true;
		}
	}

	public void addEdge(Object from, Object to, Object info) {
		super.addEdge(from, to, info);

	}

	// metodoto che determina se un nodo è gestito da questo worker
	private boolean isMine(Node node) {
		if(MODE==1){
			if ((node.getX() > own_x && node.getX() <= end_x) && (node.getY() > own_y && node.getY() <= end_y)) {
				return true;
			}
		}
		else if (MODE!=1){
			if ((node.getX() > own_x && node.getX() <= end_x))
				return true;
		}
		return false;
	}

	// metodo che inizializza gli archi della network sia quelli interni che
	// esterni
	public void initEdge() {
		UndirectedGraph<Node, DefaultEdge> graph = auxiliaryGraph.getGraph();
		Set<Node> set = graph.vertexSet();
		Iterator<Node> iter;
		Node agent;
		iter = set.iterator();
		while (iter.hasNext()) {
			agent = iter.next();

			for (Node o : listVertex) {
				if (listVertex.contains(agent) && agent.getId() != o.getId()&& auxiliaryGraph.edgeExist(o.getId(), agent.getId())) {
					//System.out.println("appartengono alla stessa regione");
					searchInNeighborhood(agent.getId(), o.getId());
				} else if (graph.containsEdge(o, agent)) {
					searchOutNeighborhood(agent, o.getId());
					//System.out.println("esiste l'arco tra (" + o.getId()+ " ; " + agent.getId() + " ) +id: "+ agent.getId());
				}
			}
		}
		// map.stampa();

	}

	// medoto che crea l'arco interno alla network determinandone i vicini.
	private void searchInNeighborhood(int id1, int id2) {
		Object a = null, b = null;
		for (Object o : myNode) {
			if (((RemoteNetwork) o).getNetworkId() == id1) {
				a = o;
				break;
			}
		}
		for (Object o : myNode) {
			if (((RemoteNetwork) o).getNetworkId() == id2) {
				b = o;
				break;
			}
		}
		RemoteAgent x = (RemoteAgent) b;
		
		((RemoteNetwork) a).addRegione(new AgentCell(x, this.cellType));
		this.addEdge(a, b, 1);
	}

	// Individua la posizione dell'estremo dell'arco non gestito dal worker in
	// modo da poter aggiornare l'hashMap MAPPA
	private void searchOutNeighborhood(Node node, int netId) {
		int i, j;
		CellDimension celld;
		Integer id;
		CellType cell;
		RemoteNetwork nt;
		AgentCell cella;
		if (MODE == 1) {
			i = cellType.pos_i;
			j = cellType.pos_j;
			do {
				j = (j + 1) % columns;
				if (j == 0)
					i = (i + 1) % rows;
				celld = createNetworkRegion(i, j);
				if ((node.getX() > celld.getOwn_x() && node.getX() <= celld.getEnd_x()) && (node.getY() > celld.getOwn_y() && node.getY() <= celld.getEnd_y())) {
					//System.out.println("individuato la cella dove è situato l'estremo dell'arco sta nella cella: "+ i + ";" + j + " id: " + node.getId());
					cell = new CellType(i, j);
					for (Object o : myNode) {
						if ((((RemoteNetwork) o).nodeId) == netId) {
							nt = new RemoteNetwork();
							nt.setNetworkId(node.getId());
							cella = new AgentCell(nt, cell);
							((RemoteNetwork) o).addRegione(cella);
						}
					}

					id = node.getId();
					if (!mappa.findKey(cell.toString())) {
						mappa.putKey(cell.toString(), id);
					} else {
						if (mappa.findNode(cell.toString(), id)) {
							
							mappa.addCounter(cell.toString(), id);
							
						} else
							mappa.putNode(cell.toString(), id);
					}
					return;

				}
			} while (i != cellType.pos_i || j != cellType.pos_j);
		}
		else if (MODE != 1) {
			
			i = cellType.pos_i;
			j = cellType.pos_j;
			do {
				j = (j + 1) % columns;
				celld = createNetworkRegion(i, j);
				if ((node.getX() > celld.getOwn_x() && node.getX() <= celld.getEnd_x()) && (node.getY() > celld.getOwn_y() && node.getY() <= celld.getEnd_y())) {
					//System.out.println("individuato la cella dove è situato l'estremo dell'arco sta nella cella: "+ i + ";" + j + " id: " + node.getId());
					cell = new CellType(i, j);
					for (Object o : myNode) {
						if ((((RemoteNetwork) o).nodeId) == netId) {
							nt = new RemoteNetwork();
							nt.setNetworkId(node.getId());
							cella = new AgentCell(nt, cell);
							((RemoteNetwork) o).addRegione(cella);
						}
					}

					id = node.getId();
					if (!mappa.findKey(cell.toString())) {
						mappa.putKey(cell.toString(), id);
					} else {
						if (mappa.findNode(cell.toString(), id)) {
							
							mappa.addCounter(cell.toString(), id);
							
						} else
							mappa.putNode(cell.toString(), id);
					}

				}
			} while (j != cellType.pos_j);
		}
		// map.stampa();
	}

	// metodo che crea il topic della propria cella e si sottoscrive alla celle
	// da cui necessita informazioni
	public void initTopicNetwork() {
		try {

			//System.out.println("Il topic che creo è: " + topicPrefix+ cellType.pos_i + "-" + cellType.pos_j + "Net");
			connection.createTopic(topicPrefix + cellType.pos_i + "-"+ cellType.pos_j + "Net", 1);
			Set<String> setCell = mappa.getKeySet();
			Iterator<String> iterator = setCell.iterator();
			String s;
			NetworkUpdaterThreadForListener u;
			while (iterator.hasNext()) {
				s = iterator.next();
				//System.out.println("il topic a cui mi sottoscrivo è: "+ topicPrefix + s + "Net");
				connection.subscribeToTopic(topicPrefix + s + "Net");
				u = new NetworkUpdaterThreadForListener(connection, topicPrefix + s + "Net", this, listeners);
				u.start();
				mappa.addListener(s, u);
			}

		} catch (Exception e) {
			System.out.println("Errore nella crezione topic");
			e.printStackTrace();
		}
		// metodi di debug commentare per evitare stampe aggiuntive
		//checkNeigboorhood();
		//mappa.stampa();
	}

	public void unLock() {
		lock = false;
	}

	public boolean synchro() {
		RemoteNetwork netag;
		RemoteNetwork n;
		while (lock){
			Thread.yield();
		}

		//System.out.println("dim: "+field.allObjects.size()+" mynode: "+myNode.size()+" dim net: "+this.allNodes.size());

		
		this.removeAllEdges();
		

		if (myNode.size()==0 && field.allObjects.size()!=0){
			Iterator<Object> i=field.allObjects.iterator();
			while(i.hasNext()){
				object=i.next();
				n=(RemoteNetwork)object;
				if (n.notSimulated || n.startInContinuos==true)
					field.remove(object);
			}
		}

		//cancella il vecchi nodi tutti quello dello step precedente

		if (listaAgentCache.size()!=0){		

			Bag listaAgentC=field.allObjects;
			Iterator<Object> iter=listaAgentC.iterator();
			while(iter.hasNext()){
				object=iter.next();

				netag=(RemoteNetwork)object;
				for(Object object:listaAgentCache){

					
					if (/*netrma.getNetworkId()==netag.getNetworkId() &&*/ netag.isNotSimulated()/*|| netag.isStart()==true*/){
						field.remove(object);
					}
					if (netag.isStart()==true)
						field.remove(object);

				}
			}
			
		}

		try{
			// fase di publish delle informazione della propria network
			drn=new DistributedRegionNetwork(sm.schedule.getSteps()-1,cellType,myNode,outList,flag);
			listaAgentCache.clear();

			connection.publishToTopic(drn, topicPrefix+cellType.pos_i+"-"+cellType.pos_j+"Net",name);
			if (drn.isNotificaSpostamento()){
				outList.clear();
				flag=false;
			}
			
			drn.setNodeList(null);
			drn.setOutlist(null);
			drn=null;
		}
		catch(Exception e1){
			e1.printStackTrace();
			System.out.println("errore nell'invio");
		}
		PriorityQueue<Object> q;
		try{

			//fase di ricezione delle informzioni dalle celle a cui si è sottoscritti
			int numMess=mappa.size();
			q=updates.getUpdatesNet(sm.schedule.getSteps()-1,numMess);
			
			
			DistributedRegionNetwork reciver=null;
			
			while(!q.isEmpty()){
				reciver=(DistributedRegionNetwork)q.poll();
			
				//listDeleteNode=reciver.getOutList();
				if (reciver.isNotificaSpostamento()==true){
					//procedura che gestisce lo spostamento del nodo non gestito dal worker
					changeExternalNode(reciver.getOutList());
					
				}
				//gestione dei messaggi ricevuti dalla fase di publish...
				
				setOutEdge(myNode,reciver.getNodeList(),auxiliaryGraph);
				

			}
			/*if (listDeleteNode!=null)
				listDeleteNode.clear();*/
			
			addInternalNode(auxiliaryGraph);
			if (outFlag){
				// gestisce il nodo che abbandona 00
				for(RemoteAgent<Double2D> a:outAgent){
					this.changeNewOut(a);
				}
				outFlag=false;
				outAgent.clear();
			}
			if (secondLock){
				//gestisce la migrazione del nodo da una cella adicente al worker corrente 01
				for(RemoteAgent<Double2D> a:rmExternal){
					this.addNewExternNodeIn(a);
				}
				secondLock=false;
				rmExternal.clear();
			}
			
			if (reciver!=null){
				reciver.nodeList.clear();
				reciver.setNodeList(null);
				reciver.outlist.clear();
				reciver.setOutlist(null);
				reciver=null;
			}
			q.clear();
			q=null;
			
		}catch(Exception e1){
			e1.printStackTrace();
			System.out.println("errore nella ricezione");
		}
		
		this.ListaOut();
		lock=true;
		return true;
	}

	/*
	 * metodo che gestisce il cambio di regione da parte dell'estremo non
	 * gestito da lui in pratica viene notificata la migrazione del nodo in modo
	 * da poter gestire le nuove possibili sottoscrizioni
	 */
	// 10

	private synchronized  void changeExternalNode(ArrayList<AgentCell> listDeleteNode) {
		//System.out.println("inizio changeExternalNode 10  cella di riferimento: " + cellType.toString());
		//System.out.println("STEP: " + sm.schedule.getSteps());
		//System.out.println("questo metodo è stato chiamato changeExternalNode");
		RemoteAgent<Double2D> rm;
		RemoteNetwork rmn;
		CellType newCell;
		CellType oldCell;
		NetworkUpdaterThreadForListener listenerDaEliminare;
		int t;
		String c;
		String xVal;
		String yVal;
		String sup;
		NetworkMessageListener li;
		NetworkUpdaterThreadForListener u;
		int i=0;
		RemoteNetwork n;
		RemoteNetwork net;
		Iterator<NetworkMessageListener> itera;
		CellType x;
		for (AgentCell agentcell : listDeleteNode) {
			rm = (RemoteAgent<Double2D>) agentcell.getAgent();
			rmn = (RemoteNetwork) agentcell.getAgent();
			oldCell = agentcell.getCellType();
			//System.out.println("MESS questo nodo ha cambiato regione: "	+ rm.getId());
			//System.out.println("MESS questo nodo ha cambiato regione l'id è: "+ rmn.getNetworkId());

			newCell = cellSearch(rm.getPos().x, rm.getPos().y);
			//System.out.println("la nuova cella da verificare è: "+ newCell.pos_i + "-" + newCell.pos_j);

			if (newCell.pos_i == cellType.pos_i	&& newCell.pos_j == cellType.pos_j) {
				//System.out.println("entrambi gli estemi nella stessa regione!!  Arresto immediato");
				// arresto preventivo previsti problemi di cooerenza in questo
				// if lo gestisce l'altro metodo
				//this.checkNeigboorhood();
				//mappa.stampa();
				continue;
			}

			// fase di descrizione... della cella e aggiornamento del vicinato
			listenerDaEliminare = null;
			
			t = mappa.getCounterByNode(rmn.getNetworkId());
			sup=mappa.keyByNode(rmn.getNetworkId());
			if (t == -1) {
				
				c = mappa.keyByNode(rmn.getNetworkId());
				listenerDaEliminare = mappa.returnListener(c);
				mappa.removeNode(c, rmn.getNetworkId());
				if (mappa.sizeForCell(c) == 0) {
					mappa.removeKey(sup);
					itera = listeners.iterator();
					while (itera.hasNext()) {
						li = itera.next();

						if (li.getTopic().equals(listenerDaEliminare.topic))
							itera.remove();
					}
					
					xVal = c.substring(0, 1);
					yVal = c.substring(2, 3);
					x = new CellType(Integer.parseInt(xVal),Integer.parseInt(yVal));
					listenerDaEliminare.stop();
					regionUnsubscribe(x);
					x=null;
					
				}
				/*-----------------------------*/
				
				if (mappa.findKey(newCell.toString())) {
					mappa.putDummyNode(newCell.toString(), rmn.getNetworkId());
				} else {
					mappa.putDummyKey(newCell.toString(), rmn.getNetworkId());
					regionSubscribe(newCell);
					u = new NetworkUpdaterThreadForListener(connection, topicPrefix + newCell.pos_i + "-"+ newCell.pos_j + "Net", this, listeners);
					u.start();
					mappa.addListener(newCell.toString(), u);
				}

				// ----riallineamento della cache
				for (OutEntry e : listaOutCache) {
					net = (RemoteNetwork) e.getAgent();
					for (AgentCell ag : net.getListaRegioni()) {
						if (((RemoteNetwork) ag.getAgent()).getNetworkId() == rmn.getNetworkId())
							ag.setCellType(newCell);
					}

				}
			}
			// ---------------------

			for (Object o : myNode) {
				n = ((RemoteNetwork) o);
				if (auxiliaryGraph.edgeExist(n.getNetworkId(),rmn.getNetworkId())) {
					
					
					mappa.subCounter(oldCell.toString(), rmn.getNetworkId());
					
					if (mappa.counterSize(oldCell.toString(),rmn.getNetworkId()) == 0) {

						listenerDaEliminare = mappa.returnListener(oldCell.toString());

						mappa.removeNode(oldCell.toString(), rmn.getNetworkId());
					}
					if (mappa.sizeForCell(oldCell.toString()) == 0) {
						// System.out.println("AAA: devo desosttoscrivermi dalla cella "+oldCell.pos_i+"-"+oldCell.pos_j);
						mappa.removeKey(oldCell.toString());

						// eliminazione del listener di ascolto
						itera = listeners.iterator();
						while (itera.hasNext()) {
							li = itera.next();
							// System.out.println("VALORE DEI LISTENER: "+li.getTopic()+" CON QUESTO "+listenerDaEliminare.topic);
							if (li.getTopic().equals(listenerDaEliminare.topic)){
								itera.remove();
							}
						}
						
						listenerDaEliminare.stop();
						regionUnsubscribe(oldCell);
					}
				}
			}
			// gestisce la parte di nuova sottoscrizione alla nuova cella
			if (testAnyAdge(rmn)) {
				if (!mappa.findKey(newCell.toString())) {

					// ---------riallineamento della cache--------------
					for (OutEntry e : listaOut) {
						net = (RemoteNetwork) e.getAgent();
						for (AgentCell ag : net.getListaRegioni()) {
							if (((RemoteNetwork) ag.getAgent()).getNetworkId() == rmn.getNetworkId())
								// System.out.println("Vicinato: "+((RemoteNetwork)ag.getAgent()).getNetworkId()+" "+ag.cellType);
								ag.setCellType(newCell);
						}

					}
					// ------------------------------------------------

					//System.out.println("ABBAA: non presente la cella: "+ newCell.pos_i + "-" + newCell.pos_j + " mappa");
					//System.out.println("AABBA: devo sottoscrivermi");
					mappa.putKey(newCell.toString(), rmn.getNetworkId());
					i = 0;
					for (Object o : myNode) {
						n = ((RemoteNetwork) o);
						if (auxiliaryGraph.edgeExist(n.getNetworkId(),
								rmn.getNetworkId()))
							i++;
					}
					
					mappa.setCounter(newCell.toString(), rmn.getNetworkId(), i);
					
					regionSubscribe(newCell);
					u = new NetworkUpdaterThreadForListener(connection, topicPrefix + newCell.pos_i + "-"+ newCell.pos_j + "Net", this, listeners);
					u.start();
					
					mappa.addListener(newCell.toString(), u);
				} else {
					if (mappa.findNode(newCell.toString(), rmn.getNetworkId())) {
						//System.out.println("AAACCC: presente la cella: "+ newCell.pos_i + "-" + newCell.pos_j+ " mappa");
						//System.out.println("AAACCC: non devo sottoscrivermi solo aggiornare mappa e la lista degli adiacenti");
						
						
						for (Object o : myNode) {
							n = ((RemoteNetwork) o);
							if (auxiliaryGraph.edgeExist(n.getNetworkId(),rmn.getNetworkId()))
								
								mappa.addCounter(newCell.toString(),rmn.getNetworkId());
						}
						u = mappa.returnListener(newCell.toString());
						mappa.addListener(newCell.toString(), u);
					} else {
						//System.out.println("non presente il nodo da aggiungere");
						u = mappa.returnListener(newCell.toString());
						mappa.putNode(newCell.toString(), rmn.getNetworkId());
						//System.out.println("Il Listener della cella: "+ newCell.toString());
						i = 0;
						for (Object o : myNode) {
							n = ((RemoteNetwork) o);
							if (auxiliaryGraph.edgeExist(n.getNetworkId(),
									rmn.getNetworkId()))
								i++;
						}
						
						mappa.setCounter(newCell.toString(),rmn.getNetworkId(), i);
						mappa.addListener(newCell.toString(), u);
					}
				}
				// aggiornamento del vicinato
				upDateNeigboorhood(rm, newCell);
			}
		}

		checkNeigboorhood();
		mappa.stampa();
		//System.out.println("fine changeExternalNode 10");
	}

	// metodo che ricerca se esiste almeno un arco tra l'agente ricevuto come
	// messaggio e uno degli agenti gestiti da me
	private boolean testAnyAdge(RemoteNetwork rmn) {
		RemoteNetwork n;
		for (Object o : myNode) {
			n = (RemoteNetwork) o;
			if (auxiliaryGraph.edgeExist(rmn.getNetworkId(), n.getNetworkId()))
				return true;
		}
		return false;
	}

	// --metodo di prova per intercettare il nuovo vicino dopo la migrazione del
	// nodo non gestito da me--
	// metodo che intercetta la cella dell'agente che è migrato
	private CellType cellSearch(double x, double y) {
		CellDimension celld;
		int i, j;
		if (MODE == 1) {
			i = cellType.pos_i;
			j = cellType.pos_j;
			do {
				j = (j + 1) % columns;
				if (j == 0)
					i = (i + 1) % rows;
				celld = createNetworkRegion(i, j);
				/*System.out.println("pos: "+x+" "+y);
				System.out.println("i j "+i+" "+j);
				System.out.println("ox "+celld.getOwn_x()+" ex "+celld.getEnd_x()+" oy "+celld.getOwn_y()+" ey "+celld.getEnd_y());*/
				if ((x > celld.getOwn_x() && x <= celld.getEnd_x()) && (y > celld.getOwn_y() && y <= celld.getEnd_y())) {

					return new CellType(i, j);
				}
			} while (i != cellType.pos_i || j != cellType.pos_j);
		}
		else if (MODE != 1) {
			i = cellType.pos_i;
			j = cellType.pos_j;
			do {
				j = (j + 1) % columns;
				celld = createNetworkRegion(i, j);

				if ((x > celld.getOwn_x() && x <= celld.getEnd_x()) && (y > celld.getOwn_y() && y <= celld.getEnd_y())) {

					return new CellType(i, j);
				}
			} while (j != cellType.pos_j);
		}
		return null;
	}

	public void addNewExternNode(RemoteAgent<Double2D> rm) {
		secondLock = true;
		rmExternal.add(rm);
	}

	// 01metodo che gestisce il trasferimento dell'agente da un worker adiacente
	// gestendone le sottoscrizioni e il vicinato
	private void addNewExternNodeIn(RemoteAgent<Double2D> rm) {
		boolean flag = false;
		String newkey;
		ArrayList<AgentCell> listAgentCell = null;
		CellType oldCell = null;
		RemoteNetwork x;
		NetworkUpdaterThreadForListener listenerDaEliminare = null;
		RemoteNetwork net;
		String xVal;
		String yVal;
		CellType ct;
		NetworkUpdaterThreadForListener u;
		RemoteNetwork n;
		RemoteNetwork rnet;
		RemoteNetwork ro;
		RemoteNetwork nn;
		Iterator<NetworkMessageListener> itera;
		NetworkMessageListener li;
		int cou;
		Iterator<Object> ite;
		RemoteNetwork rmn = (RemoteNetwork) rm;
		String st;
		
		
		//System.out.println("INIZIO DEL METODO addNewExternNodeIn 01"+ " cella di riferimento: " + cellType.toString());
		//System.out.println("STEP: " + sm.schedule.getSteps());
		//System.out.println("Abbiamo un nuovo Nodo da gestire: "+rmn.getNetworkId());
		//System.out.println("devo aggiungerlo alla mia lista di nodi mynode per intenderci");
		
		
		deleteNode(rmn);
		
		
		myNode.add(rm);
		this.addNode(rm);
		
		
		CellType nuova = cellSearch(rm.getPos().x, rm.getPos().y);
		
		
		//System.out.println("la posizione è: " + rm.getPos() + " id Dmason: "+ rm.getId() + " id network: " + rmn.getNetworkId());
		ArrayList<AgentCell> agentCell = rmn.getListaRegioni();
		
		
		for (AgentCell ac : agentCell) {
			for (Object o : myNode) {
				net = (RemoteNetwork) o;
				if (net.getNetworkId() == ((RemoteNetwork) ac.getAgent()).getNetworkId()){
					//System.out.println("id mynode: "+net.getNetworkId()+" id agentCell: "+((RemoteNetwork) ac.getAgent()).getNetworkId());
					flag = true;
				}
			}
			if (flag || ac.getCellType().pos_i == cellType.pos_i && ac.getCellType().pos_j == cellType.pos_j) {
				flag = false;
				//System.out.println("ENTRAMBI NELLA STESSA CELLA");
				for (Object o : myNode) {
					ro = (RemoteNetwork) o;
					if (auxiliaryGraph.edgeExist(ro.getNetworkId(),rmn.getNetworkId())&& ro.getNetworkId() != rmn.getNetworkId()) {
						//System.out.println("ESISTE NODO TRA: "+ ro.getNetworkId() + " " + rmn.getNetworkId()+" mi prendo il vicinato di: "+ro.getNetworkId());
						listAgentCell = ro.getListaRegioni();
						for (AgentCell ag : listAgentCell) {
							n = (RemoteNetwork) ag.getAgent();
							//System.out.println("contronto: id: "+n.getNetworkId()+" "+rmn.getNetworkId());
							if (n.getNetworkId() == rmn.getNetworkId()) {
								// System.out.println("TROVATO");
								oldCell = ag.getCellType();
							}
						}
					}
				}
				
			
				
				for (AgentCell ag : agentCell) {
					n = (RemoteNetwork) ag.getAgent();
					// System.out.println("ID del vicino: "+n.getNetworkId()+" cella di riferimento: "+ag.cellType.toString());
					for (Object o : myNode) {
						nn = (RemoteNetwork) o;
						if (n.getNetworkId() == nn.getNetworkId()) {
							ag.cellType = cellType;
						}
					}
				}
				
			

				// System.out.println("LA CELLA: "+oldCell.toString());
				//System.out.println("netid: "+rmn.getNetworkId()+" "+rm.getPos().toString());
				
				
				if (oldCell!=null){
				mappa.subCounter(oldCell.toString(), rmn.getNetworkId());
				
				if (mappa.getCounter(oldCell.toString(), rmn.getNetworkId()) == 0) {

					listenerDaEliminare = mappa.returnListener(oldCell.toString());

					mappa.removeNode(oldCell.toString(), rmn.getNetworkId());
				}
				if (mappa.sizeForCell(oldCell.toString()) == 0) {
					mappa.removeKey(oldCell.toString());
					//System.out.println("AAA: devo canellare la sottoscirzione a "+ oldCell.pos_i + "-" + oldCell.pos_j);
					
					
					itera = listeners.iterator();
					while (itera.hasNext()) {
						li = itera.next();
						// System.out.println("VALORE DEI LISTENER: "+li.getTopic()+" CON QUESTO "+listenerDaEliminare.topic);
						if (li.getTopic().equals(listenerDaEliminare.topic))
							itera.remove();
					}
					
					if (!oldCell.toString().equals(cellType.toString())){
						listenerDaEliminare.stop();
						regionUnsubscribe(oldCell);
					}
				}
			/*-------	*/}
				else{
					for (Object o : allNodes) {
						net = (RemoteNetwork) o;
						if (net.getNetworkId() == rmn.getNetworkId()) {
							this.allNodes.remove(o);
							this.indexOutInHash.remove(o);
						}
					}
				}
			} else {
				x = ((RemoteNetwork) ac.getAgent());
				//System.out.println("NON è NELLO STESSO ARCO "+ x.getNetworkId());

				
				for (AgentCell ag : agentCell) {
					n = (RemoteNetwork) ag.getAgent();
					// System.out.println("ID del vicino: "+n.getNetworkId()+" cella di riferimento: "+ag.cellType.toString());
					for (Object o : myNode) {
						nn = (RemoteNetwork) o;
						if (n.getNetworkId() == nn.getNetworkId()) {
							ag.cellType = cellType;
						}
					}
				}
				
				
				newkey = mappa.keyByNode(rmn.getNetworkId());
				if (newkey != null) {
					int i = mappa.getCounter(newkey, rmn.getNetworkId());
					if (i == -1) {
						mappa.removeNode(newkey, rmn.getNetworkId());
						if (mappa.sizeForCell(newkey) == 0) {
							listenerDaEliminare = mappa.returnListener(newkey);
							itera = listeners.iterator();
							while (itera.hasNext()) {
								li = itera.next();
								
								if (li.getTopic().equals(listenerDaEliminare.topic))
									itera.remove();
							}
							xVal = newkey.substring(0, 1);
							yVal = newkey.substring(2, 3);
							ct = new CellType(Integer.parseInt(xVal),Integer.parseInt(yVal));

							
							mappa.removeKey(newkey);
							// --------------------------
							listenerDaEliminare.stop();
							regionUnsubscribe(ct);
						}
					}
				}
				cou = mappa.getCounterByNode(x.getNetworkId());
				if (cou == -1) {
					
					newkey = mappa.keyByNode(x.getNetworkId());
					mappa.setCounter(newkey, x.getNetworkId(), 1);
					xVal = newkey.substring(0, 1);
					yVal = newkey.substring(2, 3);
					ct = new CellType(Integer.parseInt(xVal),
					Integer.parseInt(yVal));
					ac.setCellType(ct);
				}

				// ------------------------------------------------------

				else if (!mappa.findKey(ac.getCellType().toString())) {
					//System.out.println("voce non esistente creare la cella per sottoscrizione "+ ac.getCellType());
					//System.out.println("CELLA DI RIFERIMENTO: "+ac.getCellType().toString()+" "+cellType.toString());
					
					if (ac.getCellType().pos_i!=cellType.pos_i || ac.getCellType().pos_j!=cellType.pos_j) {
						mappa.putKey(ac.getCellType().toString(),x.getNetworkId());
						regionSubscribe(ac.getCellType());
						u = new NetworkUpdaterThreadForListener(connection, topicPrefix + ac.cellType.pos_i+ "-" + ac.cellType.pos_j + "Net",this, listeners);
						u.start();
						mappa.addListener(ac.getCellType().toString(), u);
					}
				} else {
					if (mappa.findNode(ac.getCellType().toString(),	x.getNetworkId())) {
						int numCounter = mappa.getCounter(ac.getCellType().toString(), x.getNetworkId());
						if (numCounter <= -1) {
							numCounter = 0;
							mappa.setCounter(ac.getCellType().toString(),x.getNetworkId(), numCounter);
						}
						mappa.addCounter(ac.getCellType().toString(),x.getNetworkId());
					} else {
						u = mappa.returnListener(ac.getCellType().toString());
						mappa.putNode(ac.getCellType().toString(),x.getNetworkId());
						mappa.addListener(ac.getCellType().toString(), u);
					}
				}
			}
		}
		for (Object o : myNode) {
			rnet = (RemoteNetwork) o;
			if (auxiliaryGraph.edgeExist(rnet.getNetworkId(),rmn.getNetworkId())) {
				this.addEdge(o, rm, 1);
				ite = listaAgentCache.iterator();
				while (ite.hasNext()) {
					object = ite.next();
					n = (RemoteNetwork) object;
					if (n.getNetworkId() == rnet.getNetworkId())
						ite.remove();
				}
			}
		}

		upDateNeigboorhood(rm, nuova);
		checkNeigboorhood();
		mappa.stampa();
		//System.out.println("FINE DEL METODO addNewExternNodeIn 01");

	}

	/* metodoo che elimina il nodo fittizio per evitare che resti in
	/ network e in continous */
	private void deleteNode(RemoteNetwork rmn) {
		RemoteNetwork net;
		for (Object o : allNodes) {
			net = (RemoteNetwork) o;
			if (net.getNetworkId() == rmn.getNetworkId()) {
				this.allNodes.remove(o);
				this.indexOutInHash.remove(o);
			}
		}
		for (Object o : field.allObjects) {
			net = (RemoteNetwork) o;
			if (net.getNetworkId() == rmn.getNetworkId()&& net.isNotSimulated()) {
				
				field.remove(o);
			}
			
			
		}
	}
	public void changeOut(RemoteAgent<Double2D> outA) {
		outAgent.add(outA);
		outFlag = true;
	}

	// metodo che gestisce la migrazione dell'agente in uscita dal worker
	// (00)
	private void changeNewOut(RemoteAgent<Double2D> outAgent) {
		//System.out.println("inizio changeNewOut 00" + " cella di riferimento: "+ cellType.toString());
		RemoteNetwork nt = (RemoteNetwork) outAgent;
		//System.out.println("STEP: " + sm.schedule.getSteps());
		//System.out.println("Questo agente ci sta lasciando: "+ outAgent.getId() + " idnet: " + nt.getNetworkId() + " dim: "+ myNode.size());
		//System.out.println("pos: " + outAgent.getPos());

		
		AgentCell agent;
		for (Object o : myNode) {
			if (((RemoteAgent) o).getId().equals(outAgent.getId())) {

				myNode.remove(o);
				
								
				this.allNodes.remove(o);
				this.indexOutInHash.remove(o);
				

				ereseNode(outAgent);

				agent = new AgentCell(o, cellType);
				outList.add(agent);
				flag = true;
				checkMappa(o);

			}
		}

		//System.out.println("fine changeNewOut 00");
		//System.out.println("dim: " + this.allNodes.size());
	}

	// metodo che eliminia il nodo fittizio valutarne l'utilizzo
	private void ereseNode(RemoteAgent rm) {

		RemoteNetwork sup;
		RemoteNetwork x;
		Bag bag;
		Iterator<Object> iterator;
		RemoteNetwork rnet = (RemoteNetwork) rm;
		ArrayList<AgentCell> listAgent = rnet.listaRegioni;
		for (AgentCell agc : listAgent) {
			sup = (RemoteNetwork) agc.getAgent();
			if (agc.getCellType().pos_i != cellType.pos_i && agc.cellType.pos_j != cellType.pos_j) {
				bag = this.allNodes;
				iterator = bag.iterator();
				while (iterator.hasNext()) {
					object = iterator.next();
					x = (RemoteNetwork) object;
					if (sup.getNetworkId() == x.getNetworkId()) {
						iterator.remove();
						this.indexOutInHash.remove(object);
					}
				}
			}

		}

	}

	/*
	 * metodo usato da changeNewOut
	 * gli agenti (00)
	 */
	private void checkMappa(Object o) {
		RemoteNetwork net = (RemoteNetwork) o;
		RemoteAgent<Double2D> x = (RemoteAgent<Double2D>) o;
		Integer nodeId;
		String cellId=null;
		String xVal=null;
		String yVal=null;
		CellType ct=null;
		NetworkMessageListener li=null;
		NetworkUpdaterThreadForListener u=null;
		CellType newCella=null;
		NetworkUpdaterThreadForListener listenerDaEliminare = null;
		ArrayList<AgentCell> listaVicinato=null;
		Iterator<Integer> itaratore;
		Set<Integer> setInt;
		Iterator<NetworkMessageListener> itera;
		// fase di desottoscrizione ed eliminazione dalla mappa
		
		Set<String> setCell = mappa.getKeySet();
		Iterator<String> indice = setCell.iterator();

		
		while (indice.hasNext()) {
			cellId = indice.next();
			setInt = mappa.getNodeSet(cellId);
			itaratore = setInt.iterator();
			
			while (itaratore.hasNext()) {
				nodeId = itaratore.next();
				if (auxiliaryGraph.edgeExist(net.getNetworkId(), nodeId)) {
					//System.out.println("AAA: il mio estremo dell'arco si trovava in: "+ cellId + " " + nodeId);
					//System.out.println("AAA: devo cancellare il valore dall'arraylist della mappa");
					mappa.subCounter(cellId, nodeId);
					if (mappa.getCounter(cellId, nodeId) == 0) {
						//System.out.println("AAA: sto rimuovendo dalla lista: "+ nodeId);
						itaratore.remove();

						listenerDaEliminare = mappa.returnListener(cellId);

						mappa.removeNode(cellId, nodeId);
					}
				}
			}
			if (mappa.sizeForCell(cellId) == 0) {
				//System.out.println("zero devo desottoscrivermi qui va chiamato il metodo e cancellata la chiave");
				indice.remove();
				mappa.removeKey(cellId);
				xVal = cellId.substring(0, 1);
				yVal = cellId.substring(2, 3);
				ct = new CellType(Integer.parseInt(xVal),Integer.parseInt(yVal));

				
				itera = listeners.iterator();
				
				while (itera.hasNext()) {
					li = itera.next();
					
					if (li.getTopic().equals(listenerDaEliminare.topic))
						itera.remove();
				}
				listenerDaEliminare.stop();
				regionUnsubscribe(ct);
			}
		}

		// fase di nuova sottoscrizone
		newCella = cellSearch(x.getPos().x, x.getPos().y);
		listaVicinato = net.getListaRegioni();
		// System.out.println("BBB: il nodo si è trasferito nella cella: "+newCella.pos_i+"-"+newCella.pos_j+" id "+((RemoteNetwork)x).nodeId);
		for (AgentCell ac : listaVicinato) {
			if (ac.getCellType().pos_i == cellType.pos_i && ac.getCellType().pos_j == cellType.pos_j) {
				//System.out.println("BBB: Ho un arco nella regione che sto per lasciare devo");
				if (mappa.findKey(newCella.toString())) {
					//System.out.println("BBB contengo la cella "+ newCella.pos_i + "-" + newCella.pos_j);
					//System.out.println("BBB Caso 1");
					if (mappa.findNode(newCella.toString(), net.getNetworkId())) {
						
						mappa.addCounter(newCella.toString(),net.getNetworkId());
					} else {
						u = mappa.returnListener(newCella.toString());
						mappa.putNode(newCella.toString(), net.getNetworkId());
						mappa.addListener(newCella.toString(), u);
					}
					upDateNeigboorhood(o, newCella);
				} else {
					//System.out.println("BBB: non contengo la cella "+ newCella.pos_i + "-" + newCella.pos_j+ "devo sottoscrivermi");
					//System.out.println("BBB: Caso 2");
					mappa.putKey(newCella.toString(), net.getNetworkId());
					regionSubscribe(newCella);
					u = new NetworkUpdaterThreadForListener(connection, topicPrefix + newCella.pos_i + "-"+ newCella.pos_j + "Net", this, listeners);
					u.start();
					mappa.addListener(newCella.toString(), u);
					upDateNeigboorhood(o, newCella);

				}
			}
		}
		checkNeigboorhood();
		mappa.stampa();
	}

	/*
	 * aggiorna la posizione del neighborhood dei nodi in mynode in pratica
	 * aggiorna tutti gli agenti che hanno nel loro ficinato L'agente Object o
	 * con la sua nuova cella di appartenenza newCell
	 */
	private void upDateNeigboorhood(Object o, CellType newCell) {
		RemoteNetwork on = (RemoteNetwork) o;
		RemoteNetwork an=null;
		ArrayList<AgentCell> listAgentCell=null;
		RemoteNetwork agcn=null;
		
		for (Object a : myNode) {
			an = (RemoteNetwork) a;
			if (auxiliaryGraph.edgeExist(an.getNetworkId(), on.getNetworkId())) {
				listAgentCell= an.getListaRegioni();
				for (AgentCell agentcell : listAgentCell) {
					agcn = (RemoteNetwork) agentcell.getAgent();
					if (agcn.getNetworkId() == on.getNetworkId()) {
						// System.out.println("sostituito il nuovo vicino");
						agentcell.setCellType(newCell);
					}
				}
			}
		}
	}

	// metodo usato per il debug della struttura stampandone il contenuto
	private void checkNeigboorhood() {
		RemoteNetwork an=null;
		ArrayList<AgentCell> listAgentCell=null;
		RemoteNetwork agcn=null;
		System.out.println("*******************************************************************");
		System.out.println("Dimensione myNode: " + myNode.size()+ " La Cella di riferiemnto: " + cellType.toString());
		System.out.println("*******************************************************************");
		for (Object a : myNode) {
			an = (RemoteNetwork) a;
			System.out.println("nodo in esame :" + an.getNetworkId());
			listAgentCell = an.getListaRegioni();
			for (AgentCell agentcell : listAgentCell) {
				agcn = (RemoteNetwork) agentcell.getAgent();
				System.out.println("id del mio vicino d'arco"+ agcn.getNetworkId() + " "+ agentcell.getCellType().pos_i + " "+ agentcell.getCellType().pos_j);
			}
		}
		System.out.println("*******************************************************************");
	}

	// metodo di Subscribe
	private void regionSubscribe(CellType c) {
		try {
			//System.out.println("*****il TOPIC A CUI MI SOTTOSCRIVO è: "+ topicPrefix + c.pos_i + "-" + c.pos_j + "Net******");
			connection.subscribeToTopic(topicPrefix + c.pos_i + "-" + c.pos_j+ "Net");
		} catch (Exception e) {
			System.out.println("problema nella regionsubcribe -.-' ");
			e.printStackTrace();
		}

	}

	// metodo di unSubscribe
	private void regionUnsubscribe(CellType c) {
		try {
			connection.unsubscribe(topicPrefix + c.pos_i + "-" + c.pos_j+ "Net");
			//System.out.println("*******mi DISCONNETTO DA: " + c.pos_i + "-"+ c.pos_j + "Net*********");
		} catch (Exception e) {
			System.out.println("problema nella regionUnsubscribe -.-' ");
			e.printStackTrace();
		}
	}

	/*
	 * cerca l'estremo dell'arco per stabilire il link con il nodo appena
	 * ricevuto e ne aggiorna la posizione spostamento interno al worker
	 */
	private void setOutEdge(Bag myNode, Bag listOfNode, AuxiliaryGraph supGraph) {
		RemoteNetwork net=null;
		for (Object n1 : myNode) {
			for (Object n2 : listOfNode) {
				if (supGraph.edgeExist(((RemoteNetwork) n1).getNetworkId(),
						((RemoteNetwork) n2).getNetworkId())) {
					upDateNodePosition(n1, n2);
				}
			}
		}
		for (Object n1 : myNode) {
			for (Object n2 : listOfNode) {
				if (supGraph.edgeExist(((RemoteNetwork) n1).getNetworkId(),
						((RemoteNetwork) n2).getNetworkId())) {

					net = (RemoteNetwork) n2;
					net.setNotSimulated(true);
					listaAgentCache.add(n2);

					
					field.setObjectLocation(n2,((RemoteAgent<Double2D>) n2).getPos());
					super.addNode(n2);
					
					addEdge(n1, n2, 1);
				}
			}
		}
	}

	private void addInternalNode(AuxiliaryGraph supGraph) {
		boolean flag = false;
		Object[] v1 = myNode.objs;
		Object[] v2 = myNode.objs;
		for (int i = 0; i < myNode.size(); i++) {
			for (int j = i + 1; j < myNode.size(); j++) {
				if (supGraph.edgeExist(((RemoteNetwork) v1[i]).getNetworkId(),((RemoteNetwork) v2[j]).getNetworkId())) {
					
					addEdge(v1[i], v2[j], 1);
				}
			}
		}

	}

	// metodo che aggiorna la posizione del nodo ricevuto cancellando la vecchia
	// posizione e inserendo la nuova
	// metodo prima usava forech ora usa iteratore sembra migliorato
	private void upDateNodePosition(Object n1, Object n2) {
		Bag lista = this.getAllNodes();
		for (Object o : lista) {
			if (((RemoteAgent) o).getId().equals(((RemoteAgent) n2).getId())) {
				this.allNodes.remove(o);
				this.indexOutInHash.remove(o);

			}
		}

	}

	private CellDimension createNetworkRegion(int i, int j) {

		double own_x, own_y, my_width, my_height, end_x, end_y;

		if (j < (width % columns))
			own_x = (int) Math.floor(width / columns + 1) * j;
		else
			own_x = (int) Math.floor(width / columns + 1) * ((width % columns))
			+ (int) Math.floor(width / columns)
			* (j - ((width % columns)));

		if (i < (height % rows))
			own_y = (int) Math.floor(height / rows + 1) * i;
		else
			own_y = (int) Math.floor(height / rows + 1) * ((height % rows))
			+ (int) Math.floor(height / rows) * (i - ((height % rows)));

		// own width and height
		if (j < (width % columns))
			my_width = (int) Math.floor(width / columns + 1);
		else
			my_width = (int) Math.floor(width / columns);

		if (i < (height % rows))
			my_height = (int) Math.floor(height / rows + 1);
		else
			my_height = (int) Math.floor(height / rows);

		// end_x and end_y
		
		if (my_width%2!=0){
			end_x = (my_width * j) + my_width;
			end_x++;
		}
		else{
			end_x = (my_width * j) + my_width;
		}
		
		if (my_height%2!=0){
			end_y = (my_height * i) + my_height;
			end_y++;
		}
		else{
			end_y = (my_height * i) + my_height;
		}
		
		return new CellDimension(own_x, own_y, my_width, my_height, end_x,
				end_y);
	}

	@Override
	public boolean setDistributedObjectLocationForPeer(Object location,
			RemoteAgent rm, SimState sm) {
		return false;
	}

	@Override
	public boolean setDistributedObjectLocation(Object location,RemoteAgent rm, SimState sm) {
		Double2D loc = (Double2D) location;
		int x = (int) loc.getX();
		int y = (int) loc.getY();
		if (isMine(new Node(x, y, 0))) {
			for (Object o : myNode) {
				if (((RemoteAgent) o).getId().equals(rm.getId())) {
					((RemoteAgent) o).setPos(loc);
					return true;
				}
			}
		}
		return false;
	}

	// metodo su cui si deve lavorare
	public void changeListaOut(RemoteAgent<Double2D> rm) {
		OutEntry e = new OutEntry(((RemoteNetwork) rm).getNetworkId(), rm);
		listaOut.add(e);
	}

	//metodo che aggiorna la mappa in funzione dei valori nelle cache e degli agenti nelle regioni out
	public void ListaOut() {
		NetworkUpdaterThreadForListener listenerDaEliminare = null;
		ArrayList<OutEntry> a = (ArrayList<OutEntry>) listaOutCache.clone();
		ArrayList<OutEntry> b = (ArrayList<OutEntry>) listaOut.clone();
		b.removeAll(a);
		RemoteAgent<Double2D> rm=null;
		RemoteNetwork rn=null;
		ArrayList<AgentCell> lista=null;
		int counter=0;
		NetworkUpdaterThreadForListener u=null;
		NetworkMessageListener li=null;
		Iterator<NetworkMessageListener> itera;
		if (b.size() != 0) {
			//System.out.println("id a cui mi devo sottoscrivere ");
			for (OutEntry o : b) {
				//System.out.println("ID: " + o.getIdNetwork());
				rm = o.getAgent();
				rn = (RemoteNetwork) rm;
				//System.out.println("Lista dei vicini:");
				lista = rn.listaRegioni;
				for (AgentCell ag : lista) {
					//System.out.println("Id: "+ ((RemoteNetwork) ag.agent).nodeId);
					//System.out.println("Cell: " + ag.cellType.toString());

					if (!ag.cellType.toString().equals(cellType.toString())) {
						if (!mappa.findKey(ag.cellType.toString())) {
							mappa.putDummyKey(ag.cellType.toString(),((RemoteNetwork) ag.agent).nodeId);
							regionSubscribe(ag.getCellType());
							u = new NetworkUpdaterThreadForListener(connection, topicPrefix + ag.cellType.pos_i+ "-" + ag.cellType.pos_j + "Net",this, listeners);
							u.start();
							mappa.addListener(ag.getCellType().toString(), u);
						} else {
							if (!mappa.findNode(ag.cellType.toString(),	((RemoteNetwork) ag.agent).nodeId)) {
								mappa.putDummyNode(ag.cellType.toString(),((RemoteNetwork) ag.agent).nodeId);
							}
						}

					}
				}
			}
			//System.out.println("inizion cache 1");
			//mappa.stampa();
			//System.out.println("fine cache 1");
		}
		a = (ArrayList<OutEntry>) listaOutCache.clone();
		b = (ArrayList<OutEntry>) listaOut.clone();
		a.removeAll(b);
		if (a.size() != 0) {
			//System.out.println("id a cui mi devo desottoscrivere ");
			for (OutEntry o : a) {
				//System.out.println("ID: " + o.getIdNetwork());
				rm = o.getAgent();
				rn = (RemoteNetwork) rm;
				//System.out.println("Lista dei vicini:");
				lista = rn.listaRegioni;
				for (AgentCell ag : lista) {
					//System.out.println("Id: "+ ((RemoteNetwork) ag.agent).nodeId);
					//System.out.println("Cell: " + ag.cellType.toString());

					if (!ag.cellType.toString().equals(cellType.toString())) {
						if (mappa.findNode(ag.cellType.toString(),
								((RemoteNetwork) ag.agent).nodeId)) {
							counter = mappa.getCounter(ag.cellType.toString(),((RemoteNetwork) ag.agent).nodeId);
							if (counter == -1) {
								// System.out.println("elimino");
								mappa.removeNode(ag.cellType.toString(),((RemoteNetwork) ag.agent).nodeId);
								listenerDaEliminare = mappa.returnListener(ag.cellType.toString());
							}
						}
						if (mappa.findKey(ag.cellType.toString())) {
							if (mappa.sizeForCell(ag.cellType.toString()) == 0) {
								mappa.removeKey(ag.getCellType().toString());
								itera = listeners.iterator();
								while (itera.hasNext()) {
									li = itera.next();
									// System.out.println("VALORE DEI LISTENER: "+li.getTopic()+" CON QUESTO "+listenerDaEliminare.topic);
									if (li.getTopic().equals(listenerDaEliminare.topic))
										itera.remove();
								}
								listenerDaEliminare.stop();
								regionUnsubscribe(ag.cellType);
							}
						}
					}
				}
			}
			//System.out.println("inizio cache 2");
			//mappa.stampa();
			//System.out.println("Fine cache 2");
		}
		listaOutCache.clear();
		listaOutCache = (ArrayList<OutEntry>) listaOut.clone();
		listaOut.clear();
		a=null;
		b=null;
	}

	@Override
	public DistributedState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setAvailableRandomLocation(RemoteAgent rm) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConnection(Connection con) {
		connection = (ConnectionNFieldsWithActiveMQAPI) con;

	}

	@Override
	public ArrayList getLocalListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getID() {
		return name;
		// return null;
	}

	@Override
	public UpdateMap getUpdates() {
		// TODO Auto-generated method stub
		return null;
	}

	public UpdateMapNet getNetUpdates() {
		// TODO Auto-generated method stub
		return updates;
	}

	@Override
	public HashMap getToSendForBalance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsSplitted(boolean isSplitted) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSplitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrepareForBalance() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUnited() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void prepareForBalance(boolean prepareForBalance) {
		// TODO Auto-generated method stub

	}

	@Override
	public HashMap getToSendForUnion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareForUnion(boolean prepareForUnion) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumAgents() {
		// TODO Auto-generated method stub
		return myNode.size();
	}

	@Override
	public void resetParameters() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getLeftMineSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRightMineSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public VisualizationUpdateMap getGlobals() {
		// TODO Auto-generated method stub
		return null;
	}

}
