package ca.eorla.fhalwani.powerj;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

class Statusbar extends JPanel {
	private static final long serialVersionUID = -1392152486932897643L;
	private JLabel lblMessage;
	private JProgressBar prgBar;

	Statusbar() {
		super();
		createPanel();
	}
	
	private void createPanel() {
		GridBagLayout layout = new GridBagLayout();
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		setLayout(layout);
		setBorder(border);
		lblMessage = new JLabel("           ");
		lblMessage.setFont(Constants.APP_FONT);
		lblMessage.setBorder(border);
		prgBar = new JProgressBar(0, 100);
		prgBar.setBorder(border);
		prgBar.setVisible(false);
		prgBar.setValue(0);
		Utilities.addComponent(lblMessage, 0, 0, 1, 1, 0.7, 0,
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
		Utilities.addComponent(prgBar, 1, 0, 1, 1, 0.3, 0, 
			GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, this);
	}

	void clear() {
		lblMessage.setText("  ");
		prgBar.setVisible(false);
	}

	void setMessage(String message) {
		lblMessage.setText(" " + message + " ");
	}

	/** If value = 0, start progress bar. If value >= 100, stop it */
	void setProgress(int value) {
		if (value < 0) {
			prgBar.setVisible(true);
			prgBar.setIndeterminate(true);
		} else if (value < 100) {
			prgBar.setIndeterminate(false);
			prgBar.setValue(value);
		} else {
			prgBar.setValue(0);
			prgBar.setVisible(false);
		}
	}
}
