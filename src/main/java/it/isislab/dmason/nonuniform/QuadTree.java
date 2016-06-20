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
package it.isislab.dmason.nonuniform;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.apache.log4j.Logger;


/**
 * Class for non-uniform distributed fields. It uses a specialized QuadTree data structure
 * for partitioning Euclidean spaces which has p leafs, where p is the number of cells 
 * in the partitioning, and  each leaf has a set of object
 * 
 * @author Michele Carillo
 * @author Carmine Spagnuolo
 * @author Flavio Serrapica
 *
 */
public class QuadTree implements Comparable<QuadTree>{

	private static Logger log=Logger.getLogger(QuadTree.class);

	/**
	 * 
	 *A node of the tree 
	 *
	 */
	class BinaryNode implements Comparable<BinaryNode>{
		QuadTree node1;
		QuadTree node2;
		ORIENTATION orientation;

		/**
		 * Constructor of a node with 2 children 
		 * @param node1 a node
		 * @param node2 a node 
		 * @param orientation a position in the space
		 */
		public BinaryNode(QuadTree node1, QuadTree node2, ORIENTATION orientation) {
			super();
			this.node1 = node1;
			this.node2 = node2;
			this.orientation=orientation;
		}

		@Override
		public int compareTo(BinaryNode o) {
			// TODO Auto-generated method stub
			return Integer.compare(node1.getObjects().size()+node2.getObjects().size(),
					o.node1.getObjects().size()+o.node2.getObjects().size());
		}

	}
	public final BinaryNode createBinaryNode(QuadTree node1, QuadTree node2,ORIENTATION orientation)
	{
		return new BinaryNode(node1, node2,orientation);
	}
	/**
	 * 
	 * The position of the node in the space
	 *
	 */
	public enum ORIENTATION {
		ROOT,NW, NE, SE, SW, N, S, W, E;
	}
	/**
	 * Translate the given orientation in the opposite 
	 * @param o
	 * @return
	 */
	public static final ORIENTATION swapOrientation(ORIENTATION o)
	{
		switch (o) {
		case NW:
			return ORIENTATION.SE;
		case NE:
			return ORIENTATION.SW;
		case SW:
			return ORIENTATION.NE;
		case SE:
			return ORIENTATION.NW;
		case N:
			return ORIENTATION.S;
		case S:
			return ORIENTATION.N;
		case E:
			return ORIENTATION.W;
		case W:
			return ORIENTATION.E;
		default:
			return null;
		}
	}

	public String ID;

	public ORIENTATION orientation;
	private static int MAX_AGENTS;
	private QuadTree[] neighbors;
	private QuadTree parent;
	private List<TreeObject> objects;
	private double x1,x2,y1,y2;
	private double discretization = 0.1;
	private int level = Integer.MAX_VALUE;

	private static double ROOT_WIDTH;
	private static double ROOT_HEIGHT;

	public HashMap<ORIENTATION, ArrayList<QuadTree>> neighborhood = new HashMap<QuadTree.ORIENTATION, ArrayList<QuadTree>>();

	private int subtreeObjectsSize;

	/**
	 * Constructor of QuadTree
	 * @param _MAX_AGENTS the maximum number of object in a leaf of the tree
	 * @param x1 north west x 
	 * @param y1 north west y
	 * @param x2 south east x
	 * @param y2 south east y
	 */
	public QuadTree(int _MAX_AGENTS,double x1,double y1,double x2,double y2) {

		MAX_AGENTS=_MAX_AGENTS;
		neighbors=new QuadTree[4];
		neighbors[0] = null;
		neighbors[1] = null;
		neighbors[2] = null;
		neighbors[3] = null;
		objects=new ArrayList<TreeObject>();
		this.x1=x1;
		this.x2=x2;
		this.y1=y1;
		this.y2=y2;
		orientation=ORIENTATION.ROOT;
		parent=null;
		ROOT_WIDTH=x2;
		ROOT_HEIGHT=y2;
		ID="1";//GLOBAL_NODE_ID++;
	}

