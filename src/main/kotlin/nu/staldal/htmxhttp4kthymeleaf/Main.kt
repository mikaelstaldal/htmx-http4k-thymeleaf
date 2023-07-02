package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.TEXT_HTML
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.template.ThymeleafTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

data class Person(val name: String, val age: Int) : ViewModel {
    override fun template() = super.template() + ".html"
}

private const val port = 8000

fun main() {
    val renderer = ThymeleafTemplates().CachingClasspath("templates")
    val htmlLens = Body.viewModel(renderer, TEXT_HTML).toLens()

    val app: HttpHandler = {
        val bob = Person("Bob", 45)
        Response(OK).with(htmlLens of bob)
    }

    app.asServer(SunHttp(port)).start()
    println("Listening on $port")
}
