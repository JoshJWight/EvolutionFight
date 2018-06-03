import java.awt.Color;
import java.util.ArrayList;


public class ArenaEvolutionController extends EvolutionController {
	private Fighter antagonist;
	
	
	public ArenaEvolutionController(){
		super();
		this.neat = new NeatEvolution(Fighter.N_INPUTS, Fighter.N_OUTPUTS, POP_SIZE);
		antagonist = new Fighter(new NeatNet(Fighter.N_INPUTS, Fighter.N_OUTPUTS));
	}


	protected Creature makeCreature(NeatNet net) {
		return new Fighter(net);
	}

	protected void runOne(Creature creature, boolean show) {
		antagonist.color = Color.PINK;
		GameController game = new GameController((Fighter)creature, antagonist);
		game.run(show);
		
	}

	protected void endOfGen() {
		int firstPositiveIndex = -1;
		for(int i=0; i<completed.size(); i++){
			if(completed.get(i).score() > 0){
				firstPositiveIndex = i;
				break;
			}
		}
		//choose the new antagonist
		//top 16 scorers vs 16 randomly chosen positive scorers in a tournament
		//winner of the tournament plays a best-of-9 match against the current antagonist
		ArrayList<Fighter> entrants = new ArrayList<Fighter>();
		for(int i=1; i<=16; i++) {
			entrants.add((Fighter)completed.get(POP_SIZE - i).clone());
			if(firstPositiveIndex != -1){
				entrants.add((Fighter)completed.get(rand.nextInt(POP_SIZE - firstPositiveIndex) + firstPositiveIndex).clone());
			}
		}
		while(entrants.size() > 1){
			ArrayList<Fighter> nextRound = new ArrayList<Fighter>();
			for(int i=0; i<entrants.size(); i+=2){
				Fighter c1 = entrants.get(i);
				Fighter c2 = entrants.get(i+1);
				GameController game = new GameController(c1, c2);
				game.run(false);
				nextRound.add(c1.score() > c2.score() ? c1 : c2);
			}
			entrants = nextRound;
		}
		Fighter champ = entrants.get(0);
		int champWins = 0;
		int antagonistWins = 0;
		for(int i=0; i<9; i++){
			GameController game = new GameController(champ, antagonist);
			game.run(false);
			if(antagonist.score() > champ.score()){
				antagonistWins++;
			} else {
				champWins++;
			}
		}
		if(champWins > antagonistWins){
			System.out.println("New antagonist!");
			antagonist = champ;
		} else{
			System.out.println("Current antagonist reigns!");
		}
	}
}
