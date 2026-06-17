package com.example.todolistapp


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

// Helper functions
fun getColorForPriority(priority: String): Color {
    return when(priority) {
        "High" -> Color(0xFFB3261E)
        "Medium" -> Color(0xFFFBBF24)
        "Low" -> Color(0xFF26A69A)
        else -> Color(0xFF6200EE)
    }
}

fun getColorForCategory(category: String): Color {
    return when(category) {
        "Personal" -> Color(0xFF6200EE)
        "Work" -> Color(0xFF03DAC5)
        "Shopping" -> Color(0xFF018786)
        "Study" -> Color(0xFFBB86FC)
        else -> Color(0xFF6200EE)
    }
}
@Composable
fun TodoItem(
    todo: Todo,
    isEditing: Boolean,
    onToggleComplete: () -> Unit,
    onStartEdit: () -> Unit,
    onSaveEdit: (String, String) -> Unit,
    onCancelEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var editTitle by remember { mutableStateOf(todo.title) }
    var editDescription by remember { mutableStateOf(todo.description) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isEditing) {
                // Edit mode
                Column {
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    
                    TextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        minLines = 3,
                        maxLines = 5
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onCancelEdit) {
                            Text("CANCEL")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { onSaveEdit(editTitle, editDescription) }) {
                            Text("SAVE")
                        }
                    }
                }
            } else {
                // View mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = todo.completed,
                        onCheckedChange = { onToggleComplete() }
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = todo.title,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                textDecoration = if (todo.completed) TextDecoration.LineThrough else null,
                                color = if (todo.completed) {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        if (todo.description.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = todo.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = if (todo.completed) TextDecoration.LineThrough else null
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(getColorForCategory(todo.category), CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = todo.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = todo.priority,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .background(getColorForPriority(todo.priority).copy(alpha = 0.2f), CircleShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(onClick = onStartEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    selectedPriority: Priority,
    onPriorityChange: (Priority) -> Unit,
    selectedCategory: Category,
    onCategoryChange: (Category) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Task",
                    style = MaterialTheme.typography.headlineSmall
                )

                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                
                TextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    minLines = 3,
                    maxLines = 5
                )

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium
                )

                var expandedPriority by remember { mutableStateOf(false) }
                var showCustomPriorityDialog by remember { mutableStateOf(false) }
                
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expandedPriority,
                        onExpandedChange = { expandedPriority = !expandedPriority }
                    ) {
                        TextField(
                            value = selectedPriority.getDisplayName(),
                            onValueChange = { },
                            label = { Text("Select priority") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }
                        ) {
                            Priority.values().forEach { priority ->
                                DropdownMenuItem(
                                    text = { Text(priority.getDisplayName()) },
                                    onClick = {
                                        onPriorityChange(priority)
                                        expandedPriority = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(priority.getColor()))
                                        )
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("+ Add custom priority") },
                                onClick = {
                                    expandedPriority = false
                                    showCustomPriorityDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Add, contentDescription = "Add")
                                }
                            )
                        }
                    }
                }
                
                if (showCustomPriorityDialog) {
                    var customPriorityName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showCustomPriorityDialog = false },
                        title = { Text("Add Custom Priority") },
                        text = {
                            Column {
                                TextField(
                                    value = customPriorityName,
                                    onValueChange = { customPriorityName = it },
                                    label = { Text("Priority name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showCustomPriorityDialog = false
                                }
                            ) {
                                Text("Add")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showCustomPriorityDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium
                )

                var expandedCategory by remember { mutableStateOf(false) }
                var showCustomCategoryDialog by remember { mutableStateOf(false) }
                
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory }
                    ) {
                        TextField(
                            value = selectedCategory.getDisplayName(),
                            onValueChange = { },
                            label = { Text("Select category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            Category.values().forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.getDisplayName()) },
                                    onClick = {
                                        onCategoryChange(category)
                                        expandedCategory = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color(category.getColor()))
                                        )
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("+ Add custom category") },
                                onClick = {
                                    expandedCategory = false
                                    showCustomCategoryDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Add, contentDescription = "Add")
                                }
                            )
                        }
                    }
                }
                
                if (showCustomCategoryDialog) {
                    var customCategoryName by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showCustomCategoryDialog = false },
                        title = { Text("Add Custom Category") },
                        text = {
                            Column {
                                TextField(
                                    value = customCategoryName,
                                    onValueChange = { customCategoryName = it },
                                    label = { Text("Category name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showCustomCategoryDialog = false
                                }
                            ) {
                                Text("Add")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showCustomCategoryDialog = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onAdd) {
                        Text("ADD")
                    }
                }
            }
        }
    }
}
@Composable
fun TodoApp() {
    val viewModel: TodoViewModel = viewModel()
    val todos by viewModel.todos.collectAsState()
    val newTodoTitle by viewModel.newTodoTitle.collectAsState()
    val newTodoDescription by viewModel.newTodoDescription.collectAsState()
    val editingTodoId by viewModel.editingTodoId.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    val activeTodos = todos.filter { !it.completed }
    val completedTodos = todos.filter { it.completed }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarTodoId by remember { mutableStateOf<String?>(null) }

    // Show snackbar when needed
    LaunchedEffect(snackbarTodoId) {
        if (snackbarTodoId != null) {
            val todo = todos.find { it.id == snackbarTodoId }
            if (todo != null) {
                val result = snackbarHostState.showSnackbar(
                    message = "Task '${todo.title}' marked as completed",
                    actionLabel = "UNDO"
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        viewModel.toggleTodoCompletion(snackbarTodoId!!)
                    }
                    SnackbarResult.Dismissed -> {
                        // Snackbar was dismissed
                    }
                }
                snackbarTodoId = null
            }
        }
    }

    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTodoDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Edit, contentDescription = "Active") },
                    label = { Text("Active") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Delete, contentDescription = "Completed") },
                    label = { Text("Completed") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = if (selectedTab == 0) "Active Tasks" else "Completed Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

                // Todo List
                val displayTodos = if (selectedTab == 0) activeTodos else completedTodos
                
                if (displayTodos.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedTab == 0) "No active tasks" else "No completed tasks",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (selectedTab == 0) "Tap + to add a new task" else "Complete some tasks to see them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayTodos) { todo ->
                            TodoItem(
                                todo = todo,
                                isEditing = editingTodoId == todo.id,
                                onToggleComplete = {
                                    viewModel.toggleTodoCompletion(todo.id)
                                    snackbarTodoId = todo.id
                                },
                                onStartEdit = { viewModel.startEditTodo(todo.id, todo.title, todo.description) },
                                onSaveEdit = { title, description -> viewModel.saveEditTodo(title, description) },
                                onCancelEdit = { viewModel.cancelEditTodo() },
                                onDelete = { viewModel.deleteTodo(todo.id) }
                            )
                        }
                    }
                }
            }


        }

        // Add Todo Dialog
        if (showAddTodoDialog) {
            AddTodoDialog(
                title = newTodoTitle,
                onTitleChange = { viewModel.onNewTodoTitleChange(it) },
                description = newTodoDescription,
                onDescriptionChange = { viewModel.onNewTodoDescriptionChange(it) },
                selectedPriority = selectedPriority,
                onPriorityChange = { viewModel.setPriority(it) },
                selectedCategory = selectedCategory,
                onCategoryChange = { viewModel.setCategory(it) },
                onAdd = {
                    viewModel.addTodo()
                    showAddTodoDialog = false
                },
                onDismiss = { showAddTodoDialog = false }
            )
        }
    }}
