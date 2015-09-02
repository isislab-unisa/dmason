package it.isislab.dmason.sim.field.network.kway.algo.metis;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.algo.interfaces.PartitioningAlgorithm;

import java.io.IOException;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "22/7/2015"
		)
public class MetisRelaxedProcessBinding implements PartitioningAlgorithm {

	/**
	 * @param bin_path - bin path of the executable
	 * @param graph_path - path of the graph to partition
	 * @param K - number of partitions
	 */
	public MetisRelaxedProcessBinding(String bin_path, String graph_path, int k) {
		this.bin_path = bin_path;
		this.graph_path = graph_path;
		this.nPart = k;
	}

	@Override
	public String partitioning() throws IOException, InterruptedException {

		Process part = Runtime.getRuntime().exec(bin_path + " " + graph_path + " " + nPart);
		int status = part.waitFor();

		if (status != 0) {
			System.err.println("Error in calling metis!");
			System.exit(0);
		}

		String output_file = graph_path + ".part." + nPart;

		return output_file;
	}

	private String graph_path;
	private String bin_path;
	private Integer nPart;
}
