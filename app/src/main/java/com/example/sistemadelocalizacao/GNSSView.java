package com.example.sistemadelocalizacao;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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

    // Variável para armazenar cor do mapa definida no layout xml
    private int mapColor;

    public GNSSView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GNSSView,
                0, 0);

        try {
            // Obter valor de map color definido no layout xml
            mapColor = a.getColor(R.styleable.GNSSView_mapColor, Color.LTGRAY);
        } finally {
            a.recycle();
        }
    }

    // Métedo para definir o raio do radar baseado no tamanho e orientação da tela
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (width < height) {
            // Se estiver na vertical, usar metade da largura
            r = (int) (width / 2 * 0.9);
        } else {
            // Se estiver na horizontal, usar metade da altura
            r = (int) (height / 2 * 0.9);
        }
    }

    // Atualizar status do GNSS para redesenhar o radar com as novas informações
    public void newStatus(GnssStatus gnssStatus) {
        this.gnssStatus = gnssStatus;
        invalidate();
    }

    // Obter coordenada central do eixo x do radar
    private int computeXc(double x) {
        return (int) (x + width / 2);
    }

    // Obter coordenada central do eixo y do radar
    private int computeYc(double y) {
        return (int) (-y + height / 2);
    }

    // Set com as constelações de satélites permitidas
    private Set<Integer> allowedGNSS = Set.of(
            GnssStatus.CONSTELLATION_GPS,
            GnssStatus.CONSTELLATION_GLONASS,
            GnssStatus.CONSTELLATION_GALILEO,
            GnssStatus.CONSTELLATION_BEIDOU
    );

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        // Configurar os estilos para a geração do radar
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(mapColor);

        // Desenhar os círculos do radar
        int radius = r;
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(45)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);
        radius = (int) (radius * Math.cos(Math.toRadians(60)));
        canvas.drawCircle(computeXc(0), computeYc(0), radius, paint);

        // Desenhar as linhas do radar
        canvas.drawLine(computeXc(0), computeYc(-r), computeXc(0), computeYc(r), paint);
        canvas.drawLine(computeXc(-r), computeYc(0), computeXc(r), computeYc(0), paint);

        sharedPreferences = getContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        // Obter configurações de quais opções estão selecionadas pelo usuário para serem exibidas
        boolean gpsAllowed =  sharedPreferences.getBoolean("GPS", true);
        boolean glonassAllowed = sharedPreferences.getBoolean("GLONASS", true);
        boolean galileoAllowed = sharedPreferences.getBoolean("GALILEO", true);
        boolean beidouAllowed = sharedPreferences.getBoolean("BEIDOU", true);
        boolean notUsedInFixAllowed = sharedPreferences.getBoolean("notUsedInFix", true);

        // Mudar estilo de pintura para preencher o círculo
        paint.setStyle(Paint.Style.FILL);

        // Se o status do GNSS estiver disponível, desenhar os satélites
        if (gnssStatus != null) {
            for (int i = 0; i < gnssStatus.getSatelliteCount(); i++) {
                // Obter de qual constelação é o satélite
                int constellationType = gnssStatus.getConstellationType(i);

                // Obter informação se o satélite é usado no fix
                boolean usedInFix = gnssStatus.usedInFix(i);

                // Se não estiver permitido, pular para o próximo satélite
                if (!allowedGNSS.contains(constellationType)) continue;

                // Se ele não for usado no fix e os não usados no fix não estiverem permitidos, pular para o próximo satélite
                if (!notUsedInFixAllowed && !usedInFix) continue;

                // Verificar se a constelação do satélite atual está permitida, se não pular para o próximo
                if (!gpsAllowed && constellationType == GnssStatus.CONSTELLATION_GPS) continue;
                if (!glonassAllowed && constellationType == GnssStatus.CONSTELLATION_GLONASS) continue;
                if (!galileoAllowed && constellationType == GnssStatus.CONSTELLATION_GALILEO) continue;
                if (!beidouAllowed && constellationType == GnssStatus.CONSTELLATION_BEIDOU) continue;

                // Mudar cor da pintura para cada tipo de constelação
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
                    default:
                        break;
                }

                // Obter informações de localização e posição do satélite
                float az = gnssStatus.getAzimuthDegrees(i);
                float el = gnssStatus.getElevationDegrees(i);
                float x = (float) (r * Math.cos(Math.toRadians(el)) * Math.sin(Math.toRadians(az)));
                float y = (float) (r * Math.cos(Math.toRadians(el)) * Math.cos(Math.toRadians(az)));

                // Desenhar ponto do satélite no radar
                canvas.drawCircle(computeXc(x), computeYc(y), 12, paint);

                // Desenhar texto com o número do satélite
                paint.setTextAlign(Paint.Align.LEFT);
                paint.setTextSize(30);
                String satID = gnssStatus.getSvid(i) + "";
                canvas.drawText(satID, computeXc(x) + 16, computeYc(y) + 10, paint);

                // Se o satélite é usado no fix, adicionar uma borda preta em volta do círculo
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
