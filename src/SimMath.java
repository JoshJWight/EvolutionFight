import java.awt.Point;

public class SimMath {
	public static double sigmoid(double d) {
		return -1.0 + 2.0 / (1.0 + Math.exp(-1 * d));
	}
	
	public static int addPolarToX(double x, double angle, double magnitude) {
		return (int)(x + (magnitude * Math.cos(angle)));
	}
	
	public static int addPolarToY(double y, double angle, double magnitude) {
		return (int)(y + (magnitude * Math.sin(angle)));
	}
	
	public static Point addPolar(double x, double y, double angle, double magnitude){
		return new Point((int)(x + (magnitude * Math.cos(angle))), (int)(y + (magnitude * Math.sin(angle))));
	}
	
	public static double euclideanDist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
	}
	
	public static boolean rayTouchesCircle(double rx, double ry, double ra, double cx, double cy, double cr) {
		double ca = angle(rx, ry, cx, cy);
		double wa = Math.atan2(cr, euclideanDist(rx, ry, cx, cy));
		double ca1 = ca + wa;
		double ca2 = ca - wa;
		ra = normAngle(ra);
		//address edge cases where ca1 or ca2 has gone out of bounds
		if(ra < ca2) {
			ra += Math.PI * 2;
		} else if(ra > ca1) {
			ra -= Math.PI * 2;
		}
		return ra > ca2 && ra < ca1;
		
	}
	
	public static double normAngle(double angle) {
		while(angle>Math.PI) {
			angle -= Math.PI * 2;
		}
		while(angle<-Math.PI) {
			angle += Math.PI * 2;
		}
		return angle;
	}
	
	public static double angle(double x1, double y1, double x2, double y2) {
		return Math.atan2(y2-y1, x2-x1);
	}
}
