package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

class PnlShifts extends PnlMain {
	private static final long serialVersionUID = -7900052154659261627L;
	private JStringField txtCode, txtDescr, txtRestrictions;
	private JIntegerField txtNoPositions;
	private JDoubleField txtFTE;
	private JCheckBox chkActive, chkAllweek, chkMonday, chkTuesday;
	private JCheckBox chkWednesday, chkThursday, chkFriday, chkSplit;
	private JCheckBox chkDaily;
	private CboSkills cboSkills;
	private CboSpecialties cboSpecialties;
	private CboSubspecial cboSubspecial;
	private CboPriorities cboPriorities;
	private CboShiftLink cboShiftLink;
	private JTableEditor tblShifts;
	private ArrayList<ClassShift> lstShifts = new ArrayList<ClassShift>();
	private ClassShift thisShift = new ClassShift();
	PnlShifts(PowerJ parent) {
		super(parent);
		setName("Staff");
		parent.dbPowerJ.prepareShifts();
		readTable();
		createPanel();
		programmaticChange = false;
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
			lstShifts.clear();
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		// Layout List panel on left side
		add(createPanelList(), BorderLayout.WEST);
		add(createPanelNames(), BorderLayout.EAST);
		if (lstShifts.size() > 0) {
			tblShifts.setRowSelectionInterval(0,0);
			updateRow(0);
		}
		tblShifts.requestFocusInWindow();
		parent.statusBar.setMessage("No rows " + lstShifts.size());
	}

