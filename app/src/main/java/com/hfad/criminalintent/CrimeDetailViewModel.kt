package com.hfad.criminalintent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.io.File
import java.util.*

/*
Когда CrimeFragment запрашивает из базы преступление с заданным идентификатором, его ViewModel делает
запрос к базе данных. После выполнения она уведомляет CrimeFragment  проходится по объекту, возникшему
в результате запроса
 */
class CrimeDetailViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get() //связь с CrimeRepository
    private val crimeIdLiveData = MutableLiveData<UUID>() //идентификатор отображаемого в данный момент преступления

    /*
    Transformation - преобразование. Преобразование данных в реальном времени - способ
    установить отношения "Триггер - ответ" между двумя объектами LiveData. Функция преобрования
    принимат два объекта: объект LiveData, используемый в качестве триггера и функцию отображения,
    которая должна вернуть объект LiveData (результата преобразования)
     */
    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }
    fun loadCrime(crimeId : UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(crime : Crime) {
        crimeRepository.updateCrime(crime)
    }
    //выдача файла
    fun getPhotoFile(crime : Crime) : File {
        return crimeRepository.getPhotoFile(crime)
    }
}