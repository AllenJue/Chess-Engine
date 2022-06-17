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
import javax.swing.plaf.metal.MetalBorders.ToolBarBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;

public class gui extends JFrame {
	private final String COL_LETTERS = "ABCDEFGH";
	// gui which holds the board and toolbar
	private final JPanel mainGui = new JPanel(new BorderLayout(3, 3)); 
	private final Image[][] pieceIcons = new Image[2][6];
	
	// board which contains the grid of squares (JButtons)
	private JPanel board;
	private JButton[][] boardSquares;
		
	// used for managing the gui
	private JLabel turnCounter;
	private List<JButton> hovered = new ArrayList<>();
	private HashSet<JButton> validSquares = new HashSet<>();
	private boolean squareSelected = false;
	private boolean sameSelectedBuffer = false;
	private boolean establishedBorder = false;
	private Point lastP = null;
	
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
		JToolBar tools = new JToolBar();
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
			pieceIcons[0][0] = ImageIO.read(getClass().getResource("/whitePawn.png"));
			pieceIcons[0][1] = ImageIO.read(getClass().getResource("/whiteKing.png"));
			pieceIcons[0][2] = ImageIO.read(getClass().getResource("/whiteKnight.png"));
			pieceIcons[0][3] = ImageIO.read(getClass().getResource("/whiteBishop.png"));
			pieceIcons[0][4] = ImageIO.read(getClass().getResource("/whiteRook.png"));
			pieceIcons[0][5] = ImageIO.read(getClass().getResource("/whiteQueen.png"));
			// black pieces
			pieceIcons[1][0] = ImageIO.read(getClass().getResource("/blackPawn.png"));
			pieceIcons[1][1] = ImageIO.read(getClass().getResource("/blackKing.png"));
			pieceIcons[1][2] = ImageIO.read(getClass().getResource("/blackKnight.png"));
			pieceIcons[1][3] = ImageIO.read(getClass().getResource("/blackBishop.png"));
			pieceIcons[1][4] = ImageIO.read(getClass().getResource("/blackRook.png"));
			pieceIcons[1][5] = ImageIO.read(getClass().getResource("/blackQueen.png"));
			// pieceIcons[1][0] = ImageIO.read(getClass().getResource("/blackPawn.png"));
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
						if(!squareSelected) {
							for(int i = hovered.size() - 1; i >= 0; i--) {
								hovered.get(i).setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
							}
							hovered.clear();
						}
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// if no square selected, then highlight the square
						if(!squareSelected) {
							JButton c = (JButton)(e.getComponent());
							hovered.add(c);
							c.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
						}
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
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
						// System.out.println(b);
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
	    	case "P": 
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][0] : pieceIcons[1][0];
	    		break;
	    	case "K":
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][1] : pieceIcons[1][1];
	    		break;
	    	case "N":
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][2] : pieceIcons[1][2];
	    		break;
	    	case "B":
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][3] : pieceIcons[1][3];
	    		break;
	    	case "R":
	    		img = b.getPiece(j, i).isWhite() ? pieceIcons[0][4] : pieceIcons[1][4];
	    		break;
	    	case "Q":
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
		lastP = null;
		
	}
	/**
	 * Selects a square to be highlighted and displays its valid moves
	 * @param square Selected square to have moves displayed
	 * @param b Board of square buttons
	 */
	private void selectSquare(JButton square, Board b) {
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
	
	/**
	 * Moves a piece from previously selected piece to a target position on board, b
	 * @param target final destination of piece
	 * @param b board with all the square buttons
	 */
	private void moveSquare(JButton target, Board b) {
		// gets location of last selected and target location
		Point tp = target.getLocation();
		int[] prevPo = b.getPosition(lastP.getX(), lastP.getY());
		int[] targPo = b.getPosition(tp.getX(), tp.getY());

		if(b.getPiece(prevPo[1], prevPo[0]).isKing() && Math.abs(prevPo[0] - targPo[0]) > 1) {
			moveCastlingRook(b, prevPo, targPo);
		}
		// if enpassant occurred remove the captured pieces icon * it's a special case
		if(b.enPassantOccurred(b, prevPo, targPo)) { 
			int colChange = prevPo[0] > targPo[0] ? -1 : 1;
			boardSquares[prevPo[1]][prevPo[0] + colChange].setIcon(null);
		}
		// move the piece on the functional board
		b.move(prevPo, targPo);
		
		// update the gui to display the text based on the updates in the functional board
		// boardSquares[targPo[0]][targPo[1]].setText(boardSquares[prevPo[0]][prevPo[1]].getText());
		boardSquares[targPo[0]][targPo[1]].setIcon(boardSquares[prevPo[0]][prevPo[1]].getIcon());
		boardSquares[prevPo[0]][prevPo[1]].setIcon(null); 
		
		b.changeTurn();
		turnCounter.setText((b.getTurn() + " turn"));	
	}
	
	
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
