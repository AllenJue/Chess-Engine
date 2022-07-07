/**
 * Knight constructor which can generate a moveset given a position of a Knight relative to other pieces on a Board
 * @author Allen Jue
 * 6/12/2022
 */

import java.util.List;

public class Knight extends Piece {
	
	/**
	 * Constructor for a new knight piece in the given row
	 * @param pType
	 * @param row
	 */
	public Knight(int pType, int row, int col) {
		super(pType, row, col);
	}

	/**
	 * Constructor for a knight piece copying the fields of an existing piece
	 * @param p piece to be copied
	 */
	public Knight(Piece p) {
		super(p);
	}
	
	/**
	 * Constructor for a specific type at row and col
	 * @param pType
	 * @param row
	 * @param col
	 */
	public Knight(char pType, int row, int col) {
		super(pType, row, col);
	}

	/**
	 * Get moves for a knight. Need to get the L shape, which is essentially moving in the lateral directions
	 * 2 times and then in the other direction once. 
	 * @param b Board with pieces
	 * @param p current piece
	 * @param i current piece row
	 * @param j current piece column
	 */
	@Override
	public List<int[]> getMoves(Board b, List<int[]> moves, Piece p, int i, int j) {
		if(correctTurn(b) ) {
			for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
				getDirMoves(b, moves, p, i + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, j - 1);
				getDirMoves(b, moves, p, i + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, j + 1);
				getDirMoves(b, moves, p, i - 1, j + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2);
				getDirMoves(b, moves, p, i + 1, j + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2);
			}
		}
		return moves;
	}

	/**
	 * Get the moves given a direction change for a knight
	 * @param b board with pieces
	 * @param m moves is list of valid moves
	 * @param rowChange change in row from i
	 * @param colChange change in columns from j
	 */
	public void getDirMoves(Board b, List<int[]> moves, Piece p, int rowChange, int colChange) {
		if(b.inBounds(rowChange, colChange) 
				&& (b.isEmpty(rowChange, colChange) || diffColor(b.getPiece(rowChange, colChange), p))
				&& b.kingSafeWithMove(p.isWhite(), p.getRow(), p.getCol(), rowChange, colChange)) {
			moves.add(new int[] {rowChange, colChange});
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Knight) {
			Knight p = (Knight)obj;
			return p.getInitialR() == this.getInitialR() && p.getInitialC() == this.getInitialC();
		}
		return false;
	}
}
