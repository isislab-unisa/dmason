package it.isislab.dmason.sim.field.network.kway.algo.social;

import it.isislab.dmason.sim.field.network.kway.graph.Graph;

public class ComputeDispersionThread implements Runnable {

    private Graph g;
    private EdgeWDispersion e;
    private int dist;

    public ComputeDispersionThread(Graph g, EdgeWDispersion e, int dist){
        this.g=g;
        this.e=e;
        this.dist=dist;
    }

    @Override
    public void run() {
        processCommand();        
    }

    private void processCommand() {
    	DispersionOnSocialTies.setDispersion(g, e, dist);
    }

}