package it.isislab.dmason.util.management.worker.thrower;


public class TestWorker {
	public static void main(String[] args)
	{
		DMasonWorkerWithGui worker=DMasonWorkerWithGui.newInstance(new String[]{});
		worker.setVisible(true);
	}
}
