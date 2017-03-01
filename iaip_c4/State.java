
public class State {
    private int[][] gb;
    private int[] h;

    private int turn;

    private int x, y;
    private int p, op;
    private int latestCol, latestRow, latestP;

    public State(int[][] gb, int[] h, int p){
        this.x = gb.length;
        this.y = gb[0].length;
        this.gb = gb;
        this.h = h;
        this.p = p;
        this.op = p == 1 ? 2 : 1;
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

    public int ranking(){
        int points = 0;
        for(int r = 0; r < y; r++){
            for(int c = 0; c < x; c++){
                if(y-r < 4 && x-c < 4) continue;
                points += score(c, r);
            }
        }
        return points;
    }

    public boolean checkScene(){
        return h[x/2-1] == 1 && isFull(x/2) && h[x/2+1] == 1;
    }

    public int score(int c, int r){
        int points = 0;
        boolean blocked = false;
        boolean eblocked = false;
        int tmp = 0;
        int space = 0;
        if(x-c >= 4){
            int max = c <= x-4 ? c+3 : x-1;
            for(int i = c; i <= max; i++){
                space = gb[i][r];
                if(space == p && !blocked) {
                    eblocked = true;
                    tmp++;
                }
                else if(space == op && !eblocked){
                    blocked = true;
                    tmp--;
                }
            }
            tmp = tmp < 0 ? tmp*tmp*-1 : tmp*tmp;
            if(!blocked || !eblocked) points += tmp;
        }
        if(y-r >= 4){
            blocked = false;
            eblocked = false;
            tmp = 0;
            int max = r <= y-4 ? r+3 : y-1;
            for(int i = r; i <= max; i++){
                space = gb[c][i];
                if(space == p && !blocked) {
                    eblocked = true;
                    tmp++;
                }
                else if(space == op && !eblocked){
                    blocked = true;
                    tmp--;
                }else break;
            }
            tmp = tmp < 0 ? tmp*tmp*-1 : tmp*tmp;
            if(!blocked || !eblocked) points += tmp; 
        }
        if(x-c >= 4 && y-r >= 4){
            blocked = false;
            eblocked = false;
            tmp = 0;
            for(int i = 0; i < 4; i++){
                space = gb[c+i][r+i];
                if(space == p && !blocked) {
                    eblocked = true;
                    tmp++;
                }
                else if(space == op && !eblocked){
                    blocked = true;
                    tmp--;
                }
            }
            tmp = tmp < 0 ? tmp*tmp*-1 : tmp*tmp;
            if(!blocked || !eblocked) points += tmp;
        }

        if(x-c >= 4 && y-r < 4){
            blocked = false;
            eblocked = false;
            tmp = 0;
            for(int i = 0; i < 4; i++){
                space = gb[c+i][r-i]; 
                if(gb[c+i][r-i] == p && !blocked) {
                    eblocked = true;
                    tmp++;
                }
                else if(gb[c+i][r-i] == op && !eblocked){
                    blocked = true;
                    tmp--;
                }
            }
            tmp = tmp < 0 ? tmp*tmp*-1 : tmp*tmp;
            if(!blocked || !eblocked) points += tmp;
        }
        
        return points;
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

    private int score(int fst, int snd, int thd, int fth){
        int tmp = 0;
        tmp = fst == latestP ? tmp+1 : fst == 0 ? tmp : tmp-1;
        tmp = snd == latestP ? tmp+1 : snd == 0 ? tmp : tmp-1;
        tmp = thd == latestP ? tmp+1 : thd == 0 ? tmp : tmp-1;
        tmp = fth == latestP ? tmp+1 : fth == 0 ? tmp : tmp-1;
        return tmp;
    }

    private int scoreRow(){
        int points = 0;
        for(int j = 0; j < y; j++){
            for(int i = 0; i < x-3; i++){
                points += score(gb[i][j], gb[i+1][j], gb[i+2][j], gb[i+3][j]);
            }
        }
        return points;
    }

    private int scoreColumn(){
        int points = 0;
        for(int j = 0; j < x; j++){
            for(int i = 0; i < y-3; i++){
                points += score(gb[j][i], gb[j][i+1], gb[j][i+2], gb[j][i+3]);
            }
        }
        return points;
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
