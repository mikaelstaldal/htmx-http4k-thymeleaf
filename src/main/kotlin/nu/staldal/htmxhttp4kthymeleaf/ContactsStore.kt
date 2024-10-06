package nu.staldal.htmxhttp4kthymeleaf

import java.util.UUID

class ContactsStore {
    private val contacts: MutableMap<String, Contact> = listOf(
        Contact(
            UUID.randomUUID().toString(),
            firstName = "Joe",
            lastName = "Smith",
            phone = "070-12345678",
            email = "joe@smith.org"
        ),
        Contact(
            UUID.randomUUID().toString(),
            firstName = "Angie",
            lastName = "MacDowell",
            phone = "070-23456789",
            email = "angie@macdowell.org"
        ),
        Contact(
            UUID.randomUUID().toString(),
            firstName = "Fuqua",
            lastName = "Tarkenton",
            phone = "070-34567890",
            email = "fuqua@tarkenton.org"
        ),
        Contact(
            UUID.randomUUID().toString(),
            firstName = "Kim",
            lastName = "Yee",
            phone = "070-45678912",
            email = "kim@yee.org"
        ),
    ).associateBy { it.id!! }.toMutableMap()

    fun all(): Collection<Contact> = contacts.values

    fun search(text: String): Collection<Contact> = contacts.values.filter {
        it.firstName.contains(text) || it.lastName.contains(text) || it.phone.contains(text) || it.email.contains(text)
    }

    fun save(contact: Contact) {
        val id = contact.id ?: UUID.randomUUID().toString()
        contacts[id] = contact.copy(id = id)
    }

    fun find(id: String): Contact? = contacts[id]

    fun update(contact: Contact) {
        if (contact.id != null && contacts.containsKey(contact.id)) {
            contacts[contact.id] = contact
        } else {
            throw IllegalArgumentException("Invalid contact id: ${contact.id}")
        }
    }

    fun delete(contact: Contact) {
        if (contact.id != null && contacts.containsKey(contact.id)) {
            contacts.remove(contact.id)
        } else {
            throw IllegalArgumentException("Invalid contact id: ${contact.id}")
        }
    }
}
