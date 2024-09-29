package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.routing.ResourceLoader
import org.http4k.routing.static

fun webjar(name: String, version: String) =
    static(ResourceLoader.Companion.Classpath("/META-INF/resources/webjars/$name/$version/dist"))
