import java.io.*;
import java.util.*;

public class LeHer_Simulation {

	private int cardSets;
	private int cards;
	private int runs;
	private double[][] winProbability;
	private ArrayList<ArrayList<Integer>> solution;
	
	public LeHer_Simulation(int cardSets, int cards, int runs) {

		this.cardSets = cardSets;
		this.cards = cards;
		this.runs = runs;
		winProbability = new double[cards+1][cards+1];
		solveMatrix();
		solution = strictlyDominantStrategy();

	}
	
	public void solveMatrix() {
		
		for(int row = 0; row<winProbability.length; row++)
			for(int col = 0; col<winProbability.length; col++)
				solveProbability(row, col);
	
	}
	
	public void solveProbability(int maxValP1, int maxValP2) {
		
		int P1Wins = 0;
		final int numCards = cards*cardSets;
		
		for(int numSims = 0; numSims<runs; numSims++) {
			
			int[] drawnCards = new int[3]; // drawnCards[0] is P1Card, drawnCards[1] is P2Card, and drawnCards[2] is deckCard
			
			// Generates each card
			for(int i = 0; i<3; i++) {
				
				// Reduces largest possible val by 1 for each turn to account for losing a card
				drawnCards[i] = (int)(Math.random()*(numCards-i))+1; 
				
				// Skips over previously drawn cards to simulate them not existing anymore
				for(int pastCards = 0; pastCards<i; pastCards++)
					if(drawnCards[i]>=drawnCards[pastCards])
						drawnCards[i]++;
			
				// We use numCards%cards to assign each specific card a real value.
				drawnCards[i] = drawnCards[i]%cards;
				
				// numCards%cards produces a range of [0, cards-1]. We make 0 = cards, so the result is [1, cards], all cards in a deck
				if(drawnCards[i]==0)
					drawnCards[i] = cards;
				
			}

			if(P1Winner(drawnCards, maxValP1, maxValP2))
				P1Wins++;
			
		}
		
		//double probability = ((double)P1Wins)/runs;
		//int round = Math.round((int)(probability*1000));
		//winProbability[maxValP1][maxValP2] = round/1000.0;
		winProbability[maxValP1][maxValP2] = ((double)P1Wins)/runs;
		
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
			
			if(winProbability[curRow][col]>winProbability[comparingRow][col])
				return false;
			
			// Accounts for if all numbers are equal but curRow contains one number that is less, making it strictly dominated
			else if(winProbability[curRow][col]!=winProbability[comparingRow][col])
				containsLesserValue = true;
				
		}
		
		return containsLesserValue;
		
	}

	// Returns true if the col is strictly dominated by another given col
	private boolean colDominated(int curCol, int comparingCol, ArrayList<Integer> dominantRowsP1) {
		
		boolean containsGreaterValue = false;
		
		for(int row: dominantRowsP1) {
			
			if(winProbability[row][curCol]<winProbability[row][comparingCol])
				return false;
			
			// Accounts for if all numbers are equal but curCol contains one number that is greater, making it strictly dominated
			else if(winProbability[row][curCol]!=winProbability[row][comparingCol])
				containsGreaterValue = true;
			
		}
		
		return containsGreaterValue;
		
	}
	
	// Returns true if P1 Wins
	private boolean P1Winner(int[] drawnCards, int maxValP1, int maxValP2) {
		
		// Case 1: P1 exchanges with P2
		if(drawnCards[0]<=maxValP1) {
			
			// P1 Loses if the trade results in a lesser number than P1Card or the trade is invalidated
			if(drawnCards[0]>=drawnCards[1] || drawnCards[1]==cards)
				return false;
			
			// Or else P1 will swap (will have P2Card) and P2 will try to swap (will have deckCard)
			return drawnCards[1]>drawnCards[2] || drawnCards[2] == cards;
			
		}
		
		// Case 2: P1 Keeps P1Card, P2 Exchanges (gets deckCard)
		if(drawnCards[1]<=maxValP2 && drawnCards[2]!=cards)
			return drawnCards[0]>drawnCards[2];

		
		// Case 3: Nobody swaps (Also covers failed Case 2 swaps)
		return drawnCards[0]>drawnCards[1];
			
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
				toReturn += "\t" + winProbability[row][col];

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
				toReturn += " " + winProbability[row][col];

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
	
}
