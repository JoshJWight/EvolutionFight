import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NeatEvolution {
	private static final int SPECIES_TARGET = 20;
	private static final double THRESHOLD_INCREMENT = 1.1;
	
	private double speciationThreshold = 1;
	private int innovationNumber;
	
	public boolean initialized = false;
	
	public int inputs;
	public int outputs;
	public int popSize;
	public ArrayList<NeatNet> nets;
	
	public NeatEvolution(int inputs, int outputs, int pop){
		this.inputs = inputs;
		this.outputs = outputs;
		this.popSize = pop;
	}
	
	public void generate(){
		innovationNumber = inputs + outputs;
		nets = new ArrayList<NeatNet>();
		for(int i=0; i<popSize; i++){
			nets.add(new NeatNet(inputs, outputs));
		}
		initialized = true;
	}
	
	public void load(String path){
		nets = new ArrayList<NeatNet>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(path));
			for(String line: lines){
				NeatNet net = new NeatNet(line);
				if(net.mostRecentInnovation()>=innovationNumber){
					innovationNumber = net.mostRecentInnovation() + 1;
				}
				nets.add(net);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		initialized = true;
	}
	
	public void save(String path){
		try {
			PrintWriter writer = new PrintWriter(path);
			for(NeatNet net: nets){
				writer.println(net.spec());
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void reproduce(){
		ArrayList<Species> species = new ArrayList<Species>();
		for(NeatNet n: nets){
			boolean added = false;
			for(Species s: species){
				if(s.addIfMember(n, speciationThreshold)){
					added = true;
					break;
				}
			}
			if(!added){
				species.add(new Species(n));
			}
		}
		
		if(species.size() > SPECIES_TARGET){
			speciationThreshold *= THRESHOLD_INCREMENT;
		} else{
			speciationThreshold /= THRESHOLD_INCREMENT;
		}
		
		nets = new ArrayList<NeatNet>();
		for(Species s: species){
			nets.addAll(s.reproduce());
		}
		for(NeatNet n: nets){
			innovationNumber += n.mutate(innovationNumber);
		}
		
		System.out.println(species.size() + " species, threshold is " + speciationThreshold);
	}
	
	class Species{
		public NeatNet rep;
		public ArrayList<NeatNet> members;
		
		public Species(NeatNet rep){
			this.rep = rep;
			members = new ArrayList<NeatNet>();
			members.add(rep);
		}
		
		public boolean addIfMember(NeatNet net, double threshold){
			if(rep.delta(net) < threshold){
				members.add(net);
				return true;
			}
			return false;
		}
		
		public ArrayList<NeatNet> reproduce(){
			Random rand = new Random();
			Collections.sort(members);
			ArrayList<NeatNet> offspring = new ArrayList<NeatNet>();
			int n = members.size();
			int t = members.size()/2; //note this rounds down
			for(int i=0; i< members.size(); i++){
				int i1 = rand.nextInt(n - t) + t;
				int i2 = rand.nextInt(n - i1) + i1;
				NeatNet newnet = members.get(i2).crossover(members.get(i1));
				offspring.add(newnet);
			}
			return offspring;
		}
	}
}
