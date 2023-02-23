package ai.lava.demoapp.android

import ai.lava.demoapp.android.inbox.InboxMessageViewModel
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

open class BaseActivity : AppCompatActivity() {
  var inboxMessageViewModel: InboxMessageViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    inboxMessageViewModel = ViewModelProviders.of(this).get(
      InboxMessageViewModel::class.java
    )
  }
}