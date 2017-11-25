package net.zoomtune.zoomtune.zoom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import net.zoomtune.zoomtune.R;

import java.util.HashSet;

public class CircleView extends View {

    private static final String TAG = CircleView.class.getSimpleName();

    private boolean resy = false;
    private Rect mMeasuredRect = new Rect();

    /** Stores data about single circle */
    public static class CircleArea {
        int radius;
        int centerX;
        int centerY;

        CircleArea(int centerX, int centerY, int radius) {
            this.radius = radius;
            this.centerX = centerX;
            this.centerY = centerY;
        }

        @Override
        public String toString() {
            return "Circle[" + centerX + ", " + centerY + ", " + radius + "]";
        }
    }

    /** Paint to draw circles */
    private Paint mCirclePaint;
    private int radius;
    private static int height;
    private static int width;

    public static int get_height() {
        return height;
    }

    public static int get_width() {
        return width;
    }

    private static final int CIRCLES_LIMIT = 1;

    /** All available circles */
    public static HashSet<CircleArea> mCircles = new HashSet<>(CIRCLES_LIMIT);
    private SparseArray<CircleArea> mCirclePointer = new SparseArray<>(CIRCLES_LIMIT);

    public static CircleArea getCircle () {
        for (CircleArea circle: mCircles){
            return circle;
        }
        return null;
    }

    /**
     * Default constructor
     *
     * @param ct {@link android.content.Context}
     */
    public CircleView(final Context ct) {
        super(ct);

        init(ct);
    }

    public CircleView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);

        init(ct);
    }

    public CircleView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);

        init(ct);
    }

    private void init(final Context context) {
        // Generate bitmap used for background
        //mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.up_image);

        mCirclePaint = new Paint();

        mCirclePaint.setStrokeWidth(10);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setShader(new LinearGradient(0, 0, 120, getHeight(),
                ContextCompat.getColor(context, R.color.colorPurplePrimary),
                ContextCompat.getColor(context, R.color.colorPrimary),
                Shader.TileMode.MIRROR));
    }

    @Override
    public void onDraw(final Canvas canv) {
        // background bitmap to cover all area
        //canv.drawBitmap(GlobalVariables.getBitmap(), null, mMeasuredRect, null);

        if (radius == 0) {
            radius = this.getHeight() / 15;
            height = this.getHeight();
            width = this.getWidth();

        }
        if (!resy) {
            resy = true;
            long downTime = System.currentTimeMillis();
            long eventTime = downTime + 1;
            Log.d("MIN TO PIRAKSEIS", String.valueOf(resy));
            MotionEvent motionEvent = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_DOWN,
                    width / 5,
                    height / 5,
                    0
            );

            // Dispatch touch event to view
            this.dispatchTouchEvent(motionEvent);
        }

        for (CircleArea circle : mCircles) {
            canv.drawCircle(circle.centerX, circle.centerY, circle.radius, mCirclePaint);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        CircleArea touchedCircle;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                Log.w(TAG, "Action_Down");
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;

                if (xTouch >= radius){
                    if (xTouch <= width - radius){
                        touchedCircle.centerX = xTouch;
                    }
                    else {
                        touchedCircle.centerX = width - radius;
                    }
                }
                else {
                    touchedCircle.centerX = radius;
                }
                if (yTouch >= radius){
                    if (yTouch <= height - radius){
                        touchedCircle.centerY = yTouch;
                    }
                    else {
                        touchedCircle.centerY = height - radius;
                    }
                }
                else {
                    touchedCircle.centerY = radius;
                }
                touchedCircle.radius = radius;
                mCirclePointer.put(event.getPointerId(0), touchedCircle);
                Log.w(TAG, "Touch: " + xTouch + " - " + yTouch);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedCircle = mCirclePointer.get(pointerId);

                    if (null != touchedCircle) {
                        if (xTouch >= radius && xTouch <= width - radius){
                            touchedCircle.centerX = xTouch;
                        }
                        if (yTouch >= radius && yTouch <= height - radius){
                            touchedCircle.centerY = yTouch;
                        }
                    }
                    Log.w(TAG, "Move: " + xTouch + " - " + yTouch);
                }

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                mCirclePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all CircleArea - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");

        mCirclePointer.clear();
    }

    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     *
     * @return obtained {@link CircleArea}
     */
    private CircleArea obtainTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touchedCircle = getTouchedCircle(xTouch, yTouch);

        if (null == touchedCircle) {
            touchedCircle = new CircleArea(xTouch, yTouch, radius);

            if (mCircles.size() == CIRCLES_LIMIT) {
                Log.w(TAG, "Clear all circles, size is " + mCircles.size());
                // remove first circle
                mCircles.clear();
            }

            Log.w(TAG, "Added circle " + touchedCircle);
            mCircles.add(touchedCircle);
        }

        return touchedCircle;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link CircleArea} touched circle or null if no circle has been touched
     */
    private CircleArea getTouchedCircle(final int xTouch, final int yTouch) {
        CircleArea touched = null;

        for (CircleArea circle : mCircles) {
            if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                touched = circle;
                break;
            }
        }

        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int squareSize = getMeasuredWidth();
        mMeasuredRect.left = 0;
        mMeasuredRect.top = 0;
        mMeasuredRect.right = squareSize;
//        mMeasuredRect.bottom = getMeasuredHeight();
        if (ZoomActivity.getSizeX() == ZoomActivity.getSizeY()) {
            setMeasuredDimension(squareSize, squareSize);
            mMeasuredRect.bottom = squareSize;
        }
    }
}
