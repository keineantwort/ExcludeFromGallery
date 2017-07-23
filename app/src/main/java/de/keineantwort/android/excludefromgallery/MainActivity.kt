package de.keineantwort.android.excludefromgallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import timber.log.Timber

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val presenter by lazy { MainPresenter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.d("Rootfolder: %s", Environment.getExternalStorageDirectory())

        folder_list.setHasFixedSize(true)
        folder_list.layoutManager = LinearLayoutManager(this)

        if (!hasPermission()) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_message)
                    .setPositiveButton(android.R.string.ok) {
                        dialogInterface, i ->
                        run {
                            dialogInterface.dismiss()
                            Timber.d("Let's request some permissions...")
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                                    1)
                        }
                    }
                    .create()
                    .show()
        } else {
            showDirectories()
        }
    }

    private fun showDirectories() {
        presenter.folder(Environment.getExternalStorageDirectory())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(Consumer {
                    Timber.d("Found %s Folders", it.size)
                    folder_list.adapter = FolderAdapter(it, presenter)
                })
    }

    fun hasPermission(): Boolean {
        return Build.VERSION.SDK_INT < 23 ||
                (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.d("YEAH! i'm Back!")
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showDirectories()
        }
    }
}
