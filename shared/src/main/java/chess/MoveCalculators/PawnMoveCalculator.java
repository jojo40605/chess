package chess.MoveCalculators;

import chess.*;
import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator implements MoveCalculator {

    private static final ChessPiece.PieceType[] PROMOTION_PIECES = {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT
    };

    @Override
    public Collection<ChessMove> calculateMoves(
            ChessBoard board,
            ChessPosition from,
            ChessPiece piece
    ) {
        Collection<ChessMove> moves = new ArrayList<>();
        int row = from.getRow();
        int col = from.getColumn();

        // Determine forward direction nand promotion row based on team color
        int dir = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
        int promotionRow = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;

        // 1. Forward one square
        int forwardRow = row + dir;
        if (MoveHelperFunction.inBounds(forwardRow, col) && MoveHelperFunction.isEmpty(board, new ChessPosition(forwardRow, col))) {
            addPawnMove(moves, from, forwardRow, col, promotionRow);

            // 2. Forward two squares from starting rank
            boolean onStartingRank = (piece.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2)
                    || (piece.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7);

            int doubleForwardRow = row + 2 * dir;
            if (onStartingRank &&
                    MoveHelperFunction.inBounds(doubleForwardRow, col) &&
                    MoveHelperFunction.isEmpty(board, new ChessPosition(doubleForwardRow, col))) {
                moves.add(new ChessMove(from, new ChessPosition(doubleForwardRow, col), null));
            }
        }

        // 3. Capture diagonally
        int[][] diagonals = {{dir, 1}, {dir, -1}};
        for (int[] d : diagonals) {
            int r = row + d[0];
            int c = col + d[1];
            ChessPosition target = new ChessPosition(r, c);
            if (MoveHelperFunction.inBounds(r, c) && MoveHelperFunction.isEnemy(board, target, piece)) {
                addPawnMove(moves, from, r, c, promotionRow);
            }
        }

        return moves;
    }

    private void addPawnMove(
            Collection<ChessMove> moves,
            ChessPosition from,
            int row,
            int col,
            int promotionRow
    ) {
        ChessPosition to = new ChessPosition(row, col);

        if (row == promotionRow) {
            // Generate one move per promotion type
            for (ChessPiece.PieceType promotion : PROMOTION_PIECES) {
                moves.add(new ChessMove(from, to, promotion));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
