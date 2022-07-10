# Chess-Engine
<img width="751" alt="Screen Shot 2022-07-07 at 12 10 55 PM" src="https://user-images.githubusercontent.com/66751933/177831277-7d1a0074-95bf-46f3-b477-e46a125d12eb.png">

## Introduction
This Chess engine is an independent passion project designed to explore the applications of search algorithms in Chess. The program creates an interactive, turn-based GUI that allows for casual play and analysis of games with alpha-beta pruning.

## Features
This Chess engine is designed to provide suggestions for moves and is intended to be user-friendly. Legal moves are highlighted in yellow, and selected pieces are highlighted in green. This allows novice players to identify how the pieces move and enjoy playing chess in a more streamlined fashion. 

<img width="746" alt="Screen Shot 2022-07-07 at 12 07 47 PM" src="https://user-images.githubusercontent.com/66751933/177836076-5ad1b184-d165-4ca1-b774-3a212b867407.png">

In the toolbar, helpful game information is given, such as the current player's turn, the state of the game (ongoing, checkmate, stalemate, or draw). A ```New Game``` button resets the state of the game. The ```Resign``` button forfeits the game from the current player's persective (TODO). The ```Undo``` and ```Redo``` buttons allow for successive moves to be undone and redone; they are implemented with a stack that is cleared when a new move is inputted.

The toolbar's text changes when certain board states occur, such as checkmate. The minimax algorithm's outputs are also inputted into the toolbar, which gives the current evaluation of the board. Currently, the engine can only search reasonably to a depth of 5.

<img width="747" alt="Screen Shot 2022-07-07 at 12 08 23 PM" src="https://user-images.githubusercontent.com/66751933/177836508-92449fc6-5218-462e-8886-ca5ccb63a6aa.png">

## To-do
* Create a visual list of suggested moves given the state of the board
* Create a visual list of captured pieces
* Create a list of moves to create a PGN and allow the engine to recreate a game based on a PGN
* Transition the search algorithm to a deep-learning algorithm
* Make the game window scalable
* Implement resign button
* Create a CPU to play against
* Re-implement the promotion popup, as currently pawns that promote are automatically promoted to queens

## Technologies
* Java 8 (```java version "1.8.0_301"
Java(TM) SE Runtime Environment (build 1.8.0_301-b09)
Java HotSpot(TM) 64-Bit Server VM (build 25.301-b09, mixed mode)``` preferred)
* Java Swing
* Java FX

## Setup and Launch
Download the code as a zip and navigate to the bin directory. From there run:

```java gui``` 

to launch the chess game.
