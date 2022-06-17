/**
 * King constructor which can generate a moveset given a position of a King relative to other pieces on a Board
 * TODO: manage checks
 * TODO: castling rights
 * @author Allen Jue
 * 6/12/2022
 */

import java.util.List;
import java.util.ArrayList;

public class King extends Piece {
	private boolean castlingRights;
	private final int[][] CASTLING_ROOK_POS = {
			{0, 0},
			{0, 7},
			{7, 0},
			{7, 7}
	};
	
	/**
	 * Constructor for a King given a type and row
	 * @param pType type of Piece (King)
	 * @param row location
	 */
	public King(int pType, int row, int col) {
		super(pType, row, col);
		castlingRights = true;
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor for a copy of a King given an existing piece
	 * @param p piece to be copied
	 */
	public King(Piece p) {
		super(p);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the valid moveset for a King. Looks diagonally in four directions 
	 * and laterally in four directions
	 * @param b the functional Board the King is on
	 * @param p King piece selected
	 * @param i row of p
	 * @param j column of p
	 */
	@Override
	public List<int[]> getMoves(Board b, Piece p, int i, int j) {
		List<int[]> moves = new ArrayList<>();
		if(correctTurn(b)) {
			for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
				getDirMoves(b, moves, p, i + LATERAL_DIR[k], j + LATERAL_DIR[k + 1]);
				getDirMoves(b, moves, p, i + DIAGONAL_DIR[k], j + DIAGONAL_DIR[k + 1]);
			}
			if(castlingRights && b.isSafe(p.isWhite(), i, j)) {
				if(p.isWhite()) {
					castleMoveHelper(b, moves, p, i, j, 2);

				} else {
					castleMoveHelper(b, moves, p, i, j, 0);
				}

			}
		}
		return moves;
	}
	
	/**
	 * Gets the castling moves for a king. 
	 * @param b functional board
	 * @param moves list of valid moves
	 * @param p King piece
	 * @param i row of King
	 * @param j column of king
	 * @param rook_pos where the queenside and kingside rooks are located
	 */
	private void castleMoveHelper(Board b, List<int[]> moves, Piece p, int i, int j, int rook_pos) {
		if(sameRook(b, p.isWhite(), CASTLING_ROOK_POS[rook_pos]) 
				&& castlingSquaresSafe(b, p.isWhite(), i, j, -1)) {
			// queen side castle
			moves.add(new int[] {i, j - 2});
		} 
		if(sameRook(b, p.isWhite(), CASTLING_ROOK_POS[rook_pos + 1])
			&& castlingSquaresSafe(b, p.isWhite(), i, j, 1)) {
			// King side castle
			moves.add(new int[] {i, j + 2});
		}
	}
	
	/**
	 * Gets if a rook of the same color has castling rights
	 * @param b functional board
	 * @param color ally color
	 * @param loc location of rook
	 * @return true if there exists a rook that can castle
	 */
	private boolean sameRook(Board b, boolean color, int[] loc) {
		if(!b.isEmpty(loc[0], loc[1])) {
			Piece targetPiece = b.getPiece(loc[0], loc[1]);
			return targetPiece.isWhite() == color && targetPiece.isRook() && targetPiece.getCastlingRights();
		}
		return false;
	}
	
	/**
	 * Checks if the castling squares from a king to the queenside or kingside is are safe
	 * @param b
	 * @param color
	 * @param i
	 * @param j
	 * @param colChange
	 * @return
	 */
	private boolean castlingSquaresSafe(Board b, boolean color, int i, int j, int colChange) {
		int row = i;
		int col = j + colChange;
		while(b.inBounds(row, col) && b.isEmpty(row, col) && b.isSafe(color, row, col)) {
			col += colChange;
		}
		System.out.println("Clear path to rook: " + (b.inBounds(row, col) && (col == 0 || col == 7)));
		return b.inBounds(row, col) && (col == 0 || col == 7);
	}
	/**
	 * Finds the valid moveset for the King
	 * @param b functional board containing pieces
	 * @param moves valid moveset to be updated
	 * @param p current piece (King)
	 * @param i target row for King
	 * @param j target column for King
	 */
	public void getDirMoves(Board b, List<int[]> moves, Piece p, int i, int j) {
		// can move king if inbounds AND the target square is empty or and enemy piece AND it is a safe square		
		if(b.inBounds(i, j) && ((b.isEmpty(i, j) || !b.getPiece(i, j).getColor().equals(p.getColor()))
				&& b.isSafe(this.isWhite(), i, j))) {
			moves.add(new int[] {i, j});
		}
	}
	
	/**
	 * Sets castling rights to false
	 */
	public void removeCastlingRights() {
		castlingRights = false;
	}
	
	/**
	 * Gets the castling rights for a piece
	 * @return castlingRights
	 */
	public boolean getCastlingRights() {
		return castlingRights;
	}
}
