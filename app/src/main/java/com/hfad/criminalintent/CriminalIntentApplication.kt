package com.hfad.criminalintent

import android.app.Application
/*
Подкласс Application, который позволяет получить информацию о жизненном цикле самого приложения
 */
class CriminalIntentApplication : Application() {
    override fun onCreate() {
        /*
        Application.onCreate() вызывается системой, когда приложениее впервые загружается в память
        Экземпляр приложения не будет постоянно уничтожаться и создаваться вновь. Он создается, когда
        приложение запускается и уничтожается, когда завершается процесс приложения.

        Чтобы класс приложения можно было использовать в системе, необходимо зарегистрировать его в
        манифесте. Когда он будет зарегистрирован в манифесте, ОС создаст экземпляр CriminalIntentApplication
        при запуске приложения и вызовет метод onCreate()
         */
        super.onCreate()
        CrimeRepository.initialize(this)
    }
}