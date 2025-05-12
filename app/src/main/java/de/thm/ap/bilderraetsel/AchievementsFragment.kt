package de.thm.ap.bilderraetsel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


/**
 * A simple [Fragment] subclass for Achievements
 * Use the [AchievementsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AchievementsFragment : Fragment() {

    private lateinit var achievementsListView: RecyclerView
    private lateinit var adapter: AchievementsAdapter
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var viewModel: ModuleViewModel
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_achievements, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()

        viewModel = ViewModelProvider(this)[ModuleViewModel::class.java]
        achievementsListView = view.findViewById(R.id.achievement_list) as RecyclerView
        adapter = AchievementsAdapter()
        navController = Navigation.findNavController(view)

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            userId = auth.currentUser!!.uid
        } else {
            navController.navigate(R.id.moduleFragment)
        }
        achievementsListView.layoutManager = LinearLayoutManager(this.requireContext())
        // fixed size for each element
        achievementsListView.hasFixedSize()
        achievementsListView.adapter = adapter

        viewModel.achievements.observe(viewLifecycleOwner, {
            adapter.setAchievementsList(it)
            adapter.notifyDataSetChanged()
        })
    }
}