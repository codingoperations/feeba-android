package io.least.case_management.ui.chat

import androidx.lifecycle.ViewModel
import io.least.case_management.data.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.bouncycastle.asn1.x500.style.RFC4519Style.uid
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.mam.MamManager


class ChatListViewModel(private val connection: XMPPTCPConnection) : ViewModel() {

    // Backing property to avoid state updates from other classes
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initial)

    // The UI collects from this StateFlow to get its state updates
    val uiState: StateFlow<ChatUiState> = _uiState

    var chatManager: ChatManager = ChatManager.getInstanceFor(connection)


    fun init() {
//        val mamManager = MamManager.getInstanceFor(connection)
//        val isSupported = mamManager.isSupported
//        val enableMamForAllMessages = mamManager.enableMamForAllMessages()
//        val mamPrefs: MamManager.MamPrefs = mamManager.retrieveArchivingPreferences().asMamPrefs()
//        val mamQueryArgs = MamManager.MamQueryArgs.builder().limitResultsToJid(jid)
//            .setResultPageSizeTo(10).queryLastPage().build()
//        val mamQuery = mamManager.queryArchive(mamQueryArgs)
//        val messages = mamQuery.messages
//        val fields = mamManager.retrieveFormFields()
//        messages.map { Message(it.body) }
    }

    fun sendMessage(text: String) {
        val message = connection.stanzaFactory
            .buildMessageStanza()
            .to("jsmith@jivesoftware.com")
            .setBody(text)
            .build()
        connection.sendStanza(message)

    }

    private fun initMam() {
//        // check the connection object
//        if (connection != null) {
//            //get the instance of MamManager
//            mamManager = MamManager.getInstanceFor(multiUserChat)
//            //enable it for fetching messages
//            mamManager.enableMamForAllMessages()
//            // Function for fetching messages
//            disposableMessages = getObservableMessages()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ listOfMessages ->
//                    tempMessageList = listOfMessages
//                }, { t ->
//                    Log.e(LOG_TAG, "-> initMam -> onError ->", t)
//                }, {
//                    messageList.value = tempMessageList
//                    tempList.clear()
//                })
//        }
    }
    // number_of_messages_to_fetch it's a limit of messages to be fetched eg. 20.
//    private fun getObservableMessages(): Observable<List<Message>> {
//        return Observable.create<List<Message>> { source ->
//            try {
//                val mamQuery = mamManager.queryMostRecentPage(mucJid, number_of_messages_to_fetch)
//                if (mamQuery.messageCount == 0 || mamQuery.messageCount < number_of_messages_to_fetch) {
//                    uid = ""
//                    doMoreLoading = false
//                } else {
//                    uid = mamQuery.mamResultExtensions[0].id
//                    doMoreLoading = true
//                }
//                source.onNext(mamQuery.messages)
//
//            } catch (e: Exception) {
//                if (!connection.isConnected) {
//                    source.onError(e)
//                } else {
//                    Log.e("ChatDetail", "Connection closed")
//                }
//            }
//            source.onComplete()
//        }
//    }
}

sealed class ChatUiState {
    object Initial : ChatUiState()
    object Empty : ChatUiState()
    data class ChatUpdated(val messages: List<Message>) : ChatUiState()
}
