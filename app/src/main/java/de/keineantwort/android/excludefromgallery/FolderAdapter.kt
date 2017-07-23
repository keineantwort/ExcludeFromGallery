package de.keineantwort.android.excludefromgallery

import android.animation.Animator
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import timber.log.Timber

/**
 * Created by martin on 16.07.17.
 */
class FolderAdapter(var items: List<Folder>, var presenter: MainPresenter) : RecyclerView.Adapter<FolderViewHolder>() {

    override fun onBindViewHolder(holder: FolderViewHolder?, position: Int) {
        holder?.initFolderView(items.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): FolderViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val view = inflater.inflate(R.layout.view_folder, parent, false)

        val vh = FolderViewHolder(view, presenter)

        return vh
    }

    override fun getItemCount(): Int = items.size

}

class FolderViewHolder(itemView: View?, var presenter: MainPresenter) : RecyclerView.ViewHolder(itemView) {

    var folderVisibleImg: ImageView? = itemView?.findViewById(R.id.folder_visible)
    var folderName: TextView? = itemView?.findViewById(R.id.folder_name)
    var folderIcon: ImageView? = itemView?.findViewById(R.id.folder)
    var subFolders: RecyclerView? = itemView?.findViewById(R.id.subfolders)

    fun initFolderView(folder: Folder) {
        setVisibilityIcon(folder)

        folderVisibleImg?.setOnClickListener {
            presenter.toggleVisibility(folder)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer { resultFolder ->
                        folderVisibleImg?.animate()?.alpha(0.0f)?.setListener(animationStopListener {
                            setVisibilityIcon(resultFolder)
                            folderVisibleImg?.animate()?.alpha(1.0f)?.start()
                        })?.start()
                    })
        }

        setClosedFolderIcon(folder)

        folderName?.text = folder.file.name
    }

    private fun setClosedFolderIcon(folder: Folder) {
        val iconResId = if (folder.hasSubFolder) R.drawable.ic_folder_has_subfolder_black_24dp else R.drawable.ic_folder_black_24dp
        folderIcon?.setImageResource(iconResId)
        subFolders?.visibility = View.GONE

        if (folder.hasSubFolder) {
            folderIcon?.setOnClickListener {
                presenter.folder(folder.file)
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe(Consumer {
                            setSubFolders(folder, it)
                        })
            }
        }
    }

    private fun setSubFolders(actual: Folder, subFolderList: List<Folder>) {
        Timber.d("Found %s Sub-Folders", subFolderList.size)
        subFolders?.visibility = View.VISIBLE
        subFolders?.setHasFixedSize(true)
        subFolders?.layoutManager = LinearLayoutManager(subFolders?.context)
        subFolders?.adapter = FolderAdapter(subFolderList, presenter)
        folderIcon?.setImageResource(R.drawable.ic_folder_open_black_24dp)
        folderIcon?.setOnClickListener { setClosedFolderIcon(actual) }
    }

    private fun setVisibilityIcon(folder: Folder) {
        val visibleResId = if (folder.visible) R.drawable.ic_visibility_black_24dp else R.drawable.ic_visibility_off_black_24dp
        folderVisibleImg?.setImageResource(visibleResId)

        val color = if (folder.visible) ContextCompat.getColor(folderVisibleImg?.context, android.R.color.holo_green_dark)
        else ContextCompat.getColor(folderVisibleImg?.context, android.R.color.holo_red_dark)

        folderVisibleImg?.setColorFilter(color)
    }

}

fun animationStopListener(body: (p0: Animator?) -> Unit): Animator.AnimatorListener {
    return object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {
        }

        override fun onAnimationEnd(p0: Animator?) {
            body(p0)
        }

        override fun onAnimationCancel(p0: Animator?) {
        }

        override fun onAnimationStart(p0: Animator?) {
        }
    }
}

fun onScanCompletedListener(body: (p0: String?, p1: Uri?) -> Unit): MediaScannerConnection.OnScanCompletedListener {
    return object : MediaScannerConnection.OnScanCompletedListener {
        override fun onScanCompleted(p0: String?, p1: Uri?) {
            body(p0, p1)
        }

    }
}