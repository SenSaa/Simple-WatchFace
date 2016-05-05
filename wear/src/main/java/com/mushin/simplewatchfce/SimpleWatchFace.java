package com.mushin.simplewatchfce;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.Random;


public class SimpleWatchFace extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {

        return new WatchFaceEngine();
    }


    private class WatchFaceEngine extends CanvasWatchFaceService.Engine implements SensorEventListener {

        // ------------------- Declare Variables ---------------------- //
        // Implement a set of member variables in your engine to keep track of device states, timer intervals, painting graphics, and attributes for your display.

        Paint secHandPaint; // "Second" watch hand Paint variable.
        Paint minHandPaint; // "Minute" watch hand Paint variable.
        Paint hourHandPaint; // "Hour" watch hand Paint variable.
        Paint tickMarkPaint; // Watch "Tick Marking" Paint variable.
        Paint logo; // Watch "Tick Marking" Paint variable.
        Paint newPaint;

        Time time; // Time variable.

        int halfWidth; // Half of screen width.
        int halfHeight; // Half of screen height.

        Random randObj = new Random();
        int randVar = randObj.nextInt(150);

        // X & Y position variables of animation shape (circle).
        private int x = randVar;
        private int y = randVar;
        // Variables for X & Y .
        private int xVelocity = 10; // Initial x-axis Speed.
        private int yVelocity = 5; // Initial y-axis Speed.
        int r = 40; // Animation Circle Initial Radius.

        // Handler to handle the shape (circle) animation.
        Handler animationHandler; // This Handler schedules a Runnable to be executed at some point in the future.

        private final int FRAME_RATE = 24; // Frame rate of animation.

        // Sensor variables.
        private SensorManager senSensorManager;
        private Sensor senAccelerometer;

        float xAxis = 0;
        float yAxis = 0;
        float zAxis = 0;

        private float xAcceleration = 0;
        private float yAcceleration = 0;

        private float xSpeed = 0;
        private float ySpeed = 0;

        private float xPosition = 0;
        private float yPosition = 0;

        private Bitmap mBitmap;
        private Bitmap mWood;

        public float frameTime = 0.666f;

        private boolean longPressState = false; // Initial state of Long Press Gesture, which is used to control the drawing of a shape.

        int shadowColor = Color.GRAY;

        //GestureDetectorCompat gestureDetector;





        // The timer is implemented by a Handler, which will force a repaint (invalidate) and sent a delayed message.
        // Handler updates the time once a second in interactive mode.
        final Handler timeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) { // Callback method - called when the messages are sent by the thread.

                // when the handler is active, call invalidate() to make the screen redraw.
                invalidate();

                // When visible and in interactive mode,
                if (isVisible() && !isInAmbientMode()) {
                    // Send message "0", delayed by 1000ms (1 second).
                    // This means that, the handler will activate every 1 second.
                    timeHandler.sendEmptyMessageDelayed(0,1000);
                }
            }
        };


        private WatchFaceEngine() {
            // Create Gesture Detector object.
/*
            gestureDetector = new GestureDetectorCompat(getApplicationContext(),this);
            gestureDetector.setOnDoubleTapListener(this);
*/
        }


        // ------------------- Initialize Variables ---------------------- //
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // Initialize Watch Face (Configure the system UI).
            setWatchFaceStyle(new WatchFaceStyle.Builder(SimpleWatchFace.this) // Sets the watch face style.
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT) // Sets how far into the screen the first card will peek while the watch face is displayed.
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN) // Sets how the first, peeking card will be displayed while the watch is in ambient mode (black & white mode).
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE) // Set how to display background of the first, peeking card.
                            // Set this to "false" if you are drawing or representing the time on your watch face. But if you want the system-style time to show over the watch face, pass the value "true".
                    .setShowSystemUiTime(false) // Sets if the system will draw the system-style time over the watch face.
                    .build()); // Constructs read-only WatchFaceStyle object.


            secHandPaint = new Paint(); // Create "Second" watch hand Paint object.
            secHandPaint.setARGB(255,0,102,204);
            secHandPaint.setStrokeWidth(2);
            secHandPaint.setStrokeCap(Paint.Cap.ROUND);
            secHandPaint.setAntiAlias(true);

            minHandPaint = new Paint(); // Create "Minute" watch hand Paint object.
            minHandPaint.setColor(Color.WHITE);
            minHandPaint.setStrokeWidth(4);
            minHandPaint.setStrokeCap(Paint.Cap.SQUARE);
            minHandPaint.setAntiAlias(true);

            hourHandPaint = new Paint(); // Create "Hour" watch hand Paint object.
            hourHandPaint.setColor(Color.WHITE);
            hourHandPaint.setStrokeWidth(8);
            hourHandPaint.setStrokeCap(Paint.Cap.BUTT);
            hourHandPaint.setAntiAlias(true);
            hourHandPaint.setTextSize(40);

            tickMarkPaint = new Paint(); // Create Watch "Tick Marking" Paint object.
            tickMarkPaint.setColor(Color.GREEN);
            tickMarkPaint.setAlpha(100);
            tickMarkPaint.setStrokeWidth(4);
            tickMarkPaint.setStrokeCap(Paint.Cap.ROUND);
            tickMarkPaint.setAntiAlias(true);

            logo = new Paint(); // Create Logo String Text.
            logo.setTextSize(30);
            logo.setColor(Color.DKGRAY);
            logo.setStrokeWidth(5);
            logo.setStrokeCap(Paint.Cap.BUTT);
            logo.setAntiAlias(true);

            newPaint = new Paint();
            newPaint.setColor(Color.BLACK);
            newPaint.setAlpha(200);


            time = new Time(); // Construct a Time object, which will hold the time value.


            // Get display size, and initialise half of the width and height variables based on the device's display.
            Resources resources = SimpleWatchFace.this.getResources(); // Return a Resources instance for your application's package.
            DisplayMetrics metrics = resources.getDisplayMetrics(); // Return the current display metrics that are in effect for this resource object.
            halfWidth = metrics.widthPixels / 2;
            halfHeight = metrics.heightPixels / 2;


            animationHandler = new Handler(); // Create a Handler object - to schedule a Runnable to be executed at some point in the future.


            // Android supports several sensors via the SensorManager.
            // Access a SensorManager via getSystemService(SENSOR_SERVICE).
            // You can access the sensor via the sensorManager.getDefaultSensor() method, which takes the sensor type and the delay defined as constants on SensorManager as parameters.
            // Once you acquired a sensor, you can register a SensorEventListener object on it. This listener will get informed, if the sensor data changes.
            senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Retrieve a SensorManager for accessing sensors.
            senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // Get the sensor of the type specified in the argument.
            senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL); // Register a SensorEventListener for the given sensor at the given sampling frequency. Where the argument (this) is the SensorEvent Listener object.

        }


        // ------------------- Timer Update ---------------------- //
        // If we're in interactive mode, we need to take care of the timing.
        // But we have to make sure, that our timer only runs if we're in interactive mode.
        // If the mode is changed to ambient mode, we should deactivate the timer.
        private void updateTimer() {

            // Remove the messages which are already in the queue.
            timeHandler.removeMessages(0);

            // If the the watch face is visible and not in ambient mode,
            if (isVisible() && !isInAmbientMode()) {
                // send the instant message into the queue.
                timeHandler.sendEmptyMessage(0);
            }

            // *** It is important to (1) First, remove Handler messages, then (2) Secondly, sendEmptyMessage if not in Ambient mode, in this order. Otherwise, the watch face won't update as intended.

        }

        // If we're in ambient mode, Android makes sure that we get an update once a minute. Now we need to redraw our watchface. The easiest way for this is to call the invalidate method.
        @Override
        public void onTimeTick() {
            super.onTimeTick();

            // The event is only invoked once a minute when the wear is in ambient mode.
            // Call "invalidate" method to invalidate the canvas to redraw the watch face.
            invalidate();
        }


        // ------------------- Mode Changing ---------------------- //
        // The "onVisibilityChanged" method is called when the user hides or shows the watch face.
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            // Call "updateTimer" method ao the timer updates when the visibility changes.
            updateTimer();
        }

        // Implement "onAmbientModeChanged", which is called when the device moves in or out of ambient mode.
        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            // Schedules a call to "onDraw" to draw the next frame.
            invalidate();
            // Call "updateTimer" method when the watch moves out of ambient mode.
            updateTimer();
        }

        // --------------- Runnable interface for running the shape animation. ------------------ //
        // Create an Anonymous Runnable interface for executing the animation.
        private Runnable runnable = new Runnable() {
            @Override
            // Method must be implemented for Runnable interface.
            public void run() { // It is a callback method that starts executing the active part of the class' code.
                invalidate(); // Schedules a call to "onDraw" to draw the next frame.
            }
        };


        // --------------- Sensor Event Listener class methods. ------------------ //
        // Method must be implemented by SensorEventListener.
        // Called when sensor values have changed.
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

