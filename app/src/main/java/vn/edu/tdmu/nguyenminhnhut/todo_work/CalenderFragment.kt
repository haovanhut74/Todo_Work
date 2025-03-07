package vn.edu.tdmu.nguyenminhnhut.todo_work
import com.prolificinteractive.materialcalendarview.CalendarDay
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.prolificinteractive.materialcalendarview.DayViewDecorator

class CalenderFragment : Fragment() {
    private lateinit var calendarView: MaterialCalendarView
    private lateinit var selectedDateText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarTaskAdapter
    private val taskList = mutableListOf<TodoItem>()
    private val handler = Handler(Looper.getMainLooper())
    private var currentSelectedDate: String = ""
    private lateinit var todoViewModel: TodoViewModel
    private lateinit var bottomNavigationView: BottomNavigationView
    @SuppressLint("SetTextI18n", "DetachAndAttachSameFragment", "DefaultLocale", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.calender_fragment, container, false)
        todoViewModel = ViewModelProvider(requireActivity())[TodoViewModel::class.java]
        calendarView = view.findViewById(R.id.calendarView)
        selectedDateText = view.findViewById(R.id.selectedDateText)
        recyclerView = view.findViewById(R.id.recyclerView)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        adapter = CalendarTaskAdapter(taskList)
        recyclerView.adapter = adapter

        val today = getTodayDate()
        if (currentSelectedDate.isEmpty()) {
            selectedDateText.text = "Tasks on: $today"
            loadTasksForDate(today) }
        else {
            selectedDateText.text = "Tasks on: $currentSelectedDate"
            loadTasksForDate(currentSelectedDate) }

        todoViewModel.tasksUpdated.observe(viewLifecycleOwner) {
            loadTasksForDate(selectedDateText.text.toString().replace("Tasks on: ", ""))
        }

        todoViewModel.taskDeleted.observe(viewLifecycleOwner) { todoItem ->
            removeTodoItemFromCalendar(todoItem)
        }

        calendarView.setOnDateChangedListener{ _, date, _  ->
            val selectedDate = String.format("%02d/%02d/%d", date.day, date.month + 1, date.year)
            selectedDateText.text = "Tasks on: $selectedDate"
            loadTasksForDate(selectedDate)
        }
        calendarView.addDecorator(EventDecorator())

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_MOVE -> hideBottomNavigationView()
                    MotionEvent.ACTION_UP -> showBottomNavigationView()
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        return view
    }


    private fun hideBottomNavigationView() {
        bottomNavigationView.visibility = View.GONE
    }

    private fun showBottomNavigationView() {
        bottomNavigationView.visibility = View.VISIBLE
    }
    private fun removeTodoItemFromCalendar(todoItem: TodoItem) {
        val position = taskList.indexOfFirst { it.id == todoItem.id }
        if (position != -1) {
            taskList.removeAt(position)
            adapter.notifyItemRemoved(position)
        }
    }

    private val updateTaskRunnable = object : Runnable {
        override fun run() {
            loadTasksForDate(selectedDateText.text.toString().replace("Tasks on: ", ""))
            handler.postDelayed(this, 5000)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasksForDate(currentSelectedDate)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTaskRunnable)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadTasksForDate(selectedDate: String) {
        val sharedPref = requireContext().getSharedPreferences("TaskByDate", Context.MODE_PRIVATE)
        val tasksForDate = sharedPref.getStringSet(selectedDate, setOf()) ?: setOf()
        println("📅 Đọc Todo cho ngày: $selectedDate -> $tasksForDate")
        taskList.clear()

        for (taskData in tasksForDate) {
            val parts = taskData.split("|")
            if (parts.size == 2) {
                val taskText = parts[0]
                val timestamp = parts[1].toLongOrNull() ?: System.currentTimeMillis()

                val taskDate = getFormattedDate(timestamp)
                if (taskDate == selectedDate) {
                    taskList.add(TodoItem(System.currentTimeMillis().toString(), taskText, timestamp))
                }
            }
        }
        adapter.notifyDataSetChanged()
        calendarView.invalidateDecorators()
    }

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getFormattedDate(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    inner class EventDecorator : DayViewDecorator {
        @SuppressLint("DefaultLocale")
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val date = String.format("%02d/%02d/%d", day.day, day.month + 1, day.year)
            val sharedPref = requireContext().getSharedPreferences("TaskByDate", Context.MODE_PRIVATE)
            val tasksForDate = sharedPref.getStringSet(date, setOf()) ?: setOf()
            return tasksForDate.isNotEmpty()
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(object : ForegroundColorSpan(Color.RED) {})
        }
    }
}