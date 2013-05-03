package dmason.master;

import dmason.util.SystemManagement.PeerStatusInfo;

public interface MasterListener
{
	public void workerInfo(PeerStatusInfo workerInfo);
	
	// What does this function do?
	// Maybe it increases completed jobs in a batch execution
	public void incrementUpdatedWorker();
}