/*
                xAxis = event.values[0];
                yAxis = event.values[1];
                zAxis = event.values[2];
*/

                //Set sensor values as acceleration
                xAcceleration = event.values[0];
                yAcceleration = event.values[1];

                updateShape();

                //invalidate();
            }
        }
        // Method must be implemented by SensorEventListener.
        // Called when the accuracy of a sensor has changed.
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }


        public void updateShape() {

            // Calculate new speed.
            xSpeed += (xAcceleration * frameTime); // xSpeed = xSpeed + (xAcceleration * frameTime);
            ySpeed += (yAcceleration * frameTime); //  ySpeed = ySpeed + (yAcceleration * frameTime);

            // Calc distance travelled in that time
            float xS = (xSpeed/2)*frameTime;
            float yS = (ySpeed/2)*frameTime;

            // Add to position negative due to sensor
            // readings being opposite to what we want!
            xPosition -=  xS; // xPosition = xPosition - xS;
            yPosition -=  yS; // yPosition = yPosition - yS;

            if (xPosition > halfWidth) {
                xPosition = 0;
            } else if (xPosition < -halfWidth) {
                xPosition = 0;
            }
            if (yPosition > halfHeight) {
                yPosition = 0;
            } else if (yPosition < -halfHeight) {
                yPosition = 0;
            }

            //invalidate();

        }


        // ------------------- Drawing the Watch Face  ---------------------- //
        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            time.setToNow(); // Sets the time of the given Time object to the current time.

            canvas.drawColor(Color.BLACK); // Background color.
            canvas.save(); // Save transformation matrix
            canvas.translate(halfWidth, halfHeight); // Translate origin to center

            // Declare and initialise the length of the hour and minute hands.
            float hourLength = halfHeight - 60;
            float minLength = halfHeight - 40;
            float secLength = halfHeight - 20;

            // ** The Y axis is reversed. Positive Y = Down, and Negative Y = Up.


            // ___ Blinking Centre Circle Shape. ___
            if (!isInAmbientMode()) {
                int c; // circle counter variable
                int randVar2 = randObj.nextInt(50);
                for (c=0;c<40;c++) {
                    canvas.drawCircle(0,0,randVar2,secHandPaint);
                }
            }



            // _____ Shape Animation. _____
            if (!isInAmbientMode()) {

                // ** x & y must be declared outside this method, since they will be reinitialised, and having two different initialisation within the method causes the method to not function as intended.
                /*
                x = 0;
                y = 0;
                xVelocity = 5;
                yVelocity = 5;
                */

                x = x + xVelocity; // Acceleration in the x-axis by adding xVelocity to the x position.
                y = y + yVelocity; // Acceleration in the y-axis by adding yVelocity to the y position.

                // if the x-position of the shape exceeds half of the screen width (the right side of the visible screen), then...
                if ( x >= (halfWidth-0.5*r) ) {
                    xVelocity = -5; // Pass a negative value to the x-axis acceleration, so that it can change direction towards the left side.
                    r = randObj.nextInt(50)+20;
                    //
                    if ( y >= (halfHeight*0.5) ) {
                        yVelocity = -5;
                    }
                    else if ( y < (halfHeight*0.5)) {
                        yVelocity = 5;
                    }
                }
                // if the x-position of the shape exceeds the negative half of the screen width (the left side of the visible screen), then...
                if ( x <= (-halfWidth+0.5*r) ) {
                    xVelocity = 5; // Pass a positive value to the x-axis acceleration, so that it can change direction towards the right side.
                    r = randObj.nextInt(50)+20;
                    //
                    if ( y >= (halfHeight*0.5) ) {
                        yVelocity = -5;
                    }
                    else if ( y < (halfHeight*0.5)) {
                        yVelocity = 5;
                    }
                }
                // if the y-position of the shape exceeds half of the screen height (the bottom side of the visible screen), then...
                if ( y >= (halfHeight-0.5*r) ) {
                    yVelocity = -5; // Pass a negative value to the y-axis acceleration, so that it can change direction towards the top side.
                    r = randObj.nextInt(60)+20;
                    //
                    if ( x >= (halfWidth*0.5) ) {
                        xVelocity = -5;
                    }
                    else if ( x < (halfWidth*0.5)) {
                        xVelocity = 5;
                    }
                }
                // if the y-position of the shape exceeds half of the screen height (the top side of the visible screen), then...
                if ( y <= (-halfHeight+0.5*r) ) {
                    yVelocity = 5; // Pass a positive value to the y-axis acceleration, so that it can change direction towards the bottom side.
                    r = randObj.nextInt(60)+20;
                    //
                    if ( x >= (halfWidth*0.5) ) {
                        xVelocity = -5;
                    }
                    else if ( x < (halfWidth*0.5)) {
                        xVelocity = 5;
                    }
                }

                canvas.drawCircle(x,y,r,tickMarkPaint); // Paint circle shape that is drawn on canvas.

                // "postDelayed" adds the Runnable to the message queue, so that it can run after the specified amount of time elapses.
                // Parameters are (Runnable r, long delayMillis), where delayMillis =	The delay (in milliseconds) until the Runnable will be executed.
                animationHandler.postDelayed(runnable, FRAME_RATE);
            }


            // _____ Draw the hour "Tick Markings". _____
