package ca.eorla.fhalwani.powerj;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

class JDateField extends JFormattedTextField {
	private static final long serialVersionUID = 2839818883703526057L;
	private boolean altered = false;
    private Date value = null;
	private final static DateFormat format = new SimpleDateFormat("dd/MM/yy");
    private DateVerifier verifier = new DateVerifier();

	JDateField() {
    	super(format);
    	setColumns(11);
    	setFont(Constants.APP_FONT);
    	setInputVerifier(verifier);
    	// Register an action listener to handle Return.
    	addActionListener(verifier);
    	addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				JFormattedTextField source = (JFormattedTextField)e.getSource();
				source.selectAll();
			}

			public void focusLost(FocusEvent e) {
				JFormattedTextField source = (JFormattedTextField)e.getSource();
				source.setCaretPosition(0);
				source.setSelectionStart(0);
				source.setSelectionEnd(0);
			}
    	});
    	getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				altered = true;
			}
			public void removeUpdate(DocumentEvent e) {
				altered = true;
			}
			public void changedUpdate(DocumentEvent e) {
				altered = true;
			}
    	});
	}

	boolean altered() {
		return altered;
	}
	
	public String getText() {
		if (value == null) {
			return "";
		}
		return format.format(value);
	}
	
	public Date getDate() {
		return value;
	}
	
    public void setText(Date date) {
    	this.value = date;
    	super.setValue(date);
    	altered = false;
    }
    
	class DateVerifier extends InputVerifier implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JFormattedTextField source = (JFormattedTextField)e.getSource();
			shouldYieldFocus(source); //ignore return value
		}

        public boolean shouldYieldFocus(JComponent input) {
        	if (!altered) return true;
            boolean inputOK = verify(input);
            if (inputOK) {
                return true;
            } else {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
        }

        /* Checks whether the JComponent's input is valid. 
         * This method should have no side effects. 
         * It returns a boolean indicating the status of the argument's input. 
         */
		public boolean verify(JComponent input) {
			return checkField(input);
		}
    	
		// Checks that the field is valid.
		protected boolean checkField(JComponent input) {
			boolean isValid = true;
			Document doc = ((JTextField)input).getDocument();
			int length = doc.getLength();
			String newValue;
			try {
				newValue = doc.getText(0, length).trim();
				Date newDate = format.parse(newValue);
				value = newDate;
			} catch (ParseException e) {
				isValid = false;
			} catch (BadLocationException e) {
				isValid = false;
			}
			return isValid;
		}
    }
}
