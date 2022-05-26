package com.hfad.criminalintent

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File
import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks, CrimeFragment.Callbacks {
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // do something
        }

    override fun onCrimeSelected(crimeId: UUID) {
        Log.d(TAG, "MainActivity.OnCrimeSelected: $crimeId")
        val fragment = CrimeFragment.newInstance(crimeId)
        /*
        Функция replace заменяет фрагмент, размещенный в activity на новый фрагмент

        Функция addToBackStack добавляет транзакцию замены в обратный стек, чтобы по нажатию
        кнопки Назад, можно было вернуться к crimeListFragment

        CrimeListFragment уведомляет MainActivity о выборе преступления и передает идентификатор
        выбранного преступления. Теперь нужен способ передать выбранный id в CrimeFragment, чтобы
        он мог извлечь данные из БД и заполнить интерфейс этими данными


         */
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).
            addToBackStack(null).commit()
    }

    override fun onPhotoSelected(file: File) {
        Log.d(TAG, "MainActivity.OnPhotoSelected")
        val fragment = DialogFragment.newInstance(file)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).
        addToBackStack(null).commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //проверям разрешение на чтение контактов
        if (ContextCompat.checkSelfPermission(
               this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //если его нет, то запрашиваем разрешение
            // Pass any permission you want while launching
            requestPermission.launch(Manifest.permission.READ_CONTACTS)
        }
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if(currentFragment == null) {
            val fragment = CrimeListFragment.newInstance()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()

        }
    }
}
/*
           Этот код создает и закрепляет транзакцию фрагмента. Они используются для добавления,
           удаления, присоединения, отсоединения и замены фрагментов в списке фрагментов

           beginTransaction создает и возвращает экземпляр FragmentTransaction. Этот класс
           использует динамический интерфейс: функции настраивающие FragmentTransaction возвращают
           FragmentTransaction вместо Unit. Это позволяет объединять их вызовы в цепоку.

           По сути код озанчает: "создать новую транзакцию фрагмента, включить в нее одну операцию
           add, а затем закрепить"

           add отвечает за основное содержание транзакции. Она получает индентификатор контейнерного
           представения и недавно созданный объект CrimeFragment, выполняющий две функции:
               Сообщает FragmentManager, где в представлении activity должно находится
               представление фрагмента

               обеспечивает однозначную идентификацию фрагмента в списке fragmentManager

            Вызов onCreate может быть выполнен в ответ на воссоздание объекта MainActivity после
            его уничтожения из-за поворота устройства или освобождения памяти
            */
