package com.vaultic

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vaultic.core.secure.BiometricGate
import com.vaultic.ui.VaulticApp
import com.vaultic.ui.VaulticViewModel
import com.vaultic.ui.theme.VaulticTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaulticTheme {
                val appViewModel: VaulticViewModel = viewModel()
                VaulticApp(
                    viewModel = appViewModel,
                    onRequireBiometric = { reason, onSuccess, onError ->
                        BiometricGate(this).authenticate(reason, onSuccess, onError)
                    }
                )
            }
        }
    }
}
