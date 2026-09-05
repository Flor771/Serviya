package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.models.ChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatListScreen(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allMessages by viewModel.chatRepo.messagesState.collectAsState()
    val allChambas by viewModel.chambaRepo.chambasState.collectAsState()

    // Group messages by chambaId
    val userMessages = allMessages.filter {
        it.senderId == currentUser?.uid || it.receiverId == currentUser?.uid
    }
    val groupedByChamba = userMessages.groupBy { it.chambaId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mensajes y Coordinación",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = ChambaNavyPrimary
                    )
                )
                Text(
                    text = "Coordina detalles directamente con clientes y trabajadores",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("chat_rooms_list"),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (groupedByChamba.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💬", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No tienes conversaciones activas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Cuando selecciones un trabajador o te seleccionen en una chamba, podrás chatear aquí.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(groupedByChamba.entries.toList()) { (chambaId, msgs) ->
                    val lastMsg = msgs.maxByOrNull { it.fecha }
                    val chamba = allChambas.firstOrNull { it.id == chambaId }
                    val isClient = currentUser?.uid == chamba?.clienteId
                    val partnerName = if (isClient) (chamba?.trabajadorSeleccionadoNombre?.ifEmpty { lastMsg?.senderNombre } ?: "Trabajador") else (chamba?.clienteNombre?.ifEmpty { "Cliente" } ?: "Cliente")
                    val partnerId = if (isClient) chamba?.trabajadorSeleccionadoId ?: "" else chamba?.clienteId ?: ""

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                viewModel.navigateTo(
                                    Screen.ChatConversation(
                                        chambaId = chambaId,
                                        otherUserId = partnerId,
                                        otherUserName = partnerName
                                    )
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ChambaNavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = partnerName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = partnerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (lastMsg != null) {
                                        Text(
                                            text = formatTimestamp(lastMsg.fecha),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (chamba != null) {
                                    Text(
                                        text = "Chamba: ${chamba.titulo}",
                                        fontSize = 11.sp,
                                        color = ChambaBlueAccent,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = lastMsg?.mensaje ?: "Sin mensajes",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatConversationScreen(
    chambaId: String,
    otherUserId: String,
    otherUserName: String,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allMessages by viewModel.chatRepo.messagesState.collectAsState()
    val chambas by viewModel.chambaRepo.chambasState.collectAsState()
    val chamba = chambas.firstOrNull { it.id == chambaId }

    val messages = allMessages.filter { it.chambaId == chambaId }.sortedBy { it.fecha }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(otherUserName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (chamba != null) {
                            Text(
                                text = chamba.titulo,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.Messages) },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Escribe un mensaje...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_text_input"),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.trim().isNotEmpty()) {
                                viewModel.sendChatMessage(chambaId, otherUserId, textInput)
                                textInput = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = ChambaNavyPrimary),
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUser?.uid
                ChatBubble(message = msg, isMe = isMe)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            color = if (isMe) ChambaNavyPrimary else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isMe) 0.dp else 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                if (!isMe) {
                    Text(
                        text = message.senderNombre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChambaBlueAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = message.mensaje,
                    fontSize = 13.sp,
                    color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(message.fecha),
                    fontSize = 9.sp,
                    color = if (isMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
