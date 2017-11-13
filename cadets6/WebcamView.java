package cadets6;

import java.awt.Color;
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

public class WebcamView extends JComponent {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	private static final long serialVersionUID = 7162394755561123469L;

	private final VideoCapture capture;
	public final BufferedImage frame;

	public WebcamView() {
		setBackground(Color.BLACK);
		capture = new VideoCapture(0);
		setLocation(0, 0);

		Mat mat = new Mat();
		capture.read(mat);
		setSize(mat.cols() * 2, mat.rows() * 2);
		frame = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_3BYTE_BGR);
	}

	@Override
	public void paintComponent(Graphics g) {
		Mat mat = new Mat();
		capture.read(mat);
		int width = mat.cols();
		int height = mat.rows();

		byte[] dat = new byte[width * height * 3];
		mat.get(0, 0, dat);
		DataBuffer buf = new DataBufferByte(dat, dat.length);

		ComponentSampleModel model = new ComponentSampleModel(DataBuffer.TYPE_BYTE, width, height, 3, width*3, new int[] {2, 1, 0});
		frame.setData(Raster.createRaster(model, buf, null));

		g.drawImage(frame, frame.getWidth() / 2, 0, null);
	}
}
