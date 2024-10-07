package com.example.lab08


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TaskViewModel(private val dao: TaskDao) : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    // Estado para el filtro
    private val _filterState = MutableStateFlow<TaskFilter>(TaskFilter.ALL)
    val filterState: StateFlow<TaskFilter> = _filterState

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            when (_filterState.value) {
                TaskFilter.ALL -> _tasks.value = dao.getAllTasks()
                TaskFilter.COMPLETED -> _tasks.value = dao.getTasksByStatus(true)
                TaskFilter.PENDING -> _tasks.value = dao.getTasksByStatus(false)
            }
        }
    }

    fun addTask(description: String) {
        viewModelScope.launch {
            dao.insertTask(Task(description = description))
            loadTasks()
        }
    }

    fun editTask(task: Task, newDescription: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(description = newDescription)
            dao.updateTask(updatedTask)
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            loadTasks()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dao.updateTask(updatedTask)
            loadTasks()
        }
    }

    fun setFilter(filter: TaskFilter) {
        viewModelScope.launch {
            _filterState.value = filter
            loadTasks()
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            loadTasks()
        }
    }
}

// TaskFilter.kt
enum class TaskFilter {
    ALL,
    COMPLETED,
    PENDING
}

class TaskViewModelFactory(private val dao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}