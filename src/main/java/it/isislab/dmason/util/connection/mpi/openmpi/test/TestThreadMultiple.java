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
    @author Carmine Spagnuolo spagnuolocarmine@gmail.com	
    @author Ada Mancuso mancuso.ada@gmial.com
    @author Francesco Milone milone.francesco1988@gmail.com
 */
package it.isislab.dmason.util.connection.mpi.openmpi.test;


import mpi.MPI;
import mpi.MPIException;

public class TestThreadMultiple extends Thread{
	public TestThreadMultiple()
	{
		try {
			for (int i = 0; i < MPI.COMM_WORLD.getSize(); i++) {
				if(i!=MPI.COMM_WORLD.getRank())
				{
					Receiver r=new Receiver(i);
					r.start();
				}

			}
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		try {
			MPI.InitThread(args, MPI.THREAD_MULTIPLE);
			
				new TestThreadMultiple().start();

			

		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run()
	{
		try {
			MPI.COMM_WORLD.barrier();
			Thread.sleep(1000);
			while(true)
			{

				int send[]=new int[1];
				send[0]=MPI.COMM_WORLD.getRank();
				MPI.COMM_WORLD.bcast(send, 0,MPI.INT,MPI.COMM_WORLD.getRank());

				MPI.COMM_WORLD.barrier();
				Thread.sleep(100);

			}
		} catch (MPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	class Receiver extends Thread{
		int torcv;
		public Receiver(int r) {
			// TODO Auto-generated constructor stub
			torcv=r;
		}
		public void run()
		{
			try {
				System.out.println("Start Receiver for thread "+torcv+" from MPI proc "+MPI.COMM_WORLD.getRank());
				while(true)
				{
					int received[]=new int[1];
					MPI.COMM_WORLD.bcast(received, 0,MPI.INT,torcv);
					System.out.println("Received "+received[0]+ " from MPI proc "+torcv+" for MPI proc "+MPI.COMM_WORLD.getRank());

				}
			} catch (MPIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
