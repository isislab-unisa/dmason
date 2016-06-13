/**
 * Copyright 2016 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package it.isislab.dmason.sim.field.network.kway.algo.social.util;

import it.isislab.dmason.sim.field.network.kway.algo.social.GraphWDispersion;
import it.isislab.dmason.sim.field.network.kway.graph.Edge;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.Vertex;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;


public class JGraphFrame extends JFrame
{
    private static final long serialVersionUID = 2202072534703043194L;

    public static void main(String [] args)
    {
    	JGraphFrame frame = new JGraphFrame();
    	frame.init(GraphWDispersion.generateGraphSubOptimal(8));
       
        frame.setTitle("JGraph ISISLab");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void init(Graph g)
    {

    	JGraphXAdapter<Vertex, Edge> jgxAdapter = new JGraphXAdapter<Vertex, Edge>(g);
    	jgxAdapter.setCellsEditable(false);
        getContentPane().add(new mxGraphComponent(jgxAdapter));

        mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

    }
    public void initWithDispersion(Graph g,HashMap<Color, ArrayList<Vertex>> mapcolor)
    {

    	JGraphXAdapter<Vertex, Edge> jgxAdapter =
    			new JGraphXAdapter<Vertex, Edge>(g);
    	jgxAdapter.setCellsEditable(false);
        getContentPane().add(new mxGraphComponent(jgxAdapter));
        HashMap<Vertex, mxICell> xTov=jgxAdapter.getVertexToCellMap();
        for(Color c :mapcolor.keySet())
        {
        	ArrayList<mxICell> vmap=new ArrayList<mxICell>();
        	for(Vertex v: mapcolor.get(c))
        		vmap.add(xTov.get(v));
        	
        	jgxAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(c), vmap.toArray());
        }
        mxFastOrganicLayout layout = new mxFastOrganicLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

    }
    public void initWithCircularLayout(Graph g)
    {

    	JGraphXAdapter<Vertex, Edge> jgxAdapter = new JGraphXAdapter<Vertex, Edge>(g);
    	jgxAdapter.setCellsEditable(false);
        getContentPane().add(new mxGraphComponent(jgxAdapter));

        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

    }
    public void initWithDispersionCircularLayout(Graph g,HashMap<Color, ArrayList<Vertex>> mapcolor)
    {

    	JGraphXAdapter<Vertex, Edge> jgxAdapter =
    			new JGraphXAdapter<Vertex, Edge>(g);
    	jgxAdapter.setCellsEditable(false);
        getContentPane().add(new mxGraphComponent(jgxAdapter));
        HashMap<Vertex, mxICell> xTov=jgxAdapter.getVertexToCellMap();
        for(Color c :mapcolor.keySet())
        {
        	ArrayList<mxICell> vmap=new ArrayList<mxICell>();
        	for(Vertex v: mapcolor.get(c))
        		vmap.add(xTov.get(v));
        	
        	jgxAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxUtils.getHexColorString(c), vmap.toArray());
        }
        mxCircleLayout layout = new mxCircleLayout(jgxAdapter);
        layout.execute(jgxAdapter.getDefaultParent());

    }
    
}

