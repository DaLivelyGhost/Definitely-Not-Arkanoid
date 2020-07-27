package com.example.mcombslab0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    BreakOut game;
    ImageView LButton;
    ImageView RButton;
    TextView score;
    TextView ballCount;
    TextView remainingBricks;
    TextView level;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_about:
                Toast.makeText(this, "Arkanoid, Spring 2020, Morgan Combs", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                this.game.pauseGame();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);

//        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), );
        game = (BreakOut) findViewById(R.id.breakOut);

        game.gameWatcher.addObserver(this);

        this.score = (TextView) findViewById(R.id.scoreText);
        this.ballCount = (TextView) findViewById(R.id.ballText);
        this.remainingBricks = (TextView) findViewById(R.id.brickText);
        this.level = (TextView) findViewById(R.id.levelText);

        this.LButton = (ImageView) findViewById(R.id.btnLeft);
        this.RButton = (ImageView) findViewById(R.id.btnRight);

        this.LButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    game.movePaddleLeft(true);
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    game.movePaddleLeft(false);
                }
                return false;
            }
        });
        this.RButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    game.movePaddleRight(true);
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    game.movePaddleRight(false);
                }
                return false;
            }
        });
        this.game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(game.paused) {
                    game.startGame();
                }
                else {
                    game.pauseGame();
                }
            }
        });
    }
    @Override
    public void update(Observable o, Object arg) {

        if (this.game.gameWatcher.gameEnd == false) {
            Toast.makeText(this, "Game Over! restarting...", Toast.LENGTH_SHORT).show();
        }
        this.score.setText("Score: " + this.game.gameWatcher.getScore());
        this.level.setText("Level: " + this.game.gameWatcher.getLevel());
        this.ballCount.setText("Balls: " + this.game.gameWatcher.getRemainingBalls());
        this.remainingBricks.setText("Bricks: " + this.game.gameWatcher.getRemainingBricks());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String brickCount = prefs.getString("brick_count", "10");
        int realBrickCount = Integer.parseInt(brickCount);
        if(realBrickCount < 10) {
            realBrickCount = 10;
        }
        if(realBrickCount > 100) {
            realBrickCount = 100;
        }
        int hits = Integer.parseInt(prefs.getString("brick_hits", "3"));
        int ballCount = Integer.parseInt(prefs.getString("ball_count", "2"));

        this.game.setPreferences(realBrickCount, hits, ballCount);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        game.pauseGame();
    }
}
