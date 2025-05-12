package de.thm.ap.bilderraetsel.model

import com.google.firebase.firestore.DocumentId

/**
 * Data model of the Achievements
 */
data class Achievements(

    @DocumentId
    var id: String = "",
    var title: String = "",
    var image: String = "",
    var description: String = ""

)