package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.louvain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
@Deprecated
public class Louvain<V,E>{

    private CommunityStructure structure;
    private double modularity;
    private boolean isRandomized = false;
    private boolean useWeight = true;
    private double resolution = 1.;
	private java.util.HashMap<V, Integer> vertexCommunity;

	public Louvain(double resolution) {
		setResolution(resolution);
		setRandom(true);
	}
	
    public void setRandom(boolean isRandomized) {
        this.isRandomized = isRandomized;
    }

    public boolean getRandom() {
        return isRandomized;
    }

    public void setUseWeight(boolean useWeight) {
        this.useWeight = useWeight;
    }

    public boolean getUseWeight() {
        return useWeight;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }

    public double getResolution() {
        return resolution;
    }


    class ModEdge {

        int source;
        int target;
        float weight;

        public ModEdge(int s, int t, float w) {
            source = s;
            target = t;
            weight = w;
        }
    }

    class CommunityStructure {

        HashMap<Community, Float>[] nodeConnectionsWeight;
        HashMap<Community, Integer>[] nodeConnectionsCount;
        HashMap<V, Integer> map;
        Community[] nodeCommunities;
        Graph graph;
        double[] weights;
        double graphWeightSum;
        LinkedList<ModEdge>[] topology;
        LinkedList<Community> communities;
        int N;
        HashMap<Integer, Community> invMap;

        CommunityStructure(Graph<V,E> hgraph) {
            this.graph = hgraph;
            N = hgraph.vertexSet().size();
            invMap = new HashMap<Integer, Community>();
            nodeConnectionsWeight = new HashMap[N];
            nodeConnectionsCount = new HashMap[N];
            nodeCommunities = new Community[N];
            map = new HashMap<V, Integer>();
            topology = new LinkedList[N];
            communities = new LinkedList<Community>();
            int index = 0;
            weights = new double[N];
            for (V node : hgraph.vertexSet()) {
                map.put(node, index);
                nodeCommunities[index] = new Community(this);
                nodeConnectionsWeight[index] = new HashMap<Community, Float>();
                nodeConnectionsCount[index] = new HashMap<Community, Integer>();
                weights[index] = 0;
                nodeCommunities[index].seed(index);
                Community hidden = new Community(structure);
                hidden.nodes.add(index);
                invMap.put(index, hidden);
                communities.add(nodeCommunities[index]);
                index++;
            }

            for (V node : hgraph.vertexSet()) {
                int node_index = map.get(node);
                topology[node_index] = new LinkedList<ModEdge>();

                for (V neighbor : Graphs.neighborListOf(hgraph, node)) {
                    if (node == neighbor) {
                        continue;
                    }
                    int neighbor_index = map.get(neighbor);
                    float weight = 1;
                    if (useWeight) {
                        weight = (float) hgraph.getEdgeWeight(hgraph.getEdge(node, neighbor));
                    }

                    weights[node_index] += weight;
                    Louvain.ModEdge me = new ModEdge(node_index, neighbor_index, weight);
                    topology[node_index].add(me);
                    Community adjCom = nodeCommunities[neighbor_index];
                    nodeConnectionsWeight[node_index].put(adjCom, weight);
                    nodeConnectionsCount[node_index].put(adjCom, 1);
                    nodeCommunities[node_index].connectionsWeight.put(adjCom, weight);
                    nodeCommunities[node_index].connectionsCount.put(adjCom, 1);
                    nodeConnectionsWeight[neighbor_index].put(nodeCommunities[node_index], weight);
                    nodeConnectionsCount[neighbor_index].put(nodeCommunities[node_index], 1);
                    nodeCommunities[neighbor_index].connectionsWeight.put(nodeCommunities[node_index], weight);
                    nodeCommunities[neighbor_index].connectionsCount.put(nodeCommunities[node_index], 1);
                    graphWeightSum += weight;
                } 
            }
            graphWeightSum /= 2.0;
        }

