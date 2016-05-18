package it.isislab.dmason.experimentals.systemmanagement.console;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public interface Action {

	public Object exec(Console c, String[] params,String stringPrompt,MasterServer ms) throws Exception;
}
