/**
 * Pawn constructor which can generate a moveset given a position of a pawn relative to other pieces on a Board
 * TODO: en_passant
 * @author Allen Jue
 * 6/12/2022
 */
import java.util.List;

public class Pawn extends Piece {
    private int en_passant;
    public static final int[] PAWN_DIR = new int[] {-1, 1};
    private boolean promoted;
    private Piece promotedPiece;
    
    /**
     * Constructor that creates a new pawn in the given row
     * @param pType
     * @param row
     */
    public Pawn(int pType, int row, int col) {
        super(pType, row, col);
        promoted = false;
        promotedPiece = null;
        en_passant = 0;
    }

    
    /**
     * Constructor that creates a pawn from a given piece 
     * @param p piece to be copied
     */
    public Pawn(Piece p) {
        super(p);
        promoted = false;
        promotedPiece = null;
        en_passant = 0;
    }
    
    
    /**
     * Constructor for a specific type at row and col
     * @param pType
     * @param row
     * @param col
     */
    public Pawn(char pType, int row, int col) {
        super(pType, row, col);
        promoted = false;
        promotedPiece = null;
    }
    
    
    /**
     * Gets the valid pawn moves if it is the correct turn
     * @param p pawn selected
     * @param i current row
     * @param j current column
     * @return a list of coordinates of the valid pawn moves
     */
    public List<int[]> getMoves(Board b, List<int[]> moves, Piece p, int i, int j) {
        if(correctTurn(b)) {
            if(promoted) {
                promotedPiece.getMoves(b, moves, p, i, j);
            } else {
                // pawns can move one square depending on their color
                int forwardMove = p.isWhite() ? i - 1 : i + 1;
                // if the pawn is on its first rank, it can move two squares ahead
                int firstMove = p.isWhite() ? i - 2 : i + 2;
                
                // check if pawn is on it first rank and if moving two squares ahead is in bounds and not blocked
                if((p.isWhite() && i == 6 || !p.isWhite() && i == 1) && b.inBounds(firstMove, j)  
                        && b.isEmpty(forwardMove, j) && b.isEmpty(firstMove, j)
                        && b.kingSafeWithMove(isWhite(), p.getRow(), p.getCol(), firstMove, j)) {
                    moves.add(new int[] {firstMove, j});
                } 
                
                if(b.inBounds(forwardMove, j) && b.isEmpty(forwardMove, j)
                        && b.kingSafeWithMove(isWhite(), p.getRow(), p.getCol(), forwardMove, j)) {
                    moves.add(new int[] {forwardMove, j});
                }
                // for left and right capture, check if inbounds, !empty, is a piece of the opposite color
                // and doesn't leave the king in check
                addCaptures(b, p, moves, i, j, forwardMove);
                addEnPassant(b, p , moves, i, j, forwardMove);
            }
        }
        return moves;
    }
    
    
    /**
     * Gets the enpassant moves
     * @param b functional board
     * @param p pawn
     * @param moves list of moves
     * @param i current row
     * @param j current column
     * @param forwardMove direction of forward move
     */
    private void addEnPassant(Board b, Piece p, List<int[]> moves, int i, int j, int forwardMove) {
        for(int k = 0; k < PAWN_DIR.length; k++) {
            int colChange = j + PAWN_DIR[k];
            // add an en_passant capture if adjacent to the pawn, there is an enemy pawn with a ply 
            // that is exactly one turn before the current
            if(b.inBounds(i, colChange) && !b.isEmpty(i, colChange)) {
                Piece target = b.getPiece(i, colChange);
                if(target.isWhite() != p.isWhite() && target.isPawn() 
                        && target.getPly() == b.getPly() - 1
                        && b.kingSafeWithMove(isWhite(), p.getRow(), p.getCol(), forwardMove, colChange)) {
                    int rowChange = p.isWhite() ? -1 : 1;
                    moves.add(new int[] {i + rowChange, colChange});
                }
            }
        }
    }

    
    /**
     * Gets the capture moves for a pawn
     * @param b functional board
     * @param p pawn piece
     * @param moves movset
     * @param i row of pawn
     * @param j col of pawn
     * @param forwardMove row change of pawn
     */
    private void addCaptures(Board b, Piece p, List<int[]> moves, int i, int j, int forwardMove) {
        for(int k = 0; k < PAWN_DIR.length; k++) {
            int colChange = j + PAWN_DIR[k];
            // add a capture if diagonal is an enemy piece and leaves the king in a safe position
            if(b.inBounds(forwardMove, colChange) && !b.isEmpty(forwardMove, colChange)) {
                Piece target = b.getPiece(forwardMove, colChange);
                if(target.isWhite() != p.isWhite()
                        && b.kingSafeWithMove(isWhite(), p.getRow(), p.getCol(), forwardMove, colChange)) { 
                    moves.add(new int[] {forwardMove, colChange});
                }
            }
        }
    }
    
    
    /**
     * Sets the en_passant ply of a pawn to the board state
     * @param b functional board
     */
    public void setPly(Board b) {
        this.en_passant = b.getPly();
    }
    
    
    /**
     * Sets the en_passant ply of a pawn to a specified int
     * @param ply specified ply
     */ 
    public void setPly(int ply) {
        this.en_passant = ply;
    }
    
    
    /**
     * Gets the ply of the piece when it was last moved two squares
     */
    public int getPly() {
        return en_passant;
    }
    
    
    /**
     * Gets the type of the piece 
     * @return 'Q' if it is promoted and 'P' is not promoted
     */
    public char getType() {
        return isPromoted() ? 'Q' : 'P';
    }
    
    
    /**
     * Automatically promote a pawn to a queen. The piece will now be considered a Queen
     */
    public void automaticPromote(int row, int col) {
        promoted = true;
        char pType = isWhite() ? 'Q' : 'q';
        promotedPiece = new Queen(pType, row, col);
    }
    
    
    /**
     * Gets if a pawn is promoted
     * @return this.promoted
     */
    public boolean isPromoted() {
        return promoted;
    }
    
    
    /**
     * Unpromotes a pawn. Used when undoing a promotion move
     */
    public void depromote() {
        promoted = false;
        promotedPiece = null;
    }
}
