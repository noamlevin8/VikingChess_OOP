import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
public class GameLogic implements PlayableLogic{
    private ConcretePlayer p1; //Player no.1 (defence)
    private ConcretePlayer p2; //Player no.2 (attack)
    private ConcretePiece[][] board;
    private int turn;
    private Position kingPos;
    private Stack<Position> plays; //All playes by position
    private ConcretePiece[] pieces; //All pieces
    private Stack<ConcretePiece[][]> turns; //Saves the board in every turn (for the undo)
    private int[][][] squares; //Counting visits in all squares

    // Constructor
    public GameLogic(){
        this.p1 = new ConcretePlayer(true);
        this.p2 = new ConcretePlayer(false);
        reset();
    }

    @Override
    public boolean move(Position a, Position b) {
        if (isValidMove(a,b)){
            ConcretePiece[][] tempB = new ConcretePiece[11][11];
            for (int i = 0; i <= 10; i++){
                for (int j = 0; j <= 10; j++){
                    tempB[i][j] = this.board[i][j];
                }
            }
            this.turns.add(tempB);

            getPieceAtPosition(a).getSteps().add(b);
            getPieceAtPosition(a).addSquares();

            // add visit in the correct square
            String name = getPieceAtPosition(a).getName().substring(1);
            Integer num = Integer.parseInt(name);
            if(this.turn == 1){
                this.squares[b.getX()][b.getY()][num+23]++;
            }
            else {
                this.squares[b.getX()][b.getY()][num - 1]++;
            }

            // update the board
            this.board[b.getX()][b.getY()] = getPieceAtPosition(a);
            this.board[a.getX()][a.getY()] = null;

            // change of king's position
            if (getPieceAtPosition(b) instanceof King)
                this.kingPos = b;

            // if the game is finished do kill, else print statistics
            if (!isGameFinished())
                kill(b);
            else {
                if(getPieceAtPosition(this.plays.peek()).getOwner() == p2) {
                    this.p1.addWin();
                    printBySteps(true);
                    printByKills(this.p1);
                    printBySquares(this.p1);
                    printByVisits();
                }
                else {
                    this.p2.addWin();
                    printBySteps(false);
                    printByKills(this.p2);
                    printBySquares(this.p2);
                    printByVisits();
                }
            }

            // change turn
            if (this.turn == 1)
                this.turn = 2;
            else
                this.turn = 1;

            // add the positions to the plays stack
            this.plays.add(a);
            this.plays.add(b);
            return true;
        }
        return false;
    }

    // Checks if the desired move is valid
    public boolean isValidMove(Position a, Position b){
        // one of the positions is not valid
        if (getPieceAtPosition(a) == null)
            return false;
        if (getPieceAtPosition(b) != null)
            return false;

        // it is not the player's turn
        if (this.turn == 1 && getPieceAtPosition(a).getOwner() != this.p1)
            return false;
        if (this.turn == 2 && getPieceAtPosition(a).getOwner() != this.p2)
            return false;

        // not a valid movement
        if(a.getX() != b.getX() && a.getY() != b.getY())
            return false;

        // a pawn can't be on one of the corners
        if (b.getX() == 0 && b.getY() == 0){
            if (getPieceAtPosition(a) instanceof Pawn){
                return false;
            }
        }
        if (b.getX() == 10 && b.getY() == 10){
            if (getPieceAtPosition(a) instanceof Pawn){
                return false;
            }
        }
        if (b.getX() == 0 && b.getY() == 10){
            if (getPieceAtPosition(a) instanceof Pawn){
                return false;
            }
        }
        if (b.getX() == 10 && b.getY() == 0){
            if (getPieceAtPosition(a) instanceof Pawn){
                return false;
            }
        }

        // checking there are no pieces in the middle of the movement
        if (a.getX() == b.getX()){
            if (a.getY() < b.getY()) {
                for (int i = a.getY() + 1; i <= b.getY() - 1; i++) {
                    Position p = new Position(a.getX(),i);
                    if (getPieceAtPosition(p) != null)
                        return false;
                }
            }
            else {
                for (int i = b.getY() + 1; i <= a.getY() - 1; i++) {
                    Position p = new Position(a.getX(),i);
                    if (getPieceAtPosition(p) != null)
                        return false;
                }
            }
        }
        else {
            if (a.getX() < b.getX()) {
                for (int i = a.getX() + 1; i <= b.getX() - 1; i++) {
                    Position p = new Position(i,a.getY());
                    if (getPieceAtPosition(p) != null)
                        return false;
                }
            }
            else {
                for (int i = b.getX() + 1; i <= a.getX() - 1; i++) {
                    Position p = new Position(i,a.getY());
                    if (getPieceAtPosition(p) != null)
                        return false;
                }
            }
        }
        return true;
    }

