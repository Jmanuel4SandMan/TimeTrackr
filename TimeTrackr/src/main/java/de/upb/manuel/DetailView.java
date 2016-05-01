package de.upb.manuel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

public class DetailView extends JFrame
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 3179189848295334532L;
	private ConfirmationHandler	confirmationHandler;
	private JPanel				buttonPanel;
	private JTable				table;

	public DetailView(List<TableRowData> data, JFrame parent)
	{
		TableModel tableModel = new TableModel(data);
		configureView("Timetable", "Save", tableModel);
		JButton addButton = new JButton("Add Activity");
		addButton.addActionListener(l -> {
			tableModel.addNewRow();
			table.setRowSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
		});
		buttonPanel.add(addButton);
	}

	private void configureView(String title, String confirmButtonText, TableModel tableModel)
	{
		this.setSize(800, 500);
		setLocationRelativeTo(null);
		setTitle(title);

		table = new JTable(tableModel);
		table.putClientProperty("terminateEditOnFocusLost", true);
		JScrollPane tableScroller = new JScrollPane(table);
		this.add(tableScroller);
		setVisible(true);

		TableColumnModel columns = table.getColumnModel();
		columns.getColumn(0).setPreferredWidth(90);
		columns.getColumn(0).setMaxWidth(200);
		columns.getColumn(1).setMaxWidth(50);
		columns.getColumn(2).setPreferredWidth(90);
		columns.getColumn(3).setMaxWidth(18);

		JButton confirmButton = new JButton(confirmButtonText);
		confirmButton.addActionListener(e -> {

			// table.editingStopped(null);
			tableModel.commitChanges();
			if (confirmationHandler != null)
			{
				confirmationHandler.confirmed(tableModel);
			}
			dispose();
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> {
			dispose();
		});
		buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(cancel);
		buttonPanel.add(confirmButton);
		add(buttonPanel, BorderLayout.PAGE_END);

		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (table.getSelectedColumn() == 3)
				{
					int confirmed = JOptionPane.showConfirmDialog(DetailView.this, "Really delete this row?", "Confirm delete",
							JOptionPane.YES_NO_OPTION);
					if (confirmed == 0)
					{
						tableModel.removeDefect(table.getSelectedRow());
						tableModel.fireTableDataChanged();
					}
				}
			}
		});
	}

	public void setConfirmationHandler(ConfirmationHandler confirmation)
	{
		confirmationHandler = confirmation;
	}

	public interface ConfirmationHandler
	{
		void confirmed(TableModel engineer);
	}
}
