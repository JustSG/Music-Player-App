package com.example.odtwarzaczmuzyki;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

public class OdtwarzaczActivity extends AppCompatActivity {
    private ImageView imageViewIkonaO;
    private TextView textViewNazwaO;
    private TextView textViewCzasBiezacy;
    private TextView textViewCzasKoniec;
    private SeekBar seekBar;
    private ImageView imageViewPauza;
    private ImageView imageViewPoprzedni;
    private ImageView imageViewNastepny;
    private ImageView imageViewWroc;
    private ImageView imageViewOpcjeO;
    private ImageView imageViewPrzyspieszenie;
    private ImageView imageViewPetla;
    private UtworDB utworDB;
    private ArrayList<Utwor> utwory;
    private Utwor obecnyUtwor;
    private MediaPlayer mediaPlayer = Odtwarzacz.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_odtwarzacz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.tlo));
        imageViewIkonaO = findViewById(R.id.imageViewIkonaO);
        textViewNazwaO = findViewById(R.id.textViewNazwaO);
        textViewCzasBiezacy = findViewById(R.id.textViewCzasBiezacy);
        textViewCzasKoniec = findViewById(R.id.textViewCzasKoniec);
        seekBar = findViewById(R.id.seekBar);
        imageViewPauza = findViewById(R.id.imageViewPauza);
        imageViewPoprzedni = findViewById(R.id.imageViewPoprzedni);
        imageViewNastepny = findViewById(R.id.imageViewNastepny);
        imageViewWroc = findViewById(R.id.imageViewWroc);
        imageViewPrzyspieszenie = findViewById(R.id.imageViewPrzyspiesszenie);
        imageViewPetla = findViewById(R.id.imageViewPetla);
        imageViewOpcjeO = findViewById(R.id.imageViewOpcjeO);
        textViewNazwaO.setSelected(true);

        utwory = (ArrayList<Utwor>) getIntent().getSerializableExtra(RecyclerViewAdapter.UTWORY);

        RoomDatabase.Callback callback = new RoomDatabase.Callback() {
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }

            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }
        };

        utworDB = Room.databaseBuilder(getApplicationContext(), UtworDB.class, "utwor").addCallback(callback).allowMainThreadQueries().build();

        przypiszDaneUtworu();

        //Kod wykonywany na głównym wątku aplikacji
        OdtwarzaczActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    textViewCzasBiezacy.setText(miliNaSek(mediaPlayer.getCurrentPosition()));
                }
                //Efekt aktualizacji interfejsu
                new Handler().postDelayed(this, 100);
                if (mediaPlayer.isPlaying()) {
                    imageViewPauza.setImageResource(R.drawable.pauza);
                } else {
                    imageViewPauza.setImageResource(R.drawable.graj);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey("CZAS_UTWORU")) {
            mediaPlayer.seekTo(savedInstanceState.getInt("CZAS_UTWORU", 0));
            float szybkosc = savedInstanceState.getFloat("SZYBKOŚĆ_UTWORU", 1.0f);
            if (szybkosc == 1.0f) {
                imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_off);
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(szybkosc));
            } else {
                imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_on);
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(szybkosc));
            }

            boolean petla = savedInstanceState.getBoolean("PETLA_UTWORU", false);
            if (petla) {
                imageViewPetla.setImageResource(R.drawable.petla_on);
                mediaPlayer.setLooping(petla);
            } else {
                imageViewPetla.setImageResource(R.drawable.petla_off);
                mediaPlayer.setLooping(petla);
            }
        }

        //Cofanie do tyłu powoduje zatrzemanie utworu
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Odtwarzacz.getInstance().reset();
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CZAS_UTWORU", mediaPlayer.getCurrentPosition());
        outState.putFloat("SZYBKOŚĆ_UTWORU", mediaPlayer.getPlaybackParams().getSpeed());
        outState.putBoolean("PETLA_UTWORU", mediaPlayer.isLooping());
    }

    void przypiszDaneUtworu() {
        obecnyUtwor = utwory.get(Odtwarzacz.index);

        textViewNazwaO.setText(obecnyUtwor.getNazwaUtworu());
        textViewCzasKoniec.setText(miliNaSek(obecnyUtwor.getCzasTrwania()));
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + utwory.get(Odtwarzacz.index).getIdAlbumu());
        Glide.with(imageViewIkonaO.getContext()).load(albumArtUri).placeholder(R.drawable.icon_pusty).error(R.drawable.icon_pusty).into(imageViewIkonaO);

        if (Odtwarzacz.index == 0) {
            imageViewPoprzedni.setVisibility(View.INVISIBLE);
        } else if (Odtwarzacz.index == utwory.size() - 1) {
            imageViewNastepny.setVisibility(View.INVISIBLE);
        } else {
            imageViewPoprzedni.setVisibility(View.VISIBLE);
        }

        imageViewNastepny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Odtwarzacz.index += 1;
                mediaPlayer.reset();
                przypiszDaneUtworu();
                imageViewPetla.setImageResource(R.drawable.petla_off);
                imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_off);
                obecnyUtwor.zwiekszLiczbeOdtworzen();
                utworDB.zwrocDao().aktualizujOdtworzenia(obecnyUtwor.getSciezkaDoPliku());
            }
        });

        imageViewPoprzedni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Odtwarzacz.index -= 1;
                mediaPlayer.reset();
                przypiszDaneUtworu();
                imageViewPetla.setImageResource(R.drawable.petla_off);
                imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_off);
                obecnyUtwor.zwiekszLiczbeOdtworzen();
                utworDB.zwrocDao().aktualizujOdtworzenia(obecnyUtwor.getSciezkaDoPliku());
            }
        });

        imageViewPauza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            }
        });

        imageViewWroc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
                mediaPlayer.stop();
            }
        });

        imageViewPrzyspieszenie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(OdtwarzaczActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_czas, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.x05) {
                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(0.5f));
                            imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_on);
                            return true;
                        } else if (menuItem.getItemId() == R.id.x10) {
                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1f));
                            imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_off);
                            return true;
                        } else if (menuItem.getItemId() == R.id.x12) {
                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1.2f));
                            imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_on);
                            return true;
                        } else if (menuItem.getItemId() == R.id.x15) {
                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(1.5f));
                            imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_on);
                            return true;
                        } else if (menuItem.getItemId() == R.id.x20) {
                            mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(2f));
                            imageViewPrzyspieszenie.setImageResource(R.drawable.przyspieszenie_on);
                            return true;
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        imageViewOpcjeO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(OdtwarzaczActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_opcje, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.wiecejInfo) {
                            Intent intent = new Intent(view.getContext(), InfoActivity.class);
                            intent.putExtra(RecyclerViewAdapter.UTWORY, utwory);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            }
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        imageViewPetla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isLooping()) {
                    mediaPlayer.setLooping(true);
                    imageViewPetla.setImageResource(R.drawable.petla_on);
                    Toast.makeText(OdtwarzaczActivity.this, "Ponowne odtwarzanie włączone", Toast.LENGTH_SHORT).show();
                } else if (mediaPlayer.isLooping()) {
                    mediaPlayer.setLooping(false);
                    imageViewPetla.setImageResource(R.drawable.petla_off);
                    Toast.makeText(OdtwarzaczActivity.this, "Ponowne odtwarzanie wyłączone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        odtworzUtwor();
    }

    public static String miliNaSek(long milisekundy) {
        int liczbaSekund = (int) (milisekundy / 1000);
        int s60 = liczbaSekund % 60;
        int h60 = liczbaSekund / 3600;
        int m60 = (liczbaSekund - h60 * 3600) / 60;
        return String.format("%02d:%02d", m60, s60);
    }

    private void odtworzUtwor() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(obecnyUtwor.getSciezkaDoPliku());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        seekBar.setProgress(0);
        seekBar.setMax(mediaPlayer.getDuration());
    }
}