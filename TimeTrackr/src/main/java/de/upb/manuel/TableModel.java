package de.upb.manuel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

public class TableModel extends AbstractTableModel
{
	/**
	 * 
	 */
	private static final long				serialVersionUID	= -7426082161207665014L;

	private final ImageIcon					deleteIcon			= new ImageIcon(
																		getClass().getClassLoader()
																				.getResource("delete.png"));

	private List<TableRowData>				original;

	private List<TableRowData>				workingCopy			= new ArrayList<>();
	private Map<TableRowData, TableRowData>	changes				= new HashMap<>();
	private List<TableRowData>				added				= new ArrayList<>();
	private List<TableRowData>				deleted				= new ArrayList<>();

	public TableModel(List<TableRowData> original)
	{
		this.original = original;
		workingCopy.addAll(original);
	}

	@Override
	public int getRowCount()
	{
		return workingCopy.size();
	}

	String[] headers = { "Date", "Hours", "Description", "" };

	@Override
	public int getColumnCount()
	{
		return headers.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return headers[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex < headers.length - 1;
	}

	public void addNewRow()
	{
		TableRowData newRow = new TableRowData(LocalDate.now(), 0, "");
		added.add(newRow);
		workingCopy.add(newRow);
		fireTableDataChanged();
	}

	public TableRowData removeDefect(int index)
	{
		TableRowData delete = workingCopy.remove(index);
		deleted.add(delete);
		return delete;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		TableRowData original = workingCopy.get(rowIndex);
		TableRowData changeCopy;
		if (!changes.containsKey(original))
		{
			changeCopy = original.clone();
			changes.put(original, changeCopy);
		}
		changeCopy = changes.get(original);

		switch (columnIndex) {
		case 0:
			changeCopy.setDate(aValue.toString());
			break;
		case 1:
			changeCopy.setDuration(aValue.toString());
			break;
		case 2:
			changeCopy.setDescription(aValue.toString());
			break;
		}
		fireTableDataChanged();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		TableRowData row = getRow(rowIndex);
		if (changes.containsKey(row))
		{
			row = changes.get(row);
		}
		switch (columnIndex) {
		case 0:
			return Main.dateFormatter.format(row.getDate());
		case 1:
			return row.getDurationAsString();
		case 2:
			return row.getDescription();
		case 3:
			return deleteIcon;
		default:
			return "";
		}
	}

	@Override
	public java.lang.Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex) {
		case 3:
			return ImageIcon.class;
		default:
			return String.class;
		}
	}

	public void commitChanges()
	{
		for (Entry<TableRowData, TableRowData> change : changes.entrySet())
		{
			TableRowData org = change.getKey();
			TableRowData changed = change.getValue();
			org.setDate(changed.getDate());
			org.setDuration(changed.getDuration());
			org.setDescription(changed.getDescription());
		}
		for (TableRowData del : deleted)
		{
			original.remove(del);
		}
		for (TableRowData add : added)
		{
			original.add(add);
		}
		Collections.sort(original);
		clearChanges();
	}

	private void clearChanges()
	{
		changes.clear();
		added.clear();
		deleted.clear();
	}

	public TableRowData getRow(int index)
	{
		return workingCopy.get(index);
	}

	public void set(int i, TableRowData data)
	{
		changes.put(workingCopy.set(i, data), data);

	}

	public void add(TableRowData data)
	{
		workingCopy.add(data);
		added.add(data);
	}
}
