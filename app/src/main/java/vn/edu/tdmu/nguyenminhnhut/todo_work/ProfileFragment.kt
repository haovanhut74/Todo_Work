// ProfileFragment.kt
package vn.edu.tdmu.nguyenminhnhut.todo_work

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

class ProfileFragment: Fragment() {
    private lateinit var tvCheckedTodos: TextView
    private lateinit var todoViewModel: TodoViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tvUncheckedTodos: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile_fragment, container, false)
        tvUncheckedTodos = view.findViewById(R.id.tvUncheckedTodos)
        tvCheckedTodos = view.findViewById(R.id.tvCheckedTodos)
        todoViewModel = ViewModelProvider(requireActivity()).get(TodoViewModel::class.java)
        sharedPreferences = requireContext().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)

        todoViewModel.uncheckedTodos.observe(viewLifecycleOwner) { unchecked ->
            tvUncheckedTodos.text = "No Complete: $unchecked"
        }
        todoViewModel.checkedTodos.observe(viewLifecycleOwner) { checked ->
            tvCheckedTodos.text = "Complete: $checked"
        }
        updateTodoCounts()

        tvUncheckedTodos.setOnClickListener {
            showUncheckedTodosDialog()
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun updateTodoCounts() {
        val allTasks = sharedPreferences.all.filterKeys { it.startsWith("todo_") }
        val totalTodos = allTasks.size
        val checkedTodos = allTasks.values.count { it as Boolean }
        val uncheckedTodos = totalTodos - checkedTodos

        tvUncheckedTodos.text = "No Complete: $uncheckedTodos"
        tvCheckedTodos.text = "Complete: $checkedTodos"
    }

    private fun showUncheckedTodosDialog() {
        val allTasks = sharedPreferences.all.filterKeys { it.startsWith("todo_") }
        val uncheckedTodos = allTasks.filter { !(it.value as Boolean) }
            .map { TodoItem(it.key, it.key, 0L) } // Adjust this to match your TodoItem structure

        val dialog = UncheckedTodosDialogFragment(uncheckedTodos)
        dialog.show(parentFragmentManager, "UncheckedTodosDialogFragment")
    }
}