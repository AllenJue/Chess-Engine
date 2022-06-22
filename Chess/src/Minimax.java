import java.util.HashMap;
import java.util.List;

public class Minimax {
	Board b;
	private final double[][] pawnValues = {
		{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
		{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},			
		{1.0, 1.0, 1.1, 1.2, 1.2, 1.1, 1.0, 1.0},
		{1.0, 1.1, 1.2, 1.3, 1.3, 1.2, 1.1, 1.0},
		{1.1, 1.2, 1.3, 1.4, 1.4, 1.3, 1.2, 1.1},			
		{1.2, 1.3, 1.4, 1.5, 1.5, 1.4, 1.3, 1.2},	
		{1.3, 1.4, 1.5, 1.6, 1.6, 1.5, 1.4, 1.3},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
	};
	
	private final double[][] knightValues = {
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0}
	};
	
	private final double[][] bishopValues = {
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0},
		{3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0}
	};
	
	private final double[][] rookValues = {
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0},
		{5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0}
	};
	
	private final double[][] queenValues = {
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
		{9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0, 9.0},
	};
	
	private final double[][] kingValues = {
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0},
		{1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0, 1000.0}
	};
	
	public Minimax(Board bCopy) {
		b = new Board(bCopy);
		
	}
	
	/**
	 * Minimax method that uses alpha-beta pruning to determine the optimal move for a player
	 * given a depth
	 * @param b functional chess board
	 * @param depth how many moves deeper to look
	 * @param alpha highest value found so far
	 * @param beta lowest value found so far
	 * @param whiteTurn true if it is white's turn to move
	 * @return optimal move for the maximizing or minimizing player
	 */
	public double minimax(Board b, int depth, double alpha, double beta, boolean whiteTurn) {
		if(depth == 0 || !b.generateAllMoves()) {
			return evaluatePosition();
		}
		// maximize score if white turn 
		if(whiteTurn) {
			double maxEval = Double.MIN_VALUE;
			 for(Piece p : b.getPieceList(true).keySet()) {
				 // TODO: do move
				 double eval = minimax(b, depth - 1, alpha, beta, !whiteTurn);
				 // TODO: undo move
				 maxEval = Math.max(maxEval, eval);
				 alpha = Math.max(alpha, maxEval);
				 if(beta <= alpha) {
					 break;
				 }
			 }
			 return maxEval;
		} else {
			// minimize score if black turn
			double minEval = Double.MAX_VALUE;
			 for(Piece p : b.getPieceList(true).keySet()) {
				 double eval = minimax(b, depth - 1, alpha, beta, !whiteTurn);
				 minEval = Math.min(minEval, eval);
				 beta = Math.min(minEval, minEval);
				 if(beta <= alpha) {
					 break;
				 }
			 }
			 return minEval;
		}
	}
	
	/**
	 * Static evaluation function that estimates the value of a board state 
	 * @return the sum of the values of each player's pieces
	 */
	private double evaluatePosition() {
		double score = 0;
		HashMap<Piece, List<int[]>> whitePieces = b.getPieceList(true);
		for(Piece p : whitePieces.keySet()) {
			// add to position score for white pieces
			score += getPieceValue(p);
		}
		HashMap<Piece, List<int[]>> blackPieces = b.getPieceList(false);
		for(Piece p : blackPieces.keySet()) {
			score -= getPieceValue(p);
		}
		return score;
	}
	
	/**
	 * Gets the piece value from a piece value table
	 * @param p piece whose value needs to be returned
	 * @return the value of the piece given its position on the board
	 */
	private double getPieceValue(Piece p) {
		double[][] pieceTable = getPieceTable(p);
		// get row and col of piece to find in piece table value
		// reflect white piece coordinates row because piece tables are black-oriented
		int row = p.isWhite() ? 8 - p.getRow() - 1 : p.getRow(); 
		int col = p.getCol();
		return pieceTable[row][col];
	}
	
	/**
	 * Gets the corresponding piece table for a piece type
	 * @param p Piece that has its type
	 * @return a piece table for the corresponding piece type
	 */
	private double[][] getPieceTable(Piece p) {
		switch(p.getType()) {
		case 'P':
			return pawnValues;
		case 'B':
			return bishopValues;
		case 'N':
			return knightValues;
		case 'R':
			return rookValues;
		case 'Q':
			return queenValues;
		case 'K':
			return kingValues;
		default:
			throw new IllegalArgumentException("Piece type is not valid");
		}
	}
}
