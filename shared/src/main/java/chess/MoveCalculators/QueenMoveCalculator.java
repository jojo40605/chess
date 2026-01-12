package chess.MoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoveCalculator implements MoveCalculator {

    private static final int[][] DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {0, 1}, {-1, 0}, {0, -1}
    };

    @Override
    public Collection<ChessMove> calculateMoves(
            ChessBoard board,
            ChessPosition from,
            ChessPiece piece
    ) {
        Collection<ChessMove> moves = new ArrayList<>();

        for (int[] dir : DIRECTIONS) {
            int row = from.getRow();
            int col = from.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];

                if (!MoveHelperFunction.inBounds(row, col)) {
                    break;
                }

                ChessPosition to = new ChessPosition(row, col);

                if (MoveHelperFunction.isEmpty(board, to)) {
                    addQueenMove(moves, from, to);
                } else {
                    if (MoveHelperFunction.isEnemy(board, to, piece)) {
                        addQueenMove(moves, from, to);
                    }
                    break; // queen is blocked
                }
            }
        }

        return moves;
    }

    private void addQueenMove(
            Collection<ChessMove> moves,
            ChessPosition from,
            ChessPosition to
    ) {
        moves.add(new ChessMove(from, to, null));
    }
}