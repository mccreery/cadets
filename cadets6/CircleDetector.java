package cadets6;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.Timer;

/** Wrapper for {@link #main(String[])} and a window displaying
 * webcam, sobel and hough visualisations (detecting circles) */
public class CircleDetector extends JFrame implements ActionListener {
	private static final long serialVersionUID = -1479110023557857534L;

	/** Container for both the raw webcam footage and the filtered footage */
	private final JLayeredPane body;

	public CircleDetector() {
		setTitle("Circle Detector");
		setResizable(false);

		final JComponent webcam = new WebcamView(); // The webcam feed

		body = new JLayeredPane();
		body.add(webcam);
		// PALETTE_LAYER is used to render on top of the webcam
		body.add(new CircleView((WebcamView)webcam), JLayeredPane.PALETTE_LAYER);// The filter/circles feed
		add(body);

		// Resize to fit (2x frame size)
		setSize(webcam.getSize());

		// Updates the screen
		new Timer(33, this).start(); // 1000 / 30 ~= 33
	}

	/** Creates a window and lets it run */
	public static void main(String[] args) {
		new CircleDetector().setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) { // Called on every timer tick
		body.repaint(); // Don't need to check source, we only have 1 event
	}
}
