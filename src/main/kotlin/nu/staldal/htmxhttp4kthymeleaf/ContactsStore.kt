package nu.staldal.htmxhttp4kthymeleaf

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import java.security.MessageDigest
import kotlin.String

val emailRegex =
    Regex("""^[a-zA-Z0-9.!#${'$'}%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*${'$'}""")

@OptIn(ExperimentalStdlibApi::class)
fun sha256(data: String): String = MessageDigest.getInstance("SHA-256").apply {
    update(data.toByteArray())
}.digest().toHexString()

data class StoredContact(
    val id: String,
    override val firstName: String,
    override val lastName: String,
    override val phone: String,
    override val email: String,
) : Contact {
    constructor(
        firstName: String,
        lastName: String,
        phone: String,
        email: String,
    ) : this(
        id = sha256(email),
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email
    )
}

class ContactsStore {
    private val contacts: MutableMap<String, StoredContact> = (listOf(
        StoredContact(
            firstName = "Joe",
            lastName = "Smith",
            phone = "070-12345678",
            email = "joe@smith.org"
        ),
        StoredContact(
            firstName = "Angie",
            lastName = "MacDowell",
            phone = "070-23456789",
            email = "angie@macdowell.org"
        ),
        StoredContact(
            firstName = "Fuqua",
            lastName = "Tarkenton",
            phone = "070-34567890",
            email = "fuqua@tarkenton.org"
        ),
        StoredContact(
            firstName = "Kim",
            lastName = "Yee",
            phone = "070-45678912",
            email = "kim@yee.org"
        )
    ) + (1..100).map {
        StoredContact(
            firstName = "John",
            lastName = "Doe $it",
            phone = "1-234-567-1234",
            email = "john.doe$it@doe.com"
        )
    }
            ).associateBy { it.id }.toMutableMap()

    fun all(): Collection<StoredContact> = contacts.values

    fun search(text: String): Collection<StoredContact> = contacts.values.filter {
        it.firstName.contains(text) || it.lastName.contains(text) || it.phone.contains(text) || it.email.contains(text)
    }

    fun find(id: String): StoredContact? = contacts[id]

    fun add(contact: Contact): Result<StoredContact, ContactData> {
        val id = sha256(contact.email)
        return validate(contact, id).map {
            val newContact =
                StoredContact(id, firstName = it.firstName, lastName = it.lastName, phone = it.phone, email = it.email)
            contacts[id] = newContact
            newContact
        }
    }

    fun update(contact: StoredContact): Result<Unit, ContactData> =
        if (contacts.containsKey(contact.id)) {
            validate(contact, contact.id).map {
                contacts[contact.id] = contact
            }
        } else {
            Failure(ContactData())
        }

    fun delete(contact: StoredContact): Result<Unit, ContactData> =
        if (contacts.remove(contact.id) != null) {
            Success(Unit)
        } else {
            Failure(ContactData())
        }

    fun validate(contact: Contact, id: String): Result<Contact, ContactData> {
        if (contact.email.isEmpty()) return Failure(ContactData(email = "Missing email"))
        if (!emailRegex.matches(contact.email)) return Failure(ContactData(email = "Invalid email"))
        if ((contacts.values.filter { it.id != id }.map { it.email }.toSet().contains(contact.email)))
            return Failure(ContactData(email = "Duplicate email"))
        return Success(contact)
    }
}
