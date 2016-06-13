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
package it.isislab.dmason.sim.app.SIRStateReflection;

import it.isislab.dmason.sim.engine.DistributedMultiSchedule;
import it.isislab.dmason.sim.engine.RemoteAgentState;

public interface DHumanState extends RemoteAgentState{
	
	public boolean getIsInfected(DistributedMultiSchedule schedule);
	public void setIsInfected(DistributedMultiSchedule schedule, boolean val);
	public boolean getIsResistent(DistributedMultiSchedule schedule);
	public void setIsResistent(DistributedMultiSchedule schedule, boolean val);
}