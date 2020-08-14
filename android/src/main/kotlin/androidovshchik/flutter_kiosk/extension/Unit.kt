@file:Suppress("unused")

package androidovshchik.flutter_kiosk.extension

import android.os.Build

fun isMarshmallow() = Build.VERSION.SDK_INT == Build.VERSION_CODES.M

fun isNougat() = Build.VERSION.SDK_INT == Build.VERSION_CODES.N

fun isNougatMR1() = Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1

fun isOreo() = Build.VERSION.SDK_INT == Build.VERSION_CODES.O

fun isOreoMR1() = Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1

fun isPie() = Build.VERSION.SDK_INT == Build.VERSION_CODES.P

fun isMarshmallowPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

fun isNougatMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun isOreoMR1Plus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P