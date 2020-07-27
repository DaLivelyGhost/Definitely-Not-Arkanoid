package com.example.mcombslab0;

import android.animation.TimeAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.util.AttributeSet;
import android.view.View;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

//Background Photo by Miriam Espacio from Pexels

public class BreakOut extends View implements TimeAnimator.TimeListener {

    final boolean GAME_WIN = true;
    final boolean GAME_LOSE = false;
    final float BRICK_ARRAY_BOUND = 10;
    final float RATIO_WIDTH = 16;
    final float RATIO_HEIGHT = 9;
    final float CENTER_X = RATIO_WIDTH / 2;
    final float CENTER_Y = RATIO_HEIGHT / 2;
    final float BRICK_WIDTH = RATIO_WIDTH / BRICK_ARRAY_BOUND;
    final float BRICK_HEIGHT = (RATIO_HEIGHT / BRICK_ARRAY_BOUND) / 2;
    final float PADDLE_LEFT = (RATIO_WIDTH / -10);
    final float PADDLE_TOP = (RATIO_HEIGHT / -40);
    final float PADDLE_RIGHT = (RATIO_WIDTH / 10);
    final float PADDLE_BOTTOM = (RATIO_HEIGHT / 40);
    final float PADDLE_MOVE_SPEED = .3f;
    final float SPEED_INCREASE = .3f;
    final int GAME_BACKGROUND = R.drawable.sky_space_milky_way_stars;

    int level;
    int score;
    int[][] brickArray;
    int brickCount;
    int brickHit;

    int prefBallCount = 2;  //Default preferences
    int prefBrickCount = 10;
    int prefBrickHit = 3;

    boolean paused;
    int incomingW;
    int incomingH;
    TimeAnimator gameTimer;
    gameObserver gameWatcher;

    boolean moveLeft;
    boolean moveRight;
    boolean paddleHitLast;
    float paddleXPos;
    float paddleYPos;
    float paddleBounceAngle;
    int ballCount;
    float ballXPos;
    float ballYPos;
    float ballYVelocity;
    float ballXVelocity;
    float ballRadius;
    float ballBottom;
    float ballTop;
    float ballLeft;
    float ballRight;

