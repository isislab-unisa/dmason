#DMASON kway Package

This package contains several classes that you can use to evaluate the performance of some partitioning algorithms. Moreover, you can include this work in your simulations to partition the field Network of D-MASON. Here you can find the definition of the data structures used with some utilities classes.


## Usage in your simulation
You generate the information about the partitioning of the field Network through this code:
 
		`NetworkPartition parts_data=PartitionManager.getNetworkPartition(graph_path, graph_parts_path);`
		
The params in input are:

- graph_path: the path of the graph that will be used in the simulation. The formats allowed to describe the graph are edgelist and graph.

- graph_parts_path: the path of the file describing the partitioning of the graph. This file looks like the standard format provided by Metis and by the suite KaHIP; it is named with the convention *.part.k, where k is the number of blocks given as input to the partitioning program. This file contains n lines. In each line the block ID of the corresponding vertex is given, i.e. line i contains the block ID of the vertex i (here the vertices are numbered from 0 to n-1). The block IDs are numbered consecutively from 0 to k-1. You can generate this file invoking the method partitioning(), implemented by the classes which represent the algorithms.
				

NetworkPartition is an object that stores the following information:

	`private Graph original_graph;
	 private Graph super_graph;
	 private HashMap<Integer, Vertex> parts2graph;
	 private HashMap<Integer, Integer> parts2graph_id;
	 private GraphSubscribersEdgeList edges_subscriber_lsit;
	 private HashMap<Vertex, Integer> graph2parts;
	 private HashMap<Integer, SuperVertex> parts2SuperGraph;`

* original_graph: the object Graph which represents the original graph.

* super_graph: the object Graph which describes the super graph generated on the basis of the computed partition.

* parts2graph & graph2parts: mapping between a vertex and the block which it belongs to. 

* parts2graph_id: mapping between a vertex id and the block which it belong to.

* parts2SuperGraph: mapping beetween a super vertex and the block associated with it.

* edges_subscriber_lsit: edges used to create/subscribe topics.
	

### Example
You can see an example of how to use this support tool in your simulation in the simulation DNetworkSIR2015 (package `it.isislab.dmason.sim.app.DNetworkSIR2015`). 

While the package `it.isislab.dmason.sim.field.network.kway.util.NetworkPartition` contains the definition of the class `NetworkPartition` (described above), the package `it.isislab.dmason.sim.field.network.DNetwork` contains the definition of the class `DNetwork` that implements the distributed version of the field `Network` of MASON. 

The main goal of this toy simulation is show how to use the DNetwork and the NetworkPartition. The simulations implements on a Network of agents the [SIR](https://en.wikipedia.org/wiki/Epidemic_model) epidemic model.


## AlgoBenchmark.java tool for compare k-way partitioning algorithms

This tool allows you to calculate several metrics about a supergraph,
in order to compare the quality of the partitions produced by a partitioning algorithm.


### Algorithms 

In this tool, we have considered the following partitioning algorithms:
* [Metis](http://glaros.dtc.umn.edu/gkhome/node/110).
* [Metis-Relaxed](http://glaros.dtc.umn.edu/gkhome/node/110).
* Kaffpa and KaffpaE, from the [KaHIP suite](http://algo2.iti.kit.edu/documents/kahip/index.html).
* [Ja-be-Ja](http://glaros.dtc.umn.edu/gkhome/node/110).
* Random
  * (our baseline)


### Requirements

This tool has been developed on a Unix system.
For executing **Metis, Metis-Relaxed, Kaffpa and KaffpaE** 
you have to make them available in the system environment (see each install instructions).

### Metrics

For each supergraph, the following values are calculated:
* number of superedges, that is the number of communication channels 
* total weight of superedges, i.e. the sum of the weight of edges composing a superedge
* variance about the dimension of a supervertex
* partitions imbalance


### How it works

#### Input file format

You can use two different graph formats:
* *.edgelist
* *.graph

To run the script, you need a file describing the test you want to execute in this format:

**testId;algorithmName;graphPath;numberOfPartition;binPathOfTheExecutable**

For jabeja:	
**testId;algorithmName;graphPath;numberOfPartitions;numberOfIterations;temperature;temperatureDelta**

To do this, you need to execute the executeTest() method of the AlgoBenchmark class.
	

#### Supergraphs

The execution of these algorithms will produce in output a file ***.part.numberOfComponents**,
where each line, representing a vertex, contains the id of the belonging partition of the vertex.

To perform the creation of the supergraph we have considered:
* that all nodes belonging to the same partition will be inserted in the same supervertex 
* each partition as a supervertex

Concerning the construction of a superedge:
* a superedge exists if exists an edge between two vertices belonging to different partitions
  * if more edges that span over the same partitions exist, they will be collapsed in the same superedge
* if exist one or more edges between two vertices belonging to the same partition, 
will be saved the number of these objects, as self-loops.


#### Output

The execution of the script will give in output a file where each line looks like this:

**test_id;graph_name;time;algorithm;numSuperEdges;weightSuperEdges;variance;imbalance**
























