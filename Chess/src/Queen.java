/**
 * Queen constructor which can generate a moveset given a position of a Queen relative to other pieces on a Board
 * @author Allen Jue
 * 6/12/2022
 */

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
	
	/**
	 * Constructor for a Queen in row
	 * @param pType type of Piece (Queen)
	 * @param row location
	 */
	public Queen(int pType, int row, int col) {
		super(pType, row, col);
	}

	/**
	 * Constructor that creates a Queen object by copying a Piece p
	 * @param p Piece to be copied
	 */
	public Queen(Piece p) {
		super(p);
	}

	/**
	 * Constructor for a specific type at row and col
	 * @param pType
	 * @param row
	 * @param col
	 */
	public Queen(char pType, int row, int col) {
		super(pType, row, col);
	}
	
	/**
	 * Gets the valid moveset for a Queen. Looks diagonally in four directions 
	 * and laterally in four directions
	 * @param b the functional Board the queen is on
	 * @param p Queen piece selected
	 * @param i row of p
	 * @param j column of p
	 */
	@Override
	public List<int[]> getMoves(Board b, Piece p, int i, int j) {
		List<int[]> moves = new ArrayList<>();
		if(correctTurn(b)) {
			for(int k = 0; k < DIAGONAL_DIR.length - 1; k++) {
				getDirMoves(b, moves, p, DIAGONAL_DIR[k], DIAGONAL_DIR[k + 1], i, j);
				getDirMoves(b, moves, p, LATERAL_DIR[k], LATERAL_DIR[k + 1], i, j);
			}
		}
		return moves;
	}

}
