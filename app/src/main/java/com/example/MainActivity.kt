package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.database.AppDatabase
import androidx.room.Room
import com.example.repository.StoreRepository
import com.example.ui.MainStoreScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.StoreViewModel
import com.example.viewmodel.StoreViewModelFactory

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "bogdadi_store_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val repository by lazy {
        StoreRepository(db.storeDao())
    }

    private val viewModel: StoreViewModel by viewModels {
        StoreViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainStoreScreen(viewModel = viewModel)
            }
        }
    }
}
