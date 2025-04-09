package com.example.odtwarzaczmuzyki;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "utwory")
public class Utwor implements Serializable {
    private String nazwaUtworu;
    private String wykonawca;
    private long czasTrwania;
    @ColumnInfo(name = "sciezkaDoPliku")
    private String sciezkaDoPliku;
    private long idAlbumu;
    private long dataDodania;
    @ColumnInfo(name = "odtworzenia")
    private int odtworzenia;
    private String nazwaAlbumu;
    private String typ;
    private long rozmiar;
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    public Utwor(String nazwaUtworu, String wykonawca, long czasTrwania, String sciezkaDoPliku, long idAlbumu, long dataDodania, int odtworzenia, String nazwaAlbumu, String typ, long rozmiar) {
        this.id = 0;
        this.nazwaUtworu = nazwaUtworu;
        this.wykonawca = wykonawca;
        this.czasTrwania = czasTrwania;
        this.sciezkaDoPliku = sciezkaDoPliku;
        this.idAlbumu = idAlbumu;
        this.dataDodania = dataDodania;
        this.odtworzenia = odtworzenia;
        this.nazwaAlbumu = nazwaAlbumu;
        this.typ = typ;
        this.rozmiar = rozmiar;
    }

    public String getNazwaUtworu() {
        return nazwaUtworu;
    }

    public long getIdAlbumu() {
        return idAlbumu;
    }

    public String getSciezkaDoPliku() {
        return sciezkaDoPliku;
    }

    public long getCzasTrwania() {
        return czasTrwania;
    }

    public String getWykonawca() {
        return wykonawca;
    }

    public long getDateAdded() {
        return dataDodania;
    }

    public int getOdtworzenia() {
        return odtworzenia;
    }

    public String getNazwaAlbumu() {
        return nazwaAlbumu;
    }

    public long getRozmiar() {
        return rozmiar;
    }

    public String getTyp() {
        return typ;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDataDodania() {
        return dataDodania;
    }

    public void setOdtworzenia(int odtworzenia) {
        this.odtworzenia = odtworzenia;
    }

    public void zwiekszLiczbeOdtworzen() {
        this.odtworzenia++;
    }
}
