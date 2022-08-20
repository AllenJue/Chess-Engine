/**
 * The Piece class lays the framework for how most of the other chess pieces move
 * It provides constructors for pieces that are entirely new or should be copies of a Piece
 * on an existing board. Most importantly, the classes that extend from Piece (e.g. Pawn, Bishop, Rook,
 * Queen, and Knight) all must implement the getDirMove method, which generates a specific moveset
 * with respect to its piece type and the state of the board.
 * @author Allen Jue
 * 6/12/2022
 */

import java.util.List;
public abstract class Piece {
    public static final int[] DIAGONAL_DIR = new int[] {-1, -1, 1, 1, -1};
    public static final int[] LATERAL_DIR = new int[] {-1, 0 , 1, 0, -1};
    private static final char[] classifier = new char[] {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R', 'P'}; // King, queen, rook, bishop, knight pawn
    private char type;
    private boolean captured;
    private boolean white;
    private final int INITIAL_R;
    private final int INITIAL_C;
    private int r;
    private int c;
    
    /**
     * Constructor for a general piece, with type pType in row
     * @param pType piece type for the Piece object
     * @param row of piece
     * @param col column of piece
     */
    public Piece(int pType, int row, int col) {
        captured = false;
        type = classifier[pType];
        r = row;
        c = col;
        INITIAL_R = r;
        INITIAL_C = c;
        white = row >= 6;
    }
    
    
    /**
     * Constructor that copies the fields of an existing Piece and makes a copy
     * @param p Piece to be copied
     */
    public Piece(Piece p) {
        captured = p.captured;
        type = p.type;
        white = p.white;
        r = p.r;
        c = p.c;
        INITIAL_R = p.INITIAL_R;
        INITIAL_C = p.INITIAL_C;
    }
    
    
    /**
     * Constructor for piece based on FEN character
     * @param pType type of piece as a character (lower case is black)
     * @param row of piece
     * @param col column of piece
     */
    public Piece(char pType, int row, int col) {
        captured = false;
        r = row;
        c = col;
        INITIAL_R = r;
        INITIAL_C = c;
        white = Character.isUpperCase(pType);
        type = Character.toUpperCase(pType);
    }
    
    
    /**
     * Gets the String representation of a Piece, which includes: type and color
     * @return "[type, color = this.getColor()]"
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(getType());
        sb.append(", ");
        sb.append("color = ");
        sb.append(this.getColor());
        sb.append(", loc: ");
        sb.append(r + " " + c);
        sb.append(", captured: " + captured);
        sb.append(", is promoted: " + isPromoted());
        sb.append(", castling rights: " + getCastlingRights());
        sb.append(']');
        return sb.toString();
    }
    
    
    /**
     * Gets the character representation of a piece
     * @return the type of the piece
     */
    public char getType() {
        return this.type;
    }
    
    
    /**
     * Gets if a piece is white
     * @return white
     */
    public boolean isWhite() {
        return white;
    }
    
    
    /**
     * Gets color of a piece
     * @return if(isWhite()) return "W" : "B"
     */
    public String getColor() {
        return isWhite() ? "W" : "B";
    }
    
    
    /**
     * Parent method for getting the moveset of a piece selected
     * Will be overridden in specially created inherited classes
     * @param b functional board
     * @param p piece selected
     * @param i starting position row
     * @param j starting position row
     * @return a list of valid coordinates to move to
     */
    public abstract List<int[]> getMoves(Board b, List<int[]> moves, Piece p, int i, int j);
    
    
    /**
     * Gets the valid moves along a specified direction given a piece and it position
     * @param moves the collection of valid moves alonga specified diagonal
     * @param rowChange direction the row is changing (down or up)
     * @param colChange direction the column is changing (left or right)
     * @param i starting row
     * @param j starting column
     */
    public void getDirMoves(Board b, List<int[]> moves, Piece p, int rowChange, int colChange, int i, int j) {
        int row = i + rowChange;
        int col = j + colChange;
        // search all moves in a given diagonal until off the board or a collision occurs
        while(b.inBounds(row, col) && b.isEmpty(row, col)) {
            if(b.kingSafeWithMove(p.isWhite(), i, j, row, col)) {
                moves.add(new int[] {row, col});
            }
            row += rowChange;
            col += colChange;
        }
        // if meeting an opposite color piece, valid move is to capture
        if(b.inBounds(row, col) && b.getPiece(row, col).isWhite() != b.getPiece(i, j).isWhite()
                && b.kingSafeWithMove(p.isWhite(), i, j, row, col)) {
            moves.add(new int[] {row, col});
        }
    }
    
    
    /**
     * Gets if the two pieces selected are different colors
     * @param p first piece
     * @param p2 second piece
     * @return first piece color != second piece color
     */
    public boolean diffColor(Piece p, Piece p2) {
        return p2 == null || !p.getColor().equals(p2.getColor());
    }
    
    
    /**
     * Returns true if the piece selected is the correct turn
     * @param b functional board
     * @return this.getColor() is the same as b.getTurn()
     */
    public boolean correctTurn(Board b) {
        return this.getColor().equals(b.getTurn()); 
    }
    
    
    /**
     * Sets castling rights for King and Rook
     */
    public void setCastlingRights(boolean change) {
        // holder for King and rook; other pieces can not castle
    }
    
    
    /**
     * Sets the plt of a piece to a board state
     * @param b functional board
     */
    public void setPly(Board b) {
        // holder for Pawn setting ply
    }
    
    
    /**
     * Sets the ply of a piece to a specified number
     * @param ply of piece move
     */
    public void setPly(int ply) {
        // holder for Pawn setting ply
    }
    
    
    /**
     * Gets castling rights for King and ROok
     * @return if the King and Rook can castle
     */
    public boolean getCastlingRights() {
        // holder for King and rook; other pieces can not castle
        return false;
    }
    
    
    /**
     * Gets if a piece is a queen type
     * @return this.type == 'Q'
     */
    public boolean isQueen() {
        return this.type == 'Q' || (this.type == 'P' && isPromoted());
    }
    
    
    /**
     * Gets if a piece is a pawn type
     * @return this.type == 'P'
     */
    public boolean isPawn() {
        return this.type == 'P' && !isPromoted();
    }
    
    
    /**
     * Gets if a piece is a King type
     * @return this.type == 'K'
     */
    public boolean isKing() {
        return this.type == 'K';
    }
    
    
    /**
     * Gets if a piece is a knight type
     * @return this.type == 'N'
     */
    public boolean isKnight() {
        return this.type == 'N';
    }
    
    
    /**
     * Gets if a piece is a bishop type
     * @return this.type == 'B'
     */
    public boolean isBishop() {
        return this.type == 'B';
    }
    
    
    /**
     * Gets if a piece is a rook type
     * @return this.type == 'R'
     */
    public boolean isRook() {
        return this.type == 'R';
    }

    
    /**
     * Updates row and column of a piece to be a new location
     * @param i new row for a piece
     * @param j new column for a piece
     */
    public void setLocations(int i, int j) {
        this.r = i;
        this.c = j;
    }
    
    
    /**
     * Gets the row location of a piece
     * @return this.r
     */
    public int getRow() {
        return this.r;
    }
    
    
    /**
     * Gets the col location of a piece
     * @return this.c
     */
    public int getCol() {
        return this.c;
    }
    
    
    /**
     * Changes the type of the piece. Used for promotion
     * @param pTpye new type for piece
     */
    public void changeType(char pTpye) {
        type = pTpye;
    }
    
    
    /**
     * Gets the ply of the piece (when it was last moved)
     * @return the move that the piece was last moved
     */
    public int getPly() {
        return -1;
    }
    
    
    /**
     * Gets if a piece is captured
     * @return this.captured
     */
    public boolean isCaptured() {
        return captured;
    }
    
    
    /**
     * Capture a piece by toggling its captured field to true
     */
    public void capture() {
        captured = true;
    }
    
    
    /**
     * Uncapture a piece by toggling its captured field to false
     */
    public void uncapture() {
        captured = false;
    }
    
    
    /**
     * Promotes a piece automatically
     * @param row of promotion
     * @param col of promotion
     */
    public void automaticPromote(int row, int col) {
        // placeholder for pawn promotion
    }
    
    
    /**
     * Gets if a piece is promoted
     * @return false for any piece that's not a pawn
     */
    public boolean isPromoted() {
        return false;
    }
    
    
    /**
     * Depromotes a piece back into a pawn
     */
    public void depromote() {
        // placeholder for pawn depromotion
    }
    
    
    /**
     * Hashcode depends on the color of a piece and its initial row and column.
     * Needed for HashMap comparator to identify the same piece
     * @return int hashcode for a piece
     */
    @Override
    public int hashCode() {
        int code = 0;
        code += white ? 11 : 10;
        code *= 100;
        code += INITIAL_R;
        code *= 10;
        code += INITIAL_C;
        return code;
    }
    
    
    /**
     * Gets the initial row of a piece
     * @return INITIAL_R
     */
    public int getInitialR() {
        return INITIAL_R;
    }
    
    
    /**
     * Gets the initial column of a piece
     * @return INITIAL_C
     */
    public int getInitialC() {
        return INITIAL_C;
    }
    
    
    /**
     * Gets if a piece is equal to another piece
     */
    @Override
    public  boolean equals(Object obj) {
        if(obj instanceof Piece) {
            Piece p = (Piece)obj;
            return p.getInitialR() == this.getInitialR() && p.getInitialC() == this.getInitialC();
        }
        return false;
    }
}