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
	public static final double SLINGSHOT_X = 250;
	public static final double SLINGSHOT_Y = 500;
	public static final double MAX_HAND_DIST = 200;
	
	public static final double MAX_HAND_V = 5;
	public static final double BALL_ACCELERATION = 3;
	public static final double GRAVITY = -1;
	
	public static final int BASE_TIME = 500;
	public static final int TARGET_TIME = 250;
	
	boolean display;
	SlingshotGUI gui;
	
	//Sim variables
	public double ballX;
	public double ballY;
	public double ballVX;
	public double ballVY;
	public double targetX;
	public double targetY;
	public BallState ballState;
	
	private int totalTime;
	private Random rand;
	
	public Slinger slinger;
	
	public SlingshotController(Slinger slinger){
		rand = new Random();
		placeTarget();
		resetBall();
		
		this.slinger = slinger;
		//TODO: random starting positions?
		slinger.x = (rand.nextDouble() * MAX_HAND_DIST * 2) - MAX_HAND_DIST + SLINGSHOT_X;
		slinger.y = (rand.nextDouble() * MAX_HAND_DIST * 2) - MAX_HAND_DIST + SLINGSHOT_Y;
	}
	
	private void placeTarget(){
		//targetX = rand.nextDouble() * SCREEN_WIDTH;
		//targetY = rand.nextDouble() * SCREEN_HEIGHT;
		
		targetX = 1000;
		targetY = 300;
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
		
		totalTime = BASE_TIME;
		//for(int i=0; i<ROUND_TIME && !killMade; i++)
		for(int timer=0; timer<totalTime; timer++)
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
		//Update the neural net
		double distToBall = SimMath.euclideanDist(SLINGSHOT_X, SLINGSHOT_Y, slinger.x, slinger.y);
		if(distToBall < slinger.closestToBall){
			slinger.closestToBall = (int)distToBall;
		}
		double distToTarget = SimMath.euclideanDist(ballX, ballY, targetX, targetY);
		if(ballState == BallState.AWAY && distToTarget < slinger.closestToTarget){
			slinger.closestToTarget = (int)distToTarget;
		}
		slinger.angle = SimMath.angle(SLINGSHOT_X, SLINGSHOT_Y, targetX, targetY);
		slinger.dist = SimMath.euclideanDist(SLINGSHOT_X, SLINGSHOT_Y, targetX, targetY);
		slinger.update();
		
		//Move hand
		slinger.x += slinger.vx;
		slinger.y -= slinger.vy;
		if(slinger.x < SLINGSHOT_X - MAX_HAND_DIST){
			slinger.x = SLINGSHOT_X - MAX_HAND_DIST;
		}
		if(slinger.y < SLINGSHOT_Y - MAX_HAND_DIST){
			slinger.y = SLINGSHOT_Y - MAX_HAND_DIST;
		}
		if(slinger.x > SLINGSHOT_X + MAX_HAND_DIST){
			slinger.x = SLINGSHOT_X + MAX_HAND_DIST;
		}
		if(slinger.y > SLINGSHOT_Y + MAX_HAND_DIST){
			slinger.y = SLINGSHOT_Y + MAX_HAND_DIST;
		}
		
		if(SimMath.euclideanDist(slinger.x, slinger.y, ballX, ballY) < BALL_RADIUS 
				&& ballState == BallState.LOADED && slinger.grabbing){
			ballState = BallState.HELD;
			slinger.hasBall = true;
			slinger.grabbedBall = true;
		} else if(ballState == BallState.HELD && !slinger.grabbing){
			slinger.hasBall = false;
			int pullDist = (int)SimMath.euclideanDist(SLINGSHOT_X, SLINGSHOT_Y, slinger.x, slinger.y);
			if(pullDist > 40){
				slinger.pullDist += pullDist;
				ballState = BallState.RELEASED;
			} else {
				//if ball not pulled far enough, don't launch it
				resetBall();
			}
			
		}
		
		//Move ball
		switch(ballState){
		case HELD:
			ballX = slinger.x;
			ballY = slinger.y;
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
				slinger.targetsHit++;
				slinger.closestToTarget = Slinger.FARTHEST_FROM_TARGET;
				totalTime = BASE_TIME + ((int)Math.sqrt(slinger.targetsHit) * TARGET_TIME);
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
	}
}