	/**
	 * Constructor 
	 * @param _MAX_AGENTS  the maximum number of object in a leaf of the tree
	 * @param x1 north west x 
	 * @param y1 north west y
	 * @param x2 south east x
	 * @param y2 south east y
	 * @param discretization the minumum size of the space for a leaf
	 * @param level the height of the node in the tree(the root has 0 level ) 
	 */
	public QuadTree(int _MAX_AGENTS,double x1,double y1,double x2,double y2,double discretization, int level) {
		this(_MAX_AGENTS, x1, y1, x2, y2);
		this.level=level;
		this.discretization=discretization;

	}
	/**
	 * Constructor 
	 * @param _MAX_AGENTS  the maximum number of object in a leaf of the tree
	 * @param x1 north west x 
	 * @param y1 north west y
	 * @param x2 south east x
	 * @param y2 south east y
	 * @param discretization the minumum size of the space for a leaf
	 */
	public QuadTree(int _MAX_AGENTS,double x1,double y1,double x2,double y2,double discretization) {
		this(_MAX_AGENTS, x1, y1, x2, y2);
		this.discretization=discretization;
	}

	/**
	 * Constructor 
	 * @param _MAX_AGENTS  the maximum number of object in a leaf of the tree
	 * @param x1 north west x 
	 * @param y1 north west y
	 * @param x2 south east x
	 * @param y2 south east y
	 * @param level the height of the node in the tree(the root has 0 level ) 
	 */
	public QuadTree(int _MAX_AGENTS,double x1,double y1,double x2,double y2, int level) {
		this(_MAX_AGENTS, x1, y1, x2, y2);
		this.level=level;
	}

	/**
	 * Constructor 
	 * @param _MAX_AGENTS  the maximum number of object in a leaf of the tree
	 * @param x1 north west x 
	 * @param y1 north west y
	 * @param x2 south east x
	 * @param y2 south east y
	 * @param orientation the position in the space of the node
	 */
	private QuadTree(int _MAX_AGENTS,double x1,double y1,double x2,double y2, ORIENTATION orientation) {

		MAX_AGENTS=_MAX_AGENTS;
		neighbors=new QuadTree[4];
		neighbors[0] = null;
		neighbors[1] = null;
		neighbors[2] = null;
		neighbors[3] = null;
		objects=new ArrayList<TreeObject>();
		this.x1=x1;
		this.x2=x2;
		this.y1=y1;
		this.y2=y2;
		this.orientation=orientation;
		//	ID=parent.ID++;//GLOBAL_NODE_ID++;
	}
	/**
	 * Constructor 
	 * @param _MAX_AGENTS  the maximum number of object in a leaf of the tree
	 * @param x1 north west x 
	 * @param y1 north west y
	 * @param x2 south east x
	 * @param y2 south east y
	 * @param discretization the minumum size of the space for a leaf
	 * @param level the height of the node in the tree(the root has 0 level ) 
	 * @param ID the unique identifier
	 */
	private QuadTree(int _MAX_AGENTS,double x1,double y1,double x2,double y2,double discretization, int level, ORIENTATION orientation,QuadTree parent, String ID) {
		this(_MAX_AGENTS, x1, y1, x2, y2, orientation);
		this.level=level;
		this.discretization=discretization;
		this.parent=parent;
		this.ID=ID;

	}

	/**
	 * Put the object in corresponding node of the tree, if it is needed split a corresponding node 
	 * @param obj  to insert
	 * @param x    position x
	 * @param y    position y
	 * @return  true if correct
	 */
	public boolean insert(Serializable obj, double x, double y)
	{
		subtreeObjectsSize++;
		if((objects.size()+1) < MAX_AGENTS || 
				((x2-x1)/2) < discretization || 
				((y2-y1)/2) < discretization ||
				(level == 0)){
			objects.add(new TreeObject(obj,x,y));	
			return true;
		}

		if(neighbors[0]==null) split(this, discretization,level);

		if (neighbors[getInternalRegion(x, y, x1 + ((x2-x1)/2), y1+((y2-y1)/2))].insert(obj, x, y)) return objects.add(new TreeObject(obj,x,y));

		return false;
	}

