//package io.least.core.connector
//
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody
//
//
//open class RestConnector<T>(
//    private val url: String,
//) : Connector<T> {
//
//    override fun read(): GenericPayload<T> {
//        TODO("Not yet implemented")
//    }
//
//    override fun update(data: GenericPayload<T>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun delete(data: GenericPayload<T>) {
//        TODO("Not yet implemented")
//    }
//
//    override fun create(data: T): GenericPayload<T> {
//        val payload = dataCollector.collect(data)
//        val jsonString = Json.encodeToString(payload)
//        val jsonType = "application/json; charset=utf-8".toMediaType()
//        val client = OkHttpClient();
//        val body = RequestBody.create(jsonType, jsonString)
//        val request: Request = Request.Builder()
//            .url(url).post(body).build()
//        client.newCall(request).execute().use { response -> response.body?.string() }
//        return payload
//    }
//}