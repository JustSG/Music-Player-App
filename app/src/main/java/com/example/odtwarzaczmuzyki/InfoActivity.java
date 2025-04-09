package com.example.odtwarzaczmuzyki;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class InfoActivity extends AppCompatActivity {
    private ArrayList<Utwor> utwory;
    private ImageView imageViewWrocI;
    private ImageView imageViewIkonaI;
    private TextView textViewNazwaI;
    private TextView textViewWykonawcaI;
    private TextView textViewCzasI;
    private TextView textViewTypI;
    private TextView textViewRozmiarI;
    private TextView textViewSciezkaI;
    private Utwor obecnyUtwor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.tlo));
        imageViewWrocI = findViewById(R.id.imageViewWrocI);
        imageViewIkonaI = findViewById(R.id.imageViewIkonaI);
        textViewNazwaI = findViewById(R.id.textViewNazwaI);
        textViewWykonawcaI = findViewById(R.id.textViewWykonawcaI);
        textViewCzasI = findViewById(R.id.textViewCzasI);
        textViewTypI = findViewById(R.id.textViewTypI);
        textViewRozmiarI = findViewById(R.id.textViewRozmiarI);
        textViewSciezkaI = findViewById(R.id.textViewSciezkaI);

        utwory = (ArrayList<Utwor>) getIntent().getSerializableExtra(RecyclerViewAdapter.UTWORY);

        przypiszDane();

        imageViewWrocI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void przypiszDane() {
        obecnyUtwor = utwory.get(Odtwarzacz.index);

        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + obecnyUtwor.getIdAlbumu());
        Glide.with(imageViewIkonaI.getContext()).load(albumArtUri).placeholder(R.drawable.icon_pusty).error(R.drawable.icon_pusty).into(imageViewIkonaI);
        textViewNazwaI.setText(obecnyUtwor.getNazwaUtworu());
        textViewWykonawcaI.setText(obecnyUtwor.getWykonawca());
        textViewCzasI.setText(OdtwarzaczActivity.miliNaSek(obecnyUtwor.getCzasTrwania()));
        textViewTypI.setText(obecnyUtwor.getTyp());
        textViewRozmiarI.setText(bajtyNaMega(obecnyUtwor.getRozmiar()));
        textViewSciezkaI.setText(obecnyUtwor.getSciezkaDoPliku());
    }

    public String bajtyNaMega(long bajty) {
        double mega = (double) bajty / (1024.0 * 1024.0);
        return String.format("%.2f MB", mega);
    }
}