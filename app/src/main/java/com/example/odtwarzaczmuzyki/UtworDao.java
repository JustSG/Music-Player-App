package com.example.odtwarzaczmuzyki;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UtworDao {
    @Insert
    public void wstawDoBazy(Utwor utwor);

    @Query("UPDATE utwory SET odtworzenia = odtworzenia + 1 WHERE sciezkaDoPliku = :sciezka")
    void aktualizujOdtworzenia(String sciezka);

    @Query("DELETE FROM utwory")
    public void usunWszystko();

    @Query("SELECT * FROM utwory")
    public List<Utwor> zwrocWszystkieUtwory();

    @Query("SELECT odtworzenia FROM utwory")
    public List<Integer> zwrocOdtworzeniaZBazy();

    @Query("SELECT * FROM utwory WHERE sciezkaDoPliku = :sciezka")
    public Utwor znajdzUtworPoSciezce(String sciezka);

    @Query("DELETE FROM utwory WHERE sciezkaDoPliku = :sciezka")
    void usunUtworPoSciezce(String sciezka);
}
