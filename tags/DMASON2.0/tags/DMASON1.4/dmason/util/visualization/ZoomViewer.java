package dmason.util.visualization;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import sim.util.Double2D;
import sim.util.Int2D;
import dmason.util.connection.ConnectionNFieldsWithActiveMQAPI;

public class ZoomViewer {
	
	public HashMap<String,Object> fields=new HashMap<String, Object>();
	
	private ReentrantLock lock2=new ReentrantLock();
	private Condition sin2=lock2.newCondition();
	
	ThreadZoomCellMessageListener  t_zoom;
	public Long STEP=null;
	public String id_cell;
	public boolean isSynchro = false;
	public int numCell;
	public int width;
	public int height;
	public int mode;
	private int local_x;
	private int local_y;
	ConnectionNFieldsWithActiveMQAPI con;
	public UpdateMap update=new UpdateMap();
	
	public ZoomViewer(ConnectionNFieldsWithActiveMQAPI conn,String id_cell, Boolean isSynchro,
			int numCell, int width, int height, int mode) throws Exception
	{
		
		this.con=conn;
		this.id_cell=id_cell;
		this.isSynchro = isSynchro;
		this.numCell = numCell;
		this.width = width;
		this.height = height;
		this.mode = mode;
		
		setZoomCoordinate();
		
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
	
	public Object getZoomAgentLocation(Object location)
	{
		if(location instanceof Int2D)
		{
			Int2D loc = (Int2D)location;
			return new Int2D((loc.getX()-local_x), (loc.getY()-local_y));
		}
		else
			if(location instanceof Double2D)
			{
				Double2D loc = (Double2D)location;
				return new Double2D((loc.getX()-local_x), (loc.getY()-local_y));
			}
		return null;
	}
	
	public static int getCellWidth(int md,Object width,int nC)
	{
		int wh = (Integer)width;
		
		if(md==1)
			return (int)(wh/Math.sqrt(nC));
		else
			return (int)(wh/nC);
	}
	
	public static int getCellHeight(int md,Object height,int nC)
	{
		int hh = (Integer)height;
		
		if(md==1)
			return (int)(hh/Math.sqrt(nC));
		else
			return hh;
	}
	
	public static Int2D getCellUpLeftCoordinates(int md, String id, int nC, Object wh, Object ht) 
	{	
		String[] id_split = id.split("-");
		int _i = Integer.parseInt(id_split[0]);
		int _j  = Integer.parseInt(id_split[1]);
		int w = (Integer)wh;
		int h = (Integer)ht;
		
		if(md==1)
		{
			for (int i = 0; i < Math.sqrt(nC); i++) {
				for (int j = 0; j < Math.sqrt(nC); j++) {
					if(i==_i && j==_j)
					{
						return new Int2D(((int)(w/Math.sqrt(nC)))*j, ((int)(h/Math.sqrt(nC)))*i);
					}
				}
			}
		}
		else
		{
			for (int j = 0; j < nC; j++) {
				if(j==_j)
				{
					return new Int2D((((int)(w/nC))*j),0);
				}
			}
		
		}
		return null;
	}
	
	public static Int2D getCellDownRightCoordinates(int md, String id, int nC, Object wh, Object ht) 
	{	
		String[] id_split = id.split("-");
		int _i = Integer.parseInt(id_split[0]);
		int _j  = Integer.parseInt(id_split[1]);
		int w = (Integer)wh;
		int h = (Integer)ht;
		
		if(md==1)
		{
			int _width = (int)(w/Math.sqrt(nC));
			int _height = (int)(h/Math.sqrt(nC));
			
			for (int i = 0; i < Math.sqrt(nC); i++) {
				for (int j = 0; j < Math.sqrt(nC); j++) {
					if(i==_i && j==_j)
					{
						return new Int2D((((int)(w/Math.sqrt(nC)))*j)+_width,
								(((int)(h/Math.sqrt(nC)))*i)+_height);
					}
				}
			}
		}
		else
		{
			int _width = (int)(w/Math.sqrt(nC));
			for (int j = 0; j < nC; j++) {
				if(j==_j)
				{
					return new Int2D((((int)(w/Math.sqrt(nC)))*j)+_width, h);
				}
			}
		
		}
		return null;
	}
	
	private void setZoomCoordinate()
	{	
		String[] id = id_cell.split("-");
		int x = Integer.parseInt(id[0]);
		int y  = Integer.parseInt(id[1]);
		
		if(mode==1)
		{
			for (int i = 0; i < Math.sqrt(numCell); i++) {
				for (int j = 0; j < Math.sqrt(numCell); j++) {
					if(i==x && j==y)
					{
						local_y = ((height)*i);
						local_x = ((width)*j);
						break;
					}
				}
			}
		}
		else
		{
			for (int j = 0; j < numCell; j++) {
				if(j==y)
				{
					local_x = ((width)*j);
					break;
				}
			}
			local_y = 0;
		}

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
