package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Method.*
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.isHtmx
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
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
            println("Person updated: ${dataStore.person}")
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
                Response(OK).with(htmlLens of AgentsListInfinite(dataStore.agents.drop(10 * page).take(10).toList(), page + 1))
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
    println("${if (activate) "Activating" else "Deactivating"} contacts: $ids")
    val mutated = ids.mapNotNull { id ->
        dataStore.contacts[id]?.let {
            if (it.active xor activate) {
                it.active = activate
                id
            } else null
        }
    }.toSet()
    return Response(OK).with(htmlLens of ContactsList(dataStore.contacts.values.map { it to mutated.contains(it.id) }, activate))
}
