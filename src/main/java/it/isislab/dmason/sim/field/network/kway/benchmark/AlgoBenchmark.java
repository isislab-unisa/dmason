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
package it.isislab.dmason.sim.field.network.kway.benchmark;

import it.isislab.dmason.annotation.AuthorAnnotation;
import it.isislab.dmason.sim.field.network.kway.algo.jabeja.JabeJa;
import it.isislab.dmason.sim.field.network.kway.algo.kaffpa.KaffpaEProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.kaffpa.KaffpaProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.metis.MetisProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.metis.MetisRelaxedProcessBinding;
import it.isislab.dmason.sim.field.network.kway.algo.random.Random;
import it.isislab.dmason.sim.field.network.kway.graph.Graph;
import it.isislab.dmason.sim.field.network.kway.graph.tools.Cleaner;
import it.isislab.dmason.sim.field.network.kway.graph.tools.DirectToIndirect;
import it.isislab.dmason.sim.field.network.kway.graph.tools.GraphFormatConverter;
import it.isislab.dmason.sim.field.network.kway.graph.tools.Metrics;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@AuthorAnnotation(
		author = {"Alessia Antelmi", "Carmine Spagnuolo"},
		date = "22/7/2015"
		)
public class AlgoBenchmark {

	/**
	 * See the file README.md for information.
	 * 
	 * @param filepath - the file describing the tests to execute
	 * @param output_filepath - path of the file describing the results
	 * @param infoPart - the number of components of the partition
	 * @throws InterruptedException the exception
	 */
	public static void executeTest(String filepath, String output_filepath, int infoPart) throws InterruptedException {
		String params;
		String[] infoTest;
		BufferedReader in = null;

		try {
			/* Reads configuration file about tests */
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));

			/* Writes the results of tests executed (in test-conf-x.results) */
			out = new BufferedOutputStream(new FileOutputStream(output_filepath
					+ File.separator + "test-conf-" + infoPart + ".results"));

			/* Every row represents info about a graph to test */
			while ((params = in.readLine()) != null) {

				if (params.equals(""))
					break;

				if (params.equals("#"))
					continue;

				infoTest = params.split(";");

				id = infoTest[0]; 							/* Test id */
				pathGraphName = infoTest[2]; 				/* graph name path */
				numPart = Integer.parseInt(infoTest[3]); 	/* number of partitions */

				String[] infoGraphName = pathGraphName.split(File.separator);
				graphName = infoGraphName[infoGraphName.length - 1].split("\\.")[0]; // graph name

				String[] s = infoGraphName[infoGraphName.length - 1].split("\\.");
				format = s[s.length - 1]; // format

				/* executes format conversions */
				doConversions(format);

				/* algorithm to perform */
				if (infoTest[1].equalsIgnoreCase("jabeja")) {
					/* niter, temp, tempDelta */
					runJabeja(Integer.parseInt(infoTest[4]),Float.parseFloat(infoTest[5]),Float.parseFloat(infoTest[6]));
				} else if (infoTest[1].equalsIgnoreCase("metis")) {
					runMetis(infoTest[4]);
				} else if (infoTest[1].equalsIgnoreCase("metis-relaxed")) {
					runMetisRelaxed(infoTest[4]);
				} else if (infoTest[1].equalsIgnoreCase("kaffpa")) {
					runKaffpa(infoTest[4]);
				} else if (infoTest[1].equalsIgnoreCase("kaffpaE")) {
					runKaffpaE(infoTest[0]);
				} else if (infoTest[1].equalsIgnoreCase("random")) {
					runRandom();
				} else {
					String error = "Error-algorithm unknown\n";
					out.write(error.getBytes());
					continue;
				}

				Cleaner.cleanOutput(new File(System.getProperty("user.dir")), graphName);

			}// end config-test file

			Cleaner.cleanOutputPartition(new File(DIRGRAPHTEST));
			Cleaner.cleanDir(new File(JABEJA_OUTPUT));

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/* auxiliary methods */

	private static void doConversions(String format) throws IOException {

		if (format.equalsIgnoreCase("edgelist")) {

			vertex_names = GraphFormatConverter.createMappingNamesGraphEdgelist(pathGraphName);

			/* format conversion: from edgelist to graph */
			GraphFormatConverter.edgelist2dotGraph(pathGraphName, graphName, vertex_names);

			/* default path of the output format conversion performed */
			graph_path = System.getProperty("user.dir") + File.separator + graphName + ".graph";

			pathFormatEdgelistDefault = pathGraphName;
			pathFormatDotGraphDefault = graph_path;

		} else if (format.equalsIgnoreCase("graph")) {

			vertex_names = GraphFormatConverter.createMappingNamesGraphDotGraph(pathGraphName);

			/* format conversion: from graph to edgelist */
			GraphFormatConverter.dotGraph2edgelist(pathGraphName, graphName, vertex_names);

			/* default path of the output format conversion performed */
			graph_path = System.getProperty("user.dir") + File.separator + graphName + ".edgelist";

			pathFormatDotGraphDefault = pathGraphName;
			pathFormatEdgelistDefault = graph_path;

		} else {
			throw new IllegalArgumentException("Format Unknown.");
		}

	}