        private void addNodeTo(int node, Community to) {
            to.add(new Integer(node));
            nodeCommunities[node] = to;

            for (ModEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Float neighEdgesTo = nodeConnectionsWeight[neighbor].get(to);
                if (neighEdgesTo == null) {
                    nodeConnectionsWeight[neighbor].put(to, e.weight);
                } else {
                    nodeConnectionsWeight[neighbor].put(to, neighEdgesTo + e.weight);
                }
                Integer neighCountEdgesTo = nodeConnectionsCount[neighbor].get(to);
                if (neighCountEdgesTo == null) {
                    nodeConnectionsCount[neighbor].put(to, 1);
                } else {
                    nodeConnectionsCount[neighbor].put(to, neighCountEdgesTo + 1);
                }

                ///////////////////
                Community adjCom = nodeCommunities[neighbor];
                Float wEdgesto = (Float) adjCom.connectionsWeight.get(to);
                if (wEdgesto == null) {
                    adjCom.connectionsWeight.put(to, e.weight);
                } else {
                    adjCom.connectionsWeight.put(to, wEdgesto + e.weight);
                }

                Integer cEdgesto = (Integer) adjCom.connectionsCount.get(to);
                if (cEdgesto == null) {
                    adjCom.connectionsCount.put(to, 1);
                } else {
                    adjCom.connectionsCount.put(to, cEdgesto + 1);
                }

                Float nodeEdgesTo = nodeConnectionsWeight[node].get(adjCom);
                if (nodeEdgesTo == null) {
                    nodeConnectionsWeight[node].put(adjCom, e.weight);
                } else {
                    nodeConnectionsWeight[node].put(adjCom, nodeEdgesTo + e.weight);
                }

                Integer nodeCountEdgesTo = nodeConnectionsCount[node].get(adjCom);
                if (nodeCountEdgesTo == null) {
                    nodeConnectionsCount[node].put(adjCom, 1);
                } else {
                    nodeConnectionsCount[node].put(adjCom, nodeCountEdgesTo + 1);
                }

                if (to != adjCom) {
                    Float comEdgesto = (Float) to.connectionsWeight.get(adjCom);
                    if (comEdgesto == null) {
                        to.connectionsWeight.put(adjCom, e.weight);
                    } else {
                        to.connectionsWeight.put(adjCom, comEdgesto + e.weight);
                    }

                    Integer comCountEdgesto = (Integer) to.connectionsCount.get(adjCom);
                    if (comCountEdgesto == null) {
                        to.connectionsCount.put(adjCom, 1);
                    } else {
                        to.connectionsCount.put(adjCom, comCountEdgesto + 1);
                    }

                }
            }
        }

        private void removeNodeFrom(int node, Community from) {

            Community community = nodeCommunities[node];
            for (ModEdge e : topology[node]) {
                int neighbor = e.target;

                ////////
                //Remove Node Connection to this community
                Float edgesTo = nodeConnectionsWeight[neighbor].get(community);
                Integer countEdgesTo = nodeConnectionsCount[neighbor].get(community);
                if (countEdgesTo - 1 == 0) {
                    nodeConnectionsWeight[neighbor].remove(community);
                    nodeConnectionsCount[neighbor].remove(community);
                } else {
                    nodeConnectionsWeight[neighbor].put(community, edgesTo - e.weight);
                    nodeConnectionsCount[neighbor].put(community, countEdgesTo - 1);
                }

                ///////////////////
                //Remove Adjacency Community's connection to this community
                Louvain.Community adjCom = nodeCommunities[neighbor];
                Float oEdgesto = (Float) adjCom.connectionsWeight.get(community);
                Integer oCountEdgesto = (Integer) adjCom.connectionsCount.get(community);
                if (oCountEdgesto - 1 == 0) {
                    adjCom.connectionsWeight.remove(community);
                    adjCom.connectionsCount.remove(community);
                } else {
                    adjCom.connectionsWeight.put(community, oEdgesto - e.weight);
                    adjCom.connectionsCount.put(community, oCountEdgesto - 1);
                }

                if (node == neighbor) {
                    continue;
                }

                if (adjCom != community) {
                    Float comEdgesto = (Float) community.connectionsWeight.get(adjCom);
                    Integer comCountEdgesto = (Integer) community.connectionsCount.get(adjCom);
                    if (comCountEdgesto - 1 == 0) {
                        community.connectionsWeight.remove(adjCom);
                        community.connectionsCount.remove(adjCom);
                    } else {
                        community.connectionsWeight.put(adjCom, comEdgesto - e.weight);
                        community.connectionsCount.put(adjCom, comCountEdgesto - 1);
                    }
                }

                Float nodeEgesTo = nodeConnectionsWeight[node].get(adjCom);
                Integer nodeCountEgesTo = nodeConnectionsCount[node].get(adjCom);
                if (nodeCountEgesTo - 1 == 0) {
                    nodeConnectionsWeight[node].remove(adjCom);
                    nodeConnectionsCount[node].remove(adjCom);
                } else {
                    nodeConnectionsWeight[node].put(adjCom, nodeEgesTo - e.weight);
                    nodeConnectionsCount[node].put(adjCom, nodeCountEgesTo - 1);
                }

            }
            from.remove(new Integer(node));
        }

