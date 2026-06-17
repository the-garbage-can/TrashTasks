package com.example.todolistapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class Todo(
    val id: String,
    val title: String,
    val description: String = "",
    val completed: Boolean,
    val priority: String = "Medium",
    val category: String = "Personal",
    val createdAt: Long = System.currentTimeMillis()
)

enum class Priority {
    HIGH, MEDIUM, LOW;
    
    fun getColor(): Long {
        return when(this) {
            HIGH -> 0xFFB3261E
            MEDIUM -> 0xFFFBBF24
            LOW -> 0xFF26A69A
        }
    }
    
    fun getDisplayName(): String {
        return when(this) {
            HIGH -> "High"
            MEDIUM -> "Medium"
            LOW -> "Low"
        }
    }
}

enum class Category {
    PERSONAL, WORK, SHOPPING, STUDY, OTHER;
    
    fun getDisplayName(): String {
        return when(this) {
            PERSONAL -> "Personal"
            WORK -> "Work"
            SHOPPING -> "Shopping"
            STUDY -> "Study"
            OTHER -> "Other"
        }
    }
    
    fun getColor(): Long {
        return when(this) {
            PERSONAL -> 0xFF6200EE
            WORK -> 0xFF03DAC5
            SHOPPING -> 0xFF018786
            STUDY -> 0xFFBB86FC
            OTHER -> 0xFF6200EE
        }
    }
}

class TodoViewModel : ViewModel() {
    private val _todos = MutableStateFlow<List<Todo>>(emptyList())
    val todos: StateFlow<List<Todo>> = _todos

    private val _newTodoTitle = MutableStateFlow("")
    val newTodoTitle: StateFlow<String> = _newTodoTitle
    
    private val _newTodoDescription = MutableStateFlow("")
    val newTodoDescription: StateFlow<String> = _newTodoDescription

    private val _editingTodoId = MutableStateFlow<String?>(null)
    val editingTodoId: StateFlow<String?> = _editingTodoId

    private var editingTodoTitle: String = ""
    private var editingTodoDescription: String = ""

    fun onNewTodoTitleChange(text: String) {
        _newTodoTitle.value = text
    }
    
    fun onNewTodoDescriptionChange(text: String) {
        _newTodoDescription.value = text
    }

    private val _selectedPriority = MutableStateFlow(Priority.MEDIUM)
    val selectedPriority: StateFlow<Priority> = _selectedPriority
    
    private val _selectedCategory = MutableStateFlow(Category.PERSONAL)
    val selectedCategory: StateFlow<Category> = _selectedCategory
    
    fun setPriority(priority: Priority) {
        _selectedPriority.value = priority
    }
    
    fun setCategory(category: Category) {
        _selectedCategory.value = category
    }
    
    fun addTodo() {
        val title = _newTodoTitle.value.trim()
        if (title.isNotEmpty()) {
            val newTodo = Todo(
                id = UUID.randomUUID().toString(),
                title = title,
                description = _newTodoDescription.value,
                completed = false,
                priority = _selectedPriority.value.getDisplayName(),
                category = _selectedCategory.value.getDisplayName()
            )
            _todos.value = _todos.value + newTodo
            _newTodoTitle.value = ""
            _newTodoDescription.value = ""
        }
    }

    fun startEditTodo(id: String, currentTitle: String, currentDescription: String) {
        _editingTodoId.value = id
        editingTodoTitle = currentTitle
        editingTodoDescription = currentDescription
    }

    fun saveEditTodo(newTitle: String, newDescription: String) {
        val title = newTitle.trim()
        if (title.isNotEmpty() && _editingTodoId.value != null) {
            _todos.value = _todos.value.map { todo ->
                if (todo.id == _editingTodoId.value) {
                    todo.copy(title = title, description = newDescription)
                } else {
                    todo
                }
            }
        }
        cancelEditTodo()
    }

    fun cancelEditTodo() {
        _editingTodoId.value = null
        editingTodoTitle = ""
        editingTodoDescription = ""
    }

    fun toggleTodoCompletion(id: String) {
        _todos.value = _todos.value.map { todo ->
            if (todo.id == id) {
                todo.copy(completed = !todo.completed)
            } else {
                todo
            }
        }
    }

    fun deleteTodo(id: String) {
        _todos.value = _todos.value.filter { it.id != id }
        // If we were editing the deleted todo, cancel editing
        if (_editingTodoId.value == id) {
            cancelEditTodo()
        }
    }
    
    // Statistics
    fun getStats(): TodoStats {
        val allTodos = _todos.value
        val completed = allTodos.count { it.completed }
        val pending = allTodos.size - completed
        val byCategory = Category.values().associateWith { category ->
            allTodos.count { it.category == category.getDisplayName() }
        }
        val byPriority = Priority.values().associateWith { priority ->
            allTodos.count { it.priority == priority.getDisplayName() }
        }
        return TodoStats(completed, pending, byCategory, byPriority)
    }
}

data class TodoStats(
    val completed: Int,
    val pending: Int,
    val byCategory: Map<Category, Int>,
    val byPriority: Map<Priority, Int>
)