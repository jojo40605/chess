package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Manages a chess game, handling turns and move validation.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() { return teamTurn; }

    public void setTeamTurn(TeamColor team) { this.teamTurn = team; }

    /**
     * Gets valid moves for a piece, ensuring the move doesn't leave the king in check.
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) { return null; }

        Collection<ChessMove> rawMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : rawMoves) {
            ChessBoard sim = copyBoard(board);
            applyMove(sim, move);

            if (!isInCheckSim(sim, piece.getTeamColor())) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        validateMove(move, piece);

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        handlePromotion(move, piece);
        switchTurn();
    }

    private void validateMove(ChessMove move, ChessPiece piece) throws InvalidMoveException {
        if (piece == null) { throw new InvalidMoveException("No piece"); }
        if (piece.getTeamColor() != teamTurn) { throw new InvalidMoveException("Wrong turn"); }

        Collection<ChessMove> legal = validMoves(move.getStartPosition());
        if (legal == null || !legal.contains(move)) { throw new InvalidMoveException("Illegal move"); }
    }

    private void handlePromotion(ChessMove move, ChessPiece piece) {
        if (move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }
    }

    private void switchTurn() {
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckSim(board, teamColor);
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !hasAnyValidMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !hasAnyValidMoves(teamColor);
    }

    /**
     * Checks if a team has any legal moves left.
     * Consolidation of this logic fixes DuplicateBlock errors in checkmate/stalemate.
     */
    private boolean hasAnyValidMoves(TeamColor teamColor) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = board.getPiece(pos);

                if (p != null && p.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(pos);
                    if (moves != null && !moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInCheckSim(ChessBoard simBoard, TeamColor teamColor) {
        ChessPosition kingPos = findKing(simBoard, teamColor);
        if (kingPos == null) { return false; }

        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                if (canPieceAttackPosition(simBoard, pos, kingPos, teamColor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canPieceAttackPosition(ChessBoard b, ChessPosition piecePos, ChessPosition target, TeamColor ourColor) {
        ChessPiece p = b.getPiece(piecePos);
        if (p == null || p.getTeamColor() == ourColor) { return false; }

        for (ChessMove m : p.pieceMoves(b, piecePos)) {
            if (m.getEndPosition().equals(target)) { return true; }
        }
        return false;
    }

    private ChessPosition findKing(ChessBoard b, TeamColor team) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = b.getPiece(pos);
                if (p != null && p.getTeamColor() == team && p.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard copy = new ChessBoard();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = original.getPiece(pos);
                if (p != null) {
                    copy.addPiece(pos, new ChessPiece(p.getTeamColor(), p.getPieceType()));
                }
            }
        }
        return copy;
    }

    private void applyMove(ChessBoard b, ChessMove move) {
        ChessPiece p = b.getPiece(move.getStartPosition());
        b.addPiece(move.getEndPosition(), p);
        b.addPiece(move.getStartPosition(), null);
    }

    public void setBoard(ChessBoard board) { this.board = board; }
    public ChessBoard getBoard() { return board; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() { return Objects.hash(board, teamTurn); }

    public enum TeamColor { WHITE, BLACK }
}