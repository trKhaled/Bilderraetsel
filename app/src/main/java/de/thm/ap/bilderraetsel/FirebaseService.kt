package de.thm.ap.bilderraetsel

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.ap.bilderraetsel.model.Achievements
import de.thm.ap.bilderraetsel.model.Module
import de.thm.ap.bilderraetsel.model.Question
import kotlinx.coroutines.tasks.await

object FirebaseService {

    private const val TAG = "FirebaseService"

    /**
     * method used to retrieve all documents inside firestore collection (ModuleList)
     * returns a list of modules if successful else an empty list
     */
    suspend fun getModuleData(): List<Module> {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("ModuleList").get().await()
                .documents.mapNotNull { it.toObject(Module::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting modules", e)
            // returns empty list if no element found
            emptyList()
        }
    }

    /**
     * method used to retrieve all documents inside firestore collection (Achievements)
     * returns a list of achievements if successful else an empty list
     */
    suspend fun getAchievmentsData(): List<Achievements> {
        val db = FirebaseFirestore.getInstance()
        return try {
            db.collection("Achievements").get().await()
                .documents.mapNotNull { it.toObject(Achievements::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting modules", e)
            // returns empty list if no element found
            emptyList()
        }
    }

    /**
     * add a module [m] to the collection ModuleList
     * returns a task of documentReference
     */
    fun insertModule(m: Module): Task<DocumentReference> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("ModuleList").add(m)
    }

    /**
     * get module from collection ModuleList using its [id]
     * returns a documentReference
     */
    fun getModule(id: String): DocumentReference {
        val db = FirebaseFirestore.getInstance()
        return db.collection("ModuleList").document(id)
    }

    /**
     * get module from collection ModuleList using its [id]
     * returns a documentReference
     */
    fun getQuestion(collectionId:String, questionId: String): DocumentReference {
        val db = FirebaseFirestore.getInstance()
        return db.collection("ModuleList").document(collectionId).collection("Questions").document(questionId)
    }

    /**
     * get module from collection ModuleList using its [id]
     * returns a documentReference
     */
    fun insertQuestion(collectionId: String, q: Question): Task<DocumentReference> {
        val db = FirebaseFirestore.getInstance()
        return db.collection("ModuleList").document(collectionId).collection("Questions").add(q)
    }
}