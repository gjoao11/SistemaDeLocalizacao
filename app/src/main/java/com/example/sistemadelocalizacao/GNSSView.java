package com.example.sistemadelocalizacao;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.GnssStatus;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

public class GNSSView extends View {
    private GnssStatus gnssStatus = null;
    private int r;
    private int height, width;
    private final Paint paint = new Paint();
    private SharedPreferences sharedPreferences;

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

    private Set<Integer> allowedGNSS = Set.of(
            GnssStatus.CONSTELLATION_GPS,
            GnssStatus.CONSTELLATION_GLONASS,
            GnssStatus.CONSTELLATION_GALILEO,
            GnssStatus.CONSTELLATION_BEIDOU
    );

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

        sharedPreferences = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        boolean gpsAllowed =  sharedPreferences.getBoolean("GPS", true);
        boolean glonassAllowed = sharedPreferences.getBoolean("GLONASS", true);
        boolean galileoAllowed = sharedPreferences.getBoolean("GALILEO", true);
        boolean beidouAllowed = sharedPreferences.getBoolean("BEIDOU", true);
        boolean notUsedInFixAllowed = sharedPreferences.getBoolean("notUsedInFix", true);

        paint.setStyle(Paint.Style.FILL);

        if (gnssStatus != null) {
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                int constellationType = gnssStatus.getConstellationType(i);

                boolean usedInFix = gnssStatus.usedInFix(i);
                if (!allowedGNSS.contains(constellationType)) continue;
                if (!notUsedInFixAllowed && !usedInFix) continue;
                if (!gpsAllowed && constellationType == GnssStatus.CONSTELLATION_GPS) continue;
                if (!glonassAllowed && constellationType == GnssStatus.CONSTELLATION_GLONASS) continue;
                if (!galileoAllowed && constellationType == GnssStatus.CONSTELLATION_GALILEO) continue;
                if (!beidouAllowed && constellationType == GnssStatus.CONSTELLATION_BEIDOU) continue;

                switch (constellationType) {
                    case GnssStatus.CONSTELLATION_GPS:
                        paint.setColor(Color.parseColor("#E40066"));
                        break;
                    case GnssStatus.CONSTELLATION_GLONASS:
                        paint.setColor(Color.parseColor("#345995"));
                        break;
                    case GnssStatus.CONSTELLATION_GALILEO:
                        paint.setColor(Color.parseColor("#4D8B31"));
                        break;
                    case GnssStatus.CONSTELLATION_BEIDOU:
                        paint.setColor(Color.parseColor("#D36135"));
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

                canvas.drawCircle(computeXc(x), computeYc(y), 12, paint);

                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(30);
                String satID = gnssStatus.getSvid(i) + "";
                canvas.drawText(satID, computeXc(x) + 16, computeYc(y) + 10, paint);

                if (usedInFix) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.BLACK);
                    canvas.drawCircle(computeXc(x), computeYc(y), 14, paint);
                    paint.setStyle(Paint.Style.FILL);
                }
            }
        }
    }
}