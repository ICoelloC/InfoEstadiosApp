package com.icoello.myapplication.ui.linterna

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.icoello.myapplication.R
import kotlinx.android.synthetic.main.fragment_linterna.*

class LinternaFragment : Fragment() {

    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var estado = false

    companion object{
        fun newInstance():LinternaFragment{
            return LinternaFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_linterna, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {

        val isFlashAvailable = context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        if (!isFlashAvailable!!) {
            showNoFlashError()
        }
        cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        linternaSwitch.setOnCheckedChangeListener { _, isChecked -> switchFlashLight(isChecked) }
    }

    private fun switchFlashLight(status:Boolean) {
        estado = status
        linternaSwitch.isChecked = estado
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager.setTorchMode(cameraId, status)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        if (status)
            linternaImagen.setImageResource(R.drawable.linterna)
        else
            linternaImagen.setImageResource(R.drawable.linterna)
    }

    private fun showNoFlashError() {
        val alert = AlertDialog.Builder(context)
            .create()
        alert.setTitle("Oops!")
        alert.setMessage("No hay Flash en este dispositivo...")
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, _ -> Log.i("Linterna", "Sin linterna") }
        alert.show()
    }
}