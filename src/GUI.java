import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JFrame;

public class GUI extends JFrame{
	GameController controller;
	public GUI(GameController controller){
		this.controller = controller;
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.out.println("game window closing");
		        controller.display = false;
		    }
		});
		this.setTitle("Evolution Sim");
		this.setSize(controller.ARENA_WIDTH, controller.ARENA_HEIGHT);
		this.setVisible(true);
	}
	
	public void paint(Graphics graphics){
		Image image = createImage(getWidth(), getHeight());
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, controller.ARENA_WIDTH, controller.ARENA_HEIGHT);
		for(int i=0; i<controller.creatures.size(); i++) {
			Creature c = controller.creatures.get(i);
			
			//draw body
			g.setColor(c.color);
			int cr = GameController.CREATURE_RADIUS;
			g.fillOval((int)c.x-cr, (int)c.y-cr, cr*2, cr*2);
			
			//draw angle
			float intensity = (float)Math.min(c.distToWall, 1);
			g.setColor(new Color(intensity, intensity, 1.0f));
			int ar = 5;
			g.fillOval(SimMath.addPolarToX(c.x, c.angle, cr) - ar, SimMath.addPolarToY(c.y, c.angle, cr) - ar, ar*2, ar*2);
			
			//draw sensors
			intensity = (float)Math.min(c.distToOther, 1);
			Color activeColor = new Color(1.0f, intensity, intensity);
			g.setColor(c.leftSensor? activeColor : Color.BLUE);
			Point dest1 = SimMath.addPolar(c.x, c.y, c.angle + (c.sensorSplit * Math.PI), 2000);
			g.drawLine((int)c.x, (int)c.y, dest1.x, dest1.y);
			g.setColor(c.rightSensor? activeColor : Color.BLUE);
			Point dest2 = SimMath.addPolar(c.x, c.y, c.angle - (c.sensorSplit * Math.PI), 2000);
			g.drawLine((int)c.x, (int)c.y, dest2.x, dest2.y);
			
			//draw scores
			g.setColor(Color.WHITE);
			g.drawString("Player " + (i+1) + ": " + c.score(), 10, 20*(i+3));
		}
		
		for(Bullet b: controller.bullets) {
			g.setColor(Color.RED);
			int br = GameController.BULLET_RADIUS;
			g.fillOval((int)b.x-br, (int)b.y-br, br*2, br*2);
		}
		graphics.drawImage(image, 0, 0, null);
	}
}