/*
            // __ {For} loop, for ensuring that only the required number of tick marks are drawn. __
            int i; // Counter variable, used for implementing the for below.
            for (i=0; i<12; i++) { // while the counter variable is between 0-12, increment it by 1.
                float tickAngle = (float) (i * (Math.PI / 6)); // Angle (2π radian/12 hours). Taking into the account the counter variable.
                float tickStartX = (float) Math.sin(tickAngle) * secLength; // x-axis starting point of line.
                float tickStartY = (float) -Math.cos(tickAngle) * secLength; // y-axis starting point of line.
                float tickStopX = (float) Math.sin(tickAngle) * (halfWidth+20); // x-axis stopping point of line.
                float tickStopY = (float) -Math.cos(tickAngle) * (halfHeight+20); // y-axis stopping point of line.
                canvas.drawLine(tickStartX, tickStartY, tickStopX, tickStopY, tickMarkPaint); // Drawing the tick marking lines.
            }
*/
            // __ {While} loop, for ensuring that only the required number of tick marks are drawn. __
            int hourTickMarkCounter =0; // Counter variable, used for implementing the loop below.
            while (hourTickMarkCounter<12) { // while the counter variable is less than 12, then...
                float tickAngle = (float) (hourTickMarkCounter * (Math.PI / 6)); // Angle (2π radian/12 hours). Taking into the account the counter variable.
                float tickStartX = (float) Math.sin(tickAngle) * secLength; // x-axis starting point of line.
                float tickStartY = (float) -Math.cos(tickAngle) * secLength; // y-axis starting point of line.
                float tickStopX = (float) Math.sin(tickAngle) * (halfWidth+20); // x-axis stopping point of line.
                float tickStopY = (float) -Math.cos(tickAngle) * (halfHeight+20); // y-axis stopping point of line.
                canvas.drawLine(tickStartX, tickStartY, tickStopX, tickStopY, tickMarkPaint); // Drawing the tick marking lines.
                hourTickMarkCounter++; // increment counter variable by 1.
            }


            // __________ Draw the "Hour" watch hand. __________
            // Angular speed (rotational speed) is the change of angle with respect to time, measured by angular displacement (the angle through which a point or line has been rotated) divided by the time.
            // Therefore, The hour hand goes through 2π radians/360 degrees/turn/revolution/complete rotation/full circle in 12 hours (2π radian/12 hours), so angular velocity, ω = π/6 rad.s-1.
            //float hourRotation = (float) (time.hour * (Math.PI / 6)); // Angle of rotation of "hour" watch hand. Taking into the account the current hour.
            float hourRotation = (float) ((time.hour + time.minute/60f) * (2f*Math.PI / 12f)); // Consider the minutes for the hour hand, since the hour hand moves towards the next hour as the minutes increase.
            // Using trigonometry (SOH, CAH, TOA), and since we need the X & Y components while the hypotenuse is known, we will use sin and cos for X & Y.
            // sin(θ)= O/H --> O = sin(θ)*H ||| where (θ) = angular velocity (ω) determined above, H = clock hand. And O is the x-axis.
            // cos(θ)= A/H --> A = cos(θ*H ||| where (θ) = angular velocity (ω) determined above, H = clock hand. And A is the y-axis.
            float hourHandX = (float) (Math.sin(hourRotation) * hourLength); // Determine the x-element of the "hour" watch hand using trigonometry.
            float hourHandY = (float) (-Math.cos(hourRotation) * hourLength); // Determine the y-element of the "hour" watch hand using trigonometry. *Since the Y axis is reversed, we add the negative sign.
            canvas.drawLine(0,0,hourHandX,hourHandY,hourHandPaint); // Paint the line on canvas based on the X & Y coordinates provided.

            // _____ Draw the colored "in line" area of the hour watch hand. _____
            float hourHandModX1 = (float) (Math.sin(hourRotation) * (20));
            float hourHandModY1 = (float) (-Math.cos(hourRotation) * (20));
            float hourHandModX2 = (float) (Math.sin(hourRotation) * (hourLength-10));
            float hourHandModY2 = (float) (-Math.cos(hourRotation) * (hourLength-10));
            canvas.drawLine(hourHandModX1,hourHandModY1,hourHandModX2,hourHandModY2,tickMarkPaint); // Paint the line on canvas based on the X & Y coordinates provided.


            // __________ Draw the "Minute" watch hand. __________
            // Angular speed (rotational speed) is the change of angle with respect to time, measured by angular displacement (the angle through which a point or line has been rotated) divided by the time.
            // The minute hand goes through 2π radians/360 degrees/turn/revolution/complete rotation/full circle in 1 hour, or 2π radian/60 minutes, so angular velocity, ω = π/30 rad.s-1.
            //float minRotation = (float) (time.minute * (Math.PI / 30)); // Angle of rotation of "minute" watch hand. Taking into the account the current minute.
            float minRotation = (float) ((time.minute + (time.second/60f)) * (2f*Math.PI / 60f)); // Consider the seconds for the min hand, since the min hand moves towards the next min as the seconds increase.
            // Using trigonometry (SOH, CAH, TOA), and since we need the X & Y components while the hypotenuse is known, we will use sin and cos for X & Y.
            // sin(θ)= O/H --> O = sin(θ)*H ||| where (θ) = angular velocity (ω) determined above, H = clock hand. And O is the x-axis.
            // cos(θ)= A/H --> A = cos(θ*H ||| where (θ) = angular velocity (ω) determined above, H = clock hand. And A is the y-axis.
            float minHandX = (float) (Math.sin(minRotation) * minLength); // Determine the x-element of the "minute" watch hand using trigonometry.
            float minHandY = (float) (-Math.cos(minRotation) * minLength); // Determine the y-element of the "minute" watch hand using trigonometry. *Since the Y axis is reversed, we add the negative sign.
            canvas.drawLine(0,0,minHandX,minHandY,minHandPaint); // Paint the line on canvas based on the X & Y coordinates provided.


            // _____ Draw the minute "Tick Markings". _____
            // __ {While} loop, for ensuring that only the required number of tick marks are drawn. __
            int minuteTickMarkCounter =0; // Counter variable, used for implementing the loop below.
            while (minuteTickMarkCounter<60) { // while the counter variable is less than 60, then...
                float tickAngle = (float) (minuteTickMarkCounter * (Math.PI / 30)); // Angle (2π radian/60 minutes). Taking into the account the counter variable.
                float tickStartX = (float) Math.sin(tickAngle) * secLength; // x-axis starting point of line.
                float tickStartY = (float) -Math.cos(tickAngle) * secLength; // y-axis starting point of line.
                float tickStopX = (float) Math.sin(tickAngle) * (halfWidth); // x-axis stopping point of line.
                float tickStopY = (float) -Math.cos(tickAngle) * (halfHeight); // y-axis stopping point of line.
                canvas.drawLine(tickStartX, tickStartY, tickStopX, tickStopY, tickMarkPaint); // Drawing the tick marking lines.
                minuteTickMarkCounter++; // increment counter variable by 1.
            }

            int iM2 =0; // Counter variable, used for implementing the loop below.
            while (iM2<60) { // while the counter variable is less than 60, then...
                float tickAngle = (float) (iM2 * (Math.PI / 30)); // Angle (2π radian/60 minutes). Taking into the account the counter variable.
                float tickStartX = (float) Math.sin(tickAngle) * hourLength; // x-axis starting point of line.
                float tickStartY = (float) -Math.cos(tickAngle) * hourLength; // y-axis starting point of line.
                float tickStopX = (float) Math.sin(tickAngle) * (hourLength+10); // x-axis stopping point of line.
                float tickStopY = (float) -Math.cos(tickAngle) * (hourLength+10); // y-axis stopping point of line.
                canvas.drawLine(tickStartX, tickStartY, tickStopX, tickStopY, tickMarkPaint); // Drawing the tick marking lines.
                iM2++; // increment counter variable by 1.
            }


            // __________ Draw the "Second" watch hand. __________
            if (!isInAmbientMode()) { // Only draw the "second" watch hand when not in Ambient mode.
                // Angular speed (rotational speed) is the change of angle with respect to time, measured by angular displacement (the angle through which a point or line has been rotated) divided by the time.
                // The second hand goes through 2π radians/360 degrees/turn/revolution/complete rotation/full circle in 1 min, or 2π radian/60 seconds, so angular velocity, ω = π/30 rad.s-1.
                float secRotation = (float) (time.second * (Math.PI / 30)); // Angle of rotation of "second" watch hand. Taking into the account the current second.
                // Using trigonometry (SOH, CAH, TOA), and since we need the X & Y components while the hypotenuse is known, we will use sin and cos for X & Y.
                // sin(θ)= O/H --> O = sin(θ)*H ||| where (θ) = angular velocity (ω) determined above, H = clock hand. And O is the x-axis.
                // cos(θ)= A/H --> A = cos(θ*H ||| where (θ) = angular velocity (ω) determined above, H = clock hand. And A is the y-axis.
                float secHandX = (float) (Math.sin(secRotation) * secLength); // Determine the x-element of the "second" watch hand using trigonometry.
                float secHandY = (float) (-Math.cos(secRotation) * secLength); // Determine the y-element of the "second" watch hand using trigonometry. *Since the Y axis is reversed, we add the negative sign.
                canvas.drawLine(0,0,secHandX,secHandY,secHandPaint); // Paint the line on canvas based on the X & Y coordinates provided.


                // __ Decoration at the tip of the second hand watch. __
                canvas.drawCircle(secHandX,secHandY,10,newPaint);
            }


            // _____ Draw the centre circles at the origin. _____
            canvas.drawCircle(0,0,10,hourHandPaint);
            canvas.drawCircle(0,0,5,secHandPaint);


            // _____ Draw the time marking text. _____
            canvas.drawText("12",-25,-halfHeight+45,hourHandPaint); // Paint the drawing of String Text 12 on canvas.
            canvas.drawText("3",halfWidth-45,15,hourHandPaint); // Paint the drawing of String Text 3 on canvas.
            canvas.drawText("6",-10,halfHeight-25,hourHandPaint); // Paint the drawing of String Text 6 on canvas.
            canvas.drawText("9",-halfWidth+25,15,hourHandPaint); // Paint the drawing of String Text 9 on canvas.


            // _____ Draw the "logo" Text String. _____
            canvas.drawText("Sony", -30, -(hourLength-40), logo); // Paint the drawing of the logo String Text on canvas. Since the text size is 30, we subtract the x-position by 30 to centre at 0.


            // _____ Draw the "current time" Text String. _____
            int sec = time.second; // Store the current second time in a variable.
            String secText = Integer.toString(sec); // Convert that integer into a String type.
            int min = time.minute; // Store the current minute time in a variable.
            String minText = Integer.toString(min); // Convert that integer into a String type.
            int hr = time.hour; // Store the current hour time in a variable.
            String hrText = Integer.toString(hr); // Convert that integer into a String type.

            // If in Visible/Interactive mode, draw the hour & minute & second text string.
            // * Ambient mode is considered visible.
            if (isVisible() && !isInAmbientMode()) {
                canvas.drawText(secText, 30, (hourLength - 40), logo); // Paint the drawing of the second String Text on canvas. Since the text size is 30, we subtract the x-position by 30 to centre at 0.
                canvas.drawText(minText, -15, (hourLength - 40), logo); // Paint the drawing of the minute String Text on canvas. Since the text size is 30, we subtract the x-position by 30 to centre at 0.
                canvas.drawText(hrText, -60, (hourLength - 40), logo); // Paint the drawing of the hour String Text on canvas. Since the text size is 30, we subtract the x-position by 30 to centre at 0.
            }
            // If in Ambient mode, only draw the hour & minute text string.
            else if (isInAmbientMode()) {
                canvas.drawText(minText, 10, (hourLength - 40), logo); // Paint the drawing of the minute String Text on canvas. Since the text size is 30, we subtract the x-position by 30 to centre at 0.
                canvas.drawText(hrText, -40, (hourLength - 40), logo); // Paint the drawing of the hour String Text on canvas. Since the text size is 30, we subtract the x-position by 30 to centre at 0.
            }


            // __________ Sensor position dependant shape. __________
            canvas.drawCircle(xPosition,yPosition,20,secHandPaint);

            drawShapeLongPress(canvas, bounds);

            canvas.restore(); // restore transformation matrix

        }

        public void drawShapeLongPress(Canvas canvas, Rect bounds){

            if (longPressState) { // (longPressState == true)
                canvas.drawRect(-20, -20, 20, 20, tickMarkPaint);
            }
            //else if (!longPressState) { // (longPressState == false)
                //invalidate();
            //}

        }


