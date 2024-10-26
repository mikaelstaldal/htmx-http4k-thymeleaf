package nu.staldal.htmxhttp4kthymeleaf

import kotlin.random.Random

object ContactsArchiver : Runnable {
    enum class Status {
        WAITING, RUNNING, COMPLETE
    }

    fun get() = this // TODO for current user

    @Volatile
    private var archiveStatus = Status.WAITING

    @Volatile
    private var archiveProgress = 0.0

    fun status() = archiveStatus

    fun progress() = archiveProgress

    fun runIt() {
        if (archiveStatus == Status.WAITING) {
            archiveStatus = Status.RUNNING
            archiveProgress = 0.0
            Thread(this).start()
        }
    }

    override fun run() {
        for (i in 1..10) {
            Thread.sleep(Random.Default.nextInt(200, 700).toLong())
            if (archiveStatus != Status.RUNNING) return
            archiveProgress = i / 10.0
            println("Here... $archiveProgress")
        }
        Thread.sleep(1000L)
        if (archiveStatus != Status.RUNNING) return
        archiveStatus = Status.COMPLETE
    }

    fun reset() {
        archiveStatus = Status.WAITING
    }

    fun fileName() = "contacts.json"

    fun fileData() = """{        
        "contacts": []        
    }""".trimIndent()
}