	private static void runJabeja(int niter, float temp, float tempDelta) throws IOException, InterruptedException {

		/* conversion of the graph from direct to indirect */
		String graphNameEdgelistUndirect = System.getProperty("user.dir") + File.separator + graphName + "I";
		DirectToIndirect.directToIndirect(pathFormatEdgelistDefault, graphNameEdgelistUndirect);
		
		startTime = System.currentTimeMillis();

		JabeJa j = new JabeJa(graphNameEdgelistUndirect + ".edgelist", 1,
				"edgelist", niter, numPart, temp, tempDelta, vertex_names);
		outputPartDefault = j.partitioning();

		totalTime = System.currentTimeMillis() - startTime;

		timeAlg = id + ";" + graphName + ";" + totalTime + ";jabeja;";
		out.write(timeAlg.getBytes());

		calculateMetrics(outputPartDefault, pathFormatEdgelistDefault, out);

	}

	private static void runMetis(String bin_path) throws IOException, InterruptedException {

		startTime = System.currentTimeMillis();

		MetisProcessBinding m = new MetisProcessBinding(bin_path, pathFormatDotGraphDefault, numPart);
		outputPartDefault = m.partitioning();

		totalTime = System.currentTimeMillis() - startTime;

		timeAlg = id + ";" + graphName + ";" + totalTime + ";metis;";
		out.write(timeAlg.getBytes());

		calculateMetrics(outputPartDefault, pathFormatEdgelistDefault, out);

	}

	private static void runMetisRelaxed(String bin_path) throws IOException, InterruptedException {

		startTime = System.currentTimeMillis();

		MetisRelaxedProcessBinding mr = new MetisRelaxedProcessBinding(bin_path, pathFormatDotGraphDefault, numPart);
		outputPartDefault = mr.partitioning();

		totalTime = System.currentTimeMillis() - startTime;

		timeAlg = id + ";" + graphName + ";" + totalTime + ";metis-relaxed;";
		out.write(timeAlg.getBytes());

		calculateMetrics(outputPartDefault, pathFormatEdgelistDefault, out);

	}

	private static void runKaffpa(String bin_path) throws IOException, InterruptedException {

		startTime = System.currentTimeMillis();

		KaffpaProcessBinding k = new KaffpaProcessBinding(bin_path, pathFormatDotGraphDefault, numPart);
		outputPartDefault = k.partitioning();

		totalTime = System.currentTimeMillis() - startTime;

		timeAlg = id + ";" + graphName + ";" + totalTime + ";kaffpa;";
		out.write(timeAlg.getBytes());

		calculateMetrics(outputPartDefault, pathFormatEdgelistDefault, out);

	}

	private static void runKaffpaE(String bin_path) throws IOException, InterruptedException {

		startTime = System.currentTimeMillis();

		KaffpaEProcessBinding kE = new KaffpaEProcessBinding(bin_path, pathFormatDotGraphDefault, numPart);
		outputPartDefault = kE.partitioning();

		totalTime = System.currentTimeMillis() - startTime;

		timeAlg = id + ";" + graphName + ";" + totalTime + ";kaffpaE;";
		out.write(timeAlg.getBytes());

		calculateMetrics(outputPartDefault, pathFormatEdgelistDefault, out);

	}

	private static void runRandom() throws IOException, InterruptedException {

		startTime = System.currentTimeMillis();

		Random s = new Random(pathFormatDotGraphDefault, numPart);
		outputPartDefault = s.partitioning();

		totalTime = System.currentTimeMillis() - startTime;

		timeAlg = id + ";" + graphName + ";" + totalTime + ";random;";
		out.write(timeAlg.getBytes());

		calculateMetrics(outputPartDefault, pathFormatEdgelistDefault, out);

	}

	/*
	 * this method calculates a super graph metrics and writes the results
	 */
	private static void calculateMetrics(String partitionFile, String originalFile, BufferedOutputStream out) {
		/* generates super graph */
		Graph superGraph = Graph.generateSupergraph(partitionFile, originalFile, vertex_names, numPart);

		/* creates the Metrics object and calculates its metrics */
		Metrics metric = new Metrics(superGraph);

		try {
			String m = metric.toCSVString() + "\n";
			out.write(m.getBytes());
			out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static long startTime, totalTime;
	private static int numPart;
	private static String timeAlg, outputPartDefault;
	private static String pathGraphName, id, graphName, graph_path, format;
	private static String pathFormatEdgelistDefault, pathFormatDotGraphDefault;
	private static BufferedOutputStream out;
	private static Integer[] vertex_names;
	private static final String DIRGRAPHTEST = System.getProperty("user.dir") + File.separator + "testgraph";
	private static final String JABEJA_OUTPUT = System.getProperty("user.dir") + File.separator + "jabeja-output";
}
