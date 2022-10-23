package io.least.case_management

import io.least.case_management.data.CaseListRepository
import io.least.core.ServerConfig
import io.least.core.ServiceLocator
import org.jivesoftware.smack.tcp.XMPPTCPConnection


object ServiceLocator {
    private lateinit var xMPPTCPConnection: XMPPTCPConnection

    fun getXMPPTCPConnection(): XMPPTCPConnection {
        if (!this::xMPPTCPConnection.isInitialized) {
            xMPPTCPConnection = XMPPTCPConnection("coder_off@jabber.uk", "12345678")
        }
        return xMPPTCPConnection
    }


    fun getCaseListRepository(hostUrl: String): CaseListRepository {
        return ServiceLocator.retrofitInstance(ServerConfig(hostUrl, "uz", ""))
            .create(CaseListRepository::class.java)
    }
}