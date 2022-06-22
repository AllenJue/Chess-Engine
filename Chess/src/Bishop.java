/**
 * Bishop constructor which can generate a moveset given a position of a Bishop relative to other pieces on a Board
 * @author Allen Jue
 * 6/12/2022
 */

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
	
	/**
	 * Constructor for a Bishop object in row
	 * @param pType type of Piece (Bishop)
	 * @param row location
	 */
	public Bishop(int pType, int row, int col) {
		super(pType, row, col);
	}

	/**
	 * Constructor for a copy of a Bishop given a Piece p
	 * @param p piece to be copied
	 */
	public Bishop(Piece p) {
		super(p);
	}
	
	/**
	 * Constructor for a specific type at row and col
	 * @param pType
	 * @param row
	 * @param col
	 */
	public Bishop(char pType, int row, int col) {
		super(pType, row, col);
	}
	
	/**
	 * Gets the valid bishop moves if it is the correct turn
	 * @param p bishop selected
	 * @param i current row
	 * @param j current column
	 * @return a list of coordinates of the valid bishop moves
	 */
	public List<int[]> getMoves (Board b, Piece p, int i, int j) {
		List<int[]> moves = new ArrayList<>();
		if(correctTurn(b)) {
			// bishops can move diagonally until another piece to capture or the end of the board
			for(int k = 0; k < DIAGONAL_DIR.length - 1; k++) {
				getDirMoves(b, moves, p, DIAGONAL_DIR[k], DIAGONAL_DIR[k + 1], i, j);
			}
		}
		return moves;
	}
}