	/**
	 * Create four children nodes for node,   suddividendo lo spazio nel punto centrale  
	 * @param node the node 
	 * @param discretization the minumum size of the space for a leaf
	 * @param level the height of the node in the tree(the root has 0 level ) 
	 */
	private static void split(QuadTree node, double discretization, int level)
	{
		QuadTree[] neighbors=new QuadTree[4];

		neighbors[0] = new QuadTree(MAX_AGENTS, node.x1, node.y1, node.x1+(node.x2-node.x1)/2, node.y1+(node.y2-node.y1)/2, discretization,level-1, ORIENTATION.NW,node,node.ID+"0");
		neighbors[1] = new QuadTree(MAX_AGENTS, node.x1+(node.x2-node.x1)/2, node.y1, node.x2, node.y1+(node.y2-node.y1)/2, discretization,level-1, ORIENTATION.NE,node,node.ID+"1");
		neighbors[2] = new QuadTree(MAX_AGENTS, node.x1, node.y1+(node.y2-node.y1)/2, node.x1+(node.x2-node.x1)/2, node.y2, discretization,level-1, ORIENTATION.SW,node,node.ID+"2");
		neighbors[3] = new QuadTree(MAX_AGENTS, node.x1+(node.x2-node.x1)/2, node.y1+(node.y2-node.y1)/2, node.x2, node.y2, discretization,level-1, ORIENTATION.SE,node,node.ID+"3");

		node.setNeighbors(neighbors);
		for(TreeObject s: node.getObjects()) neighbors[getInternalRegion(s.x, s.y, node.x1 + ((node.x2-node.x1)/2), node.y1 + ((node.y2-node.y1)/2))].insert(s.obj, s.x, s.y);
	}
	/**
	 * Merge  
	 * @param node the node
	 * @return true if correct
	 */
	private static boolean merge(QuadTree node)
	{
		if(node.getNeighbors()[0] == null) return false;

		if(node.getNeighbors()[0].getNeighbors()[0] != null
				|| node.getNeighbors()[1].getNeighbors()[0] != null
				|| node.getNeighbors()[2].getNeighbors()[0] != null
				|| node.getNeighbors()[3].getNeighbors()[0] != null) return false;


		QuadTree[] neighbors=new QuadTree[4];
		neighbors[0] = null;
		neighbors[1] = null;
		neighbors[2] = null;
		neighbors[3] = null;
		node.setNeighbors(neighbors);

		return true;
	}

