package ai.lava.demoapp.android.inbox

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.utils.CLog
import ai.lava.demoapp.android.utils.GenericUtils
import ai.lava.demoapp.android.utils.ProgressUtils
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.lava.lavasdk.*
import java.util.*
import java.time.Instant

class InboxMessageViewModel(application: Application) : AndroidViewModel(application) {
  var inboxMessageList = MutableLiveData<ArrayList<InboxMessage>?>()

  private var mState = State.ALL
  private var mFragmentTransaction: FragmentTransaction? = null
  private var mTempInboxMessageListener: InboxListener? = null
  private val mInboxMessageListener: InboxListener = object : InboxListener {
    override fun onInboxMessage(success: Boolean, message: String, messages: List<InboxMessage>) {
      ProgressUtils.cancel()

      //Just for testing purpose
      mTempInboxMessageListener?.onInboxMessage(success, message, messages)

      // -------------------------
      if (messages.isNotEmpty() && inboxMessageList.value != null) {
        inboxMessageList.value?.let {
          it.clear()
          it.addAll(messages)
          inboxMessageList.setValue(it)
        }
      } else {
        inboxMessageList.setValue(ArrayList())
      }
    }
  }
  private val messageListener: ResultListener by lazy {
    object : ResultListener {
      override fun onResult(success: Boolean, message: String) {
        if (success) {
          mFragmentTransaction?.closeFragment()

          getInboxMessageList(mState)
        }
      }
    }
  }

  fun setState(state: State) {
    mState = state
  }

  fun getInboxMessageList(state: State) {
    Lava.instance.getInboxMessages(object : InboxListener {
      override fun onInboxMessage(success: Boolean, message: String, messages: List<InboxMessage>) {
        val m = filterMessages(messages, state).sortedByDescending { it.createdAt.toEpochMilli() }
        mInboxMessageListener.onInboxMessage(success, message, m)
      }
    })
  }

  fun getUnreadCount(): Int = getNumberOfMessages(State.UNREAD)

  fun getNumberOfMessages(state: State): Int = filterMessages(inboxMessageList.value, state).size

  fun updateMessagestateToRead(selectedList: List<String>) =
    Lava.instance.markInboxMessages(selectedList, true, messageListener)


  fun updateMessagestateToUnread(selectedList: List<String>) =
    Lava.instance.markInboxMessages(selectedList, false, messageListener)

  fun deletemessages(selectedList: List<String>) =
    Lava.instance.deleteInboxMessages(selectedList, messageListener)

  fun displayMessage(ctx: Context, index: Int, message: InboxMessage) {
    displayInboxMessage(ctx, message)
    val messages = inboxMessageList.value
    val updatedMessage = message.copy(read=true)
    messages?.set(index, updatedMessage)
    inboxMessageList.value = messages
  }

  private fun displayInboxMessage(ctx: Context, message: InboxMessage) {
    if (message.messageId.isEmpty()) {
      CLog.e("displayMessage is failed - messageId  is empty")
      return
    }

    if (message.payload.isEmpty()) {
      CLog.e("displayMessage is failed - payload is empty or null")
      return
    }

    try {
      /**
       * This is an alternative way to display the overlay for Inbox messages
       */
      /*
      Lava.instance.handleNotification(
        ctx.applicationContext, MainActivity::class.java, message.toNotificationData(),
        object: ResultListener {
          override fun onResult(success: Boolean, message: String) {
            if (!success) {
              GenericUtils.displayToast(ctx, message)
            }
          }
        }
      )
      */

      /**
       * This is a newer method to display an overlay
       */
      Lava.instance.showInboxMessage(
        ctx.applicationContext,
        MainActivity::class.java,
        message
      )

      if (message.read) {
        CLog.d("Update Inbox message state not triggered - Message is already read")
        return
      }

      Lava.instance.markInboxMessages(listOf(message.messageId), true, null)
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }

  fun list(): ArrayList<InboxMessage>? = inboxMessageList.value

  fun setFragmentTransaction(fragmentTransaction: FragmentTransaction?) {
    mFragmentTransaction = fragmentTransaction
  }

  fun setTempListener(listener: InboxListener?) {
    mTempInboxMessageListener = listener
  }

  init {
    inboxMessageList.postValue(arrayListOf())
  }

  enum class State {
    ALL, READ, UNREAD
  }

  companion object {
    fun filterMessages(messages: List<InboxMessage>?, state: State): List<InboxMessage> {
      if (messages == null) {
        return listOf()
      }

      return if (state == State.ALL) {
        messages
      } else {
        val readState = State.READ == state
        messages.filter { it.read == readState }
      }
    }
  }
}