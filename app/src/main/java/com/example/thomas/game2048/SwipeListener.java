package com.example.thomas.game2048;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SwipeListener implements OnTouchListener {

    private final Game game;
    private final GestureDetector gestureDetector;

    public SwipeListener (Context context){
        this.game = (Game) context;
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            if(!game.getGameController().isLost() && (!game.getGameController().isWon() || game.getGameController().isKeepPlaying()))
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                        result = true;
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                    result = true;

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            return result;
        }
    }

    public void onSwipeTop() {
        if(game.getGameController().canMoveUp()){
            game.getGameController().moveUp();
            synchronized(game.getGameController().t){
                game.getGameController().t.notify();
            }
        }
        game.getGameCanvas().invalidate();
    }
    public void onSwipeRight() {
        if(game.getGameController().canMoveRight()){
            game.getGameController().moveRight();
            synchronized(game.getGameController().t){
                game.getGameController().t.notify();
            }
        }
        game.getGameCanvas().invalidate();
    }
    public void onSwipeLeft() {
        if(game.getGameController().canMoveLeft()){
            game.getGameController().moveLeft();
            synchronized(game.getGameController().t){
                game.getGameController().t.notify();
            }
        }
        game.getGameCanvas().invalidate();
    }
    public void onSwipeBottom() {
        if(game.getGameController().canMoveDown()){
            game.getGameController().moveDown();
            synchronized(game.getGameController().t){
                game.getGameController().t.notify();
            }
        }
        game.getGameCanvas().invalidate();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}