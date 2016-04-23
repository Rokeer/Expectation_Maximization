import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String grammarFile = "grammar.txt";
		String trainingFile = "train.txt";
		String testFile = "test.txt";
		app1(grammarFile, trainingFile, testFile);
		// app2(grammarFile, trainingFile, testFile);
		// app3(grammarFile, trainingFile, testFile);

	}

	public static void app1(String grammarFile, String trainingFile, String testFile) {
		// String[] alphabet = { "a", "b" };
		// createPalindromesGrammar(grammarFile, 2, alphabet);

		ArrayList<String> terminals = new ArrayList<String>();
		ArrayList<String> nonTerminals = new ArrayList<String>();
		HashMap<String, ArrayList<String>> productionRulesLKey = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> productionRulesRKey = new HashMap<String, ArrayList<String>>();
		HashMap<String, Double> distribution = new HashMap<String, Double>();
		String start = readGrammar(grammarFile, terminals, nonTerminals, productionRulesLKey, productionRulesRKey,
				distribution);
		ArrayList<ArrayList<String>> training = readTraining(trainingFile);
		ArrayList<String> sent;
		for (int i = 0; i < training.size(); i++) {
			sent = training.get(i);
			Cell[][] inside = calInside(sent, productionRulesRKey, distribution);
			Cell[][] outside = calOutside(sent, productionRulesLKey, distribution, inside, start);
		}

		// System.out.println(productionRulesLKey);
	}

	public static void app2(String grammarFile, String trainingFile, String testFile) {

	}

	public static void app3(String grammarFile, String trainingFile, String testFile) {

	}

	public static Cell[][] calOutside(ArrayList<String> sent,
			HashMap<String, ArrayList<String>> productionRulesLKey, HashMap<String, Double> distribution,
			Cell[][] inside, String start) {
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
								String rule = lhs + "->" + rhs;
								for (int k = i + 1; k <= j - 1; k++) {
									double prob = outside[i][j].getProbability(lhs)
											* inside[k][j].getProbability(children[1]) * distribution.get(rule);
									outside[i][k].addItem(children[0], prob);
									
									prob = outside[i][j].getProbability(lhs)
											* inside[i][k].getProbability(children[0]) * distribution.get(rule);
									outside[k][j].addItem(children[1], prob);
								}
							}
						}
					}
				}

				
			}
		}
		System.out.println(outside[1][3].getProb());
		return outside;
	}

	public static Cell[][] calInside(ArrayList<String> sent,
			HashMap<String, ArrayList<String>> productionRulesRKey, HashMap<String, Double> distribution) {
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
				String rule = lhs + "->" + word;
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
									String rule = lhs + "->" + rhs;
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
					wordList.add(tmp[i]);
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

					distribution.put(lhs + "->" + rhs, Double.parseDouble(tmp[0]));
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

	public static void createPalindromesGrammar(String grammarFile, int num, String[] alphabet) {

		try {
			FileWriter writer = new FileWriter(grammarFile);
			String tmp = "";
			for (int i = 0; i < alphabet.length; i++) {
				tmp = tmp + alphabet[i] + " ";
			}
			writer.write(tmp.substring(0, tmp.length() - 1) + "\n");
			writer.write("Hello Kuka:\n");
			writer.write("  My name is coolszy!\n");
			writer.write("  I like you and miss you��");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
