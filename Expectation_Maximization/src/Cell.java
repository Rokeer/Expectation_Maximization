import java.util.ArrayList;
import java.util.HashMap;

public class Cell {
	ArrayList<String> head = new ArrayList<String>();
	HashMap<String, Double> prob = new HashMap<String, Double>();

	public Cell() {

	}

	public void addItem(String sHead, double dProb) {
		if (prob.containsKey(sHead)) {
			prob.put(sHead, prob.get(sHead) + dProb);
		} else {
			head.add(sHead);
			prob.put(sHead, dProb);
		}

	}
	
	public double getProbability(String sHead) {
		if (prob.containsKey(sHead)) {
			return prob.get(sHead);
		} else {
			return 0.0;
		}
		
	}

	public ArrayList<String> getHead() {
		return head;
	}

	public void setHead(ArrayList<String> head) {
		this.head = head;
	}

	public HashMap<String, Double> getProb() {
		return prob;
	}

	public void setProb(HashMap<String, Double> prob) {
		this.prob = prob;
	}

}
