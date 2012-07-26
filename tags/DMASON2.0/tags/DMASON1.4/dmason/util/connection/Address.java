package dmason.util.connection;

import java.io.Serializable;

/** Wrapper for network configuration parameters . It provides ip address and port number but 
 * the developer is free to extend the class with additional informations.
 * 
 * @author ada mancuso
 */

public class Address implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String IPaddress;
	private String port;
	
	/**
	 * @param IPaddress
	 * @param port number
	 */
	public Address(String iPaddress, String port) {
		super();
		IPaddress = iPaddress;
		this.port = port;
	}

	/** Return a String containing IPaddress
	 * 
	 * @return String
	 */
	public String getIPaddress() {
		return IPaddress;
	}
	
	/**
	 * Set, given a value, IPAddress.
	 * @param iPaddress
	 */
	public void setIPaddress(String iPaddress) {
		IPaddress = iPaddress;
	}
	
	/**Return a String containing port number
	 * 
	 * @return String
	 */
	public String getPort() {
		return port;
	}

	/** Set, given a value, port number. 
	 * 
	 * @param port
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/** Rewriting of Object toString() method. */
	@Override
	public String toString() {
		return "Address [IPaddress=" + IPaddress + ", port=" + port + "]";
	}
	
	
	
	

}
