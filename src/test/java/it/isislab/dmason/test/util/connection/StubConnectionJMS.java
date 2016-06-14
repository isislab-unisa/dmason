package it.isislab.dmason.test.util.connection;

import java.util.HashMap;

import javax.jms.JMSException;

import it.isislab.dmason.util.connection.jms.ConnectionJMS;
import it.isislab.dmason.util.connection.jms.activemq.MyMessageListener;

public class StubConnectionJMS extends VirtualConnection implements ConnectionJMS{

	/**
	 * Instantiates a new stub connection jms.
	 */
	public StubConnectionJMS() {
		// TODO Auto-generated constructor stub
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.isislab.dmason.util.connection.jms.ConnectionJMS#asynchronousReceive
	 * (java.lang.String,
	 * it.isislab.dmason.util.connection.jms.activemq.MyMessageListener)
	 */
	@Override
	public boolean asynchronousReceive(String arg0, MyMessageListener arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.isislab.dmason.util.connection.jms.ConnectionJMS#setTable(java
	 * .util.HashMap)
	 */
	@Override
	public void setTable(HashMap table) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.isislab.dmason.util.connection.jms.ConnectionJMS#close()
	 */
	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

}
