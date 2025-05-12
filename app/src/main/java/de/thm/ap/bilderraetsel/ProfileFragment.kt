package de.thm.ap.bilderraetsel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.thm.ap.bilderraetsel.databinding.FragmentProfileBinding

/**
 * A simple [Fragment] subclass for profile view
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    // viewBinding setup
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        navController = Navigation.findNavController(view)

        binding.apply{
            idTxt.text = currentUser?.uid
            nameTxt.text = currentUser?.displayName
            emailTxt.text = currentUser?.email
            Glide.with(this@ProfileFragment).load(currentUser?.photoUrl).into(profileImage)
            signOutBtn.setOnClickListener{
                auth.signOut()
                navController.navigate(R.id.action_profileFragment_to_loginFragment)
            }
            achievementsButton.setOnClickListener {
                navController.navigate(R.id.action_profileFragment_to_achievementsFragment)
            }
        }
    }

    /**
     * reset binding
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}