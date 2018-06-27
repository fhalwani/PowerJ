package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class PnlTemplateDash extends PnlMain {
	private static final long serialVersionUID = 3134147065938779093L;
	private final byte DATA_NAME = 0;
	private final byte DATA_DESCR = 1;
	private final byte DATA_GROSS = 2;
	private final byte DATA_EMBED = 3;
	private final byte DATA_MICRO = 4;
	private final byte DATA_ROUTE = 5;
	private final byte DATA_FINAL = 6;
	private int[] filters = {0, 0, 0};
	private ClassData thisRow = new ClassData();
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	private JTableEditor tblData;

	PnlTemplateDash(PowerJ parent) {
		super(parent);
		setName("Templates");
		parent.dbPowerJ.prepareTemplateDash();
		readTable();
		createPanel();
	}

	boolean close() {
		if (altered) {
			int option = parent.askSave(getName());
			switch (option) {
			case Utilities.OPTION_YES:
				save();
				break;
			case Utilities.OPTION_NO:
				altered = false;
				break;
			default:
				// Cancel close
			}
		}
		if (!altered) {
			list.clear();
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		tblData = new JTableEditor(parent, new ModelData());
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JScrollPane scrollPane = new JScrollPane(tblData,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(border);
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8755059126979273544L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(createToolbar(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + list.size());
	}

	private JPanel createToolbar() {
		// Setup 3 JComboBox and fill with their data
		CboSpecialties cboSpecialties = new CboSpecialties(parent, false);
		cboSpecialties.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[0] = item.getValue();
	    			setFilters();
	    		}
	        }
	    });
		CboSubspecial cboSubspecial = new CboSubspecial(parent, false);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[1] = item.getValue();
	    			setFilters();
	    		}
	        }
	    });
		CboProcedures cboProcs = new CboProcedures(parent, false);
		cboProcs.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[2] = item.getValue();
	    			setFilters();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Specialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboSpecialties);
		panel.add(label);
		panel.add(cboSpecialties);
		label = new JLabel("Subspecialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_B);
		label.setLabelFor(cboSubspecial);
		panel.add(label);
		panel.add(cboSubspecial);
		label = new JLabel("Procedure:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setLabelFor(cboProcs);
		panel.add(label);
		panel.add(cboProcs);
		return panel;
	}

	private void readTable() {
		ResultSet rst = parent.dbPowerJ.getMasterSpecimens(0);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("MSID");
				thisRow.name = rst.getString("CODE");
				thisRow.descr = rst.getString("DESCR");
				thisRow.specialty = rst.getShort("SPYID");
				thisRow.subspecialty = rst.getShort("SUBID");
				thisRow.procedure= rst.getShort("PROCID");
				thisRow.grossTime = rst.getShort("GROSS");
				thisRow.embedTime = rst.getShort("EMBED");
				thisRow.microTime = rst.getShort("MICROTOMY");
				thisRow.routeTime = rst.getShort("ROUTE");
				thisRow.finalTime = rst.getShort("SIGNOUT");
				list.add(thisRow);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	void save() {
		boolean failed = false;
        int noUpdates = 0;
		PreparedStatement stm = parent.dbPowerJ.getStatement(0);
		try {
			for (int i = 0; i < list.size(); i++) {
		        thisRow = list.get(i);
		        if (thisRow.altered) {
					stm.setInt(1, thisRow.grossTime);
					stm.setInt(2, thisRow.embedTime);
					stm.setInt(3, thisRow.microTime);
					stm.setInt(4, thisRow.routeTime);
					stm.setInt(5, thisRow.finalTime);
		            stm.setInt(6, thisRow.ID);
					noUpdates = stm.executeUpdate();
		            if (noUpdates > 0) {
			        	thisRow.altered = false;
					} else {
						failed = true;
		            }
		        }
			}
			if (!failed) {
				altered = false;
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private void setFilters() {
		int noFilters = 0;
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		// When multiple Filters are required
		ArrayList<RowFilter<AbstractTableModel, Integer>> lstFilters = new ArrayList<RowFilter<AbstractTableModel, Integer>>();
		if (filters[0] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).specialty == filters[0]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[1] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).subspecialty == filters[1]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (filters[2] > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).procedure == filters[2]);
				}
			};
			lstFilters.add(rowFilter);
			noFilters++;
		}
		if (noFilters > 0) {
			// Add to the compound filter
			lstFilters.add(rowFilter);
			rowFilter = RowFilter.andFilter(lstFilters);
		}
		@SuppressWarnings("unchecked")
		TableRowSorter<ModelData> sorter = (TableRowSorter<ModelData>) tblData.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		// Count filtered rows
		int noCases = list.size();
		if (filters[0] > 0 || filters[1] > 0 || filters[2] > 0) {
			for (int i = 0; i < list.size(); i++) {
				if (filters[0] > 0 && filters[0] != list.get(i).specialty) {
					noCases--;
				} else if (filters[1] > 0 && filters[1] != list.get(i).subspecialty) {
					noCases--;
				} else if (filters[2] > 0 && filters[2] != list.get(i).procedure) {
					noCases--;
				}
			}
		}
		parent.statusBar.setMessage("No rows " + noCases);
	}

	private class ClassData {
		boolean altered = false;
		short ID = 0;
		short specialty = 0;
		short subspecialty = 0;
		short procedure = 0;
		int grossTime = 0;
		int embedTime = 0;
		int microTime = 0;
		int routeTime = 0;
		int finalTime = 0;
		String name = "";
		String descr = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 5578737077113502777L;
		private final String[] columns = {"Code", "Description",
				"Gross", "Embed", "Microtomy", "Route", "Signout"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			try {
				if (row < list.size()) {
					thisRow = list.get(row);
					switch (col) {
					case DATA_NAME:
						value = thisRow.name;
						break;
					case DATA_GROSS:
						value = thisRow.grossTime;
						break;
					case DATA_EMBED:
						value = thisRow.embedTime;
						break;
					case DATA_MICRO:
						value = thisRow.microTime;
						break;
					case DATA_ROUTE:
						value = thisRow.routeTime;
						break;
					case DATA_FINAL:
						value = thisRow.finalTime;
						break;
					default:
						value = thisRow.descr;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_GROSS:
			case DATA_EMBED:
			case DATA_MICRO:
			case DATA_ROUTE:
			case DATA_FINAL:
				return Integer.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return (col > DATA_DESCR);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				if (col > DATA_DESCR) {
					int value2 = (Integer) value;
					if (value2 > 0) {
						if (value2 > Short.MAX_VALUE) {
							value2 = Short.MAX_VALUE;
						}
						altered = true;
						thisRow.altered = true;
						switch (col) {
						case DATA_GROSS:
							thisRow.grossTime = value2;
							break;
						case DATA_EMBED:
							thisRow.embedTime = value2;
							break;
						case DATA_MICRO:
							thisRow.microTime = value2;
							break;
						case DATA_ROUTE:
							thisRow.routeTime = value2;
							break;
						default:
							thisRow.finalTime = value2;
						}
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
