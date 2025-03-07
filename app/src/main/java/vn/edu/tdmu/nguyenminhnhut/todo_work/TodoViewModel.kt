package vn.edu.tdmu.nguyenminhnhut.todo_work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TodoViewModel : ViewModel() {
    private val _tasksUpdated = MutableLiveData<Unit>() // Khai báo biến MutableLiveData để theo dõi sự kiện cập nhật công việc
    private val _checkedTodos = MutableLiveData<Int>()
    private val _taskDeleted = MutableLiveData<TodoItem>() // Khai báo biến MutableLiveData để theo dõi sự kiện xóa công việc
    private val _uncheckedTodos = MutableLiveData<Int>()

    val taskDeleted: LiveData<TodoItem> get() = _taskDeleted // LiveData để quan sát sự kiện xóa công việc
    val checkedTodos: LiveData<Int> get() = _checkedTodos
    val tasksUpdated: LiveData<Unit> get() = _tasksUpdated // LiveData để quan sát sự kiện cập nhật công việc
    val uncheckedTodos: LiveData<Int> get() = _uncheckedTodos

    fun notifyTaskDeleted(task: TodoItem) {
        _taskDeleted.value = task // Cập nhật giá trị của _taskDeleted khi có công việc bị xóa
    }

    init {
        _checkedTodos.value = 0
        _uncheckedTodos.value = 0
    }

    fun incrementTotalTodos() {
        _uncheckedTodos.value = (_uncheckedTodos.value ?: 0) + 1
    }

    fun incrementCheckedTodos() {
        _checkedTodos.value = (_checkedTodos.value ?: 0) + 1
        _uncheckedTodos.value = (_uncheckedTodos.value ?: 0) - 1
    }
}