package com.hfad.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hfad.criminalintent.Crime
/*
Аннотация @Database сообщает Room о том, что этот класс представляет собой базу данных

Первый параметр - список классов сущностей, который сообщает Room, какие использовать классы
при создании и управлении таблицами для этой базы данных. Второй параметр - версия базы данных

 */
@Database(entities = [Crime::class], version = 2)
@TypeConverters(CrimeTypeConverters::class)
abstract class CrimeDatabase : RoomDatabase() {

    abstract fun crimeDao(): CrimeDao
}
    /*
    при создании базы данных Room будет генерировать конкретную реализацию в DAO. Если есть ссылка
    на DAO, можно вызвать любую из функций, чтобы взаимодействовать с БД
     */

/*
Чтобы сообщить Room, как переводить базу данных с одной версии на другую, нужно добавить свойство
Migration
 */
val migration_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT ''"
            )
        }
}