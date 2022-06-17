/**
 * Rook constructor which can generate a moveset given a position of a Rook relative to other pieces on a Board
 * @author Allen Jue
 * 6/12/2022
 */

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
	private boolean castlingRights;
	
	/**
	 * Constructor for a Rook given a type and row
	 * @param pType type of Piece (Rook)
	 * @param row location
	 */
	public Rook(int pType, int row, int col) {
		super(pType, row, col);
		castlingRights = true;
	}

	/**
	 * Constructor for a copy of a Rook given an existing piece
	 * @param p piece to be copied
	 */
	public Rook(Piece p) {
		super(p);
	}

	/**
	 * Gets the valid moveset for a rook at row i and column j
	 * @param b functional board containing the piece
	 * @param i row of Rook
	 * @param j column of Rook
	 */
	@Override
	public List<int[]> getMoves(Board b, Piece p, int i, int j) {
		List<int[]> moves = new ArrayList<>();
		if(correctTurn(b)) {
			for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
				getDirMoves(b, moves, p, LATERAL_DIR[k], LATERAL_DIR[k + 1], i, j);
			}
		}
		return moves;
	}
	
	/**
	 * Sets castling rights to false
	 */
	public void removeCastlingRights() {
		castlingRights = false;
	}
	
	/**
	 * Returns true if the rook has not moved from its starting position
	 * @return castlingRights
	 */
	public boolean getCastlingRights() {
		return castlingRights;
	}
}