        private void moveNodeTo(int node, Community to) {
            Community from = nodeCommunities[node];
            removeNodeFrom(node, from);
            addNodeTo(node, to);
        }

        private void zoomOut() {
            int M = communities.size();
            LinkedList<ModEdge>[] newTopology = new LinkedList[M];
            int index = 0;
            nodeCommunities = new Community[M];
            nodeConnectionsWeight = new HashMap[M];
            nodeConnectionsCount = new HashMap[M];
            HashMap<Integer, Community> newInvMap = new HashMap<Integer, Community>();
            for (int i = 0; i < communities.size(); i++) {//Community com : mCommunities) {
                Community com = communities.get(i);
                nodeConnectionsWeight[index] = new HashMap<Community, Float>();
                nodeConnectionsCount[index] = new HashMap<Community, Integer>();
                newTopology[index] = new LinkedList<ModEdge>();
                nodeCommunities[index] = new Community(com);
                Set<Community> iter = com.connectionsWeight.keySet();
                double weightSum = 0;

                Community hidden = new Community(structure);
                int len = com.nodes.size();
                for (int k =0 ; k <  len; k++) {
                	Integer nodeInt = (Integer) com.nodes.get(k);
                    Community oldHidden = invMap.get(nodeInt);
                    hidden.nodes.addAll(oldHidden.nodes);
                }
                newInvMap.put(index, hidden);
                for (Louvain.Community adjCom : iter) {
                    int target = communities.indexOf(adjCom);
                    float weight = (Float) com.connectionsWeight.get(adjCom);
                    if (target == index) {
                        weightSum += 2. * weight;
                    } else {
                        weightSum += weight;
                    }
                    ModEdge e = new ModEdge(index, target, weight);
                    newTopology[index].add(e);
                }
                weights[index] = weightSum;
                nodeCommunities[index].seed(index);

                index++;
            }
            communities.clear();

            for (int i = 0; i < M; i++) {
                Community com = nodeCommunities[i];
                communities.add(com);
                for (ModEdge e : newTopology[i]) {
                    nodeConnectionsWeight[i].put(nodeCommunities[e.target], e.weight);
                    nodeConnectionsCount[i].put(nodeCommunities[e.target], 1);
                    com.connectionsWeight.put(nodeCommunities[e.target], e.weight);
                    com.connectionsCount.put(nodeCommunities[e.target], 1);
                }

            }

            N = M;
            topology = newTopology;
            invMap = newInvMap;
        }
    }

    class Community<V,E> {

        double weightSum;
        CommunityStructure structure;
        LinkedList<Integer> nodes;
        HashMap<Community, Float> connectionsWeight;
        HashMap<Community, Integer> connectionsCount;

        public int size() {
            return nodes.size();
        }

        public Community(Community com) {
            structure = com.structure;
            connectionsWeight = new HashMap<Community, Float>();
            connectionsCount = new HashMap<Community, Integer>();
            nodes = new LinkedList<Integer>();
            //mHidden = pCom.mHidden;
        }

