package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.Manifest // Import lớp Manifest để truy cập quyền
import android.annotation.SuppressLint // Import annotation để bỏ qua cảnh báo lint
import android.app.AlertDialog // Import lớp AlertDialog để tạo hộp thoại cảnh báo
import android.app.DatePickerDialog // Import lớp DatePickerDialog để tạo hộp thoại chọn ngày
import android.app.NotificationChannel // Import lớp NotificationChannel để tạo kênh thông báo
import android.app.NotificationManager // Import lớp NotificationManager để quản lý thông báo
import android.app.TimePickerDialog // Import lớp TimePickerDialog để tạo hộp thoại chọn giờ
import android.content.Context // Import lớp Context để truy cập tài nguyên ứng dụng
import android.content.pm.PackageManager // Import lớp PackageManager để quản lý các gói ứng dụng
import android.os.Build // Import lớp Build để kiểm tra phiên bản Android
import android.os.Bundle // Import lớp Bundle để truyền dữ liệu giữa các thành phần
import android.os.Handler // Import lớp Handler để xử lý các tác vụ trên luồng chính
import android.os.Looper // Import lớp Looper để quản lý luồng
import android.text.InputFilter // Import lớp InputFilter để lọc dữ liệu nhập vào EditText
import android.widget.Button // Import lớp Button để tạo nút bấm
import android.widget.EditText // Import lớp EditText để tạo ô nhập liệu
import android.widget.LinearLayout // Import lớp LinearLayout để tạo layout tuyến tính
import android.widget.Toast // Import lớp Toast để hiển thị thông báo ngắn
import androidx.appcompat.app.AppCompatActivity // Import lớp AppCompatActivity để tạo activity hỗ trợ
import androidx.core.app.ActivityCompat // Import lớp ActivityCompat để yêu cầu quyền
import androidx.core.content.ContextCompat // Import lớp ContextCompat để kiểm tra quyền
import androidx.fragment.app.Fragment // Import lớp Fragment để tạo fragment
import com.google.android.material.bottomnavigation.BottomNavigationView // Import lớp BottomNavigationView để tạo thanh điều hướng dưới
import com.google.android.material.floatingactionbutton.FloatingActionButton // Import lớp FloatingActionButton để tạo nút hành động nổi
import java.text.SimpleDateFormat // Import lớp SimpleDateFormat để định dạng ngày giờ
import java.util.Calendar // Import lớp Calendar để làm việc với lịch
import java.util.Date // Import lớp Date để làm việc với ngày giờ
import java.util.Locale // Import lớp Locale để định dạng theo ngôn ngữ

class MainActivity : AppCompatActivity() {
    private lateinit var homeFragment: HomeFragment // Khai báo biến homeFragment kiểu HomeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🛠️ Xóa toàn bộ dữ liệu test cũ (chỉ chạy 1 lần)
        val sharedPref =
            getSharedPreferences("TaskByDate", Context.MODE_PRIVATE) // Lấy SharedPreferences
        sharedPref.edit().clear().apply() // Xóa toàn bộ dữ liệu trong SharedPreferences

