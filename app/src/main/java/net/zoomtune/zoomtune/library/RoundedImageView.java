package net.zoomtune.zoomtune.library;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RoundedImageView extends android.support.v7.widget.AppCompatImageView {

    Path clipPath ;
    RectF rect ;

    public RoundedImageView(Context context) {
        super(context);
        clipPath = new Path();
        rect = new RectF();
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        clipPath = new Path();
        rect = new RectF();
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        clipPath = new Path();
        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = 25.0f;

        rect.set(0, 0, this.getWidth(), this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        super.onDraw(canvas);
    }
}