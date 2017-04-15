package jake.tiranozavr.task6_android2;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class Task extends AppCompatActivity {

    Arkanoid arkanoid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        arkanoid = new Arkanoid(this);
        setContentView(arkanoid);
    }

    class Arkanoid extends SurfaceView implements Runnable{

        Thread thread= null;
        SurfaceHolder surfaceHolder;

        volatile  boolean playing;
        boolean paused =true;

        Canvas canvas;
        Paint paint;

        long fps;
        private long frameTime;

        int screenX;
        int screenY;

        // objects
        Catcher catcher;
        Ball ball;

        int lives = 3;

        Brick[] bricks = new Brick[200];
        int numBricks = 0;



        public Arkanoid(Context context) {
            super(context);
            surfaceHolder= getHolder();
            paint= new Paint();

            Display display = getWindowManager().getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            screenX = size.x;
            screenY = size.y;

            catcher = new Catcher(screenX, screenY);
            ball = new Ball(screenX,screenY);


            createBricksAndRestart();


        }

        public void createBricksAndRestart(){

            // Put the ball back to the start
            ball.reset(screenX, screenY);

            int brickWidth = screenX / 15;
            int brickHeight = screenY / 10;

            // Build a wall of bricks
            numBricks = 0;

            for(int column = 0; column < 15; column ++ ){
                for(int row = 0; row < 4; row ++ ){
                    bricks[numBricks] = new Brick(row, column, brickWidth, brickHeight);
                    numBricks ++;
                }
            }

        }

        @Override
        public void run() {
            while (playing){
                long startFrameTime= System.currentTimeMillis();

                if(!paused){
                    update();
                }

                draw();


                frameTime=System.currentTimeMillis()-startFrameTime;
                if (frameTime>=1){
                    fps= 1000/frameTime;
                }

            }

        }

        public  void  update(){

            catcher.update(fps);
            ball.update(fps);

            // Check for ball colliding with a brick
            for(int i = 0; i < numBricks; i++){

                if (bricks[i].getVisibility()){

                    if(RectF.intersects(bricks[i].getRect(),ball.getRect())) {
                        bricks[i].setInvisible();
                        ball.reverseYVelocity();
                    }
                }
            }

            // Check for ball colliding with catcher
            if(RectF.intersects(catcher.getRect(),ball.getRect())) {
                //ball.setRandomXVelocity();
                ball.reverseYVelocity();
                ball.clearObstacleY(catcher.getRect().top - 2);
            }

            // Bounce the ball back when it hits the bottom of screen
            if(ball.getRect().bottom > screenY){
                ball.reverseYVelocity();
                ball.clearObstacleY(screenY - 2);

                // Lose a life
                lives --;

                if(lives == 0){
                    paused = true;
                    createBricksAndRestart();
                    lives = 50;
                }

            }

            // Bounce the ball back when it hits the top of screen
            if(ball.getRect().top < 0){
                ball.reverseYVelocity();
                ball.clearObstacleY(12);
            }

            // If the ball hits left wall bounce
            if(ball.getRect().left < 0){
                ball.reverseXVelocity();
                ball.clearObstacleX(2);
            }

            // if the ball hits right wall bounce
            if (ball.getRect().right > screenX - 10) {
                ball.reverseXVelocity();
                ball.clearObstacleX(screenX - 30);
            }

        }



        public void draw(){

            if(surfaceHolder.getSurface().isValid()){
                canvas=surfaceHolder.lockCanvas();

                canvas.drawColor(Color.rgb(161,12,237));

                paint.setColor(Color.rgb(255,255,255));

                canvas.drawRect(catcher.getRect(), paint);
                canvas.drawRect(ball.getRect(), paint);
                Log.d("MyLogs", "Catcher is drawn");
                Log.d("MyLogs", catcher.toString());

                paint.setColor(Color.rgb(216, 45, 0));

                // Draw the bricks if visible
                for(int i = 0; i < numBricks; i++){
                    if(bricks[i].getVisibility()) {
                        canvas.drawRect(bricks[i].getRect(), paint);
                    }
                }

                paint.setColor(Color.rgb(255,255,255));
                paint.setTextSize(40);
                canvas.drawText("Lives: " + lives, 10,50, paint);

                if (lives <= 0) {
                    paint.setTextSize(90);
                    canvas.drawText("OH SNAP!", screenX / 3, screenY / 2, paint);
                }


                surfaceHolder.unlockCanvasAndPost(canvas);





            }

        }



        public  void pause(){
            playing=false;
            try {
                thread.join();

            }catch (InterruptedException e){
                Log.e("Error:","SHit happen");
            }
        }


        public  void resume(){
            playing=true;
            thread=new Thread(this);

            thread.start();



        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:

                    paused = false;

                    if(event.getX() > screenX / 2){
                        catcher.setMovementState(catcher.RIGHT);
                    }
                    else{
                        catcher.setMovementState(catcher.LEFT);
                    }

                    break;

                case MotionEvent.ACTION_UP:

                    catcher.setMovementState(catcher.STOPPED);
                    break;
            }
            return true;
        }




    }

    @Override
    protected void onResume() {
        super.onResume();
        arkanoid.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        arkanoid.pause();
    }
}




