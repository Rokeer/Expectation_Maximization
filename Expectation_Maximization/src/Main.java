import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String grammarFile = "grammar2.txt";
		String trainingFile = "train2";
		String testFile = "test2";
		// app1(grammarFile, trainingFile, testFile);
		app2(grammarFile, trainingFile, testFile);
		// app3(grammarFile, trainingFile, testFile);

	}

	public static void app1(String grammarFile, String trainingFile, String testFile) {
		String[] alphabet = { "a", "b" };
		createPalindromesGrammar(grammarFile, 2, alphabet);
	}

	public static void app2(String grammarFile, String trainingFile, String testFile) {
		ArrayList<String> terminals = new ArrayList<String>();
		ArrayList<String> nonTerminals = new ArrayList<String>();
		HashMap<String, ArrayList<String>> productionRules = new HashMap<String, ArrayList<String>>();
		HashMap<String, Double> distribution = new HashMap<String, Double>();
		String start = readGrammar(grammarFile, terminals, nonTerminals, productionRules, distribution);
		
		String trainingCase= "A friend visited a friend with a friend";
		
		
	}

	public static void app3(String grammarFile, String trainingFile, String testFile) {

	}

	public static String readGrammar(String grammarFile, ArrayList<String> terminals, ArrayList<String> nonTerminals,
			HashMap<String, ArrayList<String>> productionRules, HashMap<String, Double> distribution) {
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
					if (productionRules.containsKey(rhs)) {
						tmpList = productionRules.get(rhs);
						tmpList.add(lhs);
					} else {
						tmpList = new ArrayList<String>();
						tmpList.add(lhs);
						productionRules.put(rhs, tmpList);
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
			writer.write("  I like you and miss you¡£");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
