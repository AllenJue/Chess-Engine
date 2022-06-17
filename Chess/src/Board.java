/**
 * The board class provides a functional chess board that alters and maintains the state of a 
 * 2D matrix of Piece objects. It works in conjunction with the gui class to offer a visual representation
 * of the board, and can allow users to interact with a functional board through a gui.
 * TODO: implement draws (HashMap with FEN to do repetition, 50 moves use counter, insufficient materials)
 * TODO: implement pieces taken and keep track of them
 * TODO: Try making the isSafe method better (mark targeted squares ahead of time, instead of asking if the king is in check
 * @author Allen Jue
 * 6/12/2022
 */


import java.util.ArrayList;
import java.util.List;

public class Board {
	public static final int[] DIAGONAL_DIR = new int[] {-1, -1, 1, 1, -1};
	public static final int[] LATERAL_DIR = new int[] {-1, 0 , 1, 0, -1};
	public static final int[] PAWN_DIR = new int[] {-1, 1};
	private boolean check;
	private Piece[][] board;
	private boolean whiteTurn;
	private int ply;
	private final int LOCATION_CONVERTER = 50;
	private Piece whiteKing;
	private Piece blackKing;
	
	/**
	 * Initialize empty board with pieces
	 */
	public Board() {
		board = new Piece[8][8];
		ply = 0;
		whiteTurn = true;
		for(int j = 0; j < board.length; j++) {
			board[1][j] = new Pawn(8, 1, j); // Create pawns
			board[6][j] = new Pawn(8, 6, j);
			if(j == 0 || j == 7) {
				board[0][j] = new Rook(j, 0, j); // TODO rook
				board[7][j] = new Rook(j, 7, j);
			} else if(j == 1 || j == 6) {
				board[0][j] = new Knight(j, 0, j); // TODO knight
				board[7][j] = new Knight(j, 7, j);
			} else if(j == 2 || j == 5) {
				board[0][j] = new Bishop(j, 0, j); // Create Bishop
				board[7][j] = new Bishop(j, 7, j);
			} else if(j == 3) {
				board[0][j] = new Queen(j, 0, j); // TODO Queen
				board[7][j] = new Queen(j, 7, j);
			} else {
				blackKing = new King(j, 0, j);
				board[0][j] = blackKing; // TODO King
				whiteKing = new King(j, 7, j);
				board[7][j] = whiteKing;
				
			}
		}
	}
	
	/**
	 * Get the piece at on board location
	 * @param i
	 * @param j
	 * @return
	 */
	public String getPieceName(int i, int j) {
		return board[i][j] == null ? "" : board[i][j].getColor() + board[i][j].getType();
	}
	
	// TODO: create a board based on a FEN
	public Board(String s) {
		
	}
	
	/**
	 * Gets the turn of the current player. Displaying "W" for white and "B" for black
	 * @return if(whiteTurn) "W" : "B"
	 */
	public String getTurn() {
		return whiteTurn ? "W" : "B";
	}
	
	
	/**
	 * Returns the String representation of the board
	 * @return a String that has ASCII characters representing the board
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(' ');
		for(int i = 0; i < 8; i++) {
			sb.append('-');
			sb.append(' ');
		}
		sb.append('\n');
		for(Piece[] row : board) {
			sb.append('|');
			for(Piece p : row) {
				if(p == null) {
					sb.append(' ');
				} else {
					sb.append(p.getType());
				}
				sb.append('|');
			}
			sb.append('\n');
			for(int i = 0; i < 8; i++) {
				sb.append('-');
				sb.append(' ');
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	/**
	 * Gets whether or not a king is in check
	 * @return king.check
	 */
	public boolean inCheck() {
		return false;
	}
	
	// TODO generate move list when selecting a piece
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public List<int[]> getMoves(double x, double y) {
		int[] loc = getPosition(x, y);
		Piece p = board[loc[0]][loc[1]];
		if(p != null) {
			return p.getMoves(this, p, loc[0], loc[1]);
		}
		return new ArrayList<>();
	}
	