    public BreakOut(Context context) {
        super(context);
        BreakOutConstructor();
    }
    public BreakOut(Context context, AttributeSet attr) {
        super(context, attr);
        BreakOutConstructor();
    }
    private void BreakOutConstructor() {
        //Time animator
        this.gameTimer = new TimeAnimator();
        this.gameTimer.setTimeListener(this);

        //Game State
        this.gameWatcher = new gameObserver();
        this.level = 0;
        this.score = 0;
        this.brickArray = new int[10][10];
        this.paused = true;
        this.brickCount = prefBrickCount;
        this.brickHit = prefBrickHit;
        this.ballCount = prefBallCount;
        this.gameWatcher.setRemainingBalls(this.ballCount);
        setBricks();
        setBall();
        setPaddle();

    }
    private void setBall() {
        this.ballBottom = 0;
        this.ballTop = 0;
        this.ballLeft = 0;
        this.ballRight = 0;
        this.ballYVelocity = 0.08f + (0.08f * (this.level * SPEED_INCREASE)); //multiplies the speed by 1/3
        this.ballXVelocity = 0.08f + (0.08f * (this.level * SPEED_INCREASE)); //multiplies the speed by 1/3
        this.ballXPos = CENTER_X;
        this.ballYPos = CENTER_Y;
        this.ballRadius = RATIO_HEIGHT / 30;
        this.ballBottom = this.ballYPos + this.ballRadius;
        this.ballTop = this.ballYPos - this.ballRadius;
        this.ballLeft = this.ballXPos - this.ballRadius;
        this.ballRight = this.ballXPos + this.ballRadius;
        this.paddleHitLast = false;
    }
    private void setPaddle() {
        this.paddleXPos = CENTER_X;
        this.paddleYPos = (RATIO_HEIGHT / 8) * 7;
    }
    private void setBricks() {
        //This method will randomly distribute bricks in the wall.
        Random r = new Random();
        int tempx = r.nextInt(9);
        int tempy = r.nextInt(9);

        //if the wall is maxxed out, just fill it. No reason to randomly generate them.
        if(this.prefBrickCount == 100) {
            for(int i = 0; i < 10; i++) {
                for(int j = 0; j < 10; j++) {
                    this.brickArray[i][j] = this.prefBrickHit;
                }
            }
        }
        else { //if the brick count is not full
            for (int i = 0; i < this.prefBrickCount; i++) { //generate the designated amount of bricks
                while (this.brickArray[tempx][tempy] > 0) {  //if a space is occupied, try again
                    tempx = r.nextInt(9);
                    tempy = r.nextInt(9);
                }
                this.brickArray[tempx][tempy] = this.prefBrickHit;
            }
        }
        this.gameWatcher.setRemainingBricks(this.prefBrickCount);
        this.gameWatcher.notifyObservers();
    }
    public void setPreferences(int brickCount, int brickHit, int ballCount) {

        this.prefBrickCount = brickCount;
        this.brickCount = this.prefBrickCount;

        this.prefBrickHit = brickHit;

        this.prefBallCount = ballCount;
        this.ballCount = prefBallCount;

        this.gameWatcher.setRemainingBalls(this.ballCount);
        this.gameWatcher.setRemainingBricks(this.prefBrickCount);

        pauseGame();
        resetGame();

        this.brickArray = new int[10][10];
        setBricks();
    }
    public void startGame() {
        if (this.gameTimer.isStarted()) {
            this.gameTimer.resume();
        }
        else {
            this.gameTimer.start();
        }
        this.paused = false;
    }
    public void pauseGame() {
        this.paused = true;
        this.gameTimer.pause();
    }
    private void resetGame() {
        this.paddleXPos = CENTER_X;
        setBall();
    }
    private void GameOver() {
        pauseGame();
        resetGame();
        this.brickArray = new int[10][10];
        this.brickCount = prefBrickCount;
        setBricks();
        this.score = 0;
        this.ballCount = prefBallCount;
        this.brickHit = prefBrickHit;

        this.gameWatcher.setGameEnd(GAME_LOSE);
        this.gameWatcher.setScore(0);
        this.gameWatcher.setLevel(1);
        this.gameWatcher.setRemainingBalls(this.prefBallCount);
        this.gameWatcher.notifyObservers();
        this.gameWatcher.setGameEnd(GAME_WIN);
    }
    private void GameWin() {
        pauseGame();
        resetGame();
        this.brickArray = new int[10][10];
        this.brickCount = prefBrickCount;
        setBricks();
        this.level++;
        this.gameWatcher.setLevel(this.level + 1);
        this.gameWatcher.notifyObservers();
    }
    private void reduceBrickCount() {   //reduce brick count and increment score (since one wont happen without the other)
        this.brickCount--;
        this.score = this.score + 1;
    }
    public void movePaddleLeft(boolean move) {
        if(move) {
            this.moveLeft = true;
        }
        else {
            this.moveLeft = false;
        }
    }
    public void movePaddleRight(boolean move) {
        if(move) {
            this.moveRight = true;
        }
        else {
            this.moveRight = false;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(this.incomingW / RATIO_WIDTH, this.incomingH / RATIO_HEIGHT);
        //paint initialization------------------------------------------
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        //--------------------------------------------------------------
        //Brick initialization------------------------------------------
        for(int i = 0; i < BRICK_ARRAY_BOUND; i++) {
            for(int j = 0; j < BRICK_ARRAY_BOUND; j++) {
                if (this.brickArray[i][j] > 0) {
                    float ulx = j * BRICK_WIDTH;
                    float uly = i * BRICK_HEIGHT;
                    if(this.brickArray[i][j] == 1) {
                        paint.setColor(Color.BLACK);
                        canvas.drawRect(ulx, uly, ulx + BRICK_WIDTH, uly + BRICK_HEIGHT, paint);
                    }
                    else if(this.brickArray[i][j] == 2) {
                        paint.setColor(Color.GRAY);
                        canvas.drawRect(ulx, uly, ulx + BRICK_WIDTH, uly + BRICK_HEIGHT, paint);
                    }
                    else if(this.brickArray[i][j] == 3) {
                        paint.setColor(Color.LTGRAY);
                        canvas.drawRect(ulx, uly, ulx + BRICK_WIDTH, uly + BRICK_HEIGHT, paint);
                    }
                    else if(this.brickArray[i][j] > 3) {
                        paint.setColor(Color.BLUE);
                        canvas.drawRect(ulx, uly, ulx + BRICK_WIDTH, uly + BRICK_HEIGHT, paint);
                    }
                }
            }
        }
        //--------------------------------------------------------------
        //draw ball-----------------------------------------------------
        paint.setColor(Color.GREEN);
        canvas.drawCircle(this.ballXPos, this.ballYPos, this.ballRadius, paint);
        //--------------------------------------------------------------
        //draw paddle---------------------------------------------------
        paint.setColor(Color.BLUE);
        canvas.drawRect(this.paddleXPos + PADDLE_LEFT, this.paddleYPos + PADDLE_TOP,
                this.paddleXPos + PADDLE_RIGHT, this.paddleYPos + PADDLE_BOTTOM, paint);
        //--------------------------------------------------------------
        //Ball Math
        //--------------------------------------------------------------
        canvas.restore();
    }

    @Override
    public void onTimeUpdate(TimeAnimator timeAnimator, long total, long delta) {
        //Paddle Update----------------------------------
        if(this.moveLeft && this.paddleXPos >= 0) {
            this.paddleXPos = this.paddleXPos - PADDLE_MOVE_SPEED;
        }
        if(this.moveRight && this.paddleXPos <= RATIO_WIDTH) {
            this.paddleXPos = this.paddleXPos + PADDLE_MOVE_SPEED;
        }
        //-----------------------------------------------

        //Ball Update------------------------------------

        //PADDLE DETECTION------------------------------
        //IF THE BALL IS AT THE Y POSITION OF THE PADDLE
        if(this.ballBottom - this.paddleYPos >= PADDLE_TOP && this.paddleHitLast == false) {
                //IF THE BALL IS WITHIN THE X BOUNDS OF THE PADDLE
            if((this.ballXPos > this.paddleXPos + PADDLE_LEFT) && (this.ballXPos < this.paddleXPos + PADDLE_RIGHT)) {

                float width = (PADDLE_RIGHT - PADDLE_LEFT) / 2;
                this.paddleBounceAngle = (this.ballXPos - (this.paddleXPos)) / width;
                float theta = this.paddleBounceAngle * 67.5f;

                float totalVelocity = (float)Math.sqrt((this.ballXVelocity * this.ballXVelocity) + (this.ballYVelocity * this.ballYVelocity));

                this.ballXVelocity = (float) Math.sin(theta) * totalVelocity;
                this.ballYVelocity = -(float) Math.cos(theta) * totalVelocity;

                if(this.ballYVelocity > 0) {
                    this.ballYVelocity = this.ballYVelocity * -1;
                }
                this.paddleHitLast = true;
            }
        }
        //TOP WALL DETECTION----------------------------
        if(this.ballTop <= 0) {
            this.ballYVelocity = this.ballYVelocity * -1;
        }
        //FLOOR DETECTION-------------------------------
        if(this.ballYPos > RATIO_HEIGHT) {
            this.ballCount--;
            this.gameWatcher.setRemainingBalls(this.ballCount);
            this.gameWatcher.notifyObservers();
            if(this.ballCount == 0) {
                GameOver();
            }
            else {
                pauseGame();
                resetGame();
            }
        }
        //RIGHT WALL DETECTION--------------------------
        if(this.ballRight >= RATIO_WIDTH) {
            this.ballXVelocity = this.ballXVelocity * -1;
            this.paddleHitLast = false;
        }
        //LEFT WALL DETECTION---------------------------
        if(this.ballLeft <= 0) {
            this.ballXVelocity = this.ballXVelocity * -1;
            this.paddleHitLast = false;
        }
        //BRICK DETECTION-------------------------------
        if(this.ballYPos < RATIO_HEIGHT / 2) { //bricks are only in the top half of the screen
            this.paddleHitLast = false;
            int xIndex = (int)(this.ballYPos / BRICK_HEIGHT);

            //Check top of ball for brick impact-----------------
            //Do not attempt if the top of the ball is OOB
            if(this.ballTop > 0) {
                int TopxIndex = (int) (this.ballXPos / BRICK_WIDTH);
                int TopyIndex = (int) (this.ballTop / BRICK_HEIGHT);

                if (this.brickArray[TopyIndex][TopxIndex] > 0) {
                    this.brickArray[TopyIndex][TopxIndex]--;
                    this.ballYVelocity = this.ballYVelocity * -1;

                    if(this.brickArray[TopyIndex][TopxIndex] == 0) {
                        reduceBrickCount();
                        this.gameWatcher.setScore(this.score);
                        this.gameWatcher.setRemainingBricks(this.brickCount);
                        this.gameWatcher.notifyObservers();
                    }
                }
            }
            //Check left of ball for brick impact-----------------
            int LeftxIndex = (int)(this.ballLeft / BRICK_WIDTH);
            int LeftyIndex = (int)(this.ballYPos / BRICK_HEIGHT);

            if(this.brickArray[LeftyIndex][LeftxIndex] > 0) {
                this.brickArray[LeftyIndex][LeftxIndex]--;
                this.ballXVelocity = this.ballXVelocity * -1;

                if(this.brickArray[LeftyIndex][LeftxIndex] == 0) {
                    reduceBrickCount();
                    this.gameWatcher.setScore(this.score);
                    this.gameWatcher.setRemainingBricks(this.brickCount);
                    this.gameWatcher.notifyObservers();
                }
            }
            //Check right of ball for brick impact-----------------
            if(this.ballRight < RATIO_WIDTH) {
                int RightxIndex = (int) (this.ballRight / BRICK_WIDTH);
                int RightyIndex = (int) (this.ballYPos / BRICK_HEIGHT);

                if (this.brickArray[RightyIndex][RightxIndex] > 0) {
                    this.brickArray[RightyIndex][RightxIndex]--;
                    this.ballXVelocity = this.ballXVelocity * -1;

                    if (this.brickArray[RightyIndex][RightxIndex] == 0) {
                        reduceBrickCount();
                        this.gameWatcher.setScore(this.score);
                        this.gameWatcher.setRemainingBricks(this.brickCount);
                        this.gameWatcher.notifyObservers();
                    }
                }
            }
            //Check bottom of ball for brick impact-----------------
            //First checks to make sure the bottom of the ball is in range however.
            //The other sides do not need to be checked because they are in line with the y pos of the ball
            //or before the y pos.
            if(this.ballBottom < RATIO_HEIGHT / 2) {
                int BotxIndex = (int) (this.ballXPos / BRICK_WIDTH);
                int BotyIndex = (int) (this.ballBottom / BRICK_HEIGHT);

                if (this.brickArray[BotyIndex][BotxIndex] > 0) {
                    this.brickArray[BotyIndex][BotxIndex]--;
                    this.ballYVelocity = this.ballYVelocity * -1;

                    if(this.brickArray[BotyIndex][BotxIndex] == 0) {
                        reduceBrickCount();
                        this.gameWatcher.setScore(this.score);
                        this.gameWatcher.setRemainingBricks(this.brickCount);
                        this.gameWatcher.notifyObservers();
                    }
                }
            }
            if(this.brickCount == 0) {
                GameWin();
            }
        }
        //----------------------------------------------
        this.ballXPos = this.ballXPos + this.ballXVelocity;
        this.ballYPos = this.ballYPos + this.ballYVelocity;

        this.ballBottom = this.ballYPos + this.ballRadius;
        this.ballTop = this.ballYPos - this.ballRadius;
        this.ballLeft = this.ballXPos - this.ballRadius;
        this.ballRight = this.ballXPos + this.ballRadius;
        invalidate();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.incomingW = w;
        this.incomingH = h;

    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec ) {
        float width = MeasureSpec.getSize(widthMeasureSpec);
        float height = MeasureSpec.getSize(heightMeasureSpec);

        float aspRatio = RATIO_WIDTH / RATIO_HEIGHT;
        float currAspRatio = width / height;

        if(currAspRatio != aspRatio) {
            float newWidth = height * RATIO_WIDTH;
            newWidth = newWidth / RATIO_HEIGHT;
            setMeasuredDimension((int)newWidth, (int)height);
        }
        else {
            setMeasuredDimension((int)width, (int)height);
        }
    }
    //--------------------------------------------------------------
    //Nested Class
    //--------------------------------------------------------------
    public class gameObserver extends Observable {
        boolean gameEnd;
        private int score;
        private int level;
        private int remainingBricks;
        private int remainingBalls;
        public gameObserver() {
            this.gameEnd = true;
            this.score = 0;
            this.remainingBricks = 0;
            this.level = 1;
            this.remainingBalls = 0;
        }
        public void setGameEnd(boolean end) {
            this.gameEnd = end;
            this.setChanged();
        }
        public void setScore(int score) {
            this.score = score;
            this.setChanged();
        }
        public void setLevel(int level) {
            this.level = level;
            this.setChanged();
        }
        public void setRemainingBricks(int r) {
            this.remainingBricks = r;
            this.setChanged();
        }
        public void setRemainingBalls(int b) {
            this.remainingBalls = b;
            this.setChanged();
        }
        public int getScore() {
            return this.score;
        }
        public int getLevel() {
            return this.level;
        }
        public int getRemainingBalls() {
            return this.remainingBalls;
        }
        public int getRemainingBricks() {
            return this.remainingBricks;
        }
    }
}
