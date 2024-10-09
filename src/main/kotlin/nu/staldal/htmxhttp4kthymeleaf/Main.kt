package nu.staldal.htmxhttp4kthymeleaf

import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.Uri
import org.http4k.core.isHtmx
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
import org.http4k.filter.flash
import org.http4k.filter.removeFlash
import org.http4k.filter.withFlash
import org.http4k.lens.location
import org.http4k.routing.Router.Companion.orElse
import org.http4k.routing.bind
import org.http4k.routing.htmxWebjars
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.ThymeleafTemplates
import org.http4k.template.viewModel

private const val port = 8000

val renderer = ThymeleafTemplates().CachingClasspath("templates")
val htmlLens = Body.viewModel(renderer, TEXT_HTML).toLens()

fun main() {
    val dataStore = DataStore()
    val contactsStore = ContactsStore()

    val app = routes(
        "/" bind GET to {
            Response(OK).with(htmlLens of index)
        },

        "/click-to-edit" bind GET to {
            Response(OK).with(htmlLens of ClickToEdit(dataStore.person))
        },
        "/person" bind GET to {
            Response(OK).with(htmlLens of ViewPerson(dataStore.person))
        },
        "/person/edit" bind GET to {
            Response(OK).with(htmlLens of EditPerson(dataStore.person))
        },
        "/person" bind PUT to { request ->
            dataStore.person = personLens(request)
            Response(OK).with(htmlLens of ViewPerson(dataStore.person))
        },

        "/bulk-update" bind GET to {
            Response(OK).with(htmlLens of BulkUpdate(dataStore.contacts.values))
        },
        "/contacts/activate" bind PUT to { request ->
            activateOrDeactivateContact(request, true, dataStore)
        },
        "/contacts/deactivate" bind PUT to { request ->
            activateOrDeactivateContact(request, false, dataStore)
        },

        "/click-to-load" bind GET to routes(
            Request.isHtmx bind { request ->
                val page = pageLens(request)
                Response(OK).with(htmlLens of AgentsList(dataStore.agents.drop(10 * page).take(10).toList(), page + 1))
            },
            orElse bind { request ->
                Response(OK).with(htmlLens of ClickToLoad(dataStore.agents.take(10).toList(), 1))
            }
        ),

        "/infinite-scroll" bind GET to routes(
            Request.isHtmx bind { request ->
                val page = pageLens(request)
                Response(OK).with(
                    htmlLens of AgentsListInfinite(
                        dataStore.agents.drop(10 * page).take(10).toList(),
                        page + 1
                    )
                )
            },
            orElse bind { request ->
                Response(OK).with(htmlLens of InfiniteScroll(dataStore.agents.take(10).toList(), 1))
            }
        ),

        "/value-select" bind GET to {
            Response(OK).with(htmlLens of ValueSelect(dataStore.makes))
        },
        "/models" bind GET to { request ->
            val make = makeLens(request)
            dataStore.models[make]?.let {
                Response(OK).with(htmlLens of Models(it))
            } ?: Response(NOT_FOUND)
        },

        "/modal-dialog" bind GET to {
            Response(OK).with(htmlLens of ModalDialog)
        },
        "/modal" bind GET to {
            Response(OK).with(htmlLens of Modal)
        },

        "/contacts1" bind GET to { request ->
            val q = qLens(request)
            val contacts = if (q != null) {
                contactsStore.search(q)
            } else {
                contactsStore.all()
            }
            Response(OK).removeFlash().with(htmlLens of Contacts1(contacts, q, flash = request.flash()))
        },
        "/contacts1/new" bind GET to { request ->
            Response(OK).with(htmlLens of Contacts1New(ContactData(), ContactData()))
        },
        "/contacts1/new" bind POST to { request ->
            val contact = contactLens(request)
            contactsStore.add(contact).map {
                Response(SEE_OTHER).withFlash("Contact created").location(Uri.of("/contacts1"))
            }.mapFailure {
                Response(OK).with(htmlLens of Contacts1New(contact, it))
            }.get()
        },
        "/contacts1/{id}" bind GET to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                Response(OK).with(htmlLens of Contacts1View(contact))
            } else {
                Response(NOT_FOUND)
            }
        },
        "/contacts1/{id}/edit" bind GET to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                Response(OK).with(htmlLens of Contacts1Edit(contact, ContactData()))
            } else {
                Response(NOT_FOUND)
            }
        },
        "/contacts1/{id}/edit" bind POST to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                val newData = contactLens(request)
                val newContact = contact
                    .let { if (newData.firstName.isNotBlank()) it.copy(firstName = newData.firstName) else it }
                    .let { if (newData.lastName.isNotBlank()) it.copy(lastName = newData.lastName) else it }
                    .let { if (newData.phone.isNotBlank()) it.copy(phone = newData.phone) else it }
                    .let { if (newData.email.isNotBlank()) it.copy(email = newData.email) else it }
                contactsStore.update(newContact).map {
                    Response(SEE_OTHER).withFlash("Updated contact").location(Uri.of("/contacts1"))
                }.mapFailure {
                    Response(OK).with(htmlLens of Contacts1Edit(contact, it))
                }.get()
            } else {
                Response(NOT_FOUND)
            }
        },
        "/contacts1/{id}/delete" bind POST to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                contactsStore.delete(contact).map {
                    Response(SEE_OTHER).withFlash("Deleted contact").location(Uri.of("/contacts1"))
                }.mapFailure {
                    Response(SEE_OTHER).withFlash("Unable to delete contact").location(Uri.of("/contacts1"))
                }.get()
            } else {
                Response(NOT_FOUND)
            }
        },

        "/contacts2" bind GET to { request ->
            val q = qLens(request)
            val contacts = if (q != null) {
                contactsStore.search(q)
            } else {
                contactsStore.all()
            }
            Response(OK).removeFlash().with(htmlLens of Contacts2(contacts, q, flash = request.flash()))
        },
        "/contacts2/new" bind GET to { request ->
            Response(OK).with(htmlLens of Contacts2New(ContactData(), ContactData()))
        },
        "/contacts2/new" bind POST to { request ->
            val contact = contactLens(request)
            contactsStore.add(contact).map {
                Response(SEE_OTHER).withFlash("Contact created").location(Uri.of("/contacts2"))
            }.mapFailure {
                Response(OK).with(htmlLens of Contacts2New(contact, it))
            }.get()
        },
        "/contacts2/{id}" bind GET to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                Response(OK).with(htmlLens of Contacts2View(contact))
            } else {
                Response(NOT_FOUND)
            }
        },
        "/contacts2/{id}/edit" bind GET to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                Response(OK).with(htmlLens of Contacts2Edit(contact, ContactData()))
            } else {
                Response(NOT_FOUND)
            }
        },
        "/contacts2/{id}/edit" bind POST to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                val newData = contactLens(request)
                val newContact = contact
                    .let { if (newData.firstName.isNotBlank()) it.copy(firstName = newData.firstName) else it }
                    .let { if (newData.lastName.isNotBlank()) it.copy(lastName = newData.lastName) else it }
                    .let { if (newData.phone.isNotBlank()) it.copy(phone = newData.phone) else it }
                    .let { if (newData.email.isNotBlank()) it.copy(email = newData.email) else it }
                contactsStore.update(newContact).map {
                    Response(SEE_OTHER).withFlash("Updated contact").location(Uri.of("/contacts2"))
                }.mapFailure {
                    Response(OK).with(htmlLens of Contacts2Edit(contact, it))
                }.get()
            } else {
                Response(NOT_FOUND)
            }
        },
        "/contacts2/{id}" bind DELETE to { request ->
            val id = idLens(request)
            val contact = contactsStore.find(id)
            if (contact != null) {
                contactsStore.delete(contact).map {
                    Response(SEE_OTHER).withFlash("Deleted contact").location(Uri.of("/contacts2"))
                }.mapFailure {
                    Response(SEE_OTHER).withFlash("Unable to delete contact").location(Uri.of("/contacts2"))
                }.get()
            } else {
                Response(NOT_FOUND)
            }
        },

        htmxWebjars(),
        webjar("bootstrap", "5.3.3"),
    )

    ServerFilters.CatchAll { t ->
        t.printStackTrace()
        Response(Status.INTERNAL_SERVER_ERROR)
    }.then(app).asServer(SunHttp(port)).start()
    println("Listening on $port")
}

private fun activateOrDeactivateContact(request: Request, activate: Boolean, dataStore: DataStore): Response {
    val ids = idsLens(request)
    val mutated = ids.mapNotNull { id ->
        dataStore.contacts[id]?.let {
            if (it.active xor activate) {
                it.active = activate
                id
            } else null
        }
    }.toSet()
    return Response(OK).with(
        htmlLens of ContactsList(
            dataStore.contacts.values.map { it to mutated.contains(it.id) },
            activate
        )
    )
}
