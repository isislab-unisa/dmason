package it.isislab.dmason.sim.field.network.partitioning.algo.dendogram.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GraphMLImpoter<V,E> implements GraphImporter<V, E>{

	private InputStream toImport;
	private Graph<V,DefaultWeightedEdge> g;
	private Document doc;
	private boolean directed;

	public GraphMLImpoter(String file_name) throws ImportException {

		try {
			File f = new File(file_name);
			toImport=new FileInputStream(f);
		} catch (FileNotFoundException e1) {
			throw new ImportException("File "+file_name+" does not exist");
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			throw new ImportException("Parser Configuration Error");
		}
		try {
			doc = db.parse(toImport);
		} catch (Exception e1) {
			throw new ImportException("IO Problems");
		}
		Node graphML = doc.getElementsByTagName("graph").item(0).getAttributes().getNamedItem("edgedefault");
		Node gexf = doc.getElementsByTagName("graph").item(0).getAttributes().getNamedItem("defaultedgetype");
		directed = graphML == null ? gexf.getTextContent().equalsIgnoreCase("directed") : graphML.getTextContent().equalsIgnoreCase("directed");
		g = directed ?  new SimpleDirectedWeightedGraph<V,DefaultWeightedEdge>(DefaultWeightedEdge.class) : new SimpleWeightedGraph<V, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	}

	public Graph<V, DefaultWeightedEdge> getGraph() 
	{
		
		SimpleDirectedWeightedGraph<V, DefaultWeightedEdge> dg = null;
		SimpleWeightedGraph<V, DefaultWeightedEdge> ug = null;

		if(directed)
			dg = (SimpleDirectedWeightedGraph<V, DefaultWeightedEdge>) g;
		else
			ug = (SimpleWeightedGraph<V, DefaultWeightedEdge>) g;

		NodeList nodes = doc.getElementsByTagName("node");
		for (int i=0; i < nodes.getLength(); i++) {
			@SuppressWarnings("unchecked")
			V id = ((V) nodes.item(i).getAttributes().getNamedItem("id").getTextContent());
			g.addVertex(id);
			//System.out.println("Vertex "+id);
		}

		NodeList edge = doc.getElementsByTagName("edge");
		for (int i=0; i < edge.getLength(); i++) {
			NamedNodeMap attributes = edge.item(i).getAttributes();
			@SuppressWarnings("unchecked")
			V source = ((V) attributes.getNamedItem("source").getTextContent());
			@SuppressWarnings("unchecked")
			V target = ((V) attributes.getNamedItem("target").getTextContent());
			Node sweight = attributes.getNamedItem("weight");
			String weight = sweight == null ? "0"  : sweight.getTextContent(); 

			DefaultWeightedEdge dwe = new DefaultWeightedEdge();
			if(directed) dg.setEdgeWeight(dwe, Double.parseDouble(weight));
			else ug.setEdgeWeight(dwe, Double.parseDouble(weight));

			g.addEdge(source, target, dwe);
			//System.out.println("Edge "+source + " " + target+" "+weight);
		}

		return g;
	}

//		public static void main(String[] args) throws Exception{
//			GraphMLImpoter<String,Integer> gmli = new GraphMLImpoter<String, Integer>("karate.gexf");
//			Graph g;
//			System.out.println((g=gmli.getGraph()));
//			System.out.println("#Edge: "+g.edgeSet().size() + " #Node: " + g.vertexSet().size());
//		}
}