	/**
	 *  Divide the tree 
	 * @param P number of partitions 
	 * @param root the root of tree
	 * @param isToroidal true if the field is toroidal false otherwise
	 */
	public static final void partition(int P, QuadTree root, boolean isToroidal)
	{
		ArrayList<QuadTree> leafs=new ArrayList<QuadTree>(findLeafs(root));
		log.info("Partitioning "+root.getObjects().size()+" objects in P="+P +" start with "+leafs.size()+" leafs.");
		if(leafs.size() < P)
		{

			TreeSet<QuadTree> splittable_leafs=new TreeSet<QuadTree>();

			for(QuadTree leaf: leafs)
			{
				if( isSpaceSplittable(leaf)){
					splittable_leafs.add(leaf);
				}
			}
			int number_leafs=leafs.size();
			QuadTree toSplit=null;	
			while(number_leafs < P && ((toSplit=splittable_leafs.pollLast())!=null))
			{
				split(toSplit, toSplit.discretization, toSplit.level);
				log.info("SPLIT "+toSplit.ID+" "+toSplit.getObjects().size());
				if(isSpaceSplittable(toSplit.getNeighbors()[0])) splittable_leafs.add(toSplit.getNeighbors()[0]);
				if(isSpaceSplittable(toSplit.getNeighbors()[1])) splittable_leafs.add(toSplit.getNeighbors()[1]);
				if(isSpaceSplittable(toSplit.getNeighbors()[2])) splittable_leafs.add(toSplit.getNeighbors()[2]);
				if(isSpaceSplittable(toSplit.getNeighbors()[3])) splittable_leafs.add(toSplit.getNeighbors()[3]);
				number_leafs+=3;
			}
			if(number_leafs < P)
			{
				log.warn("The obtained partitioning is not possible with discretization= "+root.discretization+", level="+root.level+" and number of processors="+P);
				return;
			}
			//NOW THE VALUE OF P IS AT MOST P+2
		}else{
			TreeSet<QuadTree> parents=new TreeSet<QuadTree>(findLeafParent(leafs));
			int number_leafs=leafs.size();
			while(number_leafs >= P+3 )
			{

				QuadTree toMerge=parents.pollFirst();

				log.info("MERGE "+toMerge.ID+" "+toMerge.getObjects().size());

				merge(toMerge);

				QuadTree parent=toMerge.parent;

				if(parent.getNeighbors()[0].getNeighbors()[0] == null
						&& parent.getNeighbors()[1].getNeighbors()[0] == null
						&& parent.getNeighbors()[2].getNeighbors()[0] == null
						&& parent.getNeighbors()[3].getNeighbors()[0] == null)
					parents.add(parent);
				number_leafs-=3;
			}
			log.info("Correctness merge:"+checkMinMaxSplitMerge(root));
			while(number_leafs != P)
			{
				refinePartition(root);
				number_leafs--;
			}
		}
		printQuadTree(root, 0);
		computeNeighborhood(root,isToroidal);

	}
	/**
	 * Calculate the neighborhood
	 * 
	 * @param root root of the tree
	 * @param isToroidal true if the field is toroidal 
	 */
	private static final void computeNeighborhood(QuadTree root, boolean isToroidal)
	{
		ArrayList<QuadTree> leafs=new ArrayList<QuadTree>(findLeafs(root));

		for (QuadTree leaf1 : leafs) {
			for (QuadTree leaf2 : leafs) {

				if(!leaf1.equals(leaf2))
				{
					if(isWestNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.W)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.W);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.W,npart);
					}
					if(isEastNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.E)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.E);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.E,npart);
					}
					if(isNorthNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.N)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.N);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.N,npart);
					}
					if(isSouthNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.S)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.S);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.S,npart);
					}
					if(isNorthWestNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.NW)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.NW);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.NW,npart);
					}
					if(isSouthEastNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.SE)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.SE);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.SE,npart);
					}
					if(isNorthEastNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.NE)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.NE);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.NE,npart);
					}
					if(isSouthWestNeighbor(leaf1, leaf2,isToroidal))
					{
						ArrayList<QuadTree> npart=leaf1.neighborhood.get(ORIENTATION.SW)==null?new ArrayList<QuadTree>():leaf1.neighborhood.get(ORIENTATION.SW);
						npart.add(leaf2);
						leaf1.neighborhood.put(ORIENTATION.SW,npart);
					}
				}
			}
		}
	}
	private static final boolean isNorthNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return (((l1.getY1() == l2.getY2()) || (isToroidal && (l1.getY1() == 0 && l2.getY2() == ROOT_WIDTH))) 
				&& (((l1.getX1() <= l2.getX1()) && (l1.getX2() >= l2.getX2())) || ((l1.getX1() >= l2.getX1()) && (l1.getX2() <= l2.getX2()))));
	}
	private static final boolean isSouthNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return isNorthNeighbor(l2, l1,isToroidal);
	}
	private static final boolean isWestNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return (((l1.getX1() == l2.getX2()))|| (isToroidal && (l1.getX1() == 0 && l2.getX2() == ROOT_HEIGHT)))  
				&& (((l1.getY1() <= l2.getY1()) && (l1.getY2() >= l2.getY2())) || ((l1.getY1() >= l2.getY1()) && (l1.getY2() <= l2.getY2())));
	}
	private static final boolean isEastNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return isWestNeighbor(l2, l1, isToroidal);
	}




	private static final boolean isNorthWestNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return (l1.getX1() == l2.getX2()) && (l1.getY1() == l2.getY2())  || (isToroidal && (((l1.getX1()%ROOT_WIDTH) == (l2.getX2()%ROOT_WIDTH)) && ((l1.getY1()%ROOT_HEIGHT) == (l2.getY2()%ROOT_HEIGHT))));
	}
	private static final boolean isSouthEastNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return isNorthWestNeighbor(l2, l1, isToroidal);
	}
	private static final boolean isSouthWestNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return (l1.getX1() == l2.getX2()) && (l1.getY2() == l2.getY1()) || (isToroidal && (((l1.getX1()%ROOT_WIDTH) == (l2.getX2()%ROOT_WIDTH)) && ((l1.getY2()%ROOT_HEIGHT) == (l2.getY1()%ROOT_HEIGHT))));
	}
	private static final boolean isNorthEastNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	{
		return isSouthWestNeighbor(l2, l1, isToroidal);
	}

	//	private static final boolean isNorthWestNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	//	{
	//  		return quadTreeContainsPoint(l2, 
	//  			isToroidal?
	//  					(ROOT_WIDTH+l1.getX1()-(l1.discretization/2))%ROOT_WIDTH:
	//  					(l1.getX1()-(l1.discretization/2)), 
	//  			isToroidal?
	//  		  			(ROOT_HEIGHT+l1.getY1()-(l1.discretization/2))%ROOT_HEIGHT:
	//  		  			(l1.getY1()-(l1.discretization/2)));
	//	}
	//	private static final boolean isSouthWestNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	//	{
	//		return quadTreeContainsPoint(l2, 
	//	  			isToroidal?
	//	  					(ROOT_WIDTH+l1.getX1()-(l1.discretization/2))%ROOT_WIDTH:
	//	  					(l1.getX1()-(l1.discretization/2)), 
	//	  			isToroidal?
	//	  		  			(ROOT_HEIGHT+l1.getY2()+(l1.discretization/2))%ROOT_HEIGHT:
	//	  		  			(l1.getY2()+(l1.discretization/2)));
	//	}
	//	private static final boolean isSouthEastNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	//	{
	//		return quadTreeContainsPoint(l2, 
	//	  			isToroidal?
	//	  					(ROOT_WIDTH+l1.getX2()+(l1.discretization/2))%ROOT_WIDTH:
	//	  					(l1.getX2()+(l1.discretization/2)), 
	//	  			isToroidal?
	//	  		  			(ROOT_HEIGHT+l1.getY2()+(l1.discretization/2))%ROOT_HEIGHT:
	//	  		  			(l1.getY2()+(l1.discretization/2)));
	//	}
	//	private static final boolean isNorthEastNeighbor(QuadTree l1, QuadTree l2, boolean isToroidal)
	//	{
	//		return quadTreeContainsPoint(l2, 
	//	  			isToroidal?
	//	  					(ROOT_WIDTH+l1.getX2()+(l1.discretization/2))%ROOT_WIDTH:
	//	  					(l1.getX2()+(l1.discretization/2)), 
	//	  			isToroidal?
	//	  		  			(ROOT_HEIGHT+l1.getY1()-(l1.discretization/2))%ROOT_HEIGHT:
	//	  		  			(l1.getY1()-(l1.discretization/2)));
	//	}
	//	
	//	private static final boolean quadTreeContainsPoint(QuadTree node, double x,double y)
	//	{
	//		return x > node.getX1() && x < node.getX2() && y > node.getY1() && y < node.getY2();
	//	}


	private static boolean checkMinMaxSplitMerge(QuadTree root)
	{
		TreeSet<QuadTree> leafs=new TreeSet<QuadTree>(findLeafs(root));
		TreeSet<QuadTree> parents=new TreeSet<QuadTree>(findLeafParent(findLeafs(root)));
		return leafs.pollFirst().getObjects().size() <= parents.pollLast().getObjects().size() ;
	}

	private static void refinePartition(QuadTree root) {
		ArrayList<QuadTree> leafs=new ArrayList<QuadTree>(findLeafs(root));

		BinaryNode mergable=findBinaryMergableBrother(leafs);
		log.info("MERGE REFINE "+mergable.node1.ID+" "+mergable.node2.ID);

		QuadTree node=new QuadTree(MAX_AGENTS, mergable.node1.x1, mergable.node1.y1, mergable.node2.x2, mergable.node2.y2, mergable.node1.discretization,mergable.node1.level, mergable.orientation,mergable.node1.parent, mergable.node1.ID);
		node.getObjects().addAll(mergable.node1.getObjects());
		node.getObjects().addAll(mergable.node2.getObjects());

		switch (mergable.orientation) {
		case N:
			mergable.node1.parent.getNeighbors()[0]=node;
			mergable.node1.parent.getNeighbors()[1]=null;
			break;
		case S:
			mergable.node1.parent.getNeighbors()[2]=node;
			mergable.node1.parent.getNeighbors()[3]=null;

			break;
		case W:
			mergable.node1.parent.getNeighbors()[0]=node;
			mergable.node1.parent.getNeighbors()[2]=null;

			break;
		case E:
			mergable.node1.parent.getNeighbors()[1]=node;
			mergable.node1.parent.getNeighbors()[3]=null;
			break;
		default:
			break;
		}

	}

	private static BinaryNode findBinaryMergableBrother(List<QuadTree> leafs)
	{
		TreeSet<BinaryNode> mergeable=new TreeSet<BinaryNode>();
		for(QuadTree leaf: leafs)
		{

			switch (leaf.orientation) {

			case NW:
				if(leaf.parent.neighbors[1] != null && leaf.parent.neighbors[1].orientation.equals(ORIENTATION.NE))
				{
					mergeable.add(leaf.createBinaryNode(leaf, leaf.parent.neighbors[1],ORIENTATION.N));
				}
				if(leaf.parent.neighbors[2] != null && leaf.parent.neighbors[2].orientation.equals(ORIENTATION.SW))
				{
					mergeable.add(leaf.createBinaryNode(leaf, leaf.parent.neighbors[2],ORIENTATION.W));
				}
				break;
			case NE:
				if(leaf.parent.neighbors[3] != null && leaf.parent.neighbors[3].orientation.equals(ORIENTATION.SE))
				{
					mergeable.add(leaf.createBinaryNode(leaf, leaf.parent.neighbors[3],ORIENTATION.E));
				}
				break;
			case SW:
				if(leaf.parent.neighbors[3] != null && leaf.parent.neighbors[3].orientation.equals(ORIENTATION.SE))
				{
					mergeable.add(leaf.createBinaryNode(leaf, leaf.parent.neighbors[3],ORIENTATION.S));

				}
				break;
			default:
				break;
			}
		}

		return mergeable.pollFirst();
	}
	private static  boolean isSpaceSplittable(QuadTree node)
	{
		return ( ((node.x2-node.x1)/2) >= node.discretization && 
				((node.y2-node.y1)/2) > node.discretization &&
				(node.level != 0));
	}

	private static int getInternalRegion(double x, double y,double cx, double cy)
	{
		if(x < cx && y < cy) return 0;
		if(x < cx && y >= cy) return 2;
		if(x >= cx && y < cy) return 1;
		return 3;
	}
	/**
	 * restiruisce le foglie dell albero
	 * @param node
	 * @return
	 */
	public static List<QuadTree> findLeafs(QuadTree node)
	{
		if(node==null) return new ArrayList<QuadTree>();
		QuadTree[] neighbors=node.getNeighbors();
		ArrayList<QuadTree> leafs=new ArrayList<QuadTree>();

		if(neighbors[0]== null && neighbors[1]== null && neighbors[2]== null && neighbors[3]== null)
		{
			leafs.add(node);
			return leafs;
		}
		if(neighbors[0]!=null)
		{
			leafs.addAll(findLeafs(neighbors[0]));
		}
		if(neighbors[1]!=null)
		{
			leafs.addAll(findLeafs(neighbors[1]));
		}
		if(neighbors[2]!=null)
		{
			leafs.addAll(findLeafs(neighbors[2]));
		}
		if(neighbors[3]!=null)
		{
			leafs.addAll(findLeafs(neighbors[3]));
		}
		return leafs;

	}

	private static List<QuadTree> findLeafParent(List<QuadTree> leafs)
	{
		ArrayList<QuadTree> parents=new ArrayList<QuadTree>();
		for(QuadTree leaf: leafs)
		{
			boolean toAdd=true;

			for(QuadTree brother: leaf.parent.getNeighbors())
			{
				if(brother != leaf && brother.getNeighbors()[0]!=null)
				{
					toAdd=false;
					break;
				}
			}
			if(toAdd && !parents.contains(leaf.parent)) parents.add(leaf.parent);

		}

		return parents;
	}

	/**
	 * Print
	 * @param node
	 */
	public static final void printTree(QuadTree node)
	{
		QuadTree[] neighbors=node.getNeighbors();
		System.out.print(node.ID+" ");
		if(neighbors[0]!=null)
		{
			System.out.print(neighbors[0].ID+" ");
			printTree(neighbors[0]);
		}
		if(neighbors[1]!=null)
		{
			System.out.print(neighbors[1].ID+" ");
			printTree(neighbors[1]);
		}
		if(neighbors[2]!=null)
		{
			System.out.print(neighbors[2].ID+" ");
			printTree(neighbors[2]);
		}
		if(neighbors[3]!=null)
		{
			System.out.print(neighbors[3].ID+" ");
			printTree(neighbors[3]);
		}
		System.out.println("");

	}
	public static final int printLeafs(QuadTree node)
	{
		QuadTree[] neighbors=node.getNeighbors();
		if(neighbors[0]==null && neighbors[1]==null && neighbors[2]==null && neighbors[3]==null)
		{
			//			System.out.println(node.ID+" "+node.getObjects().size());
			return 1;
		}

		int leafs=0;
		if(neighbors[0]!=null)
		{
			leafs+=printLeafs(neighbors[0]);
		}
		if(neighbors[1]!=null)
		{
			leafs+=printLeafs(neighbors[1]);
		}
		if(neighbors[2]!=null)
		{
			leafs+=printLeafs(neighbors[2]);
		}
		if(neighbors[3]!=null)
		{
			leafs+=printLeafs(neighbors[3]);
		}

		return leafs;


	}
	private static double computeArea(QuadTree node)
	{
		QuadTree[] neighbors=node.getNeighbors();
		if(neighbors[0]==null && neighbors[1]==null && neighbors[2]==null &&neighbors[3]==null) return (node.getY2()-(node.getY1()))*(node.getX2()-(node.getX1()));

		double parea=(neighbors[0]!=null)?computeArea(neighbors[0]):0;
		parea+=(neighbors[1]!=null)?computeArea(neighbors[1]):0;
		parea+=(neighbors[2]!=null)?computeArea(neighbors[2]):0;
		parea+=(neighbors[3]!=null)?computeArea(neighbors[3]):0;

		return parea;

	}
	/**
	 * Verifica la correttezza della struttura dell albero
	 * @param root
	 * @return
	 */
	public static final boolean checkObjects(QuadTree root)
	{
		if(root == null || (root.getNeighbors()[0]==null && root.getNeighbors()[1]==null && root.getNeighbors()[2]==null && root.getNeighbors()[3]==null)) return true;

		int sum=0;
		sum+=root.getNeighbors()[0]!=null?root.getNeighbors()[0].getObjects().size():0;
		sum+=root.getNeighbors()[1]!=null?root.getNeighbors()[1].getObjects().size():0;
		sum+=root.getNeighbors()[2]!=null?root.getNeighbors()[2].getObjects().size():0;
		sum+=root.getNeighbors()[3]!=null?root.getNeighbors()[3].getObjects().size():0;

		if(sum != root.getObjects().size())
		{

			return false;
		}

		return checkObjects(root.getNeighbors()[0]) && checkObjects(root.getNeighbors()[1]) && checkObjects(root.getNeighbors()[2]) && checkObjects(root.getNeighbors()[3]);
	}


	/**
	 * 
	 * @param root
	 * @return
	 */
	public static final  List<QuadTree> getPartitioning(QuadTree root)
	{
		return new ArrayList<QuadTree>(findLeafs(root));

	}

	/**
	 * Restituisce info sul partizion 
	 * @param partitioning
	 * @return
	 */
	public static String reportPartitioning(List<QuadTree> partitioning)
	{
		String report="Partitioning={\n";

		for (QuadTree quadTree : partitioning) {
			report+="\t"+
					quadTree + " \n\t--neighborhood=" +
					quadTree.neighborhood+"#\n";

		}

		return report+"\n}";
	}

	/**
	 * Resttuisce statistiche sul partizionamento
	 * @param partitioning
	 * @return
	 */
	public static String reportStatsPartitioning(List<QuadTree> partitioning)
	{
		String report="Partitioning={\n P: "+partitioning.size()+", ";
		int comm_channel=0;
		int comm_amount=0;
		int tot_agents=0;
		HashMap<QuadTree,Integer> map_sizes=new HashMap<QuadTree, Integer>();
		for (QuadTree quadTree : partitioning) {
			comm_channel+=quadTree.neighborhood.size();
			map_sizes.put(quadTree, quadTree.getObjects().size());
			tot_agents+=quadTree.getObjects().size();
			for(ArrayList<QuadTree> subscriber:quadTree.neighborhood.values())
			{
				comm_amount+=subscriber.size();
			}
		}

		report+="nummer of topics: "+comm_channel+", communication amount: "+comm_amount;
		int min_size=Collections.min(map_sizes.values());
		int max_size=Collections.max(map_sizes.values());
		double mean_size=tot_agents/partitioning.size();
		double unbalance=((max_size*100)/mean_size)-100;

		report+=", min size: "+min_size+", max size: "+max_size+", mean size: "+mean_size+", max unbalance: "+unbalance+"%";


		return report+"\n}";
	}
	public static final void printQuadTree(QuadTree root, int level){
		if(root==null)
			return;
		printQuadTree(root.getNeighbors()[0], level+1);
		printQuadTree(root.getNeighbors()[1], level+1);
		if(level!=0){
			for(int i=0;i<level-1;i++)
				System.out.print("|\t");
			System.out.println("|-------"+root.ID+" ["+root.getObjects().size()+" "+root.orientation+"]");

		}
		else
			System.out.println(root.ID+" ["+root.getObjects().size()+"]");
		printQuadTree(root.getNeighbors()[2], level+1);
		printQuadTree(root.getNeighbors()[3], level+1);
	}    
	public static void main(String[] args) {
		//TEST MAIN
		int W=600;
		int H=600;

		int TOT_AGENTS=4;
		int P=3;

		int MAX_AGENTS=TOT_AGENTS/(P);
		double discretiazion=20;
		int level=4;

		Random r=new Random(0);

		//QuadTree t=new QuadTree(MAX_AGENTS, 0, 0, W, H,discretiazion,level);
		QuadTree t=new QuadTree(MAX_AGENTS, 0, 0, W, H);
		//		for (int i = 0; i < TOT_AGENTS; i++) {
		//			if(!t.insert(new String(i+""), r.nextInt(W),  r.nextInt(H)))
		////				if(!t.insert(new String(i+""), r.nextGaussian()*1920,  r.nextGaussian()*1080))
		//			{
		//				System.err.println("Error");
		//				System.exit(-1);
		//			}
		//		}
		t.insert(new String(0+""), 450, 450);
		t.insert(new String(1+""), 455, 455);

		t.insert(new String(2+""), 400, 100);
		t.insert(new String(3+""), 500, 100);

		//		t.insert(new String(3+""), 400, 200);
		//		t.insert(new String(3+""), 500, 200);

		System.out.println(checkObjects(t));

		System.out.println("Test area split: "+(computeArea(t)==(W*H)));
		String firstlevelprint=(t.getNeighbors()[0]!=null)?((t.getNeighbors()[0].getSubtreeObjectsSize()+t.getNeighbors()[1].getSubtreeObjectsSize()+t.getNeighbors()[2].getSubtreeObjectsSize()+t.getNeighbors()[3].getSubtreeObjectsSize()))+"":"NO FIRST LEVEL";
		System.out.println("Test number of agents ROOT: "+t.getSubtreeObjectsSize()+" FIRST TREE LEVEL: "+firstlevelprint);

		System.out.println("Number of leafs "+printLeafs(t));
		//		printTree(t);
		printQuadTree(t,0);
		System.out.println("----------------------------------");
		QuadTree.partition(P, t,true);
		System.out.println("----------------------------------");
		System.out.println("Number of leafs "+printLeafs(t));

		printQuadTree(t,0);

		//		System.out.println(QuadTree.reportPartitioning(QuadTree.getPartitioning(t)));

		System.out.println(QuadTree.reportStatsPartitioning(QuadTree.getPartitioning(t)));

		for(QuadTree leaf: QuadTree.getPartitioning(t))
		{
			System.out.println(leaf);
			System.out.println(leaf.neighborhood);
		}
	}

	@Override
	public String toString() {
		return "QuadTree "+ID+" [objects size=" + objects.size() +" ORIENTATION="+orientation+" "+ ", x1=" + x1 +", y1=" + y1 +", x2=" + x2+ ", y2=" + y2 + "]";
	}

	//GETTER AND SETTER

	public static double getMAX_AGENTS() {
		return MAX_AGENTS;
	}

	public double getDiscretization() {
		return discretization;
	}
	public void setDiscretization(double discretization) {
		this.discretization = discretization;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getSubtreeObjectsSize() {
		return subtreeObjectsSize;
	}
	public void setSubtreeObjectsSize(int subtreeObjectsSize) {
		this.subtreeObjectsSize = subtreeObjectsSize;
	}
	public static void setMAX_AGENTS(int mAX_AGENTS) {
		MAX_AGENTS = mAX_AGENTS;
	}

	public QuadTree[] getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(QuadTree[] neighbors) {
		this.neighbors = neighbors;
	}

	public List<TreeObject> getObjects() {
		return objects;
	}

	public void setObjects(List<TreeObject> objects) {
		this.objects = objects;
	}

	public double getX1() {
		return x1;
	}

	public void setX1(double x1) {
		this.x1 = x1;
	}

	public double getX2() {
		return x2;
	}

	public void setX2(double x2) {
		this.x2 = x2;
	}

	public double getY1() {
		return y1;
	}

	public void setY1(double y1) {
		this.y1 = y1;
	}

	public double getY2() {
		return y2;
	}

	public void setY2(double y2) {
		this.y2 = y2;
	}
	@Override
	public int compareTo(QuadTree o) {
		int r;
		return (r=Integer.compare(this.getObjects().size(),o.getObjects().size()))==0?this.ID.compareTo(o.ID):r;

	}


	//END GETTER AND SETTER

}
