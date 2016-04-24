import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class Main {
	public static boolean generateGrammer = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String grammarFile = "grammar1.txt";
		String updateGrammarFile = "grammar1_up.txt";
		String trainingFile = "train1.txt";
		String testFile = "test1.txt";
		app1(grammarFile, trainingFile, updateGrammarFile);
		// app2(grammarFile, trainingFile, updateGrammarFile);
		// app3(grammarFile, trainingFile, updateGrammarFile);

	}

	public static void app1(String grammarFile, String trainingFile, String updateGrammarFile) {
		if (generateGrammer) {
			ArrayList<String> terminals = new ArrayList<String>();
			terminals.add("a");
			terminals.add("b");
			createGrammars(grammarFile, 5, terminals);
		}
		app2(grammarFile, trainingFile, updateGrammarFile);
		
	}

	public static void app2(String grammarFile, String trainingFile, String updateGrammarFile) {
		ArrayList<String> terminals = new ArrayList<String>();
		ArrayList<String> nonTerminals = new ArrayList<String>();
		HashMap<String, ArrayList<String>> productionRulesLKey = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> productionRulesRKey = new HashMap<String, ArrayList<String>>();
		HashMap<String, Double> distribution = new HashMap<String, Double>();
		String start = readGrammar(grammarFile, terminals, nonTerminals, productionRulesLKey, productionRulesRKey,
				distribution);
		ArrayList<ArrayList<String>> training = readTraining(trainingFile);
		ArrayList<String> sent;
		for (int itr = 0; itr < 100; itr++) {
			HashMap<String, Double> eStep = new HashMap<String, Double>();
			for (int i = 0; i < training.size(); i++) {
				sent = training.get(i);
				Cell[][] inside = calInside(sent, productionRulesRKey, distribution);
				if (inside[0][sent.size()].getProbability(start) > 0) {
					Cell[][] outside = calOutside(sent, productionRulesLKey, distribution, inside, start);
					expectation(inside, outside, terminals, nonTerminals, productionRulesLKey, distribution, eStep, sent,
							start);
				} else {
					System.out.println(sent);
				}
				
			}
			maximization(eStep, nonTerminals, productionRulesLKey, distribution);
			saveGrammars(updateGrammarFile, terminals, nonTerminals, distribution);
			System.out.println(distribution);
		}
	}

	public static void app3(String grammarFile, String trainingFile, String updateGrammarFile) {

	}

	public static void maximization(HashMap<String, Double> eStep, ArrayList<String> nonTerminals,
			HashMap<String, ArrayList<String>> productionRulesLKey, HashMap<String, Double> distribution) {
		for (int l = 0; l < nonTerminals.size(); l++) {
			String lhs = nonTerminals.get(l);
			ArrayList<String> rightHandSide = productionRulesLKey.get(lhs);
			for (int m = 0; m < rightHandSide.size(); m++) {
				String rhs = rightHandSide.get(m);
				String rule = lhs + " -> " + rhs;
				distribution.put(rule, eStep.get(rule) / eStep.get(lhs));
			}
		}
	}

	public static void expectation(Cell[][] inside, Cell[][] outside, ArrayList<String> terminals,
			ArrayList<String> nonTerminals, HashMap<String, ArrayList<String>> productionRulesLKey,
			HashMap<String, Double> distribution, HashMap<String, Double> eStep, ArrayList<String> sent, String start) {
		for (int l = 0; l < nonTerminals.size(); l++) {
			String lhs = nonTerminals.get(l);
			double denominator = 0.0;
			for (int i = 0; i < inside.length - 1; i++) {
				for (int j = 1; j < inside.length; j++) {
					denominator = denominator + inside[i][j].getProbability(lhs) * outside[i][j].getProbability(lhs);
				}
			}
			denominator = denominator / inside[0][sent.size()].getProbability(start);
			if (eStep.containsKey(lhs)) {
				eStep.put(lhs, eStep.get(lhs) + denominator);
			} else {
				eStep.put(lhs, denominator);
			}
			// System.out.println(eStep.get(lhs));
			ArrayList<String> rightHandSide = productionRulesLKey.get(lhs);
			for (int m = 0; m < rightHandSide.size(); m++) {
				String rhs = rightHandSide.get(m);
				String[] children = rhs.split("\\s+");
				String rule = lhs + " -> " + rhs;
				double numerator = 0.0;
				double pRule = distribution.get(rule);
				for (int i = 0; i < inside.length - 1; i++) {
					if (children.length == 2) {
						for (int j = 1; j < inside.length; j++) {
							double tmp = 0.0;
							for (int k = i + 1; k <= j - 1; k++) {
								tmp = tmp + inside[i][k].getProbability(children[0])
										* inside[k][j].getProbability(children[1]);
							}
							numerator = numerator + outside[i][j].getProbability(lhs) * pRule * tmp;
						}
					} else {
						if (sent.get(i).equals(rhs)) {
							numerator = numerator
									+ outside[i][i + 1].getProbability(lhs) * inside[i][i + 1].getProbability(lhs);
						}
					}

				}
				numerator = numerator / inside[0][sent.size()].getProbability(start);
				if (eStep.containsKey(rule)) {
					eStep.put(rule, eStep.get(rule) + numerator);
				} else {
					eStep.put(rule, numerator);
				}

			}
		}
	}

	public static Cell[][] calOutside(ArrayList<String> sent, HashMap<String, ArrayList<String>> productionRulesLKey,
			HashMap<String, Double> distribution, Cell[][] inside, String start) {
		int length = sent.size();
		Cell[][] outside = new Cell[length + 1][length + 1];
		for (int i = 0; i < length; i++) {
			for (int j = 1; j < length + 1; j++) {
				outside[i][j] = new Cell();
			}
		}

		outside[0][length].addItem(start, 1.0);

		for (int len = length; len >= 2; len--) {
			for (int i = 0; i <= length - len; i++) {
				int j = i + len;
				ArrayList<String> heads = inside[i][j].getHead();
				for (int m = 0; m < heads.size(); m++) {
					String lhs = heads.get(m);
					if (productionRulesLKey.containsKey(lhs)) {
						ArrayList<String> rightHandSide = productionRulesLKey.get(heads.get(m));
						for (int n = 0; n < rightHandSide.size(); n++) {
							String rhs = rightHandSide.get(n);
							String[] children = rhs.split("\\s+");
							if (children.length == 2) {
								String rule = lhs + " -> " + rhs;
								for (int k = i + 1; k <= j - 1; k++) {
									double prob = outside[i][j].getProbability(lhs)
											* inside[k][j].getProbability(children[1]) * distribution.get(rule);
									outside[i][k].addItem(children[0], prob);

									prob = outside[i][j].getProbability(lhs) * inside[i][k].getProbability(children[0])
											* distribution.get(rule);
									outside[k][j].addItem(children[1], prob);
								}
							}
						}
					}
				}

			}
		}
		// System.out.println(outside[1][3].getProb());
		return outside;
	}

	public static Cell[][] calInside(ArrayList<String> sent, HashMap<String, ArrayList<String>> productionRulesRKey,
			HashMap<String, Double> distribution) {
		int length = sent.size();
		Cell[][] inside = new Cell[length + 1][length + 1];
		for (int i = 0; i < length; i++) {
			for (int j = 1; j < length + 1; j++) {
				inside[i][j] = new Cell();
			}
		}

		for (int i = 0; i < length; i++) {
			int j = i + 1;
			String word = sent.get(i);
			ArrayList<String> heads = productionRulesRKey.get(word);
			for (int m = 0; m < heads.size(); m++) {
				String lhs = heads.get(m);
				String rule = lhs + " -> " + word;
				double prob = distribution.get(rule);
				inside[i][j].addItem(lhs, prob);
				// System.out.println(prob + " " + rule);
			}
		}

		for (int j = 2; j < length + 1; j++) {
			for (int i = j - 2; i >= 0; i--) {
				for (int k = i + 1; k <= j - 1; k++) {
					ArrayList<String> leftChildren = inside[i][k].getHead();
					ArrayList<String> rightChildren = inside[k][j].getHead();
					for (int m = 0; m < leftChildren.size(); m++) {
						for (int n = 0; n < rightChildren.size(); n++) {
							String leftChild = leftChildren.get(m);
							String rightChild = rightChildren.get(n);
							String rhs = leftChild + " " + rightChild;
							if (productionRulesRKey.containsKey(rhs)) {
								ArrayList<String> heads = productionRulesRKey.get(rhs);
								for (int x = 0; x < heads.size(); x++) {
									String lhs = heads.get(x);
									String rule = lhs + " -> " + rhs;
									double prob = distribution.get(rule) * inside[i][k].getProbability(leftChild)
											* inside[k][j].getProbability(rightChild);
									inside[i][j].addItem(lhs, prob);
									// System.out.println(prob + " " + rule);
								}
							}
						}
					}
				}
			}
		}

		// System.out.println(inside[2][5].getProb());
		return inside;
	}

	public static ArrayList<ArrayList<String>> readTraining(String trainingFile) {
		ArrayList<ArrayList<String>> training = new ArrayList<ArrayList<String>>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(trainingFile));
			String line = in.readLine();

			while (line != null) {
				line = line.trim();
				String[] tmp = line.split("\\s+");
				ArrayList<String> wordList = new ArrayList<String>();
				for (int i = 0; i < tmp.length; i++) {
					wordList.add(tmp[i].toLowerCase());
				}
				training.add(wordList);
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return training;
	}

	public static String readGrammar(String grammarFile, ArrayList<String> terminals, ArrayList<String> nonTerminals,
			HashMap<String, ArrayList<String>> productionRulesLKey,
			HashMap<String, ArrayList<String>> productionRulesRKey, HashMap<String, Double> distribution) {
		String start = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(grammarFile));
			String line = in.readLine();
			int count = 0;
			String[] tmpStr;

			while (line != null) {
				line = line.trim();
				if (count == 0) {
					tmpStr = line.split(" ");
					for (int i = 0; i < tmpStr.length; i++) {
						terminals.add(tmpStr[i]);
					}
				} else if (count == 1) {
					tmpStr = line.split(" ");
					for (int i = 0; i < tmpStr.length; i++) {
						nonTerminals.add(tmpStr[i]);
					}
				} else if (count == 2) {
					start = line;
				} else {
					String[] tmp = line.split("\\s+");
					String lhs = tmp[1];
					String rhs = "";
					for (int i = 3; i < tmp.length; i++) {
						rhs = rhs + tmp[i] + " ";
					}
					rhs = rhs.trim();
					ArrayList<String> tmpList;
					if (productionRulesRKey.containsKey(rhs)) {
						tmpList = productionRulesRKey.get(rhs);
						tmpList.add(lhs);
					} else {
						tmpList = new ArrayList<String>();
						tmpList.add(lhs);
						productionRulesRKey.put(rhs, tmpList);
					}

					if (productionRulesLKey.containsKey(lhs)) {
						tmpList = productionRulesLKey.get(lhs);
						tmpList.add(rhs);
					} else {
						tmpList = new ArrayList<String>();
						tmpList.add(rhs);
						productionRulesLKey.put(lhs, tmpList);
					}

					distribution.put(lhs + " -> " + rhs, Double.parseDouble(tmp[0]));
				}
				line = in.readLine();
				count = count + 1;
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return start;
	}
	
	public static void saveGrammars(String grammarFile, ArrayList<String> terminals, ArrayList<String> nonTerminals, HashMap<String, Double> distribution) {
		try {
			FileWriter writer = new FileWriter(grammarFile);
			String ters = "";
			String nonTers = "";
			for (int i = 0; i < terminals.size(); i++) {
				ters = ters + terminals.get(i) + " ";
			}
			for (int i = 0; i < nonTerminals.size(); i++) {
				nonTers = nonTers + i + " ";
			}
			writer.write(ters.substring(0, ters.length() - 1) + "\n");
			writer.write(nonTers.substring(0, nonTers.length() - 1) + "\n");
			writer.write("0\n");
			
			
			Iterator iter = distribution.entrySet().iterator();
			String rule;
			double prob;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				rule = (String) entry.getKey();
				prob = (double) entry.getValue();
				writer.write(prob + " "+ rule +"\n");
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createGrammars(String grammarFile, int num, ArrayList<String> terminals) {

		try {
			FileWriter writer = new FileWriter(grammarFile);
			String ters = "";
			String nonTers = "";
			for (int i = 0; i < terminals.size(); i++) {
				ters = ters + terminals.get(i) + " ";
			}
			for (int i = 0; i < num; i++) {
				nonTers = nonTers + i + " ";
			}
			writer.write(ters.substring(0, ters.length() - 1) + "\n");
			writer.write(nonTers.substring(0, nonTers.length() - 1) + "\n");
			writer.write("0\n");
			Random r = new Random();
			double rd = 0.0;
			for (int i = 0; i < num; i++) {
				double prob = 1.0;
				for (int j = 0; j < num; j++) {
					for (int k = 0; k < num; k++) {
						rd = r.nextDouble();
						
						if (j == num - 1 && k == num - 1) {
							rd = prob;
						} else {
							while (prob <= rd) {
								rd = rd / 10.0;
							}
							prob = prob - rd;
						}
						
						writer.write(rd + " " + i + " -> " + j + " " + k + "\n");
						
						//writer.write();
					}
				}
				prob = 1.0;
				for (int j = 0; j < terminals.size(); j++) {
					rd = r.nextDouble();
					
					if (j == terminals.size()) {
						rd = prob;
					} else {
						while (prob <= rd) {
							rd = rd / 10.0;
						}
						prob = prob - rd;
					}
					
					writer.write(rd + " " + i + " -> " + terminals.get(j) + "\n");
				}
				
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
