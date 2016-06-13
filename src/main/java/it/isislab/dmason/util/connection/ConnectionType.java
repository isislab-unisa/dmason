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
package it.isislab.dmason.util.connection;

import java.io.Serializable;

/**
 * A support class for Connection 
 * 
 *
 */
public final class ConnectionType implements Serializable{

		public static final int fakeUnitTestJMS = -5;
		public static final int pureMPIBcast = 1;
		public static final int pureMPIGather = 2;
		public static final int pureMPIParallel = 3; 
		public static final int pureMPIMultipleThreads = 4; 
		public static final int pureActiveMQ = 0;
		public static final int hybridActiveMQMPIBcast = -1;
		public static final int hybridActiveMQMPIGather = -2;
		public static final int hybridActiveMQMPIParallel = -3;
		public static final int hybridMPIMultipleThreads = -4;
		
		private static int ConnectionType = pureActiveMQ;
	
}
