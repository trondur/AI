import java.lang.*;
import java.util.HashMap;

public class GameBoard {
    public enum Winner {
        NONE, BLUE, RED, TIE
    }

    private final static int MAX_TIME = 750;

    private final int timeBufferInitial = 10000;
    private final int timeBufferIncrement = 500;
    private int timeBuffer = timeBufferInitial;

    private final int width, height;
    private final int maxMoves;

    private final HashMap<Long, Integer>[] startMoves;

    private byte state[][];

    private long playerBoard;
    private long opponentBoard;
    private long emptyBoard;
    private Masks masks;

    private final byte player;
    private final byte opponent;

    private Winner winner = Winner.NONE;
    private int[] columnHeights;
    private int moves;

    private int[] bestColumns = { 3, 2, 4, 1, 5, 0, 6 };

    private Stopwatch stopwatch;

    private long searchCount = 0;
    private Statistics statistics;

    public GameBoard(int width, int height, int player) {
        this.width = width;
        this.height = height;
        this.maxMoves = width * height;

        this.state = new byte[width][height];

        this.masks = new Masks(width, height);

        this.player = player == 1 ? Coin.BLUE : Coin.RED;
        this.opponent = player == 2 ? Coin.BLUE : Coin.RED;

        this.state = new byte[width][height];
        this.columnHeights = new int[width];

        this.stopwatch = new Stopwatch();

        if (width == 7) {
            startMoves = new HashMap[2];

            startMoves[0] = new HashMap<>();
            startMoves[0].put(0L, 3);

            startMoves[1] = new HashMap<>();
            startMoves[1].put(masks.getCoinMask(0, 0), 3);
            startMoves[1].put(masks.getCoinMask(1, 0), 2);
            startMoves[1].put(masks.getCoinMask(2, 0), 3);
            startMoves[1].put(masks.getCoinMask(3, 0), 3);
            startMoves[1].put(masks.getCoinMask(4, 0), 3);
            startMoves[1].put(masks.getCoinMask(5, 0), 4);
            startMoves[1].put(masks.getCoinMask(6, 0), 3);
        }
        else startMoves = null;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int row = height - 1; row >= 0; row--) {
            for (int column = 0; column < width; column++) {
                byte coin = state[column][row];
                char symbol = ' ';

                if (coin == Coin.BLUE)
                    symbol = 'B';

                else if (coin == Coin.RED)
                    symbol = 'R';

                builder.append(symbol);

                if (column < width)
                    builder.append(' ');
            }

            if (row != 0)
                builder.append('\n');
        }