	private JPanel createPanelList() {
		ModelShifts mdlData = new ModelShifts();
		tblShifts = new JTableEditor(parent, mdlData);
		// detect row selection
		tblShifts.setName("tblShifts");
        tblShifts.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int row = lsm.getMinSelectionIndex();
		        if (row > -1) {
					// else, Selection got filtered away.
					int modelRow = tblShifts.convertRowIndexToModel(row);
					updateRow(modelRow);
		        }
			}
        });
		JScrollPane scrollPane = new JScrollPane(tblShifts,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		scrollPane.setBorder(borderEmpty);
		scrollPane.setPreferredSize(new Dimension(320, 600));
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(scrollPane);
		return panel;
	}

	private JPanel createPanelNames() {
		JPanel panel = new JPanel();
		panel.setName("Details");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Details");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		chkActive = new JCheckBox();
		chkActive.setName("Active");
		chkActive.setText("Active");
		chkActive.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.active = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkActive, 0, 0, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkDaily = new JCheckBox();
		chkDaily.setName("Active");
		chkDaily.setText("Every Day of Week");
		chkDaily.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.daily = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkDaily, 0, 1, 2, 1, 0.2, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkAllweek = new JCheckBox();
		chkAllweek.setName("Allweek");
		chkAllweek.setText("Same staff all week");
		chkAllweek.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.allweek = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkAllweek, 2, 1, 2, 1, 0.2, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkMonday = new JCheckBox();
		chkMonday.setName("Monday");
		chkMonday.setText("Monday");
		chkMonday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.monday = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkMonday, 0, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkTuesday = new JCheckBox();
		chkTuesday.setName("Tuesday");
		chkTuesday.setText("Tuesday");
		chkTuesday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.tuesday = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkTuesday, 1, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkWednesday = new JCheckBox();
		chkWednesday.setName("Wednesday");
		chkWednesday.setText("Wednesday");
		chkWednesday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.wednesday = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkWednesday, 2, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkThursday = new JCheckBox();
		chkThursday.setName("Thursday");
		chkThursday.setText("Thursday");
		chkThursday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.thursday = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkThursday, 3, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkFriday = new JCheckBox();
		chkFriday.setName("Friday");
		chkFriday.setText("Friday");
		chkFriday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.friday = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkFriday, 4, 2, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Code: ");
		label.setDisplayedMnemonic(KeyEvent.VK_C);
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setLabelFor(txtCode);
		Utilities.addComponent(label, 0, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtCode = new JStringField(2, 3);
		txtCode.setName("txtCode");
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		txtCode.addAncestorListener(new RequestFocusListener());
		txtCode.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					if (txtCode.altered()) {
						thisShift.code = txtCode.getText();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtCode, 1, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Restriction: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_R);
		label.setLabelFor(txtRestrictions);
		Utilities.addComponent(label, 2, 3, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtRestrictions = new JStringField(0, 2);
		txtRestrictions.setName("txtRestrictions");
		txtRestrictions.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					if (txtRestrictions.altered()) {
						thisShift.restrictions = txtRestrictions.getText().toUpperCase();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtRestrictions, 3, 3, 3, 1, 0.6, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Description: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_D);
		label.setLabelFor(txtDescr);
		Utilities.addComponent(label, 0, 4, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtDescr = new JStringField(2, 30);
		txtDescr.setName("txtDescr");
		txtDescr.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					if (txtDescr.altered()) {
						thisShift.descr = txtDescr.getText();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtDescr, 1, 4, 3, 1, 0.6, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Skill: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboSkills);
		Utilities.addComponent(label, 0, 5, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		cboSkills = new CboSkills(parent);
		cboSkills.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			CboMain cb = (CboMain)e.getSource();
		    			thisShift.skillID = cb.getIndex();
						setAltered();
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboSkills, 1, 5, 2, 1, 0.3, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Priority: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setLabelFor(cboPriorities);
		Utilities.addComponent(label, 0, 6, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		cboPriorities = new CboPriorities(parent);
		cboPriorities.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			CboMain cb = (CboMain)e.getSource();
		    			thisShift.priority = cb.getIndex();
						setAltered();
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboPriorities, 1, 6, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Link: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_L);
		label.setLabelFor(cboShiftLink);
		Utilities.addComponent(label, 0, 7, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		cboShiftLink = new CboShiftLink(parent);
		cboShiftLink.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			CboMain cb = (CboMain)e.getSource();
		    			thisShift.linkType = cb.getIndex();
						setAltered();
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboShiftLink, 1, 7, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Specialty: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setLabelFor(cboSpecialties);
		Utilities.addComponent(label, 0, 8, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		cboSpecialties = new CboSpecialties(parent, true);
		cboSpecialties.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			CboMain cb = (CboMain)e.getSource();
		    			thisShift.linkID = cb.getIndex();
						setAltered();
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboSpecialties, 1, 8, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("Subspecialty: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_U);
		label.setLabelFor(cboSubspecial);
		Utilities.addComponent(label, 0, 9, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		cboSubspecial = new CboSubspecial(parent, true);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			CboMain cb = (CboMain)e.getSource();
		    			thisShift.linkID = cb.getIndex();
						setAltered();
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboSubspecial, 1, 9, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("FTE: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(txtFTE);
		Utilities.addComponent(label, 0, 10, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtFTE = new JDoubleField(parent, 2, 0.00, 9.9);
		txtFTE.setName("FTE");
		txtFTE.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					if (txtFTE.altered()) {
						thisShift.fte = parent.numbers.toInt(100 * txtFTE.getDouble());
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtFTE, 1, 10, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		label = new JLabel("No. Positions: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_N);
		label.setLabelFor(txtNoPositions);
		Utilities.addComponent(label, 0, 11, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		txtNoPositions = new JIntegerField(parent, 0, 9);
		txtNoPositions.setName("NoPositions");
		txtNoPositions.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					if (txtFTE.altered()) {
						thisShift.noPositions = txtNoPositions.getByte();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtNoPositions, 1, 11, 1, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		chkSplit = new JCheckBox();
		chkSplit.setName("Split");
		chkSplit.setText("Equal split");
		chkSplit.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisShift.split = source.isSelected();
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkSplit, 2, 11, 3, 1, 0.1, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		Utilities.addComponent(Box.createVerticalGlue(), 0, 12, 1, 1, 1, 1, 
				GridBagConstraints.VERTICAL, GridBagConstraints.NORTH, panel);
		return panel;
	}

	private boolean inspectRow() {
		boolean valid = true;
		if (thisShift.descr.length() > 64) {
			thisShift.descr = thisShift.descr.substring(0, 64);
		} else if (thisShift.descr.length() == 0) {
			valid = false;
			new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(), "Invalid shift description.");
		}
		if (thisShift.priority < 1 || thisShift.priority > 4) {
			valid = false;
			new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(), "Invalid priority.");
		}
		if (thisShift.skillID < 1) {
			valid = false;
			new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(), "Invalid skill.");
		}
		if (!(thisShift.daily || thisShift.monday
				|| thisShift.tuesday || thisShift.wednesday
				|| thisShift.thursday || thisShift.friday)) {
			valid = false;
			new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(), "Invalid days of week.");
		}
		return valid;
	}

	private void readTable() {
		short lastID = 1;
		ResultSet rst = parent.dbPowerJ.getShifts();
		try {
			while (rst.next()) {
				thisShift = new ClassShift();
				thisShift.shiftID = rst.getShort("ID");
				thisShift.skillID = rst.getShort("SKID");
				thisShift.fte = rst.getShort("FTE");
				thisShift.linkID = rst.getShort("LNKID");
				thisShift.linkType = rst.getByte("LNKTYPE");
				thisShift.priority = rst.getByte("PRIORITY");
				thisShift.noPositions = rst.getByte("NOPOS");
				thisShift.active = (rst.getString("ACTIVE").equalsIgnoreCase("Y"));
				thisShift.split = (rst.getString("SPLIT").equalsIgnoreCase("Y"));
				thisShift.allweek = (rst.getString("ALLWEEK").equalsIgnoreCase("Y"));
				thisShift.monday = (rst.getString("MONDAY").equalsIgnoreCase("Y"));
				thisShift.tuesday = (rst.getString("TUESDAY").equalsIgnoreCase("Y"));
				thisShift.wednesday = (rst.getString("WEDNESDAY").equalsIgnoreCase("Y"));
				thisShift.thursday = (rst.getString("THURSDAY").equalsIgnoreCase("Y"));
				thisShift.friday = (rst.getString("FRIDAY").equalsIgnoreCase("Y"));
				thisShift.restrictions = rst.getString("SEPARATE").toUpperCase();
				thisShift.code = rst.getString("INITIALS");
				thisShift.descr = rst.getString("DESCR");
				thisShift.daily = (thisShift.monday && thisShift.tuesday && thisShift.wednesday
						&& thisShift.thursday && thisShift.friday);
				lstShifts.add(thisShift);
				if (lastID < thisShift.shiftID) {
					lastID = thisShift.shiftID;
				}
			}
			// Add a blank
			lastID++;
			thisShift = new ClassShift();
			thisShift.shiftID = lastID;
			thisShift.newRow = true;
			lstShifts.add(thisShift);
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
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < lstShifts.size(); i++) {
				thisShift = lstShifts.get(i);
				if (!thisShift.altered) {
					if (txtDescr.altered()) {
						thisShift.altered = true;
						thisShift.descr = txtDescr.getText();
					}
					if (txtCode.altered()) {
						thisShift.altered = true;
						thisShift.code = txtCode.getText();
					}
					if (txtRestrictions.altered()) {
						thisShift.altered = true;
						thisShift.restrictions = txtRestrictions.getText().toUpperCase();
					}
				}
				if (thisShift.altered) {
					if (!inspectRow()) {
						failed = true;
						continue;
					} else if (thisShift.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisShift.skillID);
					stm.setInt(2, thisShift.linkID);
					stm.setInt(3, thisShift.fte);
					stm.setInt(4, thisShift.priority);
					stm.setInt(5, thisShift.linkType);
					stm.setByte(6, thisShift.noPositions);
		            stm.setString(7, (thisShift.active ? "Y": "N"));
		            stm.setString(8, (thisShift.allweek ? "Y": "N"));
		            stm.setString(9, (thisShift.monday ? "Y": "N"));
		            stm.setString(10, (thisShift.tuesday ? "Y": "N"));
		            stm.setString(11, (thisShift.wednesday ? "Y": "N"));
		            stm.setString(12, (thisShift.thursday ? "Y": "N"));
		            stm.setString(13, (thisShift.friday ? "Y": "N"));
		            stm.setString(14, (thisShift.split ? "Y": "N"));
					stm.setString(15, thisShift.restrictions);
					stm.setString(16, thisShift.code);
					stm.setString(17, thisShift.descr);
					stm.setShort(18, thisShift.shiftID);
					noUpdates = stm.executeUpdate();
					if (noUpdates > 0) {
						thisShift.altered = false;
						thisShift.newRow = false;
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

	private void setAltered() {
		if (!thisShift.altered) {
			thisShift.altered = true;
			altered = true;
			if (thisShift.newRow) {
				// Add another new row
				short lastID = thisShift.shiftID;
				lastID++;
				ClassShift anotherShift = new ClassShift();
				anotherShift.newRow = true;
				anotherShift.shiftID = lastID;
				anotherShift.descr = "New";
				lstShifts.add(anotherShift);
				AbstractTableModel mdl = (AbstractTableModel) tblShifts.getModel();
				mdl.fireTableRowsInserted(lstShifts.size()-1, lstShifts.size()-1);
			}
		}
		setView();
	}

	private void setView() {
		if (thisShift.active) {
			cboSkills.setEnabled(true);
			cboPriorities.setEnabled(true);
			cboShiftLink.setEnabled(true);
			cboSpecialties.setEnabled(thisShift.linkType == 2);
			cboSubspecial.setEnabled(thisShift.linkType == 3);
			txtFTE.setEnabled(true);
			txtRestrictions.setEnabled(true);
			txtNoPositions.setEnabled(true);
			chkDaily.setEnabled(true);
			chkAllweek.setEnabled(true);
			chkSplit.setEnabled(true);
			if (thisShift.daily) {
				thisShift.monday = true;
				thisShift.tuesday = true;
				thisShift.wednesday = true;
				thisShift.thursday = true;
				thisShift.friday = true;
				chkMonday.setEnabled(false);
				chkTuesday.setEnabled(false);
				chkWednesday.setEnabled(false);
				chkThursday.setEnabled(false);
				chkFriday.setEnabled(false);
			} else {
				chkMonday.setEnabled(true);
				chkTuesday.setEnabled(true);
				chkWednesday.setEnabled(true);
				chkThursday.setEnabled(true);
				chkFriday.setEnabled(true);
			}
		} else {
			cboSkills.setEnabled(false);
			cboPriorities.setEnabled(false);
			cboShiftLink.setEnabled(false);
			cboSpecialties.setEnabled(false);
			cboSubspecial.setEnabled(false);
			txtRestrictions.setEnabled(false);
			txtFTE.setEnabled(false);
			txtNoPositions.setEnabled(false);
			chkDaily.setEnabled(false);
			chkMonday.setEnabled(false);
			chkTuesday.setEnabled(false);
			chkWednesday.setEnabled(false);
			chkThursday.setEnabled(false);
			chkFriday.setEnabled(false);
			chkAllweek.setEnabled(false);
			chkSplit.setEnabled(false);
		}
	}

	private void updateRow(int row) {
		// Move to desired row
		programmaticChange = true;
		thisShift = lstShifts.get(row);
		txtCode.setText(thisShift.code);
		txtDescr.setText(thisShift.descr);
		txtRestrictions.setText(thisShift.restrictions);
		txtNoPositions.setInt(thisShift.noPositions);
		txtFTE.setDouble(parent.numbers.toDouble(2, thisShift.fte));
		chkActive.setSelected(thisShift.active);
		chkAllweek.setSelected(thisShift.allweek);
		chkDaily.setSelected(thisShift.daily);
		chkMonday.setSelected(thisShift.monday);
		chkTuesday.setSelected(thisShift.tuesday);
		chkWednesday.setSelected(thisShift.wednesday);
		chkThursday.setSelected(thisShift.thursday);
		chkFriday.setSelected(thisShift.friday);
		chkSplit.setSelected(thisShift.split);
		cboSkills.setIndex(thisShift.skillID);
		cboPriorities.setIndex(thisShift.priority);
		cboShiftLink.setIndex(thisShift.linkType);
		cboSpecialties.setIndex(thisShift.linkType == 2 ? thisShift.linkID : -1);
		cboSubspecial.setIndex(thisShift.linkType == 3 ? thisShift.linkID : -1);
		setView();
		programmaticChange = false;
	}

	private class ClassShift {
		boolean altered = false;
		boolean newRow = false;
		boolean active = false;
		boolean allweek = false;
		boolean daily = true;
		boolean monday = true;
		boolean tuesday = true;
		boolean wednesday = true;
		boolean thursday = true;
		boolean friday = true;
		boolean split = false;
		byte noPositions = 0;
		short shiftID = 0;
		int fte = 0;
		int priority = 1;
		int skillID = 0;
		int linkID = 0;
		int linkType = 2;
		String restrictions = "";
		String code = "";
		String descr = "";
	}

	private class ModelShifts extends AbstractTableModel {
		private static final long serialVersionUID = -4039595848605680184L;

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return lstShifts.size();
		}

		public String getColumnName(int col) {
			return "Shifts";
		}

		public Object getValueAt(int row, int col) {
			if (row < lstShifts.size()) {
				ClassShift item = lstShifts.get(row);
				return item.descr;
			}
			return "";
		}

		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			// This table is not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, table not editable
		}
	}
}
