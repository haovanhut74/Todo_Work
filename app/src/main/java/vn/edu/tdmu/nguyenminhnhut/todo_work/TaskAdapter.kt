package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color.WHITE
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("NAME_SHADOWING")
class TaskAdapter(
    context: Context,
    private val taskList: MutableList<TodoItem>,
    private val onDeleteClick: (Int) -> Unit,
    private val todoViewModel: TodoViewModel // Add ViewModel as a parameter
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE) // Khởi tạo SharedPreferences

    private var isDeleteEnabled = true // Trạng thái cho phép xóa

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTime : TextView = itemView.findViewById(R.id.taskTime)
        val taskName: TextView = itemView.findViewById(R.id.taskName) // TextView hiển thị tên task
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete) // ImageView nút xóa
        val taskCheckBox: CheckBox = itemView.findViewById(R.id.taskCheckBox) // CheckBox để đánh dấu hoàn thành task
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false) // Inflate layout cho item task
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position] // Lấy task hiện tại
        holder.taskName.text = task.name // Đặt tên task cho TextView
        holder.taskTime.text = task.name// Đặt thời gian task cho TextView
        holder.taskTime.text = getFormattedDate(task.timestamp)// Đặt thời gian task cho TextView
        // Xóa listener cũ trước khi cập nhật trạng thái
        holder.taskCheckBox.setOnCheckedChangeListener(null)

        // Dùng task.id thay vì task.name để lưu trạng thái trong SharedPreferences
        val isChecked = sharedPreferences.getBoolean(task.id, false)
        holder.taskCheckBox.isChecked = isChecked // Đặt trạng thái cho CheckBox
        updateTextStyle(holder.taskName, holder.taskCheckBox.isChecked) // Cập nhật kiểu chữ
        holder.taskName.setTextColor(if (isChecked) WHITE else WHITE) // Đặt màu chữ
        holder.taskCheckBox.isEnabled = !isChecked // Vô hiệu hóa CheckBox nếu đã hoàn thành

        // Khi checkbox thay đổi trạng thái
        holder.taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(task.id, isChecked).apply() // Lưu trạng thái vào SharedPreferences
            holder.taskCheckBox.isEnabled = !isChecked // Vô hiệu hóa CheckBox nếu đã hoàn thành
            updateTextStyle(holder.taskName, isChecked) // Cập nhật kiểu chữ
            if (isChecked) {
                todoViewModel.incrementCheckedTodos() // Increment checked Todos
            }
        }

        // Nếu là item cuối, thêm khoảng trống lớn
        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        params.bottomMargin = if (position == taskList.size - 1) 200 else 0
        holder.itemView.layoutParams = params

        // Xử lý sự kiện xóa với delay
        holder.btnDelete.setOnClickListener {
            if (isDeleteEnabled) {
                isDeleteEnabled = false // Tạm thời chặn xóa
                sharedPreferences.edit().remove(task.id).apply() // Xóa trạng thái trong SharedPreferences
                onDeleteClick(position) // Gọi callback xóa

                // Chờ 1.5 giây rồi mới cho phép xóa tiếp
                Handler(Looper.getMainLooper()).postDelayed({
                    isDeleteEnabled = true
                }, 1500)
            }
        }
    }

    override fun getItemCount(): Int = taskList.size // Trả về số lượng task

    private fun updateTextStyle(textView: TextView, isCompleted: Boolean) {
        textView.paintFlags = if (isCompleted) {
            textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG // Gạch ngang chữ nếu hoàn thành
        } else {
            textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv() // Bỏ gạch ngang nếu chưa hoàn thành
        }
        textView.setTextColor(WHITE) // Luôn giữ màu xanh
    }

    private fun getFormattedDate(timeInMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }
}