package de.thm.ap.bilderraetsel.model

import com.google.firebase.firestore.DocumentId

/**
 * Data model of a Module
 */
data class Module(

    @DocumentId
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var level: String = "",
    var questions: Long = 0,
    var image: String = ""
)