    // Kills piece if necessary
    public void kill(Position b){
        Position p1;
        Position p2;
        Piece last = null;

        // king can't kill
        if (getPieceAtPosition(b) instanceof King)
            return;

        // if the dest position is (10, ?)
        if (b.getX() == 10){
            // checking only one possible side
            p1 = new Position(9,b.getY());
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(8,b.getY());
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }

            // checking from to sides by Y
            if (b.getY() > 1 && b.getY() < 9){
                p1 = new Position(10,b.getY() - 1);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(10,b.getY() - 2);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
                p1 = new Position(10,b.getY() + 1);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(10,b.getY() + 2);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
            }
        }

        // same but when the position is (0, ?)
        if (b.getX() == 0){
            p1 = new Position(1,b.getY());
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(2,b.getY());
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            if (b.getY() > 1 && b.getY() < 9){
                p1 = new Position(0,b.getY() - 1);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(0,b.getY() - 2);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
                p1 = new Position(0,b.getY() + 1);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(0,b.getY() + 2);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
            }
        }

        // same but when the position is (?, 0)
        if (b.getY() == 0){
            p1 = new Position(b.getX(), 1);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(b.getX(), 2);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            if (b.getX() > 1 && b.getX() < 9){
                p1 = new Position(b.getX() - 1,0);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(b.getX() - 2, 0);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
                p1 = new Position(b.getX() + 1, 0);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(b.getX() + 2, 0);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
            }
        }

