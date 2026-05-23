package com.rrameshbtech.micromoves.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rrameshbtech.micromoves.data.Break
import com.rrameshbtech.micromoves.data.local.MicroMovesDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class BreaksListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = MicroMovesDatabase.getDatabase(application).breakDao()

    val breaks: StateFlow<List<Break>> = dao.getAllBreaks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