        setContentView(R.layout.activity_main) // Thiết lập layout cho activity
        try {
            setContentView(R.layout.activity_main) // Thiết lập lại layout (có thể thừa)

            homeFragment = HomeFragment() // Khởi tạo homeFragment
            loadFragment(homeFragment) // Tải fragment homeFragment

            val bottomNavigation: BottomNavigationView =
                findViewById(R.id.bottom_navigation) // Tìm BottomNavigationView trong layout
            val fabAdd: FloatingActionButton =
                findViewById(R.id.fab_add) // Tìm FloatingActionButton trong layout

            bottomNavigation.setOnItemSelectedListener { item -> // Thiết lập listener cho BottomNavigationView
                when (item.itemId) {
                    R.id.nav_home -> { // Nếu chọn item nav_home
                        loadFragment(homeFragment) // Tải homeFragment
                        fabAdd.show() // Hiển thị FloatingActionButton
                        true // Trả về true để xác nhận đã xử lý sự kiện
                    }

                    R.id.nav_tasks -> { // Nếu chọn item nav_tasks
                        val calenderFragment = CalenderFragment() // Khởi tạo calenderFragment
                        loadFragment(calenderFragment) // Tải calenderFragment

                        Handler(Looper.getMainLooper()).postDelayed({ // Sử dụng Handler để trì hoãn việc tải dữ liệu
                            if (calenderFragment.isAdded) { // Kiểm tra xem calenderFragment đã được thêm vào activity chưa
                                calenderFragment.loadTasksForDate(calenderFragment.getTodayDate()) // Tải dữ liệu cho ngày hôm nay
                            }
                        }, 300)
                        // Delay 300ms

                        fabAdd.hide() // Ẩn FloatingActionButton
                        true // Trả về true để xác nhận đã xử lý sự kiện
                    }

                    R.id.nav_profile -> { // Nếu chọn item nav_profile
                        loadFragment(ProfileFragment()) // Tải ProfileFragment
                        fabAdd.hide() // Ẩn FloatingActionButton
                        true // Trả về true để xác nhận đã xử lý sự kiện
                    }

                    else -> false // Nếu không khớp với item nào, trả về false
                }
            }

            fabAdd.setOnClickListener { // Thiết lập listener cho FloatingActionButton
                showAddTodoDialog() // Hiển thị hộp thoại thêm công việc
            }

            createNotificationChannel() // Tạo kênh thông báo
            requestNotificationPermission() // Yêu cầu quyền thông báo
        } catch (e: Exception) { // Xử lý ngoại lệ
            e.printStackTrace() // In lỗi ra log
            Toast.makeText(this, "Lỗi: " + e.message, Toast.LENGTH_LONG)
                .show() // Hiển thị thông báo lỗi
        }
    }

    /** ✅ Tạo kênh thông báo (Chỉ chạy trên Android 8.0 trở lên) */
    private fun createNotificationChannel() {
        // Kiểm tra phiên bản Android
        val channel = NotificationChannel(
            "todo_channel", // ID của kênh phải giống với ID trong NotificationCompat.Builder
            "Task Reminders", // Tên kênh
            NotificationManager.IMPORTANCE_HIGH // Mức độ quan trọng của kênh
        )
        channel.description = "Channel for task reminders" // Mô tả kênh

        val notificationManager =
            getSystemService(NotificationManager::class.java) // Lấy NotificationManager
        notificationManager.createNotificationChannel(channel) // Tạo kênh thông báo
    }

    /** ✅ Yêu cầu quyền thông báo trên Android 13+ */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) // Kiểm tra quyền thông báo
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                ) // Yêu cầu quyền thông báo
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) { // Kiểm tra mã yêu cầu
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Kiểm tra quyền được cấp
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT)
                    .show() // Hiển thị thông báo quyền được cấp
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT)
                    .show() // Hiển thị thông báo quyền bị từ chối
            }
        }
    }

    /** ✅ Hiển thị hộp thoại nhập công việc mới */
    @SuppressLint("SetTextI18n")
    private fun showAddTodoDialog() {
        val builder = AlertDialog.Builder(this) // Tạo AlertDialog.Builder
        builder.setTitle("Add your task") // Thiết lập tiêu đề

        val layout = LinearLayout(this) // Tạo LinearLayout
        layout.orientation = LinearLayout.VERTICAL // Thiết lập hướng dọc
        layout.setPadding(50, 20, 50, 20) // Thiết lập padding

        val input = EditText(this) // Tạo EditText
        input.hint = "Write your task name (max 30 characters)" // Thiết lập hint
        input.filters = arrayOf(InputFilter.LengthFilter(30)) // Giới hạn số ký tự
        layout.addView(input) // Thêm EditText vào layout

        // Nút chọn thời gian
        val btnSetTimer = Button(this) // Tạo Button
        btnSetTimer.text = "SET TIME" // Thiết lập text
        layout.addView(btnSetTimer) // Thêm Button vào layout

        var selectedDateTime: Long? = null // 🔥 Đổi kiểu thành Long để lưu thời gian chính xác

        btnSetTimer.setOnClickListener { // Thiết lập listener cho Button
            showDateTimePicker(this) { dateTime -> // 🔥 Thêm `this` để truyền context
                selectedDateTime = dateTime // Lưu thời gian đã chọn
                btnSetTimer.text =
                    "Time: ${getFormattedDate(dateTime)}" // 🔥 Hiển thị thời gian đã chọn
            }
        }

        builder.setView(layout) // Thiết lập view cho dialog

        builder.setPositiveButton("Ok") { _, _ -> // Thiết lập button ok
            val taskText = input.text.toString().trim() // Lấy text từ edittext
            if (taskText.isNotEmpty()) { // Kiểm tra text không rỗng
                val dateTimeToSave = selectedDateTime
                    ?: System.currentTimeMillis() //  Nếu không chọn thời gian, lấy thời gian hiện tại

                homeFragment.addTask(taskText, dateTimeToSave) // Thêm task vào home fragment
                saveTask(dateTimeToSave, taskText) //  Truyền timestamp (Long) thay vì String

                Toast.makeText(
                    this,
                    "Added: $taskText - ${getFormattedDate(dateTimeToSave)}",
                    Toast.LENGTH_SHORT
                ).show() // Hiển thị thông báo
            } else {
                Toast.makeText(this, "Task cannot be empty!", Toast.LENGTH_SHORT)
                    .show() // Hiển thị thông báo lỗi
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() } // Thiết lập button cancel
        builder.show() // Hiển thị dialog
    }

    /** ✅ Chuyển đổi giữa các Fragment */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction() // Bắt đầu transaction
            .replace(R.id.fragment_container, fragment) // Thay thế fragment
            .commit() // Commit transaction
    }

    /** ✅ Hiển thị bộ chọn ngày giờ */
    private fun showDateTimePicker(context: Context, onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance() // Lấy instance của Calendar

        DatePickerDialog(
            context, { _, year, month, day -> // Tạo DatePickerDialog
                calendar.set(Calendar.YEAR, year) // Thiết lập năm
                calendar.set(Calendar.MONTH, month) // Thiết lập tháng
                calendar.set(Calendar.DAY_OF_MONTH, day) // Thiết lập ngày

                TimePickerDialog(
                    context, { _, hour, minute -> // Tạo TimePickerDialog
                        calendar.set(Calendar.HOUR_OF_DAY, hour) // Thiết lập giờ
                        calendar.set(Calendar.MINUTE, minute) // Thiết lập phút
                        calendar.set(Calendar.SECOND, 0) // Thiết lập giây

                        onDateTimeSelected(calendar.timeInMillis) //  Trả về timestamp chính xác
                    }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE), true
                ).show() // Hiển thị TimePickerDialog

            }, calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show() // Hiển thị DatePickerDialog
    }

    @SuppressLint("MutatingSharedPrefs")
    private fun saveTask(timestamp: Long, task: String) {
        val sharedPref = getSharedPreferences("TASKS", MODE_PRIVATE) // Lấy SharedPreferences
        val editor = sharedPref.edit() // Lấy editor

        //  Chuyển timestamp thành ngày (dd/MM/yyyy)
        val dateKey = getFormattedDate(timestamp).split(" ")[0] // Lấy ngày từ timestamp

        val tasksForDate = sharedPref.getStringSet(dateKey, mutableSetOf())
            ?: mutableSetOf() // Lấy danh sách task theo ngày
        tasksForDate.add(task) // Thêm task mới vào danh sách ngày đó

        editor.putStringSet(dateKey, tasksForDate) // Lưu danh sách task
        editor.apply() // Áp dụng thay đổi

        println("✅ Đã lưu task vào ngày: $dateKey -> $tasksForDate") // In thông báo đã lưu
    }

    private fun getFormattedDate(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) // Tạo SimpleDateFormat
        return sdf.format(Date(timeInMillis)) // Định dạng ngày giờ
    }
}