        // same but when the position is (?, 10)
        if (b.getY() == 10){
            p1 = new Position(b.getX(), 9);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(b.getX(), 8);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            if (b.getX() > 1 && b.getX() < 9){
                p1 = new Position(b.getX() - 1,10);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(b.getX() - 2, 10);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
                p1 = new Position(b.getX() + 1, 10);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                    p2 = new Position(b.getX() + 2, 10);
                    if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                        this.board[p1.getX()][p1.getY()] = null;
                        ((Pawn)getPieceAtPosition(b)).addKill();
                    }
                }
            }
        }

        // kill with side or on the other sides
        if (b.getY() == 9 && b.getX() != 10 && b.getX() != 0){
            p1 = new Position(b.getX(),10);
            p2 = new Position(b.getX(), 8);
            Position p3 = new Position(b.getX(), 7);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (getPieceAtPosition(p2) != null && getPieceAtPosition(p3) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p3).getOwner() == getPieceAtPosition(b).getOwner()){
                this.board[p2.getX()][p2.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }

            // only two sides
            if (b.getX() == 1) {
                p1 = new Position(b.getX() + 1, b.getY());
                p2 = new Position(b.getX() + 2, b.getY());
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn) getPieceAtPosition(b)).addKill();
                }
            }
            else if (b.getX() == 9) {
                p1 = new Position(b.getX() - 1, b.getY());
                p2 = new Position(b.getX() - 2, b.getY());
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }


            // all sides
            else {
                p1 = new Position(b.getX() + 1, b.getY());
                p2 = new Position(b.getX() + 2, b.getY());
                p3 = new Position(b.getX() - 1, b.getY());
                Position p4 = new Position(b.getX() - 2, b.getY());
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
                if (getPieceAtPosition(p3) != null && getPieceAtPosition(p4) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p4) instanceof Pawn && getPieceAtPosition(p3).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p4).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p3.getX()][p3.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }

        // same
        if (b.getY() == 1 && b.getX() != 10 && b.getX() != 0){
            p1 = new Position(b.getX(),0);
            p2 = new Position(b.getX(), 2);
            Position p3 = new Position(b.getX(), 3);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (getPieceAtPosition(p2) != null && getPieceAtPosition(p3) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p3).getOwner() == getPieceAtPosition(b).getOwner()){
                this.board[p2.getX()][p2.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (b.getX() == 1) {
                p1 = new Position(b.getX() + 1, b.getY());
                p2 = new Position(b.getX() + 2, b.getY());
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn) getPieceAtPosition(b)).addKill();
                }
            }
            else if (b.getX() == 9) {
                p1 = new Position(b.getX() - 1, b.getY());
                p2 = new Position(b.getX() - 2, b.getY());
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            else {
                p1 = new Position(b.getX() + 1, b.getY());
                p2 = new Position(b.getX() + 2, b.getY());
                p3 = new Position(b.getX() - 1, b.getY());
                Position p4 = new Position(b.getX() - 2, b.getY());
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
                if (getPieceAtPosition(p3) != null && getPieceAtPosition(p4) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p4) instanceof Pawn && getPieceAtPosition(p3).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p4).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p3.getX()][p3.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }

        }

        // same
        if (b.getX() == 9 && b.getY() != 10 && b.getY() != 0){
            p1 = new Position(10, b.getY());
            p2 = new Position(8, b.getY());
            Position p3 = new Position(7, b.getY());
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (getPieceAtPosition(p2) != null && getPieceAtPosition(p3) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p3).getOwner() == getPieceAtPosition(b).getOwner()){
                this.board[p2.getX()][p2.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (b.getY() == 1) {
                p1 = new Position(b.getX(), b.getY() + 1);
                p2 = new Position(b.getX(), b.getY() + 2);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn) getPieceAtPosition(b)).addKill();
                }
            }
            else if (b.getY() == 9) {
                p1 = new Position(b.getX(), b.getY() - 1);
                p2 = new Position(b.getX(), b.getY() - 2);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            else {
                p1 = new Position(b.getX(), b.getY() + 1);
                p2 = new Position(b.getX(), b.getY() + 2);
                p3 = new Position(b.getX(), b.getY()- 1);
                Position p4 = new Position(b.getX(), b.getY()- 2);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
                if (getPieceAtPosition(p3) != null && getPieceAtPosition(p4) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p4) instanceof Pawn && getPieceAtPosition(p3).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p4).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p3.getX()][p3.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }

        // same
        if (b.getX() == 1 && b.getY() != 10 && b.getY() != 0){
            p1 = new Position(0, b.getY());
            p2 = new Position(2, b.getY());
            Position p3 = new Position(3, b.getY());
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (getPieceAtPosition(p2) != null && getPieceAtPosition(p3) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p3).getOwner() == getPieceAtPosition(b).getOwner()){
                this.board[p2.getX()][p2.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
            if (b.getY() == 1) {
                p1 = new Position(b.getX(), b.getY() + 1);
                p2 = new Position(b.getX(), b.getY() + 2);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn) getPieceAtPosition(b)).addKill();
                }
            }
            else if (b.getY() == 9) {
                p1 = new Position(b.getX(), b.getY() - 1);
                p2 = new Position(b.getX(), b.getY() - 2);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            else {
                p1 = new Position(b.getX(), b.getY() + 1);
                p2 = new Position(b.getX(), b.getY() + 2);
                p3 = new Position(b.getX(), b.getY() - 1);
                Position p4 = new Position(b.getX(), b.getY() - 2);
                if (getPieceAtPosition(p1) != null && getPieceAtPosition(p2) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
                if (getPieceAtPosition(p3) != null && getPieceAtPosition(p4) != null && getPieceAtPosition(p3) instanceof Pawn && getPieceAtPosition(p4) instanceof Pawn && getPieceAtPosition(p3).getOwner() != getPieceAtPosition(b).getOwner() && getPieceAtPosition(p4).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p3.getX()][p3.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }

        // regular state
        if (b.getX() > 1 && b.getX() < 9 && b.getY() > 1 && b.getY() < 9){
            p1 = new Position(b.getX() - 1, b.getY());
            p2 = new Position(b.getX() - 2, b.getY());
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            p1 = new Position(b.getX() + 1, b.getY());
            p2 = new Position(b.getX() + 2, b.getY());
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            p1 = new Position(b.getX(), b.getY() - 1);
            p2 = new Position(b.getX(), b.getY() - 2);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
            p1 = new Position(b.getX(), b.getY() + 1);
            p2 = new Position(b.getX(), b.getY() + 2);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()){
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }

        // kill with corner
        if (b.getX() == 10 && b.getY() == 8){
            p1 = new Position(10, 9);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 10 && b.getY() == 2){
            p1 = new Position(10, 1);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 0 && b.getY() == 8){
            p1 = new Position(0, 9);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 0 && b.getY() == 2){
            p1 = new Position(0, 1);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 8 && b.getY() == 10){
            p1 = new Position(9,10);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 2 && b.getY() == 10){
            p1 = new Position(1,10);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 8 && b.getY() == 0){
            p1 = new Position(9,0);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }
        if (b.getX() == 2 && b.getY() == 0){
            p1 = new Position(1,0);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                this.board[p1.getX()][p1.getY()] = null;
                ((Pawn)getPieceAtPosition(b)).addKill();
            }
        }

        // kill on the frame
        if (b.getX() == 9 && b.getY() == 10){
            p1 = new Position(8, 10);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(7,10);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 9 && b.getY() == 0){
            p1 = new Position(8, 0);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(7,0);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 1 && b.getY() == 10){
            p1 = new Position(2, 10);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(3,10);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 1 && b.getY() == 0){
            p1 = new Position(2, 0);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(3,0);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 10 && b.getY() == 9){
            p1 = new Position(10,8);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(10,7);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 10 && b.getY() == 1){
            p1 = new Position(10,2);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(10,3);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 0 && b.getY() == 9){
            p1 = new Position(0,8);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(0,7);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
        if (b.getX() == 0 && b.getY() == 1){
            p1 = new Position(0,2);
            if (getPieceAtPosition(p1) != null && getPieceAtPosition(p1) instanceof Pawn && getPieceAtPosition(p1).getOwner() != getPieceAtPosition(b).getOwner()){
                p2 = new Position(0,3);
                if (getPieceAtPosition(p2) != null && getPieceAtPosition(p2) instanceof Pawn && getPieceAtPosition(p2).getOwner() == getPieceAtPosition(b).getOwner()) {
                    this.board[p1.getX()][p1.getY()] = null;
                    ((Pawn)getPieceAtPosition(b)).addKill();
                }
            }
        }
    }

    // Returns the piece at the desired position
    @Override
    public ConcretePiece getPieceAtPosition(Position position) {
        if(this.board[position.getX()][position.getY()] == null)
            return null;
        return this.board[position.getX()][position.getY()];
    }

    @Override
    public ConcretePlayer getFirstPlayer() {
        return this.p1;
    }

    @Override
    public ConcretePlayer getSecondPlayer() {
        return this.p2;
    }

    // Checks if the game is finished
    @Override
    public boolean isGameFinished() {
        // if king is on one of the corners
        Position p = new Position(0,0);
        if (getPieceAtPosition(p) instanceof King) {
            return true;
        }
        p = new Position(10,0);
        if (getPieceAtPosition(p) instanceof King){
            return true;
        }
        p = new Position(0,10);
        if (getPieceAtPosition(p) instanceof King){
            return true;
        }
        p = new Position(10,10);
        if (getPieceAtPosition(p) instanceof King){
            return true;
        }

        // if the king is surrounded (4 sides)
        if (this.kingPos.getX() > 0 && this.kingPos.getX() < 10 && this.kingPos.getY() > 0 && this.kingPos.getY() < 10){
            Position p1 = new Position(this.kingPos.getX() - 1, this.kingPos.getY());
            Position p2 = new Position(this.kingPos.getX() + 1, this.kingPos.getY());
            Position p3 = new Position(this.kingPos.getX(), this.kingPos.getY() - 1);
            Position p4 = new Position(this.kingPos.getX(), this.kingPos.getY() + 1);
            if (getPieceAtPosition(p1) == null || getPieceAtPosition(p2) == null || getPieceAtPosition(p3) == null || getPieceAtPosition(p4) == null){
                return false;
            }
            if (getPieceAtPosition(p1).getOwner() == this.p1 || getPieceAtPosition(p2).getOwner() == this.p1 || getPieceAtPosition(p3).getOwner() == this.p1 || getPieceAtPosition(p4).getOwner() == this.p1){
                return false;
            }
            return true;
        }

        // if the king is surrounded (3 sides)
        if (this.kingPos.getX() == 0 && this.kingPos.getY() > 1 && this.kingPos.getY() < 9) {
            Position p1 = new Position(this.kingPos.getX(), this.kingPos.getY() - 1);
            Position p2 = new Position(this.kingPos.getX(), this.kingPos.getY() + 1);
            Position p3 = new Position(this.kingPos.getX() + 1, this.kingPos.getY());
            if (getPieceAtPosition(p1) == null || getPieceAtPosition(p2) == null || getPieceAtPosition(p3) == null) {
                return false;
            }
            if (getPieceAtPosition(p1).getOwner() == this.p1 || getPieceAtPosition(p2).getOwner() == this.p1 || getPieceAtPosition(p3).getOwner() == this.p1) {
                return false;
            }
            return true;
        }
        if (this.kingPos.getX() == 10 && this.kingPos.getY() > 1 && this.kingPos.getY() < 9) {
            Position p1 = new Position(this.kingPos.getX(), this.kingPos.getY() - 1);
            Position p2 = new Position(this.kingPos.getX(), this.kingPos.getY() + 1);
            Position p3 = new Position(this.kingPos.getX() - 1, this.kingPos.getY());
            if (getPieceAtPosition(p1) == null || getPieceAtPosition(p2) == null || getPieceAtPosition(p3) == null) {
                return false;
            }
            if (getPieceAtPosition(p1).getOwner() == this.p1 || getPieceAtPosition(p2).getOwner() == this.p1 || getPieceAtPosition(p3).getOwner() == this.p1) {
                return false;
            }
            return true;
        }
        if (this.kingPos.getY() == 0 && this.kingPos.getX() > 1 && this.kingPos.getX() < 9) {
            Position p1 = new Position(this.kingPos.getX() + 1, this.kingPos.getY());
            Position p2 = new Position(this.kingPos.getX() - 1, this.kingPos.getY());
            Position p3 = new Position(this.kingPos.getX(), this.kingPos.getY() + 1);
            if (getPieceAtPosition(p1) == null || getPieceAtPosition(p2) == null || getPieceAtPosition(p3) == null) {
                return false;
            }
            if (getPieceAtPosition(p1).getOwner() == this.p1 || getPieceAtPosition(p2).getOwner() == this.p1 || getPieceAtPosition(p3).getOwner() == this.p1) {
                return false;
            }
            return true;
        }
        if (this.kingPos.getY() == 10 && this.kingPos.getX() > 1 && this.kingPos.getX() < 9) {
            Position p1 = new Position(this.kingPos.getX() + 1, this.kingPos.getY());
            Position p2 = new Position(this.kingPos.getX() - 1, this.kingPos.getY());
            Position p3 = new Position(this.kingPos.getX(), this.kingPos.getY() - 1);
            if (getPieceAtPosition(p1) == null || getPieceAtPosition(p2) == null || getPieceAtPosition(p3) == null) {
                return false;
            }
            if (getPieceAtPosition(p1).getOwner() == this.p1 || getPieceAtPosition(p2).getOwner() == this.p1 || getPieceAtPosition(p3).getOwner() == this.p1) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isSecondPlayerTurn() {
        return this.turn==2;
    }

    @Override
    public void reset() {
        this.board = new ConcretePiece[11][11];
        this.turn = 2;
        this.kingPos = new Position(5,5);
        this.plays = new Stack<>();
        this.pieces = new ConcretePiece[37];
        this.turns = new Stack<>();
        this.squares = new int[11][11][37];

        // pieces
        ConcretePiece p = new Pawn(this.p1, "♙", "D1", new Position(5,3));
        this.board[5][3] = p;
        this.pieces[24] = p;
        this.squares[5][3][24]++;
        p = new Pawn(this.p1, "♙", "D2", new Position(4,4));
        this.board[4][4] = p;
        this.pieces[25] = p;
        this.squares[4][4][25]++;
        p = new Pawn(this.p1, "♙", "D3", new Position(5,4));
        this.board[5][4] = p;
        this.pieces[26] = p;
        this.squares[5][4][26]++;
        p = new Pawn(this.p1, "♙", "D4", new Position(6,4));
        this.board[6][4] = p;
        this.pieces[27] = p;
        this.squares[6][4][27]++;
        p = new Pawn(this.p1, "♙", "D5", new Position(3,5));
        this.board[3][5] = p;
        this.pieces[28] = p;
        this.squares[3][5][28]++;
        p = new Pawn(this.p1, "♙", "D6", new Position(4,5));
        this.board[4][5] = p;
        this.pieces[29] = p;
        this.squares[4][5][29]++;
        p = new King(this.p1, "♔", "K7", new Position(5,5));
        this.board[5][5] = p;
        this.pieces[30] = p;
        this.squares[5][5][30]++;
        p = new Pawn(this.p1, "♙", "D8", new Position(6,5));
        this.board[6][5] = p;
        this.pieces[31] = p;
        this.squares[6][5][31]++;
        p = new Pawn(this.p1, "♙", "D9", new Position(7,5));
        this.board[7][5] = p;
        this.pieces[32] = p;
        this.squares[7][5][32]++;
        p = new Pawn(this.p1, "♙", "D10", new Position(4,6));
        this.board[4][6] = p;
        this.pieces[33] = p;
        this.squares[4][6][33]++;
        p = new Pawn(this.p1, "♙", "D11", new Position(5,6));
        this.board[5][6] = p;
        this.pieces[34] = p;
        this.squares[5][6][34]++;
        p = new Pawn(this.p1, "♙", "D12", new Position(6,6));
        this.board[6][6] = p;
        this.pieces[35] = p;
        this.squares[6][6][35]++;
        p = new Pawn(this.p1, "♙", "D13", new Position(5,7));
        this.board[5][7] = p;
        this.pieces[36] = p;
        this.squares[5][7][36]++;

        p = new Pawn(this.p2, "♟", "A1", new Position(3,0));
        this.board[3][0] = p;
        this.pieces[0] = p;
        this.squares[3][0][0]++;
        p = new Pawn(this.p2, "♟", "A2", new Position(4,0));
        this.board[4][0] = p;
        this.pieces[1] = p;
        this.squares[4][0][1]++;
        p = new Pawn(this.p2, "♟", "A3", new Position(5,0));
        this.board[5][0] = p;
        this.pieces[2] = p;
        this.squares[5][0][2]++;
        p = new Pawn(this.p2, "♟", "A4", new Position(6,0));
        this.board[6][0] = p;
        this.pieces[3] = p;
        this.squares[6][0][3]++;
        p = new Pawn(this.p2, "♟", "A5", new Position(7,0));
        this.board[7][0] = p;
        this.pieces[4] = p;
        this.squares[7][0][4]++;
        p = new Pawn(this.p2, "♟", "A6", new Position(5,1));
        this.board[5][1] = p;
        this.pieces[5] = p;
        this.squares[5][1][5]++;
        p = new Pawn(this.p2, "♟", "A7", new Position(0,3));
        this.board[0][3] = p;
        this.pieces[6] = p;
        this.squares[0][3][6]++;
        p = new Pawn(this.p2, "♟", "A8", new Position(10,3));
        this.board[10][3] = p;
        this.pieces[7] = p;
        this.squares[10][3][7]++;
        p = new Pawn(this.p2, "♟", "A9", new Position(0,4));
        this.board[0][4] = p;
        this.pieces[8] = p;
        this.squares[0][4][8]++;
        p = new Pawn(this.p2, "♟", "A10", new Position(10,4));
        this.board[10][4] = p;
        this.pieces[9] = p;
        this.squares[10][4][9]++;
        p = new Pawn(this.p2, "♟", "A11", new Position(0,5));
        this.board[0][5] = p;
        this.pieces[10] = p;
        this.squares[0][5][10]++;
        p = new Pawn(this.p2, "♟", "A12", new Position(1,5));
        this.board[1][5] = p;
        this.pieces[11] = p;
        this.squares[1][5][11]++;
        p = new Pawn(this.p2, "♟", "A13", new Position(9,5));
        this.board[9][5] = p;
        this.pieces[12] = p;
        this.squares[9][5][12]++;
        p = new Pawn(this.p2, "♟", "A14", new Position(10,5));
        this.board[10][5] = p;
        this.pieces[13] = p;
        this.squares[10][5][13]++;
        p = new Pawn(this.p2, "♟", "A15", new Position(0,6));
        this.board[0][6] = p;
        this.pieces[14] = p;
        this.squares[0][6][14]++;
        p = new Pawn(this.p2, "♟", "A16", new Position(10,6));
        this.board[10][6] = p;
        this.pieces[15] = p;
        this.squares[10][6][15]++;
        p = new Pawn(this.p2, "♟", "A17", new Position(0,7));
        this.board[0][7] = p;
        this.pieces[16] = p;
        this.squares[0][7][16]++;
        p = new Pawn(this.p2, "♟", "A18", new Position(10,7));
        this.board[10][7] = p;
        this.pieces[17] = p;
        this.squares[10][7][17]++;
        p = new Pawn(this.p2, "♟", "A19", new Position(5,9));
        this.board[5][9] = p;
        this.pieces[18] = p;
        this.squares[5][9][18]++;
        p = new Pawn(this.p2, "♟", "A20", new Position(3,10));
        this.board[3][10] = p;
        this.pieces[19] = p;
        this.squares[3][10][19]++;
        p = new Pawn(this.p2, "♟", "A21", new Position(4,10));
        this.board[4][10] = p;
        this.pieces[20] = p;
        this.squares[4][10][20]++;
        p = new Pawn(this.p2, "♟", "A22", new Position(5,10));
        this.board[5][10] = p;
        this.pieces[21] = p;
        this.squares[5][10][21]++;
        p = new Pawn(this.p2, "♟", "A23", new Position(6,10));
        this.board[6][10] = p;
        this.pieces[22] = p;
        this.squares[6][10][22]++;
        p = new Pawn(this.p2, "♟", "A24", new Position(7,10));
        this.board[7][10] = p;
        this.pieces[23] = p;
        this.squares[7][10][23]++;
    }

    @Override
    public void undoLastMove() {
        if (!this.turns.isEmpty()) {
            this.board = this.turns.pop();
            if (this.turn == 1) {
                this.turn = 2;
            }
            else {
                this.turn = 1;
            }
        }
    }

    @Override
    public int getBoardSize() {
        return this.board.length;
    }

    // Game statistics
    public void printBySteps(boolean p){
        ArrayList<ConcretePiece> defence = new ArrayList<>();
        ArrayList<ConcretePiece> attack = new ArrayList<>();
        for (int i = 24; i <= 36; i++){
            defence.add(this.pieces[i]);
        }
        for (int i = 0; i <= 23; i++){
            attack.add(this.pieces[i]);
        }
        Collections.sort(defence, new ComperBySteps());
        Collections.sort(attack, new ComperBySteps());
        int s1 = 0, s2 = 0;
        while (defence.get(s1).getSteps().size() < 2)
            s1++;
        while (attack.get(s2).getSteps().size() < 2)
            s2++;
        if (p == true) {
            for (int i = s1; i < defence.size(); i++) {
                System.out.print(defence.get(i).getName() + ": [");
                for (int j = 0; j < defence.get(i).getSteps().size() - 1; j++) {
                    System.out.print("(" + defence.get(i).getSteps().get(j).getX() + ", " + defence.get(i).getSteps().get(j).getY() + "), ");
                }
                System.out.println("(" + defence.get(i).getSteps().get(defence.get(i).getSteps().size() - 1).getX() + ", " + defence.get(i).getSteps().get(defence.get(i).getSteps().size() - 1).getY() + ")]");
            }
            for (int i = s2; i < attack.size(); i++) {
                System.out.print(attack.get(i).getName() + ": [");
                for (int j = 0; j < attack.get(i).getSteps().size() - 1; j++) {
                    System.out.print("(" + attack.get(i).getSteps().get(j).getX() + ", " + attack.get(i).getSteps().get(j).getY() + "), ");
                }
                System.out.println("(" + attack.get(i).getSteps().get(attack.get(i).getSteps().size() - 1).getX() + ", " + attack.get(i).getSteps().get(attack.get(i).getSteps().size() - 1).getY() + ")]");
            }
        }
        else {
            for (int i = s2; i < attack.size(); i++) {
                System.out.print(attack.get(i).getName() + ": [");
                for (int j = 0; j < attack.get(i).getSteps().size() - 1; j++) {
                    System.out.print("(" + attack.get(i).getSteps().get(j).getX() + ", " + attack.get(i).getSteps().get(j).getY() + "), ");
                }
                System.out.println("(" + attack.get(i).getSteps().get(attack.get(i).getSteps().size() - 1).getX() + ", " + attack.get(i).getSteps().get(attack.get(i).getSteps().size() - 1).getY() + ")]");
            }
            for (int i = s1; i < defence.size(); i++) {
                System.out.print(defence.get(i).getName() + ": [");
                for (int j = 0; j < defence.get(i).getSteps().size() - 1; j++) {
                    System.out.print("(" + defence.get(i).getSteps().get(j).getX() + ", " + defence.get(i).getSteps().get(j).getY() + "), ");
                }
                System.out.println("(" + defence.get(i).getSteps().get(defence.get(i).getSteps().size() - 1).getX() + ", " + defence.get(i).getSteps().get(defence.get(i).getSteps().size() - 1).getY() + ")]");
            }
        }
        for (int i = 0; i < 75; i++)
            System.out.print("*");
        System.out.println();
    }
    public void printByKills(ConcretePlayer p){
        ArrayList<ConcretePiece> cp = new ArrayList<>();
        for (int i = 0; i <= 36; i++) {
            if (i != 30) // without the king
                cp.add(this.pieces[i]);
        }

        Collections.sort(cp, new ComperByKills(p));
        int s = cp.size()-1;
        while (s >= 0 && ((Pawn)cp.get(s)).getKills() == 0){
            s--;
        }
        for (int i = 0; i <= s; i++){
            System.out.println(cp.get(i).getName() + ": " + ((Pawn)cp.get(i)).getKills() + " kills");
        }
        for (int i = 0; i < 75; i++)
            System.out.print("*");
        System.out.println();
    }
    public void printBySquares(ConcretePlayer p){
        ArrayList<ConcretePiece> cp = new ArrayList<>();
        for (int i = 0; i <= 36; i++) {
            cp.add(this.pieces[i]);
        }

        Collections.sort(cp, new ComperBySquares(p));
        int s = cp.size()-1;
        while (s >= 0 && cp.get(s).getSquares() == 0){
            s--;
        }
        for (int i = 0; i <= s; i++){
            System.out.println(cp.get(i).getName() + ": " + cp.get(i).getSquares() + " squares");
        }
        for (int i = 0; i < 75; i++)
            System.out.print("*");
        System.out.println();
    }
    public void printByVisits(){
        ArrayList<Position> p = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j <= 10; j++) {
                Position ps = new Position(i,j);
                int n = 0;
                for (int k = 0; k <= 36; k++){
                    if(this.squares[i][j][k] > 0)
                        ps.addVisit();
                }
                p.add(ps);
            }
        }

        Collections.sort(p, new ComperByVisits());
        int s = p.size()-1;
        while (s >= 0 && p.get(s).getVisits() < 2){
            s--;
        }
        for (int i = 0; i <= s; i++){
            System.out.println("(" + p.get(i).getX() + ", " + p.get(i).getY() + ")" + p.get(i).getVisits() + " pieces");
        }
        for (int i = 0; i < 75; i++)
            System.out.print("*");
        System.out.println();
    }
}
