package cadets6;

import java.awt.Point;
import java.util.Iterator;

public class Circle implements Iterable<Point> {
	public int x, y, radius;

	public Circle(int x, int y, int radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	private float theta = 0;
	public void  reset() {theta = 0;}

	@Override
	public Iterator<Point> iterator() {
		return new Iterator<Point>() {
			@Override
			public boolean hasNext() {
				if(theta >= Math.PI*2) { // Reset for the next consumer
					reset();
					return false;
				} else {
					return true;
				}
			}

			@Override
			public Point next() {
				Point p = new Point(
					x + (int)(Math.cos(theta) * radius),
					y + (int)(Math.sin(theta) * radius)
				);
				theta += Math.PI;
				return p;
			}
		};
	}
}
