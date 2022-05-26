package com.hfad.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

/*
Ключевой параметр inSampleSize определяет величину «образца» для каждого
пиксела исходного изображения: образец с размером 1 содержит один горизонтальны
пиксел для каждого горизонтального пиксела исходного файла, а образец
с размером 2 содержит один горизонтальный пиксел для каждых двух горизонтальных
пикселов исходного файла. Таким образом, если значение inSampleSize
равно 2, количество пикселов в изображении составляет четверть от количества
пикселов оригинала.
 */

fun getScaledBitmap(path : String, destWidth : Int, destHeight : Int) : Bitmap {
    //чтение размеров изображения на диске
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    //на сколько нужно уменьшить
    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destWidth) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth
        val sampleScale = if (heightScale > widthScale) {
            heightScale
        } else {
            widthScale
        }
        inSampleSize = Math.round(sampleScale)
    }
    options = BitmapFactory.Options()
    options.inSampleSize = inSampleSize
    // Чтение и создание окончательного растрового изображения
    return BitmapFactory.decodeFile(path, options)
}
/*
И последняя неприятная новость: при запуске фрагмента вы еще не знаете величину
PhotoView. До обработки макета никаких экранных размеров не существует.
Первый проход этой обработки происходит после выполнения onCreate(...),
onStart() и onResume(), поэтому PhotoView и не знает своих размеров.
 */
fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    val size = Point()
    activity.windowManager.defaultDisplay.getSize(size)
    return getScaledBitmap(path, size.x, size.y)
}