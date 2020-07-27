package com.dragynslayr.magicdb2.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.Card
import com.dragynslayr.magicdb2.helper.log
import com.dragynslayr.magicdb2.view.Overlay
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.fragment_scan.view.*

class ScanFragment : Fragment() {

    private lateinit var cameraSource: CameraSource
    private lateinit var searchThread: Thread
    private lateinit var overlay: Overlay
    private lateinit var surface: SurfaceView

    private var scanning = false
    private var scanned = ""
    private var lastScanned = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_scan, container, false)
        overlay = v.overlayView
        surface = v.surfaceView

        delayScan()
        createSearchThread()
        startCameraSource()

        return v
    }

    private fun delayScan() {
        overlay.reset()
        Thread {
            Thread.sleep(2000)
            scanning = true
            scanned = ""
        }.start()
    }

    private fun createSearchThread() {
        searchThread = Thread {
            scanning = false
            if (scanned == lastScanned) {
                lastScanned = ""
                delayScan()
            } else {
                lastScanned = scanned
                delayScan()
            }
        }
    }

    private fun startCameraSource() {
        val recognizer = TextRecognizer.Builder(context).build()

        if (!recognizer.isOperational) {
            "Detector dependencies not loaded".log()
        } else {
            cameraSource =
                CameraSource.Builder(context, recognizer).setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1920, 1080).setAutoFocusEnabled(true)
                    .setRequestedFps(2f).build()

            surface.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(
                    holder: SurfaceHolder?,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                }

                override fun surfaceDestroyed(holder: SurfaceHolder?) {
                    cameraSource.stop()
                }

                override fun surfaceCreated(holder: SurfaceHolder?) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource.start(surface.holder)
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.CAMERA),
                            CAM_PERM_ID
                        )
                    }
                }
            })

            recognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {}

                override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
                    val items = detections?.detectedItems!!
                    if (scanning && items.size() != 0) {
                        scanned = items[0].value
                        scanned = Card.clean(scanned)
                        if (Card.isValid(scanned)) {
                            overlay.update(scanned)
                            searchThread.start()
                        } else {
                            overlay.reset()
                        }
                    }
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode != CAM_PERM_ID) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        } else {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    cameraSource.start(surface.holder)
                }
            } else {
                showDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scanning = false
        delayScan()
    }

    private fun showDialog() {
        AlertDialog.Builder(context).setCancelable(false).setTitle("Camera Permission not granted")
            .setMessage("Card scanning will not work without the camera, use the search function instead")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ -> }.show()
    }

    companion object {
        private const val CAM_PERM_ID = 101
    }
}