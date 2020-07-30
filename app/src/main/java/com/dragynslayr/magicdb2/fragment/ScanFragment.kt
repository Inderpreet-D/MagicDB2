package com.dragynslayr.magicdb2.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dragynslayr.magicdb2.R
import com.dragynslayr.magicdb2.data.Card
import com.dragynslayr.magicdb2.data.CardListAdapter
import com.dragynslayr.magicdb2.data.User
import com.dragynslayr.magicdb2.helper.log
import com.dragynslayr.magicdb2.view.Overlay
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_scan.view.*
import org.json.JSONObject

class ScanFragment : Fragment() {

    private lateinit var cameraSource: CameraSource
    private lateinit var overlay: Overlay
    private lateinit var surface: SurfaceView
    private lateinit var cards: ArrayList<Card>
    private lateinit var v: View
    private lateinit var user: User
    private lateinit var db: DatabaseReference

    private var scanning = false
    private var scanned = ""
    private var lastScanned = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_scan, container, false)
        overlay = v.overlay_view
        surface = v.surface_view

        db = Firebase.database.reference
        user =
            activity?.intent?.extras?.getSerializable(getString(R.string.user_object_key)) as User

        setupResultView()

        delayScan()
        startCameraSource()

        return v
    }

    private fun setupResultView() {
        cards = arrayListOf()
        with(v) {
            card_recycler.layoutManager = LinearLayoutManager(requireContext())
            card_recycler.adapter = CardListAdapter(cards)
            card_recycler.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    card_recycler.postDelayed({
                        card_recycler.scrollToPosition(0)
                    }, 100)
                }
            }
            hideResult()

            back_button.setOnClickListener {
                hideResult()
                delayScan()
            }

            add_button.setOnClickListener {
                cards.forEach {
                    if (it.amount!! > 0) {
                        "${it.name} -> ${it.amount}".log()
                        it.addToCollection(user, db)
                    }
                }
                hideResult()
                delayScan()
            }
        }
    }

    private fun hideResult() {
        requireActivity().runOnUiThread {
            with(v) {
                scan_result.visibility = View.GONE
                overlay_view.visibility = View.VISIBLE
            }
        }
    }

    private fun showResult() {
        requireActivity().runOnUiThread {
            with(v) {
                scan_result.visibility = View.VISIBLE
                overlay_view.visibility = View.GONE
            }
        }
    }

    private fun delayScan() {
        overlay.reset()
        Thread {
            Thread.sleep(2000)
            scanning = true
            scanned = ""
        }.start()
    }

    private fun startSearch() {
        Thread {
            scanning = false
            if (scanned == lastScanned) {
                lastScanned = ""
                delayScan()
            } else {
                lastScanned = scanned
                val json = Card.searchText(scanned)
                "$lastScanned ==> Read: $json".log()
                if (json.has("data") && v.scan_result.visibility == View.GONE) {
                    cards.clear()
                    val length = json.getInt("total_cards")
                    "Found $length card${if (length != 1) "s" else ""}".log()
                    val data = json.getJSONArray("data")
                    for (i in 0 until length) {
                        if (!data.isNull(i)) {
                            val card = data[i] as JSONObject
                            "$i -> $card".log()
                            val id = card.getString("id")
                            val name = card.getString("name")
                            cards.add(Card(id, name))
                        }
                    }
                    requireActivity().runOnUiThread {
                        v.card_recycler.adapter!!.notifyDataSetChanged()
                    }
                    showResult()
                } else {
                    "No data for $lastScanned".log()
                    delayScan()
                }
            }
        }.start()
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
                        requestPermissions(
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
                            startSearch()
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