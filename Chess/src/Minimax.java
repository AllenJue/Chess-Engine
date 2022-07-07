import java.util.ArrayList;
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
	
	/**
	 * Pair class to get the moves and evaluation in a single object
	 *
	 * @param <F> first val to be stored in the tuple
	 * @param <S> second val to be stored in the tuple
	 * @param <T> third val to be stored in the tuple
	 */
	public static class Tuple<F, S, T> {
		private final F first;
		private final S second;
		private final T third;
		
		public Tuple(F f, S s, T t) {
			this.first = f;
			this.second = s;
			this.third = t;
		}
		
		public F getFirst() {
			return first;
		}
		
		public S getSecond() {
			return second;
		}
		
		public T getThird() {
			return third;
		}
	}
	
	public Minimax(Board bCopy) {
		b = bCopy;
		
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
	public double minimax(int depth, double alpha, double beta, boolean whiteTurn) {
		if(!b.movesAvailable()) {
			System.out.println("No moves available for " + b.getTurn());
			System.out.println(b);
			return b.whiteTurn() ? -999 : 999;
		} else if(depth == 0) {
			return evaluatePosition();
		}
		double compEval;
		if(whiteTurn) {
			compEval = Double.MIN_VALUE;
		} else {
			compEval = Double.MAX_VALUE;
		}
		// maximize score if white turn 
		HashMap<Piece, List<int[]>> pieceListCopy = deepCopy(b.getPieceList(whiteTurn)); // Need to make a DEEP COPY FIX!!!		
		for(Piece p : pieceListCopy.keySet()) {
			 // try moving piece
			int[] prevPo = new int[] {p.getCol(), p.getRow()};
			for(int[] moves : pieceListCopy.get(p)) {
				if(!p.isCaptured()) {
					int[] targPo = new int[] {moves[1], moves[0]};
					// make move
					b.move(prevPo, targPo);
					double eval = minimax(depth - 1, alpha, beta, !whiteTurn);
					b.undoMove();
					if(whiteTurn) {
						compEval = Math.max(compEval, eval);
						alpha = Math.max(alpha, eval);
					} else {
						compEval = Math.min(compEval, eval);
						beta = Math.min(beta, eval);
					}
					if(beta <= alpha) {
						break;
					}
				}
			}
			if(beta <= alpha) {
				break;
			}
		}
		return compEval;
	}
	
	public HashMap<Piece, List<int[]>> deepCopy(HashMap<Piece, List<int[]>> copy) {
		HashMap<Piece, List<int[]>> deepCopy = new HashMap<>();
		for(Piece p : copy.keySet()) {
			List<int[]> copyList = new ArrayList<>();
			for(int[] move : copy.get(p)) {
				copyList.add(new int[] {move[0], move[1]});
			}
			deepCopy.put(p, copyList);
		}
		return deepCopy;
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
			if(!p.isCaptured()) {
				score += getPieceValue(p);
			}
		}
		HashMap<Piece, List<int[]>> blackPieces = b.getPieceList(false);
		for(Piece p : blackPieces.keySet()) {
			if(!p.isCaptured()) {
				score -= getPieceValue(p);
			}
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
