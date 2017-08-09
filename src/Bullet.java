
public class Bullet {
	private static double SPEED = 10;
	
	public Creature owner;
	public double x;
	public double y;
	public double angle;
	public Bullet(double x, double y, double angle, Creature owner) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.owner = owner;
	}
	public void update(){
		x += SPEED * Math.cos(angle);
		y += SPEED * Math.sin(angle);
	}
	
}
