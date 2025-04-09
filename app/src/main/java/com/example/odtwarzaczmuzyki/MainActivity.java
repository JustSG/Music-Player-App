package com.example.odtwarzaczmuzyki;

import android.content.pm.PackageManager;
import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Utwor> utwory = new ArrayList<>();
    private UtworDB utworDB;
    private List<Integer> odtworzeniaList = new ArrayList<>();
    private TextView textViewIloscPlikow;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private ImageView imageViewSortowanie;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.tlo));
        textViewIloscPlikow = findViewById(R.id.textViewIloscPlikow);
        recyclerView = findViewById(R.id.recyclerViewUtwory);
        imageViewSortowanie = findViewById(R.id.imageViewSortowanie);

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

        if (!sprawdzUprawnienie()) {
            poprosOUrpawnienie();
        }

        utworDB = Room.databaseBuilder(getApplicationContext(), UtworDB.class, "utwor").addCallback(callback).allowMainThreadQueries().build();
        pobierzUtwory();
        synchronizujBazeZTelefonem();
        odczytajOdtworzeniaZBazy();

        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this, utwory, odtworzeniaList, utworDB);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);

        imageViewSortowanie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu_sortowanie, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.sortNazwa) {
                            utwory.sort((u1, u2) -> u1.getNazwaUtworu().compareToIgnoreCase(u2.getNazwaUtworu()));
                            recyclerViewAdapter.notifyDataSetChanged();
                            return true;
                        } else if (menuItem.getItemId() == R.id.sortDlugosc) {
                            utwory.sort((u1, u2) -> Long.compare(u2.getCzasTrwania(), u1.getCzasTrwania()));
                            recyclerViewAdapter.notifyDataSetChanged();
                            return true;
                        } else if (menuItem.getItemId() == R.id.sortData) {
                            utwory.sort((u1, u2) -> Long.compare(u2.getDateAdded(), u1.getDateAdded()));
                            recyclerViewAdapter.notifyDataSetChanged();
                            return true;
                        } else if (menuItem.getItemId() == R.id.sortOdtworzenia) {
                            utwory.sort((u1, u2) -> Integer.compare(u2.getOdtworzenia(), u1.getOdtworzenia()));
                            recyclerViewAdapter.notifyDataSetChanged();
                            return true;
                        } else if (menuItem.getItemId() == R.id.sortWykonawca) {
                            utwory.sort((u1, u2) -> u2.getWykonawca().compareToIgnoreCase(u1.getWykonawca()));
                            recyclerViewAdapter.notifyDataSetChanged();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }

    private void pobierzUtwory() {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String filtrPlikow = MediaStore.Audio.Media.IS_MUSIC;

        //Zapytanie do bazy MediaStore: uri | wszystkie kolumny z bazy | tylko pliki muzyczka | bez filtrów | zwykłe wypisywanie bez sortowania
        Cursor cursor = getContentResolver().query(uri, null, filtrPlikow, null, null);
        if (cursor != null) {
            int nazwaUtworuIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int wykonawcaIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int czasTrwaniaIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int sciezkaDoPlikuIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int idAlbumuIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int dataDodaniaIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            int nazwaAlbumuIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int typIndex = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int rozmiarIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
            int i = 0;

            while (cursor.moveToNext()) {
                String title = cursor.getString(nazwaUtworuIndex);
                String artist = cursor.getString(wykonawcaIndex);
                long duration = cursor.getLong(czasTrwaniaIndex);
                String data = cursor.getString(sciezkaDoPlikuIndex);
                long albumId = cursor.getLong(idAlbumuIndex);
                long dataDodania = cursor.getLong(dataDodaniaIndex);
                String nazwaAlbumu = cursor.getString(nazwaAlbumuIndex);
                String typ = cursor.getString(typIndex);
                long rozmiar = cursor.getLong(rozmiarIndex);
                int odtworzenia = (i < odtworzeniaList.size()) ? odtworzeniaList.get(i) : 0;

                utwory.add(new Utwor(title, artist, duration, data, albumId, dataDodania, odtworzenia, nazwaAlbumu, typ, rozmiar));
                //dodajOdtworzeniaDoBazy(new Utwor(title, artist, duration, data, albumId, dataDodania, odtworzenia, nazwaAlbumu, typ, rozmiar));
                i++;
            }

            if (utwory.isEmpty()) {
                textViewIloscPlikow.setText("Brak plików mp3.");
            } else {
                textViewIloscPlikow.setText("Utwory: " + utwory.size());
            }
            cursor.close();
        }
    }

    private void dodajOdtworzeniaDoBazy(Utwor utwor) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                utworDB.zwrocDao().wstawDoBazy(utwor);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Wstawiono do bazy", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void odczytajOdtworzeniaZBazy() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List<Integer> pobraneOdtworzenia = utworDB.zwrocDao().zwrocOdtworzeniaZBazy();
                odtworzeniaList.addAll(pobraneOdtworzenia);
                while (odtworzeniaList.size() < utwory.size()) {
                    odtworzeniaList.add(0);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void synchronizujBazeZTelefonem() {
        //Pobirea liste utworów z telefonu
        List<String> sciezkiUtworowZTelefonu = new ArrayList<>();
        for (Utwor utworek : utwory) {
            sciezkiUtworowZTelefonu.add(utworek.getSciezkaDoPliku());
        }

        //Pobirea liste utworów z bazy danych
        List<Utwor> utworyZBazy = utworDB.zwrocDao().zwrocWszystkieUtwory();

        //Jeżeli nie ma....dodaje nowe utwory
        for (Utwor utworek : utwory) {
            boolean czyIstniejeWBazie = false;
            for (Utwor utworZBazy : utworyZBazy) {
                if (utworZBazy.getSciezkaDoPliku().equals(utworek.getSciezkaDoPliku())) {
                    czyIstniejeWBazie = true;
                    utworek.setOdtworzenia(utworZBazy.getOdtworzenia());
                    break;
                }
            }
            if (!czyIstniejeWBazie) {
                //Dodaje te nowe utwory do bazy
                utworek.setOdtworzenia(0);
                utworDB.zwrocDao().wstawDoBazy(utworek);
            }
        }

        //Usuwa utwory których nie ma już w telefonie
        for (Utwor utworZBazy : utworyZBazy) {
            if (!sciezkiUtworowZTelefonu.contains(utworZBazy.getSciezkaDoPliku())) {
                utworDB.zwrocDao().usunUtworPoSciezce(utworZBazy.getSciezkaDoPliku());
            }
        }
    }

    boolean sprawdzUprawnienie() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    void poprosOUrpawnienie() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO)) {
            Toast.makeText(this, "Aby kożystać z aplikacji, potrzebne są uprawnienia!", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 123);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Odświeżanie po powrocie z OdtwarzaczActivity
        synchronizujBazeZTelefonem();
        odczytajOdtworzeniaZBazy();
    }
}