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

data class BulkUpdate(val contacts: Collection<ContactInList>) : HtmlViewModel
data class ContactsList(val contacts: Collection<Pair<ContactInList, Boolean>>, val activate: Boolean) : HtmlViewModel

data class ClickToLoad(val agents: List<Agent>, val page: Int) : HtmlViewModel
data class AgentsList(val agents: List<Agent>, val page: Int) : HtmlViewModel

data class InfiniteScroll(val agents: List<Agent>, val page: Int) : HtmlViewModel
data class AgentsListInfinite(val agents: List<Agent>, val page: Int) : HtmlViewModel

data class ValueSelect(val makes: List<IdName>) : HtmlViewModel
data class Models(val models: List<IdName>) : HtmlViewModel

data object ModalDialog : HtmlViewModel
data object Modal : HtmlViewModel

data class Contacts1(
    val contacts: Collection<StoredContact>, val q: String?, val page: Int, val pageSize: Int,
    val flash: String? = null
) : HtmlViewModel

data class Contacts1New(val contact: Contact, val errors: Contact) : HtmlViewModel
data class Contacts1View(val contact: StoredContact) : HtmlViewModel
data class Contacts1Edit(val contact: StoredContact, val errors: Contact) : HtmlViewModel

data class Contacts2(
    val contacts: Collection<StoredContact>, val q: String?, val page: Int, val pageSize: Int,
    val archiveStatus: ContactsArchiver.Status, val archiveProgress: Double,
    val flash: String? = null
) : HtmlViewModel
data class Contacts2Rows(
    val contacts: Collection<StoredContact>, val q: String?, val page: Int, val pageSize: Int,
) : HtmlViewModel

data class Contacts2New(val contact: Contact, val errors: Contact) : HtmlViewModel
data class Contacts2View(val contact: StoredContact) : HtmlViewModel
data class Contacts2Edit(val contact: StoredContact, val errors: Contact) : HtmlViewModel

data class Contacts2ArchiveUI(val archiveStatus: ContactsArchiver.Status, val archiveProgress: Double) : HtmlViewModel

data class Contacts2Sync(val isUpdated: Boolean) : HtmlViewModel
