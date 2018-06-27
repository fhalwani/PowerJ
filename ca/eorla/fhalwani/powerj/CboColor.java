package ca.eorla.fhalwani.powerj;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;

/** Enables a JComboBox to store a string and a color image. **/
class CboColor extends JComboBox {
	private static final long serialVersionUID = -8101318546819823475L;
	protected ArrayList<DataColorCodes> list = new ArrayList<DataColorCodes>();
	
	CboColor() {
		super();
		setName("cboColors");
		setFont(Constants.APP_FONT);
		setEditable(false);
		Dimension dim = new Dimension(120, 24);
		setPreferredSize(dim);
		setMaximumSize(dim);
		getData();
		DefaultComboBoxModel model = new DefaultComboBoxModel(list.toArray());
		setModel(model);
	}

	/** Clears the list. */
	void close() {
		list.clear();
	}
	
	void getData() {
		list.add(new DataColorCodes("* All *", Color.BLACK));
		list.add(new DataColorCodes("Red", Color.RED));
		list.add(new DataColorCodes("Amber", Constants.AMBER));
		list.add(new DataColorCodes("Green", Color.GREEN));
	}

	/** Get the Item ID of a selected JComboBox. */
	int getIndex() {
		int index = getSelectedIndex();
		if (index < 0) {
			index = 0;
		}
		return index;
	}
	
	class DataColorCodes {
		// Enables a JComboBox to store a string and a color image
		private String strName;
		private ImageIcon img;

		DataColorCodes(String Name, Color color) {
			strName = Name;
			setIcon(color);
		}

		String getName() {
			return strName;
		}

		ImageIcon getIcon() {
			return img;
		}

		private void setIcon(Color color) {
			BufferedImage bImg = new BufferedImage(50, 25,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = bImg.createGraphics();
			g2d.setPaint(color);
			g2d.fillRect(0, 0, bImg.getWidth(), bImg.getHeight());
			g2d.dispose();
			img = new ImageIcon(bImg);
		}
		
		public String toString() {
			return strName;
		}
	}
}
