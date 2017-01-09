package com.example.thomas.game2048;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.*;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameCanvas extends View {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Game game;
    private GameController gameController;

    private int GAP;
    private int SQUARE_SIZE;

    private Typeface font;

    private int SIZE1;
    private int SIZE2;
    private int SIZE3;
    private int SIZE4;

    private TextView score;
    private TextView bestScore;
    private TextView bestTile;
    private TextView labelEnd;
    private TextView labelAddition;

    private boolean animationUnlocked;

    private Animation fadeLost;
    private Animation fadeWon;

    private Rect rectangle = new Rect();
    private RectF rectF = new RectF();

    public GameCanvas(Context context) {
        super(context);
        game = (Game) context;

        GAP = (int) Screen.convertDpToPixel(5, context);
        SQUARE_SIZE = (int) Screen.convertDpToPixel(75, context);

        SIZE1 = (int) Screen.convertDpToPixel(40, context);
        SIZE2 = (int) Screen.convertDpToPixel(30, context);
        SIZE3 = (int) Screen.convertDpToPixel(25, context);
        SIZE4 = (int) Screen.convertDpToPixel(20, context);

        int size = (int) Screen.convertDpToPixel(325, context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(size, size);

        int marginTop = (int) Screen.convertDpToPixel(120, context);
        int marginLeft = (int) Screen.convertDpToPixel(2, context);
        lp.setMargins(marginLeft, marginTop, 0, 0);
        setLayoutParams(lp);

        score = (TextView) game.getView().findViewById(R.id.score);
        bestScore = (TextView) game.getView().findViewById(R.id.bestScore);
        bestTile = (TextView) game.getView().findViewById(R.id.bestTile);

        TextView labelScore = (TextView) game.getView().findViewById(R.id.labelScore);
        TextView labelBestScore = (TextView) game.getView().findViewById(R.id.labelBestScore);
        TextView labelBestTile = (TextView) game.getView().findViewById(R.id.labelBestTile);

        labelAddition = (TextView) game.getView().findViewById(R.id.labelAddition);
        lp = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        marginTop = (int) Screen.convertDpToPixel(10, game);
        int marginRight = (int) Screen.convertDpToPixel(10, game);
        lp.setMargins(0, marginTop, marginRight, 0);
        labelAddition.setLayoutParams(lp);

        Typeface face = Typeface.createFromAsset(game.getAssets(), "fonts/temp.ttf");
        Typeface labels = Typeface.create(face, Typeface.BOLD);

        score.setTypeface(labels);
        bestScore.setTypeface(labels);
        labelBestTile.setTypeface(labels);
        bestTile.setTypeface(labels);
        labelScore.setTypeface(labels);
        labelBestScore.setTypeface(labels);

        labelEnd = (TextView) game.getView().findViewById(R.id.labelLost);
        labelEnd.setTypeface(labels);

        font = Typeface.createFromAsset(game.getAssets(), "fonts/tahomabd.ttf");

        gameController = game.getGameController();

        animationUnlocked = true;

        instantiateAnimations();
    }

    private void instantiateAnimations() {
        fadeLost = new AlphaAnimation(0.0f, 1.0f);
        fadeLost.setDuration(3000);
        fadeLost.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                labelEnd.setVisibility(View.VISIBLE);
                animationUnlocked = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeWon = new AlphaAnimation(0.0f, 1.0f);
        fadeWon.setDuration(3000);
        fadeWon.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                labelEnd.setVisibility(View.VISIBLE);
                animationUnlocked = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setLabelLostToInvisible();
                gameController.getButtonContinue().setEnabled(true);
                gameController.getButtonContinue().setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void updateScores(){
        score.setText(""+game.getGameController().getScore());
        bestScore.setText("" + game.getGameController().getBestScore());
        bestTile.setText("" + game.getGameController().getBestTile());
    }

    public void animateScoreAddition(int value){
        labelAddition.setText("+" + value);
        Animation slide = AnimationUtils.loadAnimation(game, R.anim.anim_slide);
        labelAddition.setAnimation(slide);
        slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                labelAddition.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                labelAddition.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        labelAddition.startAnimation(slide);

    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setColor(Color.parseColor("#BBADA0"));
        paint.setStyle(Style.FILL);
        canvas.drawRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), 6, 6, paint);
        paint.setTextAlign(Align.CENTER);


        paint.setTypeface(font);

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++) {
                Square s = gameController.getGameBoard()[i][j];
                paint.setColor(s.getColor());
                int xPosition, yPosition;

                xPosition = (j + 1) * GAP + j * SQUARE_SIZE;
                yPosition = (i + 1) * GAP + i * SQUARE_SIZE;
                rectangle.set(xPosition,
                        yPosition,
                        xPosition + SQUARE_SIZE,
                        yPosition + SQUARE_SIZE);
                rectF.set(rectangle);

                canvas.drawRoundRect(rectF, 6, 6, paint);
                if(s.getValue() > 4)
                    paint.setColor(Color.parseColor("#F9F6F2"));
                else
                    paint.setColor(Color.rgb(119,110,101));

                if(s.getValue() > 9999)
                    paint.setTextSize(SIZE4);
                else if(s.getValue() > 512)
                    paint.setTextSize(SIZE3);
                else if (s.getValue() > 64)
                    paint.setTextSize(SIZE2);
                else
                    paint.setTextSize(SIZE1);

                if(!s.isCanMerge()){
                    paint.setTextSize(paint.getTextSize() + 20);
                }

                int xPos = xPosition + (SQUARE_SIZE / 2);
                int yPos = yPosition + (int) ((SQUARE_SIZE / 2) - ((paint.descent() + paint.ascent()) / 2));

                if(s.getValue() > 0) {
                    canvas.drawText("" + gameController.getGameBoard()[i][j].getValue(), xPos, yPos, paint);
                }
            }
        }

        if(animationUnlocked) {
            if (gameController.isLost()) {
                labelEnd.setText("You Lost!");

                labelEnd.startAnimation(fadeLost);
            } else if (gameController.isWon()) {
                labelEnd.setText("You Won!");

                labelEnd.startAnimation(fadeWon);
            }
        }
    }


    public void setAnimationUnlocked(boolean animationUnlocked) {
        this.animationUnlocked = animationUnlocked;
    }

    public void setLabelLostToInvisible(){
        labelEnd.setVisibility(View.GONE);
    }

}