        return builder.toString();
    }

    public Winner getWinner() {
        return winner;
    }

    public void insertCoin(int column, int player) {
        byte coin = player == 1 ? Coin.BLUE : Coin.RED;
        addCoin(column, coin);
    }

    public int decideNextMove() {
        timeBuffer = Math.min(timeBufferInitial, timeBuffer + timeBufferIncrement);

        // Statistics
        statistics = new Statistics();
        stopwatch.reset();
        searchCount = 0;

        long elapsed = 0;
        int depth = 0;

        int bestColumn = -1;
        float bestResult = Float.NEGATIVE_INFINITY;

        // Use hard coded starter moves if available
        if (moves < startMoves.length && startMoves != null) {
            return startMoves[moves].get(opponentBoard);
        }

        while(depth < maxMoves - moves) {
            bestResult = Float.NEGATIVE_INFINITY;
//            int[] newBestColumns = new int[width];

//            System.out.println("\nDepth " + depth);

            for (int column = 0; column < width; column++) {
                if (columnHeights[column] == height)
                    continue;

                // TODO: If any column is Infinity on depth 0, take it.
                // TODO: Otherwise if any column is -Infinity on depth 0, take it.

                // Apply move to board
                addCoin(column, player);

                // Evaluate maximum
                float result = evaluate(false, depth);
                if (result >= bestResult) {
                    bestResult = result;
                    bestColumn = column;
                }

//                System.out.println("Column = " + column + ", Value = " + result);

                // Undo move
                removeCoin(column);
            }

            long delta = stopwatch.elapsed() - elapsed;
            long left = MAX_TIME - elapsed;
            elapsed = stopwatch.elapsed();

            if((float)delta * Math.pow(1.08, depth + 1 - (moves * 0.2)) >
                    left - (timeBufferInitial * 0.75 - timeBuffer) / (maxMoves - moves))
                break;

            depth++;
        }

        if(bestResult == Float.NEGATIVE_INFINITY)
            System.out.println("No non-losing move available");

        // Statistics
        statistics.column       = bestColumn;
        statistics.depth        = depth;
        statistics.elapsed      = stopwatch.elapsed();
        statistics.mnodes       = searchCount / 1000000f;
        statistics.mnodesPerSec = (float)searchCount / ((float)stopwatch.elapsed() / 1000f) / 1000000f;
//        System.out.println(statistics);

        timeBuffer -= elapsed;
        return bestColumn;
    }

    private float evaluate(boolean maximize, float alpha, float beta, int depthLeft) {
        searchCount++;

        if (depthLeft <= 0 || isTerminal()) {
            if (winner == Coin.toWinner(player))
                return Float.POSITIVE_INFINITY;

            if (winner == Coin.toWinner(opponent))
                return Float.NEGATIVE_INFINITY;

            if (winner == Winner.NONE)
                return heuristic();

            return 0;
        }

        float best = maximize ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;

        for (int i = 0; i < width; i++) {
            int column = bestColumns[i];

            // TODO: Order evaluations after best columns from last whole iteration

            if (columnHeights[column] == height)
                continue;

            // Apply move to board
            addCoin(column, maximize ? player : opponent);

            // Evaluate child node
            float result = evaluate(!maximize, alpha, beta, depthLeft - 1);
            best = maximize ? Math.max(result, best) : Math.min(result, best);

            // Alpha-beta pruning
            boolean prune = false;
            if (maximize) {
                if (best >= beta)
                    prune = true;

                alpha = Math.max(alpha, best);
            } else {
                if (best <= alpha)
                    prune = true;

                beta = Math.min(beta, best);
            }

            // Undo move
            removeCoin(column);

            if (prune) {
                statistics.prunes[depthLeft + 1]++;
                return best;
            }
        }

        return best;
    }

    private float heuristic() {
        int goodValue = 0;
        int badValue = 0;

        long goodBoard = playerBoard | emptyBoard;
        long badBoard =  opponentBoard | emptyBoard;

        for (int i = 0; i < maxMoves; i++) {
            int row = (i / width);
            int value = height - row;

            if (Masks.intersectsMask(goodBoard, masks.won.N [i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.NE[i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.E [i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.SE[i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.S [i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.SW[i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.W [i])) goodValue += value;
            if (Masks.intersectsMask(goodBoard, masks.won.NW[i])) goodValue += value;

            if (Masks.intersectsMask(badBoard, masks.won.N [i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.NE[i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.E [i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.SE[i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.S [i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.SW[i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.W [i])) badValue += value;
            if (Masks.intersectsMask(badBoard, masks.won.NW[i])) badValue += value;
        }

        return ((float)goodValue * 0.75f) - (float)badValue; // 0.5
    }

    private float evaluate(boolean maximize, int depthLeft) {
        return evaluate(maximize, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, depthLeft);
    }

    private void addCoin(int column, byte coin) {
        int row = columnHeights[column]++;
        state[column][row] = coin;

        if (coin == player) playerBoard |= masks.getCoinMask(column, row);
        else if (coin == opponent) opponentBoard |= masks.getCoinMask(column, row);
        emptyBoard = ~(playerBoard | opponentBoard);

        moves++;
        winner = checkWinner(column, coin);
    }

    private void removeCoin(int column) {
        int row = --columnHeights[column];
        state[column][row] = Coin.NONE;

        long coinMask = masks.getCoinMask(column, row);
        playerBoard   |= coinMask;
        playerBoard   ^= coinMask;
        opponentBoard |= coinMask;
        opponentBoard ^= coinMask;
        emptyBoard = ~(playerBoard | opponentBoard);

        moves--;
        winner = Winner.NONE;
    }

    private boolean isTerminal() {
        return winner != Winner.NONE;
    }

    private Winner checkWinner(int column, byte coin) {
        if (checkLine(column, coin))
            return Coin.toWinner(coin);

        // TODO: Remove when confident
        if (moves > maxMoves)
            throw new RuntimeException();

        if (moves == maxMoves)
            return Winner.TIE;

        return Winner.NONE;
    }

    private int checkLineLength(int right, int up, int column, int row, byte coin) {
        int count = 0;

        column += right;
        row += up;

        while(column >= 0 && column < width && row >= 0 && row < height) {
            if(state[column][row] == coin) count++;
            else break;

            column += right;
            row += up;
        }

        return count;
    }

    private boolean checkLine(int column, byte coin) {
        int row = columnHeights[column] - 1;

        if (checkLineLength(-1, 1, column, row, coin) + checkLineLength(1,  -1, column, row, coin) >= 3 || // NW + SE
                checkLineLength(1, 1, column, row, coin)  + checkLineLength(-1, -1, column, row, coin) >= 3 || // NE + SW
                checkLineLength(-1, 0, column, row, coin)  + checkLineLength(1,  0, column, row, coin) >= 3 || // W + E
                checkLineLength(0, -1, column, row, coin) >= 3) // S
            return true;

        return false;
    }

    public class Statistics {
        public int column;
        public int depth;

        public long elapsed;

        public float mnodes;
        public float mnodesPerSec;

        public int[] prunes = new int[maxMoves];

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("Statistics for move\n");
            sb.append("  column     = " + column                               + "\n");
            sb.append("  depth      = " + depth                                + "\n");
            sb.append("  elapsed    = " + elapsed                              + " ms\n");
            sb.append("  total      = " + String.format("%.2f", mnodes)        + " Mnodes\n");
            sb.append("  speed      = " + String.format("%.2f", mnodesPerSec)  + " Mnodes/sec\n");

            sb.append("\n");
            sb.append("  Pruning:\n");

            sb.append("  DEPTH        ");
            for (int i = 0; i < 10 && i < prunes.length; i++) {
                sb.append(String.format("%02d   ", i));
            }
            sb.append("\n");

            sb.append("  PRUNES(K)    ");
            for (int i = 0; i < 10 && i < prunes.length; i++) {
                sb.append(String.format("%04d ", prunes[i]/1000));
            }
            sb.append("\n");

            return sb.toString();

//            sb.append("  cache size  = " + cache.size() / 1000 + " k");
//            sb.append("  cache ratio = " + String.format("%.1f", ((float)cacheHits / (float)searchCount)) + " %");
        }
    }

    private static class Coin {
        public static final byte NONE = 0;
        public static final byte BLUE = 1;
        public static final byte RED = 2;

        private static Winner toWinner(byte coin) {
            switch (coin) {
                case Coin.BLUE:
                    return Winner.BLUE;
                case Coin.RED:
                    return Winner.RED;
                default:
                    throw new RuntimeException();
            }
        }
    }

    private class BoardState {
        public byte state[][];

        // TODO: Use for transposition table

        public BoardState(int width, int height) {
            state = new byte[width][height];
        }

        @Override
        public int hashCode() {
            int hash = 5;
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    // Mirror
                    hash = hash * 7 + state[column][row] + state[width - column - 1][row];
                }
            }

            return hash;
        }
    }
}