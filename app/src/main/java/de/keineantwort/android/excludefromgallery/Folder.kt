package de.keineantwort.android.excludefromgallery

import java.io.File

/**
 * Created by martin on 17.07.17.
 */
data class Folder(val file: File, var open: Boolean = false, var visible: Boolean = false, var hasSubFolder: Boolean = false)