package ai.lava.demoapp.android.utils

import ai.lava.demoapp.android.R
import android.app.Activity
import android.app.AlertDialog

object ProgressUtils {
  private var pd: AlertDialog? = null

  /**
   * Show progress dialog
   *
   * @param a The activity reference
   */
  @JvmStatic
  fun showProgress(a: Activity?) {
    showProgress(a, "loading", true)
  }

  /**
   * Show progress dialog
   *
   * @param a             The activity reference
   * @param isCancellable make the dialog cancellable?
   */
  @JvmStatic
  fun showProgress(a: Activity?, isCancellable: Boolean) {
    showProgress(a, "loading", isCancellable)
  }

  /**
   * Show progress dialog
   *
   * @param a       The activity reference
   * @param loading The string to be shown
   */
  @JvmStatic
  fun showProgress(a: Activity?, loading: String?) {
    showProgress(a, loading, true)
  }

  /**
   * Show progress dialog
   *
   * @param a             The activity reference
   * @param loading       The string to be shown
   * @param isCancellable make the dialog cancellable?
   */
  @JvmStatic
  fun showProgress(a: Activity?, loading: String?, isCancellable: Boolean) {
    try {
      a?.runOnUiThread {
        try {
          cancel()
          val builder = AlertDialog.Builder(a)
          builder.setMessage(loading)
          builder.setCancelable(isCancellable)
          builder.setView(R.layout.layout_loading_dialog)
          pd = builder.create()
          pd!!.show()
        } catch (e: Throwable) {
          e.printStackTrace()
        }
      }
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  /**
   * Cancel the progress dialog
   */
  @JvmStatic
  fun cancel() {
    try {
      if (pd != null) {
        pd!!.cancel()
      }
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }
}