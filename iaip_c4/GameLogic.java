
public class GameLogic implements IGameLogic {
    private int x = 0;
    private int y = 0;
    private int playerID;
    private int cut;
    private int[] columnOrder;

    private State state;
    
    public GameLogic() {
        //TODO Write your implementation for this method
    }
	//x = columns, y = rows
    public void initializeGame(int x, int y, int playerID) {
        this.x = x;
        this.y = y;
        this.playerID = playerID;
        this.columnOrder = new int[x];
        for(int i = 0; i < x; i++){
            columnOrder[i] = x/2 + (1-2*(i%2))*(i+1)/2;
        }
        this.state = new State(new int[x][y], new int[x], playerID);
        this.cut = 12;
    }
	
    public Winner gameFinished() {
        int res = state.isTerminal();
        return res < 0 ? Winner.NOT_FINISHED : res == 0 ? Winner.TIE : res == 1 ? Winner.PLAYER1 : Winner.PLAYER2;
    }

    public void insertCoin(int column, int playerID) {
        state.put(column, playerID);
    }

    public int decideNextMove() {
    	if(state.getTurn() < 6){
    		return startMoves();
    	}
    	if(state.getH()[3] >= 5) cut++;
        int move = 0;
        if(state.allEmpty()) move = x/2;
        else {
            long startTime = System.nanoTime();
            move = alphaBeta(state);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/1000000;
            System.out.println(duration);
        }
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
        int mid = x/2;
        System.out.println("start");
        //if(s.checkScene()) return (x/2+1);
        if(s.isTerminal() >= 0) return eval(s, 0);
        System.out.println("stop");
        int v = Integer.MIN_VALUE;
        for(int j = 0; j < x; j++){
            int i = columnOrder[j];
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, alp, bet, 1);
            System.out.println(res + ", " + i);
            if(res == v && Math.abs(mid-i) < Math.abs(mid-best)){
                best = i;
            }else{
                best = res > v ? i : best;
                v = res > v ? res : v;
                alp = v > alp ? v : alp;
            }
        }
        return best;
    }

    private int maxValue(State s, int alp, int bet, int dep){
        if(cutoff(dep, s)) return eval(s, dep);
        int v = Integer.MIN_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = minValue(c, alp, bet, dep+1);
            v = res > v ? res : v;
            if(v >= bet) return v;
            alp = v > alp ? v : alp;
        }
        return v;
    }

    private int minValue(State s, int alp, int bet, int dep){
        if(cutoff(dep, s)) return eval(s, dep);
        int v = Integer.MAX_VALUE;
        for(int i = 0; i < x; i++){
            if(s.isFull(i)) continue;
            State c = s.clone();
            c.put(i);
            int res = maxValue(c, alp, bet, dep+1);
            v = res < v ? res : v;
            if(v <= alp) return v;
            bet = v < bet ? v : bet;
        }
        return v;
    }

    private int eval(State s, int dep){
        int res = s.isTerminal();
        if(res >= 0) return utility(res, dep);
        return s.ranking();
    }

    private boolean cutoff(int depth, State s){
        return (depth >= cut) || (s.isTerminal() >= 0);
    }

    private int utility(int res, int dep){
        int val = 0;
        switch (res) {
            case 0:
                val = 0;
                break;
            case 1:
                val = playerID == 1 ? 1000000000 : -100000000/dep;
                break;
            case 2:
                val = playerID == 2 ? 1000000000 : -100000000/dep;
                break;
        }
        //System.out.println(res + ", " + val);
        return val;
    }
    private boolean isFull(int column){
        return state.getH()[column] >= y;
    }

    private boolean allFull(){
        for(int i = 0; i < x; i++){
            System.out.println(i);
            if(!isFull(i)) return false;
        }
        return true;
    }
    private int startMoves(){
    	if(state.getH()[3] == state.getTurn() && !isFull(3)) return 3;
    	switch((state.getTurn())){ 		
    		case 0:
    			return 3;
    		case 2:
    			if(state.getH()[1] == 1) return 1;
    			if(state.getH()[5] == 1) return 5;
    		case 1:
    			if(state.getH()[1] == 1) return 2; 
    			if(state.getH()[5] == 1) return 4;
    		case 3:
    			if(state.getH()[1] == 1) return 2;
    			if(state.getH()[2] == 1) return 1;
    			if(state.getH()[5] == 1) return 4;
    			if(state.getH()[4] == 1) return 5;
    		default:
    			return 3;
    	}
    }
}
