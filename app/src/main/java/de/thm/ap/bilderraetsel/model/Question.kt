package de.thm.ap.bilderraetsel.model

import com.google.firebase.firestore.DocumentId

/**
 * Data model of the questions
 */
data class Question(

  @DocumentId
  var id: String = "",
  var question: String = "",
  var image: String = "",
  var optionA: String = "",
  var optionB: String = "",
  var optionC: String = "",
  var optionD: String = "",
  var answer: String = ""
)