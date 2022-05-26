package com.hfad.criminalintent

import android.app.Activity
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.io.File
import java.util.*


private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val DIALOG_PHOTO = "DialogPhoto"
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val TAG = "CrimeFragment"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.CallBbacks {
    interface Callbacks {
        fun onPhotoSelected(file : File)
    }

    private var callbacks: CrimeFragment.Callbacks? = null

    private lateinit var crime : Crime
    private lateinit var titleField : EditText
    private lateinit var dateButton : Button
    private lateinit var reportButton : Button
    private lateinit var suspectButton : Button
    private lateinit var callSuspectButton : Button
    private lateinit var solvedCheckBox : CheckBox
    private lateinit var photoButton : ImageButton
    private lateinit var photoView : ImageView
    private lateinit var photoFile : File
    private lateinit var photoUri : Uri

    private var switcher = 0


    //проассоциируем CrimeFrament и CrimeDetailViewModel
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    //настроили экземпляр фрагмента
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        //извлечем UUID из аргументов фрагмента
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime ID: $crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
    }

    //настроили представление фрагмента
    override fun onCreateView( //явно заполняем представление фрагмента
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //явно заполняем представление фрагмента
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        //работаем с ним, вызывая основную функцию findViewById
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callSuspectButton = view.findViewById(R.id.call_suspect) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        return view
        /*
        передаем идентификатор ресурса макета, определям родителя представления, и указываем, нужно
        ли включать заполненное представление в родителя
         */
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    /*
                    Вызов FileProvider.getUriForFile(...) преобразует локальный путь к файлу
                    в Uri, который видит приложение камеры. Функция принимает на вход activity,
                    провайдера и файл фотографии для создания URI, который указывает на файл.
                    */
                    photoUri = FileProvider.getUriForFile(requireActivity(),
                        "com.hfad.criminalintent.fileprovider", photoFile)
                    updateUI()
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        //cоздали анонимный класс, реализующий интерфейс слушателя TextWatcher. Он содержит 3 функции
        super.onStart()

        val titleWatcher = object : TextWatcher {

            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        //установим слушателя для checkBox
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {

            /*
            Для добавления экземпляра DialogFragment во FragmentManager и вывода его на
            экран используются следующией функции экземпляра фрагммента:
                show(manager: FragmentManager, tag: String)
                show(transaction: FragmentTransaction, tag: String)

            если выбрать транзакцию, то за создание и закрепление отвечает программист, иначе
            за него все сделает FragmentManager
             */
            if (switcher % 2 == 0) {
                DatePickerFragment.newInstance(crime.date).apply {
                    //установили связь между CrimeFragment и DatePickerFragment
                    setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                    show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
                }
            } else {
                TimePickerFragment.newInstance(crime.date).apply {
                    setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                    show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
                }
            }
            switcher++;
        }
        //создаем неявный интент и запускаем выбор актвностей, которые могу тего прниять
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent =
                    Intent.createChooser(intent, getString(R.string.send_report))
                    startActivity(chooserIntent)
                //startActivity(intent)
            }
        }

        //запрос контакта у Android
        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }
            //защита от отсутствия приложения адресной книги
            //val packageManager : PackageManager = requireActivity().packageManager
            //val resolvedActivity: ResolveInfo? = packageManager.
            //resolveActivity(pickContactIntent, PackageManager.MATCH_ALL)
            //if (resolvedActivity == null) isEnabled = false
        }

        //набор номера подозреваемого
        callSuspectButton.setOnClickListener {

            val phoneNumber = getPhoneNumber(requireContext())
            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:" + phoneNumber)
            )
            startActivity(intent)
        }

        photoButton.apply {
            //проверили, что приложение камеры существует
            val packageManager : PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity : ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities : List<ResolveInfo> =
                    packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                for(cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }

                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

        photoView.setOnClickListener {
            Log.d(TAG, "photoViewClicked")
            callbacks?.onPhotoSelected(photoFile)

        }


    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }
    /*
    Слушатель TextWatcher настраивается в функции onStart. Слушатели, которые реагируют
    на ввод данных срабатывают не только при взаимодействии с ними но и при восстановалении
    состояня виджета. Слушатели, которые реагируют только на взаимодействие с пользователем не
    восприимчивы в такому поведению. Состояние виджета восстанавливается после onCreateView
    и перед onStart(). При восстановлении состояния содержимое EditText будет установлено на любое
    значение, которое в данный момент находится в заголовке crime.title. Если слушатель установлен
    в onCreate() будут выполняться функции beforeTextChanged, onTextChanged, afterTextChanged.
    Установка слушателя в onStart() позволяет избежать такого поведения, так как слушатель
    подключается после восстановления состояния виджета
     */


    /*
    Получаем UUID, создаем пакет аргументов, создаем экземпляр фрагмента а затем присоединяем
    к нему аргументы
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as CrimeFragment.Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        callbacks = null
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState() //пропуск анимации установки флажка
        }
        if(crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    //обновление photoView:
    private fun updatePhotoView() {
        if (photoFile.exists()) {
            photoView.rotation = 0f
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
                photoView.setImageBitmap(bitmap)
                photoView.rotation = 90f
        } else {
            photoView.setImageDrawable(null)
        }
    }
    //метод получения номера телефона
    fun getPhoneNumber( context: Context): String {
        var ret: String? = null
        val selection =
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + crime.suspect + "%'"
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val c: Cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, selection, null, null
        )!!
        if (c.moveToFirst()) {
            ret = c.getString(0)
        }
        c.close()
        if (ret == null) ret = "Unsaved"
        return ret
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            /*
             создается запрос всех отображаемых имен контактов в возвращенных данных.
             Затем вы запрашиваете базу данных контактов и получаете объект Cursor,
             с которым мы работаем. После проверки того, что возвращенный курсор содержит
             хотя бы одну строку, вы вызываете функцию Cursor.moveToFirst()
             для перемещения курсора в первую строку. Наконец, вы вызываете функцию
             Cursor.getString(Int) для перемещения содержимого первого столбца в виде строки.
             Эта строка будет именем подозреваемого, и вы используете ее для установки
             подозреваемого в преступлении
             */
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                //укащаание на поля, для которых запрос возвращает значения
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

                val cursor = requireActivity().contentResolver.query(
                    contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    if(it.count == 0) return

                    //первый столбей первой строки - имя
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
            requestCode == REQUEST_PHOTO -> {
                //перекрываем доступ к файлу
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }


    //теперь CrimeFragment отвечает на новые даты
    override fun onDateSelected(date: java.util.Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: java.util.Date) {
        crime.date = date
        updateUI()
    }

    //функция, которая, работая с форматной строкой, создает отчет
    private fun getCrimeReport() : String {
        val solvedString = if(crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = android.text.format.DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if(crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report,
                crime.title, dateString, solvedString, suspect)
    }


    companion object {
        /*
        Теперь MainActivity будет вызывать CrimeFragment.newInstanсе каждый раз, как ему потребуется
        создать CrimeFragment. При вызове передет значение UUID, полученное из OnCrimeSelected
         */
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}
