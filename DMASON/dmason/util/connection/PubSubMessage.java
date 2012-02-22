package dmason.util.connection;

import java.io.Serializable;

/** 
 * @author Ada Mancuso
 * Wrapper for a message delivered using Socket implementation of Publish/Subscribe paradigm.
 * The message is three-parts divided :
 * part1 = command like 'publish' and 'subscribe'
 * part2 = topic's name
 * part3 = if we are publishing this field contains the body of the message,an Object, otherwise it's set null.
 *
 */
public class PubSubMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String part1;
	private String part2;
	private Object part3;
	
	/** Costructor using fields. */
	public PubSubMessage(String part1, String part2, Object part3) {
		super();
		this.part1 = part1;
		this.part2 = part2;
		this.part3 = part3;
	}

	/** Return the String identifying the command */
	public String getPart1() {
		return part1;
	}
	
	/** Set the value of part1 */
	public void setPart1(String part1) {
		this.part1 = part1;
	}

	/** Return the topic's name String */
	public String getPart2() {
		return part2;
	}

	/** Set the value of part2 */
	public void setPart2(String part2) {
		this.part2 = part2;
	}

	/** Return the value of part3 */
	public Object getPart3() {
		return part3;
	}

	/** Set the value of part3 */
	public void setPart3(Object part3) {
		this.part3 = part3;
	}
}
