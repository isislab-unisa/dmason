package it.isislab.dmason.util.connection;

import java.io.Serializable;


public final class ConnectionType implements Serializable{

		public static final int fakeUnitTestJMS = -1;
	
		public static final int pureMPIBcast = 1;
		public static final int pureMPIGather = 2;
		public static final int pureMPIParallel = 3; 
		public static final int pureMPIMultipleThreads = 4; 
		
		public static final int pureActiveMQ = 0;
		
		public static final int hybridActiveMQMPIBcast = -1;
		public static final int hybridActiveMQMPIGather = -2;
		public static final int hybridActiveMQMPIParallel = -3;
		public static final int hybridMPIMultipleThreads = -4;
		
		private static int ConnectionType = pureMPIParallel;
	
}
