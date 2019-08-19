/*
 * Aakash Dutt
 * LeHer_Analytical: Creates a probability matrix for P1 winning based on the maximum card P1 and P2 will keep in the card game LeHer.
 */

import java.util.*;
import java.io.*;
public class LeHer_Simplified {

	private int cardSets;
	private int cards;
	private Fraction[][] winProbability;
	private ArrayList<ArrayList<Integer>> solution;
	
	public static void main(String[] args) {
	
		/*
		// Specify cardSets and cards as shown in the constructor; public LeHer_Analytical(int cardSets, int cards); default is 4, 13 if unspecified
		LeHer_Analytical leHer = new LeHer_Analytical(4, 13);
		
		// Do test.print()) to print out everything important to console
		System.out.println(leHer.domMatrix());
		
		// Do leHer.exportExcelAll() to generate a txt file named 'LeHerMatrixReadable_*cards*cards_*cards*Decks' in an easier to read format
		// This test file will be located in the project folder, though won't be visible to eclipse or most IDEs. 
		//leHer.exportAll();
		
		// Do leHer.exportExcel() to generate a txt file named 'LeHerMatrix_*cards*cards_*cards*Decks' that can be easily imported by excel
		// This test file will be located in the project folder, though won't be visible to eclipse or most IDEs. 
		//leHer.exportExcel();
		*/
		
	}

	public LeHer_Simplified(int cardSets, int cards) {

		this.cardSets = cardSets;
		this.cards = cards;
		winProbability = new Fraction[cards+1][cards+1];
		solveMatrix();
		solution = strictlyDominantStrategy();

	}
	
	public LeHer_Simplified() {
		
		this(4, 13); // Default version of the game with a standard deck
		
	}

	// Finds P1's win probability of each individual spot in order to fill the matrix in O(n^3) (original algorithm works in O(n^4))
	private void solveMatrix() {
		
		for(int row = 0; row<winProbability.length; row++)
			for(int col = 0; col<winProbability.length; col++)
				solveProbability(row, col);
			
	}
	
	// Finds the probability of P1 winning given the max value that P1 will keep and the max value that P2 will keep (Simplified from original O(n^2) formula to O(n)
	private void solveProbability(int maxValP1, int maxValP2) {
		
		final int numCards = cardSets*cards;
		long numerator = 0L;
		long denominator = numCards * (numCards-1) * (numCards-2);

		// When both players swap their cards (P1's Card is less than P1's max value, so P1 swaps)
		// O(n)
		
		for(int P1Card = 1; P1Card<=maxValP1; P1Card++)
			if(P1Card+1<=cards-1)
				numerator += ((long)cardSets*cardSets)*((long)((cards-P1Card-1)*cardSets*(P1Card+cards)*0.5)+P1Card-cards+1);			
		
		// When P1 Swaps and P2 doesn't Swap
		// O(n)
		long sum = 0;
		
		// P1Card < P2Card
		for(int P1Card = maxValP1+1; P1Card<=cards; P1Card++)
			if(P1Card+1<=maxValP2)
				sum += ((long)(maxValP2-P1Card)) * (long)(cardSets * cardSets * (P1Card-1));
		
		// P1Card == P2Card
		for(int P1Card = maxValP1+1; P1Card<=cards; P1Card++)
			if(P1Card<=maxValP2)
				sum += ((long)(cardSets-1))*((long)(cardSets*(P1Card-1)));
		
		// P1Card > P2Card && P1Card!=cards
		for(int P1Card = maxValP1+1; P1Card<=cards-1; P1Card++)
			sum += Math.min(maxValP2, P1Card-1)*((long)cardSets*(cardSets*(P1Card-1) + cardSets - 1));
		
		// P1Card > P2Card && P1Card==cards
		for(int P1Card = Math.max(cards, maxValP1+1); P1Card<=cards; P1Card++)
			sum += Math.min(maxValP2, P1Card-1)*((long)(cardSets*(cardSets*(P1Card-1)+cardSets-2)));
		
		numerator += sum*cardSets;

		// When neither players swap
		// O(n)
		sum = 0;
		
		for(int P1Card = maxValP1+1; P1Card <=cards; P1Card++) {
			
			long toAdd = P1Card-maxValP2-1;
			
			if(toAdd>0)
				sum += toAdd;
			
		}
		
		numerator += sum * (long)(cardSets*cardSets*(numCards-2));
		winProbability[maxValP1][maxValP2] = new Fraction(numerator, denominator);
		
	}
	
