package com.ahmedpasha.smartfarm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ahmedpasha.smartfarm.balegh.*
import com.ahmedpasha.smartfarm.ui.viewmodel.FarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: FarmViewModel) {
    var input by remember { mutableStateOf("") }
    val messages by viewModel.baleghMessages.collectAsState()
    val state by viewModel.baleghUiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("بليغ — العقل التشغيلي") })
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                BaleghMessageItem(message, viewModel::confirmBaleghAction, viewModel::cancelBaleghAction)
            }
            if (state is BaleghUiState.Processing) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = viewModel::triggerBaleghVoice,
                modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary)
            ) { Icon(Icons.Default.Mic, "صوت", tint = Color.White) }
            OutlinedTextField(
                value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("اكتب طلبك لبليغ...") }, maxLines = 3, shape = RoundedCornerShape(24.dp)
            )
            IconButton(
                enabled = input.isNotBlank() && state !is BaleghUiState.Processing,
                onClick = { val text = input; input = ""; viewModel.sendMessageToBalegh(text) }
            ) { Icon(Icons.Default.Send, "إرسال") }
        }
    }
}

@Composable
private fun BaleghMessageItem(
    message: BaleghChatMessage,
    onConfirm: (BaleghActionProposal) -> Unit,
    onCancel: (BaleghActionProposal) -> Unit
) {
    val isUser = message.sender == BaleghChatMessage.Sender.USER
    val isSystem = message.sender == BaleghChatMessage.Sender.SYSTEM
    val alignment = if (isUser) Alignment.CenterEnd else if (isSystem) Alignment.Center else Alignment.CenterStart
    val background = if (isUser) MaterialTheme.colorScheme.primaryContainer
        else if (isSystem) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.secondaryContainer

    Box(Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(Modifier.widthIn(max = 340.dp).background(background, RoundedCornerShape(16.dp)).padding(12.dp)) {
            Text(message.text)
            message.proposal?.let { proposal ->
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text(proposal.description, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                when (message.resolved) {
                    null -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onConfirm(proposal) }) { Text("تنفيذ") }
                        OutlinedButton(onClick = { onCancel(proposal) }) { Text("إلغاء") }
                    }
                    true -> Text("تم التنفيذ", color = MaterialTheme.colorScheme.primary)
                    false -> Text("تم الإلغاء", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
