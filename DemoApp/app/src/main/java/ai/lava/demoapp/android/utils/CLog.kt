package ai.lava.demoapp.android.utils

import android.content.Context
import android.util.Log

object CLog {
  enum class LogLevel {
    NONE, ERROR, WARN, INFO, DEBUG, VERBOSE
  }
  var logLevel = LogLevel.WARN

  val sShowError: Boolean
    get() = logLevel >= LogLevel.ERROR

  val sShowWarn: Boolean
    get() = logLevel >= LogLevel.WARN

  val sShowInfo: Boolean
    get() = logLevel >= LogLevel.INFO

  val sShowDebug: Boolean
    get() = logLevel >= LogLevel.DEBUG

  val sShowVerbose: Boolean
    get() = logLevel >= LogLevel.VERBOSE

  fun l(msg: String): String {
    return l(true, msg)
  }

  fun l(print: Boolean, msg: String): String {
    return l(print, msg, null)
  }

  fun l(msg: String, e: Throwable?): String {
    return l(true, msg, e)
  }

  fun l(print: Boolean, msg: String, e: Throwable?): String {
    if (print) {
      Log.e(getCallerClassName(), msg, e)
    }
    return msg
  }

  fun e(msg: String): String {
    if (sShowError) {
      Log.e(getCallerClassName(), msg)
    }
    return msg
  }

  fun e(msg: String, t: Throwable?): String {
    if (sShowError) {
      Log.e(getCallerClassName(), msg, t)
    }
    return msg
  }

  fun e(c: Context?, resId: Int): Int {
    if (c != null && sShowError) {
      try {
        Log.e(getCallerClassName(), c.getString(resId))
      } catch (e: Throwable) {
        e("Error while reporting error, having a bad day?")
      }
    }
    return resId
  }

  fun d(msg: String): String {
    if (sShowDebug) {
      Log.d(getCallerClassName(), msg)
    }
    return msg
  }

  fun d(msg: String, t: Throwable?): String {
    if (sShowError) {
      Log.d(getCallerClassName(), msg, t)
    }
    return msg
  }

  fun v(msg: String): String {
    if (sShowVerbose) {
      Log.v(getCallerClassName(), msg)
    }
    return msg
  }

  fun i(msg: String): String {
    if (sShowInfo) {
      Log.i(getCallerClassName(), msg)
    }
    return msg
  }

  fun i(c: Context?, resId: Int): Int {
    if (c != null && sShowInfo) {
      try {
        Log.i(getCallerClassName(), c.getString(resId))
      } catch (e: Throwable) {
        e("Error while reporting error, having a bad day?")
      }
    }
    return resId
  }

  fun w(msg: String): String {
    if (sShowWarn) {
      Log.w(getCallerClassName(), msg)
    }
    return msg
  }

  fun wtf(msg: String): String {
    Log.wtf(getCallerClassName(), msg)
    return msg
  }

  private fun getCallerClassName(): String =  Exception().stackTrace[2].className
}