package com.example.thomas.game2048;

import java.io.Serializable;

public class SaveFile implements Serializable {

    private Square[][] gameBoard;
    private int score;
    private int bestScore;
    private int bestTile;

    private boolean lost;
    private boolean won;
    private boolean keepPlaying;

    public SaveFile(Square[][] gameBoard, int score, int bestScore, int bestTile, boolean lost, boolean won, boolean keepPlaying){
        this.gameBoard = gameBoard;
        this.score = score;
        this.bestScore = bestScore;
        this.bestTile = bestTile;
        this.lost = lost;
        this.won = won;
        this.keepPlaying = keepPlaying;
    }

    public int getBestScore() {
        return bestScore;
    }

    public int getScore() {
        return score;
    }

    public Square[][] getGameBoard() {
        return gameBoard;
    }

    public int getBestTile() {
        return bestTile;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isKeepPlaying() {
        return keepPlaying;
    }

    public boolean isLost() {
        return lost;
    }
}

