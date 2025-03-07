package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var todoViewModel: TodoViewModel // Khai báo biến ViewModel
    private lateinit var recyclerView: RecyclerView // Khai báo biến RecyclerView
    private lateinit var adapter: TaskAdapter // Khai báo biến Adapter
    private lateinit var bottomNavigationView: BottomNavigationView // Khai báo biến BottomNavigationView
    private val taskList = mutableListOf<TodoItem>() // Khai báo danh sách task
    private lateinit var sharedPreferences: SharedPreferences // Khai báo biến SharedPreferences
    private lateinit var etSearch: EditText // Khai báo biến EditText cho tìm kiếm
    private var backupTaskList: MutableList<TodoItem>? = null // Khai báo biến lưu danh sách gốc khi lọc

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home_fragment, container, false) // Inflate layout cho fragment
        // Khởi tạo ViewModel
        todoViewModel = ViewModelProvider(requireActivity())[TodoViewModel::class.java]
        recyclerView = view.findViewById(R.id.recyclerView) // Tìm RecyclerView trong layout
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation) // Tìm BottomNavigationView trong layout
        sharedPreferences = requireContext().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE) // Khởi tạo SharedPreferences
        etSearch = view.findViewById(R.id.etSearch) // Tìm EditText cho tìm kiếm trong layout
        view.isFocusableInTouchMode = true // Đặt focus cho view
        view.requestFocus() // Yêu cầu focus cho view
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Đặt layout manager cho RecyclerView
        recyclerView.setHasFixedSize(true) // Đặt cờ hasFixedSize cho RecyclerView
        adapter = TaskAdapter(requireContext(), taskList, { position -> // Khởi tạo adapter
            val task = taskList[position]
            deleteTask(task.id, task.name) // Gọi hàm xóa task khi nhấn nút xóa
        }, todoViewModel)
        recyclerView.adapter = adapter // Đặt adapter cho RecyclerView
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() { // Thêm listener cho sự kiện cuộn của RecyclerView
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (taskList.size > 4) { // Nếu danh sách task có hơn 4 phần tử
                    if (dy > 10) {
                        bottomNavigationView.animate().translationY(bottomNavigationView.height.toFloat()).setDuration(200) // Ẩn BottomNavigationView khi cuộn xuống
                    } else if (dy < -10) {
                        bottomNavigationView.animate().translationY(0f).setDuration(200) // Hiện BottomNavigationView khi cuộn lên
                    }
                } else {
                    bottomNavigationView.animate().translationY(0f).setDuration(200) // Hiện BottomNavigationView nếu danh sách task có ít hơn hoặc bằng 4 phần tử
                }
            }
        })

        etSearch.addTextChangedListener(object : TextWatcher { // Thêm listener cho sự kiện thay đổi văn bản của EditText
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTaskList(s.toString()) // Gọi hàm lọc danh sách khi văn bản thay đổi
            }
        })

        etSearch.clearFocus() // Xóa focus của EditText
        view.setOnTouchListener { _, _ ->
            etSearch.clearFocus() // Xóa focus của EditText khi chạm vào view
            hideKeyboard() // Ẩn bàn phím
            true
        }

        updateBottomNavVisibility() // Cập nhật trạng thái hiển thị của BottomNavigationView
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addTask(taskText: String, taskTime: Long?) {
        val uniqueId = System.currentTimeMillis().toString() // Tạo ID duy nhất cho task
        val taskWithTime = if (taskTime != null) taskText else taskText // Thêm thời gian vào task nếu có
        val newTask = TodoItem(uniqueId, taskWithTime, taskTime ?: 0L) // Tạo đối tượng TodoItem mới

        taskList.add(0, newTask) // Thêm task vào danh sách
        adapter.notifyDataSetChanged() // Thông báo adapter cập nhật dữ liệu
        recyclerView.scrollToPosition(0) // Cuộn RecyclerView đến vị trí đầu tiên

        taskTime?.let { saveTaskToSharedPrefs(taskText, it) } // Lưu task vào SharedPreferences nếu có thời gian
        todoViewModel.incrementTotalTodos()
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun saveTaskToSharedPrefs(taskText: String, timestamp: Long) {
        val sharedPref = requireContext().getSharedPreferences("TaskByDate", Context.MODE_PRIVATE) // Khởi tạo SharedPreferences
        val editor = sharedPref.edit() // Khởi tạo editor

        val dateKey = getFormattedDate(timestamp) // Chuyển timestamp thành định dạng ngày "dd/MM/yyyy"
        val existingTasks = sharedPref.getStringSet(dateKey, mutableSetOf()) ?: mutableSetOf() // Lấy danh sách task hiện có hoặc tạo mới

        val uniqueTask = "$taskText|$timestamp" // Lưu task kèm timestamp để không bị trùng
        existingTasks.add(uniqueTask) // Thêm task vào danh sách

        editor.putStringSet(dateKey, existingTasks) // Lưu danh sách task vào SharedPreferences
        editor.apply() // Áp dụng thay đổi
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun deleteTask(taskId: String, taskName: String) {
        val task = taskList.find { it.id == taskId && it.name == taskName } // Tìm task trong danh sách

        if (task != null) {
            val position = taskList.indexOf(task) // Lấy vị trí của task

            // Xóa task khỏi SharedPreferences
            val sharedPref = requireContext().getSharedPreferences("TaskByDate", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            val dateKey = getFormattedDate(task.timestamp)

            val existingTasks = sharedPref.getStringSet(dateKey, mutableSetOf()) ?: mutableSetOf()
            val taskToRemove = existingTasks.find { it.startsWith(task.name) }

            if (taskToRemove != null) {
                existingTasks.remove(taskToRemove) // Xóa task khỏi danh sách
                editor.putStringSet(dateKey, existingTasks)

                if (existingTasks.isEmpty()) {
                    editor.remove(dateKey) // Xóa key nếu danh sách rỗng
                }
                editor.apply() // Áp dụng thay đổi
            }

            taskList.removeAt(position) // Xóa task khỏi danh sách
            adapter.notifyItemRemoved(position) // Thông báo adapter cập nhật dữ liệu

            todoViewModel.notifyTaskDeleted(task) // Thông báo ViewModel về việc xóa task
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterTaskList(query: String) {
        if (query.isEmpty()) {
            if (backupTaskList != null) {
                taskList.clear()
                taskList.addAll(backupTaskList!!) // Khôi phục danh sách gốc
                backupTaskList = null // Xóa bản sao tạm
            }
        } else {
            if (backupTaskList == null) {
                backupTaskList = ArrayList(taskList) // Lưu danh sách gốc trước khi lọc
            }
            val filtered = taskList.filter { it.name.contains(query, ignoreCase = true) } // Lọc danh sách theo query
            taskList.clear()
            taskList.addAll(filtered) // Cập nhật danh sách với kết quả lọc
        }
        adapter.notifyDataSetChanged() // Thông báo adapter cập nhật dữ liệu
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // Lấy InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0) // Ẩn bàn phím
    }

    private fun updateBottomNavVisibility() {
        if (taskList.size <= 4) {
            bottomNavigationView.animate().translationY(0f).setDuration(200) // Hiện BottomNavigationView nếu danh sách task có ít hơn hoặc bằng 4 phần tử
        }
    }

    private fun getFormattedDate(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Định dạng ngày
        return sdf.format(Date(timeInMillis)) // Trả về chuỗi ngày đã định dạng
    }
}