package com.example.sistemadelocalizacao;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GNSSView extends View {
    private GnssStatus gnssStatus = null;
    private int r;
    private int height, width;
    private final Paint paint = new Paint();

    public GNSSView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (width < height) {
            r = (int) (width / 2 * 0.9);
        } else {
            r = (int) (height / 2 * 0.9);
        }
    }

    public void newStatus(GnssStatus gnssStatus) {
        this.gnssStatus = gnssStatus;
        invalidate();
    }

    private int computeXc(double x) {
        return (int) (x + width / 2);
    }

    private int computeYc(double y) {
        return (int) (-y + height / 2);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(Color.LTGRAY);

        int radius = r;
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(45)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(60)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);

        canvas.drawLine(computeXc(0), computeYc(-r), computeXc(0), computeYc(r), paint);
        canvas.drawLine(computeXc(-r), computeYc(0), computeXc(r), computeYc(0), paint);

        paint.setStyle(Paint.Style.FILL);

        if (gnssStatus != null) {
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                int constellationType = gnssStatus.getConstellationType(i);
                boolean usedInFix = gnssStatus.usedInFix(i);

                switch (constellationType) {
                    case GnssStatus.CONSTELLATION_GPS:
                        paint.setColor(Color.BLUE);
                        break;
                    case GnssStatus.CONSTELLATION_GLONASS:
                        paint.setColor(Color.RED);
                        break;
                    case GnssStatus.CONSTELLATION_GALILEO:
                        paint.setColor(Color.MAGENTA);
                        break;
                    case GnssStatus.CONSTELLATION_BEIDOU:
                        paint.setColor(Color.GREEN);
                        break;
                    // Adicione outros casos se necessário (QZSS, SBAS, etc.)
                    default:
                        paint.setColor(Color.GRAY); // Cor padrão para outros sistemas
                        break;
                }

                float az = gnssStatus.getAzimuthDegrees(i);
                float el = gnssStatus.getElevationDegrees(i);
                float x = (float) (r * Math.cos(Math.toRadians(el)) * Math.sin(Math.toRadians(az)));
                float y = (float) (r * Math.cos(Math.toRadians(el)) * Math.cos(Math.toRadians(az)));

                if (!usedInFix) {
                    canvas.drawCircle(computeXc(x), computeYc(y), 10, paint);
                } else {
                    canvas.drawRect(computeXc(x) - 15, computeYc(y) - 10, computeXc(x) + 5, computeYc(y) + 10, paint);
                }

                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(30);
                String satID = gnssStatus.getSvid(i) + "";
                canvas.drawText(satID, computeXc(x) + 10, computeYc(y) + 10, paint);
            }
        }
    }
}
