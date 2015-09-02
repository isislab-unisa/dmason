package it.isislab.dmason.sim.field.network.kway.algo.interfaces;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.IOException;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "22/7/2015"
		)
public  interface PartitioningAlgorithm {

	/**
	 * Executes the partition of a graph using the algorithm specified 
	 * by the class which implements this interface.
	 * @return the path of the file which describes the partition calculated.
	 */
	public  String partitioning() throws IOException,InterruptedException;
}
