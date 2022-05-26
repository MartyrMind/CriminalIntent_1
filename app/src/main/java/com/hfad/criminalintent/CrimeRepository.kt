package com.hfad.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.hfad.criminalintent.database.CrimeDatabase
import com.hfad.criminalintent.database.migration_1_2
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"
/*
Класс репозитория инкапсулирует логку для доступа к данным из одного источника или совокупности
источников. Он определяет, как захыватывать и хранить определенный набор данных - локально, в базе
данных или с удаленного сервера. Код UI будет запрашивать все данные из репозитория
 */
class CrimeRepository private constructor(context: Context){
    /*
    Функция Room.databaseBuilder() создает конкреную реализацию абстрактного класса CrimeDatabase
    Параметры:
        объект Context
        Класс базы данных, которую создает Room
        Третий - имя файла базы данных, которую создаст Room
     */
    private val database : CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    //передадим миграции в БД
    ).addMigrations(migration_1_2).build()

    private val crimeDao = database.crimeDao()
    /*
    Исполнитель - объект, ссылающийся на поток. Экземпляр исполнителя
    имеет функцию, называемую execute, которая принимает на выполнение блок кода. Код, который
    находится в этом блоке будет выполняться в любом потоке, на который ссылается исполнитель
     */
    private val executor = Executors.newSingleThreadExecutor()
    private val filesDir = context.applicationContext.filesDir

    //заполним репозиторий, обращаясь к реализациям этих методов в DAO

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }
    //этот код не создает файлов, он лишь возвращает объекты File, указывающие в нужные места
    fun getPhotoFile(crime : Crime): File = File(filesDir, crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        fun get() : CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}

/*
CrimeRepository - одноэлементный класс (синглтон). Это означает, что в приложении единовременно
существует только один его экземпляр и существует он до тех пор, пока приложение находится в памяти
Синглтоны не подходят для долговременного хранения данных
 */