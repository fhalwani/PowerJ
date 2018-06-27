package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

class DlgLogin extends JDialog implements ActionListener, FocusListener, KeyListener {
	private static final long serialVersionUID = -2399727982880231472L;
	boolean cancel = false;
	private JPasswordField txtPassword = new JPasswordField(15);
	PowerJ parent;

	DlgLogin(PowerJ parent) {
		super(parent, true);
		this.parent = (PowerJ) parent;
		parent.variables.apLogin = System.getProperty("user.name");
		createDialog();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Cancel")) {
			cancel = true;
			setVisible(false);
		} else if (e.getActionCommand().equals("Okay")) {
			if (parent.variables.apLogin.length() > 2
					&& parent.variables.apPassword.length() > 2) {
				cancel = false;
				setVisible(false);
			}
		}
	}

	/** Create and set up the GUI window **/
	private void createDialog() {
		setName("Login");
		// Make it modal
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		// The calling frame must dispose of window
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		// Remove all window decoration & buttons
		setUndecorated(true);
		setFocusable(true);
		// default BorderLayout used
		setLayout(new BorderLayout());
		// Create a panel within JDialog to manage layout better
		add(createPanel(), BorderLayout.CENTER);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocationRelativeTo(null);	// for double monitors
		setLocation((screenSize.width / 2) - (this.getWidth() / 2),
				(screenSize.height / 2) - (this.getHeight() / 2));
		setVisible(true);
	}

	/** Create and set up the GUI window **/
	private JPanel createPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		// Display image
		ImageIcon icon = Utilities.getIcon("eorla");
		JLabel lblImage = new JLabel(icon);
		Utilities.addComponent(lblImage, 0, 0, 1, 3, 0.5, 0.33,
				GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, panel);
		JLabel lblLicence = new JLabel("<html><H1><B><I>" + Constants.LAB_NAME + "</I></B></H1></html>");
		lblLicence.setHorizontalAlignment(SwingConstants.CENTER);
		Utilities.addComponent(lblLicence, 1, 0, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblAppName = new JLabel("<html><H2><B><I>" + Constants.APP_NAME + "</I></B></H2></html>");
		lblAppName.setHorizontalAlignment(SwingConstants.CENTER);
		Utilities.addComponent(lblAppName, 1, 1, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblVersion = new JLabel("<html><H4><B><I>" + Constants.APP_VERSION + "</I></B></H4></html>");
		lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
		Utilities.addComponent(lblVersion, 1, 2, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		// Add a horizontal line
		Utilities.addComponent(new JSeparator(SwingConstants.HORIZONTAL), 0, 3, 2, 1, 1, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, panel);
		JLabel lblName = new JLabel("Name: ");
		lblName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblName.setDisplayedMnemonic(KeyEvent.VK_N);
		Utilities.addComponent(lblName, 0, 4, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JTextField txtName = new JTextField(15);
		lblName.setLabelFor(txtName);
		txtName.setName("Name");
		txtName.setText(parent.variables.apLogin);
		txtName.addFocusListener(this);
		// Escape closes form
		txtName.addKeyListener(this);
		Utilities.addComponent(txtName, 1, 4, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		JLabel lblPassword = new JLabel("Password: ");
		lblPassword.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPassword.setDisplayedMnemonic(KeyEvent.VK_P);
		Utilities.addComponent(lblPassword, 0, 5, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		lblPassword.setLabelFor(txtPassword);
		txtPassword.setName("Password");
		txtPassword.addFocusListener(this);
		// Escape closes form
		txtPassword.addKeyListener(this);
		// No copy/paste
		txtPassword.setDragEnabled(false);
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		txtPassword.addAncestorListener(new RequestFocusListener());
		Utilities.addComponent(txtPassword, 1, 5, 1, 1, 0.5, 0.1, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		JButton btnOkay = new JButton("OK");
		btnOkay.setMnemonic(KeyEvent.VK_O);
		btnOkay.setIcon(Utilities.getIcon("ok"));
		btnOkay.setActionCommand("Okay");
		btnOkay.setFocusable(true);
		btnOkay.addActionListener(this);
		Utilities.addComponent(btnOkay, 0, 6, 1, 1, 0.5, 0.1, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setMnemonic(KeyEvent.VK_C);
		btnCancel.setIcon(Utilities.getIcon("Cancel"));
		btnCancel.setActionCommand("Cancel");
		btnCancel.setFocusable(true);
		btnCancel.addActionListener(this);
		Utilities.addComponent(btnCancel, 1, 6, 1, 1, 0.5, 0.1,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		return panel;
	}

	public void focusGained(FocusEvent e) {
		// Highlight text
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent) c).setSelectionStart(0);
				((JTextComponent) c).setSelectionEnd(
						((JTextComponent) c).getText().length());
			}
		}
	}

	public void focusLost(FocusEvent e) {
		// De-highlight text
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent) c).setSelectionStart(0);
				((JTextComponent) c).setSelectionEnd(0);
				if (c instanceof JPasswordField) {
					char[] input = ((JPasswordField) c).getPassword();
					parent.variables.apPassword = String.copyValueOf(input);
				} else {
					parent.variables.apLogin = ((JTextComponent) c).getText();
				}
			}
		}
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyChar() == KeyEvent.VK_ENTER) {
			Component c = e.getComponent();
			if (c instanceof JPasswordField) {
				char[] input = ((JPasswordField) c).getPassword();
				parent.variables.apPassword = String.copyValueOf(input);
			} else {
				parent.variables.apLogin = ((JTextComponent) c).getText();
			}
			if (parent.variables.apLogin.length() > 2
					&& parent.variables.apPassword.length() > 2) {
				cancel = false;
				setVisible(false);
			}
		} else if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			// Escape closes form
			parent.variables.apLogin = "";
			parent.variables.apPassword = "";
			cancel = true;
			setVisible(false);
		}
	}
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}

	/** Check login access to APIS & set userID value **/
	boolean validateLogin() {
		boolean success = false;
		DbAPIS dbAP = new DbAPIS(parent);
		if (dbAP.connected) {
			ResultSet rst = null;
			try {
				rst = dbAP.getLogin(parent.variables.apLogin);
				while (rst.next()) {
					parent.variables.userID = rst.getShort("id");
					success = true;
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, "Login", e);
			} finally {
				dbAP.closeRst(rst);
				dbAP.closeStm();
				dbAP.close();
			}
		}
		return success;
	}
}
