package cadets6;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

import javax.swing.JComponent;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/** Displays the current frame of the primary webcam when painted */
public class WebcamView extends JComponent {
	static {
		// OpenCV uses this: load the native webcam library in before use
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static final long serialVersionUID = 7162394755561123469L;

	/** The webcam instance */
	private final VideoCapture capture;
	/** The last frame we captured */
	public final BufferedImage frame;

	public WebcamView() {
		capture = new VideoCapture(0);
		setLocation(0, 0);

		// Grab sample frame to find out size
		Mat mat = new Mat();
		capture.read(mat);
		setSize(mat.cols() * 2, mat.rows() * 2);
		// Create frame - constant size so never changes reference
		frame = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
	}

	@Override
	public void paintComponent(Graphics g) {
		/* Mat means image in this context
		 * capture a frame into the matrix */
		Mat mat = new Mat();
		capture.read(mat);
		int width = mat.cols();
		int height = mat.rows();

		// We have to convert to feed RGB data into a BGR BufferedImage for display
		byte[] dat = new byte[width * height * 3];
		mat.get(0, 0, dat);
		DataBuffer buf = new DataBufferByte(dat, dat.length);

		// Conversion here
		ComponentSampleModel model = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, width*3, new int[] {2, 1, 0});
		frame.setData(Raster.createRaster(model, buf, null));

		// Draw the frame we captured
		g.drawImage(frame, frame.getWidth() / 2, 0, null);
	}
}
