package io.least.case_management.ui.cases

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.least.case_management.data.CaseItem
import io.least.case_management.data.CaseListRepository
import io.least.case_management.data.Message
import io.least.case_management.viewmodel.CaseListConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jivesoftware.smack.android.AndroidSmackInitializer
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.bookmarks.BookmarkManager
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import kotlin.random.Random

class CaseListViewModel(
    private val connection: XMPPTCPConnection,
    private val caseListRepository: CaseListRepository,
    private val config: CaseListConfig
) : ViewModel() {
    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<CaseListUiState>(CaseListUiState.Initial)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<CaseListUiState> = _uiState


    // Get the MultiUserChatManager instance
    private lateinit var multiUserChatManager: MultiUserChatManager
    lateinit var establishedConnection: Deferred<XMPPTCPConnection>

    fun init(context: Context) {
        try {

            AndroidSmackInitializer.initialize(context)
            CoroutineScope(Dispatchers.Main).launch { _uiState.emit(CaseListUiState.Initial) }

            establishedConnection = viewModelScope.async(Dispatchers.IO) {
                connection.connect()
                Log.d("TEST", "Lgging in....")
                connection.login()
                Log.d("TEST", "Creating MultiUserChatManager....")
                multiUserChatManager = MultiUserChatManager.getInstanceFor(connection)

                withContext(Dispatchers.Main) { _uiState.emit(CaseListUiState.Connected) }
                connection
            }
        } catch (t: Throwable) {
            Log.e("TEST", "Error -> ${t.stackTraceToString()}")
            CoroutineScope(Dispatchers.Main).launch { _uiState.emit(CaseListUiState.Error(t.stackTraceToString())) }
        }
    }

    fun fetchCases() {
        Log.d("TEST", "fetchCases()")
        viewModelScope.launch(Dispatchers.IO) {
            val connection = establishedConnection.await()
            val bookmarkManager = BookmarkManager.getBookmarkManager(connection)
            val bookmarkedConferences = bookmarkManager.bookmarkedConferences
            Log.d("TEST", "Fetched bookmarks -> ${bookmarkedConferences.toString()}")
            // FIXME
            withContext(Dispatchers.Main) {
                _uiState.emit(CaseListUiState.UpdatedCaseList(bookmarkedConferences.map {
                    CaseItem(
                        it.jid.toString(),
                        it.name,
                        Message("", "", 0L, true, ""),
                        false
                    )
                }))
            }
        }
    }

    fun createNewCase() {
        val defer = CoroutineScope(Dispatchers.IO).async {
            try {
                // xmppServiceGroupDomain is specific for group chat for eg ("muc_localhost")
                val chatJid = "Case-${Random.nextInt()}@conference.jabber.uk"
                val mucJid: EntityBareJid = JidCreate.entityBareFrom(chatJid)

                Log.d("TEST", "Creating MultiUserChat....")
                // Get the multiuserchat instance
                val multiUserChat: MultiUserChat = multiUserChatManager.getMultiUserChat(mucJid)

                //userName can be your name by which you want to join the room
                //nickName is the name which will be shown on group chat
                val nickNameString = "coder_off"
                val nickNameRes = Resourcepart.from(nickNameString)
                multiUserChat.create(nickNameRes).makeInstant()

                Log.d("TEST", "Creating MultiUserChat config....")

                val mucEnterConfiguration = multiUserChat.getEnterConfigurationBuilder(nickNameRes)
                    .requestNoHistory()
                    .build()


                if (!multiUserChat.isJoined) {
                    Log.d("TEST", "Joining....")
                    multiUserChat.join(mucEnterConfiguration)
                    Log.d("TEST", "Joined")
                }
                // Store the room in the bookmark
                val bookmarkManager = BookmarkManager.getBookmarkManager(connection)
                bookmarkManager.addBookmarkedConference(
                    chatJid, mucJid, true, nickNameRes, null
                )

                // For listening incoming message
                multiUserChat.addMessageListener { Log.d("TEST", "--> Message: ${it.body}") }
                // incomingMessageListener is implemented below.
                fetchCases()
            } catch (t: Throwable) {
                Log.e("TEST", t.stackTraceToString())
                CoroutineScope(Dispatchers.Main).launch { _uiState.emit(CaseListUiState.Error(t.stackTraceToString())) }
            }
        }
    }
}

sealed class CaseListUiState {
    object Initial : CaseListUiState()
    object Connected : CaseListUiState()
    object Empty : CaseListUiState()
    class Error(val errorString: String) : CaseListUiState()
    data class UpdatedCaseList(val cases: List<CaseItem>) : CaseListUiState()
}
