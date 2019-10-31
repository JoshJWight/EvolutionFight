
public class SlingshotEvolutionController extends EvolutionController {

	public SlingshotEvolutionController(){
		super();
		this.neat = new NeatEvolution(Slinger.INPUTS, Slinger.OUTPUTS, POP_SIZE);
	}
	
	protected Creature makeCreature(NeatNet net) {
		return new Slinger(net);
	}

	@Override
	protected void runOne(Creature creature, boolean show) {
		//all creatures within a generation get the same conditions even though they are randomized
		SlingshotController sim = new SlingshotController((Slinger)creature, this.generation);
		sim.run(show);
		
	}

	protected void endOfGen() {
		//No end of gen stuff here yet
	}

}
