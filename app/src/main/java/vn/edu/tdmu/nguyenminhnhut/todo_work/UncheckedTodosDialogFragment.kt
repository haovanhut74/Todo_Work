// UncheckedTodosDialogFragment.kt
package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class UncheckedTodosDialogFragment(private val uncheckedTodos: List<TodoItem>) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_unchecked_todos, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewUncheckedTodos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TaskAdapter(requireContext(), uncheckedTodos.toMutableList(), {}, TodoViewModel())
        return view
    }
}