        public Community(CommunityStructure structure) {
            this.structure = structure;
            connectionsWeight = new HashMap<Community, Float>();
            connectionsCount = new HashMap<Community, Integer>();
            nodes = new LinkedList<Integer>();
        }

        public void seed(int node) {
            nodes.add(node);
            weightSum += structure.weights[node];
        }

        public boolean add(int node) {
            nodes.addLast(new Integer(node));
            weightSum += structure.weights[node];
            return true;
        }

        public boolean remove(int node) {
            boolean result = nodes.remove(new Integer(node));
            weightSum -= structure.weights[node];
            if (nodes.size() == 0) {
                structure.communities.remove(this);
            }
            return result;
        }
    }

    public HashMap<V, Integer> execute(Graph<V,E> hgraph) {

        structure = new Louvain.CommunityStructure(hgraph);
        int[] comStructure = new int[hgraph.vertexSet().size()];

        computeModularity(hgraph, structure, comStructure, resolution, isRandomized, useWeight);

        vertexCommunity=getClusters(comStructure,hgraph,structure);
        return vertexCommunity;
    }
    
    private HashMap<V, Integer> getClusters(int[] struct, Graph<V,E> hgraph, CommunityStructure theStructure) {
       
    	HashMap<V,Integer> results = new HashMap<V, Integer>();
    	
        for (V n : hgraph.vertexSet()) {
            int n_index = theStructure.map.get(n);;
            results.put(n, struct[n_index]);
        }
        
        return results;
    }


    protected HashMap<String, Double> computeModularity(Graph<V,E> hgraph, CommunityStructure theStructure, int[] comStructure,
            double currentResolution, boolean randomized, boolean weighted) {
        Random rand = new Random();

        double totalWeight = theStructure.graphWeightSum;
        double[] nodeDegrees = theStructure.weights.clone();

        HashMap<String, Double> results = new HashMap<String, Double>();

        boolean someChange = true;
        while (someChange) {
            someChange = false;
            boolean localChange = true;
            while (localChange) {
                localChange = false;
                int start = 0;
                if (randomized) {
                    start = Math.abs(rand.nextInt()) % theStructure.N;
                }
                int step = 0;
                for (int i = start; step < theStructure.N; i = (i + 1) % theStructure.N) {
                    step++;
                    Community bestCommunity = updateBestCommunity(theStructure, i, currentResolution);
                    if ((theStructure.nodeCommunities[i] != bestCommunity) && (bestCommunity != null)) {
                        theStructure.moveNodeTo(i, bestCommunity);
                        localChange = true;
                    }
                }
                someChange = localChange || someChange;
            }
            if (someChange) {
                theStructure.zoomOut();
            }
        }

        fillComStructure(hgraph, theStructure, comStructure);
        double[] degreeCount = fillDegreeCount(hgraph, theStructure, comStructure, nodeDegrees, weighted);

        double computedModularity = finalQ(comStructure, degreeCount, hgraph, theStructure, totalWeight, 1., weighted);
        double computedModularityResolution = finalQ(comStructure, degreeCount, hgraph, theStructure, totalWeight, currentResolution, weighted);

        results.put("modularity", computedModularity);
        results.put("modularityResolution", computedModularityResolution);

        return results;
    }

    Community updateBestCommunity(CommunityStructure theStructure, int i, double currentResolution) {
        double best = 0.;
        Community bestCommunity = null;
        Set<Community> iter = theStructure.nodeConnectionsWeight[i].keySet();
        for (Community com : iter) {
            double qValue = q(i, com, theStructure, currentResolution);
            if (qValue > best) {
                best = qValue;
                bestCommunity = com;
            }
        }
        return bestCommunity;
    }

    int[] fillComStructure(Graph hgraph, CommunityStructure theStructure, int[] comStructure) {
//        int[] comStructure = new int[hgraph.getNodeCount()];
        int count = 0;

        for (Community com : theStructure.communities) {
        	int len=com.nodes.size();
            for (int k = 0; k< len ; k++) {
            	Integer node = (Integer) com.nodes.get(k);
                Community hidden = theStructure.invMap.get(node);
                int lenHidden = hidden.nodes.size();
                for (int j = 0; j<lenHidden; j++) {
                    comStructure[(Integer)hidden.nodes.get(j)] = count;
                }
            }
            count++;
        }
        return comStructure;
    }