// *** At this point (2015) onGestureListener and onDoubleTapListener interfaces do not work with watch faces. ***

/*
        // Implement the OnClickListener callback.
        @Override
        public void onClick(View v) {
            Log.i("Click", "Click");
        }


        // Implement the OnGestureListener callback methods.
        @Override
        public boolean onDown(MotionEvent e) {
            Log.i("Down", e.toString());
            return true;
        }
        @Override
        public void onShowPress(MotionEvent e) {
            Log.i("Show Press", e.toString());
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("Tap Up", e.toString());
            return true;
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i("Scroll", e1.toString()+e2.toString());
            return true;
        }
        @Override
        public void onLongPress(MotionEvent e) {

            Log.i("Long Press", e.toString());
/*
            if (e.getAction()== MotionEvent.ACTION_DOWN) { // ACTION_DOWN is when you are long pressing.
                longPressState = true;
            }

            if (e.getAction()== MotionEvent.ACTION_MOVE) { // ACTION_MOVE is when you stop long pressing.
                longPressState = false;
            }

            invalidate();
*/
/*
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.i("Fling", e1.toString()+e2.toString());
            longPressState = true;
            return true;
        }


        // Implement the OnDoubleTapListener callback.
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }


        // Implement the OnTouchListener callback.
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            Log.i("Touch", event.toString());
            longPressState = true;
            return true;
        }


        @Override
        public void setTouchEventsEnabled(boolean enabled) {
            super.setTouchEventsEnabled(enabled);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            // Detect if a gesture event occurred.
            gestureDetector.onTouchEvent(event);
            super.onTouchEvent(event);
        }
*/


    }

}


// ------------------------------- ***** How to update time? ***** ------------------------------- //
// (1) Create a handler - to schedule messages & Runnables to be executed at some point in the future (after a delay).
// (2) Implement "handleMessage" callback method - to receive messages.
// (3) Within "handleMessage" -> call "onDraw" (method that reschedules the drawing).
// (4) Within "handleMessage" -> use "sendEmptyMessageDelayed" through the created handler. <- while in the visible and interactive mode.

// !! Take into account:
// ** When WatchFace is visible and not in Ambient mode -> re-run "sendEmptyMessageDelayed" - to resume the updating of time every second.
// ** Otherwise -> "removeMessages" - to stop Handler from sending messages(stopping the time from update every second).

