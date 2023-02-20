package ai.lava.demoapp.android

import ai.lava.demoapp.android.inbox.InboxMessageViewModel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

open class BaseActivity : AppCompatActivity() {
  var inboxMessageViewModel: InboxMessageViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    inboxMessageViewModel = ViewModelProvider(this)[InboxMessageViewModel::class.java]
  }
}