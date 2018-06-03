
public class Slinger extends Creature {
	//Inputs: ????
	public static final int INPUTS = 6;
	//Outputs: horizontal and vertical velocities, grabbing yes/no
	public static final int OUTPUTS = 3;
	
	public static final double BIAS_STEP = 1;
	
	public static final int FARTHEST_FROM_BALL = 500;
	public static final int FARTHEST_FROM_TARGET = 1500;
	public static final int APPROACH_BALL_VALUE = 1;
	public static final int GRAB_VALUE = 10000;
	public static final int APPROACH_TARGET_VALUE = 500;
	public static final int PULL_DIST_VALUE = 0;
	public static final int TARGET_VALUE = 1000000;
	

	//net inputs
	public double x;
	public double y;
	public double angle;
	public double dist;
	//public double bias = 0;
	public boolean hasBall;
	
	
	//net outputs
	public double vx;
	public double vy;
	public boolean grabbing;

	//score components
	public int closestToBall;
	public boolean grabbedBall;
	public int closestToTarget;
	public int targetsHit;
	public int pullDist;
	
	
	public Slinger(NeatNet net){
		super(net);
		resetScore();
	}
	
	public int score() {
		int score = 0;
		score += (FARTHEST_FROM_BALL - closestToBall) * APPROACH_BALL_VALUE;
		score += (grabbedBall ? 1 : 0) * GRAB_VALUE;
		score += (FARTHEST_FROM_TARGET - closestToTarget) * APPROACH_TARGET_VALUE;
		score += TARGET_VALUE * targetsHit;
		score += PULL_DIST_VALUE * pullDist;
		return score;
	}

	public void resetScore() {
		closestToBall = 500;
		grabbedBall = false;
		closestToTarget = 1500;
		targetsHit = 0;
		pullDist = 0;
	}

	public void update() {
		double input[] = new double[INPUTS];
		input[0] = (x - SlingshotController.SLINGSHOT_X) / SlingshotController.MAX_HAND_DIST;
		input[1] = (y - SlingshotController.SLINGSHOT_Y) / SlingshotController.MAX_HAND_DIST;
		input[2] = SimMath.normAngle(angle) / Math.PI;
		input[3] = (dist / FARTHEST_FROM_TARGET);
		input[4] = hasBall? 0 : 1;
		//input[5] = SimMath.sigmoid(bias);
		input[5] = 1; //constant
		
		double[] output = net.update(input);
		
		vx = output[0] * SlingshotController.MAX_HAND_V;
		vy = output[1] * SlingshotController.MAX_HAND_V;
		grabbing = (output[2] > 0.5);
	}

	public Creature clone() {
		Slinger c = new Slinger(new NeatNet(this.net.spec()));
		return c;
	}
	
	public int inputs() {
		return INPUTS;
	}

	public int outputs() {
		return OUTPUTS;
	}
}