	// Returns strictly dominant strategies for both players (P1 strategies in ArrayList.get(0), P2 in ArrayList.get(1))
	private ArrayList<ArrayList<Integer>> strictlyDominantStrategy() {

		ArrayList<Integer> dominantRowsP1 = new ArrayList<Integer>();
		ArrayList<Integer> dominantRowsP2 = new ArrayList<Integer>();

		for(int i = 0; i<=cards; i++) {

			dominantRowsP1.add(i);
			dominantRowsP2.add(i);

		}

		return strictlyDominantStrategyHelper(dominantRowsP1, dominantRowsP2);

	}
	
	// Recursive method that keeps narrowing down dominant strategies for each playeruntil they can't be narrowed down.
	private ArrayList<ArrayList<Integer>> strictlyDominantStrategyHelper(ArrayList<Integer> dominantRowsP1, ArrayList<Integer> dominantColsP2) {
		
		ArrayList<Integer> newDomRowsP1 = new ArrayList<Integer>();
		
		for(int curRow: dominantRowsP1)
			if(!checkRows(curRow, dominantRowsP1, dominantColsP2))
				newDomRowsP1.add(curRow);

		
		dominantRowsP1 = newDomRowsP1;
		ArrayList<Integer> newDomColsP2 = new ArrayList<Integer>();	
		
		for(int curCol: dominantColsP2)
			if(!checkCols(curCol, dominantRowsP1, dominantColsP2))
				newDomColsP2.add(curCol);
		
		/*
		* If dominantColsP2's size doesn't change, nothing has been narrowed down and nothing will change.
		* This check isn't done for dominantRowsP1 because when the method is initially called it's possible
		* that nothing will be narrowed down for dominantRowsP1, but something will be narrowed down for
		* dominantColsP2.
		*/
		
		if(newDomColsP2.size() == dominantColsP2.size()) {
			
			ArrayList<ArrayList<Integer>> toReturn = new ArrayList<ArrayList<Integer>>();
			toReturn.add(new ArrayList<Integer>());
			toReturn.add(new ArrayList<Integer>());
			toReturn.get(0).addAll(dominantRowsP1);
			toReturn.get(1).addAll(newDomColsP2);
			return toReturn;
			
		}
		
		return strictlyDominantStrategyHelper(dominantRowsP1, newDomColsP2);

	}
	
	// Checks a row for strict dominance by any other row
	private boolean checkRows(int curRow, ArrayList<Integer> dominantRowsP1, ArrayList<Integer> dominantColsP2) {
		
		for(int comparingRow: dominantRowsP1)
			if(rowDominated(curRow, comparingRow, dominantColsP2))
				return true;
		
		return false;
	
	}
	
	// Checks a col for strict dominance by any other row
	private boolean checkCols(int curCol, ArrayList<Integer> dominantRowsP1, ArrayList<Integer> dominantColsP2) {
		
		for(int comparingCol: dominantColsP2)
			if(colDominated(curCol, comparingCol, dominantRowsP1))
				return true;
		
		return false;
		
	}
	
	// Returns true if the row is strictly dominated by another given row
	private boolean rowDominated(int curRow, int comparingRow, ArrayList<Integer> dominantColsP2) {
		
		boolean containsLesserValue = false;
		
		for(int col: dominantColsP2) {
			
			if(winProbability[curRow][col].multiple()>winProbability[comparingRow][col].multiple())
				return false;
			
			// Accounts for if all numbers are equal but curRow contains one number that is less, making it strictly dominated
			else if(winProbability[curRow][col].multiple()!=winProbability[comparingRow][col].multiple())
				containsLesserValue = true;
				
		}
		
		return containsLesserValue;
		
	}

	// Returns true if the col is strictly dominated by another given col
	private boolean colDominated(int curCol, int comparingCol, ArrayList<Integer> dominantRowsP1) {
		
		boolean containsGreaterValue = false;
		
		for(int row: dominantRowsP1) {
			
			if(winProbability[row][curCol].multiple()<winProbability[row][comparingCol].multiple())
				return false;
			
			// Accounts for if all numbers are equal but curCol contains one number that is greater, making it strictly dominated
			else if(winProbability[row][curCol].multiple()!=winProbability[row][comparingCol].multiple())
				containsGreaterValue = true;
			
		}
		
		return containsGreaterValue;
		
	}
	
	// Prints matrix in an easy to read manner
	private String matrixString(ArrayList<Integer> rows, ArrayList<Integer> cols) {
		
		String toReturn = "\t";

		for(int i: cols)
			toReturn += "\t" + i;

		toReturn += "\tMax Value P2 Will Keep";
		toReturn += "\n";
		
		for(int i = 0; i<rows.size()+2; i++)
			toReturn += "--------";
		
		toReturn += "\n";

		// O(n^2)
		for(int row: rows) {

			toReturn += (row) + "\t|";

			for(int col: cols)
				toReturn += "\t" + winProbability[row][col].multiple();

			toReturn += "\n";

		}
		
		return toReturn + "Max Value P1 Will Keep\n";
		
	}

