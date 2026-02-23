package chess.movecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoveCalculator implements MoveCalculator {

    private static final int[][] DIRECTIONS = {
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
                    addRookMove(moves, from, to);
                } else {
                    if (MoveHelperFunction.isEnemy(board, to, piece)) {
                        addRookMove(moves, from, to);
                    }
                    break;
                }
            }
        }

        return moves;
    }

    private void addRookMove(
            Collection<ChessMove> moves,
            ChessPosition from,
            ChessPosition to
    ) {
        moves.add(new ChessMove(from, to, null));
    }
}