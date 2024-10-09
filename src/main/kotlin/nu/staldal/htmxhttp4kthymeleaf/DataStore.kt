package nu.staldal.htmxhttp4kthymeleaf

import java.util.UUID

class DataStore {
    var person = PersonData("Bob", "Smith", "bsmith@example.com")

    val contacts: Map<String, ContactInList> = listOf(
        ContactInList(UUID.randomUUID().toString(), "Joe Smith", "joe@smith.org", true),
        ContactInList(UUID.randomUUID().toString(), "Angie MacDowell", "angie@macdowell.org", true),
        ContactInList(UUID.randomUUID().toString(), "Fuqua Tarkenton", "fuqua@tarkenton.org", true),
        ContactInList(UUID.randomUUID().toString(), "Kim Yee", "kim@yee.org", false)
    ).associateBy { it.id }

    val agents = generateSequence(Agent(1, "Agent Smith", "void1@null.com", UUID.randomUUID().toString())) {
        Agent(it.number + 1, "Agent Smith", "void${it.number + 1}@null.com", UUID.randomUUID().toString())
    }

    val makes = listOf(IdName("audi", "Audi"), IdName("toyota", "Toyota"), IdName("bmw", "BMW"))
    val models = mapOf(
        "audi" to listOf(IdName("a1", "A1"), IdName("a3", "A3"), IdName("a6", "A6")),
        "toyota" to listOf(IdName("landcruiser", "Landcruiser"), IdName("tacoma", "Tacoma"), IdName("yaris", "Yaris")),
        "bmw" to listOf(IdName("325i", "325i"), IdName("325ix", "325ix"), IdName("X5", "X5"))
    )
}
