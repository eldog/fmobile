package uk.me.eldog.fface;

import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PointF;
import android.media.FaceDetector.Face;

public class FaceRect extends RectF
{
    private RectF mRectF;

    public FaceRect(Face face)
    {
        super();
        setFace(face);
    } // FaceRectF

    public void setFace(Face face)
    {
        PointF eyesMidPoint = new PointF();
        face.getMidPoint(eyesMidPoint);
        float eyesDistance = face.eyesDistance();
        eyesDistance *= 2;
        float x = eyesMidPoint.x;
        float y = eyesMidPoint.y;
        left = x - eyesDistance;
        top = y - eyesDistance; 
        right = x + eyesDistance;
        bottom = y + eyesDistance;
    } // setFace

    public FaceRect()
    {
        super();
    } // FaceRect

    public FaceRect(float left, float top, float right, float bottom)
    {
        super(left, top, right, bottom);
    } // FaceRect

    public FaceRect(RectF rect)
    {
        super(rect);
    } // FaceRect

    public FaceRect(Rect rect)
    {
        super(rect);
    } // FaceRect

} // FaceRect

