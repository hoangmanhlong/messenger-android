package com.android.kotlin.familymessagingapp.model

import androidx.annotation.DrawableRes
import com.android.kotlin.familymessagingapp.R

enum class FileType(val value: String) {
    TEXT("0"),
    IMAGE("1"),
    PDF("2"),
    DOC("3"),
    EXCEL("4"),
    PRESENTATION("5"),
    AUDIO("6"),
    VIDEO("7"),
    COMPRESSED("8"),
    SCRIPT("9"),
    FONT("10"),
    UNKNOWN("11"),
}

private val fileTypeDrawableMap = mapOf(
    FileType.IMAGE to R.drawable.mc_file_image,
    FileType.PDF to R.drawable.mc_file_pdf,
    FileType.DOC to R.drawable.mc_file_document,
    FileType.TEXT to R.drawable.mc_file_text,
    FileType.COMPRESSED to R.drawable.mc_file_pack,
    FileType.PRESENTATION to R.drawable.mc_file_presentation,
    FileType.SCRIPT to R.drawable.mc_file_script,
    FileType.AUDIO to R.drawable.mc_file_audio,
    FileType.EXCEL to R.drawable.mc_file_spreadsheet,
    FileType.VIDEO to R.drawable.mc_file_video,
    FileType.FONT to R.drawable.mc_file_font,
    FileType.UNKNOWN to R.drawable.mc_file_unknown
)

@DrawableRes
fun getFileDrawableRes(fileType: FileType): Int {
    return fileTypeDrawableMap[fileType] ?: R.drawable.mc_file_unknown
}

@DrawableRes
fun getFileDrawableRes(fileType: String?): Int {
    val type = FileType.entries.find { it.value == fileType } ?: FileType.UNKNOWN
    return getFileDrawableRes(type)
}
