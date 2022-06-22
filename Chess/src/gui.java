/**
 * The gui class is a visual representation of a chess game. It's meant to be a beginner friendly
 * chess game that highlights legal moves and works in conjunction with the functional Board class.
 * TODO: display piece counter
 * TODO: add piece images
 * @author Allen Jue
 * 6/12/2022
 */

import javax.imageio.ImageIO;
import javax.swing.*; 
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;

public class gui extends JFrame {
	private final String COL_LETTERS = "ABCDEFGH";
	// gui which holds the board and toolbar
	private final JPanel mainGui = new JPanel(new BorderLayout(3, 3)); 
	private final Image[][] pieceIcons = new Image[2][6];
	private final JToolBar tools;
	
	// board which contains the grid of squares (JButtons)
	private JPanel board;
	private JButton[][] boardSquares;
		
	// used for managing the gui
	private JLabel gameText;
	private JLabel turnCounter;
	private List<JButton> hovered = new ArrayList<>();
	private HashSet<JButton> validSquares = new HashSet<>();
	private boolean squareSelected = false;
	private boolean sameSelectedBuffer = false;
	private boolean gameHalt = false;
	private Point lastP = null;
	
	private final String[] testFEN = new String[] {"rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3",
			"8/2b5/2knRP2/2p4p/r7/4N2P/3RK3/8 b - - 0 56",
			"2r1nbk1/5p1p/p5p1/1N2Q3/q3P3/6Pb/2P2P1P/1R2N1K1 w - - 0 33"};
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
				    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
					Board functionalBoard = new Board();
					gui frame = new gui(functionalBoard);
					frame.setMinimumSize(new Dimension(550, 550));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public gui(Board b) {		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainGui.setBorder(new EmptyBorder(5, 5, 5, 5));
		setBounds(100, 100, 550, 550);
		// Insert toolbar
		tools = new JToolBar();
        tools.setFloatable(false);
        mainGui.add(tools, BorderLayout.PAGE_START);
        initializeToolBar(tools, b);
        
        // Create grid layout
		board = new JPanel(new GridLayout(0, 9));
		setContentPane(mainGui);
		mainGui.add(board);	
		JPanel boardConstrain = new JPanel(new GridBagLayout());
        boardConstrain.setBackground(Color.LIGHT_GRAY);
        boardConstrain.add(board);
        mainGui.add(boardConstrain);
        initializeIcons();
		createBoard(b);
        initializeBoard(b);
	}
	
