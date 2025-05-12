package de.thm.ap.bilderraetsel

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.thm.ap.bilderraetsel.model.Module
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream


/**
 * A simple [Fragment] subclass for module form view
 * Use the [ModuleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ModuleFormFragment : Fragment() {
    private lateinit var viewModel: ModuleViewModel
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var navController: NavController
    private lateinit var imgPickButton: Button
    private lateinit var saveModuleBtn: Button
    private var moduleId: String = "null"
    private lateinit var moduleImg: ImageView
    private lateinit var moduleName: EditText
    private lateinit var moduleDesc: EditText
    private lateinit var moduleLevel: EditText


    private var pos: Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            moduleImg.setImageURI(selectedImage)
        }
    }

    companion object {
        //image pick code
        val IMAGE_PICK_CODE = 1000

        //Permission code
        val PERMISSION_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_form_module, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        viewModel = ViewModelProvider(this)[ModuleViewModel::class.java]
        pos = DetailsFragmentArgs.fromBundle(requireArguments()).position

        navController = Navigation.findNavController(view)

        saveModuleBtn = view.findViewById(R.id.save_module)
        imgPickButton = view.findViewById(R.id.img_pick_btn)
        moduleImg = view.findViewById(R.id.module_image)
        moduleName = view.findViewById(R.id.module_name)
        moduleDesc= view.findViewById(R.id.module_desc)
        moduleLevel = view.findViewById(R.id.module_level)

        viewModel.module.observe(viewLifecycleOwner, {
            if(pos != -1) {
                Glide.with(requireContext()).load(it[pos].image).centerCrop().into(moduleImg)
                moduleId = it[pos].id
                // Retrieve question Id value
            firebaseFirestore.collection("ModuleList").document(moduleId).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val document = task.result
                        Glide.with(requireContext()).load(it[pos].image).centerCrop()
                            .into(moduleImg)
                        moduleDesc.setText(document?.data?.get("description").toString())
                        moduleName.setText(document?.data?.get("name").toString())
                        moduleLevel.setText(document?.data?.get("level").toString())

                        when {
                            document?.data?.get("level") == "Easy" -> {
                                moduleLevel.setTextColor(Color.parseColor("#FF0000"))
                            }
                            document?.data?.get("level") == "Intermediate" -> {
                                moduleLevel.setTextColor(Color.parseColor("#FFFF00"))
                            }
                            document?.data?.get("level") == "Hard" -> {
                                moduleLevel.setTextColor(Color.parseColor("#00FF00"))
                            }
                        }
                    } else {
                        Log.d(ContentValues.TAG, "Cached get failed: ", task.exception)
                    }
                }
            }
        })

        //BUTTON CLICK

        imgPickButton.setOnClickListener {
            //check runtime permission
            if (context?.let { checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) } ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else{
                //permission already granted
                pickImageFromGallery()
            }
        }
        saveModuleBtn.setOnClickListener {
            if(!fieldsSet()) return@setOnClickListener
            GlobalScope.launch() {
                val storageRef: StorageReference = firebaseStorage.reference
                val bitmap = (moduleImg.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
                val filename: String = (1..20)
                    .map { allowedChars.random() }
                    .joinToString("")
                val uploadTask = storageRef.child("/module_cover/$filename.jpg").putBytes(data)
                uploadTask.addOnFailureListener {
                }.addOnSuccessListener { taskSnapshot ->
                    GlobalScope.launch(){
                        val uri = storageRef.child("/module_cover/$filename.jpg").downloadUrl.await()
                        if (moduleId != "null") {
                            val moduleRef = FirebaseService.getModule(moduleId)
                            moduleRef.update("name",moduleName.text.toString())
                            moduleRef.update("description",moduleDesc.text.toString())
                            moduleRef.update("level",moduleLevel.text.toString())
                            moduleRef.update("image",uri.toString())
                        } else {
                            FirebaseService.insertModule(
                                Module(
                                    name = moduleName.text.toString(),
                                    description = moduleDesc.text.toString(),
                                    level = moduleLevel.text.toString(),
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
    private fun fieldsSet(): Boolean {
        return moduleImg.drawable != null
                && moduleName.text.isNotEmpty()
                && moduleDesc.text.isNotEmpty()
                && moduleLevel.text.isNotEmpty()
    }
}