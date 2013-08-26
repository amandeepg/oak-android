package com.michaelpardo.android.widget.chartview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSeries implements Parcelable {
    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE MEMBERS
    //////////////////////////////////////////////////////////////////////////////////////

    protected Paint mPaint = new Paint();

    private List<AbstractPoint> mPoints;
    private boolean mPointsSorted = false;

    private double mMinX = Double.MAX_VALUE;
    private double mMaxX = Double.MIN_VALUE;
    private double mMinY = Double.MAX_VALUE;
    private double mMaxY = Double.MIN_VALUE;

    private double mRangeX = 0;
    private double mRangeY = 0;

    protected abstract void drawPoint(Canvas canvas, AbstractPoint point, float scaleX, float scalY, Rect gridBounds);

    //////////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTORS
    //////////////////////////////////////////////////////////////////////////////////////

    public AbstractSeries() {
        mPaint.setAntiAlias(true);
    }

    private AbstractSeries(Parcel in) {
        this();

        in.readList(mPoints, null);

        mMinX = in.readDouble();
        mMaxX = in.readDouble();
        mMinY = in.readDouble();
        mMaxY = in.readDouble();

        mRangeX = in.readDouble();
        mRangeY = in.readDouble();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("UnusedDeclaration")
    public List<AbstractPoint> getPoints() {
        Collections.sort(mPoints);
        return mPoints;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPoints(List<? extends AbstractPoint> points) {
        mPoints = new ArrayList<AbstractPoint>();
        mPoints.addAll(points);

        sortPoints();
        resetRange();

        for (AbstractPoint point : mPoints) {
            extendRange(point.getX(), point.getY());
        }
    }

    public void addPoint(AbstractPoint point) {
        if (mPoints == null) {
            mPoints = new ArrayList<AbstractPoint>();
        }

        extendRange(point.getX(), point.getY());
        mPoints.add(point);

        mPointsSorted = false;
    }

    // Line properties

    public void setLineColor(int color) {
        mPaint.setColor(color);
    }

    public void setLineWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS
    //////////////////////////////////////////////////////////////////////////////////////

    private void sortPoints() {
        if (!mPointsSorted) {
            if (mPoints != null) {
                Collections.sort(mPoints);
            }
            mPointsSorted = true;
        }
    }

    private void resetRange() {
        mMinX = Double.MAX_VALUE;
        mMaxX = Double.MIN_VALUE;
        mMinY = Double.MAX_VALUE;
        mMaxY = Double.MIN_VALUE;

        mRangeX = 0;
        mRangeY = 0;
    }

    private void extendRange(double x, double y) {
        if (x < mMinX) {
            mMinX = x;
        }

        if (x > mMaxX) {
            mMaxX = x;
        }

        if (y < mMinY) {
            mMinY = y;
        }

        if (y > mMaxY) {
            mMaxY = y;
        }

        mRangeX = mMaxX - mMinX;
        mRangeY = mMaxY - mMinY;
    }

    public void setRange(double mMinX, double mMaxX, double mMinY, double mMaxY) {
        this.mMinX = mMinX;
        this.mMinY = mMinY;
        this.mMaxX = mMaxX;
        this.mMaxY = mMaxY;

        mRangeX = mMaxX - mMinX;
        mRangeY = mMaxY - mMinY;
    }

    public double getMinX() {
        return mMinX;
    }

    public double getMaxX() {
        return mMaxX;
    }

    public double getMinY() {
        return mMinY;
    }

    public double getMaxY() {
        return mMaxY;
    }

    @SuppressWarnings("UnusedDeclaration")
    double getRangeX() {
        return mRangeX;
    }

    @SuppressWarnings("UnusedDeclaration")
    double getRangeY() {
        return mRangeY;
    }

    void draw(Canvas canvas, Rect gridBounds, RectD valueBounds) {
        sortPoints();

        final float scaleX = (float) gridBounds.width() / (float) valueBounds.width();
        final float scaleY = (float) gridBounds.height() / (float) valueBounds.height();

        if (mPoints != null) {
            for (AbstractPoint point : mPoints) {
                drawPoint(canvas, point, scaleX, scaleY, gridBounds);
            }
        }

        onDrawingComplete();
    }

    protected void onDrawingComplete() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeList(mPoints);

        out.writeDouble(mMinX);
        out.writeDouble(mMaxX);
        out.writeDouble(mMinY);
        out.writeDouble(mMaxY);

        out.writeDouble(mRangeX);
        out.writeDouble(mRangeY);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC CLASSES
    //////////////////////////////////////////////////////////////////////////////////////

    public static abstract class AbstractPoint implements Comparable<AbstractPoint>,
            Parcelable {
        private double mX;
        private double mY;

        public AbstractPoint() {
        }

        private AbstractPoint(Parcel in) {
            this();
            mX = in.readDouble();
            mY = in.readDouble();
        }

        public AbstractPoint(double x, double y) {
            mX = x;
            mY = y;
        }

        public double getX() {
            return mX;
        }

        public double getY() {
            return mY;
        }

        public void set(double x, double y) {
            mX = x;
            mY = y;
        }

        @Override
        public int compareTo(AbstractPoint another) {
            return Double.compare(mX, another.mX);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeDouble(mX);
            out.writeDouble(mY);
        }
    }
}