	/**
	 * Initializes icon array to contain the png for black and white chess pieces
	 */
	private void initializeIcons() {
		try {
			// white pieces
			// TODO implement switching between funny and styled icons
//			pieceIcons[0][0] = ImageIO.read(getClass().getResource("/Funny-icons/whitePawn.png"));
//			pieceIcons[0][1] = ImageIO.read(getClass().getResource("/Funny-icons/whiteKing.png"));
//			pieceIcons[0][2] = ImageIO.read(getClass().getResource("/Funny-icons/whiteKnight.png"));
//			pieceIcons[0][3] = ImageIO.read(getClass().getResource("/Funny-icons/whiteBishop.png"));
//			pieceIcons[0][4] = ImageIO.read(getClass().getResource("/Funny-icons/whiteRook.png"));
//			pieceIcons[0][5] = ImageIO.read(getClass().getResource("/Funny-icons/whiteQueen.png"));
			pieceIcons[0][0] = ImageIO.read(getClass().getResource("/Styled-icons/WhitePawnStyled.png"));
			pieceIcons[0][1] = ImageIO.read(getClass().getResource("/Styled-icons/WhiteKingStyled.png"));
			pieceIcons[0][2] = ImageIO.read(getClass().getResource("/Styled-icons/WhiteKnightStyled.png"));
			pieceIcons[0][3] = ImageIO.read(getClass().getResource("/Styled-icons/WhiteBishopStyled.png"));
			pieceIcons[0][4] = ImageIO.read(getClass().getResource("/Styled-icons/WhiteRookStyled.png"));
			pieceIcons[0][5] = ImageIO.read(getClass().getResource("/Styled-icons/WhiteQueenStyled.png"));
//			// black pieces
//			pieceIcons[1][0] = ImageIO.read(getClass().getResource("/Funny-icons/blackPawn.png"));
//			pieceIcons[1][1] = ImageIO.read(getClass().getResource("/Funny-icons/blackKing.png"));
//			pieceIcons[1][2] = ImageIO.read(getClass().getResource("/Funny-icons/blackKnight.png"));
//			pieceIcons[1][3] = ImageIO.read(getClass().getResource("/Funny-icons/blackBishop.png"));
//			pieceIcons[1][4] = ImageIO.read(getClass().getResource("/Funny-icons/blackRook.png"));
//			pieceIcons[1][5] = ImageIO.read(getClass().getResource("/Funny-icons/blackQueen.png"));
			pieceIcons[1][0] = ImageIO.read(getClass().getResource("/Styled-icons/BlackPawnStyled.png"));
			pieceIcons[1][1] = ImageIO.read(getClass().getResource("/Styled-icons/BlackKingStyled.png"));
			pieceIcons[1][2] = ImageIO.read(getClass().getResource("/Styled-icons/BlackKnightStyled.png"));
			pieceIcons[1][3] = ImageIO.read(getClass().getResource("/Styled-icons/BlackBishopStyled.png"));
			pieceIcons[1][4] = ImageIO.read(getClass().getResource("/Styled-icons/BlackRookStyled.png"));
			pieceIcons[1][5] = ImageIO.read(getClass().getResource("/Styled-icons/BlackQueenStyled.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create an empty board once
	 */
	public final void createBoard(Board b) {
		boardSquares = new JButton[8][8];
        for(int i = 0; i < 8; i++) {
        	for(int j = 0; j < 8; j++) {
        		boardSquares[i][j] = new JButton();
        		boardSquares[i][j].addMouseListener(new MouseListener() {
    				
					@Override
					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// un-highlight the square after exiting if no square selected
						if(!squareSelected && !gameHalt) {
							for(int i = hovered.size() - 1; i >= 0; i--) {
								hovered.get(i).setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
							}
							hovered.clear();
						}
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// if no square selected, then highlight the square
						if(!squareSelected && !gameHalt) {
							JButton c = (JButton)(e.getComponent());
							hovered.add(c);
							c.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
						}
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						if(!gameHalt) {
							// get clicked button
							JButton square = (JButton)(e.getComponent());
							// remove previously highlighted squares
							// take note if the same button was selected. Treat as a de-select
							boolean sameButton = square.getLocation().equals(lastP) && !sameSelectedBuffer;
							boolean move = !sameButton && validSquares.contains(square);
							sameSelectedBuffer = sameButton;
							// toggle clicked
							squareSelected = !sameButton && !move;
							for(JButton jb : validSquares) {
								jb.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
							}
							validSquares.clear();
							if(move) { // perform a move
								moveSquare(square, b);
							} else if (!sameButton) { // if new button selected then find valid moves
								selectSquare(square, b);
								lastP = square.getLocation();
							}
						}
					}
				});
        	}
        }
        
        turnCounter = new JLabel((b.getTurn() + " turn"), SwingConstants.CENTER);
		// top-left corner depicts the turn of the player
		board.add(turnCounter);
		
		// add column of letters to the board
		for(int i = 0; i < 8; i++) {
			board.add(new JLabel(COL_LETTERS.substring(i, i + 1), SwingConstants.CENTER));
		}
		
		// add the rest of the board to the board gui
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				if(j == 0) {
					// left column, make it the numbers
					 board.add(new JLabel("" + (8 - i), SwingConstants.CENTER));
				} 
				board.add(boardSquares[j][i]);
			}
		}
	}
	
	/**
	 * Create a board with an array of button squares given a starting board position
	 * @param b starting board position
	 */
	public final void initializeBoard(Board b) {
		Dimension buttonSize = new Dimension(50, 50);
		Insets margInsets = new Insets(1, 1, 1, 1);
		// populate the board with colors and bit pieces
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				JButton button = boardSquares[i][j];
				button.setMargin(margInsets);
				button.setPreferredSize(buttonSize);
				// set buttons white and black depending on position in grid and
				// create piece names
				if((i % 2 == 0 && j % 2 == 0)
						|| (i % 2 == 1 && j % 2 == 1)) {
					button.setBackground(Color.WHITE);
				} else {
					button.setBackground(Color.BLACK);
				}
				if(b.getPiece(j, i) != null) {
					try {
					    Image img = getImage(b, j, i);
					    button.setIcon(new ImageIcon(img));
					  } catch (Exception ex) {
					    System.out.println(ex);
					  }
					// boardSquares[i][j].setText(b.getPieceName(j, i));
				} else {
					button.setIcon(null);
				}
			}
		}
	}
	
	/**
	 * Gets the icon from pieceIcons given a pieces row and column
	 * @param b functional board
	 * @param j row of the piece, j because it corresponds to the y-axis of a grid
	 * @param i column of the piece, i because it corresponds to the x-axis of a grid
	 * @return the piece icon for a piece, null if it doesn't exist
	 */
	private Image getImage(Board b, int j, int i) {
		Image img;
	    switch(b.getPiece(j, i).getType()) {
	    	case 'P': 
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][0] : pieceIcons[1][0];
	    		break;
	    	case 'K':
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][1] : pieceIcons[1][1];
	    		break;
	    	case 'N':
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][2] : pieceIcons[1][2];
	    		break;
	    	case 'B':
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][3] : pieceIcons[1][3];
	    		break;
	    	case 'R':
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][4] : pieceIcons[1][4];
	    		break;
	    	case 'Q':
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][5] : pieceIcons[1][5];
	    		break;
	    	default: 
	    		img = null;
	    }
	    return img;
	}
	/**
	 * Create a tool bar with the functionality of restarting game
	 * TODO: resign, draw, and restore
	 * @param toolBar Toolbar with buttons
	 * @param b Board that needs to be reset when reset button selected
	 */
	public final void initializeToolBar(JToolBar toolBar, Board b) {
		toolBar.add(new JLabel("AJAX"));
		toolBar.addSeparator();
		Action newGameButton = new AbstractAction("New Game") {
            @Override
            public void actionPerformed(ActionEvent e) {
            	reset(b);
            }
        };
        toolBar.add(newGameButton);
		toolBar.add(new JButton("Resign"));
		toolBar.addSeparator();
		Action undoButton = new AbstractAction("Undo") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				b.undoMove();
				initializeBoard(b);
				turnCounter.setText(b.getTurn() + " turn");
				boolean movesAvailable = b.generateAllMoves();
				// no more moves, either a stalemate or checkmate
				if(!movesAvailable) {
					gameHalt = true;
					String winner = b.getTurn().equals("W") ? "B" : "W";
					gameText.setText("Game over. " +  winner + " wins!");
				} else {
					gameHalt = false;
					gameText.setText("Welcome to my chess game.");

				}
			}
		};
		toolBar.add(undoButton);
		Action redoButton = new AbstractAction("Redo") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				b.redoMove();
				initializeBoard(b);
				turnCounter.setText(b.getTurn() + " turn");
				boolean movesAvailable = b.generateAllMoves();
				// no more moves, either a stalemate or checkmate
				if(!movesAvailable) {
					gameHalt = true;
					String winner = b.getTurn().equals("W") ? "B" : "W";
					gameText.setText("Game over. " +  winner + " wins!");
				} else {
					gameHalt = false;
					gameText.setText("Welcome to my chess game.");

				}
			}
		};
		toolBar.add(redoButton);
		toolBar.addSeparator();
		gameText = new JLabel("Welcome to my chess game.");
		toolBar.add(gameText);
		
	}
	
	/**
	 * Start the game over. Makes a new board
	 * @param b Board to be reset and re-initialized
	 */
	private final void reset(Board b) {
		b.reset();
    	initializeBoard(b);
    	// make all borders gray again
    	for(int i = hovered.size() - 1; i >= 0; i--) {
			hovered.get(i).setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}
		hovered.clear();
		for(JButton jb : validSquares) {
			jb.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		}
		// reset turn counter and other fields
		turnCounter.setText(b.getTurn() + " turn");
		validSquares.clear();
		squareSelected = false;
		sameSelectedBuffer = false;
		gameHalt = false;
		lastP = null;
		
	}
	/**
	 * Selects a square to be highlighted and displays its valid moves
	 * @param square Selected square to have moves displayed
	 * @param b Board of square buttons
	 */
	private void selectSquare(JButton square, Board b) {
		if(!gameHalt) {
			square.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
			// get location of clicked square
			Point p = square.getLocation();
			validSquares.add(square);
			// find valid moves
			List<int[]> moves = b.getMoves(p.getY(), p.getX());
			for(int[] m : moves) {
				// System.out.println(m[0] + " " + m[1]);
				validSquares.add(boardSquares[m[1]][m[0]]);
				boardSquares[m[1]][m[0]].setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
			}
		}
	}
	
	/**
	 * Moves a piece from previously selected piece to a target position on board, b
	 * @param target final destination of piece
	 * @param b board with all the square buttons
	 */
	private void moveSquare(JButton target, Board b) {
		if(!gameHalt) {
			// gets location of last selected and target location
			Point tp = target.getLocation();
			int[] prevPo = b.getPosition(lastP.getX(), lastP.getY());
			int[] targPo = b.getPosition(tp.getX(), tp.getY());

			if(b.getPiece(prevPo[1], prevPo[0]).isKing() && Math.abs(prevPo[0] - targPo[0]) > 1) {
				moveCastlingRook(b, prevPo, targPo);
			}
			// if enpassant occurred remove the captured pieces icon * it's a special case
			if(b.enPassantOccurred(b, prevPo, targPo)) { 
				boardSquares[targPo[0]][prevPo[1]].setIcon(null);
			}
			// move the piece on the functional board
			b.move(prevPo, targPo);
			
			// update the gui to display the text based on the updates in the functional board
			boardSquares[targPo[0]][targPo[1]].setIcon(boardSquares[prevPo[0]][prevPo[1]].getIcon());
			boardSquares[prevPo[0]][prevPo[1]].setIcon(null); 
			
			boolean movesAvailable = b.generateAllMoves();
			// no more moves, either a stalemate or checkmate
			if(!movesAvailable || b.promotionOccurred(b, targPo)) {
				gameHalt = true;
				String winner = b.getTurn().equals("W") ? "B" : "W";
				gameText.setText("Game over. " +  winner + " wins!");
			}
			System.out.println(b);
			turnCounter.setText((b.getTurn() + " turn"));	
		}
	}
	
	/**
	 * Castle a rook. Under the assumption that the squares of safe
	 * @param b functional board
	 * @param prevPo previous rook position
	 * @param targPo target rook position
	 */
	private void moveCastlingRook(Board b, int[] prevPo, int[] targPo) {
		// if castling, the king will move more than one square, move the rook as well
		int[] rookPrevPo = new int[2];
		int[] rookTargPo = new int[2];
		rookPrevPo[1] = prevPo[1];
		rookTargPo[1] = prevPo[1];

		// queen side castling, rook should end in column 4
		if(targPo[0] == 2) {
			rookPrevPo[0] = 0;
			rookTargPo[0] = 3;
		} else {
			rookPrevPo[0] = 7;
			rookTargPo[0] = 5;
		}
		b.move(rookPrevPo, rookTargPo);
		boardSquares[rookTargPo[0]][rookTargPo[1]].setIcon(boardSquares[rookPrevPo[0]][rookPrevPo[1]].getIcon());
		boardSquares[rookPrevPo[0]][rookPrevPo[1]].setIcon(null);
	}
}
