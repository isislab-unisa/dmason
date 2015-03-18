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

import java.util.ArrayList;


public class Round extends ArrayList<Tuple>{

	private static final long serialVersionUID = 1L;
	private int number;
	public Round(int i) {
		this.number=i;
	}
	@Override
	public String toString()
	{
		String toR="<"+number+" size:"+this.size()+">[";
		for(Tuple t:this)
		{
			toR+=" "+t.from+"-"+t.to;
		}
		toR+="]";
		return toR;
	}
	public int getNumber() {
		return number;
	}
	

}
