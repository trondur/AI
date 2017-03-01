
public class State {
    private int[][] gb;
    private int[] h;

    private int turn;

    private int x, y;
    private int p;
    private int latestCol, latestRow, latestP;

    public State(int[][] gb, int[] h, int p){
        this.x = gb.length;
        this.y = gb[0].length;
        this.gb = gb;
        this.h = h;
        this.p = p;
        this.latestP = p;
    }
    public int[] getH(){
        return h;
    }
    public int getTurn(){
        return turn;
    }

    public void put(int a, int player){
        p = player;
        put(a);
        turn++;
    }

    public void put(int a){
        gb[a][h[a]] = p;
        latestCol = a;
        latestRow = h[a];
        latestP = p;
        h[a] += 1;
        p = p == 1 ? 2 : 1;
    }

    public State clone(){
        return new State(copy(gb), copy(h), p);
    }

    private int[][] copy(int[][] board){
        int[][] arr = new int[board.length][board[0].length];
        for(int i = 0; i < board.length; i++ ){
            for(int j = 0; j < board[0].length; j++){
                arr[i][j] = board[i][j];
            }
        }
        return arr;
    }

    private int[] copy(int[] board){
        int[] arr = new int[board.length];
        for(int i = 0; i < board.length; i++ ){
            arr[i] = board[i];
        }
        return arr;
    }

    public int ranking(int chainSize){
        if(checkColumn(chainSize) || checkRow(chainSize) || checkDiagLeft(chainSize) || checkDiagRight(chainSize)){
            return 1;
        }
        return 0;
    }

    public int isTerminal(){
        if(checkColumn(4) || checkRow(4) || checkDiagLeft(4) || checkDiagRight(4)){
            return latestP;
        }
        if(allFull()) return 0;
        return -1;
    }

    public boolean isFull(int column){
        return h[column] >= y;
    }

    public boolean isEmpty(int column){
        return h[column] == 0;
    }

    public boolean allFull(){
        for(int i = 0; i < x; i++){
            if(!isFull(i)) return false;
        }
        return true;
    }

    public boolean allEmpty(){
        for(int i = 0; i < x; i++){
            if(!isEmpty(i)) return false;
        }
        return true;
    }

    private boolean checkColumn(int chainSize){
        int tmp = 0;
        for(int i = 0; i < y; i++){
            if(gb[latestCol][i] == latestP) {
                if(++tmp == chainSize) return true;
            }
            else tmp = 0;
        }
        return false;
    }

    private boolean checkRow(int chainSize){
        int tmp = 0;
        for(int i = 0; i < x; i++){
            if(gb[i][latestRow] == latestP) {
                if(++tmp == chainSize) return true;
            }
            else tmp = 0;
        }
        return false;
    }

    private boolean checkDiagLeft(int chainSize){
        int d = latestCol - latestRow;
        int tx = d < 0 ? 0 : d;
        int ty = d < 0 ? -d : 0;
        int tmp = 0;
        while(tx < x && ty < y) {
            if(gb[tx][ty] == latestP) {
                if(++tmp == chainSize) return true;
            }
            else tmp = 0;
            tx++;
            ty++;
        }
        return false;
    }

    private boolean checkDiagRight(int chainSize){
        int d = latestCol + latestRow;
        int diff = d - x + 1;
        int tx = d >= x ? d - diff : d;
        int ty = d >= x ? diff : 0;
        int tmp = 0;
        while(tx < x && ty < y && tx >= 0) {
            if(gb[tx][ty] == latestP) {
                if(++tmp == chainSize) return true;
            }
            else tmp = 0;
            tx--;
            ty++;
        }
        return false;
    }
}