    double[] fillDegreeCount(Graph<V,E> hgraph, CommunityStructure theStructure, int[] comStructure, double[] nodeDegrees, boolean weighted) {
        double[] degreeCount = new double[theStructure.communities.size()];

        for (V node : hgraph.vertexSet()) {
            int index = theStructure.map.get(node);
            if (weighted) {
                degreeCount[comStructure[index]] += nodeDegrees[index];
            } else {
 
                degreeCount[comStructure[index]] += hgraph.edgesOf(node).size();
            }

        }
        return degreeCount;
    }

    private double finalQ(int[] struct, double[] degrees, Graph<V,E> hgraph,
            CommunityStructure theStructure, double totalWeight, double usedResolution, boolean weighted) {

        double res = 0;
        double[] internal = new double[degrees.length];
        for (V n : hgraph.vertexSet()) {
            int n_index = theStructure.map.get(n);
            for (V neighbor : Graphs.neighborListOf(hgraph, n)) {
                if (n == neighbor) {
                    continue;
                }
                int neigh_index = theStructure.map.get(neighbor);
                if (struct[neigh_index] == struct[n_index]) {
                    if (weighted) {
                        internal[struct[neigh_index]] += hgraph.getEdgeWeight(hgraph.getEdge(n, neighbor));
                    } else {
                        internal[struct[neigh_index]]++;
                    }
                }
            }
        }
        for (int i = 0; i < degrees.length; i++) {
            internal[i] /= 2.0;
            res += usedResolution * (internal[i] / totalWeight) - Math.pow(degrees[i] / (2 * totalWeight), 2);//HERE
        }
        return res;
    }

    public double getModularity() {
        return modularity;
    }

    private double q(int node, Community community, CommunityStructure theStructure, double currentResolution) {
        Float edgesToFloat = theStructure.nodeConnectionsWeight[node].get(community);
        double edgesTo = 0;
        if (edgesToFloat != null) {
            edgesTo = edgesToFloat.doubleValue();
        }
        double weightSum = community.weightSum;
        double nodeWeight = theStructure.weights[node];
        double qValue = currentResolution * edgesTo - (nodeWeight * weightSum) / (2.0 * theStructure.graphWeightSum);
        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() > 1)) {
            qValue = currentResolution * edgesTo - (nodeWeight * (weightSum - nodeWeight)) / (2.0 * theStructure.graphWeightSum);
        }
        if ((theStructure.nodeCommunities[node] == community) && (theStructure.nodeCommunities[node].size() == 1)) {
            qValue = 0.;
        }
        return qValue;
    }
    
    public HashMap<Integer,ArrayList<V>> getArrayListCommunity()
    {
    	if(vertexCommunity==null)
    		return null;
    
    	
    	HashMap<Integer,ArrayList<V>> toReturn = new HashMap<Integer, ArrayList<V>>();
    	int community;
    	for(V vertex : vertexCommunity.keySet())
    	{
    		community=vertexCommunity.get(vertex);
    		if(toReturn.get(community)==null)
    			toReturn.put(community, new ArrayList<V>());
    		toReturn.get(community).add(vertex);
    	}
    	return toReturn;
    }
    
    public HashMap<Integer, TreeSet<V>> getSetCommunity()
    {
    	if(vertexCommunity==null)
    		return null;
    
    	HashMap<Integer,TreeSet<V>> toReturn = new HashMap<Integer, TreeSet<V>>();
    	int community;
    	for(V vertex : vertexCommunity.keySet())
    	{
    		community=vertexCommunity.get(vertex);
    		if(toReturn.get(community)==null)
    			toReturn.put(community, new TreeSet<V>());
    		toReturn.get(community).add(vertex);
    	}
    	return toReturn;
    }
}