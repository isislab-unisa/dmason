package it.isislab.dmason.sim.app.DWoims3D;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import it.isislab.dmason.experimentals.systemmanagement.utils.activemq.ActiveMQStarter;
import it.isislab.dmason.experimentals.tools.batch.data.EntryParam;
import it.isislab.dmason.experimentals.tools.batch.data.GeneralParam;

import it.isislab.dmason.sim.engine.DistributedState;
import it.isislab.dmason.sim.field.DistributedField2D;
import it.isislab.dmason.sim.field3D.DistributedField3D;
import it.isislab.dmason.util.connection.ConnectionType;
import sim.display.Console;

public class TestDWoims3D {
    private static boolean graphicsOn=false; //with or without graphics?
    private static int numSteps = 1000;//number of step
    private static int rows = 2; //number of rows
    private static int columns = 2; //number of columns
    private static int lenghts=2;
    private static int AOI=15; //max distance
    private static int NUM_AGENTS=100; //number of agents
    private static int WIDTH=1000; //field width
    private static int HEIGHT=1000; //field height
    private static int LENGHT=1000;
    private static int CONNECTION_TYPE=ConnectionType.pureActiveMQ;
    private static String ip="127.0.0.1"; //ip of activemq
    private static String port="61616"; //port of activemq
    private static String topicPrefix=""; //unique string to identify topics for this simulation



    private static int MODE = DistributedField2D.UNIFORM_PARTITIONING_MODE;

    private static ActiveMQStarter s = new ActiveMQStarter();
    public static void main(String[] args)
    {
        s.startActivemq();
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES","*");
//	try {
//			System.setOut(new PrintStream("/home/matdar/Scrivania/prova_woims_out_1.txt"));
//			//System.setErr(new PrintStream("/home/matdar/Scrivania/prova_woims_err.txt"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        if(args.length>0)
            topicPrefix=args[0];
        else
            topicPrefix="woim";


        class worker extends Thread
        {
            private DistributedState<?> ds;
            public worker(DistributedState<?> ds) {
                this.ds=ds;
                ds.start();
            }
            @Override
            public void run() {
                int i=0;
                while(i!=numSteps)
                {
                    //					if(!graphicsOn){
                    //						if(i==numSteps-1)
                    //						System.out.println("simulation finished");
                    //					}
                    ds.schedule.step(ds);
                    i++;
                }
                System.exit(0);
            }
        }

        ArrayList<worker> myWorker = new ArrayList<worker>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                for(int z=0;z<lenghts;z++){
                    GeneralParam genParam = new GeneralParam(WIDTH, HEIGHT,LENGHT, AOI, rows, columns,lenghts, NUM_AGENTS, MODE, CONNECTION_TYPE,true);
                    genParam.setI(i);
                    genParam.setJ(j);
                    genParam.setZ(z);
                    genParam.setIp(ip);
                    genParam.setPort(port);
                    ArrayList<EntryParam<String, Object>> simParams=new ArrayList<EntryParam<String,Object>>();
                    if(graphicsOn || (i==0 && j==0 && z==0) /*to watch 0-0 celltype*/)
                    {
                        DWoims3DWithUI sim =new DWoims3DWithUI(genParam, simParams, topicPrefix);
                        ((Console)sim.createController()).pressPause();
                    }
                    else
                    {
                        DWoims3D sim = new DWoims3D(genParam, simParams,topicPrefix);
                        worker a = new worker(sim);
                        myWorker.add(a);
                    }
                }
            }
        }
        if(!graphicsOn)
            for (worker w : myWorker) {
                w.start();
            }
    }
}