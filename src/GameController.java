import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.SwingUtilities;

public class GameController {
	public static final int CREATURE_RADIUS = 30;
	public static final int BULLET_RADIUS = 8;
	public static final int ARENA_WIDTH = 1500;
	public static final int ARENA_HEIGHT = 900;
	public static final int ROUND_TIME = 500;
	
	public ArrayList<Fighter> creatures;
	public ArrayList<Bullet> bullets;
	public boolean display;
	private GUI gui;
	
	private boolean killMade;
	
	public GameController(Fighter c1, Fighter c2) {
		creatures = new ArrayList<Fighter>();
		bullets = new ArrayList<Bullet>();
		
		Random rand = new Random();
		c1.x = rand.nextInt(ARENA_WIDTH);
		c1.y = rand.nextInt(ARENA_HEIGHT);
		c1.angle = rand.nextDouble() * Math.PI * 2;
		creatures.add(c1);
		
		c2.x = 1000;
		c2.y = 500;
		c2.angle = 0;
		creatures.add(c2);
		
		c1.resetScore();
		c2.resetScore();
	}
	
	public void run(boolean display){
		this.display = display;
		if(display){
			gui = new GUI(this);
		}
		
		
		for(int i=0; i<ROUND_TIME && !killMade; i++)
		{
			tick();
			if(display){
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						gui.repaint();
					}
				});
				Thread.yield();
				try {
					Thread.sleep(20);
			   	} catch (InterruptedException e) {
			   		e.printStackTrace();
			   	}
			}
		}
		if(display) {
			gui.setVisible(false);
			gui.dispose();
		}
	}
	
	public void tick(){
		for(Fighter c: creatures) {
	
			c.update();
			
			//snap Fighter position to stay within box
			if(c.x < CREATURE_RADIUS) {
				c.x = CREATURE_RADIUS;
			}
			if(c.y < CREATURE_RADIUS) {
				c.y = CREATURE_RADIUS;
			}
			if(c.x > ARENA_WIDTH - CREATURE_RADIUS) {
				c.x = ARENA_WIDTH - CREATURE_RADIUS;
			}
			if(c.y > ARENA_HEIGHT - CREATURE_RADIUS) {
				c.y = ARENA_HEIGHT - CREATURE_RADIUS;
			}
			
			//update sensors
			c.leftSensor = false;
			c.rightSensor = false;
			c.distToOther = 0;
			for(Fighter other: creatures) {
				if(other !=c) {
					if(SimMath.euclideanDist(c.x, c.y, other.x, other.y) < CREATURE_RADIUS * 2){
						c.collideFrames += 1;
					}
					
					if(SimMath.rayTouchesCircle(c.x, c.y, c.angle - (c.sensorSplit * Math.PI), other.x, other.y, CREATURE_RADIUS)) {
						c.rightSensor = true;
						c.distToOther = SimMath.euclideanDist(c.x, c.y, other.x, other.y) / ARENA_WIDTH;
						//this will be a frame fast for some creatures.
						other.beingScanned = true;
						c.scanFrames++;
					}
					if(SimMath.rayTouchesCircle(c.x, c.y, c.angle + (c.sensorSplit * Math.PI), other.x, other.y, CREATURE_RADIUS)) {
						c.leftSensor = true;
						c.distToOther = SimMath.euclideanDist(c.x, c.y, other.x, other.y) / ARENA_WIDTH;
						other.beingScanned = true;
						c.scanFrames++;
					}
					
				}
			}
			
			
			
			c.distToWall = distToWall(c) / ARENA_WIDTH;
			
			if(c.shooting) {
				Bullet b = new Bullet(c.x + Math.cos(c.angle) * CREATURE_RADIUS, c.y + Math.sin(c.angle) * CREATURE_RADIUS, c.angle, c);
				bullets.add(b);
				c.shots++;
			}
		}
		Iterator<Bullet> bi = bullets.iterator();
		while(bi.hasNext()) {
			Bullet b = bi.next();
			b.update();
			if(b.x<-BULLET_RADIUS || b.y<-BULLET_RADIUS || b.x > ARENA_WIDTH + BULLET_RADIUS || b.y > ARENA_HEIGHT + BULLET_RADIUS) {
				bi.remove();
				continue;
			}
			for(Fighter c: creatures) {
				if(c!= b.owner && SimMath.euclideanDist(b.x, b.y, c.x, c.y) < BULLET_RADIUS + CREATURE_RADIUS){
					b.owner.hits += 1;
					c.hitsTaken += 1;
					killMade = true;
					bi.remove();
					break;
				}
			}
		}
		
		//TODO collision checking etc
	}
	
	private double distToWall(Fighter c){
		double atl = SimMath.angle(c.x, c.y, 0, 0);
		double atr = SimMath.angle(c.x, c.y, ARENA_WIDTH, 0);
		double abl = SimMath.angle(c.x, c.y, 0, ARENA_HEIGHT);
		double abr = SimMath.angle(c.x, c.y, ARENA_WIDTH, ARENA_HEIGHT);
		
		double ca = SimMath.normAngle(c.angle);
		
		if(ca > atl && ca <= atr) {
			//top
			return c.y / Math.abs(Math.cos(ca));
		} else if(ca > atr && ca<=abr) {
			//right
			return (ARENA_WIDTH - c.x) / Math.abs(Math.cos(ca));
		} else if(ca > abr && ca<=abl) {
			//bottom
			return (ARENA_HEIGHT - c.y) / Math.abs(Math.cos(ca));
		} else {
			//left
			return c.x / Math.abs(Math.cos(ca));
		}
	}
}
