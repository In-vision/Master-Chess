package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import i18n.I18N;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class ChessBoard extends GridPane {
	public static Space[][] spaces;

	public Space activeSquare = null;	//Ultima casilla clickeada
	public static boolean playerTurn = true; //True para blancas. False para negras
	public Label turn = null;		
	public String playerTurnStr;
	public Space previousWhiteMove = null;
	public Space previousBlackMove = null;
	public MoveList prevWhiteMoveList = null;
	public MoveList prevBlackMoveList = null;
	
	public static boolean isPlayable;
	public boolean pawnHasPromoted = false;
	public boolean castled = false;
	
	public static boolean kingInCheck = false;
	public boolean discoveredCheck = false;

	public boolean checkmate = false;

	public int piecesGivingCheck = 0;
	public ArrayList<Space> whitePieces = new ArrayList<>();
	public HashMap<String, Space> whitePieces2 = new HashMap<>();
	public ArrayList<Space> blackPieces = new ArrayList<>();
	@SuppressWarnings("unused")
	private int langID = 0, voiceID = 0;
	public static int themeID = 0;

	public ArrayList<Space> piecesChecking = new ArrayList<>();
	/** Sound effects **/
	Media standardChessMove = new Media(getClass().getResource("/soundEffects/chessMove.mp3").toExternalForm());
	Media enroqueMedia = new Media(getClass().getResource("/soundEffects/enroque.mp3").toExternalForm());
	Media pallaJaque = new Media(getClass().getResource("/soundEffects/PallaJaque.mp3").toExternalForm());
	Media pallaMate = new Media(getClass().getResource("/soundEffects/PallaMate.mp3").toExternalForm());
	MediaPlayer soundPlayer = new MediaPlayer(standardChessMove);

	/**               **/
	public ChessBoard(boolean colorPieces, int langID, int voiceID, int themeID, boolean isNewGame) {
		super();
		this.langID = langID;
		this.voiceID = voiceID;
		ChessBoard.themeID = themeID;
		/*** Inicializar el tablero ***/
		ChessBoard.isPlayable = true;
		ChessBoard.spaces = new Space[8][8];
		for (int row = 0; row < 8; row++) {
			for (int column = 0; column < 8; column++) {
				boolean isLightSquare = ((row + column) % 2 != 0); //Controla colores de casillas
				ChessBoard.spaces[row][column] = new Space(isLightSquare, row, column, ChessBoard.themeID);
		
				/*
				 * Si es blancas, a�ade al gridPane de manera que la esquina izquierda sea 0,0
				 * Si es negras, a�ade al gridPane de manera que la esquina izquierda sea 7,7
				 */
				if (colorPieces) 	this.add(ChessBoard.spaces[row][column], row, 7 - column);
				else 				this.add(ChessBoard.spaces[row][column], 7 - row, column);
				// Tiene que ser final para que el eventHandler funcione
				final int tempRow = row;
				final int tempColumn = column;
				ChessBoard.spaces[row][column].addEventFilter(
				MouseEvent.MOUSE_CLICKED, (e) -> clickOnSquare(ChessBoard.spaces[tempRow][tempColumn]));
			}
		}
		// Posiciones iniciales de las piezas
		if (isNewGame)
			this.initialPos();
	}

	public ChessBoard(List<String> lines) {
		super();
		ChessBoard.isPlayable = true;
		ChessBoard.spaces = new Space[8][8];
		for (int row = 0; row < 8; row++) {
			for (int column = 0; column < 8; column++) {

				boolean isLightSquare = ((row + column) % 2 != 0); //Controla colores de casillas
				ChessBoard.spaces[row][column] = new Space(isLightSquare, row, column, ChessBoard.themeID);
				/*
				 * Si es blancas, a�ade al gridPane de manera que la esquina izquierda sea 0,0
				 * Si es negras, a�ade al gridPane de manera que la esquina izquierda sea 7,7
				 */
				if (ChessBoard.playerTurn) 	this.add(ChessBoard.spaces[row][column], row, 7 - column);
				else 				this.add(ChessBoard.spaces[row][column], 7 - row, column);
				
				// Tiene que ser final para que el eventHandler funcione
				final int tempRow = row;
				final int tempColumn = column;
				System.out.println(ChessBoard.spaces[row][column]);
				ChessBoard.spaces[row][column].addEventHandler(MouseEvent.MOUSE_CLICKED,
						(e) -> clickOnSquare(ChessBoard.spaces[tempRow][tempColumn]));
			}
		}

		ChessBoard.playerTurn = Boolean.parseBoolean(lines.get(0));
		ChessBoard.kingInCheck = Boolean.parseBoolean(lines.get(1));

		whitePieces.clear();
		blackPieces.clear();
		for (int i = 2; i < lines.size(); i++) {
			String[] pieceInfo = lines.get(i).split(",");
			System.out.println(Arrays.toString(pieceInfo));
			int x = Integer.parseInt(pieceInfo[0]), y = Integer.parseInt(pieceInfo[1]);
			ChessBoard.spaces[x][y].setThreatenedByWhite(Boolean.parseBoolean(pieceInfo[2]));
			ChessBoard.spaces[x][y].setThreatenedByBlack(Boolean.parseBoolean(pieceInfo[3]));
			ChessBoard.spaces[x][y].setWhiteThreads(Integer.parseInt(pieceInfo[4]));
			ChessBoard.spaces[x][y].setBlackThreads(Integer.parseInt(pieceInfo[5]));
			if (!pieceInfo[6].equals("null")) {
				boolean pieceColor = (pieceInfo[7].equals("white") ? true : false);
				if (pieceColor) {
					whitePieces.add(ChessBoard.spaces[x][y]);
				} else {
					blackPieces.add(ChessBoard.spaces[x][y]);
				}
				switch (pieceInfo[6]) {
				case "pawn":
					ChessBoard.spaces[x][y].setPiece(new Pawn(pieceColor));
					break;
				case "rook":
					ChessBoard.spaces[x][y].setPiece(new Rook(pieceColor));
					break;
				case "knight":
					ChessBoard.spaces[x][y].setPiece(new Knight(pieceColor));
					break;
				case "bishop":
					ChessBoard.spaces[x][y].setPiece(new Bishop(pieceColor));
					break;
				case "queen":
					ChessBoard.spaces[x][y].setPiece(new Queen(pieceColor));
					break;
				case "king":
					ChessBoard.spaces[x][y].setPiece(new King(pieceColor));
					break;
				default:
					break;
				}
			}
		}

	}

	public Space getSpace(int x, int y) {
		return ChessBoard.spaces[x][y];
	}

	public void activeSpaceHandler(Space s) {
		if (this.activeSquare != null) {
			this.activeSquare.getStyleClass().removeAll("chess-space-active");
		}
		this.activeSquare = s;

		if (this.activeSquare != null) {
			this.activeSquare.getStyleClass().add("chess-space-active");
		}
	}

	public Space getActiveSpace() {
		return this.activeSquare;
	}

	public void setLabelTurn(Label turn) {
		this.turn = turn;
		this.turn.setAlignment(Pos.CENTER);
		this.turn.setStyle("-fx-font-weight: bold; -fx-font: 14 arial;");
		if (ChessBoard.playerTurn) {
			this.turn.setText("Blancas");
		} else {
			this.turn.setText("Negras");
		}
	}

	public void changePlayerStr() { 
		if (!ChessBoard.playerTurn) {
			this.turn.setText("Negras");
			this.turn.setStyle(
					"-fx-text-fill: white; -fx-background-color: black; -fx-font-weight: bold; -fx-font: 14 arial;");
			this.turn.setAlignment(Pos.CENTER);
		} else {
			this.turn.setText("Blancas");
			this.turn.setStyle(
					"-fx-text-fill: black; -fx-background-color: white; -fx-font-weight: bold; -fx-font: 14 arial;");
			this.turn.setAlignment(Pos.CENTER);
		}

	}

	// prints location of all pieces on the board
	// TODO: Unfinished
	public String toString() {
		String pieceList = "";
		for (int i = 0; i < spaces[0].length; i++) {
			for (int j = 0; j < spaces[1].length; j++) {
				if (spaces[i][j].isOccupied()) {
					// pieceList += spaces[i][j].toString();
				}
			}
		}
		return pieceList;
	}

	public void initialPos() {

		/*	Piezas blancas	*/
		ChessBoard.spaces[0][0].setPiece(new Rook(true));
		ChessBoard.spaces[1][0].setPiece(new Knight(true));
		ChessBoard.spaces[2][0].setPiece(new Bishop(true));
		ChessBoard.spaces[3][0].setPiece(new Queen(true));
		ChessBoard.spaces[4][0].setPiece(new King(true));
		ChessBoard.spaces[5][0].setPiece(new Bishop(true));
		ChessBoard.spaces[6][0].setPiece(new Knight(true));
		ChessBoard.spaces[7][0].setPiece(new Rook(true));
		whitePieces.add(ChessBoard.spaces[0][0]);
		whitePieces.add(ChessBoard.spaces[7][0]);
		whitePieces.add(ChessBoard.spaces[2][0]);
		whitePieces.add(ChessBoard.spaces[5][0]);
		whitePieces.add(ChessBoard.spaces[3][0]);
		whitePieces.add(ChessBoard.spaces[1][0]);
		whitePieces.add(ChessBoard.spaces[4][0]);
		whitePieces.add(ChessBoard.spaces[6][0]);
		for (int i = 0; i < ChessBoard.spaces[0].length; i++) {
			ChessBoard.spaces[i][1].setPiece(new Pawn(true));
			threadSwitch(ChessBoard.spaces[i][1].getPiece(), ChessBoard.spaces[i][1], true, false);
			whitePieces.add(ChessBoard.spaces[i][1]);
		}
		threadSwitch(ChessBoard.spaces[6][0].getPiece(), ChessBoard.spaces[6][0], true, false); //Caballos
		threadSwitch(ChessBoard.spaces[1][0].getPiece(), ChessBoard.spaces[1][0], true, false);
		threadSwitch(ChessBoard.spaces[2][0].getPiece(), ChessBoard.spaces[2][0], true, false); //Alfiles
		threadSwitch(ChessBoard.spaces[5][0].getPiece(), ChessBoard.spaces[5][0], true, false);
		threadSwitch(ChessBoard.spaces[4][0].getPiece(), ChessBoard.spaces[4][0], true, false); //Rey
		threadSwitch(ChessBoard.spaces[3][0].getPiece(), ChessBoard.spaces[3][0], true, false); //Dama
		threadSwitch(ChessBoard.spaces[7][0].getPiece(), ChessBoard.spaces[7][0], true, false);
		threadSwitch(ChessBoard.spaces[0][0].getPiece(), ChessBoard.spaces[0][0], true, false);
		/*	Piezas negras	*/
		ChessBoard.spaces[0][7].setPiece(new Rook(false));
		ChessBoard.spaces[1][7].setPiece(new Knight(false));
		ChessBoard.spaces[2][7].setPiece(new Bishop(false));
		ChessBoard.spaces[3][7].setPiece(new Queen(false));
		ChessBoard.spaces[4][7].setPiece(new King(false));
		ChessBoard.spaces[5][7].setPiece(new Bishop(false));
		ChessBoard.spaces[6][7].setPiece(new Knight(false));
		ChessBoard.spaces[7][7].setPiece(new Rook(false));
		blackPieces.add(ChessBoard.spaces[0][7]);
		blackPieces.add(ChessBoard.spaces[7][7]);
		blackPieces.add(ChessBoard.spaces[2][7]);
		blackPieces.add(ChessBoard.spaces[5][7]);
		blackPieces.add(ChessBoard.spaces[3][7]);
		
		blackPieces.add(ChessBoard.spaces[1][7]);
		blackPieces.add(ChessBoard.spaces[4][7]);
		blackPieces.add(ChessBoard.spaces[6][7]);
		for (int i = 0; i < ChessBoard.spaces[0].length; i++) {
			ChessBoard.spaces[i][6].setPiece(new Pawn(false));
			threadSwitch(ChessBoard.spaces[i][6].getPiece(), ChessBoard.spaces[i][6], true, true);
			blackPieces.add(ChessBoard.spaces[i][6]);

		}
		threadSwitch(ChessBoard.spaces[6][7].getPiece(), ChessBoard.spaces[6][7], true, true);
		threadSwitch(ChessBoard.spaces[1][7].getPiece(), ChessBoard.spaces[1][7], true, true);
		threadSwitch(ChessBoard.spaces[2][7].getPiece(), ChessBoard.spaces[2][7], true, true);
		threadSwitch(ChessBoard.spaces[5][7].getPiece(), ChessBoard.spaces[5][7], true, true);
		threadSwitch(ChessBoard.spaces[4][7].getPiece(), ChessBoard.spaces[4][7], true, true);
		threadSwitch(ChessBoard.spaces[3][7].getPiece(), ChessBoard.spaces[3][7], true, true);
		threadSwitch(ChessBoard.spaces[7][7].getPiece(), ChessBoard.spaces[7][7], true, true);
		threadSwitch(ChessBoard.spaces[0][7].getPiece(), ChessBoard.spaces[0][7], true, true);
	}

	public void clickOnSquare(Space selectedSquare) {
		System.out.println("[" + selectedSquare.getX() + "][" + selectedSquare.getY() + "] threat by white: "
				+ selectedSquare.isThreatenedByWhite() + " - " + selectedSquare.getWhiteThreads());
		System.out.println("       threat by black: " + selectedSquare.isThreatenedByBlack() + " - "
				+ selectedSquare.getBlackThreads());
		System.out.println("       Piece: " + selectedSquare.getPiece());
		int x = selectedSquare.getX(), y = selectedSquare.getY();
		if (this.checkmate || ChessBoard.isPlayable == false)
			return;
		// Si una pieza ha sido seleccionada y no se selecciono en otra pieza del mismo
		// color
		if (this.activeSquare != null && selectedSquare.getPieceColor() != this.activeSquare.getPieceColor()) {
			MoveInfo p;
			p = new MoveInfo(this.activeSquare.getX(), this.activeSquare.getY(), x, y);
			moveProcesser(p);
			// decouples space from space on board
			this.activeSpaceHandler(null);
			// Si no se ha seleccionado una pieza, significa que no hay
			// una casilla activa
		} else if (spaces[x][y].getPiece() != null) {
			// Si la casilla seleccionada no tiene una pieza en ella

			// Si es el turno de blancas
			if (ChessBoard.playerTurn) {
				if (spaces[x][y].getPiece().getColor() == "white") {
					this.activeSpaceHandler(spaces[x][y]);
				}
			}

			// Si es el turno de negras
			else {
				if (spaces[x][y].getPiece().getColor() == "black") {
					this.activeSpaceHandler(spaces[x][y]);
				}
			}
		}
	}

	private void soundEffectHandler () {
		if(!ChessBoard.playerTurn && (prevWhiteMoveList == MoveList.KING_CASTLE_KINGSIDE ||
				prevWhiteMoveList == MoveList.KING_CASTLE_QUEENSIDE)) {
			soundPlayer = new MediaPlayer(enroqueMedia);
			soundPlayer.play();
		}
		else if (ChessBoard.playerTurn && (prevBlackMoveList == MoveList.KING_CASTLE_KINGSIDE ||
				prevBlackMoveList == MoveList.KING_CASTLE_QUEENSIDE)) {
			soundPlayer = new MediaPlayer(enroqueMedia);
			soundPlayer.play();		
		}
		else {
			soundPlayer = new MediaPlayer(standardChessMove);
			soundPlayer.play();
		}
		
		if(this.checkmate) {
			soundPlayer = new MediaPlayer(pallaMate);
			soundPlayer.play();
		}
		else if(ChessBoard.kingInCheck) {
			soundPlayer = new MediaPlayer(pallaJaque);
			soundPlayer.play();
		}
	}
	
	// Procesa una jugada, esto se activa al darle click a una pieza y luego a darle click a otra casilla
	protected boolean moveProcesser(MoveInfo p){
		if (moveIsValid(p, true) && !this.checkmate) {
			ChessBoard.kingInCheck = false;
			this.discoveredCheck = false;
			this.piecesGivingCheck = 0;
			this.piecesChecking.clear();

			Space oldSquare = spaces[p.getOldX()][p.getOldY()];
			Space newSquare = spaces[p.getNewX()][p.getNewY()];
			previousWhiteMove = (ChessBoard.playerTurn) ? newSquare: previousWhiteMove;
			previousBlackMove = (!ChessBoard.playerTurn)? newSquare: previousBlackMove;

			Piece movedPiece = oldSquare.releasePiece();
			Piece previousTakenPiece = null;
			threadSwitch(movedPiece, oldSquare, false, false); //Quitar casillas que la pieza amenazaba
			if(this.pawnHasPromoted && ChessBoard.playerTurn) {
				previousTakenPiece = newSquare.releasePiece();
				newSquare.setPiece(new Queen(true));
				whitePieces.remove(oldSquare);
				movedPiece = newSquare.getPiece();
				this.pawnHasPromoted = false;
				whitePieces.add(newSquare);
				blackPieces.remove(newSquare);
			}
			else if(this.pawnHasPromoted && !ChessBoard.playerTurn) {
				previousTakenPiece = newSquare.releasePiece();
				newSquare.setPiece(new Queen(false));
				blackPieces.remove(oldSquare);
				movedPiece = newSquare.getPiece();
				this.pawnHasPromoted = false;
				blackPieces.add(newSquare);
				whitePieces.remove(newSquare);
			}
			else {
				previousTakenPiece = newSquare.releasePiece();
				newSquare.setPiece(movedPiece); 
				if (ChessBoard.playerTurn) {
					blackPieces.remove(newSquare);
					if(this.castled) {
						if(prevWhiteMoveList == MoveList.KING_CASTLE_KINGSIDE) {
							threadSwitch(spaces[5][0].getPiece(), spaces[5][0], true, false);
						}
						else if(prevWhiteMoveList == MoveList.KING_CASTLE_QUEENSIDE) {
							threadSwitch(spaces[3][0].getPiece(), spaces[3][0], true, false);
						}
					}
					whitePieces.remove(oldSquare);
					whitePieces.add(newSquare);
				}
				else {
					whitePieces.remove(newSquare);
					if(this.castled) {
						if(prevBlackMoveList == MoveList.KING_CASTLE_KINGSIDE) {
							threadSwitch(spaces[5][7].getPiece(), spaces[5][7], true, false);
						}
						else if(prevBlackMoveList == MoveList.KING_CASTLE_QUEENSIDE) {
							threadSwitch(spaces[3][7].getPiece(), spaces[3][7], true, false);
						}
					}
					blackPieces.remove(oldSquare);
					blackPieces.add(newSquare);
				}
			}
			threadHandler(oldSquare, newSquare, movedPiece, previousTakenPiece);
			this.castled = false;
			ChessBoard.playerTurn = !ChessBoard.playerTurn;
			changePlayerStr();
			if(ChessBoard.kingInCheck && !ChessBoard.playerTurn) {
				this.checkmate = checkMateChecker(blackPieces);
			}
			else if(ChessBoard.kingInCheck && ChessBoard.playerTurn){
				this.checkmate = checkMateChecker(whitePieces);
			}
			if(this.checkmate) System.out.println("Checkmated");
			soundEffectHandler();
			return true;
		} else { //Si no cumple nada mas, significa que no fue jugada valida
			return false;
		}
	}

	private boolean horizontalVerticalDiscoveredCheck(Space tmp, Space oldSquare, Space newSquare, boolean pieceColor){
		boolean check = false, pieceInTheMiddle = false;

		if (oldSquare.getY() == tmp.getY()) {
			if (oldSquare.getX() > tmp.getX()) {
				for (int i = tmp.getX() + 1; i < oldSquare.getX(); i++) {
					if (spaces[i][tmp.getY()].isOccupied()) {
						pieceInTheMiddle = true;
						break;
					}
				}
			} else {
				for (int i = tmp.getX() - 1; i > oldSquare.getX(); i--) {
					if (spaces[i][tmp.getY()].isOccupied()) {
						pieceInTheMiddle = true;
						break;
					}
				}
			}
			if (!pieceInTheMiddle) {
				
				if (newSquare.getY() != oldSquare.getY()) {
					if (oldSquare.getX() > tmp.getX()) {
						if (oldSquare.getX() < 7) {
							for (int i = oldSquare.getX() + 1; i <= 7; i++) {
								check = check || spaces[i][tmp.getY()].threadHandlerAndChecker(true, pieceColor);
								if (spaces[i][tmp.getY()].isOccupied())
									break;
							}
						}
					} else {
						if (oldSquare.getX() > 0) {
							for (int i = oldSquare.getX() - 1; i >= 0; i--) {
								check = check || spaces[i][tmp.getY()].threadHandlerAndChecker(true, pieceColor);
								if (spaces[i][tmp.getY()].isOccupied())
									break;
							}
						}
					}

				} else {
					if(pieceColor){
						if(tmp.getPiece().getName().equals("rook")) {
							if(prevWhiteMoveList == MoveList.KING_CASTLE_KINGSIDE  && tmp.getX() == 5) {
								return false;
							}
							if(prevWhiteMoveList == MoveList.KING_CASTLE_QUEENSIDE && tmp.getX() == 3) {
								return false;
							}
						}
					}
					else {
						if(tmp.getPiece().getName().equals("rook")) {
							if(prevBlackMoveList == MoveList.KING_CASTLE_KINGSIDE  && tmp.getX() == 5) {
								return false;
							}
							if(prevBlackMoveList == MoveList.KING_CASTLE_QUEENSIDE && tmp.getX() == 3) {
								return false;
							}
						}
					}
					if (oldSquare.getX() > tmp.getX()) {
						if (newSquare.getX() < oldSquare.getX()) {
							for (int i = oldSquare.getX(); i > newSquare.getX(); i--) {
								spaces[i][tmp.getY()].threadHandlerAndChecker(false, pieceColor);
							}
						} else {
							for (int i = oldSquare.getX() + 1; i <= newSquare.getX(); i++) {
								if(this.castled && spaces[i][tmp.getY()].getPiece().getName().equals("king")) break;
								spaces[i][tmp.getY()].threadHandlerAndChecker(true, pieceColor);
							}
						}
					} else {
						if (newSquare.getX() > oldSquare.getX()) {
							for (int i = oldSquare.getX(); i < newSquare.getX(); i++) {
								spaces[i][tmp.getY()].threadHandlerAndChecker(false, pieceColor);
							}
						} else {
							for (int i = oldSquare.getX() - 1; i >= newSquare.getX(); i--) {
								if(this.castled && spaces[i][tmp.getY()].getPiece().getName().equals("king")) break;
								spaces[i][tmp.getY()].threadHandlerAndChecker(true, pieceColor);
							}
						}
					}
				}
			}
			// Si estan en la misma fila, checar si no hay
			// piezas entre ellos

		} else if (oldSquare.getX() == tmp.getX()) {
			if (oldSquare.getY() > tmp.getY()) {
				for (int i = tmp.getY() + 1; i < oldSquare.getY(); i++) {
					if (spaces[tmp.getX()][i].isOccupied()) {
						pieceInTheMiddle = true;
						break;
					}
				}
			} else {
				for (int i = tmp.getY() - 1; i > oldSquare.getY(); i--) {
					if (spaces[tmp.getX()][i].isOccupied()) {
						pieceInTheMiddle = true;
						break;
					}
				}
			}

			if (!pieceInTheMiddle) {
				if (newSquare.getX() != oldSquare.getX()) {
					if (oldSquare.getY() > tmp.getY()) {
						if (oldSquare.getY() < 7) {
							for (int i = oldSquare.getY() + 1; i <= 7; i++) {
								check = check || spaces[tmp.getX()][i].threadHandlerAndChecker(true, pieceColor);
								if (spaces[tmp.getX()][i].isOccupied())
									break;
							}
						}
					} else {
						if (oldSquare.getY() > 0) {
							for (int i = oldSquare.getY() - 1; i >= 0; i--) {
								check = check || spaces[tmp.getX()][i].threadHandlerAndChecker(true, pieceColor);
								if (spaces[tmp.getX()][i].isOccupied())
									break;
							}
						}
					}
				} else {
					if (oldSquare.getY() > tmp.getY()) {
						if (newSquare.getY() < oldSquare.getY()) {
							for (int i = oldSquare.getY(); i > newSquare.getY(); i--) {
								spaces[tmp.getX()][i].threadHandlerAndChecker(false, pieceColor);
							}
						} else {
							for (int i = oldSquare.getY() + 1; i <= newSquare.getY(); i++) {
								spaces[tmp.getX()][i].threadHandlerAndChecker(true, pieceColor);
							}
						}
					} else {
						if (newSquare.getY() > oldSquare.getY()) {
							for (int i = oldSquare.getY(); i < newSquare.getY(); i++) {
								spaces[tmp.getX()][i].threadHandlerAndChecker(false, pieceColor);
							}
						} else {
							for (int i = oldSquare.getY() - 1; i >= newSquare.getY(); i--) {
								spaces[tmp.getX()][i].threadHandlerAndChecker(true, pieceColor);
							}
						}
					}
				}
			}
		}

		return check;
	}

	private boolean diagonalDiscoveredCheck(Space tmp, Space oldSquare, Space newSquare, boolean pieceColor) {
		boolean check = false, pieceInTheMiddle = false;
		int tempx, tempy;
		/*    /
		*    /
		    /   */ 
		if((oldSquare.getX() - tmp.getX()) == (oldSquare.getY() - tmp.getY())){
			if(tmp.getX() < oldSquare.getX()) {
				tempx = tmp.getX() + 1;
				tempy = tmp.getY() + 1;
				for(; tempx < oldSquare.getX() && tempy < oldSquare.getY(); tempx++, tempy++) {
					if(spaces[tempx][tempy].isOccupied()) pieceInTheMiddle = true;
				}
				if(pieceInTheMiddle) return false;
				
				if(((newSquare.getX() - tmp.getX()) != (newSquare.getY() - tmp.getY())) /*&&
						(oldSquare.getX() < 7 || oldSquare.getY() < 7)*/) {
					tempx = oldSquare.getX() + 1;
					tempy = oldSquare.getY() + 1;
					for(; tempx <= 7 && tempy <= 7; tempx++, tempy++) {
						check = check || spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						if(spaces[tempx][tempy].isOccupied()) break;
					}
				}
				else{
					if(newSquare.getX() > oldSquare.getX() && newSquare.getY() > oldSquare.getY()) {
						tempx = oldSquare.getX() + 1;
						tempy = oldSquare.getY() + 1;
						for(; tempx <= newSquare.getX() && tempy <= newSquare.getY(); tempx++, tempy++) {
							spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						}
					}
					else { //Se supone que aqui no ocupa un else if
						tempx = oldSquare.getX();
						tempy = oldSquare.getY();
						for(; tempx > newSquare.getX() && tempy > newSquare.getY(); tempx--, tempy--) {
							spaces[tempx][tempy].threadHandlerAndChecker(false, pieceColor);
						}
					}
				}
			}
			else {
				tempx = tmp.getX() - 1;
				tempy = tmp.getY() - 1;
				for(; tempx > oldSquare.getX() && tempy > oldSquare.getY(); tempx--, tempy--) {
					if(spaces[tempx][tempy].isOccupied()) pieceInTheMiddle = true;
				}
				if(pieceInTheMiddle) return false;
				if(((newSquare.getX() - tmp.getX()) != (newSquare.getY() - tmp.getY()))) {
					tempx = oldSquare.getX() - 1;
					tempy = oldSquare.getY() - 1;
					for(; tempx >= 0 && tempy >= 0; tempx--, tempy--) {
						check = check || spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						if(spaces[tempx][tempy].isOccupied()) break;
					}
				}
				else /*if(((newSquare.getX() - tmp.getX()) == (newSquare.getY() - tmp.getY())))*/{
					if(newSquare.getX() > oldSquare.getX() && newSquare.getY() > oldSquare.getY()) {
						tempx = oldSquare.getX();
						tempy = oldSquare.getY();
						for(; tempx < newSquare.getX() && tempy < newSquare.getY(); tempx++, tempy++) {
							spaces[tempx][tempy].threadHandlerAndChecker(false, pieceColor);
						}
					}
					else { //Se supone que aqui no ocupa un else if
						tempx = oldSquare.getX() - 1;
						tempy = oldSquare.getY() - 1;
						for(; tempx >= newSquare.getX() && tempy >= newSquare.getY(); tempx--, tempy--) {
							spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						}
					}
				}
			}
			
		}
		// \
		//  \
		//   \
		else if(Math.abs(oldSquare.getX() - tmp.getX()) == Math.abs(oldSquare.getY() - tmp.getY())) { 
			if(tmp.getX() > oldSquare.getX()) {
				tempx = tmp.getX() - 1;
				tempy = tmp.getY() + 1;
				for(; tempx > oldSquare.getX() && tempy < oldSquare.getY(); tempx--, tempy++) {
					if(spaces[tempx][tempy].isOccupied()) pieceInTheMiddle = true;
				}
				if(pieceInTheMiddle) return false;
				
				if((Math.abs(newSquare.getX() - tmp.getX()) != Math.abs(newSquare.getY() - tmp.getY())) /*&&
						(oldSquare.getX() < 7 || oldSquare.getY() < 7)*/) {
					tempx = oldSquare.getX() - 1;
					tempy = oldSquare.getY() + 1;
					for(; tempx >= 0 && tempy <= 7; tempx--, tempy++) {
						check = check || spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						if(spaces[tempx][tempy].isOccupied()) break;
					}
				}
				else /*if(((newSquare.getX() - tmp.getX()) == (newSquare.getY() - tmp.getY())))*/{
					if(newSquare.getX() < oldSquare.getX() && newSquare.getY() > oldSquare.getY()) {
						tempx = oldSquare.getX() - 1;
						tempy = oldSquare.getY() + 1;
						for(; tempx >= newSquare.getX() && tempy <= newSquare.getY(); tempx--, tempy++) {
							spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						}
					}
					else { //Se supone que aqui no ocupa un else if
						tempx = oldSquare.getX();
						tempy = oldSquare.getY();
						for(; tempx < newSquare.getX() && tempy > newSquare.getY(); tempx++, tempy--) {
							spaces[tempx][tempy].threadHandlerAndChecker(false, pieceColor);
						}
					}
				}
			}
			else {
				tempx = tmp.getX() + 1;
				tempy = tmp.getY() - 1;
				for(; tempx < oldSquare.getX() && tempy > oldSquare.getY(); tempx++, tempy--) {
					if(spaces[tempx][tempy].isOccupied()) pieceInTheMiddle = true;
				}
				if(pieceInTheMiddle) return false;
				if((Math.abs(newSquare.getX() - tmp.getX()) != Math.abs(newSquare.getY() - tmp.getY())) /*&&
						(oldSquare.getX() > 0 || oldSquare.getY() > 0)*/) {
					tempx = oldSquare.getX() + 1;
					tempy = oldSquare.getY() - 1;
					for(; tempx <= 7 && tempy >= 0; tempx++, tempy--) {
						check = check || spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						if(spaces[tempx][tempy].isOccupied()) break;
					}
				}
				else /*if(((newSquare.getX() - tmp.getX()) == (newSquare.getY() - tmp.getY())))*/{
					if(newSquare.getX() < oldSquare.getX() && newSquare.getY() > oldSquare.getY()) {
						tempx = oldSquare.getX();
						tempy = oldSquare.getY();
						for(; tempx > newSquare.getX() && tempy < newSquare.getY(); tempx--, tempy++) {
							spaces[tempx][tempy].threadHandlerAndChecker(false, pieceColor);
						}
					}
					else { //Se supone que aqui no ocupa un else if
						tempx = oldSquare.getX() + 1;
						tempy = oldSquare.getY() - 1;
						for(; tempx <= newSquare.getX() && tempy >= newSquare.getY(); tempx++, tempy--) {
							spaces[tempx][tempy].threadHandlerAndChecker(true, pieceColor);
						}
					}
				}
			}
		}
		return check;
	}

	private boolean discoveredCheckChecker(Space oldSquare, Space newSquare) {
		boolean check = false;
		for (Space tmp : whitePieces) {
			if (!newSquare.equals(tmp)) {
				if (tmp.getPiece().getName().equals("rook")) {
					check = check || horizontalVerticalDiscoveredCheck(tmp, oldSquare, newSquare, true);
				}
				if (tmp.getPiece().getName().equals("bishop")) {
					check = check || diagonalDiscoveredCheck(tmp, oldSquare, newSquare, true);
				}
				if (tmp.getPiece().getName().equals("queen")) {
					check = check || horizontalVerticalDiscoveredCheck(tmp, oldSquare, newSquare, true)
							|| diagonalDiscoveredCheck(tmp, oldSquare, newSquare, true);
				}
				if (check) {
					this.piecesChecking.add(tmp);
					this.discoveredCheck = true;
				}

				this.piecesGivingCheck = (check) ? ++this.piecesGivingCheck : this.piecesGivingCheck;
			}
		}

		for (Space tmp : blackPieces) {
			if (!newSquare.equals(tmp)) {
				if (tmp.getPiece().getName().equals("rook")) {
					check = check || horizontalVerticalDiscoveredCheck(tmp, oldSquare, newSquare, false);
				}
				if (tmp.getPiece().getName().equals("bishop")) {
					check = check || diagonalDiscoveredCheck(tmp, oldSquare, newSquare, false);
				}
				if (tmp.getPiece().getName().equals("queen")) {
					check = check || horizontalVerticalDiscoveredCheck(tmp, oldSquare, newSquare, false)
							|| diagonalDiscoveredCheck(tmp, oldSquare, newSquare, false);
				}
				if (check)
					this.piecesChecking.add(tmp);
				this.piecesGivingCheck = (check) ? ++this.piecesGivingCheck : this.piecesGivingCheck;
			}
		}
		return check;
	}

	private void threadHandler(Space oldSquare, Space newSquare, Piece movedPiece, Piece prevTakenPiece) {
		boolean check = false;
		if (prevTakenPiece != null)
			threadSwitch(prevTakenPiece, newSquare, false, true);
		/*
		 * Pone las nuevas casillas amenazadas, adem�s de que checa si hay un jaque
		 * directo
		 */
		/*
		 * Pone las nuevas casillas amenazadas, adem�s de que checa si hay un jaque
		 * directo
		 */
		boolean directCheck = threadSwitch(movedPiece, newSquare, true, false);
		if (directCheck)
			this.piecesChecking.add(newSquare);
		blockingPathChecker(oldSquare, newSquare);
		discoveredCheckChecker(oldSquare, newSquare);

		this.piecesGivingCheck = (check) ? ++this.piecesGivingCheck: this.piecesGivingCheck;
		ChessBoard.kingInCheck =  directCheck || this.discoveredCheck;

	}

	private boolean checkMateChecker(ArrayList<Space> pieces) {
		Space king = null;
		boolean findValidMove = true;
		for (Space piece : pieces) {
			if (piece.getPiece().getName().equals("king")) {
				king = piece;
				break;
			}
		}
		MoveList[] kingMoves = king.getPiece().getPieceMoves();

		// See if King can move to any square
		for (MoveList m : kingMoves) {
			findValidMove = moveIsValid(
					new MoveInfo(king.getX(), king.getY(), king.getX() + m.getX(), king.getY() + m.getY()), false);
			if (findValidMove)
				return false;
		}

		// If more than one piece is giving check and no possible squares for the king,
		// then its checkmate
		if (this.piecesGivingCheck > 1)
			return false;
		// See if you can eat the checkingPiece Or block its check
		int xNewPos, yNewPos;
		for (Space piece : pieces) {
			// System.out.println(piece.getPiece() + " - " + piece.toString());
			MoveList[] moves;
			if (!piece.getPiece().getName().equals("king")) {
				moves = piece.getPiece().getPieceMoves();
				if (piece.getPiece().usesSingleMove()) {
					for (MoveList m : moves) {
						xNewPos = piece.getX() + m.getX();
						yNewPos = piece.getY() + m.getY();
						if ((xNewPos >= 0 && xNewPos <= 7) && (yNewPos >= 0 && yNewPos <= 7)) {
							// System.out.println("Verifying pos: [" + xNewPos + "," + yNewPos + "]");;
							findValidMove = moveIsValid(new MoveInfo(piece.getX(), piece.getY(), xNewPos, yNewPos),
									false);
							if (findValidMove)
								return false;
						}
					}
				} else {
					for (MoveList m : moves) {
						for (int c = 1; c <= 7; c++) {
							xNewPos = piece.getX() + (m.getX() * c);
							yNewPos = piece.getY() + (m.getY() * c);
							if ((xNewPos >= 0 && xNewPos <= 7) && (yNewPos >= 0 && yNewPos <= 7)) {
								// System.out.println("Verifying pos: [" + xNewPos + "," + yNewPos + "]");
								findValidMove = moveIsValid(new MoveInfo(piece.getX(), piece.getY(), xNewPos, yNewPos),
										false);
								if (findValidMove)
									return false;
								if (spaces[xNewPos][yNewPos].isOccupied()) {
									break;
								}
							}
						}
					}
				}
			}
		}

		return true;
	}

	private void blockingPathChecker(Space oldSquare, Space newSquare) {
		if (this.castled)
			return;
		for (Space tmp : whitePieces) {

			if (!newSquare.equals(tmp)) {
				if (tmp.getPiece().getName().equals("rook")) {
					blockRookSquares(tmp, oldSquare, newSquare, true);
				}
				if (tmp.getPiece().getName().equals("bishop")) {
					blockBishopSquares(tmp, oldSquare, newSquare, true);
				}
				if (tmp.getPiece().getName().equals("queen")) {
					blockRookSquares(tmp, oldSquare, newSquare, true);
					blockBishopSquares(tmp, oldSquare, newSquare, true);
				}
			}
		}

		for (Space tmp : blackPieces) {
			if (!newSquare.equals(tmp)) {
				if (tmp.getPiece().getName().equals("rook")) {
					blockRookSquares(tmp, oldSquare, newSquare, false);
				}
				if (tmp.getPiece().getName().equals("bishop")) {
					blockBishopSquares(tmp, oldSquare, newSquare, false);
				}
				if (tmp.getPiece().getName().equals("queen")) {
					blockRookSquares(tmp, oldSquare, newSquare, false);
					blockBishopSquares(tmp, oldSquare, newSquare, false);
				}
			}
		}
	}

	private void blockBishopSquares(Space tmp, Space oldSquare, Space newSquare, boolean blackOrWhite) {
		int minX, maxX, minY, maxY;
		boolean pieceInTheMiddle = false;
		if ((newSquare.getY() - tmp.getY()) == (newSquare.getX() - tmp.getX())) {
			minX = (tmp.getX() > newSquare.getX()) ? newSquare.getX() : tmp.getX();
			maxX = (tmp.getX() > newSquare.getX()) ? tmp.getX() : newSquare.getX();
			minY = (tmp.getY() > newSquare.getY()) ? newSquare.getY() : tmp.getY();
			maxY = (tmp.getY() > newSquare.getY()) ? tmp.getY() : newSquare.getY();
			for (int i = minX + 1, j = minY + 1; i < maxX && j < maxY; i++, j++) {
				if (spaces[i][j].isOccupied()) {
					pieceInTheMiddle = true;
					break;
				}
			}
			if (pieceInTheMiddle)
				return;
			if (minX == newSquare.getX()) {
				for (int i = minX - 1, j = minY - 1; i >= 0 && j >= 0; i--, j--) {
					spaces[i][j].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[i][j].isOccupied())
						break;
				}
			} else {
				for (int i = maxX + 1, j = maxY + 1; i <= 7 && j <= 7; i++, j++) {
					spaces[i][j].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[i][j].isOccupied())
						break;
				}
			}
		} else if (Math.abs(newSquare.getY() - tmp.getY()) == Math.abs(newSquare.getX() - tmp.getX())) {
			minX = (tmp.getX() > newSquare.getX()) ? tmp.getX() : newSquare.getX();
			maxX = (tmp.getX() > newSquare.getX()) ? newSquare.getX() : tmp.getX();

			minY = (tmp.getY() > newSquare.getY()) ? newSquare.getY() : tmp.getY();
			maxY = (tmp.getY() > newSquare.getY()) ? tmp.getY() : newSquare.getY();
			for (int i = minX - 1, j = minY + 1; i > maxX && j < maxY; i--, j++) {
				if (spaces[i][j].isOccupied()) {
					pieceInTheMiddle = true;
					break;
				}
			}
			if (pieceInTheMiddle)
				return;
			if (minX == newSquare.getX()) {
				for (int i = minX + 1, j = minY - 1; i <= 7 && j >= 0; i++, j--) {
					spaces[i][j].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[i][j].isOccupied())
						break;
				}
			} else {
				for (int i = minX - 1, j = minY + 1; i >= 0 && j <= 7; i--, j++) {
					spaces[i][j].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[i][j].isOccupied())
						break;
				}
			}
		}
	}

	private void blockRookSquares(Space tmp, Space oldSquare, Space newSquare, boolean blackOrWhite) {
		int min, max;
		boolean pieceInTheMiddle = false;
		if (tmp.getX() == newSquare.getX() && oldSquare.getX() != newSquare.getX()) {
			min = (tmp.getY() > newSquare.getY()) ? newSquare.getY() : tmp.getY();
			max = (tmp.getY() > newSquare.getY()) ? tmp.getY() : newSquare.getY();
			for (int i = min + 1; i < max; i++) {
				if (spaces[tmp.getX()][i].isOccupied()) {
					pieceInTheMiddle = true;
					break;
				}
			}
			if (pieceInTheMiddle)
				return;

			if (min == newSquare.getY()) {
				for (int i = min - 1; i >= 0; i--) {
					spaces[tmp.getX()][i].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[tmp.getX()][i].isOccupied())
						break;
				}
			} else {
				for (int i = max + 1; i <= 7; i++) {
					spaces[tmp.getX()][i].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[tmp.getX()][i].isOccupied())
						break;
				}
			}
		} else if (tmp.getY() == newSquare.getY() && oldSquare.getY() != newSquare.getY()) {
			min = (tmp.getX() > newSquare.getX()) ? newSquare.getX() : tmp.getX();
			max = (tmp.getX() > newSquare.getX()) ? tmp.getX() : newSquare.getX();

			for (int i = min + 1; i < max; i++) {
				if (spaces[i][tmp.getY()].isOccupied()) {
					pieceInTheMiddle = true;
					break;
				}
			}
			if (pieceInTheMiddle)
				return;

			if (min == newSquare.getX()) {
				for (int i = min - 1; i >= 0; i--) {
					spaces[i][tmp.getY()].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[i][tmp.getY()].isOccupied())
						break;
				}
			} else {
				for (int i = max + 1; i <= 7; i++) {
					spaces[i][tmp.getY()].threadHandlerAndChecker(false, blackOrWhite);
					if (spaces[i][tmp.getY()].isOccupied())
						break;
				}
			}
		}
	}

	private boolean threadSwitch(Piece piece, Space square, boolean giveOrTake, boolean capturedPiece) {
		boolean check = false;
		switch (piece.getName()) {
		case "pawn":
			check = pawnChecker(square.getX(), square.getY(), giveOrTake, capturedPiece);
			break;
		case "rook":
			check = horizontalVerticalChecker(square.getX(), square.getY(), giveOrTake, capturedPiece);
			break;
		case "bishop":
			check = diagonalChecker(square.getX(), square.getY(), giveOrTake, capturedPiece);
			break;
		case "queen":
			check = horizontalVerticalChecker(square.getX(), square.getY(), giveOrTake, capturedPiece)
					|| diagonalChecker(square.getX(), square.getY(), giveOrTake, capturedPiece);
			break;
		case "king":
			kingThreadChecker(square.getX(), square.getY(), giveOrTake, capturedPiece);
			break;

		case "knight":
			check = knightChecker(square.getX(), square.getY(), giveOrTake, capturedPiece);
			break;
		default:
			break;
		}
		return check;
	}

	private boolean pawnChecker(int x, int y, boolean giveOrTake, boolean capturedPiece) {
		Space temp;
		boolean check = false, tempPlayerTurn = (capturedPiece) ? !ChessBoard.playerTurn : ChessBoard.playerTurn;
		if (tempPlayerTurn) {
			if (x > 0) {
				temp = spaces[x - 1][y + 1];
				if (giveOrTake)
					temp.incWhiteThreads();
				else
					temp.decWhiteThreads();
				if (temp.isOccupied() && temp.getPiece().getName() == "king" && temp.getPieceColor() == "black") {
					check = true;
				}
			}
			if (x < 7) {
				temp = spaces[x + 1][y + 1];
				if (giveOrTake)
					temp.incWhiteThreads();
				else
					temp.decWhiteThreads();
				if (temp.isOccupied() && temp.getPiece().getName() == "king" && temp.getPieceColor() == "black") {
					check = true;
				}
			}
		} else {
			if (x > 0) {
				temp = spaces[x - 1][y - 1];
				if (giveOrTake)
					temp.incBlackThreads();
				else
					temp.decBlackThreads();
				if (temp.isOccupied() && temp.getPiece().getName() == "king" && temp.getPieceColor() == "white") {
					check = true;
				}
			}
			if (x < 7) {
				temp = spaces[x + 1][y - 1];
				if (giveOrTake)
					temp.incBlackThreads();
				else
					temp.decBlackThreads();
				if (temp.isOccupied() && temp.getPiece().getName() == "king" && temp.getPieceColor() == "white") {
					check = true;
				}
			}
		}
		return check;
	}

	private boolean horizontalVerticalChecker(int x, int y, boolean giveOrTake, boolean capturedPiece) {
		boolean check = false, tempPlayerTurn = (capturedPiece) ? !ChessBoard.playerTurn : ChessBoard.playerTurn;
		Space spaceTemp;
		Space spaceTemp2;
		int temp1 = x, temp2 = x;
		for (int i = 0; i < 2; i++) {
			if (i == 0)
				temp1 = temp2 = x;
			else
				temp1 = temp2 = y;
			while (temp1 > 0 || temp2 < 7) {
				if (temp1 > 0) {
					if (i == 0)
						spaceTemp = spaces[--temp1][y];
					else
						spaceTemp = spaces[x][--temp1];
					check = check || spaceTemp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
					if (spaceTemp.isOccupied())
						temp1 = 0;
				}
				if (temp2 < 7) {
					if (i == 0)
						spaceTemp2 = spaces[++temp2][y];
					else
						spaceTemp2 = spaces[x][++temp2];
					check = check || spaceTemp2.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
					if (spaceTemp2.isOccupied()) {
						temp2 = 7;
					}
				}
			}
		}
		return check;
	}

	private boolean diagonalChecker(int x, int y, boolean giveOrTake, boolean capturedPiece) {
		boolean check = false, tempPlayerTurn = (capturedPiece) ? !ChessBoard.playerTurn : ChessBoard.playerTurn;
		Space spaceTemp;
		Space spaceTemp2;
		int x1 = x, x2 = x;
		int y1 = y, y2 = y;
		while ((x1 > 0 && y1 > 0) || (x2 < 7 && y2 < 7)) {
			if (x1 > 0 && y1 > 0) {
				spaceTemp = spaces[--x1][--y1];
				check = check || spaceTemp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				if (spaceTemp.isOccupied())
					x1 = y1 = 0;
			}
			if (x2 < 7 && y2 < 7) {
				spaceTemp2 = spaces[++x2][++y2];
				check = check || spaceTemp2.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				if (spaceTemp2.isOccupied())
					x2 = y2 = 7;
			}
		}
		x1 = x2 = x;
		y1 = y2 = y;
		while ((x1 < 7 && y1 > 0) || (x2 > 0 && y2 < 7)) {
			if (x1 < 7 && y1 > 0) {
				spaceTemp = spaces[++x1][--y1];
				check = check || spaceTemp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				if (spaceTemp.isOccupied())
					y1 = 0;
			}
			if (x2 > 0 && y2 < 7) {
				spaceTemp2 = spaces[--x2][++y2];
				check = check || spaceTemp2.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				if (spaceTemp2.isOccupied())
					y2 = 7;
			}
		}
		return check;
	}

	private void kingThreadChecker(int x, int y, boolean giveOrTake, boolean capturedPiece) {
		boolean playerTurn = (capturedPiece) ? !ChessBoard.playerTurn : ChessBoard.playerTurn;
		if (x < 7)
			spaces[x + 1][y].threadHandlerAndChecker(giveOrTake, playerTurn);
		if (x > 0)
			spaces[x - 1][y].threadHandlerAndChecker(giveOrTake, playerTurn);

		if (y < 7) {
			if (x < 7)
				spaces[x + 1][y + 1].threadHandlerAndChecker(giveOrTake, playerTurn);
			if (x > 0)
				spaces[x - 1][y + 1].threadHandlerAndChecker(giveOrTake, playerTurn);
			spaces[x][y + 1].threadHandlerAndChecker(giveOrTake, playerTurn);
		}

		if (y > 0) {
			if (x < 7)
				spaces[x + 1][y - 1].threadHandlerAndChecker(giveOrTake, playerTurn);
			if (x > 0)
				spaces[x - 1][y - 1].threadHandlerAndChecker(giveOrTake, playerTurn);
			spaces[x][y - 1].threadHandlerAndChecker(giveOrTake, playerTurn);
		}
	}

	private boolean knightChecker(int x, int y, boolean giveOrTake, boolean capturedPiece) {
		boolean check = false, tempPlayerTurn = (capturedPiece) ? !ChessBoard.playerTurn : ChessBoard.playerTurn;
		Space temp;
		if (x < 7) {
			if (x < 6) {
				if (y < 7) {
					temp = spaces[x + 2][y + 1];
					check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				}
				if (y > 0) {
					temp = spaces[x + 2][y - 1];
					check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				}
			}
			if (y < 6) {
				temp = spaces[x + 1][y + 2];
				check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
			}
			if (y > 1) {
				temp = spaces[x + 1][y - 2];
				check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
			}
		}
		// ************************** ***************************//
		if (x > 0) {
			if (x > 1) {
				if (y < 7) {
					temp = spaces[x - 2][y + 1];
					check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				}
				if (y > 0) {
					temp = spaces[x - 2][y - 1];
					check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
				}
			}
			if (y < 6) {
				temp = spaces[x - 1][y + 2];
				check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
			}
			if (y > 1) {
				temp = spaces[x - 1][y - 2];
				check = check || temp.threadHandlerAndChecker(giveOrTake, tempPlayerTurn);
			}
		}
		return check;
	}

	private boolean moveIsValid(MoveInfo p, boolean changePrev) {
		Space oldSpace;
		Space newSpace;
		Piece piece;
		MoveList[] moves;
		if (p == null) {
			return false;
		}

		// Checa que el OldSpace no se salga del rango
		try {
			oldSpace = spaces[p.getOldX()][p.getOldY()];
		} catch (NullPointerException e) {
			return false;
		}

		// Checa que el newSpace no se salga del rango
		try {
			newSpace = spaces[p.getNewX()][p.getNewY()];
		} catch (NullPointerException e) {
			return false;
		}
		catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
		//Checa que el active space tenga una pieza
		if (!oldSpace.isOccupied()) {
			return false;
		}

		piece = oldSpace.getPiece();
		moves = piece.getPieceMoves();
		boolean matchesPieceMoves = false;

		//Para las piezas que se muevan mas de una sola casilla, como alfil, torre y dama
		int multiMoveCount;
		int stretchedMoveX;
		int stretchedMoveY;
		
		MoveLoop: for (MoveList m : moves) {
			multiMoveCount = 1;
			if (piece.usesSingleMove() == false) {
				multiMoveCount = 8;
			}

			boolean hasCollided = false;

			for (int c = 1; c <= multiMoveCount; c++) {
				if (hasCollided) {
					break;
				}

				// Esto checa movimiento de alfiles, torres y damas
				stretchedMoveX = m.getX() * c;
				stretchedMoveY = m.getY() * c;
				Space tempSpace;
				try {
					tempSpace = spaces[p.getOldX() + stretchedMoveX][p.getOldY() + stretchedMoveY];
				} catch (Exception e) {
					break;
				}
				// Si la casilla destino esta ocupada...
				if (tempSpace.isOccupied()) {
					hasCollided = true;
					boolean piecesSameColor = tempSpace.getPiece().getColor() == oldSpace.getPiece().getColor();
					// stops checking this move if pieces are the same color
					if (piecesSameColor) {
						break;
					}
				}
				
				// si la casilla es la misma que la que se esta checando
				if (p.getGapX() == stretchedMoveX && p.getGapY() == stretchedMoveY) {
					matchesPieceMoves = true;
					if(doesntLeaveKingAttacked(p) == false) {
						System.out.println("Leaving king attacked");
						return false;
					}
					
					if(kingWrongMove(p) == false) {
						System.out.println("Cant move to a threaten square");
						return false;
					}
					if(kingInCheckValidator(p) == false) {
						return false;
					}
					if(castleCheck(p, m) == false) {
						System.out.println("Invalid castle");
						return false;
					}
					if (pawnValidityCheck(p, m) == false) {
						System.out.println("Pawn validity");
						return false;
					}
					prevWhiteMoveList = (ChessBoard.playerTurn  && changePrev)? m: prevWhiteMoveList;
					prevBlackMoveList = (!ChessBoard.playerTurn && changePrev)? m: prevBlackMoveList;
					piece.setHasMoved(true);
					break MoveLoop;
				}
			}
			
		}
		if (!matchesPieceMoves) {
			return false;
		}
		return true;
	}

	private boolean doesntLeaveKingAttacked(MoveInfo p) {
		if (ChessBoard.playerTurn)
			return doesntLeaveKingAttackedAux(p, whitePieces, blackPieces);
		else
			return doesntLeaveKingAttackedAux(p, blackPieces, whitePieces);
	}

	private boolean doesntLeaveKingAttackedAux(MoveInfo p, ArrayList<Space> kingGetter, ArrayList<Space> enemyPieces) {
		Space king = null;
		boolean kingSafe = true;
		for (Space tmp : kingGetter) {
			if (tmp.getPiece().getName().equals("king")) {
				king = tmp;
				break;
			}
		}

		if(king.getX() == p.getOldX() && king.getY() == p.getOldY() && !ChessBoard.kingInCheck) return true;
		
		for(Space tmp : enemyPieces) {
			if(tmp.getPiece().getName().equals("rook")) {
				kingSafe = rookAttacksKing(king, tmp, p);
				if (!kingSafe) {
					return false;
				}
			} else if (tmp.getPiece().getName().equals("bishop")) {
				kingSafe = bishopAttacksKing(king, tmp, p);
				if (!kingSafe) {
					return false;
				}
			} else if (tmp.getPiece().getName().equals("queen")) {
				kingSafe = rookAttacksKing(king, tmp, p) && bishopAttacksKing(king, tmp, p);
				if (!kingSafe) {
					return false;
				}
			}
		}

		return kingSafe;
	}

	private boolean rookAttacksKing(Space king, Space tmp, MoveInfo p) {
		int piecesBetweenKing = 0;
		if(king.equals(spaces[p.getOldX()][p.getOldY()]) && ChessBoard.kingInCheck) {
			if(king.getX() == tmp.getX()) {
				if(king.getY() > tmp.getY()) {
					if(p.getNewX() == p.getOldX() && p.getNewY() > p.getOldY()) return false;
				}
				if (king.getY() < tmp.getY()) {
					if (p.getNewX() == p.getOldX() && p.getNewY() < p.getOldY())
						return false;
				}
			} else if (king.getY() == tmp.getY()) {
				if (king.getX() > tmp.getX()) {
					if (p.getNewY() == p.getOldY() && p.getNewX() > p.getOldX())
						return false;
				}
				if (king.getX() < tmp.getX()) {
					if (p.getNewY() == p.getOldY() && p.getNewX() < p.getOldX())
						return false;
				}
			}
		}
		if (king.getX() == tmp.getX() && tmp.getX() == p.getOldX() && p.getOldX() != p.getNewX()) {
			int min = (king.getY() > tmp.getY()) ? tmp.getY() + 1 : king.getY() + 1;
			int max = (king.getY() > tmp.getY()) ? king.getY() : tmp.getY();
			for (; min < max; min++) {
				if (spaces[tmp.getX()][min].isOccupied())
					piecesBetweenKing++;
			}
			if (piecesBetweenKing == 1)
				return false;
		} else if (king.getY() == tmp.getY() && tmp.getY() == p.getOldY() && p.getOldY() != p.getNewY()) {
			int min = (king.getX() > tmp.getX()) ? tmp.getX() + 1 : king.getX() + 1;
			int max = (king.getX() > tmp.getX()) ? king.getX() : tmp.getX();
			for (; min < max; min++) {
				if (spaces[min][tmp.getY()].isOccupied())
					piecesBetweenKing++;
			}
			if (piecesBetweenKing == 1)
				return false;
		}
		return true;
	}

	private boolean bishopAttacksKing(Space king, Space tmp, MoveInfo p) {
		int piecesBetweenKing = 0;
		if(king.equals(spaces[p.getOldX()][p.getOldY()]) && ChessBoard.kingInCheck
				&& !tmp.equals(spaces[p.getNewX()][p.getNewY()])){
			if((tmp.getY() - king.getY()) == (tmp.getX() - king.getX())) {
				if((tmp.getY() - p.getNewY()) == (tmp.getX() - p.getNewX())) return false;
			}
			else if(Math.abs(tmp.getY() - king.getY()) == Math.abs(tmp.getX() - king.getX())) {
				if(Math.abs(tmp.getY() - p.getNewY()) == Math.abs(tmp.getX() - p.getNewX())) return false;
			}
		}
		if (tmp.equals(spaces[p.getNewX()][p.getNewY()]))
			return true;

		if ((tmp.getY() - king.getY()) == (tmp.getX() - king.getX())
				&& (king.getY() - p.getOldY()) == (king.getX() - p.getOldX())) {
			int minY = (king.getY() > tmp.getY()) ? tmp.getY() + 1 : king.getY() + 1;
			int maxY = (king.getY() > tmp.getY()) ? king.getY() : tmp.getY();
			int minX = (king.getX() > tmp.getX()) ? tmp.getX() + 1 : king.getX() + 1;
			int maxX = (king.getX() > tmp.getX()) ? king.getX() : tmp.getX();
			for (; minX < maxX && minY < maxY; minX++, minY++) {
				if (spaces[minX][minY].isOccupied()) {
					piecesBetweenKing++;
				}
			}

			if (piecesBetweenKing == 1)
				return false;
		} else if (Math.abs(tmp.getY() - king.getY()) == Math.abs(tmp.getX() - king.getX())
				&& Math.abs(king.getY() - p.getOldY()) == Math.abs(king.getX() - p.getOldX())
				&& Math.abs(tmp.getY() - p.getOldY()) == Math.abs(tmp.getX() - p.getOldX())) {
			int minY = (king.getY() > tmp.getY()) ? tmp.getY() + 1 : king.getY() + 1;
			int maxY = (king.getY() > tmp.getY()) ? king.getY() : tmp.getY();
			int minX = (king.getX() > tmp.getX()) ? king.getX() - 1 : tmp.getX() - 1;
			int maxX = (king.getX() > tmp.getX()) ? tmp.getX() : king.getX();
			for (; minX > maxX && minY < maxY; minX--, minY++) {
				if (spaces[minX][minY].isOccupied()) {
					piecesBetweenKing++;
				}
			}
			if (piecesBetweenKing == 1)
				return false;
		}

		return true;
	}

	private boolean kingWrongMove(MoveInfo p) {
		if(ChessBoard.spaces[p.getOldX()][p.getOldY()].getPiece().getName().equals("king")) {
			if(ChessBoard.playerTurn && spaces[p.newX][p.newY].isThreatenedByBlack()){
				return false;
			} else if (!ChessBoard.playerTurn && spaces[p.newX][p.newY].isThreatenedByWhite()) {
				return false;
			}
		}
		return true;

	}

	
	
	
	private boolean kingInCheckValidator(MoveInfo p) { 
		
		if(ChessBoard.kingInCheck) {

			boolean movePossible = false;
			if (this.piecesChecking.size() > 1) {
				if (spaces[p.getOldX()][p.getOldY()].getPiece().getName().equals("king")) {
					return true;
				}
			} else {
				if (spaces[p.getOldX()][p.getOldY()].getPiece().getName().equals("king")) {
					if (ChessBoard.playerTurn && !spaces[p.newX][p.newY].isThreatenedByBlack()
							&& doesntLeaveKingAttacked(p)) {

						movePossible = true;
					}
					if (!ChessBoard.playerTurn && !spaces[p.newX][p.newY].isThreatenedByWhite()
							&& doesntLeaveKingAttacked(p)) {
						movePossible = true;
					}
				} else {
					Space tmp = this.piecesChecking.get(0);
					if (tmp.getX() == p.newX && tmp.getY() == p.newY) {
						movePossible = true;
					} else {
						Space king = null;
						if (ChessBoard.playerTurn) {
							for (Space kingFinder : whitePieces) {
								if (kingFinder.getPiece().getName().equals("king")) {
									king = kingFinder;
									break;
								}
							}
						} else {
							for (Space kingFinder : blackPieces) {
								if (kingFinder.getPiece().getName().equals("king")) {
									king = kingFinder;
									break;
								}
							}
						}
						switch (tmp.getPiece().getName()) {
						case "bishop":
							movePossible = blockingPathOfBishop(king, tmp, p.getNewX(), p.getNewY());
							break;
						case "rook":
							movePossible = blockingPathOfRook(king, tmp, p.getNewX(), p.getNewY());
							break;
						case "queen":
							movePossible = blockingPathOfRook(king, tmp, p.getNewX(), p.getNewY())
									|| blockingPathOfBishop(king, tmp, p.getNewX(), p.getNewY());
							break;
						default:
							break;
						}
					}

				}
			}

			return movePossible;
		}
		return true;
	}

	private boolean blockingPathOfBishop(Space king, Space pieceGivingCheck, int x, int y) {
		// ActiveSquare = rey
		if ((pieceGivingCheck.getY() - y) == (pieceGivingCheck.getX() - x)
				&& (pieceGivingCheck.getY() - king.getY()) == (pieceGivingCheck.getX() - king.getX())) {
			if (king.getX() < pieceGivingCheck.getX() && king.getX() < x && pieceGivingCheck.getX() > x) {
				return true;
			} else if (king.getX() > pieceGivingCheck.getX() && king.getX() > x && pieceGivingCheck.getX() < x) {
				return true;
			}
		} else if (Math.abs(pieceGivingCheck.getY() - y) == Math.abs(pieceGivingCheck.getX() - x)
				&& Math.abs(pieceGivingCheck.getY() - king.getY()) == Math.abs(pieceGivingCheck.getX() - king.getX())) {
			if (king.getX() > pieceGivingCheck.getX() && king.getX() > x && pieceGivingCheck.getX() < x) {
				return true;
			} else if (king.getX() < pieceGivingCheck.getX() && king.getX() < x && pieceGivingCheck.getX() > x) {
				return true;
			}
		}

		return false;
	}

	private boolean blockingPathOfRook(Space king, Space pieceGivingCheck, int x, int y) {
		if (pieceGivingCheck.getX() == x && x == king.getX()) {
			if (king.getY() > pieceGivingCheck.getY() && king.getY() > y && y > pieceGivingCheck.getY())
				return true;
			if (king.getY() < pieceGivingCheck.getY() && king.getY() < y && y < pieceGivingCheck.getY())
				return true;
		} else if (pieceGivingCheck.getY() == y && y == king.getY()) {
			if (king.getX() > pieceGivingCheck.getX() && king.getX() > x && x > pieceGivingCheck.getX())
				return true;
			if (king.getX() < pieceGivingCheck.getX() && king.getX() < x && x < pieceGivingCheck.getX())
				return true;
		}
		return false;
	}

	/************** L�GICA PARA ENROCARSE **************************/
	/************** aun faltan ciertas validaciones ****************/
	private boolean castleCheck(MoveInfo p, MoveList m) {
		String pieceName = spaces[p.getOldX()][p.getOldY()].getPiece().getName();
		if(pieceName.equals("king")) {
			if(m.isEqual(MoveList.KING_CASTLE_KINGSIDE) && !ChessBoard.kingInCheck) {
				Space KingRookSpace = spaces[7][p.getOldY()];
				if (!KingRookSpace.isOccupied() || spaces[5][p.getOldY()].isOccupied()) {
					return false;
				} else if (KingRookSpace.getPiece().getName().equals("rook")) {

					Space newKingRookSpace = spaces[5][p.getOldY()];
					if (ChessBoard.playerTurn && newKingRookSpace.isThreatenedByBlack())
						return false;
					if (!ChessBoard.playerTurn && newKingRookSpace.isThreatenedByWhite())
						return false;
					threadSwitch(KingRookSpace.getPiece(), KingRookSpace, false, false);
					if (ChessBoard.playerTurn) {
						for (Space tmp : whitePieces) {
							if (tmp.equals(KingRookSpace)) {
								whitePieces.remove(tmp);
								whitePieces.add(newKingRookSpace);
								break;
							}
						}
					} else {
						for (Space tmp : blackPieces) {
							if (tmp.equals(KingRookSpace)) {
								blackPieces.remove(tmp);
								blackPieces.add(newKingRookSpace);
								break;
							}
						}
					}
					newKingRookSpace.setPiece(KingRookSpace.releasePiece());
					// threadSwitch(newKingRookSpace.getPiece(), newKingRookSpace, true, false);
					this.castled = true;
					return true;
				}
			}
			else if(m.isEqual(MoveList.KING_CASTLE_QUEENSIDE)&& !ChessBoard.kingInCheck){

				Space QueenRookSpace = spaces[0][p.getOldY()];
				if (!QueenRookSpace.isOccupied() || spaces[1][p.getOldY()].isOccupied()
						|| spaces[3][p.getOldY()].isOccupied()) {
					return false;
				} else if (QueenRookSpace.getPiece().getName().equals("rook")) {
					Space newQueenRookSpace = spaces[3][p.getOldY()];
					if (ChessBoard.playerTurn && newQueenRookSpace.isThreatenedByBlack())
						return false;
					if (!ChessBoard.playerTurn && newQueenRookSpace.isThreatenedByWhite())
						return false;
					threadSwitch(QueenRookSpace.getPiece(), QueenRookSpace, false, false);
					if (ChessBoard.playerTurn) {
						for (Space tmp : whitePieces) {
							if (tmp.equals(QueenRookSpace)) {
								whitePieces.remove(tmp);
								whitePieces.add(newQueenRookSpace);
								break;
							}
						}
					} else {
						for (Space tmp : blackPieces) {
							if (tmp.equals(QueenRookSpace)) {
								blackPieces.remove(tmp);
								blackPieces.add(newQueenRookSpace);
								break;
							}
						}
					}
					newQueenRookSpace.setPiece(QueenRookSpace.releasePiece());
					// threadSwitch(newQueenRookSpace.getPiece(), newQueenRookSpace, true, false);
					this.castled = true;
					return true;
				}
			} else {
				return true;
			}
		}
		return true;
	}

	protected boolean pawnValidityCheck(MoveInfo p, MoveList m) {
		
		Space oldSpace = spaces[p.getOldX()][p.getOldY()];
		Space newSpace = spaces[p.getNewX()][p.getNewY()];
		Piece piece = oldSpace.getPiece();

		//Si no es un peon, pasa la validacion
		if (!piece.getName().equals("pawn")) {
			return true;
		}

		/****************** PROMOTION *****************************/
		this.pawnHasPromoted = ((ChessBoard.playerTurn && p.getNewY() == 7)
				|| (!ChessBoard.playerTurn && p.getNewY() == 0)) ? true : false;
		// if this is a "straight" move
		if (p.getGapX() == 0) {
			// black is negative 1, white is positive 1, for direction later
			int colorMod = p.getGapY() / Math.abs(p.getGapY());

			// if there's a piece in the way for a straight move, don't allow move
			for (int c = 1; c <= Math.abs(p.getGapY()); c++) {
				if (spaces[p.getOldX()][p.getOldY() + (c * colorMod)].isOccupied()) {
					return false;
				}
			}

		} else { // Jugada en diagonal del peon
			/************* COMER AL PASO CON EL PEON ****************************/
			Space enemyPawn = null;
			if (piece.getColor() == "white") {

				if (p.getOldY() == 4 && m == MoveList.UP_RIGHT && (p.getOldX() + 1) < 8) {
					enemyPawn = spaces[p.getOldX() + 1][p.getOldY()];
					if (enemyPawn.getPiece() != null && enemyPawn.getPiece().getName() == "pawn"
							&& enemyPawn.getX() == previousBlackMove.getX()
							&& enemyPawn.getY() == previousBlackMove.getY() && !newSpace.isOccupied()
							&& prevBlackMoveList == MoveList.DOUBLE_DOWN) {
						enemyPawn.releasePiece();
						for (Space tmp : blackPieces) {
							if (tmp.equals(enemyPawn)) {
								blackPieces.remove(tmp);
								break;
							}
						}
						return true;
					}
				} else if (p.getOldY() == 4 && m == MoveList.UP_LEFT && (p.getOldX() - 1) >= 0) {
					enemyPawn = spaces[p.getOldX() - 1][p.getOldY()];
					if (enemyPawn.getPiece() != null && enemyPawn.getPiece().getName() == "pawn"
							&& enemyPawn.getX() == previousBlackMove.getX()
							&& enemyPawn.getY() == previousBlackMove.getY() && !newSpace.isOccupied()
							&& prevBlackMoveList == MoveList.DOUBLE_DOWN) {
						enemyPawn.releasePiece();
						for (Space tmp : blackPieces) {
							if (tmp.equals(enemyPawn)) {
								blackPieces.remove(tmp);
								break;
							}
						}
						return true;
					}
				}

			} else if (piece.getColor() == "black") {
				if (p.getOldY() == 3 && m == MoveList.DOWN_RIGHT && (p.getOldX() + 1) < 8) {
					enemyPawn = spaces[p.getOldX() + 1][p.getOldY()];
					if (enemyPawn.getPiece() != null && enemyPawn.getPiece().getName() == "pawn"
							&& enemyPawn.getX() == previousWhiteMove.getX()
							&& enemyPawn.getY() == previousWhiteMove.getY() && !newSpace.isOccupied()
							&& prevWhiteMoveList == MoveList.DOUBLE_UP) {
						enemyPawn.releasePiece();
						for (Space tmp : whitePieces) {
							if (tmp.equals(enemyPawn)) {
								whitePieces.remove(tmp);
								break;
							}
						}
						return true;
					}
				} else if (p.getOldY() == 3 && m == MoveList.DOWN_LEFT && (p.getOldX() - 1) >= 0) {
					enemyPawn = spaces[p.getOldX() - 1][p.getOldY()];
					if (enemyPawn.getPiece() != null && enemyPawn.getPiece().getName() == "pawn"
							&& enemyPawn.getX() == previousWhiteMove.getX()
							&& enemyPawn.getY() == previousWhiteMove.getY() && !newSpace.isOccupied()
							&& prevWhiteMoveList == MoveList.DOUBLE_UP) {
						enemyPawn.releasePiece();
						for (Space tmp : whitePieces) {
							if (tmp.equals(enemyPawn)) {
								whitePieces.remove(tmp);
								break;
							}
						}
						return true;
					}
				}
			}
			//Si la casilla destino no tiene una pieza enemiga, no permitir la jugada
			if ((!newSpace.isOccupied()) || piece.getColor() == newSpace.getPiece().getColor()) {
				return false;
			}
		}

		return true;
	}

}
