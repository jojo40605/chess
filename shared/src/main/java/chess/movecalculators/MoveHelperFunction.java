package chess.movecalculators;

import chess.*;

import java.util.Collection;

public final class MoveHelperFunction {

    private MoveHelperFunction() {}

    public static boolean inBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public static boolean isEmpty(ChessBoard board, ChessPosition pos) {
        return board.getPiece(pos) == null;
    }

    public static boolean isEnemy(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        ChessPiece target = board.getPiece(pos);
        return target != null && target.getTeamColor() != piece.getTeamColor();
    }
    public static void addSlidingMoves(Collection<ChessMove> moves, ChessBoard board,
                                       ChessPosition from, ChessPiece piece, int[][] directions) {
        for (int[] dir : directions) {
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            while (inBounds(row, col)) {
                ChessPosition to = new ChessPosition(row, col);
                if (isEmpty(board, to)) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (isEnemy(board, to, piece)) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    break; // Blocked by piece
                }
                row += dir[0];
                col += dir[1];
            }
        }
    }

    /**
     * Calculates moves for "stepping" pieces (Knight, King).
     */
    public static void addSteppingMoves(Collection<ChessMove> moves, ChessBoard board,
                                        ChessPosition from, ChessPiece piece, int[][] directions) {
        for (int[] dir : directions) {
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            if (inBounds(row, col)) {
                ChessPosition to = new ChessPosition(row, col);
                if (isEmpty(board, to) || isEnemy(board, to, piece)) {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
    }
}