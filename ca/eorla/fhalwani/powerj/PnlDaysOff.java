package ca.eorla.fhalwani.powerj;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

import ca.eorla.fhalwani.powerj.PnlShifts.ClassShift;

public class PnlDaysOff extends PnlMain {
	final byte STATUS_PENDING = 1;
	final byte STATUS_REJECTED = 2;
	final byte STATUS_APPROVED = 3;
	private JLabel lblNoDays;
	private CboReasons cboReasons = null;
	private CboStatus cboStatus = null;
	private ClassData data = new ClassData();

	public PnlDaysOff(PowerJ parent) {
		super(parent);
		setName("DaysOff");
		parent.dbPowerJ.prepareDaysOff();
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
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setLayout(new GridBagLayout());
		setOpaque(true);
		Calendar calMin = Calendar.getInstance();
		calMin.add(Calendar.WEEK_OF_YEAR, 2);
		calMin.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		calMin.set(Calendar.HOUR_OF_DAY, 0);
		calMin.set(Calendar.MINUTE, 0);
		calMin.set(Calendar.SECOND, 0);
		calMin.set(Calendar.MILLISECOND, 0);
		data.dateFrom.setTime(calMin.getTimeInMillis());
		data.noDays = Byte.MAX_VALUE;
		Calendar calMax = Calendar.getInstance();
		calMax.setTimeInMillis(calMin.getTimeInMillis());
		calMax.add(Calendar.DAY_OF_YEAR, data.noDays);
		data.dateFrom.setTime(calMax.getTimeInMillis());
		Calendar calStart = Calendar.getInstance();
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		CboDate cboStart = new CboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		cboStart.addAncestorListener(new RequestFocusListener());
		cboStart.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Calendar cal = cboStart.getValue();
					setDates(1, cal);
				}
			}

		});
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("From:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(cboStart);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		Utilities.addComponent(cboStart, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		CboDate cboEnd = new CboDate(calEnd, calMin, calMax);
		cboEnd.setName("cboEnd");
		cboEnd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Calendar cal = cboEnd.getValue();
					setDates(1, cal);
				}
			}
		});
		label = new JLabel("To:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_T);
		label.setLabelFor(cboEnd);
		Utilities.addComponent(label, 2, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		Utilities.addComponent(cboEnd, 3, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		lblNoDays.setFont(Constants.APP_FONT);
		lblNoDays.setBorder(border);
		lblNoDays.setText("0 days ");
		Utilities.addComponent(lblNoDays, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		label = new JLabel("Reason:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_R);
		label.setLabelFor(cboReasons);
		Utilities.addComponent(label, 0, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		cboReasons = new CboReasons(parent);
		cboReasons.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			data.reasonID = cboReasons.getIndex();
						setAltered();
		    		}
				}
	        }
	    });
		Utilities.addComponent(cboReasons, 1, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
	}

	private void setAltered() {
		if (!altered) {
			altered = true;
		}
	}

	private void setDates(int alteredDate, Calendar newDate) {
		if (alteredDate == 1) {
			data.dateFrom.setTime(newDate.getTimeInMillis());
			data.alteredFrom = true;
		} else {
			data.dateTo.setTime(newDate.getTimeInMillis());
			data.alteredTo = true;
		}
		altered = (data.alteredFrom && data.alteredTo
				&& data.dateFrom.getTime() <= data.dateTo.getTime());
		if (altered) {
			// Match working dates
			altered = false;
			data.alteredFrom = false;
			data.alteredTo = false;
			data.noDays = 0;
			String message = "";
			Date endDate = new Date(data.dateTo.getTime());
			PreparedStatement stm = null;
			ResultSet rst = null;
			try {
				stm = parent.dbPowerJ.getStatement(0);
				stm.setDate(1, new java.sql.Date(data.dateFrom.getTime()));
				stm.setDate(2, new java.sql.Date(data.dateTo.getTime()));
				rst = parent.dbPowerJ.getResultSet(stm);
				while (rst.next()) {
					if (!data.alteredFrom) {
						if (data.dateFrom.getTime() <= rst.getDate("WODATE").getTime()) {
							data.dateFrom.setTime(rst.getDate("WODATE").getTime());
							data.fromID = rst.getInt("ID");
							data.alteredFrom = true;
						}
					}
					if (data.alteredFrom) {
						data.noDays++;
						if (endDate.getTime() <= rst.getDate("WODATE").getTime()) {
							data.dateTo.setTime(rst.getDate("WODATE").getTime());
							data.toID = rst.getInt("ID");
							data.alteredTo = true;
						} else {
							break;
						}
					}
				}
				parent.dbPowerJ.closeRst(rst);
				if (!data.alteredFrom) {
					message = "Invalid start date!";
				} else if (!data.alteredTo) {
					message = "Invalid start date!";
				} else {
					altered = true;
				}
				if (altered) {
					// No overlap with other dates off
					stm = parent.dbPowerJ.getStatement(1);
					stm.setShort(1, data.staffID);
					stm.setByte(2, STATUS_REJECTED);
					stm.setDate(3, new java.sql.Date(data.dateFrom.getTime()));
					stm.setDate(4, new java.sql.Date(data.dateTo.getTime()));
					stm.setDate(5, new java.sql.Date(data.dateFrom.getTime()));
					stm.setDate(6, new java.sql.Date(data.dateFrom.getTime()));
					rst = parent.dbPowerJ.getResultSet(stm);
					while (rst.next()) {
						message = "The dates overlap with a " +
								rst.getString("DESCR") + " request on " +
								rst.getDate("FROMD") + " to " + rst.getDate("TOD");
						altered = false;
					}
				}
				if (altered) {
					lblNoDays.setText(numbers.formatNumber(data.noDays) + " days");
				} else {
					new DlgMessage(parent, JOptionPane.INFORMATION_MESSAGE, getName(),
							message);
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				parent.dbPowerJ.closeRst(rst);
			}
		}
	}

	private class ClassData {
		boolean newRow = false;
		boolean alteredFrom = false;
		boolean alteredTo = false;
		byte status = 0;
		int reasonID = 0;`
		byte noDays = 0;
		short staffID = 0;
		int id = 0;
		int fromID = 0;
		int toID = 0;
		Date dateFrom = new Date(0);
		Date dateTo = new Date(0);
		String commentStaff = "";
		String commentAdmin = "";
	}
}
