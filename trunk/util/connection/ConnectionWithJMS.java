package dmason.util.connection;

import java.util.HashMap;

/**
 * @author Ada Mancuso
 * This interface extends Connection for a purpose : while many programs have to only receive message and
 * callback the program with the result , others more complex applications could might perform different
 * operations depending on the type of message. So extending Connection with this interface developers
 * can set a different listener for any topic.
 *
 */
public interface ConnectionWithJMS extends Connection{
	
	/** Allow client to to receive in asynchronous way messages and to customize listeners for every topic. */
	public boolean asynchronousReceive(String arg0,MyMessageListener arg1);
	
	public void setTable(HashMap table);
}
