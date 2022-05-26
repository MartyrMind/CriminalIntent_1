package com.hfad.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import java.util.*

private const val TAG = "CrimeListFragment"


class CrimeListFragment : Fragment() {
    /*
    Для передачи функциональности обратно хостингу в фрагменте обычно определяется интерфейс обратного
    вызова Callbacks, определяющий работу, которую должна вызвать хост-activity. Любая
    activity, которая будет содержать этот фрагмент, должна реализовать этот интерфейс

    С помощью интерфейса обратного вызова фрагмент способен вызывать функции, связанные с его
    хост-activity без необходимости знать о том, какая activity является хостом
     */
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null

    private lateinit var crimeRecyclerView : RecyclerView

    /*
    Данная функция жизненного цикла вызывается, когда фрагмент прикрепляется к activity. Аргумент
    Context помещается в свойство callback. Этот объект является экземпляром activity, в которой
    размещен фрагмент
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        сообщили менеджеру фрагментов, что экземпляр CrimeListFragment должен получать обратные
        вызовы меню
         */
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    /*
    Обратите внимание, что эта функция возвращает значение логического типа.
    После того как обработали MenuItem, вы должны вернуть true, чтобы указать, что
    дальнейшая обработка не требуется. Если вернете false, обработка меню будет
    продолжена вызовом функции onOptionsItemSelected(MenuItem) из хост-activity
    (или, если activity содержит другие фрагменты, на этих фрагментах будет вызвана
    функция onOptionsItemSelected). По умолчанию вызывается реализация
    суперкласса, если в вашей реализации идентификатор элемента отсутствует.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.new_crime -> {
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    /*
    Фрагмент ожидает результатов из базы данных, чтобы заполнить утилизатор, поэтому
    инициализируем пустым списком
     */
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel : CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*
        Recycler View не отображает элементы на самом экране. Он передает эту задачу LayoutManager
         */
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView =  view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter
        return view
    }
    /*
    функция LiveData.observe(LifecycleOwner, Observer) используется для регистрации
    наблюдателя за экземпляром LiveData и связи наблюдения с жизненным циклом другого
    компонента. Observer - объект, который отвечает за реакцию на новые данные из LiveData.

    В этом случае наблюдатель получает список преступлений из LiveData и печатает сообщение
    в журнал, если свойство не равно нулю

    Пока владелец жизненного цикла, которому передан наблюдатель, находится в допустимом состоянии
    жизненного цикла, объект LiveData уведомляет наблюдателя о получении новых данных
    */

    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(
            viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    Log.i(TAG, "curr_crimes size ${adapter?.currentList?.size}")
                    adapter?.submitList(crimes) //{
                        //crimeRecyclerView.adapter = adapter
                    //}
                    updateUI(crimes)
                }
            })
    }


    /*
    RecyclerView ожидает, что элемент представления будет обернут в экзепляр ViewHolder, который
    хранит ссылку на представление элемента

    В конструкторе CrimeHolder мы берем представление для закрепления, которое сразу передается
    в конструктор классов RecyclerView.ViewHolder. Базовый класс ViewHolder будет закрепляться на
    свойство под названием itemView

    RecyclerView никогда не создает объекты View сами по себе. Он всегда создает ViewHolder,
    используя адаптер, которые выводят свои itemView

    Адаптер - объект контроллера, который находится между RecyclerView и наборами данных, которые он
    отображает. Адаптер выполняет следующие функции:
        создание необходимых ViewHolder по запросу
        связывание ViewHolder с данными из модельного слоя
    Утилизатор - один объект из списка данных представления, выполняющий такие функции:
        запрашивает адаптер на создание нового ViewHolder
        запрашивает адаптер привязать ViewHolder к элементу данных на этой позиции
     */
    private inner class CrimeAdapter(var crimes : List<Crime>)
        : ListAdapter<Crime, CrimeHolder>(DiffCallback()){

//        private val TYPE_ITEM1 = 0
//        private val TYPE_ITEM2 = 1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
//            val view = when {
//                viewType == TYPE_ITEM1 -> layoutInflater.inflate(R.layout.list_item_crime, parent, false)
//                else -> layoutInflater.inflate(R.layout.list_item_hard_crime, parent, false)
//            }
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        //        override fun getItemViewType(position: Int): Int {
//            return when {
//                crimes[position].requiresPolice -> TYPE_ITEM2
//                else -> TYPE_ITEM1
//            }
//        }
        /*
        Отвечает за заполнение данного холдера данными из данной позиции
        Это событие должно выполнять минимальный объем работы, иначе будет медленная анимация
         */
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }
        /*
        Когда утилизатору нужно знать, сколько элементов в наборе данных поддерживают его он
        будет просить свой адаптер вызвать этот метод
         */
        override fun getItemCount() = crimes.size
        /*
        Сам RecyclerView ичего не знает об объекте преступления, зато CrimeAdapter знает все
        данные преступлени. Он также знает, какие преступления входят в утилизатор
         */
    }

    class DiffCallback : DiffUtil.ItemCallback<Crime>() {
        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            Log.i(TAG, "areContentsTheSame")
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            Log.i(TAG, "areItemsTheSame")
            return oldItem == newItem
        }
    }

    private inner class CrimeHolder(view : View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }
        private lateinit var crime : Crime
        private val titleTextView : TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView : TextView = itemView.findViewById(R.id.crime_date)
        //получили объект для привязки
        fun bind(crime : Crime) {
            //обновили свойства преступления
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            //если это тяжелое но раскрытое преступление, то выводим наручники
            //if (viewType == 0) {
            val solvedImageView : ImageView = itemView.findViewById(R.id.imageView)
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
            //}

        }

        override fun onClick(v: View) {
            Toast.makeText(context, "${crime.title} clicked!", Toast.LENGTH_SHORT)
                .show()
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    companion object {
        /*
        Эту функцию будут вызывать activity, чтобы получить экземпляр фрагмента. Это похоже
        на функцию newIntent
         */
        fun newInstance() : CrimeListFragment {
            return CrimeListFragment()
        }
    }
}