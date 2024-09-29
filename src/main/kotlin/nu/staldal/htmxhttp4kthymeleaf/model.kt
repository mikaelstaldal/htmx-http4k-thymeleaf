package nu.staldal.htmxhttp4kthymeleaf

interface Person {
    val firstName: String
    val lastName: String
    val email: String
}

data class PersonData(override val firstName: String, override val lastName: String, override val email: String) :
    Person

data class Contact(val id: String, val name: String, val email: String, var active: Boolean)

data class Agent(val number: Int, val name: String, val email: String, val id: String)

data class IdName(val id: String, val name: String)

data class Todo(val id: String, val description: String, var done: Boolean = false)
