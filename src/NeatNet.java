import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class NeatNet implements Comparable<NeatNet> {
	private static final int UPDATE_ITERATIONS = 5;
	private static final double DISJOINT_MULTIPLIER = 1;
	private static final double CONNECTION_MULTIPLIER = 0.4;
	private static final double EXCESS_MULTIPLIER = 1;
	private static final double ADD_CONNECTION_PROBABILITY = .05;
	private static final double ADD_NODE_PROBABILITY = .01;
	private static final double CHANGE_PROBABILITY = .1;
	private static final double DISABLE_PROBABILITY = .02;
	private static final double MUTATION_STRENGTH = 0.5;
	
	public int inputs;
	public int outputs;
	public int fitness = 0;
	
	public ArrayList<Node> nodes;
	public HashMap<Integer, Node> nodesByID;
	public ArrayList<Node> inputNodes;
	public ArrayList<Node> outputNodes;
	public ArrayList<Node> hiddenNodes;
	public ArrayList<Connection> connections;
	
	public NeatNet(int inputs, int outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
		nodes = new ArrayList<Node>();
		inputNodes = new ArrayList<Node>();
		outputNodes = new ArrayList<Node>();
		hiddenNodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		nodesByID = new HashMap<Integer, Node>();
		for(int i=0; i<inputs; i++){
			Node n = new Node(i);
			nodes.add(n);
			inputNodes.add(n);
		}
		for(int i=0; i<outputs; i++){
			Node n = new Node(inputs + i);
			nodes.add(n);
			outputNodes.add(n);
		}
	}
	
	public NeatNet(String spec){
		JsonObject obj = new JsonParser().parse(spec).getAsJsonObject();
		
		this.inputs = obj.get("inputs").getAsInt();
		this.outputs = obj.get("outputs").getAsInt();
		nodes = new ArrayList<Node>();
		inputNodes = new ArrayList<Node>();
		outputNodes = new ArrayList<Node>();
		hiddenNodes = new ArrayList<Node>();
		connections = new ArrayList<Connection>();
		nodesByID = new HashMap<Integer, Node>();
		for(JsonElement e: obj.getAsJsonArray("nodes")){
			Node n = new Node(e.getAsInt());
			nodes.add(n);
			nodesByID.put(e.getAsInt(), n);
		}
		for(int i=0; i<inputs; i++){
			inputNodes.add(nodes.get(i));
		}
		for(int i = inputs; i<inputs+outputs; i++){
			outputNodes.add(nodes.get(i));
		}
		for(int i= inputs+outputs; i<nodes.size(); i++){
			hiddenNodes.add(nodes.get(i));
		}
		
		for(JsonElement e: obj.getAsJsonArray("connections")){
			JsonObject o = e.getAsJsonObject();
			int id = o.get("id").getAsInt();
			double weight = o.get("weight").getAsDouble();
			Node source = nodesByID.get(o.get("source").getAsInt());
			Node dest = nodesByID.get(o.get("dest").getAsInt());
			boolean disabled = o.get("disabled").getAsBoolean();
			Connection c = new Connection(id, weight, source, dest, disabled);
			connections.add(c);
		}
	}
	
	public NeatNet(int inputs, int outputs, ArrayList<Node> nodes, ArrayList<Connection> connections){
		this.inputs = inputs;
		this.outputs = outputs;
		this.nodes = nodes;
		this.connections = connections;
		
		this.inputNodes = new ArrayList<Node>();
		this.outputNodes = new ArrayList<Node>();
		this.hiddenNodes = new ArrayList<Node>();
		nodesByID = new HashMap<Integer, Node>();
		for(Node n: nodes){
			nodesByID.put(n.id, n);
		}
		
		for(int i=0; i<inputs; i++){
			inputNodes.add(nodes.get(i));
		}
		for(int i = inputs; i<inputs+outputs; i++){
			outputNodes.add(nodes.get(i));
		}
		for(int i= inputs+outputs; i<nodes.size(); i++){
			hiddenNodes.add(nodes.get(i));
		}
	}
	
	public NeatNet clone(){
		return new NeatNet(this.spec());
	}
	
	public String spec(){
		JsonObject obj = new JsonObject();
		obj.add("inputs", new JsonPrimitive(inputs));
		obj.add("outputs", new JsonPrimitive(outputs));
		JsonArray nodeArr = new JsonArray();
		for(Node n: nodes){
			nodeArr.add(n.id);
		}
		obj.add("nodes", nodeArr);
		JsonArray connArr = new JsonArray();
		for(Connection c: connections){
			JsonObject conn = new JsonObject();
			conn.add("id", new JsonPrimitive(c.id));
			conn.add("weight", new JsonPrimitive(c.weight));
			conn.add("source", new JsonPrimitive(c.source.id));
			conn.add("dest", new JsonPrimitive(c.dest.id));
			conn.add("disabled", new JsonPrimitive(c.disabled));
			connArr.add(conn);
		}
		obj.add("connections", connArr);
		return new Gson().toJson(obj);
	}
	
	public double delta(NeatNet other){
		int i=0, j = 0;
		int disjoint = 0;
		int excess = 0;
		//node genes
		while(true){
			Node n1 = nodes.get(i);
			Node n2 = other.nodes.get(j);
			if(n1.id == n2.id){
				i++;
				j++;
			} else if(n1.id < n2.id){
				i++;
				disjoint++;
			} else{
				j++;
				disjoint++;
			}
			
			if(i >= nodes.size()) {
				excess += other.nodes.size() - j;
				break;
			} else if(j >= other.nodes.size()){
				excess += nodes.size() - i;
				break;
			}
		}
		//connection genes
		i = j = 0;
		double weightDiff = 0;
		double matching = 0;
		while(true){
			if(i >= connections.size()) {
				excess += other.connections.size() - j;
				break;
			} else if(j >= other.connections.size()){
				excess += connections.size() - i;
				break;
			}
			
			Connection c1 = connections.get(i);
			Connection c2 = other.connections.get(j);
			if(c1.id == c2.id){
				i++;
				j++;
				matching++;
				weightDiff += Math.abs(c1.weight - c2.weight); 
			} else if(c1.id < c2.id){
				i++;
				disjoint++;
			} else{
				j++;
				disjoint++;
			}
		}
		int n = (nodes.size() + other.nodes.size()) / 2;
		double result = (EXCESS_MULTIPLIER * excess / n) + (DISJOINT_MULTIPLIER * disjoint / n) + (CONNECTION_MULTIPLIER * weightDiff / (matching + .0001));
		//System.out.println("result: " + result + ", excess: " + excess + ", disjoint: " + disjoint + ", weight: " + weightDiff);
		return result;
	}
	
	//assuming THIS ONE has the higher fitness
	public NeatNet crossover(NeatNet other){
		ArrayList<Node> newNodes = new ArrayList<Node>();
		HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		ArrayList<Connection> newConnections = new ArrayList<Connection>();
		
		Random rand = new Random();
		
		
		for(Node n: nodes){
			Node n1 = new Node(n.id);
			newNodes.add(n1);
			nodeMap.put(n.id, n1);
		}
		int i=0, j=0;
		while(i<connections.size()){
			Connection c1 = connections.get(i);
			Connection c2 = (j>=other.connections.size()) ? null : other.connections.get(j);
			Connection c = null;
			if(c2==null || c1.id < c2.id){
				i++;
				c = c1;
			} else if(c1.id == c2.id){
				i++;
				j++;
				c = rand.nextBoolean() ? c1 : c2;
			}  else{
				j++;
			}
			if(c!=null){
				Connection newc = new Connection(c.id, c.weight, nodeMap.get(c.source.id), nodeMap.get(c.dest.id), c.disabled);
				newConnections.add(newc);
			}
		}
		
		return new NeatNet(inputs, outputs, newNodes, newConnections);
	}
	
	//return number of innovations made
	public int mutate(int innovationNo){
		int mutations = 0;
		Random rand = new Random();
		if(rand.nextDouble() < ADD_CONNECTION_PROBABILITY){
			//first one is only from inputs + hidden nodes
			int i1 = rand.nextInt(inputs + hiddenNodes.size());
			if(i1 >=inputs){
				i1 += outputs;
			}
			Node n1 = nodes.get(i1);
			//second one is only from outputs + hidden nodes
			Node n2 = nodes.get(rand.nextInt(outputs + hiddenNodes.size()) + inputs);
			if(!n2.connectedTo(n1) && !n1.connectedTo(n1)){
				Connection c = new Connection(innovationNo, (rand.nextDouble()*2)-1, n1, n2, false);
				connections.add(c);
			}
			mutations++;
			innovationNo++;
		} 
		if(connections.size() > 0 && rand.nextDouble() < ADD_NODE_PROBABILITY){
			Connection c = connections.get(rand.nextInt(connections.size()));
			c.disabled = true;
			Node n = new Node(innovationNo);
			nodes.add(n);
			hiddenNodes.add(n);
			Connection c1 = new Connection(innovationNo+1, 1, c.source, n, false);
			Connection c2 = new Connection(innovationNo+2, (rand.nextDouble()*2)-1, n, c.dest, false);
			connections.add(c1);
			connections.add(c2);
			
			mutations +=3;
		}
		for(Connection c: connections){
			if(rand.nextDouble()<CHANGE_PROBABILITY){
				c.weight+=(rand.nextGaussian() * MUTATION_STRENGTH);
			}
			if(rand.nextDouble()<DISABLE_PROBABILITY){
				c.disabled = !c.disabled;
			}
		}
		
		return mutations;
	}
	
	//Preferably input should be normalized
	public double[] update(double input[]){
		for(int i=0; i<inputs; i++){
			nodes.get(i).value = input[i];
		}
		
		//repeatedly update hidden nodes
		for(int i=0; i<UPDATE_ITERATIONS; i++){
			for(Node n: hiddenNodes){
				n.update();
			}
		}
		double[] output = new double[outputs];
		for(int i=0; i<outputs; i++){
			Node n = outputNodes.get(i);
			n.update();
			output[i] = n.value;
		}
		return output;
	}
	
	public int mostRecentInnovation(){
		if(connections.size()>0){
			return connections.get(connections.size()-1).id;
		} else{
			return nodes.get(nodes.size()-1).id;
		}
	}
	
	class Node{
		public int id;
		public double value;
		//connections where this node is the DESTINATION
		public ArrayList<Connection> connections;
		public Node(int id){
			this.id = id;
			this.value = 0;
			connections = new ArrayList<Connection>();
		}
		
		public void update(){
			int total = 0;
			for(Connection c: connections){
				if(!c.disabled){
					total += c.source.value * c.weight;
				}
			}
			this.value = SimMath.sigmoid(total);
		}
		
		public boolean connectedTo(Node other){
			for(Connection c: connections){
				if(c.source==other){
					return true;
				}
			}
			return false;
		}
	}
	
	class Connection{
		public int id;
		public double weight;
		public Node source;
		public Node dest;
		public boolean disabled;
		public Connection(int id, double weight, Node source, Node dest, boolean disabled){
			this.id = id;
			this.weight = weight;
			this.source = source;
			this.dest = dest;
			this.disabled = disabled;
			dest.connections.add(this);
		}
	}

	public int compareTo(NeatNet other) {
		return fitness - other.fitness;
	}

	
}
