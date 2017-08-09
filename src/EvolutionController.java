import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EvolutionController {
	private EvolutionGUI gui;
	private NeatEvolution neat;
	
	public static final int POP_SIZE = 1000;
	public static final int WINNER_MULTIPLIER = 1;
	
	private Random rand;
	
	public int generation = 1;
	
	private ArrayList<Creature> contestants;
	private ArrayList<Creature> survivors;
	private Creature antagonist;
	
	public boolean auto = false;
	
	public EvolutionController(){
		neat = new NeatEvolution(Creature.N_INPUTS, Creature.N_OUTPUTS, POP_SIZE);
		rand = new Random();
		gui = new EvolutionGUI(this);
		antagonist = new Creature();
	}
	
	public synchronized void generate(){
		neat.generate();
		makeContestants();
	}
	
	public synchronized void makeContestants(){
		contestants = new ArrayList<Creature>();
		survivors = new ArrayList<Creature>();
		for(NeatNet net: neat.nets){
			contestants.add(new Creature(net));
		}
	}
	
	public synchronized void startAuto() {
		if(!neat.initialized){
			System.out.println("NEAT not initialized!");
			return;
		}
		
		System.out.println("Starting auto");
		auto = true;
		while(auto) {
			finish();
		}
		System.out.println("auto stopped");
	}
	
	public synchronized void show() {
		fight(1, true);
	}
	
	public synchronized void finish() {
		fight(POP_SIZE, false);
	}
	
	private synchronized void fight(int n, boolean show) {
		if(!neat.initialized){
			System.out.println("NEAT not initialized!");
			return;
		}
		for(int i=0; i<n && contestants.size() > 0; i++){
			Creature hero = select();
			antagonist.color = Color.PINK;
			GameController game = new GameController(hero, antagonist);
			game.run(show);
			survivors.add(hero);
			hero.net.fitness = hero.score();
		}
		
		if(contestants.size() == 0) {
			System.out.println("------------------------------");
			System.out.println("end of gen " + generation);
			generation++;
			
			Collections.sort(survivors);
			int firstPositiveIndex = -1;
			for(int i=0; i<survivors.size(); i++){
				if(survivors.get(i).score() > 0){
					firstPositiveIndex = i;
					break;
				}
			}
			
			Creature best = survivors.get(POP_SIZE - 1);
			System.out.println("Best creature scored " + best.score());
			
			//choose the new antagonist
			//top 16 scorers vs 16 randomly chosen positive scorers in a tournament
			//winner of the tournament plays a best-of-9 match against the current antagonist
			ArrayList<Creature> entrants = new ArrayList<Creature>();
			for(int i=1; i<=16; i++) {
				entrants.add(survivors.get(POP_SIZE - i).clone());
				if(firstPositiveIndex != -1){
					entrants.add(survivors.get(rand.nextInt(POP_SIZE - firstPositiveIndex) + firstPositiveIndex).clone());
				}
			}
			while(entrants.size() > 1){
				ArrayList<Creature> nextRound = new ArrayList<Creature>();
				for(int i=0; i<entrants.size(); i+=2){
					Creature c1 = entrants.get(i);
					Creature c2 = entrants.get(i+1);
					GameController game = new GameController(c1, c2);
					game.run(false);
					nextRound.add(c1.score() > c2.score() ? c1 : c2);
				}
				entrants = nextRound;
			}
			Creature champ = entrants.get(0);
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
			
			
			
			neat.reproduce();
			makeContestants();
		}
	}
	
	public void save(String path) {
		neat.save(path);
	}
	
	public void read(String path) {
		neat.load(path);
		makeContestants();
	}
	
	private synchronized Creature select(){
		int i = rand.nextInt(contestants.size());
		return contestants.remove(i);
	}
	
	public class GameThread extends Thread{
		public GameController game;
		public boolean show;
		
		public GameThread(GameController game, boolean show){
			this.game = game;
			this.show = show;
		}
		
		public void run(){
			game.run(show);
		}
	}
}
