package TotalKnockoutChess.Chess.Pieces;

import TotalKnockoutChess.Chess.ChessGameTile;

public class King extends ChessPiece {
    private static final long serialVersionUID = 0L;

    private boolean checked, checkMated, canCastle;

    private Coordinate coordinate;

    public King(String color, Coordinate coordinate) {
        super(color);
        checked = false;
        checkMated = false;
        canCastle = true;
        this.coordinate = coordinate;
    }

    // Getter for the coordinate of the piece
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public String calculateAvailableMoves(ChessGameTile[][] board, Coordinate currentPosition) {
        String moves = "";

        for(Coordinate c : Coordinate.values()){
            moves += c.toString();
            System.out.println(c);
        }
        return moves;
    }

    @Override
    public final String toString() {
        return color.charAt(0) + "K";
    }


    // Getter/Setter for checked field
    public boolean isChecked(){ return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }


    // Getter/Setter for checkMated field
    public boolean isCheckMated(){ return checkMated; }
    public void setCheckMated(boolean checkMated){ this.checkMated = checkMated; }


    // Getter/Setter for canCastle field
    public boolean canCastle(){ return canCastle; }
    public void setCanCastle(boolean canCastle){ this.canCastle = canCastle; }
}
