public class Checking {

    public static int checkWinner(int[][] gb, int x, int y, int c, int r, int p){
        if(checkColumn(gb, y, c, p)         ||
           checkRow(gb, x, r, p)            ||
           checkDiagLeft(gb, x, y, c, r, p) ||
           checkDiagRight(gb, x, y, c, r, p)){
            return p;
        }
        //if(allFull()) return Winner.TIE;
        return -1;
    }

    private static boolean checkColumn(int[][] gameBoard, int y, int latestCol, int latestP){
        int tmp = 0;
        for(int i = 0; i < y; i++){
            if(gameBoard[latestCol][i] == latestP) {
                if(++tmp == 4) return true;
            }
            else tmp = 0;
        }
        return false;
    }

    private static boolean checkRow(int[][] gameBoard, int x, int latestRow, int latestP){
        int tmp = 0;
        for(int i = 0; i < x; i++){
            if(gameBoard[i][latestRow] == latestP) {
                if(++tmp == 4) return true;
            }
            else tmp = 0;
        }
        return false;
    }

    private static boolean checkDiagLeft(int[][] gameBoard, int x, int y, int latestCol, int latestRow, int latestP){
        int d = latestCol - latestRow;
        int tx = d < 0 ? 0 : d;
        int ty = d < 0 ? -d : 0;
        int tmp = 0;
        while(tx < x && ty < y) {
            if(gameBoard[tx][ty] == latestP) {
                if(++tmp == 4) return true;
            }
            else tmp = 0;
            tx++;
            ty++;
        }
        return false;
    }

    private static boolean checkDiagRight(int[][] gameBoard, int x, int y, int latestCol, int latestRow, int latestP){
        int d = latestCol + latestRow;
        int diff = d - x + 1;
        int tx = d >= x ? d - diff : d;
        int ty = d >= x ? diff : 0;
        int tmp = 0;
        while(tx < x && ty < y && tx >= 0) {
            if(gameBoard[tx][ty] == latestP) {
                if(++tmp == 4) return true;
            }
            else tmp = 0;
            tx--;
            ty++;
        }
        return false;
    }
}