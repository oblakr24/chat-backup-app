package com.rokoblak.chatbackup.services.parsing

import com.rokoblak.chatbackup.data.Message
import com.rokoblak.chatbackup.data.MinimalContact
import com.rokoblak.chatbackup.services.ConversationBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.core.Persister
import java.io.InputStream
import java.time.Instant
import javax.inject.Inject

class XMLParser @Inject constructor(private val builder: ConversationBuilder) {

    suspend fun parse(file: InputStream, filename: String): ImportResult =
        withContext(Dispatchers.IO) {
            val serializer = Persister()
            val wrapper = try {
                serializer.read(SMSWrapper::class.java, file)
            } catch (e: Throwable) {
                e.printStackTrace()
                return@withContext ImportResult.Error(e.message ?: "Parse failure")
            }

            val smses = wrapper.smses
            val messages = smses.map {
                val contactId = "C${it.address}"

                val timestampMs = it.date?.toLongOrNull() ?: Instant.EPOCH.toEpochMilli()
                val msgId = contactId + it.body.hashCode() + timestampMs
                val num = it.address ?: "/"
                Message(
                    id = msgId,
                    content = it.body.orEmpty(),
                    contact = MinimalContact(num, id = contactId),
                    timestamp = Instant.ofEpochMilli(timestampMs),
                    incoming = it.type == "1",
                )
            }

            ImportResult.Success(filename, builder.groupMessages(messages))
        }
}


@Root(name = "smses", strict = false)
class SMSWrapper {
    @field:ElementList(name = "smses", required = false)
    lateinit var smses: List<SMS>
}

@Root(strict = false, name = "REC")
class SMS {

    @field:Attribute(name = "address", required = false)
    var address: String? = null

    @field:Attribute(name = "body", required = false)
    var body: String? = null

    @field:Attribute(name = "date", required = false)
    var date: String? = null

    @field:Attribute(name = "contact_name", required = false)
    var contact_name: String? = null

    @field:Attribute(name = "type", required = false)
    var type: String? = null
}