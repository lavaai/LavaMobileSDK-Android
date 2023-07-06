package ai.lava.demoapp.android.inbox

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.auth.LoginActivity
import ai.lava.demoapp.android.utils.GenericUtils
import ai.lava.demoapp.android.utils.ProgressUtils
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lava.lavasdk.InboxMessage
import java.util.*

class NotificationInboxEditFragment : Fragment(), View.OnClickListener {
  private var mBtDelete: Button? = null
  private var mBtRead: Button? = null
  private var mBtUnread: Button? = null
  private var mRvInboxMessage: RecyclerView? = null
  private val mSelectedMessageIds = ArrayList<String>()
  private var mInboxMessageViewModel: InboxMessageViewModel? = null

  private val mCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
    val messageId = buttonView.tag as String
    if (!TextUtils.isEmpty(messageId)) {
      if (isChecked) {
        if (!mSelectedMessageIds.contains(messageId)) {
          mSelectedMessageIds.add(messageId)
        }
      } else {
        mSelectedMessageIds.remove(messageId)
      }
    }
  }

  private var mAdapter: NotificationInboxEditAdapter? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_inbox_message_edit, container, false)
    initUi(view)
    setupUi()
    return view
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (activity is MainActivity) {
      mInboxMessageViewModel = (activity as MainActivity?)!!.inboxMessageViewModel
    } else if (activity is LoginActivity) {
      mInboxMessageViewModel = (activity as LoginActivity?)!!.inboxMessageViewModel
    }
  }

  private fun initUi(view: View) {
    mBtDelete = view.findViewById(R.id.btDelete)
    mBtRead = view.findViewById(R.id.btRead)
    mBtUnread = view.findViewById(R.id.btUnread)
    mRvInboxMessage = view.findViewById(R.id.rvNotificationInboxEdit)
  }

  private fun setupUi() {
    mBtDelete?.setOnClickListener(this)
    mBtRead?.setOnClickListener(this)
    mBtUnread?.setOnClickListener(this)
    mAdapter = NotificationInboxEditAdapter(mInboxMessageViewModel?.list(), mCheckedChangeListener)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
    mRvInboxMessage?.adapter = mAdapter
    mRvInboxMessage?.layoutManager = mLayoutManager

    setUpMessageList()

    mInboxMessageViewModel?.setFragmentTransaction(object : FragmentTransaction {
      override fun closeFragment() {
        if (activity is MainActivity) {
          activity!!.supportFragmentManager.popBackStack()
        } else if (activity is LoginActivity) {
          activity!!.supportFragmentManager.popBackStack()
        }
      }
    })
  }

  private fun setUpMessageList() {
    mInboxMessageViewModel?.inboxMessageList?.observe(viewLifecycleOwner) { inboxMessages ->
      if (inboxMessages != null && inboxMessages.size > 0) {
        mRvInboxMessage?.visibility = View.VISIBLE
        handleMessageListObtained(inboxMessages)
      } else {
        mRvInboxMessage?.visibility = View.GONE
      }
      mAdapter?.notifyDataSetChanged()
    }
  }

  private fun handleMessageListObtained(list: List<InboxMessage>) {
    mAdapter?.updateMessageList(list)
  }

  override fun onClick(v: View) {
    when (v.id) {
      R.id.btDelete -> handleButtonClick()
      R.id.btRead -> updateStateToRead()
      R.id.btUnread -> updateStateToUnread()
    }
  }

  private fun handleButtonClick() {
    if (mSelectedMessageIds.size > 0) {
      ProgressUtils.showProgress(activity, "Deleting")
      mInboxMessageViewModel!!.deletemessages(mSelectedMessageIds)
    } else {
      GenericUtils.displayToast(context, "No item selected")
    }
  }

  private fun updateStateToRead() {
    if (mSelectedMessageIds.size > 0) {
      ProgressUtils.showProgress(activity, "Updating to read state")
      mInboxMessageViewModel!!.updateMessagestateToRead(mSelectedMessageIds)
    } else {
      GenericUtils.displayToast(context, "No item selected")
    }
  }

  private fun updateStateToUnread() {
    if (mSelectedMessageIds.size > 0) {
      ProgressUtils.showProgress(activity, "Updating to unread state")
      mInboxMessageViewModel!!.updateMessagestateToUnread(mSelectedMessageIds)
    } else {
      GenericUtils.displayToast(context, "No item selected")
    }
  }
}