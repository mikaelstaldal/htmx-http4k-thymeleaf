package nu.staldal.htmxhttp4kthymeleaf

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import java.util.UUID

class ContactsStore {
    private val contacts: MutableMap<String, StoredContact> = listOf(
        StoredContact(
            UUID.randomUUID().toString(),
            firstName = "Joe",
            lastName = "Smith",
            phone = "070-12345678",
            email = "joe@smith.org"
        ),
        StoredContact(
            UUID.randomUUID().toString(),
            firstName = "Angie",
            lastName = "MacDowell",
            phone = "070-23456789",
            email = "angie@macdowell.org"
        ),
        StoredContact(
            UUID.randomUUID().toString(),
            firstName = "Fuqua",
            lastName = "Tarkenton",
            phone = "070-34567890",
            email = "fuqua@tarkenton.org"
        ),
        StoredContact(
            UUID.randomUUID().toString(),
            firstName = "Kim",
            lastName = "Yee",
            phone = "070-45678912",
            email = "kim@yee.org"
        ),
    ).associateBy { it.id }.toMutableMap()

    fun all(): Collection<StoredContact> = contacts.values

    fun search(text: String): Collection<StoredContact> = contacts.values.filter {
        it.firstName.contains(text) || it.lastName.contains(text) || it.phone.contains(text) || it.email.contains(text)
    }

    fun find(id: String): StoredContact? = contacts[id]

    fun add(contact: Contact): Result<StoredContact, ContactData> {
        val id = UUID.randomUUID().toString()
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
        if (!contact.email.contains("@")) return Failure(ContactData(email = "Invalid email"))
        if ((contacts.values.filter { it.id != id }.map { it.email }.toSet().contains(contact.email)))
            return Failure(ContactData(email = "Duplicate email"))
        return Success(contact)
    }
}
