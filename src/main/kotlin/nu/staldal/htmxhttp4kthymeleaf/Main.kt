package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.core.*
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.FormField
import org.http4k.lens.Validator
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.webJars
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.ThymeleafTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

val firstNameField = FormField.string().required("firstName")
val lastNameField = FormField.string().required("lastName")
val emailField = FormField.string().required("email")
val personLens = Body.webForm(Validator.Strict, firstNameField, lastNameField, emailField)
    .map { PersonData(firstNameField(it), lastNameField(it), emailField(it)) }
    .toLens()

abstract class HtmlViewModel : ViewModel {
    override fun template() = javaClass.simpleName + ".html"
}

interface Person {
    val firstName: String
    val lastName: String
    val email: String
}

data class PersonData(override val firstName: String, override val lastName: String, override val email: String) :
    Person

data class ClickToEdit(val personData: PersonData) : HtmlViewModel(), Person by personData
data class ViewPerson(val personData: PersonData) : HtmlViewModel(), Person by personData
data class EditPerson(val personData: PersonData) : HtmlViewModel(), Person by personData

@Suppress("ClassName")
object index : HtmlViewModel()

private const val port = 8000

fun main() {
    val renderer = ThymeleafTemplates().CachingClasspath("templates")
    val htmlLens = Body.viewModel(renderer, TEXT_HTML).toLens()

    var person = PersonData("Bob", "Smith", "bsmith@example.com")

    val app = routes(
        "/" bind Method.GET to {
            Response(OK).with(htmlLens of index)
        },
        "/person" bind Method.GET to {
            Response(OK).with(htmlLens of ClickToEdit(person))
        },
        "/person/view" bind Method.GET to {
            Response(OK).with(htmlLens of ViewPerson(person))
        },
        "/person/edit" bind Method.GET to {
            Response(OK).with(htmlLens of EditPerson(person))
        },
        "/person" bind Method.PUT to { request ->
            person = personLens(request)
            println("Person updated: $person")
            Response(OK).with(htmlLens of ViewPerson(person))
        },
        webJars()
    )

    app.asServer(SunHttp(port)).start()
    println("Listening on $port")
}
