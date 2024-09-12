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

val mimeTypeToFileTypeMap = mapOf(
    "image/jpeg" to FileType.IMAGE,
    "image/png" to FileType.IMAGE,
    "image/gif" to FileType.IMAGE,
    "image/bmp" to FileType.IMAGE,
    "image/webp" to FileType.IMAGE,
    "image/jpg" to FileType.IMAGE,

    "application/pdf" to FileType.PDF,

    "application/msword" to FileType.DOC,
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to FileType.DOC,

    "text/plain" to FileType.TEXT,

    "application/zip" to FileType.COMPRESSED,
    "application/x-7z-compressed" to FileType.COMPRESSED,
    "application/x-rar-compressed" to FileType.COMPRESSED,
    "application/gzip" to FileType.COMPRESSED,

    "application/x-sh" to FileType.SCRIPT,
    "application/x-python" to FileType.SCRIPT,
    "application/javascript" to FileType.SCRIPT,
    "application/x-java-source" to FileType.SCRIPT,

    "application/vnd.ms-powerpoint" to FileType.PRESENTATION,
    "application/vnd.openxmlformats-officedocument.presentationml.presentation" to FileType.PRESENTATION,

    "audio/mpeg" to FileType.AUDIO,
    "audio/x-wav" to FileType.AUDIO,
    "audio/ogg" to FileType.AUDIO,
    "audio/flac" to FileType.AUDIO,
    "audio/aac" to FileType.AUDIO,
    "audio/x-ms-wma" to FileType.AUDIO,

    "font/ttf" to FileType.FONT,
    "font/otf" to FileType.FONT,
    "application/x-font-ttf" to FileType.FONT,
    "application/x-font-opentype" to FileType.FONT,

    "application/vnd.ms-excel" to FileType.EXCEL,
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to FileType.EXCEL,

    "video/mp4" to FileType.VIDEO,
    "video/x-matroska" to FileType.VIDEO,
    "video/x-msvideo" to FileType.VIDEO,
    "video/quicktime" to FileType.VIDEO,
    "video/x-ms-wmv" to FileType.VIDEO
)

private val fileTypeToMimeTypesMap = mapOf(
    FileType.IMAGE to listOf(
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp",
        "image/jpg"
    ),
    FileType.PDF to listOf("application/pdf"),
    FileType.DOC to listOf(
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ),
    FileType.TEXT to listOf("text/plain"),
    FileType.COMPRESSED to listOf(
        "application/zip",
        "application/x-7z-compressed",
        "application/x-rar-compressed",
        "application/gzip"
    ),
    FileType.SCRIPT to listOf(
        "application/x-sh",
        "application/x-python",
        "application/javascript",
        "application/x-java-source"
    ),
    FileType.PRESENTATION to listOf(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    ),
    FileType.AUDIO to listOf(
        "audio/mpeg",
        "audio/x-wav",
        "audio/ogg",
        "audio/flac",
        "audio/aac",
        "audio/x-ms-wma"
    ),
    FileType.FONT to listOf(
        "font/ttf",
        "font/otf",
        "application/x-font-ttf",
        "application/x-font-opentype"
    ),
    FileType.EXCEL to listOf(
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    ),
    FileType.VIDEO to listOf(
        "video/mp4",
        "video/x-matroska",
        "video/x-msvideo",
        "video/quicktime",
        "video/x-ms-wmv"
    )
)

fun getMimeTypesFromFileType(fileType: FileType): List<String> {
    return fileTypeToMimeTypesMap[fileType] ?: emptyList()
}

@DrawableRes
fun getFileDrawableRes(fileType: FileType): Int {
    return fileTypeDrawableMap[fileType] ?: R.drawable.mc_file_unknown
}

@DrawableRes
fun getFileDrawableRes(fileType: String?): Int {
    val type = FileType.entries.find { it.value == fileType } ?: FileType.UNKNOWN
    return getFileDrawableRes(type)
}
