package com.rokoblak.chatbackup.commonui

import androidx.annotation.VisibleForTesting
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.rokoblak.chatbackup.data.Contact
import com.rokoblak.chatbackup.data.Message
import com.rokoblak.chatbackup.data.MinimalContact
import com.rokoblak.chatbackup.ui.theme.*
import com.rokoblak.chatbackup.util.formatRelative
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import java.time.ZoneId

object PreviewDataUtils {

    private val firstNames =
        "John,Jessica,Su,Jane,Jacob,Rok,Rick,Omar,Hans,Anita,Christie,Christina,Peter,Franz,T.,Ursula,Richard,James,Maria,Mike,M.,Michael,Ana,Eve,Eva,Dwayne,Aaron,Euna,Matthew,Eugene"
    private val lastNames =
        "Smith,Doe,Anderson,Bay,Sue,Oblak,Zimmerman,Schwarzenegger,Tannenberg,Li,Kim,A.,B.,C.,D.,E.,F.,G."

    private val combinedNames = firstNames.split(",").flatMap { fn ->
        lastNames.split(",").map { ln ->
            "$fn $ln"
        }
    }

    private val names = listOf(
        "John Doe",
        "Jane Sue",
        "John McLane",
        "Unknown",
        "Google",
        "Mary Smith",
        "Someone With A Long Name",
        "Michael A.",
        "Jessica B.",
        "Sandra Bullock",
        "Mister Anderson",
        "TheRock",
        "Michael Bay",
        "T Mobile",
    ) + combinedNames

    private val sentences = listOf(
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris lobortis vitae mi nec aliquet. Morbi ligula massa, vulputate sit amet cursus at, varius vitae enim. ",
        "Vestibulum condimentum arcu odio, vitae bibendum urna eleifend id. Quisque vehicula mi risus, in pharetra nunc dictum eleifend.",
        "Donec pellentesque, orci facilisis vulputate semper, mi felis accumsan nisi, vel maximus ante ex nec odio. Donec fermentum nisl eu odio varius, non iaculis dui commodo.",
        "In sem lorem, consectetur nec elit eget, condimentum viverra metus. Duis cursus arcu eu ipsum laoreet laoreet. In vel feugiat nibh. Proin ac erat eros.",
        "Pellentesque egestas porta turpis, non dignissim nisi aliquet ac.",
        "Morbi luctus nibh sapien, eu vulputate leo consectetur vitae. Vestibulum iaculis sed tellus a tincidunt.",
        "Mauris consequat ligula orci, sit amet malesuada felis ultrices sed. Etiam porta eros ac est pellentesque malesuada.",
        "Aliquam erat volutpat. Duis dapibus urna at lorem vestibulum, eget luctus dui pretium.",
        "Fusce auctor efficitur nibh vel auctor. Cras tincidunt quam ornare sapien aliquet tincidunt.",
        "In lectus diam, egestas finibus imperdiet vitae, euismod eget dui. Vestibulum porttitor nunc risus, volutpat tristique orci egestas a.",
        "Vestibulum porttitor nunc risus, volutpat tristique orci egestas a.",
        "Cras tincidunt quam ornare sapien aliquet tincidunt.",
    )

    private val avatars = listOf(
        AvatarData.Initials("?", Color.DarkGray),
        AvatarData.Initials("AB", DarkRed),
        AvatarData.Initials("BC", Color.Blue),
        AvatarData.Initials("CD", DarkGreen),
        AvatarData.Initials("DE", DarkYellow),
        AvatarData.Initials("FG", DarkBrown),
        AvatarData.Initials("GH", DarkOrange),
        AvatarData.Initials("HI", DarkRed),
        AvatarData.Initials("JK", DarkOrange),
        AvatarData.Initials("KI", Color.Blue),
        AvatarData.Initials("IJ", DarkOrange),
    )

    val mockConversations = (0..10).map {
        val date = LocalDate.of(2022, 1 + it, 2 + it).atTime(15, 34, 34)
            .atZone(ZoneId.systemDefault())
        val dateFormatted = date.toInstant().formatRelative()
        ConversationDisplayData(
            contactId = "C_id1",
            id = "id$it",
            number = "num$it",
            title = AnnotatedString(names[it]),
            subtitle = AnnotatedString(sentences[it]),
            date = dateFormatted,
            checked = true,
            avatar = avatars[it],
        )
    }.toImmutableList()

    val mockChats = (0..20).map { idx ->
        val date = LocalDate.of(2022, 1 + idx.mod(11), 2 + idx).atTime(15, 34, 34)
            .atZone(ZoneId.systemDefault())
        val dateFormatted = date.toInstant().formatRelative()
        val isMine = idx.mod(2) == 0
        val content = sentences.takeAtMod(idx)
        val avatar = if (isMine.not()) avatars.takeAtMod(idx) else null
        ChatDisplayData(
            idx.toString(),
            content = content,
            date = dateFormatted,
            alignedLeft = isMine.not(),
            avatar = avatar,
            imageUri = null,
        )
    }.toImmutableList()

    private fun <T> List<T>.takeAtMod(idx: Int): T {
        return get(idx.mod(size))
    }

    @VisibleForTesting
    fun Message.obfuscate() = copy(
        content = sentences.takeAtMod(content.hashCode()),
        contact = contact.obfuscate(),
    )

    @VisibleForTesting
    fun Contact.obfuscate() = Contact(
        name = names.takeAtMod(id.hashCode()),
        orgNumber = orgNumber,
        avatarUri = avatarUri,
        phoneType = phoneType,
    )

    @VisibleForTesting
    fun MinimalContact.obfuscate() = MinimalContact(
        orgNumber = orgNumber,
    )
}