	/**
	 * Gets a piece at board[i][j]
	 * @param i row of board
	 * @param j col of board
	 * @return a Piece object at board[i][j]
	 */
	public Piece getPiece(int i, int j) {
		if(!inBounds(i, j)) {
			throw new IndexOutOfBoundsException("Invalid index in board");
		}
		return board[i][j];
	}
	/**
	 * Calculates the coordinates of the 
	 * @param x
	 * @param y
	 * @return
	 */
	public int[] getPosition(double x, double y) {
		int ax = (int) Math.floor(x / LOCATION_CONVERTER) - 1;
		int ay = (int) Math.floor(y / LOCATION_CONVERTER) - 1;
		return new int[] {ax, ay};
	}
	
	/**
	 * Gets whether or not the square in question is a valid location on the board
	 * @param i row of the board
	 * @param j column of the board
	 * @return (i >= 0 && i < board.length && j >= 0 && j < board.length)
	 */
	public boolean inBounds(int i, int j) {
		return i >= 0 && i < board.length && j >= 0 && j < board.length;
	}
	
	
	/**
	 * Gets whether or not a square is currently being target by an enemy piece
	 * @param i row of square
	 * @param j column of square
	 * @return true if no enemy piece is targeting the current square
	 */
	public boolean isSafe(boolean color, int i, int j) {
		if(!knightSafe(color, i, j) || !pawnSafe(color, i, j) || !kingSafe(color, i, j)) {
			return false;
		}
		for(int k = 0; k < DIAGONAL_DIR.length - 1; k++) {
			if(!lineIsLafe(color, i, j, LATERAL_DIR[k], LATERAL_DIR[k + 1]) 
					|| !lineIsLafe(color, i, j, DIAGONAL_DIR[k], DIAGONAL_DIR[k + 1])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Return if square board[i][j] is safe, given that a piece is of the same color is moving
	 * from board[movedFromI][movedFromJ] to board[movedToI][movedToJ]. Used to see if a king
	 * is safe with the move
	 * @param color of king
	 * @param movedFromI moving piece starting row
	 * @param movedFromJ moving piece starting column
	 * @param movedToI moving piece ending row
	 * @param movedToJ moving piece ending column
	 * @return
	 */
	public boolean kingSafeWithMove(boolean color, int movedFromI, int movedFromJ, int movedToI, int movedToJ) {
		Piece p = color ? whiteKing : blackKing;
		int i = p.getRow();
		int j = p.getCol();
		if(!knightSafeWithMove(color, i, j, movedToI, movedToJ)) {
			return false;
		}
		for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
			if(!lineIsSafeWithMove(color, i, j, LATERAL_DIR[k], LATERAL_DIR[k + 1], movedFromI, movedFromJ, movedToI, movedToJ)
					|| !lineIsSafeWithMove(color, i, j, DIAGONAL_DIR[k], DIAGONAL_DIR[k + 1], movedFromI, movedFromJ, movedToI, movedToJ)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets if a piece is safe after a potentially different piece has been moved
	 * @param color of ally pieces
	 * @param i row of piece to be safe
	 * @param j col of piece to be safe
	 * @param movedToI row ally piece move to
	 * @param movedToJ col ally piece move to
	 * @return true if after ally piece has moved to board[movedToI][movedToJ], then board[i][j] is safe from an enemy knight 
	 */
	private boolean knightSafeWithMove(boolean color, int i, int j, int movedToI, int movedToJ) {
		for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
			if(knightSafeHelperWithMove(color, i + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, j - 1, movedToI, movedToJ)
					|| knightSafeHelperWithMove(color, i + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, j + 1, movedToI, movedToJ)
					|| knightSafeHelperWithMove(color, i - 1, j + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, movedToI, movedToJ)
					|| knightSafeHelperWithMove(color, i + 1, j + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, movedToI, movedToJ)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper for knight safe with move. Basically makes sure that if the piece has captured a knight, do not
	 * consider that knight dangerous
	 * @param color of ally pieces
	 * @param i row of piece to be safe
	 * @param j col of piece to be safe
	 * @param movedToI row ally piece move to
	 * @param movedToJ col ally piece move to
	 * @return true if after ally piece has moved to board[movedToI][movedToJ], then board[i][j] is safe from an enemy knight 
	 */
	private boolean knightSafeHelperWithMove(boolean color, int i, int j, int movedToI, int movedToJ) {
		if(inBounds(i, j) && !isEmpty(i, j) && !(i == movedToI && j == movedToJ)) {
			return getPiece(i, j).isWhite() != color && getPiece(i, j).isKnight();
		}
		return false;
	}

	/**
	 * Gets if a line is safe given that a piece has been moved with color TODO
	 * @param color of ally pieces
	 * @param i row of square to be protected
	 * @param j col of square to be protected
	 * @param rowChange change in row
	 * @param colChange change in col
	 * @param movedFromI moving piece initial row
	 * @param movedFromJ moving piece initial col
	 * @param movedToI moving piece ending row
	 * @param movedToJ moving piece ending col
	 * @return true if no piece in the line is attacking board[i][j]
	 */
	public boolean lineIsSafeWithMove(boolean color, int i, int j, int rowChange, int colChange, 
			int movedFromI, int movedFromJ, int movedToI, int movedToJ) {
		int row = i + rowChange;
		int col = j + colChange;
		// skip square if it is empty or is a square previously occupied
		while(inBounds(row, col) && (isEmpty(row, col) || (row == movedFromI && col == movedFromJ)) 
				&& !(row == movedToI && col == movedToJ)) {
			row += rowChange;
			col += colChange;
		}
		// return false (line is not safe) if encountering a bishop, rook, or queen in the same line
		if(inBounds(row, col) && !(row == movedToI && col == movedToJ) && getPiece(row, col).isWhite() != color) {
			Piece targetPiece = this.getPiece(row, col);
			if(((row == i || col == j) && (targetPiece.isQueen() || targetPiece.isRook()))
					|| (((row + col == i + j) || (row - col == i - j)) && (targetPiece.isQueen() || targetPiece.isBishop()))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets if a particular line (lateral or diagonal) extending from square (i, j)
	 * is attacked by an enemy piece.
	 * @param i row of square
	 * @param j column of square
	 * @param rowChange increment for row
	 * @param colChange increment for column
	 * @return true if a square is safe from any attacks in this line
	 */
	private boolean lineIsLafe(boolean color, int i, int j, int rowChange, int colChange) {
		int row = i + rowChange;
		int col = j + colChange;
		// look for any intersecting piece
		while(inBounds(row, col) && (isEmpty(row, col) || (getPiece(row, col).isWhite() == color && getPiece(row, col).isKing()))) {
			row += rowChange;
			col += colChange;
		}
		// if a piece is an opposite color, the King is attacked if it is in the same:
		// row or column as a Queen or rook (means the row or column must be equal
		// diagonal as a Queen or Bishop (means diff or rows and cols OR sum of rows and cols must be equal)
		// knight targeted square
		if(inBounds(row, col) && getPiece(row, col).isWhite() != color) {
			Piece targetPiece = this.getPiece(row, col);
			if(((row == i || col == j) && (targetPiece.isQueen() || targetPiece.isRook()))
					|| (((row + col == i + j) || (row - col == i - j)) && (targetPiece.isQueen() || targetPiece.isBishop()))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets if a square is safe from a pawn
	 * @param i row of square
	 * @param j column of square
	 * @return true if a square is not attacked by an enemy pawn
	 */
	private boolean pawnSafe(boolean color, int i, int j) {
		int rowChange = color ? -1 : 1;
		for(int k = 0; k < PAWN_DIR.length; k++) {
			if(inBounds(i + rowChange, j + PAWN_DIR[k]) && !isEmpty(i + rowChange, j + PAWN_DIR[k]) 
					&& getPiece(i + rowChange, j + PAWN_DIR[k]).isWhite() != color
					&& getPiece(i + rowChange, j + PAWN_DIR[k]).isPawn()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets if a square is safe from a Knight
	 * @param i row of square
	 * @param j column of square
	 * @return true if a square is not attacked by an enemy knight
	 */
	private boolean knightSafe(boolean color, int i, int j) {
		for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
			if(knightSafeHelper(color, i + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, j - 1)
					|| knightSafeHelper(color, i + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2, j + 1)
					|| knightSafeHelper(color, i - 1, j + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2)
					|| knightSafeHelper(color, i + 1, j + (LATERAL_DIR[k] + LATERAL_DIR[k + 1]) * 2)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Gets if a square is a knight of the opposite color
	 * @param color to be compared against
	 * @param row of square
	 * @param col of square
	 * @return if board[row][col] is a knight of the opposite color
	 */
	private boolean knightSafeHelper(boolean color, int row, int col) {
		if(inBounds(row, col) && !isEmpty(row, col)) {
			return getPiece(row, col).isWhite() != color && getPiece(row, col).isKnight();
		}
		return false;
	}
	
	/**
	 * Gets if a square is safe from a King
	 * @param i row of square
	 * @param j column of square
	 * @return true if a square is not attacked by an enemy king
	 */
	private boolean kingSafe(boolean color, int i, int j) {
		for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
			if(!kingSafeHelper(color, i + LATERAL_DIR[k], j + LATERAL_DIR[k + 1]) 
					|| !kingSafeHelper(color, i + DIAGONAL_DIR[k], j + DIAGONAL_DIR[k + 1])) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if square board[i][j] is a king of the opposite color
	 * @param color to be compared against
	 * @param i row of square
	 * @param j column of square
	 * @return if board[i][j] is a King of the opposite color
	 */
	private boolean kingSafeHelper(boolean color, int i, int j) {
		if(inBounds(i, j) && !isEmpty(i, j) && getPiece(i, j).isWhite() != color
				&& getPiece(i, j).isKing()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets whether or not the square in question is empty (no piece is inhabiting the square)
	 * @param i row of the board
	 * @param j column of the board
	 * @return board[i][j] == null
	 */
	public boolean isEmpty(int i, int j) {
		return board[i][j] == null;
	}

	/**
	 * Moves a piece from prevPo to targPo
	 * @param prevPo previous position
	 * @param targPo target position
	 */
	public void move(int[] prevPo, int[] targPo) {
		if(board[targPo[1]][targPo[0]] != null) {
			kill(targPo);
		} 
		if(enPassantOccurred(this, prevPo, targPo)) {
			// if the target square is less than the current square column, 
			// cacptured piece is on the left
			int colChange = prevPo[0] > targPo[0] ? -1 : 1;
			board[prevPo[1]][prevPo[0] + colChange] = null;
		}
		// move the piece
		board[targPo[1]][targPo[0]] = createPiece(board[prevPo[1]][prevPo[0]]);
		board[prevPo[1]][prevPo[0]] = null;
		// if the piece is a king, keep track of where it should go
		if(board[targPo[1]][targPo[0]].isKing()) {
			if(board[targPo[1]][targPo[0]].isWhite()) {
				whiteKing = board[targPo[1]][targPo[0]];
				whiteKing.setLocations(targPo[1], targPo[0]);
			} else {
				blackKing = board[targPo[1]][targPo[0]];
				blackKing.setLocations(targPo[1], targPo[0]);
			}
		}
		checkEnPassant(prevPo, targPo);
		// remove castling rights (if any) from the piece that just moved and change location
		board[targPo[1]][targPo[0]].setLocations(targPo[1], targPo[0]);
		board[targPo[1]][targPo[0]].removeCastlingRights();
		ply++;
		// check if the current players turn is in check, if so, generate all moves - need to be done for potential checkmate 
//		Piece curKing = whiteTurn ? whiteKing : blackKing;
//		if(!isSafe(curKing.isWhite(), curKing.getRow(), curKing.getRow())) {
//			check = true;
//		} else {
//			check = false;
//		}
	}

	/**
	 * Checks if en_passant is possible for a pawn for the next move (if it moves two squares ahead)
	 * @param prevPo previous posiiton of pawn
	 * @param targPo target posiiton of pawn
	 */
	private void checkEnPassant(int[] prevPo, int[] targPo) {
		Piece p = board[targPo[1]][targPo[0]];
		// if pawn moved two forward in the same column, mark its ply so its possible for en_passant captures
		if(p.isPawn() && Math.abs(targPo[1] - prevPo[1]) > 1 && (targPo[0] == prevPo[0])) {
			board[targPo[1]][targPo[0]].setPly(this);
		}
	}

	/**
	 * Gets if an enpassant has occurred on the board
	 * @param b functional board
	 * @param prevPo previous position of pawn
	 * @param targPo target position of pawn
	 * @return true if an en passant has occurred
	 */
	public boolean enPassantOccurred(Board b, int[] prevPo, int[] targPo) {
		return !b.isEmpty(prevPo[1], prevPo[0]) && board[prevPo[1]][prevPo[0]].isPawn() 
				&& board[targPo[1]][targPo[0]] == null && prevPo[0] != targPo[0];
		// en_passant if pawn is moving diagonally to an empty square. need to remove the adjacent pawn
	}
	
	/**
	 * Basically a constructor for new piece of type p.getType()
	 * @param p piece to duplicate
	 * @return a duplicate p with type p.getType()
	 */
	private Piece createPiece(Piece p) {
		switch (p.getType()) {
			case "P": 
				return new Pawn(p);
			case "R":
				return new Rook(p);
			case "N":
				return new Knight(p);
			case "B":
				return new Bishop(p);
			case "K":
				return new King(p);
			case "Q": 
				return new Queen(p);
			default:
				throw new IllegalArgumentException("Unexpected value: " + p.getType());
		}
	}
	
	/**
	 * Toggles the turn of the game
	 */
	public void changeTurn() {
		whiteTurn = !whiteTurn;
	}
	
	private void kill(int[] targPo) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Gets the ply of the board
	 * @return ply
	 */
	public int getPly() {
		return ply;
	}
	
	/**
	 *	Create a new board 
	 */
	public void reset() {
		board = new Piece[8][8];
		ply = 0;
		whiteTurn = true;
		for(int j = 0; j < board.length; j++) {
			board[1][j] = new Pawn(8, 1, j); // Create pawns
			board[6][j] = new Pawn(8, 6, j);
			if(j == 0 || j == 7) {
				board[0][j] = new Rook(j, 0, j); // Create Rook
				board[7][j] = new Rook(j, 7, j);
			} else if(j == 1 || j == 6) {
				board[0][j] = new Knight(j, 0, j); // Create knight
				board[7][j] = new Knight(j, 7, j);
			} else if(j == 2 || j == 5) {
				board[0][j] = new Bishop(j, 0, j); // Create Bishop
				board[7][j] = new Bishop(j, 7, j);
			} else if(j == 3) {
				board[0][j] = new Queen(j, 0, j); // Create Queen
				board[7][j] = new Queen(j, 7, j);
			} else {
				board[0][j] = new King(j, 0, j); // Create King
				board[7][j] = new King(j, 7, j);
				blackKing = board[0][j];
				whiteKing = board[7][j];
			}
		}
	}
}
