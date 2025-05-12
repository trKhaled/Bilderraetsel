package de.thm.ap.bilderraetsel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [Fragment] subclass for module view
 * Use the [ModuleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ModuleFragment : Fragment(), ModuleAdapter.OnModuleClickListener {

    private lateinit var viewModel: ModuleViewModel
    private lateinit var listView: RecyclerView
    private lateinit var adapter: ModuleAdapter
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_module, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        viewModel = ViewModelProvider(this)[ModuleViewModel::class.java]
        listView = view.findViewById(R.id.list_view) as RecyclerView
        adapter = ModuleAdapter(this)

        listView.layoutManager = LinearLayoutManager(this.requireContext())
        // fixed size for each element
        listView.hasFixedSize()
        listView.adapter = adapter

        viewModel.module.observe(viewLifecycleOwner, {
            adapter.setModuleList(it)
            adapter.notifyDataSetChanged()
        })
        val newModuleBtn: Button = view.findViewById(R.id.new_module_btn)
        newModuleBtn.setOnClickListener {
            onNewModuleBtnClick()
        }
    }

    override fun onItemClick(position: Int) {
        val action = ModuleFragmentDirections.actionModuleListFragmentToDetailFragment()
        action.position = position
        navController.navigate(action)
    }

    override fun onChangeBtnClick(position: Int) {
        val action = ModuleFragmentDirections.actionModuleFragmentToModuleFormFragment()
        action.position = position
        navController.navigate(action)
    }

    override fun onNewModuleBtnClick() {
        val action = ModuleFragmentDirections.actionModuleFragmentToModuleFormFragment2()
        action.position = -1
        navController.navigate(action)
    }
}