
public class GameLogic implements IGameLogic {
    private int x = 0;
    private int y = 0;
    private int playerID;

    private int gameBoard[][];
    private int height[];
    private int latestCol = -1;
    private int latestRow = -1;
    private int latestP = -1;
    
    public GameLogic() {
        //TODO Write your implementation for this method
    }
	//x = columns, y = rows
    public void initializeGame(int x, int y, int playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        this.gameBoard = new int[x][y];
        this.height = new int[x];
    }
	
    public Winner gameFinished() {
        int res = Checking.checkWinner(gameBoard, x, y, latestCol, latestRow, latestP);
        return res < 0 ? Winner.NOT_FINISHED : res == 1 ? Winner.PLAYER1 : Winner.PLAYER2;
    }

    public void insertCoin(int column, int playerID) {
        gameBoard[column][height[column]] = playerID;
        latestCol = column;
        latestRow = height[column];
        latestP = playerID;
        height[column] += 1;
    }

    public int decideNextMove() {
        return alphaBeta(new State(gameBoard, height, playerID));
    }

    private int miniMax(State s){
        //int[][] cgb = copy(gb);
        int best = -1;
        int max = -1000;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, Integer.MIN_VALUE, Integer.MAX_VALUE);
            System.out.println(res);
            best = res > max ? i : best;
            max = res > max ? res : max;
        }
        //System.out.println("--------------");
        return best;
    }

    private int alphaBeta(State s){
        int best = -1;
        int max = -1000;
        int alp = Integer.MIN_VALUE;
        int bet = Integer.MAX_VALUE;
        System.out.println("start");
        if(s.isTerminal() >= 0) return utility(s);
        System.out.println("stop");
        int v = Integer.MIN_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, alp, bet);
            //System.out.println(res);
            best = res > v ? i : best;
            v = res > v ? res : v;
            alp = v > alp ? v : alp;
        }
        return best;
    }

    private int maxValue(State s, int alp, int bet){
        if(s.isTerminal() >= 0) return utility(s);
        int v = Integer.MIN_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, alp, bet);
            v = res > v ? res : v;
            if(v >= bet) return v;
            alp = v > alp ? v : alp;
        }
        return v;
    }

    private int minValue(State s, int alp, int bet){
        if(s.isTerminal() >= 0) return utility(s);
        int v = Integer.MAX_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = maxValue(c,alp,bet);
            v = res < v ? res : v;
            if(v <= alp) return v;
            bet = v < bet ? v : bet;
        }
        return v;
    }

    private int utility(State s){
        int res = s.isTerminal();
        int val = 0;
        switch (res) {
            case 0:
                val = 0;
                break;
            case 1:
                val = playerID == 1 ? 1 : -1;
                break;
            case 2:
                val = playerID == 2 ? 1 : -1;
                break;
        }
        //System.out.println(res + ", " + val);
        return val;
    }

    private boolean isFull(int column){
        return height[column] >= y;
    }

    private boolean allFull(){
        for(int i = 0; i < x; i++){
            System.out.println(i);
            if(!isFull(i)) return false;
        }
        return true;
    }

}
