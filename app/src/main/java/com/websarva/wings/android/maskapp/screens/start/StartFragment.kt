package com.websarva.wings.android.maskapp.screens.start

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.websarva.wings.android.maskapp.R
import com.websarva.wings.android.maskapp.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentStartBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_start, container, false
        )

        val viewModelFactory = StartViewModelFactory()
        val startViewModel = ViewModelProvider(this, viewModelFactory)[StartViewModel::class.java]

        binding.lifecycleOwner = this
        binding.startViewModel = startViewModel

        startViewModel.language.observe(viewLifecycleOwner, Observer { language ->
            language?.let {
                if (allPermissionsGranted()) {
                    this.findNavController().navigate(
                        StartFragmentDirections.actionStartFragmentToDetectionFragment(language)
                    )
                } else {
                    requestPermissions(
                        REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                    )
                }
                startViewModel.onNavigateDetection()
            }
        })
        return binding.root
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val viewModelFactory = StartViewModelFactory()
        val startViewModel = ViewModelProvider(this, viewModelFactory)[StartViewModel::class.java]
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                this.findNavController().navigate(
                    StartFragmentDirections.actionStartFragmentToDetectionFragment(startViewModel.language.value)
                )
            }
            startViewModel.onNavigateDetection()
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

}