package com.hfad.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hfad.criminalintent.Crime
import java.util.*

/*
Первый шаг к взаимодействию с таблицами БД - создание объекта доступа к данным или DAO. Это интерфейс,
который содержит функции для каждой операции с БД, которые вы хотите реализовать.
 */
@Dao
interface CrimeDao {
    //добавим функции запросов к БД
    @Query("SELECT * FROM crime")
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
    fun getCrime(id: UUID): LiveData<Crime?>

    /*
    Room автоматически выполняет запросы getCrimes и getCrime в фоновом потоке, потому что эти
    функции возвращают LiveData. Для операций вставки и обновления Room не будет автоматически
    запускать взаимодействие с БД. Нужно будет явно выполнить вызовы DAO. Обычно используют
    исполнителя

     */
    @Update
    fun updateCrime(crime : Crime)

    @Insert
    fun addCrime(crime : Crime)
    /*
    Аннотация @Query указывает, что getCrimes() и getCrime(UUID) предназначены для извлечения
    информации из базы данных. В качестве входных данных ожидается строку, содержащую команду SQL

     */
}