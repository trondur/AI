
public class GameLogic implements IGameLogic {
    private int x = 0;
    private int y = 0;
    private int playerID;
    private int cut;

    private State state;
    
    public GameLogic() {
        //TODO Write your implementation for this method
    }
	//x = columns, y = rows
    public void initializeGame(int x, int y, int playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        this.state = new State(new int[x][y], new int[x], playerID);
        this.cut = 7;
    }
	
    public Winner gameFinished() {
        int res = state.isTerminal();
        return res < 0 ? Winner.NOT_FINISHED : res == 0 ? Winner.TIE : res == 1 ? Winner.PLAYER1 : Winner.PLAYER2;
    }

    public void insertCoin(int column, int playerID) {
        state.put(column, playerID);
    }

    public int decideNextMove() {
        int move = 0;
        if(state.allEmpty()) move = x/2;
        else move = alphaBeta(state);

        return move;
    }

    // private int miniMax(State s){
    //     //int[][] cgb = copy(gb);
    //     int best = -1;
    //     int max = -1000;
    //     for(int i = 0; i < x; i++){
    //         if(s.isFull(i)) continue;
    //         State c = s.clone();
    //         c.put(i);
    //         int res = minValue(c, Integer.MIN_VALUE, Integer.MAX_VALUE);
    //         System.out.println(res);
    //         best = res > max ? i : best;
    //         max = res > max ? res : max;
    //     }
    //     //System.out.println("--------------");
    //     return best;
    // }

    private int alphaBeta(State s){
        int best = -1;
        int max = -1000;
        int alp = Integer.MIN_VALUE;
        int bet = Integer.MAX_VALUE;
        System.out.println("start");
        if(s.isTerminal() >= 0) return eval(s);
        System.out.println("stop");
        int v = Integer.MIN_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, alp, bet, 1);
            //System.out.println(res);
            best = res > v ? i : best;
            v = res > v ? res : v;
            alp = v > alp ? v : alp;
        }
        return best;
    }

    private int maxValue(State s, int alp, int bet, int dep){
        System.out.println("Max: " + dep);
        if(cutoff(dep, s)) return eval(s);
        int v = Integer.MIN_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, alp, bet, ++dep);
            v = res > v ? res : v;
            if(v >= bet) return v;
            alp = v > alp ? v : alp;
        }
        return v;
    }

    private int minValue(State s, int alp, int bet, int dep){
        System.out.println("Min: " + dep);
        if(cutoff(dep, s)) return eval(s);
        int v = Integer.MAX_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = maxValue(c, alp, bet, ++dep);
            v = res < v ? res : v;
            if(v <= alp) return v;
            bet = v < bet ? v : bet;
        }
        return v;
    }

    private int eval(State s){
        int res = s.isTerminal();
        if(res >= 0) return utility(res);
        return s.ranking(4);
    }

    private boolean cutoff(int depth, State s){
        System.out.println("Cutoff: "+ depth + ", " + cut);
        return (depth >= cut) || (s.isTerminal() >= 0);
    }

    private int utility(int res){
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
}
