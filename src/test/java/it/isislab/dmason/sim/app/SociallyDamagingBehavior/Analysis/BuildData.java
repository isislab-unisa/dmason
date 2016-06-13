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
package it.isislab.dmason.sim.app.SociallyDamagingBehavior.Analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class BuildData {
	private File[] listFile;
	private File tempFile;
	private File percentFile;
	private FileReader[] listFileReader;
	private BufferedReader[] listReader;
	private PrintStream tempWriter;
	private PrintStream percentWriter;
	private long numStep = 0;
	private int numHonestAgent = 0;
	private int numDishonestAgent = 0;
	private int numHonestAction = 0;
	private int numDishonestAction = 0;
	private Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private XYSeries numHonest;
	private XYSeries numDishonest;
	private XYSeries numHAction;
	private XYSeries numDAction;
	private XYSeriesCollection datasetAgents;
	private XYSeriesCollection datasetActions;
	/*private Properties property = System.getProperties();
	private String fileSeparator = property.getProperty("file.separator");
	private String path = property.getProperty("user.dir");
	private String myPath = path+fileSeparator;*/
	
	public BuildData(File[] list) {
		listFile = list;
		this.listFileReader = new FileReader[listFile.length];
		listReader = new BufferedReader[listFileReader.length];
		numHonest = new XYSeries("Honest Agents");
		numDishonest = new XYSeries("Dishonest Agents");
		numHAction = new XYSeries("Honest Actions");
		numDAction = new XYSeries("Dishonest Actions");
		datasetAgents = new XYSeriesCollection();
		datasetActions = new XYSeriesCollection();
		int i = 0;
		try {
			for (int j = 0; j < listFileReader.length; j++) {
				listFileReader[i] = new FileReader(list[i]);
				listReader[i] = new BufferedReader(listFileReader[i]);
				i++;
			}
			
			
			//tempFile = File.createTempFile("MergedFile", ".tmp");
			tempFile = new File("Output.txt");
			tempWriter = new PrintStream(tempFile);
			percentFile = new File("PercentOutput.txt");
			percentWriter = new PrintStream(percentFile);
		} catch (FileNotFoundException e) {
			System.err.println("Error to found the file");
			e.printStackTrace();
		}
 
		build();
	}
	
	
	private void build(){
		String line = "";
		String[] column = {};
		boolean isEnd = false;
		double percentHAgents = 0.0;
		double percentDAgents = 0.0;
		double percentHAct = 0.0;
		double percentDAct = 0.0;
		System.out.println("Starting....");
		System.out.println("Total number of files: "+listFileReader.length);
		while(true){
			for (int i = 0; i < listFileReader.length; i++) {
				try {
					line = listReader[i].readLine();
				} catch (IOException e) {
					System.err.println("Error to read file "+listFile[i].getName());
					e.printStackTrace();
				}
				if(line == null){
					isEnd = true;
					break;
				}
				column = line.split(";");
				if (i==0)
					numStep = Long.parseLong(column[0]);
				numHonestAgent += Integer.parseInt(column[1]);
				numDishonestAgent += Integer.parseInt(column[2]);
				numHonestAction += Integer.parseInt(column[3]);
				numDishonestAction += Integer.parseInt(column[4]);
			}
			
			if(!isEnd){
				percentHAgents = getPercent(numHonestAgent, numDishonestAgent);
				percentDAgents = 100-percentHAgents;
				numHonest.add(numStep, percentHAgents);
				numDishonest.add(numStep, percentDAgents);
				percentHAct = getPercent(numHonestAction, numDishonestAction);
				percentDAct = 100-percentHAct;
				numHAction.add(numStep, percentHAgents);
				numDAction.add(numStep, percentDAgents);
				tempWriter.println(numStep+";"+numHonestAgent+";"+numDishonestAgent+";"+numHonestAction+";"+numDishonestAction);
				tempWriter.flush();
				percentWriter.println(numStep+";"+percentHAgents+";"+percentDAgents+";"+percentHAct+";"+percentDAct);
				percentWriter.flush();
			}
			else
			{
				tempWriter.close();
				percentWriter.close();
				for (int i = 0; i < listFileReader.length; i++){
					try {
						listFileReader[i].close();
						listReader[i].close();
					} catch (IOException e) {
						System.err.println("Error to closing the files");
						e.printStackTrace();
					}
					
				}
				System.out.println("Output file in :"+tempFile.getAbsolutePath());
				System.out.println("End operation!");
				break;
			}
			numStep = 0;
			numHonestAgent = 0;
			numDishonestAgent = 0;
			numHonestAction = 0;
			numDishonestAction = 0;
		}
		datasetAgents.addSeries(numHonest);
		datasetAgents.addSeries(numDishonest);
		datasetActions.addSeries(numHAction);
		datasetActions.addSeries(numDAction);
	}
	
	private double getPercent(int h, int d){
		int sum = h + d;
		return (100*h)/sum;
	}
	
	public XYSeriesCollection getDatasetAgents() {
		return datasetAgents;
	}


	public XYSeriesCollection getDatasetActions() {
		return datasetActions;
	}


	public static void main(String args[]){
		File[] list = new File[args.length];
		for (int i = 0; i < list.length; i++) {
			list[i] = new File(args[i]);
		}
		BuildData b = new BuildData(list);
	}
}
