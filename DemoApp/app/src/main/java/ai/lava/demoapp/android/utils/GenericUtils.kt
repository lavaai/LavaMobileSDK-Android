package ai.lava.demoapp.android.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

object GenericUtils {

  fun getVersionName(context: Context): String {
    return getPackageInfo(context)?.versionName ?: ""
  }

  private fun getPackageInfo(context: Context): PackageInfo? {
    val packageInfo: PackageInfo = try {
      val packageName = context.packageName ?: return null
      context.packageManager.getPackageInfo(packageName, 0)
    } catch (e: Throwable) {
      e.printStackTrace()
      return null
    }
    return packageInfo
  }

  fun hideKeyboard(a: Activity) {
    // Check if no view has focus:
    try {
      val view = a.currentFocus
      if (view != null) {
        val inputManager = a.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
      }
    } catch (e: Throwable) {
      //Not the end of the world. Keyborad didn't hide, that's it.
      e.printStackTrace()
    }
  }

  fun displayToast(context: Context?, text: String?): String? {
    try {
      if (context == null) return text
      /*  Toast toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 200);
            toast.show();
    */Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    } catch (e: Throwable) {
      e.printStackTrace()
    }
    return text
  }
}