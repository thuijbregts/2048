package com.example.thomas.game2048;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

public class Game extends Activity {

    private GameCanvas gameCanvas;
    private GameController gameController;
    private ViewGroup view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        view = (ViewGroup) findViewById(R.id.view);

        gameController = new GameController(this);
        gameCanvas = new GameCanvas(this);
        gameController.t.start();
        view.addView(gameCanvas);
        view.setOnTouchListener(new SwipeListener(this));

    }

    public GameCanvas getGameCanvas() {
        return gameCanvas;
    }

    public GameController getGameController() {
        return gameController;
    }

    public ViewGroup getView() {
        return view;
    }
}
