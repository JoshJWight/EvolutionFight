import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;


public class SlingshotGUI extends JFrame{ //implements KeyListener{
	
	public static final boolean MANUAL_ENABLED = false;
	
	SlingshotController controller;
	public SlingshotGUI(final SlingshotController controller){
		//this.addKeyListener(this);
		this.controller = controller;
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.out.println("game window closing");
		        controller.display = false;
		    }
		});
		this.setTitle("Evolution Sim");
		this.setSize(controller.SCREEN_WIDTH, controller.SCREEN_HEIGHT);
		this.setVisible(true);
	}
	
	//find a point X on the circle centered at A, such that
	//XB is tangent to the circle, and X is maximally far from C
	//This is how we draw the slingshot strings
	private Point findTangentPoint(Point a, Point b, Point c){
		double dist = SimMath.euclideanDist(a, b);
		double r = SlingshotController.BALL_RADIUS;
		if(dist==0){return b;}//Don't draw if they're overlapping
		double theta = Math.asin(r/dist);
		double len = Math.sqrt((dist * dist) - (r * r));
		double ang = SimMath.angle(b.x, b.y, a.x, a.y);
		Point t1 = SimMath.addPolar(b.x, b.y, ang - theta, len);
		Point t2 = SimMath.addPolar(b.x, b.y, ang + theta, len);
		if(SimMath.euclideanDist(t1, c) > SimMath.euclideanDist(t2, c)){
			return t1;
		}
		return t2;
	}
	
	public void paint(Graphics graphics){
		Image image = createImage(getWidth(), getHeight());
		Graphics g = image.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, SlingshotController.SCREEN_WIDTH, SlingshotController.SCREEN_HEIGHT);
		
		Point s = new Point((int)SlingshotController.SLINGSHOT_X, (int)SlingshotController.SLINGSHOT_Y);
		Point b = new Point((int)controller.ballX, (int)controller.ballY);
		Point p1 = new Point(s.x, s.y + 100);
		Point p2 = new Point(s.x, s.y - 100);
		Point h = new Point((int)controller.slinger.x, (int)controller.slinger.y);
		Point t = new Point((int)controller.targetX, (int)controller.targetY);
		//Draw the ball
		int br = SlingshotController.BALL_RADIUS;
		g.setColor(Color.RED);
		g.fillOval(b.x - br, b.y - br, br * 2, br * 2);
		//Draw the pegs
		int pr = 10;
		g.setColor(Color.BLUE);
		g.fillOval(p1.x - pr, p1.y - pr, pr * 2, pr * 2);
		g.fillOval(p2.x - pr, p2.y - pr, pr * 2, pr * 2);
		
		//Draw the strings
		if(controller.ballState == SlingshotController.BallState.AWAY){
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
		} else {
			Point t1 = findTangentPoint(b, p1, p2);
			Point t2 = findTangentPoint(b, p2, p1);
			//hopefully fixes the glitchy lines when touching the peg
			if(!(SimMath.euclideanDist(p1, t1) > 500)){
				g.drawLine(p1.x, p1.y, t1.x, t1.y);
			}
			if(!(SimMath.euclideanDist(p2, t2) > 500)){
				g.drawLine(p2.x, p2.y, t2.x, t2.y);
			}
		}
		//Draw the target
		int tr = SlingshotController.TARGET_RADIUS;
		g.setColor(Color.YELLOW);
		g.fillOval(t.x - tr, t.y - tr, tr * 2, tr * 2);
		
		//Draw the hand
		int hr = 10;
		if(controller.slinger.grabbing){
			g.setColor(Color.GREEN);
		} else {
			g.setColor(Color.CYAN);
		}
		g.fillOval(h.x - hr, h.y - hr, hr * 2, hr * 2);
		
		graphics.drawImage(image, 0, 0, null);
	}

	/*public void keyPressed(KeyEvent e) {
		if(!MANUAL_ENABLED){
			return;
		}
		
		switch(e.getKeyCode()){
		case KeyEvent.VK_SPACE:
			controller.handGrabbing = true;
			break;
		case KeyEvent.VK_UP:
			controller.handVY = SlingshotController.MAX_HAND_V;
			break;
		case KeyEvent.VK_DOWN:
			controller.handVY = -1 * SlingshotController.MAX_HAND_V;
			break;
		case KeyEvent.VK_RIGHT:
			controller.handVX = SlingshotController.MAX_HAND_V;
			break;
		case KeyEvent.VK_LEFT:
			controller.handVX = -1 * SlingshotController.MAX_HAND_V;
			break;
		default:
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		if(!MANUAL_ENABLED){
			return;
		}
		
		switch(e.getKeyCode()){
		case KeyEvent.VK_SPACE:
			controller.handGrabbing = false;
			break;
		case KeyEvent.VK_UP:
			controller.handVY = 0;
			break;
		case KeyEvent.VK_DOWN:
			controller.handVY = 0;
			break;
		case KeyEvent.VK_RIGHT:
			controller.handVX = 0;
			break;
		case KeyEvent.VK_LEFT:
			controller.handVX = 0;
			break;
		default:
			break;
		}
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}*/
}
