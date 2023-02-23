package ai.lava.demoapp.android.utils

import android.app.Activity
import android.app.ProgressDialog

object ProgressUtils {
  private var pd: ProgressDialog? = null

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
          pd = ProgressDialog(a)
          pd!!.setMessage(loading)
          pd!!.setCancelable(isCancellable)
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