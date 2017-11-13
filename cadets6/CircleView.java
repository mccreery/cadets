package cadets6;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/** Responsible for drawing filtered versions of the webcam and circles found */
public class CircleView extends JComponent {
	private static final long serialVersionUID = -2974098472854709810L;

	/** The webcam to filter */
	private final WebcamView source;

	/** Buffers holding the filtered versions of the current frame */
	private final BufferedImage sobel, hough;

	/** The circles we found this frame */
	private final List<Circle> circles = new ArrayList<Circle>();

	/** @param source The webcam to filter */
	public CircleView(WebcamView source) {
		this.source = source;

		setLocation(0, 0);
		setSize(source.getSize());

		// Create buffers the same size as the original webcam
		sobel = new BufferedImage(source.frame.getWidth(), source.frame.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		hough = new BufferedImage(source.frame.getWidth(), source.frame.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	}

	/** Performs the Sobel operator on the source image and outputs a 0-255 grayscale representation to the destination image
	 * @param srcImage The source image
	 * @param destImage The destination image */
	public void sobel(BufferedImage srcImage, BufferedImage destImage) {
		// Allows direct access to the images' pixels
		WritableRaster src = srcImage.getRaster();
		WritableRaster dest = destImage.getRaster();

		// Sobel kernels in the horizontal and vertical directions
		float[][] kernelX = {
			{-1, 0, 1},
			{-2, 0, 2},
			{-1, 0, 1}
		};
		float[][] kernelY = {
			{-1, -2, -1},
			{0, 0, 0},
			{1, 2, 1}
		};

		// Loop across all valid pixels (edges of the image have missing sides)
		for(int x = 1; x < src.getWidth() - 1; x++) {
			for(int y = 1; y < src.getHeight() - 1; y++) {
				float magX = 0, magY = 0; // magnitude = 'edginess'

				// Loop over kernel
				for(int i = 0; i < 3; i++) {
					for(int j = 0; j < 3; j++) {
						// Average out pixel to get greyscale
						int[] pixel = src.getPixel(x+i-1, y+j-1, (int[])null);
						float grey = ((float)pixel[0] / 255f
							+ (float)pixel[1] / 255f
							+ (float)pixel[2] / 255f) / 3;

						// Partially convolute
						magX += grey * kernelX[i][j];
						magY += grey * kernelY[i][j];
					}
				}

				// Overall edginess is the length of the vector (magX, magY)
				int[] output = new int[] {(int)(Math.sqrt(magX*magX + magY*magY) * 255)};
				dest.setPixel(x, y, output); // Pixel complete
			}
		}
	}

	/** The desired radius of circles to identify, in screen pixels */
	private static final int RADIUS = 50;
	/** The minimum 'circle-ness' of each pixel, as a threshold to identify the centre of a circle */
	private static final int LIMIT = 127;

	/** Performs the Hough transform on the source image and outputs a 0-255 grayscale representation to the destination image
	 * @param srcImage The source image (output from Sobel)
	 * @param destImage The destination image */
	public void hough(BufferedImage srcImage, BufferedImage destImage) {
		// Direct access to image pixels
		WritableRaster src = srcImage.getRaster();

		// Reset image to black (no circles)
		Graphics g = destImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, destImage.getWidth(), destImage.getHeight());
		g.setColor(new Color(255, 255, 255, 1)); // Almost completely transparent white

		// Loop over each pixel in the image
		for(int y = 0; y < srcImage.getHeight(); y++) {
			for(int x = 0; x < srcImage.getWidth(); x++) {
				/* If we have a strong edge here, this pixel could be 
				 *on the edge of a circle */
				if(src.getPixel(x, y, (int[])null)[0] > 127) {
					/* Add 1 to all possible pixels which could be a centre of the circle that (x, y) is on the edge of
					 * Meaning: Draw a circle around (x, y) - all possible centres are just the circumference of
					 * a circle with the same radius as the circle. If every point on a circle is present,
					 * these circumferences will overlap to make a very bright spot in the real centre */
					g.drawArc(x-RADIUS, y-RADIUS, RADIUS*2, RADIUS*2, 0, 360);
				}
			}
		}
		g.dispose();
	}

	@Override
	public void paintComponent(Graphics g) {
		// Perform our filters
		sobel(source.frame, sobel);
		hough(sobel, hough);

		// Draw the outputs from both filters
		g.drawImage(sobel, 0, source.frame.getHeight(), source.frame.getWidth(), source.frame.getHeight(), null);
		g.drawImage(hough, source.frame.getWidth(), source.frame.getHeight(), source.frame.getWidth(), source.frame.getHeight(), null);

		// Look for circles
		circles.clear();
		WritableRaster raster = hough.getRaster(); // Direct access for reading

		for(int y = 0; y < raster.getHeight(); y++) {
			for(int x = 0; x < raster.getWidth(); x++) {
				// Hough found a good enough match for a circle
				if(raster.getPixel(x, y, (int[])null)[0] > LIMIT) {
					circles.add(new Circle(x, y, RADIUS));
				}
			}
		}

		// Draw nice clean circles we found
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.RED);

		for(Circle c : circles) {
			g.drawArc(source.frame.getWidth() / 2 + c.x - c.radius, c.y - c.radius, c.radius * 2, c.radius * 2, 0, 360);
		}
	}
}
