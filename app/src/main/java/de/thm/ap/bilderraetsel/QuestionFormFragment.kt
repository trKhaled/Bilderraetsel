package de.thm.ap.bilderraetsel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.protobuf.Option
import de.thm.ap.bilderraetsel.ModuleFormFragment.Companion.IMAGE_PICK_CODE
import de.thm.ap.bilderraetsel.model.Question
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [QuestionFormFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuestionFormFragment : Fragment() {

    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var questionId: String = ""
    private var collectionId: String = ""

    private lateinit var questionTitle: TextView
    private lateinit var questionImage: ImageView
    private lateinit var questionSaveBtn: Button
    private lateinit var optionA: TextView
    private lateinit var optionB: TextView
    private lateinit var optionC: TextView
    private lateinit var optionD: TextView
    private lateinit var nextBtn: Button
    private lateinit var navController: NavController
    private lateinit var pickImage: Button
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_form_question, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        navController = Navigation.findNavController(view)
        pickImage = view.findViewById(R.id.pick_image_form)
        questionSaveBtn = view.findViewById(R.id.next_question_form)
        radioGroup = view.findViewById(R.id.radioGroup)
        // Get question id
        questionId = QuestionFragmentArgs.fromBundle(requireArguments()).questionId

        // Bind data
        questionTitle = view.findViewById(R.id.question_title_form)

        questionImage = view.findViewById(R.id.question_image_form)

        optionA = view.findViewById(R.id.optionAForm)
        optionB = view.findViewById(R.id.optionBForm)
        optionC = view.findViewById(R.id.optionCForm)
        optionD = view.findViewById(R.id.optionDForm)

        // Get question id
        collectionId = QuestionDetailFragmentArgs.fromBundle(requireArguments()).collectionId
        // Get question id
        questionId = QuestionDetailFragmentArgs.fromBundle(requireArguments()).questionId

        if(questionId != "null"){
            firebaseFirestore.collection("ModuleList").document(collectionId).collection("Questions")
                .document(questionId)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        // retrieving all questions in a collection
                        val document = task.result
                        Glide.with(requireContext()).load(document?.get("image").toString())
                            .centerCrop()
                            .into(questionImage)
                        questionTitle.text = document?.get("question").toString();
                        optionA.text = document?.get("optionA").toString();
                        optionB.text = document?.get("optionB").toString();
                        optionC.text = document?.get("optionC").toString();
                        optionD.text = document?.get("optionD").toString();
                        var radioId: Int = when (document?.get("answer").toString()) {
                            document?.get("optionA").toString() -> R.id.optionARbtn
                            document?.get("optionB").toString() -> R.id.optionBRbtn
                            document?.get("optionC").toString() -> R.id.optionCRbtn
                            document?.get("optionD").toString() -> R.id.optionDRbtn
                            else -> {
                                0
                            }
                        }
                        if (radioId != 0) radioGroup.check(radioId)
                    } else {
                        questionTitle.text = "Error retrieving from database!"
                    }
                }
            }
        pickImage.setOnClickListener {
            //check runtime permission
            if (context?.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                } == PackageManager.PERMISSION_DENIED) {
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, ModuleFormFragment.PERMISSION_CODE)
            } else {
                //permission already granted
                pickImageFromGallery()
            }
        }
        questionSaveBtn.setOnClickListener {
            if(!fieldsSet())
                return@setOnClickListener

            GlobalScope.launch() {
              val answer: String = when(radioGroup.checkedRadioButtonId) {
                  R.id.optionARbtn -> optionA.text.toString()
                  R.id.optionBRbtn -> optionB.text.toString()
                  R.id.optionCRbtn -> optionC.text.toString()
                  R.id.optionDRbtn -> optionD.text.toString()

                    else -> ""
                }
                val storageRef: StorageReference = firebaseStorage.reference
                val bitmap = (questionImage.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
                val filename: String = (1..20)
                    .map { allowedChars.random() }
                    .joinToString("")
                val uploadTask = storageRef.child("/$collectionId/$filename.jpg").putBytes(data)
                uploadTask.addOnFailureListener {
                }.addOnSuccessListener { taskSnapshot ->
                    GlobalScope.launch(){
                        val uri = storageRef.child("/$collectionId/$filename.jpg").downloadUrl.await()
                        if (questionId != "null") {
                            val questionRef = FirebaseService.getQuestion(collectionId, questionId)
                            questionRef.update("question",questionTitle.text.toString())
                            questionRef.update("optionA",optionA.text.toString())
                            questionRef.update("optionB",optionB.text.toString())
                            questionRef.update("optionC",optionC.text.toString())
                            questionRef.update("optionD",optionD.text.toString())
                            questionRef.update("answer",answer)
                            questionRef.update("image",uri.toString())
                        } else {
                            FirebaseService.insertQuestion(collectionId,
                                Question(
                                    question = questionTitle.text.toString(),
                                    optionA = optionA.text.toString(),
                                    optionB = optionB.text.toString(),
                                    optionC = optionC.text.toString(),
                                    optionD = optionD.text.toString(),
                                    answer = answer,
                                    image = uri.toString()
                                )
                            )
                        }
                    }
                }
            }
            navController.navigateUp()
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            questionImage.setImageURI(selectedImage)
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, ModuleFormFragment.IMAGE_PICK_CODE)
    }

    private fun fieldsSet(): Boolean {
        return questionImage.drawable != null
                && questionTitle.text.isNotEmpty()
                && optionA.text.isNotEmpty()
                && optionB.text.isNotEmpty()
                && optionC.text.isNotEmpty()
                && optionD.text.isNotEmpty()
                && radioGroup.checkedRadioButtonId != -1
    }
}