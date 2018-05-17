import java.util.Random;

import javax.swing.SwingUtilities;


public class SlingshotController {
	public enum BallState{
		LOADED,
		HELD,
		RELEASED,
		AWAY
	}
	
	
	public static final int SCREEN_WIDTH = 1500;
	public static final int SCREEN_HEIGHT = 900;
	public static final int BALL_RADIUS = 20;
	public static final int TARGET_RADIUS = 40;
	public static final int SLINGSHOT_X = 250;
	public static final int SLINGSHOT_Y = 500;
	public static final int MAX_HAND_DIST = 200;
	
	public static final double MAX_HAND_V = 5;
	public static final double BALL_ACCELERATION = 3;
	public static final double GRAVITY = -1;
	
	boolean display;
	SlingshotGUI gui;
	
	//Sim variables
	public double ballX;
	public double ballY;
	public double ballVX;
	public double ballVY;
	public double handX;
	public double handY;
	public double targetX;
	public double targetY;
	public double handVX;
	public double handVY;
	public BallState ballState;
	
	public boolean handGrabbing;
	
	private Random rand;
	
	public SlingshotController(){
		rand = new Random();
		handX = 400;
		handY = 400;
		placeTarget();
		resetBall();
		this.run(true);
	}
	
	private void placeTarget(){
		targetX = rand.nextDouble() * SCREEN_WIDTH;
		targetY = rand.nextDouble() * SCREEN_HEIGHT;
	}
	
	private void resetBall(){
		ballState = BallState.LOADED;
		ballX = SLINGSHOT_X;
		ballY = SLINGSHOT_Y;
		ballVX = 0;
		ballVY = 0;
	}
	
	public void run(boolean display){
		this.display = display;
		if(display){
			gui = new SlingshotGUI(this);
		}
		
		
		//for(int i=0; i<ROUND_TIME && !killMade; i++)
		while(gui!=null)
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
		//TODO: handle state changes from the neural net
		
		//Move hand
		handX += handVX;
		handY -= handVY;
		if(handX < SLINGSHOT_X - MAX_HAND_DIST){
			handX = SLINGSHOT_X - MAX_HAND_DIST;
		}
		if(handY < SLINGSHOT_Y - MAX_HAND_DIST){
			handY = SLINGSHOT_Y - MAX_HAND_DIST;
		}
		if(handX > SLINGSHOT_X + MAX_HAND_DIST){
			handX = SLINGSHOT_X + MAX_HAND_DIST;
		}
		if(handY > SLINGSHOT_Y + MAX_HAND_DIST){
			handY = SLINGSHOT_Y + MAX_HAND_DIST;
		}
		
		if(SimMath.euclideanDist(handX, handY, ballX, ballY) < BALL_RADIUS 
				&& ballState == BallState.LOADED && handGrabbing){
			ballState = BallState.HELD;
		} else if(ballState == BallState.HELD && !handGrabbing){
			ballState = BallState.RELEASED;
		}
		
		//Move ball
		switch(ballState){
		case HELD:
			ballX = handX;
			ballY = handY;
			break;
		case RELEASED:
			ballX += ballVX;
			ballY += ballVY;
			double theta = SimMath.angle(ballX, ballY, SLINGSHOT_X, SLINGSHOT_Y);
			ballVX += BALL_ACCELERATION * Math.cos(theta);
			ballVY += BALL_ACCELERATION * Math.sin(theta);
			if(SimMath.euclideanDist(ballX, ballY, SLINGSHOT_X, SLINGSHOT_Y) < SimMath.euclideanDist(0, 0, ballVX, ballVY)){
				ballState = BallState.AWAY;
			}
			break;
		case AWAY:
			ballX += ballVX;
			ballY += ballVY;
			ballVY -= GRAVITY;
			//Reset if target is hit
			if(SimMath.euclideanDist(ballX, ballY, targetX, targetY) < TARGET_RADIUS){
				resetBall();
				placeTarget();
			}
			
			//Respawn the ball if out of bounds
			if(ballX + BALL_RADIUS < 0 || 
					ballX - BALL_RADIUS > SCREEN_WIDTH || ballY - BALL_RADIUS > SCREEN_HEIGHT ){
				resetBall();
				
			}
			break;
		default:
			break;
		}
		
		//TODO: handle hitting targets
		
		
	}
}
