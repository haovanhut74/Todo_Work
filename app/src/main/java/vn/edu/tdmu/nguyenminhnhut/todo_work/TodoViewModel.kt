package vn.edu.tdmu.nguyenminhnhut.todo_work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TodoViewModel : ViewModel() {
    private val _tasksUpdated = MutableLiveData<Unit>() // Khai báo biến MutableLiveData để theo dõi sự kiện cập nhật công việc
    val tasksUpdated: LiveData<Unit> get() = _tasksUpdated // LiveData để quan sát sự kiện cập nhật công việc

    private val _taskDeleted = MutableLiveData<TodoItem>() // Khai báo biến MutableLiveData để theo dõi sự kiện xóa công việc
    val taskDeleted: LiveData<TodoItem> get() = _taskDeleted // LiveData để quan sát sự kiện xóa công việc

    fun notifyTaskDeleted(task: TodoItem) {
        _taskDeleted.value = task // Cập nhật giá trị của _taskDeleted khi có công việc bị xóa
    }

}