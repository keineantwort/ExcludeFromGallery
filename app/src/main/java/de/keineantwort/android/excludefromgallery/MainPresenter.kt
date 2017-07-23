package de.keineantwort.android.excludefromgallery

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.io.File

/**
 * Created by martin on 10.07.17.
 */
class MainPresenter(val context: Context) {

    fun folder(baseFolder: File): Single<List<Folder>>? {
        return Single.just(baseFolder)
                .subscribeOn(Schedulers.io())
                .map { it.listFiles().asList().filter { file: File? -> file != null && file.isDirectory } }
                .map {
                    val list = mutableListOf<Folder>()

                    it.forEach {
                        val folder = Folder(it)
                        list.add(folder)
                    }

                    list
                }
                .map {
                    it.forEach {
                        it.visible = !it.file.hasNoMedia()
                        it.hasSubFolder = it.file.hasSubFolder()
                    }
                    it
                }
                .map { it.sortedBy { it.file.name.toLowerCase() } }
    }

    fun toggleVisibility(folder: Folder): Single<Folder> {
        return Single.just(folder)
                .subscribeOn(Schedulers.io())
                .map {
                    val file = File(it.file, ".nomedia")
                    if (it.visible) {
                        file.createNewFile()
                    } else {
                        file.delete()
                    }

                    it
                }
                .map {
                    it.visible = !it.file.hasNoMedia()
                    it
                }
                .map {
                    val paths = it.file.listFiles().map { it.absolutePath }

                    MediaScannerConnection.scanFile(context, paths.toTypedArray(), null, object : MediaScannerConnection.OnScanCompletedListener {
                        override fun onScanCompleted(p0: String?, p1: Uri?) {
                            Timber.d("Scan finished - Path: %s", p0)
                            Timber.d("Scan finished -  Uri: %s", p1)
                        }
                    })
                    it
                }
    }
}

fun File.hasNoMedia(): Boolean {
    return this.list({ _, s -> s.equals(".nomedia", true) }).isNotEmpty()
}

fun File.hasSubFolder(): Boolean {
    return this.list({ file, _ -> file.isDirectory }).isNotEmpty()
}