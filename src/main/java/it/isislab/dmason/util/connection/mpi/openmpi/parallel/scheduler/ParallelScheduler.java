package it.isislab.dmason.util.connection.mpi.openmpi.parallel.scheduler;

/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    @author Carmine Spagnuolo spagnuolocarmine@gmail.com	
    @author Ada Mancuso mancuso.ada@gmial.com
    @author Francesco Milone milone.francesco1988@gmail.com
 */
import it.isislab.dmason.util.connection.mpi.MPITopic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

public final class ParallelScheduler {

	public final static ArrayList<Round> makeSchedule(TreeMap<String, MPITopic> topics)
	{

		ArrayList<Tuple> randomTuples=doRandomSort(topics);
		ArrayList<Round> rounds =new ArrayList<Round>();

		int roundNumber=0;
		for(Tuple t:randomTuples)
		{
			boolean setted=false;

			for(Round r: rounds)
			{
				if(!r.contains(t))
				{
					setted=true;
					r.add(t);
					break;
				}


			}
			if(!setted)
			{
				Round round=new Round(roundNumber++);
				round.add(t);
				rounds.add(round);

			}

		}

		return rounds;
	}

	private final static ArrayList<Tuple> doRandomSort(TreeMap<String, MPITopic> topics)
	{
		Random r=new Random(9999);

		ArrayList<Tuple> toSort=new ArrayList<Tuple>();
		for(String s:topics.keySet())
		{
			MPITopic topic=topics.get(s);
			TreeSet<Integer> froms=topic.getPublisher();
			for(Integer from: froms)
			{
				for(Integer to:topic.getGroup())
				{
					if(from!=to)
					{
						
						Tuple t=new Tuple(r,topic,from,to);
						toSort.add(t);
					}

				}
			}
		}

		Collections.sort(toSort,new Comparator<Tuple>() {

			@Override
			public int compare(Tuple c1,Tuple c2){
				if(c1.randomValue <  c2.randomValue) return -1;
				if(c1.randomValue == c2.randomValue) return 0;
				return 1;
			}

		});

		return toSort;
	}
	public static final void printRounds(ArrayList<Round> rounds)
	{
		System.out.println("------------------------------------\n");
		System.out.print("{#Rounds:"+rounds.size()+"}:\n");
		for(Round r: rounds)
		{
			System.out.print(r+"\n");
		}
		System.out.println("\n------------------------------------\n");

	}
	public static void main(String[] args)
	{

		int N=2;
		TreeMap<String, MPITopic> topics =new TreeMap<String, MPITopic>();
		for (int i = 0; i < N; i++) {
			MPITopic topic=new MPITopic(i+"");
			TreeSet<Integer> group=new TreeSet<Integer>();
			topic.addPublisher(i);
			group.add(i-1<0?N-1:i-1);
			group.add(i+1>N-1?0:i+1);
			topic.setGroup(group);
			topics.put(i+"", topic);
		}



		ArrayList<Round> rounds=ParallelScheduler.makeSchedule(topics);
		printRounds(rounds);

	}

}
