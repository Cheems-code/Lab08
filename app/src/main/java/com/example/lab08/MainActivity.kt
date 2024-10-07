package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    private lateinit var db: TaskDatabase
    private lateinit var viewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "task_db"
        ).build()

        val taskDao = db.taskDao()
        val factory = TaskViewModelFactory(taskDao)
        viewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                TaskScreenWithBackground(viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}

@Composable
fun TaskScreenWithBackground(viewModel: TaskViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondo_lab08),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido semi-transparente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.9f))
        ) {
            TaskScreenContent(viewModel)
        }
    }
}

@Composable
fun TaskScreenContent(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    var newTaskDescription by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editingDescription by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Lista de Tareas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Campo para nueva tarea con estilo mejorado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .shadow(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TextField(
                    value = newTaskDescription,
                    onValueChange = { newTaskDescription = it },
                    label = { Text("Nueva tarea") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )

                Button(
                    onClick = {
                        if (newTaskDescription.isNotEmpty()) {
                            viewModel.addTask(newTaskDescription)
                            newTaskDescription = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Agregar tarea")
                }
            }
        }

        // Filtros con estilo mejorado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = filterState == TaskFilter.ALL,
                    onClick = { viewModel.setFilter(TaskFilter.ALL) },
                    label = { Text("Todas") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    )
                )
                FilterChip(
                    selected = filterState == TaskFilter.COMPLETED,
                    onClick = { viewModel.setFilter(TaskFilter.COMPLETED) },
                    label = { Text("Completadas") }
                )
                FilterChip(
                    selected = filterState == TaskFilter.PENDING,
                    onClick = { viewModel.setFilter(TaskFilter.PENDING) },
                    label = { Text("Pendientes") }
                )
            }
        }

        // Lista de tareas con estilo mejorado
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp)
            ) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        editingTask = editingTask,
                        editingDescription = editingDescription,
                        onEditStart = {
                            editingTask = task
                            editingDescription = task.description
                        },
                        onEditComplete = {
                            viewModel.editTask(task, editingDescription)
                            editingTask = null
                        },
                        onEditCancel = { editingTask = null },
                        onDelete = { viewModel.deleteTask(task) },
                        onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                        onEditDescriptionChange = { editingDescription = it }
                    )
                }
            }
        }

        // Botón eliminar todas con estilo mejorado
        Button(
            onClick = { viewModel.deleteAllTasks() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Eliminar todas las tareas")
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    editingTask: Task?,
    editingDescription: String,
    onEditStart: () -> Unit,
    onEditComplete: () -> Unit,
    onEditCancel: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    onEditDescriptionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (editingTask?.id == task.id) {
            // Modo edición
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = editingDescription,
                    onValueChange = onEditDescriptionChange,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                IconButton(onClick = onEditComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Guardar")
                }
                IconButton(onClick = onEditCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }
            }
        } else {
            // Modo visualización
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.description,
                    modifier = Modifier.weight(1f),
                    textDecoration = if (task.isCompleted)
                        TextDecoration.LineThrough else TextDecoration.None
                )
                Row {
                    IconButton(onClick = onEditStart) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                    IconButton(onClick = onToggleComplete) {
                        Icon(
                            if (task.isCompleted)
                                Icons.Default.CheckBox
                            else
                                Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Completar"
                        )
                    }
                }
            }
        }
    }
}