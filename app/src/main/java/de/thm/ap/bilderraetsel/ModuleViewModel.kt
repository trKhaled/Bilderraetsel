package de.thm.ap.bilderraetsel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.thm.ap.bilderraetsel.model.Achievements
import de.thm.ap.bilderraetsel.model.Module
import de.thm.ap.bilderraetsel.model.Question
import kotlinx.coroutines.launch

class ModuleViewModel: ViewModel() {

    private val _module = MutableLiveData<List<Module>>()
    val module: LiveData<List<Module>> = _module

    private val _achievements = MutableLiveData<List<Achievements>>()
    val achievements: LiveData<List<Achievements>> = _achievements

    init {
        viewModelScope.launch {
            _module.value = FirebaseService.getModuleData()
            _achievements.value = FirebaseService.getAchievmentsData()
        }
    }
}