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

public abstract class EvolutionController {
	protected EvolutionGUI gui;
	protected NeatEvolution neat;
	
	public static final int POP_SIZE = 1000;
	
	protected Random rand;
	
	public int generation = 1;
	
	protected ArrayList<Creature> remaining;
	protected ArrayList<Creature> completed;
	
	protected Creature lastGenBest;
	
	
	public boolean auto = false;
	
	public EvolutionController(){
		rand = new Random();
		gui = new EvolutionGUI(this);
	}
	
	public synchronized void generate(){
		neat.generate();
		makeremaining();
	}
	
	protected abstract Creature makeCreature(NeatNet net);
	
	public synchronized void makeremaining(){
		remaining = new ArrayList<Creature>();
		completed = new ArrayList<Creature>();
		for(NeatNet net: neat.nets){
			remaining.add(makeCreature(net));
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
	
	public void showBest() {
		if(lastGenBest != null){
			runOne(lastGenBest, true);
		}
	}
	
	public synchronized void show() {
		run(1, true);
	}
	
	public synchronized void finish() {
		run(POP_SIZE, false);
	}
	
	protected abstract void runOne(Creature creature, boolean show);
	
	protected abstract void endOfGen();
	
	private synchronized void run(int n, boolean show) {
		if(!neat.initialized){
			System.out.println("NEAT not initialized!");
			return;
		}
		for(int i=0; i<n && remaining.size() > 0; i++){
			Creature hero = select();
			runOne(hero, show);
			completed.add(hero);
			hero.net.fitness = hero.score();
		}
		
		if(remaining.size() == 0) {
			System.out.println("------------------------------");
			System.out.println("end of gen " + generation);
			generation++;
			
			Collections.sort(completed);
			lastGenBest = completed.get(completed.size()-1).clone();
			endOfGen();
			
			Creature best = completed.get(POP_SIZE - 1);
			System.out.println("Best creature scored " + best.score() + 
					" using " + best.net.hiddenNodes.size() + " hidden nodes and " + 
					best.net.connections.size() + " connections");
			
			neat.reproduce();
			makeremaining();
		}
	}
	
	public void save(String path) {
		neat.save(path);
	}
	
	public void read(String path) {
		neat.load(path);
		makeremaining();
	}
	
	private synchronized Creature select(){
		int i = rand.nextInt(remaining.size());
		return remaining.remove(i);
	}
	
	/*public class GameThread extends Thread{
		public GameController game;
		public boolean show;
		
		public GameThread(GameController game, boolean show){
			this.game = game;
			this.show = show;
		}
		
		public void run(){
			game.run(show);
		}
	}*/
}