	// Prints matrix in a format that can easily be imported by excel if it is saved in a text file
	private String matrixStringExcel(ArrayList<Integer> rows, ArrayList<Integer> cols) {
	
		String toReturn = "";

		for(int i: cols)
			toReturn += " " + i;

		toReturn += " MaxValueP2WillKeep\n";

		// O(n^2)
		for(int row: rows) {

			toReturn += (row);

			for(int col: cols)
				toReturn += " " + winProbability[row][col].multiple();

			toReturn += "\n";
			
		}
		
		return toReturn + "MaxValueP1WillKeep\n";
		
	}
	
	// Returns strategies that aren't strictly dominated
	public String domStrat() {
		
		String toReturn = ("P1 Dominant Strategies: ");
		
		for(int P1DominantStrategy: solution.get(0))
			toReturn += (" " + (P1DominantStrategy));
		
		toReturn += "\nP2 Dominant Strategies: ";
		
		for(int P2DominantStrategy: solution.get(1))
			toReturn += " " + (P2DominantStrategy);
			
		return toReturn + "\n";
		
	}
	
	// Returns ArrayList with all indices of an array of a certain size
	private ArrayList<Integer> getIndices(int size){
		
		ArrayList<Integer> toReturn = new ArrayList<Integer>();
		
		for(int i = 0; i<size; i++)
			toReturn.add(i);
		
		return toReturn;
		
	}
	
	// Prints full matrix in easy to read format
	public String fullMatrix() {
		
		return matrixString(getIndices(winProbability.length), getIndices(winProbability[0].length));
		
	}
	
	// Prints full matrix in format easy to import by excel in .txt format
	public String fullMatrixExcel() {
		
		return matrixStringExcel(getIndices(winProbability.length), getIndices(winProbability[0].length));
		
	}
	
	// Prints dominant matrix in easy to read format
	public String domMatrix() {
		
		return matrixString(solution.get(0), solution.get(1));
		
	}
	
	// Prints dominant matrix in format easy to import by excel in .txt format
	public String domMatrixExcel() {
		
		return matrixStringExcel(solution.get(0), solution.get(1));
		
	}
	
	public String toString() {
		
		return fullMatrixExcel() + "\n\n" + domMatrixExcel() + "\n\n" + fullMatrix() + "\n\n" + domMatrix() + "\n\n" + domStrat();
		
	}
	
	public void print() {
		
		System.out.print(this.toString());
		
	}

	public void exportExcel() {
		
		export("LeHerMatrix_" + cards + "cards" + cardSets + "cardSets.txt" , fullMatrixExcel() + "\n\n" + domMatrixExcel());
		
	}
	
	public void exportAll() {
		
		export("LeHerMatrixReadable_" + cards + "cards" + cardSets + "cardSets.txt" , toString());
		
	}
	
	private void export(String fname, String toExport) {

		FileWriter fileOut = null;
		
		try {
			
			fileOut = new FileWriter(new File(fname));
			fileOut.write(toExport);
			fileOut.close();
			
		} catch (IOException e) {

			System.out.println("File Error, file has not been generated");
			
		}
		
		
	}
	
	class Fraction {
		
		private long num;
		private long den;
		
		public Fraction(long numerator, long denominator) {
			
			num = numerator;
			den = denominator;
			
		}
		
		public Fraction() {
		
			this(0L, 1L);
			
		}
		
		// Brings fraction down to its most simple form
		public void simplify() {
			
			if(den<0) {
				
				num*=-1;
				den*=-1;
				
			}
			
			long gcf = gcf();
			
			// Keeps on dividing the numerator and denominator by GCF until the GCF is 1
			while(gcf!=1) {
				
				num /= gcf;
				den /= gcf;
				gcf = gcf();
				
			}
			
		}
		
		// Finds gcf of numerator and denominator
		private long gcf() {
			
			long minValue = Math.min(Math.abs(num), Math.abs(den));
			
			for(long i = minValue; i>1; i--)
				if(num%i==0 && den%i==0)
					return i;
			
			return 1;
			
		}
		
		// Returns the fraction in decimal/double form.
		public double toDecimal() {
			
			return ((double)num)/den;
			
		}
		
		// Returns fraction multiplied by a multiple of the denominator of every value in the matrix. Then does 2(num*multiple) - multiple 
		public long multiple() {
			
			long numCards = cardSets*cards;
			long multiple = (numCards)*(numCards-1)*(numCards-2);
			multiple /= Math.pow(2, 3);
			long toReturn = ((long)(num * (((double)multiple)/den)));
			return toReturn*2-multiple;
			
		}
		
		// Returns string fraction in typical fraction format
		public String toString() {
			
			return num + "/" + den;
			
		}
		
	}
	
}
