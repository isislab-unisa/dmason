package dmason.master.model;

import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import dmason.master.Master.WorkerInfoList;
import dmason.util.SystemManagement.PeerStatusInfo;

public class WorkersTable implements TableModel
{
	private final static boolean[] columnEditable = { true,    false,  false,  false,    false,    false        };
	private final static String[]  columnNames    = { "Alias", "Host", "ID",   "Digest", "Status", "Simulation" };
	
	ArrayList<PeerStatusInfo> workers = new ArrayList<PeerStatusInfo>();  
	
	public WorkersTable()
	{
		
	}

	public WorkersTable(WorkerInfoList list)
	{
		for (Entry<String, PeerStatusInfo> entry : list.entrySet())
		{
			workers.add(entry.getValue());
		}
	}

	@Override
	public int getRowCount()
	{
		return workers.size();
	}

	@Override
	public int getColumnCount()
	{
		return WorkersTable.columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return WorkersTable.columnNames[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnEditable[columnIndex];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
		case 0: return workers.get(rowIndex).getAlias();
		case 1: return workers.get(rowIndex).getHostname();
		case 2: return workers.get(rowIndex).getId();
		case 3: return workers.get(rowIndex).getDigest();
		case 4: return workers.get(rowIndex).getStatus();
		case 5: return workers.get(rowIndex).getSimulationName();
		default: return "";
		}

	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if (columnIndex == 0)
		{
			workers.get(rowIndex).setAlias((String)aValue);
		}
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		// TODO Auto-generated method stub
		
	}

	

}
