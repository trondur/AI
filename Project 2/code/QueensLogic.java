/**
 * This class implements the logic behind the BDD for the n-queens problem
 * You should implement all the missing methods
 * 
 * @author Stavros Amanatidis
 *
 */
import java.util.*;

import net.sf.javabdd.*;

public class QueensLogic {
    private int[][] board;

    private int N;
    private BDDFactory fact;
    private BDD True;
    private BDD False;
    private BDD bdd;


    public QueensLogic() {
       //constructor
    }

    public void initializeGame(int size) {
        this.N = size;
        this.board = new int[N][N];

        createBDD();
        setInvalids();
    }

    //Initializes the BDD
    private void createBDD(){
        fact = JFactory.init(2000000,200000);
		fact.setVarNum(N*N);

        this.True = fact.one();
		this.False = fact.zero();

        bdd = True;

        createRules();
    }

    //Creates rules for the BDD
    private void createRules(){
        for(int i = 0; i<N; i++){
            BDD colHasQueen = False;
            for(int j = 0; j<N; j++){
                //Constraint for the column to make sure atleast one cell contains a queen 
                colHasQueen = colHasQueen.or(fact.ithVar(position(i,j)));
                //Add no capture constraints for cell
                noCaptureRule(i,j);
            }

            bdd = bdd.and(colHasQueen);
        }
    }

    //Adds constraint for cell to make sure a placed queen won't be able to capture another
    private void noCaptureRule(int c, int r){
        BDD noCapture = True;

        //Adds constraints to make sure no queen is placed in the same column or row.
        for(int i = 0; i < N; i++){
            if(i != c){
                noCapture = noCapture.and(fact.nithVar(position(i,r)));
            }
            if(i != r){
                noCapture = noCapture.and(fact.nithVar(position(c,i)));
            }
        }

        //Add constrains to make sure no queen is placed in the same diagonals.
        for(int i = 0; i < N; i++){
            if(!(r-(i-c) < 0 || r-(i-c) > N-1) && c != i){
                noCapture = noCapture.and(fact.nithVar(position(i, r-(i-c))));
            }
        }
        for(int i = 0; i < N; i++){
            if(!(r-(c-i) < 0 || r-(c-i) > N-1) && c != i){
                noCapture = noCapture.and(fact.nithVar(position(i, r-(c-i))));
            }
        }
        
        bdd = bdd.and(fact.nithVar(position(c,r)).or(noCapture));
    }

    //Returns the variable number of the cell
    private int position(int c, int r){
        return c*N+r;
    }
   
    public int[][] getGameBoard() {
        return board;
    }

    public boolean insertQueen(int column, int row) {

        if (board[column][row] == -1 || board[column][row] == 1) {
            return true;
        }
        
        board[column][row] = 1;
        
        //Restricts the BDD with the value of the cell, where the queen is placed, is true
        bdd = bdd.restrict(fact.ithVar(position(column,row)));

        //Set invalid positions
        setInvalids();

        //If only one possible solutions is left, place remaining queens.
        if(bdd.pathCount() == 1){
            setRemainingValids();
        }
      
        return true;
    }

    //If a placement is invalid register on the board
    private void setInvalids(){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                if(isInvalid(i,j)){
                    board[i][j] = -1;
                }
            }
        }
    }

    //If a placement is valid set queen
    //Should only be called if only one solution remains
    private void setRemainingValids(){
        for(int i = 0; i < N; i++){
            for(int j = 0; j < N; j++){
                if(!isInvalid(i,j)){
                    board[i][j] = 1;
                }
            }
        }
    }

    //Returns whether placing a queen in the given position results in an invalid state.
    private boolean isInvalid(int c, int r){
        return bdd.restrict(fact.ithVar(position(c,r))).isZero();
    }
}
