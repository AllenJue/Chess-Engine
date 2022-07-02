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
	private JPanel promotionPanel;
	// board which contains the grid of squares (JButtons)
	private Board functionalBoard;
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
	private boolean gameOver = false;
	private Point lastP = null;
	private int[] promoteSquare = null;
	private int[] previousSquare = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
				    UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
				    String[] buttonTexts = {"first","second"}; //create the button texts here

				  //display a modal dialog with your buttons (stops program until user selects a button)
//				  int userDecision =  JOptionPane.showOptionDialog(null,"title","Select a button!",JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE,null,buttonTexts,buttonTexts[0]);

				  //check what button the user selected: stored in the userDecision
				  // if its the first (left to right) its 0, if its the second then the value is 1 and so on

//				  if(userDecision == 0){
//				    //first button was clicked, do something
//				  } else if(userDecision == 1) {
//				    //second button was clicked, do something
//				  } else {
//				   //user canceled the dialog
//				  }
					Board functionalBoard = new Board();
					functionalBoard.clearPieces();
					functionalBoard.generateAllMoves();
					gui frame = new gui(functionalBoard);
					frame.setMinimumSize(new Dimension(750, 550));
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
		setBounds(100, 100, 750, 550);
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
		functionalBoard = b;
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
						if(!gameHalt && !gameHalt) {
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
				// b.undoMoveWithFEN();
				initializeBoard(b);
				for(JButton jb : validSquares) {
					jb.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
				}
				validSquares.clear();
				turnCounter.setText(b.getTurn() + " turn");
				boolean movesAvailable = b.generateAllMoves();
				gameHalt = false;
				// no more moves, either a stalemate or checkmate
				if(!movesAvailable) {
					gameOver = true;
					String winner = b.whiteTurn()? "B" : "W";
					gameText.setText("Game over. " +  winner + " wins!");
				} else {
					gameOver = false;
					gameText.setText("Welcome to my chess game.");

				}
			}
		};
		toolBar.add(undoButton);
		Action redoButton = new AbstractAction("Redo") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				b.redoMoveWithFEN();
				initializeBoard(b);
				turnCounter.setText(b.getTurn() + " turn");
				for(JButton jb : validSquares) {
					jb.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
				}
				validSquares.clear();
				boolean movesAvailable = b.generateAllMoves();
				// no more moves, either a stalemate or checkmate
				if(!movesAvailable) {
					gameOver = true;
					String winner = b.whiteTurn() ? "B" : "W";
					gameText.setText("Game over. " +  winner + " wins!");
				} else {
					gameOver = false;
					gameText.setText("Welcome to my chess game.");

				}
			}
		};
		toolBar.add(redoButton);
		toolBar.addSeparator();
		gameText = new JLabel("Welcome to my chess game.");
		toolBar.add(gameText);
		toolBar.addSeparator();
		promotionPanel = new JPanel();
		initializePromotion();
		toolBar.add(promotionPanel);
		promotionPanel.setVisible(false);
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
		gameOver = false;
		lastP = null;
		gameText.setText("Welcome to my chess game.");		
	}
	/**
	 * Selects a square to be highlighted and displays its valid moves
	 * @param square Selected square to have moves displayed
	 * @param b Board of square buttons
	 */
	private void selectSquare(JButton square, Board b) {
		if(!gameOver && !gameHalt) {
			square.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
			// get location of clicked square
			Point p = square.getLocation();
			validSquares.add(square);
			// find valid moves
			int[] pos = b.getPosition(p.getX(), p.getY());
			List<int[]> moves = b.getMoves(p.getY(), p.getX());
			for(int[] m : moves) {
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
		if(!gameOver && !gameHalt) {
			// gets location of last selected and target location
			Point tp = target.getLocation();
			int[] prevPo = b.getPosition(lastP.getX(), lastP.getY());
			int[] targPo = b.getPosition(tp.getX(), tp.getY());
			// if enpassant occurred remove the captured pieces icon * it's a special case
			if(b.enPassantOccurred(b, prevPo, targPo)) { 
				boardSquares[targPo[0]][prevPo[1]].setIcon(null);
			}
			// if pawn is promoting, break and establish the promotion squares
//			if(b.getPiece(prevPo[1], prevPo[0]).isPawn() && (targPo[1] == 0 || targPo[1] == 7)) {
//				promotePiece(prevPo, targPo);
//				return;
//			}
			// move the piece on the functional board
			b.move(prevPo, targPo);
			castleOccurred(b, prevPo, targPo);
			// update the gui to display the text based on the updates in the functional board
			boardSquares[targPo[0]][targPo[1]].setIcon(new ImageIcon(getImage(b, targPo[1], targPo[0])));
			boardSquares[prevPo[0]][prevPo[1]].setIcon(null); 
			boolean movesAvailable = b.movesAvailable();
			// no more moves, either a stalemate or checkmate
			if(!movesAvailable) {
				gameOver = true;
				String winner = b.whiteTurn() ? "B" : "W";
				gameText.setText("Game over. " +  winner + " wins!");
			}
			Minimax mm = new Minimax(b);
			System.out.printf("Eval: %.5f\n", mm.minimax(4, Double.MIN_VALUE, Double.MAX_VALUE, b.whiteTurn()));
			b.clearPieces();
			b.generateAllMoves();
			turnCounter.setText((b.getTurn() + " turn"));	
		}
	}
	
	/**
	 * Updates rook and king icons if castle has occurred
	 * @param b functional board
	 * @param prevPo previous position of castling king
	 * @param targPo target posiiton of castling king
	 */
	private void castleOccurred(Board b, int[] prevPo, int[] targPo) {
		if(Math.abs(targPo[0] - prevPo[0]) > 1 && b.getPiece(targPo[1], targPo[0]).isKing()) {
			if(targPo[0] == 6) {
				boardSquares[5][targPo[1]].setIcon(boardSquares[7][targPo[1]].getIcon()); ;
				boardSquares[7][targPo[1]].setIcon(null);
			} else {
				boardSquares[3][targPo[1]].setIcon(boardSquares[0][targPo[1]].getIcon());
				boardSquares[0][targPo[1]].setIcon(null);
			}
		}
	}
	
	/**
	 * Performs the actual promotion of the pieces by creating the promotion buttons that become visible when a pawn moves
	 * to a promotion square
	 * The game state is halted until a the pawn is selected to have a promotion type
	 */
	private void initializePromotion() {
		Action qPromote = new AbstractAction("Q") {
			public void actionPerformed(ActionEvent e) {
				gameHalt = false;
				// creates a new piece where the pawn was
				functionalBoard.setPiece(previousSquare, 'Q');
				Image img = getImage(functionalBoard, previousSquare[1], previousSquare[0]);
				boardSquares[previousSquare[0]][previousSquare[1]].setIcon(new ImageIcon(img));
				// then moves the piece to where the pawn would've promoted to
				moveSquare(boardSquares[promoteSquare[0]][promoteSquare[1]], functionalBoard);
				promotionPanel.setVisible(false);
			}
		};
		Action bPromote = new AbstractAction("B") {
			public void actionPerformed(ActionEvent e) {
				gameHalt = false;
				functionalBoard.setPiece(previousSquare, 'B');
				Image img = getImage(functionalBoard, previousSquare[1], previousSquare[0]);
				boardSquares[previousSquare[0]][previousSquare[1]].setIcon(new ImageIcon(img));
				// then moves the piece to where the pawn would've promoted to
				moveSquare(boardSquares[promoteSquare[0]][promoteSquare[1]], functionalBoard);
				promotionPanel.setVisible(false);
			}
		};
		Action nPromote = new AbstractAction("N") {
			public void actionPerformed(ActionEvent e) {
				gameHalt = false;
				functionalBoard.setPiece(previousSquare, 'N');
				Image img = getImage(functionalBoard, previousSquare[1], previousSquare[0]);
				boardSquares[previousSquare[0]][previousSquare[1]].setIcon(new ImageIcon(img));
				// then moves the piece to where the pawn would've promoted to
				moveSquare(boardSquares[promoteSquare[0]][promoteSquare[1]], functionalBoard);
				promotionPanel.setVisible(false);
			}
		};
		Action rPromote = new AbstractAction("R") {
			public void actionPerformed(ActionEvent e) {
				gameHalt = false;
				functionalBoard.setPiece(previousSquare, 'R');
				Image img = getImage(functionalBoard, previousSquare[1], previousSquare[0]);
				boardSquares[previousSquare[0]][previousSquare[1]].setIcon(new ImageIcon(img));
				// then moves the piece to where the pawn would've promoted to
				moveSquare(boardSquares[promoteSquare[0]][promoteSquare[1]], functionalBoard);
				promotionPanel.setVisible(false);
			}
		};
		JButton qButton = new JButton(qPromote);
		JButton bButton = new JButton(bPromote);
		JButton nButton = new JButton(nPromote);
		JButton rButton = new JButton(rPromote);
		promotionPanel.add(qButton);
		promotionPanel.add(bButton);
		promotionPanel.add(nButton);
		promotionPanel.add(rButton);
	}
	
	/**
	 * Change the game state to halt until a pawn is promoted from the promotion panel
	 * @param prevPo position of pawn
	 * @param targPo promotion square of pawn
	 */
	private void promotePiece(int[] prevPo, int[] targPo) {
		gameHalt = true;
		gameText.setText("Select a promotion piece");
		previousSquare = prevPo;
		promoteSquare = targPo;
		promotionPanel.setVisible(true);
	}
}
