package it.isislab.dmason.experimentals.systemmanagement.console;

import it.isislab.dmason.experimentals.systemmanagement.master.MasterServer;

public interface Prompt {
	public Object exec(final Console c, final String[] params, String stringPrompt, MasterServer ms, final PromptListener l);
	
}
