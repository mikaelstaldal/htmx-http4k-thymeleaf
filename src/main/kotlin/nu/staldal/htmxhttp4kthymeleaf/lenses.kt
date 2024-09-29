package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.core.Body
import org.http4k.lens.FormField
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.lens.webForm

val firstNameField = FormField.string().required("firstName")
val lastNameField = FormField.string().required("lastName")
val emailField = FormField.string().required("email")
val personLens = Body.webForm(Validator.Strict, firstNameField, lastNameField, emailField)
    .map { PersonData(firstNameField(it), lastNameField(it), emailField(it)) }
    .toLens()

val idsField = FormField.string().multi.required("ids")
val idsLens =
    Body.webForm(Validator.Ignore, idsField).map { if (it.fields.containsKey("ids")) idsField(it) else emptyList() }
        .toLens()

val pageLens = Query.int().required("page")

val makeLens = Query.string().required("make")
