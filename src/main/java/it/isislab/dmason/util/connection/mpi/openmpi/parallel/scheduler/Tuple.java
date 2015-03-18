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

import java.util.Random;


public class Tuple{
	public MPITopic topic;
	public int randomValue;
	public int from,to;

	public Tuple(Random r,MPITopic topic,int from,int to)
	{
		randomValue=r.nextInt();
		this.topic=topic;
		this.from=from;
		this.to=to;
	}

	@Override
	public boolean equals(Object t)
	{
		Tuple tuple=(Tuple)t;
		
		return (
				this.from == tuple.from 
				||
				this.from == tuple.to
				||
				this.to == tuple.from
				||
				this.to == tuple.to
				);
	}
	@Override
	public String toString()
	{
		return "["+this.from+"-"+this.to+"]";
	}
}
