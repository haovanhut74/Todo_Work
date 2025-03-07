package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalendarTaskAdapter(private val taskList: List<TodoItem>) :
    RecyclerView.Adapter<CalendarTaskAdapter.ViewHolder>() {

    // ViewHolder chứa các view của item
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.calendarTaskName) // TextView hiển thị tên công việc
    }

    // Tạo ViewHolder mới khi RecyclerView cần
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_task, parent, false) // Inflate layout cho item
        return ViewHolder(view) // Trả về ViewHolder mới
    }

    // Gán dữ liệu cho ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = taskList[position] // Lấy công việc hiện tại
        holder.taskName.text = task.name // Đặt tên công việc cho TextView
    }

    // Trả về số lượng item trong danh sách
    override fun getItemCount(): Int = taskList.size
}