package com.michaelpardo.android.widget.chartview;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class LinearSeries extends AbstractSeries {
    private PointF mLastPoint;

    public LinearSeries() {
    }

    public LinearSeries(Parcel in) {
        super(in);
    }

    @Override
    public void drawPoint(Canvas canvas, AbstractPoint point, float scaleX, float scaleY, Rect gridBounds) {
        final float x = (float) (gridBounds.left + (scaleX * (point.getX() - getMinX())));
        final float y = (float) (gridBounds.bottom - (scaleY * (point.getY() - getMinY())));

        if (mLastPoint != null) {
            canvas.drawLine(mLastPoint.x, mLastPoint.y, x, y, mPaint);
        } else {
            mLastPoint = new PointF();
        }

        mLastPoint.set(x, y);
    }

    @Override
    protected void onDrawingComplete() {
        mLastPoint = null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class LinearPoint extends AbstractPoint {
        public LinearPoint() {
            super();
        }

        public LinearPoint(double x, double y) {
            super(x, y);
        }
    }

    public static final Parcelable.Creator<LinearSeries> CREATOR
            = new Parcelable.Creator<LinearSeries>() {
        public LinearSeries createFromParcel(Parcel in) {
            return new LinearSeries(in);
        }

        public LinearSeries[] newArray(int size) {
            return new LinearSeries[size];
        }
    };

}