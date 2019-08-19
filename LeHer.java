import java.util.ArrayList;

/*
 * Aakash Dutt and Agustin Jauregui
 * LeHer: Creates a probability matrix winProbability[][] for the chance of P1 winning the game given a maxValP1 and maxValP2
 */

public class LeHer {

	private int cards;
	private int cardSets;
	private Fraction[][] winProbability;
	
	public LeHer(int cardSets, int cards) {

		this.cardSets = cardSets;
		this.cards = cards;
		winProbability = new Fraction[cards+1][cards+1];
		solveMatrix();

	}

	// Finds P1's win probability of each possible value of maxValP1 and maxValP2 [0, cards]
	public void solveMatrix() {

		for(int row = 0; row<=cards; row++)
			for(int col = 0; col<=cards; col++)
				solveProbability(row, col);

	}

	// Finds the probability of P1 winning given the max value that P1 will keep and the max value that P2 will keep
	public void solveProbability(int maxValP1, int maxValP2) {

		// Separates out numerator and denominator for more accurate calculation at the end
		final int numCards = cardSets*cards;
		int numerator = 0;
		int denominator = numCards * (numCards-1) * (numCards-2);

		// Case 1
		for(int P1Card = 1; P1Card<=maxValP1; P1Card++)
			for(int P2Card=1; P2Card<=cards; P2Card++) 
				if(P1Card<P2Card && P2Card<cards)
					numerator += cardSets*cardSets*((cardSets*(P2Card-1))-1+cardSets);

		// Case 2
		for(int P1Card = maxValP1+1; P1Card<=cards; P1Card++) {
			for(int P2Card=1; P2Card<=maxValP2; P2Card++) {

				int prob = cardSets;

				if(P1Card==P2Card)
					prob*=cardSets-1;

				else
					prob*=cardSets;

				if(P1Card>P2Card) {

					if(P1Card==cards)
						prob *= cardSets*(P1Card-1) + (cardSets-2);

					else
						prob *= cardSets*(P1Card-1) + (cardSets-1);

				}

				else {

					prob *= cardSets*(P1Card-1);

				}

				numerator += prob;

			}

		}	

		// Case 3
		for(int P1Card = maxValP1+1; P1Card<=cards; P1Card++)
			for(int P2Card = maxValP2+1; P2Card<=cards; P2Card++)
				if(P1Card>P2Card)
					numerator += cardSets*cardSets*(numCards-2);

		winProbability[maxValP1][maxValP2] = new Fraction(numerator, denominator);

	}

	// Prints matrix in an easy to read manner
	public String toString() {

		String toReturn = "\t";

		for(int i = 0; i<=cards; i++)
			toReturn += "\t" + i;

		toReturn += "\tMax Value P2 Will Keep";
		toReturn += "\n";

		for(int i = 0; i<cards+2; i++)
			toReturn += "--------";

		toReturn += "\n";

		for(int row = 0; row<=cards; row++) {

			toReturn += (row) + "\t|";

			for(int col = 0; col<=cards; col++)
				toReturn += "\t" + winProbability[row][col].multiple(); 
			// Use winProbability[row][col].toDecimal() to see decimal values

			toReturn += "\n";

		}

		return toReturn + "Max Value P1 Will Keep\n";

	}

	// Returns strictly dominant strategies for both players (P1 strategies in ArrayList.get(0), P2 in ArrayList.get(1))
	public ArrayList<ArrayList<Integer>> strictlyDominantStrategy() {

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

	class Fraction {

		private int num;
		private int den;

		public Fraction(int numerator, int denominator) {

			num = numerator;
			den = denominator;

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

	}

}
