package ca.eorla.fhalwani.powerj;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;

class ChartPie extends JComponent {
	private static final long serialVersionUID = 4896127554101032020L;
	private boolean hasImage = false;
	private String title = "";
	Rectangle pieCoordinates = new Rectangle(0, 0, 1, 1);
	private BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	protected ArrayList<DataPie> list = new ArrayList<DataPie>();
	
	ChartPie() {
		super();
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				paintImage();
			}
		});
	}

	/** Check if mouse was clicked on one of the bars */
	String getMessage(PowerJ parent, int x, int y) {
		String message = "";
		if (hasImage) {
			if (x >= pieCoordinates.x
					&& x <= pieCoordinates.x + pieCoordinates.width) {
				if (y > pieCoordinates.y
						&& y < pieCoordinates.y + pieCoordinates.height) {
					// translate mouse coordinates to pie center
					x = x - (pieCoordinates.x + (pieCoordinates.width/2));
					y = (pieCoordinates.y + (pieCoordinates.height/2)) - y;
					int radius = (int) Math.sqrt(x*x + y*y);
					if (radius <= (pieCoordinates.width/2)
							&& radius <= (pieCoordinates.height/2)) {
			    		int angle = (int) Math.toDegrees(Math.atan2(y,x));
			    		if (angle < 0) {
			    			angle += 360;
			    		}
			    		DataPie slice = new DataPie();
			    		for (int i = 0; i < list.size(); i++) {
							slice = list.get(i);
					    	if (angle >= slice.startAngle
					    			&& angle <= slice.endAngle) {
					    		message = slice.label + ": " + slice.value +
					    				" (" + parent.numbers.formatDouble(2, slice.value2) + "%)";
					    		break;
					    	}
			    		}
					}
				}
			}
		}
		return message;
	}

	/** Display BufferedImage */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
	}
	
	/** Draws BufferedImage */
	void paintImage() {
		if (!hasImage) return;
		boolean labelsHorizontal = true;
		int startAngle = 0, arcAngle = 0, noLabels = 0, labelsOffset = 0;
		double curValue = 0, total = 0;
		Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
		pieCoordinates = new Rectangle(0, 0, getWidth(), getHeight());
		DataPie slice = new DataPie();
		// count totals and exclude blanks
		for (int i = 0; i < list.size(); i++) {
			slice = list.get(i);
			if (slice.value > 0) {
				total += slice.value;
				noLabels++;
			}
		}
		if (total > 0) {
			for (int i = 0; i < list.size(); i++) {
				slice = list.get(i);
				if (slice.value > 0) {
					slice.value2 = (100.00 * slice.value / total);
				}
			}
		}
		// For best results, image must be square and not rectangular
		if (pieCoordinates.width > pieCoordinates.height) {
			labelsHorizontal = false;
			labelsOffset = pieCoordinates.width - pieCoordinates.height;
			pieCoordinates.width = pieCoordinates.height;
			pieCoordinates.x = labelsOffset;
		} else if (pieCoordinates.height > pieCoordinates.width) {
			labelsHorizontal = true;
			labelsOffset = pieCoordinates.height - pieCoordinates.width;
			pieCoordinates.height = pieCoordinates.width;
			pieCoordinates.y = labelsOffset;
		}
		// Now draw the whole image
		image = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, area.width, area.height);
		if (total > 0.1 && noLabels > 0) {
			BufferedImage imgPie = new BufferedImage(pieCoordinates.width, pieCoordinates.height,
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g2dPie = imgPie.createGraphics();
			g2dPie.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2dPie.setColor(Color.WHITE);
			g2dPie.fillRect(0, 0, pieCoordinates.width, pieCoordinates.height);
			// Draw pie chart
			for (int i = 0; i < list.size(); i++) {
				slice = list.get(i);
				if (slice.value > 0) {
					startAngle = (int) (curValue * 360 / total);
					arcAngle = (int) (slice.value * 360 / total);
					g2dPie.setColor(slice.color);
					g2dPie.fillArc(0, 0, pieCoordinates.width, pieCoordinates.height,
							startAngle, arcAngle);
					curValue += slice.value;
					slice.startAngle = startAngle;	// Start angle
					slice.endAngle = startAngle + arcAngle;	// End angle
				}
			}
			g2dPie.dispose();
			if (labelsHorizontal) {
				// Pie on bottom of chart; drawImage(Image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer)
				g2d.drawImage(imgPie, 0, labelsOffset, area.width, area.height,
						0, 0, pieCoordinates.width, pieCoordinates.height, Color.WHITE, null);
			} else {
				// Pie on right side; drawImage(Image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer)
				g2d.drawImage(imgPie, labelsOffset, 0, area.width, area.height,
						0, 0, pieCoordinates.width, pieCoordinates.height,
						Color.WHITE, null);
			}
		}
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_LARGE);
		FontMetrics fm = g2d.getFontMetrics();
		g2d.drawString(title, (area.width - fm.stringWidth(title)) /2, 15);
		g2d.dispose();
		repaint();
	}
	
	void setData(ArrayList<DataPie> data, int statusID) {
		list = data;
		if (statusID > Constants.STATUS_NAMES.length -1) {
			title = "Pending Cases";
		} else {
			title = Constants.STATUS_NAMES[statusID];
		}
		hasImage = true;
		paintImage();
	}

	void setData(ArrayList<DataPie> data, String title) {
		list = data;
		this.title = title;
		hasImage = true;
		paintImage();
	}
}
