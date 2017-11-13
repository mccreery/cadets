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

public class CircleView extends JComponent {
	private static final long serialVersionUID = -2974098472854709810L;

	private final WebcamView source;
	private final BufferedImage sobel, hough;

	private final List<Circle> circles = new ArrayList<Circle>();

	public CircleView(WebcamView source) {
		setBackground(Color.BLACK);
		this.source = source;
		setLocation(0, 0);
		setSize(source.getSize());

		sobel = new BufferedImage(source.frame.getWidth(), source.frame.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		hough = new BufferedImage(source.frame.getWidth(), source.frame.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	}

	public void sobel(BufferedImage srcImage, BufferedImage destImage) {
		WritableRaster src = srcImage.getRaster();
		WritableRaster dest = destImage.getRaster();

		// no clue
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

		for(int x = 1; x < src.getWidth() - 1; x++) {
			for(int y = 1; y < src.getHeight() - 1; y++) {
				float magX = 0, magY = 0;

				for(int i = 0; i < 3; i++) {
					for(int j = 0; j < 3; j++) {
						int[] pixel = src.getPixel(x+i-1, y+j-1, (int[])null);
						float grey = ((float)pixel[0] / 255f
							+ (float)pixel[1] / 255f
							+ (float)pixel[2] / 255f) / 3;

						magX += grey * kernelX[i][j];
						magY += grey * kernelY[i][j];
					}
				}
				int[] output = new int[] {(int)(Math.sqrt(magX*magX + magY*magY) * 255)};
				dest.setPixel(x, y, output);
			}
		}
	}

	private static final int RADIUS = 53;
	private static final int LIMIT = 81;

	public void hough(BufferedImage srcImage, BufferedImage destImage) {
		//int maxRadius = Math.min(srcImage.getWidth(), srcImage.getHeight()) / 2;

		WritableRaster src = srcImage.getRaster();
		Graphics g = destImage.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, destImage.getWidth(), destImage.getHeight());
		g.setColor(new Color(255, 255, 255, 1));

		for(int y = 0; y < srcImage.getHeight(); y++) {
			for(int x = 0; x < srcImage.getWidth(); x++) {
				if(src.getPixel(x, y, (int[])null)[0] > 127) {
					// Guess a circle
					g.drawArc(x-RADIUS, y-RADIUS, RADIUS*2, RADIUS*2, 0, 360);
				}
			}
		}
		g.dispose();
	}

	@Override
	public void paintComponent(Graphics g) {
		sobel(source.frame, sobel);
		hough(sobel, hough);

		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(sobel, 0, source.frame.getHeight(), source.frame.getWidth(), source.frame.getHeight(), null);
		g.drawImage(hough, source.frame.getWidth(), source.frame.getHeight(), source.frame.getWidth(), source.frame.getHeight(), null);

		circles.clear();
		WritableRaster raster = hough.getRaster();
		for(int y = 0; y < raster.getHeight(); y++) {
			for(int x = 0; x < raster.getWidth(); x++) {
				if(raster.getPixel(x, y, (int[])null)[0] > LIMIT) {
					circles.add(new Circle(x, y, RADIUS));
				}
			}
		}

		g.setColor(Color.RED);
		// Draw all the circles we know about
		for(Circle c : circles) {
			g.drawArc(source.frame.getWidth() / 2 + c.x - c.radius, c.y - c.radius, c.radius * 2, c.radius * 2, 0, 360);
		}
	}
}
