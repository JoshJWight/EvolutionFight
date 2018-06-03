import java.awt.Color;


public class Fighter extends Creature {
	//net inputs: const, left sensor, right sensor, distance to wall, sensor split angle 
	//net outputs: sensor split angle, speed, turn speed, shoot
	private static final int SHOOT_COOLDOWN = 200;
	private static final double SPEED_MULTIPLIER = 5;
	private static final double TURN_MULTIPLIER = 0.2;
	private static final double SENSOR_MULTIPLIER = 0.002;
	public static final int N_INPUTS = 7;
	public static final int N_OUTPUTS = 5;
		
	//score parameters
	public static final int HIT_VALUE = 1000000;
	public static final int HIT_TAKEN_VALUE = -10000000;
	public static final int SHOT_VALUE = 1000;
	public static final int SCAN_VALUE = 1;
	public static final int COLLIDE_VALUE = -1000;
	
	public Color color = Color.CYAN;
	
	//these parameters are updated by the update method here
	//and can be manually adjusted by the controller if needed
	public double x;
	public double y;
	public double speed;
	public double strafeSpeed;
	public double turnSpeed;
	public double angle;
	public double sensorSplit;
	public double sensorSplitSpeed;
	public boolean shooting;
	
	public int shootCountdown = 0;

	//score parameters
	public int hits = 0;
	public int hitsTaken = 0;
	public int scanFrames = 0;;
	public int collideFrames = 0;
	public int shots = 0;
	
	//sensor data must be updated by controller
	public boolean leftSensor;
	public boolean rightSensor;
	public double distToWall;
	public double distToOther;
	public boolean beingScanned;
	
    public Fighter(NeatNet net){
    	super(net);
    }
	
	public Creature clone() {
		Fighter c = new Fighter(new NeatNet(this.net.spec()));
		return c;
	}

	@Override
	public int score() {
		int score = 0;
		score += hits * HIT_VALUE;
		score += hitsTaken * HIT_TAKEN_VALUE;
		score += scanFrames * SCAN_VALUE;
		score += collideFrames * COLLIDE_VALUE;
		score += shots * SHOT_VALUE;
		return score;
	}

	@Override
	public void resetScore() {
		hits = 0;
		hitsTaken = 0;
		scanFrames = 0;
		collideFrames = 0;
		shots = 0;
	}

	@Override
	public void update() {
		if(shootCountdown > 0) {
			shootCountdown--;
		}
		shooting = false;
		
		double[] input = new double[N_INPUTS];
		input[0] = 1; //const
		input[1] = leftSensor ? 1 : 0;
		input[2] = rightSensor ? 1 : 0;
		input[3] = distToWall;
		input[4] = sensorSplit;
		input[5] = distToOther;
		input[6] = beingScanned ? 1: 0;
		double[] output = net.update(input);
		sensorSplitSpeed = output[0] * SENSOR_MULTIPLIER;
		speed = output[1] * SPEED_MULTIPLIER;
		turnSpeed = output[2] * TURN_MULTIPLIER;
		if(shootCountdown == 0 && output[3] > 0) {
			shooting = true;
			shootCountdown = SHOOT_COOLDOWN;
		}
		strafeSpeed = output[4];
		
		angle += turnSpeed;
		if(angle > Math.PI * 2) {
			angle -= Math.PI * 2;
		}
		sensorSplit += sensorSplitSpeed;
		if(sensorSplit<0) {
			sensorSplit=0;
		} else if(sensorSplit>1) {
			sensorSplit=1;
		}
		
		x+= speed * Math.cos(angle) + strafeSpeed * Math.sin(angle);
		y+= speed * Math.sin(angle) + strafeSpeed * Math.cos(angle);
		
		beingScanned = false;
	}
	
	public int inputs() {
		return N_INPUTS;
	}

	public int outputs() {
		return N_OUTPUTS;
	}

}
