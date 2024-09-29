package nu.staldal.htmxhttp4kthymeleaf

import org.http4k.template.ViewModel

interface HtmlViewModel : ViewModel {
    override fun template() = javaClass.simpleName + ".html"
}

@Suppress("ClassName")
object index : HtmlViewModel

data class ClickToEdit(val personData: PersonData) : HtmlViewModel, Person by personData
data class ViewPerson(val personData: PersonData) : HtmlViewModel, Person by personData
data class EditPerson(val personData: PersonData) : HtmlViewModel, Person by personData

data class BulkUpdate(val contacts: Collection<Contact>) : HtmlViewModel
data class ContactsList(val contacts: Collection<Pair<Contact, Boolean>>, val activate: Boolean) : HtmlViewModel

data class ClickToLoad(val agents: List<Agent>, val page: Int) : HtmlViewModel
data class AgentsList(val agents: List<Agent>, val page: Int) : HtmlViewModel

data class InfiniteScroll(val agents: List<Agent>, val page: Int) : HtmlViewModel
data class AgentsListInfinite(val agents: List<Agent>, val page: Int) : HtmlViewModel

data class ValueSelect(val makes: List<IdName>) : HtmlViewModel
data class Models(val models: List<IdName>) : HtmlViewModel
