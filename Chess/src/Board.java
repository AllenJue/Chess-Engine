/**
 * The board class provides a functional chess board that alters and maintains the state of a 
 * 2D matrix of Piece objects. It works in conjunction with the gui class to offer a visual representation
 * of the board, and can allow users to interact with a functional board through a gui.
 * TODO: implement draws (HashMap with FEN to do repetition, 50 moves use counter, insufficient materials)
 * TODO: implement pieces taken and keep track of them
 * TODO: implement undo button (maybe keep track of piece and location moved + captured pieces in a stack) ?
 * TODO: implement resign button
 * @author Allen Jue
 * 6/12/2022
 */


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Board {
	// King, queen, rook, bishop, knight pawn
	private final char[] classifier = new char[] {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R', 'P'};
	// directional arrays to simplify common directional moves
	public static final int[] DIAGONAL_DIR = new int[] {-1, -1, 1, 1, -1};
	public static final int[] LATERAL_DIR = new int[] {-1, 0 , 1, 0, -1};
	public static final int[] PAWN_DIR = new int[] {-1, 1};
	private Piece[][] board;
	// hashmap for active white and black pieces. Make iterating through pieces simple
	private HashMap<Piece, List<int[]>> whitePieces;
	private HashMap<Piece, List<int[]>> blackPieces;
	// access to white and black king is essential, as all moves must ensure king safety
	private boolean movesAvailable;
	private Piece whiteKing;
	private Piece blackKing;
	private boolean whiteTurn;
	private int ply;
	private int halfMoves;
	private final int LOCATION_CONVERTER = 50;
	// Stacks to implement command pattern for redo and undo
	private ArrayDeque<String> undoFEN;
	private ArrayDeque<String> redoFEN;
	// Stacks to implement undo withoutFEN
	private ArrayDeque<Piece> captured;
	private ArrayDeque<Piece> lastMoved;
	private ArrayDeque<int[]> previousPos;
	private ArrayDeque<Boolean> prevCastle;
	private final Piece NULL_PIECE = new Pawn(0, -1, -1);
	
	/**
	 * Initialize empty board with pieces
	 */
	public Board() {
		initializeFields();
		initializeBoard();
		movesAvailable = generateAllMoves();
	}

	/**
	 * Constructor for a board that creates a copy of the existing board
	 * @param b board to be copied
	 */
	public Board(Board b) {
		board = new Piece[8][8];
		ply = b.getPly();
		whiteTurn = b.whiteTurn;
		blackPieces = new HashMap<>(b.blackPieces);
		whitePieces = new HashMap<>(b.whitePieces);
		undoFEN = new ArrayDeque<>(b.undoFEN);
		redoFEN = new ArrayDeque<>(b.redoFEN);
		captured = new ArrayDeque<>(b.captured);
		lastMoved = new ArrayDeque<>(b.lastMoved);
		previousPos = new ArrayDeque<>(b.previousPos);
		prevCastle = new ArrayDeque<>(b.prevCastle);
		movesAvailable = b.movesAvailable;
		for(Piece p : blackPieces.keySet()) {
			board[p.getRow()][p.getCol()] = createPiece(p);
			if(p.isKing()) {
				blackKing = board[p.getRow()][p.getCol()];
			}
		}
		for(Piece p : whitePieces.keySet()) {
			board[p.getRow()][p.getCol()] = createPiece(p);
			if(p.isKing()) {
				whiteKing = board[p.getRow()][p.getCol()];
			}
		}
		generateAllMoves();
	}
	
	/**
	 *	Create a new board 
	 */
	public void reset() {
		initializeFields();
		initializeBoard();
		generateAllMoves();
	}
	
	/**
	 * Initializes the board to a default starting position
	 */
	public void initializeBoard() {
		for(int j = 0; j < board.length; j++) {
			board[1][j] = new Pawn(8, 1, j); // Create pawns
			board[6][j] = new Pawn(8, 6, j);
			blackPieces.put(board[1][j], new ArrayList<>());
			whitePieces.put(board[6][j], new ArrayList<>());
			board[0][j] = createPiece(j, 0, j);
			board[7][j] = createPiece(j, 7, j);
			if(j == 4) {
				whiteKing = board[7][j];
				blackKing = board[0][j];
			}
			// catalog white pieces and black pieces. Use for later to iterate through to find moves
			blackPieces.put(board[0][j], new ArrayList<>());
			whitePieces.put(board[7][j], new ArrayList<>());
		}
		undoFEN.offer(generateFEN());
	}
	
	/**
	 * Initializes default fields for a board
	 */
	private void initializeFields() {
		board = new Piece[8][8];
		ply = 1;
		halfMoves = 0;
		whiteTurn = true;
		blackPieces = new HashMap<>();
		whitePieces = new HashMap<>();
		undoFEN = new ArrayDeque<>();
		redoFEN = new ArrayDeque<>();
		previousPos = new ArrayDeque<>();
		lastMoved = new ArrayDeque<>();
		captured = new ArrayDeque<>();
		prevCastle = new ArrayDeque<>();
	}
	
	/**
	 * Initializes a board with a fen
	 * @param FEN initializing FEN
	 */
	private void initializeWithFen(String FEN) {
		String[] parsedFen  = FEN.split("\\s");
		if(parsedFen.length < 6) {
			throw new IllegalArgumentException("FEN is not formatted correctly");
		}
		placePieces(parsedFen[0]);
		whiteTurn = parsedFen[1].equals("w");
		assignCastlingRights(parsedFen[2]);
		this.halfMoves = Integer.parseInt(parsedFen[4]);
		this.ply = Integer.parseInt(parsedFen[5]);
		assignEnPassant(parsedFen[3]);
	}

	/**
	 * Assigns to a pawn the correct ply given its en passant square, marking it
	 * as capturable
	 * @param enPassantSquare, the square that can be moved to in an en passant move
	 */
	private void assignEnPassant(String enPassantSquare) {
		if(!enPassantSquare.equals("-")) {
			int[] pos = processNotation(enPassantSquare);
			if(whiteTurn) {
				board[pos[0] + 1][pos[1]].setPly(this.getPly());;
			} else {
				board[pos[0] - 1][pos[1]].setPly(this.getPly());
			}
		}
	}

	/**
	 * Gets the positional square on the board from algebraic notation
	 * @param enPassantSquare algebraic notation square
	 * @return the coordinates of the algebraic notation
	 */
	private int[] processNotation(String enPassantSquare) {
		if(enPassantSquare.length() != 2 || enPassantSquare.charAt(0) < 'a' || enPassantSquare.charAt(0) > 'h'
				) {
			throw new IllegalArgumentException("Incorrect format of algebraic notation: " + enPassantSquare);
		}
		int row = 8 - (enPassantSquare.charAt(1) - '0');
		int col = enPassantSquare.charAt(0) - 'a';
		return new int[] {row, col};
	}
	
	/**
	 * Gets the algebraic notation from a positional square on the board
	 * @param coordinates position of square on board
	 * @return the algebraic notation from a given coordinate
	 */
	private String processCoordinates(int[] coordinates) {
		if(coordinates.length != 2 || coordinates[0] < 0 || coordinates[0] > 7 || coordinates[1] < 0 || coordinates[1] > 7) {
			throw new IllegalArgumentException("Invalid format of coordinates for algebraic notation");
		}
		StringBuilder sb = new StringBuilder();
		sb.append((char)('a' + coordinates[1]));
		sb.append(8 - coordinates[0]);
		return sb.toString();
	}

	/**
	 * Assign castling rights of kings based on a FEN
	 * @param castling rights - the third field of a FEN
	 */
	private void assignCastlingRights(String castlingRights) {
		HashSet<Character> temp = new HashSet<>();
		for(char c : castlingRights.toCharArray()) {
			temp.add(c);
		}
		FenCanCastle(temp, whiteKing, 'K', 'Q');
		FenCanCastle(temp, blackKing, 'k', 'q');
	}
	
	/**
	 * Establishes the castling rights based on the FEN
	 * @param temp HashSet with the available castling sides
	 * @param king to be castled
	 * @param kingChar symbol in FEN to designate kingside castling
	 * @param queenChar symbol in FEN to designate queenside castling
	 */
	private void FenCanCastle(HashSet<Character> temp, Piece king, char kingChar, char queenChar) {
		// if castling rights are valid in FEN, the rooks must be in their initial positions
		// if castling rights are invalid (temp does not contain K or Q), can just remove castling rights
		// from the corresponding king
		if(temp.contains(kingChar) || temp.contains(queenChar)) {
			int row = king.isWhite() ? 7 : 0;
			king.setCastlingRights(true);
			if(temp.contains(kingChar)) {
				board[row][7].setCastlingRights(true);
			}
			if(temp.contains(queenChar)) {
				board[row][0].setCastlingRights(true);
			}
		} else {
			king.setCastlingRights(false);
		}
	}

	/**
	 * Place the pieces based on the first field of the FEN
	 * @param pieces arrangement in FEN
	 */
	private void placePieces(String pieces) {
		whitePieces = new HashMap<>();
		blackPieces = new HashMap<>();
		String[] rows = pieces.split("/");

		for(int r = 0; r < rows.length; r++) {
			int c = 0;
			for(int j = 0; j < rows[r].length(); j++) {
				try {
					// if a number encountered. Skip ahead that many columns in the board
					int skip = Integer.parseInt(rows[r].charAt(j) + "");
					for(int k = 0; k < skip; k++) {
						board[r][c] = null;
						c++;
					}
				} catch(NumberFormatException e) {
					// if a character encountered, put in the board at the row and col, a piece
					// based on the FEN specification
					board[r][c] = createPiece(rows[r].charAt(j), r, c);
					// keep track of white and black kings
					trackKing(new int[] {c, r});
					if(board[r][c].isWhite()) {
						whitePieces.put(board[r][c], new ArrayList<>());
					} else {
						blackPieces.put(board[r][c], new ArrayList<>());
					}
					c++;
				}
			}
		}
	}
	
	/**
	 * Gets the piece list for a desired color
	 * @param isWhite the color desired (isWhite == whitePieces)
	 * @return isWhite ? whitePieces : blackPieces
	 */
	public HashMap<Piece, List<int[]>> getPieceList(boolean isWhite) {
		return isWhite ? whitePieces : blackPieces;
	}
	
	/**
	 * Gets all the moves for a specific color. Returns true if there is at least one move
	 * @return number of legal moves > 0
	 */
	public boolean generateAllMoves() {
		HashMap<Piece, List<int[]>> colorPieces = whiteTurn ? whitePieces : blackPieces;
		int size = 0;
		for(Piece p : colorPieces.keySet()) {
			if(!p.isCaptured()) {
				try {
					List<int[]> moves = colorPieces.get(p);
					moves.clear();
					p.getMoves(this, moves, p, p.getRow(), p.getCol());
					size += moves.size();
				} catch (NullPointerException e) {
					System.out.println("Error trying to get: " + p + " from: " + colorPieces);
				}
		
			}
		}
		return size > 0;
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
	

	/**
	 * Gets the turn of the current player. Displaying 'W' for white and 'B' for black
	 * @return if(whiteTurn) 'W' : 'B'
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
					if(p.isWhite()) {
						sb.append(p.getType());
					} else {
						sb.append(Character.toLowerCase(p.getType()));
					}
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
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public List<int[]> getMoves(double x, double y) {
		int[] loc = getPosition(x, y);
		Piece p = board[loc[0]][loc[1]];
		if(p != null) {  
			if(p.isWhite() == whiteTurn) {
				HashMap<Piece, List<int[]>> colorPieces = whiteTurn ? whitePieces : blackPieces;
				return colorPieces.get(p);
			} 
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
		if(!knightSafeWithMove(color, i, j, movedToI, movedToJ) || !pawnSafeWithMove(color, i, j, movedToI, movedToJ)) {
			return false;
		}
		int[] enPassant; 
		// if pawn is moving like enpassant should ignore its captured piece as well
		if(!isEmpty(movedFromI, movedFromJ) && getPiece(movedFromI, movedFromJ).isPawn() && movedFromJ != movedToJ && isEmpty(movedToI, movedToJ)) {
			enPassant = new int[] {movedFromI, movedToJ};
		} else {
			enPassant = new int[] {-1, -1};
		}
		for(int k = 0; k < LATERAL_DIR.length - 1; k++) {
			if(!lineIsSafeWithMove(color, i, j, LATERAL_DIR[k], LATERAL_DIR[k + 1],
					movedFromI, movedFromJ, movedToI, movedToJ, enPassant)
					|| !lineIsSafeWithMove(color, i, j, DIAGONAL_DIR[k], DIAGONAL_DIR[k + 1],
							movedFromI, movedFromJ, movedToI, movedToJ, enPassant)) {
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
	 * Gets if a line is safe given that a piece has been moved with color 
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
			int movedFromI, int movedFromJ, int movedToI, int movedToJ, int[] enPassant) {
		int row = i + rowChange;
		int col = j + colChange;
		// skip square if it is empty or is a square previously occupied
		while(inBounds(row, col) && (isEmpty(row, col) || (row == movedFromI && col == movedFromJ) || (row == enPassant[0] && col == enPassant[1])) 
				&& !(row == movedToI && col == movedToJ)) {
			row += rowChange;
			col += colChange;
		}
		// return false (line is not safe) if encountering a bishop, rook, or queen in the same line
		if(inBounds(row, col) && !(row == movedToI && col == movedToJ) && getPiece(row, col).isWhite() != color) {
			Piece targetPiece = this.getPiece(row, col);
			if(((row == i || col == j) && (targetPiece.isQueen() || targetPiece.isRook() || targetPiece.isPromoted()))
					|| (((row + col == i + j) || (row - col == i - j)) && (targetPiece.isQueen() || targetPiece.isBishop() || targetPiece.isPromoted()))) {
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
			if(((row == i || col == j) && (targetPiece.isQueen() || targetPiece.isRook() || targetPiece.isPromoted()))
					|| (((row + col == i + j) || (row - col == i - j)) && (targetPiece.isQueen() || targetPiece.isBishop() || targetPiece.isPromoted()))) {
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
	 * Gets if a square is safe from a pawn with a move
	 * @param color of ally pieces
	 * @param i row of square to be kept safe
	 * @param j column of square to be kept safe
	 * @param movedToI ally piece moving to that row
	 * @param movedToJ ally piece moving to that column
	 * @return true if a square is safe when considering an ally piece is moving to a target square
	 */
	private boolean pawnSafeWithMove(boolean color, int i, int j, int movedToI, int movedToJ) {
		int rowChange = color ? -1 : 1;
		for(int k = 0; k < PAWN_DIR.length; k++) {
			if(!(i + rowChange == movedToI && j + PAWN_DIR[k] == movedToJ) && 
					inBounds(i + rowChange, j + PAWN_DIR[k]) && !isEmpty(i + rowChange, j + PAWN_DIR[k])
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
		if(board[prevPo[1]][prevPo[0]].isWhite() != whiteTurn) {
			throw new IllegalArgumentException("Moving on the wrong turn: " + board[prevPo[1]][prevPo[0]]
					+ " \n" + whitePieces + " \n" + blackPieces + " \n trying to move: " + prevPo[1] + " " + prevPo[0]
							+ " to " + targPo[1] + " " + targPo[0] + "\n board state: \n" + this);
		}
		// kill a piece by removng it from the list of pieces (includes en passant captures)
		capturePieceMap(prevPo, targPo);
		// move the piece
		if(getPiece(prevPo[1], prevPo[0]).isKing() && Math.abs(prevPo[0] - targPo[0]) > 1) {
			moveCastlingRook(targPo);
		}
		movePieceMap(prevPo, targPo);
		// if the piece is a king, keep track of where it should go
		trackKing(targPo);
		checkEnPassant(prevPo, targPo);
		// remove castling rights (if any) from the piece that just moved and change location
		ply++;
		changeTurn();
		movesAvailable = generateAllMoves();
		redoFEN.clear();
		undoFEN.offer(generateFEN());
		previousPos.offer(prevPo);
		lastMoved.offer(board[targPo[1]][targPo[0]]);
	}

	/**
	 * Moves the rook correctly if a castle has occurred
	 * @param prevPo previous position of king
	 * @param targPo target position of king
	 */
	private void moveCastlingRook(int[] targPo) {
		// kingside castle
		if(targPo[0] == 6) {
			board[targPo[1]][5] = board[targPo[1]][7]; 
			board[targPo[1]][5].setLocations(targPo[1], 5); 
			board[targPo[1]][7] = null; 
		} else {
			board[targPo[1]][3] = board[targPo[1]][0]; 
			board[targPo[1]][3].setLocations(targPo[1], 3); 
			board[targPo[1]][0] = null;
		}
	}

	/**
	 * Remove a piece from the board by setting its value to null
	 * @param kill the piece at killSquare
	 */
	private void kill(int[] killSquare) {
		captured.offer(board[killSquare[1]][killSquare[0]]);
		HashMap<Piece, List<int[]>> otherPieces = whiteTurn ? blackPieces : whitePieces;
		for(Piece p : otherPieces.keySet()) {
			if(board[killSquare[1]][killSquare[0]].equals(p)) {
				p.capture();
			}
		}
		board[killSquare[1]][killSquare[0]].capture();
		board[killSquare[1]][killSquare[0]] = null;
	}
	
	/**
	 * Remove a piece from the hashmap of pieces available given a piece's move. 
	 * If it is whiteTurn, the pieces captured should be removed from black's pieces and vice versa.
	 * @param prevPo previous position of piece
	 * @param targPo target position of piece
	 */
	private void capturePieceMap(int[] prevPo, int[] targPo) {
		// kill a piece by pseudo-removing it from the list of pieces -- remove the piece from the OPPOSITE piece map
		if(board[targPo[1]][targPo[0]] != null) {
			kill(targPo);
		} else if(enPassantOccurred(this, prevPo, targPo)) {
			// if the target square is less than the current square column, 
			// captured piece is on the left
			kill(new int[] {targPo[0], prevPo[1]});
		} else {
			captured.offer(NULL_PIECE);
		}
	}
	
	/**
	 * Updates the piece map when a piece moves. The piece map should
	 * update itself to remove the previous reference to the piece with the newly-created
	 * Piece object
	 * @param prevPo previous position of moving piece
	 * @param targPo target position of moving piece
	 */
	private void movePieceMap(int[] prevPo, int[] targPo) {
		board[targPo[1]][targPo[0]] = board[prevPo[1]][prevPo[0]];
		board[targPo[1]][targPo[0]].setLocations(targPo[1], targPo[0]);
		prevCastle.offer(board[targPo[1]][targPo[0]].getCastlingRights());
		board[targPo[1]][targPo[0]].setCastlingRights(false);
		Piece p = board[targPo[1]][targPo[0]];
		HashMap<Piece, List<int[]>> colorPieces = whiteTurn ? whitePieces : blackPieces;
		if(promotionOccurred(this, targPo)) {
			p.automaticPromote(p.getRow(), p.getCol());
			p.setPly(this);
			for(Piece bp : colorPieces.keySet()) {
				if(bp.equals(p)) {
					bp.automaticPromote(p.getRow(), p.getCol());
					bp.setPly(this);
					break;
				}
			}
		}
		for(Piece bp : colorPieces.keySet()) {
			if(bp.equals(p)) {
				bp.setLocations(p.getRow(), p.getCol());
				break;
			}
		}
		board[prevPo[1]][prevPo[0]] = null;
	}
	
	/**
	 * Keep track of the king piece when it moves
	 * @param prevPo
	 * @param targPo
	 */
	private void trackKing(int[] targPo) {
		if(board[targPo[1]][targPo[0]].isKing()) {
			if(board[targPo[1]][targPo[0]].isWhite()) {
				whiteKing = board[targPo[1]][targPo[0]];
			} else {
				blackKing = board[targPo[1]][targPo[0]];
			}
		}
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
			board[targPo[1]][targPo[0]].setPly(ply);
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
		Piece potentialTarget = board[prevPo[1]][targPo[0]];
		return board[prevPo[1]][prevPo[0]].isPawn() && prevPo[0] != targPo[0] && potentialTarget != null 
				&& potentialTarget.getPly() == this.ply - 1
				&& potentialTarget.isWhite() != board[prevPo[1]][prevPo[0]].isWhite();
		// en_passant if pawn is moving diagonally to an empty square. need to remove the adjacent pawn
	}
	
	/**
	 * Gets if a promotion should occur. When a pawn reaches the opposite side of its board
	 * @param b functional board
	 * @param targPo target position of a piece
	 * @return true if a pawn has reached the end of the board
	 */
	public boolean promotionOccurred(Board b, int[] targPo) {
		Piece p = b.getPiece(targPo[1], targPo[0]);
		// pawn is promoting if it has reached the last row for its corresponding color
		return p.isPawn() && ((p.isWhite() && targPo[1] == 0) || (!p.isWhite() && targPo[1] == 7));
	}
	
	/**
	 * Generates a FEN from a board position
	 * @return a FEN string from the current board state
	 */
	public String generateFEN() {
		Piece enPassant = null;
		StringBuilder sb = new StringBuilder();
		// append board position
		for(int i = 0; i < board.length; i++) {
			int skips = 0;
			for(int j = 0; j < board[0].length; j++) {
				if(isEmpty(i, j)) {
					skips++;
				} else {
					// append number of skips when encountering > 0
					if(skips > 0) {
						sb.append(skips);
						skips = 0;
					}
					// append piece, lowercase if it is black
					char piece = board[i][j].getType();
					if(!board[i][j].isWhite()) {
						piece = Character.toLowerCase(piece);
					}
					if(board[i][j].isPawn() && (i == 3 || i == 4) && board[i][j].getPly() == this.ply) {
						enPassant = board[i][j];
					}
					sb.append(piece);
				}
			}
			if(skips > 0) {
				sb.append(skips);
			}
			sb.append('/');
		}
		// remove last slash
		sb.deleteCharAt(sb.length() - 1);
		sb.append(' ');
		if(whiteTurn) {
			sb.append('w');
		} else {
			sb.append('b');
		}
		sb.append(' ');
		appendCastlingRights(sb);
		appendEnPassant(sb, enPassant);
		// append half moves
		sb.append(halfMoves);
		sb.append(' ');
		// fullmoves is the number of times black moves. This can be translated to the floor of (ply / 2) + 1
		sb.append(((ply - 1) / 2) + 1);
		return sb.toString();
	}
	
	/**
	 * Gets the FEN enpassant code ('-' if no enpassant available)
	 * @param sb total FEN string to append en passant rights to
	 * @param enPassant pawn that can be en passant captured
	 */
	private void appendEnPassant(StringBuilder sb, Piece enPassant) {
		if(enPassant != null) {
			int row = enPassant.isWhite() ? enPassant.getRow() + 1 : enPassant.getRow() - 1;
			int col = enPassant.getCol();
			sb.append(processCoordinates(new int[] {row, col}));
		} else {
			sb.append('-');
		}
		sb.append(' ');
	}

	/**
	 * Gets the FEN castling rights from a board
	 * K means white kingside castle possible
	 * Q mean white queenside castle possible
	 * If the letters are lowercase, then the black varian to of castling is available
	 * @param sb total FEN to append castling rights to
	 */
	private void appendCastlingRights(StringBuilder sb) {
		// if possible castling, find the castling rights, otherwise append '-'
		if(whiteKing.getCastlingRights() || blackKing.getCastlingRights()) {
			boolean wKingside = false;
			boolean wQueenside = false;
			boolean bKingside = false;
			boolean bQueenside = false;
			for(Piece p : whitePieces.keySet()) {
				if(p.isRook()) {
					// get castling rights from the rooks if they're in the correct column
					if(p.getCol() == 7) {
						wKingside = (whiteKing.getCastlingRights() && p.getCastlingRights());
					} else if(p.getCol() == 0) {
						wQueenside = (whiteKing.getCastlingRights() && p.getCastlingRights());
					}
				} 
			}
			for(Piece p : blackPieces.keySet()) {
				if(p.isRook()) {
					if(p.getCol() == 7) {
						bKingside = (blackKing.getCastlingRights() && p.getCastlingRights());
					} else if(p.getCol() == 0) {
						bQueenside = (blackKing.getCastlingRights() && p.getCastlingRights());
					}
				} 
			}
			if(wKingside) {
				sb.append('K');
			}
			if(wQueenside) {
				sb.append('Q');
			}
			if(bKingside) {
				sb.append('k');
			}
			if(bQueenside) {
				sb.append('q');
			}
		} else {
			sb.append('-');
		}
		sb.append(' ');
	}

	/**
	 * Undoes a singular move if possible
	 */
	public void undoMove() {
		if(lastMoved.size() > 0) {
			changeTurn();
			ply--;
			int[] prevPo = previousPos.removeLast();
			Piece lastP = lastMoved.removeLast();
			int[] targPo = new int[] {lastP.getCol(), lastP.getRow()};
			boolean lastCastle = prevCastle.removeLast();
			lastP.setCastlingRights(lastCastle);
			// put last moved piece in previous position
			board[prevPo[1]][prevPo[0]] = lastP;
			// null out where it was
			board[lastP.getRow()][lastP.getCol()] = null;
			lastP.setLocations(prevPo[1], prevPo[0]);
			// replace the captured piece
			if(captured.peekLast() == NULL_PIECE) {
				board[targPo[1]][targPo[0]] = null;
				captured.removeLast();
			} else {
				Piece capturedP = captured.removeLast();
				board[capturedP.getRow()][capturedP.getCol()] = capturedP;
				HashMap<Piece, List<int[]>> otherColor = getPieceList(!whiteTurn);
				for(Piece p : otherColor.keySet()) {
					if(p.equals(capturedP)) {
						p.uncapture();
						break;
					}					
				}
				capturedP.uncapture();
			}
			HashMap<Piece, List<int[]>> colorPieces = getPieceList(whiteTurn);
			// track the king
			trackKing(prevPo);
			if(lastP.isKing() && Math.abs(prevPo[0] - targPo[0]) > 1) {
				// castling occurred, undo rook move as well
				unmoveCastlingRook(targPo);
			}
			// update board state
			for(Piece p : colorPieces.keySet()) {
				if(p.equals(lastP)) {
					p.setLocations(prevPo[1], prevPo[0]);
					p.setCastlingRights(lastCastle);
					break;
				}
			}
			// undo promotion
			if(lastP.getPly() == this.getPly() && lastP.isPromoted()) {
				automaticUndoPromote(lastP);
			}
		}
	}
	
	/**
	 * Automatically unpromotes a queen to a pawn
	 * @param lastP pawn to be depromoted
	 */
	private void automaticUndoPromote(Piece lastP) {
		lastP.depromote();
		HashMap<Piece, List<int[]>> colorPieces = getPieceList(whiteTurn);
		for(Piece p : colorPieces.keySet()) {
			if(p.equals(lastP)) {
				p.depromote();
				break;
			}
		}
	}

	/**
	 * umoves a castling rook. Reassigns correct positions // TODO properly undo castling rook amd King
	 * @param prevPo
	 */
	private void unmoveCastlingRook(int[] prevPo) {
		if(prevPo[0] == 6) {
			board[prevPo[1]][7] = board[prevPo[1]][5];
			board[prevPo[1]][7].setCastlingRights(true);
			board[prevPo[1]][7].setLocations(prevPo[1], 7);
			Piece p = board[prevPo[1]][7];
			HashMap<Piece, List<int[]>> colorPieces = whiteTurn ? whitePieces : blackPieces;
			for(Piece bp : colorPieces.keySet()) {
				if(p.equals(bp)) {
					bp.setLocations(prevPo[1], 7);
					bp.setCastlingRights(true);
					break;
				}
			}
			board[prevPo[1]][5] = null;
		} else {
			board[prevPo[1]][0] = board[prevPo[1]][3];
			board[prevPo[1]][0].setCastlingRights(true);
			board[prevPo[1]][0].setLocations(prevPo[1], 0);
			Piece p = board[prevPo[1]][0];
			HashMap<Piece, List<int[]>> colorPieces = whiteTurn ? whitePieces : blackPieces;
			for(Piece bp : colorPieces.keySet()) {
				if(p.equals(bp)) {
					bp.setLocations(prevPo[1], 0);
					bp.setCastlingRights(true);
					break;
				}
			}
			board[prevPo[1]][3] = null;
		}
	}
	
	/**
	 * Redoes a singular move if possible
	 */
	public void redoMove() {
		
	}
	
	/**
	 * Undos a move by initializing the board with the previous FEN
	 * Keeps a tab on the undid FEN by offering it to the redo Stack
	 */
	public void undoMoveWithFEN() {
		if(undoFEN.size() > 1) {
			redoFEN.offer(undoFEN.removeLast());
			initializeWithFen(undoFEN.peekLast());
		}
	}
	
	/**
	 * Redoes a move based on the moves stored in redo Stack
	 */
	public void redoMoveWithFEN() {
		if(!redoFEN.isEmpty()) {
			String redoPos = redoFEN.removeLast();
			undoFEN.offer(redoPos);
			initializeWithFen(redoPos);
		}
	}
	
	/**
	 * Basically a constructor for new piece of type p.getType()
	 * @param p piece to duplicate
	 * @return a duplicate p with type p.getType()
	 */
	public Piece createPiece(Piece p) {
		switch (p.getType()) {
			case 'P': 
				return new Pawn(p);
			case 'R':
				return new Rook(p);
			case 'N':
				return new Knight(p);
			case 'B':
				return new Bishop(p);
			case 'K':
				return new King(p);
			case 'Q': 
				return new Queen(p);
			default:
				throw new IllegalArgumentException("Unexpected value: " + p.getType());
		}
	}
	
	/**
	 * Creates a new piece of type p.getType()
	 * @param pType type of piece to duplicate
	 * @param row row of piece on board
	 * @param col column of piece on board
	 * @return a duplicate p with type p.getType()
	 */
	private Piece createPiece(int pType, int row, int col) {
		switch (classifier[pType]) {
			case 'P': 
				return new Pawn(pType, row, col);
			case 'R':
				return new Rook(pType, row, col);
			case 'N':
				return new Knight(pType, row, col);
			case 'B':
				return new Bishop(pType, row, col);
			case 'K':
				return new King(pType, row, col);
			case 'Q': 
				return new Queen(pType, row, col);
			default:
				throw new IllegalArgumentException("Unexpected value: " + classifier[col]);
		}
	}
	
	/**
	 * Creates a new piece based on a character pType and puts it at row and col
	 * @param pType type of piece
	 * @param row of piece on board
	 * @param col column of piece on board
	 */
	private Piece createPiece(char pType, int row, int col) {
		switch (pType) {
			case 'P':
			case 'p':
				return new Pawn(pType, row, col);
			case 'R':
			case 'r':
				return new Rook(pType, row, col);
			case 'N':
			case 'n':
				return new Knight(pType, row, col);
			case 'B':
			case 'b':
				return new Bishop(pType, row, col);
			case 'K':
			case 'k':
				return new King(pType, row, col);
			case 'Q': 
			case 'q':
				return new Queen(pType, row, col);
		default:
			throw new IllegalArgumentException("Unexpected value: " + pType);
		}
	}
	
	/**
	 * Sets a piece at position targPo to be specified type
	 * @param targPo location to set piece
	 * @param type of piece to be created
	 */
	public void setPiece(int[] targPo, char type) {
		HashMap<Piece, List<int[]>> colorPieces;
		if(!board[targPo[1]][targPo[0]].isWhite()) {
			colorPieces = blackPieces;
			type = Character.toLowerCase(type);
		} else {
			colorPieces = whitePieces;
		}
		colorPieces.remove(board[targPo[1]][targPo[0]]);
		board[targPo[1]][targPo[0]] = createPiece(type, targPo[1], targPo[0]);
		colorPieces.put(board[targPo[1]][targPo[0]], new ArrayList<>());
	}
	
	/**
	 * Toggles the turn of the game
	 */
	public void changeTurn() {
		whiteTurn = !whiteTurn;
	}
	
	public boolean whiteTurn() {
		return whiteTurn;
	}
	
 	/**
	 * Gets the ply of the board
	 * @return ply
	 */
	public int getPly() {
		return ply;
	}
	
	public ArrayDeque<String> getUndoFEN() {
		return undoFEN;
	}

	/**
	 * Gets if there are moves available for the current player's turn. If
	 * moves available == 0, game is over
	 * @return true if there are moves available for the current player
	 */
	public boolean movesAvailable() {
		return movesAvailable;
	}
	
	public void clearPieces() {
		for(Piece p : blackPieces.keySet()) {
			blackPieces.get(p).clear();
		}
		for(Piece p : whitePieces.keySet()) {
			whitePieces.get(p).clear();
		}
	}
	
	public void countPieces() {
		int counted = 0;
		for(Piece[] row : board) { 
			for(Piece  p : row) {
				if(p != null && !p.isCaptured()) {
					counted++;
				}
			}
		}
		int listCount = 0;
		for(Piece p : whitePieces.keySet()) {
			if(!p.isCaptured()) listCount++;
		}
		for(Piece p : blackPieces.keySet()) {
			if(!p.isCaptured()) listCount++;
		}
		if(counted != listCount) {
			System.out.println("Listcount: " + listCount + " not equal to: " + counted);
		}
	}
	
	public ArrayDeque<Piece> getCaptured() {
		return captured;
	}

	public ArrayDeque<Piece> actualCaptured() {
		ArrayDeque<Piece> ad = new ArrayDeque<>();
		for(Piece p : blackPieces.keySet()) {
			if(p.isCaptured()) {
				ad.offer(p);
			}
		}
		for(Piece p : whitePieces.keySet()) {
			if(p.isCaptured()) {
				ad.offer(p);
			}
		}
		return ad;
	}
}
