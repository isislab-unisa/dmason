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
package it.isislab.dmason.sim.field.network.kway.graph.tools;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

@AuthorAnnotation(author = { "Alessia Antelmi", "Carmine Spagnuolo" }, date = "20/7/2015")
public class DirectToIndirect {

	/**
	 * Reads a graph described as an edgelist 
	 * and transforms it into an indirect graph
	 * 
	 * @param filepath - edgelist file, direct
	 * @param outFilename - edgelist file, indirect
	 */
	public static void directToIndirect(String filepath, String outFilename) {

		String edge, separator;
		String invEdge;
		String info;
		String[] vertices = new String[2];
		HashMap<String, String> edges = new HashMap<String, String>();
		BufferedReader in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename + ".edgelist"));

			while ((edge = in.readLine()) != null) {

				if (edges.containsKey(edge))
					continue;

				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);

				if (vertices[0].equals(vertices[1]))
					continue;

				invEdge = vertices[1] + separator + vertices[0];

				edges.put(edge, null);

				info = edge + "\n";
				out.write(info.getBytes());

				edges.put(invEdge, null);

				info = invEdge + "\n";
				out.write(info.getBytes());
			}

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

	/**
	 * Reads a graph described as an edgelist 
	 * and transforms it into a direct graph
	 * 
	 * @param filepath - edgelist file, indirect
	 * @param outFilename - edgelist file, direct
	 */
	public static void indirecTodirect(String filepath, String outFilename) {
		String edge, separator;
		String invEdge;
		String info;
		String[] vertices = new String[2];
		HashMap<String, String> edges = new HashMap<String, String>();
		BufferedReader in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(
					new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename
					+ ".edgelist"));

			while ((edge = in.readLine()) != null) {

				if (edges.containsKey(edge))
					continue;

				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);

				invEdge = vertices[1] + separator + vertices[0];

				if (edges.containsKey(invEdge))
					continue;

				edges.put(edge, null);

				info = edge + "\n";
				out.write(info.getBytes());

			}

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

	/**
	 * Reads a graph described as an edgelist 
	 * and removes any self-loop
	 * 
	 * @param filepath - edgelist file to examine
	 * @param outFilename - file describing the result
	 */
	public static void deleteSelfLoops(String filepath, String outFilename) {
		String edge, separator, info;
		String[] vertices = new String[2];
		BufferedReader in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			out = new BufferedOutputStream(new FileOutputStream(outFilename));

			while ((edge = in.readLine()) != null) {

				separator = Utility.findSeparator(edge);
				vertices = edge.split(separator);

				if (vertices[0].equals(vertices[1]))
					continue;

				info = edge + "\n";

				out.write(info.getBytes());
			}

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

}
