package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.core.Body
import org.http4k.lens.FormField
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.lens.webForm

val firstNameField = FormField.string().optional("firstName")
val lastNameField = FormField.string().optional("lastName")
val phoneField = FormField.string().optional("phone")
val emailField = FormField.string().optional("email")
val personLens = Body.webForm(Validator.Strict, firstNameField, lastNameField, emailField)
    .map { PersonData(firstNameField(it)!!, lastNameField(it)!!, emailField(it)!!) }
    .toLens()

val idsField = FormField.string().multi.required("ids")
val idsLens =
    Body.webForm(Validator.Ignore, idsField).map { if (it.fields.containsKey("ids")) idsField(it) else emptyList() }
        .toLens()

val pageLens = Query.int().required("page")

val makeLens = Query.string().required("make")

val qLens = Query.string().optional("q")

val contactLens = Body.webForm(Validator.Ignore, firstNameField, lastNameField, phoneField, emailField)
    .map {
        Contact(
            firstName = firstNameField(it) ?: "",
            lastName = lastNameField(it) ?: "",
            phone = phoneField(it) ?: "",
            email = emailField(it) ?: "",
        )
    }
    .toLens()

val idLens = Path.of("id")
