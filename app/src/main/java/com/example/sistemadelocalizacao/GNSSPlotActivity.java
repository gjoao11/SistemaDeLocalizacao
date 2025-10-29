package com.example.sistemadelocalizacao;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.stream.IntStream;

public class GNSSPlotActivity extends AppCompatActivity {
    // Definir código para identificar solicitação de permissão da localização
    private static final int REQUEST_LOCATION_UPDATES = 1;
    private LocationManager locationManager;
    LocationListener locationListener;
    GnssStatus.Callback gnssCallback;

    // View para exibir o radar de satélites
    GNSSView gnssView;

    // TextView para exibir quantidade total de satélites
    private TextView quantityVisibleTextView;
    // TextView para exibir quantidade total de satélites usados no fix
    private TextView quantityInFixTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gnss_plot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gnssView = findViewById(R.id.GNSSViewId);

        quantityVisibleTextView = findViewById(R.id.text_quantity_value);
        quantityInFixTextView = findViewById(R.id.text_fix_value);

        startGnssUpdate();
        gnssView.setOnClickListener(v -> {
            Intent i = new Intent(GNSSPlotActivity.this, DialogConfigActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Caso a app seja parada, para atualização de localização
        stopGNSSUpdate();
    }

    public void startGnssUpdate() {
        // Se a permissão foi dada, ativa a chamada para atualizações da localização
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        ) {
            // Recebe uma instância de uma classe anônima que implementa LocationListener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // Processa nova localização
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                }
            };

            // Informa:
            // Provedor de localização,
            // Intervalo de tempo mínimo em ms para as atualizações de localização
            // Distância mínima em metros para as atualizações de localização
            // Objeto que irá processar as atualizações de localização
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,   // Provedor de localização
                    1000,                           // Intervalo mínimo (ms)
                    0,                              // Distância mínima (m)
                    locationListener                // Escutador de atualizações
            );

            // Recebe uma instância de uma classe anônima que implementa GnssStatus.Callback
            gnssCallback = new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    super.onSatelliteStatusChanged(status);
                    // Processa as informações do sistema de satélite
                    gnssView.newStatus(status);

                    // Obter número de satélites
                    int satelliteCount = status.getSatelliteCount();

                    // Obter número de satélites usados no fix
                    int satellitesInFixCount = (int) IntStream.range(0, satelliteCount).filter(status::usedInFix).count();

                    // Recebemos o seguinte erro ao fazer o for normal:
                    // Variable used in lambda expression should be final or effectively final
                    // E alteramos para o código sugerido pela IDE
                    // Código original:
                    // Obter número de satélites usados no fix
                    // int satellitesInFixCount = 0;
                    // Percorre cada satélite usando seu índice, de 0 até o total de satélites
                    // for (int i = 0; i < satelliteCount; i++) {
                    //      Para cada satélite (i), verifica se ele está sendo usado no fix
                    //      if (status.usedInFix(i)) {
                    //          Se estiver, incrementa o contador
                    //          satellitesInFixCount++;
                    //      }
                    // }

                    // Atualiza o TextView de satélites visíveis
                    quantityVisibleTextView.setText(String.format("%d", satelliteCount));

                    // Atualiza o TextView de satélites em uso
                    quantityInFixTextView.setText(String.format("%d", satellitesInFixCount));
                }
            };
            // informa o escutador do sistema de satelites e a thread para processar essas infos
            locationManager.registerGnssStatusCallback(gnssCallback, new Handler(Looper.getMainLooper()));
        } else {
            // Se a permissão não foi dada, solicita ao usuário
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }

    public void stopGNSSUpdate() {
        // desliga a GnssCallback
        if (gnssCallback != null) {
            try {
                locationManager.unregisterGnssStatusCallback(gnssCallback);
            } catch (Exception e) {
                e.printStackTrace(); // evita crash se já tiver sido desregistrado
            }
        }
        // desliga o location listener
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Verificar se o código da solicitação é o código definido para a solicitação de permissão da localização
        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Se o usuário deu a permissão solicitava, ativa a chamada para atualizações da localização
                startGnssUpdate();
            } else {
                // Se o usuário não deu a permissão solicitava, encerra a atividade
                Toast.makeText(
                        this,
                        "Sem permissão para mostrar informações do sistema GNSS",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }
        }
    }
}