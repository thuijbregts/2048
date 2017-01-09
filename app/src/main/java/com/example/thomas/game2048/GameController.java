package com.example.thomas.game2048;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;


public class GameController implements Runnable{

    public final Thread t = new Thread(this);

    final private Game game;


    private Square[][] gameBoard;

    private int previousScore;
    private int score;
    private int bestScore;
    private int bestTile;

    private boolean lost;
    private boolean won;
    private boolean keepPlaying;

    private boolean animationNeeded;

    private Button buttonContinue;
    private SoundPool soundPool;
    private int soundMerge;
    private int soundLose;
    private int soundWin;
    private boolean dataLoaded;

    public GameController(Game game){
        this.game = game;

        AudioAttributes attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        SoundPool.Builder builder =  new SoundPool.Builder();
        builder.setMaxStreams(25);
        soundPool = builder.setAudioAttributes(attributes).build();
        soundMerge = soundPool.load(game, R.raw.merge, 1);
        soundLose = soundPool.load(game, R.raw.over, 1);
        soundWin = soundPool.load(game, R.raw.win, 1);

        buttonContinue = (Button) game.getView().findViewById(R.id.buttonContinue);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundPool.play(soundMerge, 1, 1, 0, 0, 1);
                continueGame();
                buttonContinue.setEnabled(false);
                buttonContinue.setVisibility(View.GONE);
                GameController.this.game.getGameCanvas().setLabelLostToInvisible();
            }
        });
        Button buttonRetry = (Button) game.findViewById(R.id.buttonRetry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
                soundPool.play(soundMerge, 1, 1, 0, 0, 1);
                buttonContinue.setEnabled(false);
                buttonContinue.setVisibility(View.GONE);
                getGame().getGameCanvas().setLabelLostToInvisible();
            }
        });

        Button buttonReset = (Button) game.findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialog dialog = new CustomDialog();
                dialog.show(GameController.this.game.getFragmentManager(), null);
                GameController.this.game.getFragmentManager().executePendingTransactions();
            }
        });
    }

    public void restart(){
        init();
        game.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                game.getGameCanvas().updateScores();
                game.getGameCanvas().invalidate();
            }
        });
        synchronized(t){
            t.notify();
        }
    }

    public void continueGame(){
        keepPlaying = true;
        synchronized(t){
            t.notify();
        }
    }

    @Override
    public void run() {
        if(!loadData()) {
            init();
        }
        else{
            resetSquareBooleans();
            previousScore = 0;
            game.getGameCanvas().setAnimationUnlocked(true);
            game.getGameCanvas().setLabelLostToInvisible();
            animationNeeded = false;

            dataLoaded = true;
        }
        game.getGameCanvas().updateScores();
        do {
            while (!lost && (!won || keepPlaying)) {
                if (!dataLoaded) {
                    generateSquare();
                }
                dataLoaded = false;
                saveData();
                if (!canMove()) {
                    lost = true;
                    soundPool.play(soundLose, 1, 1, 0, 0, 1);
                    game.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            game.getGameCanvas().invalidate();
                        }
                    });
                    break;
                }
                game.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        game.getGameCanvas().invalidate();
                    }
                });
                synchronized (t) {
                    try {
                        t.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (score > bestScore)
                    bestScore = score;
                if (!keepPlaying && hasWon()) {
                    soundPool.play(soundWin, 1, 1, 0, 0, 1);
                    won = true;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                game.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        game.getGameCanvas().updateScores();

                        if (previousScore < score) {
                            game.getGameCanvas().animateScoreAddition(score - previousScore);
                            previousScore = score;
                        }
                        if (animationNeeded) {
                            try {
                                game.getGameCanvas().invalidate();
                                soundPool.play(soundMerge, 1, 1, 0, 0, 1);
                                Thread.sleep(150);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } finally {
                                resetSquareBooleans();
                            }
                        }
                        game.getGameCanvas().invalidate();

                    }
                });
            }

            synchronized (t) {
                try {
                    t.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (true);
    }

    public void init(){

        gameBoard = new Square[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                gameBoard[i][j] = new Square(0, i, j);
            }
        }
        score = 0;
        lost = false;
        won = false;
        keepPlaying = false;
        previousScore = 0;
        dataLoaded = false;

        game.getGameCanvas().setAnimationUnlocked(true);
        game.getGameCanvas().setLabelLostToInvisible();
        animationNeeded = false;

        generateSquare();
    }

    public void generateSquare(){
        Square[] empty = new Square[16];
        int emptySquaresCount = 0;
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(gameBoard[i][j].isEmpty()){
                    empty[emptySquaresCount] = gameBoard[i][j];
                    emptySquaresCount++;
                }
            }
        }
        int index = (int) Math.floor(Math.random()*emptySquaresCount);
        Square s = empty[index];
        int rng = (int) Math.ceil(Math.random()*15);
        s.setValue(rng < 15 ? 2 : 4);
    }

    public boolean hasRoom(){
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(gameBoard[i][j].isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasWon(){
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(gameBoard[i][j].getValue() == 2048)
                    return true;
            }
        }
        return false;
    }

    public boolean canMove(){
        if(hasRoom()){
            return true;
        }
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(canMerge(gameBoard[i][j]))
                    return true;
            }
        }
        return false;
    }


    public void moveLeft(){
        for(int i = 0; i < 4; i++){
            for(int j = 1; j < 4; j++){
                if(!gameBoard[i][j].isEmpty()){
                    int index = j-1;
                    while(gameBoard[i][index].isEmpty() && index > 0){
                        index--;
                    }
                    if(gameBoard[i][index].isEmpty()){
                        gameBoard[i][index].setValue(gameBoard[i][j].getValue());
                        gameBoard[i][j].setValue(0);
                    }
                    else if(gameBoard[i][index].getValue() == gameBoard[i][j].getValue() && gameBoard[i][index].isCanMerge()){
                        mergeSquares(gameBoard[i][index], gameBoard[i][j]);
                    }
                    else if(index+1 != j){
                        gameBoard[i][index+1].setValue(gameBoard[i][j].getValue());
                        gameBoard[i][j].setValue(0);
                    }
                }
            }
        }
    }

    public void moveRight(){
        for(int i = 0; i < 4; i++){
            for(int j = 2; j >= 0; j--){
                if(!gameBoard[i][j].isEmpty()){
                    int index = j+1;
                    while(gameBoard[i][index].isEmpty() && index < 3){
                        index++;
                    }
                    if(gameBoard[i][index].isEmpty()){
                        gameBoard[i][index].setValue(gameBoard[i][j].getValue());
                        gameBoard[i][j].setValue(0);
                    }
                    else if(gameBoard[i][index].getValue() == gameBoard[i][j].getValue() && gameBoard[i][index].isCanMerge()){
                        mergeSquares(gameBoard[i][index], gameBoard[i][j]);
                    }
                    else if(index-1 != j){
                        gameBoard[i][index-1].setValue(gameBoard[i][j].getValue());
                        gameBoard[i][j].setValue(0);
                    }
                }
            }
        }
    }

    public void moveUp(){
        for(int i = 0; i < 4; i++){
            for(int j = 1; j < 4; j++){
                if(!gameBoard[j][i].isEmpty()){
                    int index = j-1;
                    while(gameBoard[index][i].isEmpty() && index > 0){
                        index--;
                    }
                    if(gameBoard[index][i].isEmpty()){
                        gameBoard[index][i].setValue(gameBoard[j][i].getValue());
                        gameBoard[j][i].setValue(0);
                    }
                    else if(gameBoard[index][i].getValue() == gameBoard[j][i].getValue() && gameBoard[index][i].isCanMerge()){
                        mergeSquares(gameBoard[index][i], gameBoard[j][i]);
                    }
                    else if(index+1 != j){
                        gameBoard[index+1][i].setValue(gameBoard[j][i].getValue());
                        gameBoard[j][i].setValue(0);
                    }
                }
            }
        }
    }

    public void moveDown(){
        for(int i = 0; i < 4; i++){
            for(int j = 2; j >= 0; j--){
                if(!gameBoard[j][i].isEmpty()){
                    int index = j+1;
                    while(gameBoard[index][i].isEmpty() && index < 3){
                        index++;
                    }
                    if(gameBoard[index][i].isEmpty()){
                        gameBoard[index][i].setValue(gameBoard[j][i].getValue());
                        gameBoard[j][i].setValue(0);
                    }
                    else if(gameBoard[index][i].getValue() == gameBoard[j][i].getValue() && gameBoard[index][i].isCanMerge()){
                        mergeSquares(gameBoard[index][i], gameBoard[j][i]);
                    }
                    else if(index-1 != j){
                        gameBoard[index-1][i].setValue(gameBoard[j][i].getValue());
                        gameBoard[j][i].setValue(0);
                    }
                }
            }
        }
    }

    public boolean canMoveUp(){
        for(int i = 0; i < 4; i++){
            for(int j = 1; j < 4; j++){
                if(!gameBoard[j][i].isEmpty() && gameBoard[j][i].getValue() == gameBoard[j-1][i].getValue())
                    return true;
            }
        }
        boolean fullSquareFound;
        for(int i = 0; i < 4; i++){
            fullSquareFound = false;
            for(int j = 3; j >= 0; j--){
                if(!gameBoard[j][i].isEmpty())
                    fullSquareFound = true;
                else{
                    if(fullSquareFound)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean canMoveDown(){
        for(int i = 0; i < 4; i++){
            for(int j = 2; j >= 0; j--){
                if(!gameBoard[j][i].isEmpty() && gameBoard[j][i].getValue() == gameBoard[j+1][i].getValue())
                    return true;
            }
        }
        boolean fullSquareFound;
        for(int i = 0; i < 4; i++){
            fullSquareFound = false;
            for(int j = 0; j < 4; j++){
                if(!gameBoard[j][i].isEmpty())
                    fullSquareFound = true;
                else{
                    if(fullSquareFound)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean canMoveLeft(){
        for(int i = 0; i < 4; i++){
            for(int j = 1; j < 4; j++){
                if(!gameBoard[i][j].isEmpty() && gameBoard[i][j].getValue() == gameBoard[i][j-1].getValue())
                    return true;
            }
        }
        boolean fullSquareFound;
        for(int i = 0; i < 4; i++){
            fullSquareFound = false;
            for(int j = 3; j >= 0; j--){
                if(!gameBoard[i][j].isEmpty())
                    fullSquareFound = true;
                else{
                    if(fullSquareFound)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean canMoveRight(){
        for(int i = 0; i < 4; i++){
            for(int j = 2; j >= 0; j--){
                if(!gameBoard[i][j].isEmpty() && gameBoard[i][j].getValue() == gameBoard[i][j+1].getValue())
                    return true;
            }
        }
        boolean fullSquareFound;
        for(int i = 0; i < 4; i++){
            fullSquareFound = false;
            for(int j = 0; j < 4; j++){
                if(!gameBoard[i][j].isEmpty())
                    fullSquareFound = true;
                else{
                    if(fullSquareFound)
                        return true;
                }

            }
        }
        return false;
    }


    public boolean canMerge(Square s){
        if(s.getRow() > 0)
            if(gameBoard[s.getRow()-1][s.getColumn()].getValue() == s.getValue())
                return true;
        if(s.getRow() < 3)
            if(gameBoard[s.getRow()+1][s.getColumn()].getValue() == s.getValue())
                return true;
        if(s.getColumn() > 0)
            if(gameBoard[s.getRow()][s.getColumn()-1].getValue() == s.getValue())
                return true;
        if(s.getColumn() < 3)
            if(gameBoard[s.getRow()][s.getColumn()+1].getValue() == s.getValue())
                return true;
        return false;
    }

    public void mergeSquares(Square destination, Square source){
        destination.setValue(source.getValue()*2);
        if(destination.getValue() > bestTile)
            bestTile = destination.getValue();
        destination.setCanMerge(false);
        score += destination.getValue();
        animationNeeded = true;
        source.setValue(0);
    }

    public void resetSquareBooleans(){
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                gameBoard[i][j].setCanMerge(true);
            }
        }
        animationNeeded = false;
    }

    public void saveData(){
        SaveFile file = new SaveFile(gameBoard, score, bestScore, bestTile, lost, won, keepPlaying);

        SharedPreferences prefs = game.getSharedPreferences("bestScore", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(file);
        prefsEditor.putString("saveFile", json);
        prefsEditor.apply();
    }

    public boolean loadData(){
        SharedPreferences prefs = game.getSharedPreferences("bestScore", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString("saveFile", "");
        SaveFile file = gson.fromJson(json, SaveFile.class);

        if(file != null){
            gameBoard = file.getGameBoard();
            score = file.getScore();
            bestScore = file.getBestScore();
            bestTile = file.getBestTile();
            lost = file.isLost();
            won = file.isWon();
            keepPlaying = file.isKeepPlaying();
            return true;
        }
        return false;
    }

    public void resetStats(){
        bestScore = 0;
        bestTile = 0;
        restart();
    }

    public int getBestScore() {
        return bestScore;
    }

    public Square[][] getGameBoard() {
        return gameBoard;
    }

    public int getScore() {
        return score;
    }

    public int getBestTile() {
        return bestTile;
    }

    public boolean isLost() {
        return lost;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isKeepPlaying() {
        return keepPlaying;
    }

    public Game getGame() {
        return game;
    }

    public Button getButtonContinue() {
        return buttonContinue;
    }


    private class CustomDialog extends DialogFragment {

        @Override
        public void onStart() {
            // This MUST be called first! Otherwise the view tweaking will not be present in the displayed Dialog (most likely overriden)
            super.onStart();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog builder = new AlertDialog.Builder(getActivity()).create();

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.dialog_layout, null);
            builder.setView(layout, 0, 0, 0, 0);
            Button buttonCancel = (Button) layout.findViewById(R.id.buttonCancel);
            Button buttonAccept = (Button) layout.findViewById(R.id.buttonAccept);

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundPool.play(soundMerge, 1, 1, 0, 0, 1);
                    dismiss();
                }
            });

            buttonAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundPool.play(soundMerge, 1, 1, 0, 0, 1);
                    resetStats();
                    dismiss();
                }
            });
            return builder;
        }

    }
}


