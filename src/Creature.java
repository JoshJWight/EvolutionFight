public abstract class Creature implements Comparable{
	
	public NeatNet net;
	
	
	public Creature(NeatNet net) {
		this.net = net;
	}
	
	public abstract int score();
	
	public abstract int inputs();
	
	public abstract int outputs();
	
	public abstract void resetScore();
	
	public String getSpec(){
		return net.spec();
	}
	
	public abstract void update();
	
	
	public abstract Creature clone();
	

	public int compareTo(Object o) {
		if(!(o instanceof Creature)) {
			return 0;
		} else {
			Creature other = (Creature) o;
			return score() - other.score();
		}
		
	}
}
