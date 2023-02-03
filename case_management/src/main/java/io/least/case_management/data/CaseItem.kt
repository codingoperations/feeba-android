package io.least.case_management.data

data class CaseItem(
    val id: String,
    val name: String,
    val lastMessage: Message,
    val hasUnread: Boolean
)

data class Message(
    val from: String,
    val profilePicUrl: String,
    val time: Long,
    val read: Boolean,
    val text: String
)