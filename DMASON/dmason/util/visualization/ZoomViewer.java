package dmason.util.visualization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class ZoomViewer {
	
	public HashMap<String,Object> fields=new HashMap<String, Object>();
	
	private ReentrantLock lock2=new ReentrantLock();
	private Condition sin2=lock2.newCondition();
	
	private ConnectionNFieldsWithActiveMQAPI dstate;
	ThreadZoomCellMessageListener  t_zoom;
	public Long STEP=null;
	public String id_cell;
	public boolean isSynchro = false;
	ConnectionNFieldsWithActiveMQAPI con;
	public UpdateMap update=new UpdateMap();
	
	public ZoomViewer(ConnectionNFieldsWithActiveMQAPI conn,String id_cell, Boolean isSynchro) throws Exception
	{
		
		this.con=conn;
		this.id_cell=id_cell;
		this.isSynchro = isSynchro;
		
		t_zoom=new ThreadZoomCellMessageListener(con, id_cell,this);
		t_zoom.run();
	
		if(this.isSynchro)
			con.publishToTopic("ZOOM_SYNCHRO","GRAPHICS"+id_cell,"GRAPHICS"+id_cell);
		else
			con.publishToTopic("ZOOM","GRAPHICS"+id_cell,"GRAPHICS"+id_cell);
		
		
	
	}
	public void registerField(String key,Object field)
	{
		fields.put(key, field);
	}
	
	public void setInStep()
	{
		lock2.lock();
			sin2.signal();
		lock2.unlock();
	}
	public HashMap<String, Object> synchronizedWithSimulation() throws InterruptedException {
		// TODO Auto-generated method stub
		HashMap<String, Object> hash=null;
		if(STEP==null)
		{
			lock2.lock();
				sin2.await();
			lock2.unlock();
		}
		if(STEP!=null)
		{
			System.out.println("Rikiesta sincro per step "+STEP+" valore hash "+update.size());
			hash=update.getUpdates(STEP);
			System.out.println("Fine sincro per step "+STEP+" valore hash "+update.size());
		
		}
		else
		{
			System.out.println("Problemi nello step");
			System.exit(-1);
		}
		System.out.println("Sincronizzazione completata per step "+STEP+" valore delle hash "+hash.size());
		STEP++;
		return hash;
		
	}

	public class UpdateMap extends HashMap<Long,HashMap<String,Object>> implements Serializable
	{
	    private final ReentrantLock lock;
	    private final Condition block;
	    
	    public UpdateMap()
	    {
	    	lock=new ReentrantLock();
	    	block=lock.newCondition();
	    }
	    
		public HashMap<String,Object> getUpdates(long step) throws InterruptedException
		{
			lock.lock();
			HashMap<String,Object> tmp=this.get(step);

			while(tmp==null)
			{
				System.out.println("Round Wait per step "+step);
				block.await();
				tmp=this.get(step);
			}
			this.remove(step);
			System.out.println("Rimozione per step "+step+" dimensione "+this.size());
			
			lock.unlock();
			
			return tmp;
		}
		
		public void putSblock(long step,Object d_reg)
		{		
			System.out.println("PRE PUT");
			lock.lock();
				super.put(step,(HashMap<String,Object>)d_reg);	
				System.out.println("Sblocco per step "+step);
			block.signal();	
			lock.unlock();
			System.out.println("POST PUT");
		}
		
	}

	public void sendAckToCell(long step) throws Exception {
		
		con.publishToTopic("ZOOM_STEP"+step,"GRAPHICS"+id_cell,"GRAPHICS"+id_cell);
	}
}
