package ai.lava.demoapp.android.inbox

import ai.lava.demoapp.android.MainActivity
import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.auth.LoginActivity
import ai.lava.demoapp.android.utils.GenericUtils
import ai.lava.demoapp.android.utils.ProgressUtils
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lava.lavasdk.InboxListener
import com.lava.lavasdk.InboxMessage
import java.util.*

class NotificationInboxFragment : Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {
  private var mRvNotificationInbox: RecyclerView? = null
  private var mTvNoItem: TextView? = null
  private var mBtEdit: Button? = null
  private var mBtRefresh: Button? = null
  private var mSpState: Spinner? = null
  private var mTvUnreadCount: TextView? = null
  private var mAdapter: NotificationInboxAdapter? = null
  private var mToolbar: Toolbar? = null
  private var mStateCheck = 0

  private val mOnClickListener = View.OnClickListener { v ->
    val index = v.tag as Int
    val inboxMessage = mInboxMessageViewModel?.list()?.get(index)
    if (inboxMessage != null) {
      val messageId = inboxMessage.messageId
      if (TextUtils.isEmpty(messageId)) {
        GenericUtils.displayToast(context, "MessageId is empty")
        CLog.e("MessageId is empty")
        return@OnClickListener
      }
      mInboxMessageViewModel?.displayMessage(requireContext(), index, inboxMessage)
    }
  }

  //Added to display message status
  private val mInboxListener: InboxListener = object : InboxListener {
    override fun onInboxMessage(success: Boolean, message: String, messages: List<InboxMessage>) {
      Toast.makeText(context, "IsSuccess - $success\nMessage - $message", Toast.LENGTH_LONG).show()
    }
  }

  private var mInboxMessageViewModel: InboxMessageViewModel? = null
  private var mBtGetUnreadCountt: Button? = null
  private var mBtNumberOfMessages: Button? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (activity is MainActivity) {
      mInboxMessageViewModel = (activity as MainActivity?)!!.inboxMessageViewModel
    } else if (activity is LoginActivity) {
      mInboxMessageViewModel = (activity as LoginActivity?)!!.inboxMessageViewModel
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_notification_inbox, container, false)
    initUi(view)
    setupUi()
    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    fetchNotificationInboxMessageList()
  }

  private fun setupUi() {
    if (activity is LoginActivity) {
      mToolbar?.visibility = View.VISIBLE
      mToolbar?.setNavigationIcon(R.drawable.icn_back)
      mToolbar?.setNavigationOnClickListener { activity?.onBackPressed() }
    } else {
      mToolbar?.visibility = View.GONE
    }
    mAdapter = NotificationInboxAdapter(ArrayList(), mOnClickListener)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
    mRvNotificationInbox!!.adapter = mAdapter
    mRvNotificationInbox!!.layoutManager = mLayoutManager
    mBtEdit!!.setOnClickListener(this)
    mBtGetUnreadCountt!!.setOnClickListener(this)
    mBtNumberOfMessages!!.setOnClickListener(this)
    mBtRefresh!!.setOnClickListener(this)
    val spinnerAdapter = activity?.let {
      ArrayAdapter(it, R.layout.inbox_state_spinner_item, InboxMessageViewModel.State.values())
    }
    mSpState?.adapter = spinnerAdapter
    mSpState?.setSelection(spinnerAdapter?.getPosition(InboxMessageViewModel.State.ALL) ?: 0)
    mSpState?.onItemSelectedListener = this
    mInboxMessageViewModel?.setTempListener(mInboxListener)
    setUpMessageList()
  }

  private fun setUpMessageList() {
    if (mInboxMessageViewModel != null) {
      mInboxMessageViewModel?.inboxMessageList?.observe(
        viewLifecycleOwner,
        { lavaInboxMessages: ArrayList<InboxMessage>? ->
          if (lavaInboxMessages != null && lavaInboxMessages.size > 0) {
            mTvNoItem?.visibility = View.GONE
            mRvNotificationInbox?.visibility = View.VISIBLE
            handleMessageListObtained(lavaInboxMessages)
          } else {
            mTvNoItem?.visibility = View.VISIBLE
            mRvNotificationInbox?.visibility = View.GONE
          }
          mAdapter?.notifyDataSetChanged()
          mTvUnreadCount?.text = mInboxMessageViewModel?.getUnreadCount()?.toString()
        })
    }
  }

  private fun initUi(view: View) {
    mToolbar = view.findViewById(R.id.toolbar)
    mRvNotificationInbox = view.findViewById(R.id.rvNotificationInbox)
    mTvNoItem = view.findViewById(R.id.tvNoMessage)
    mBtEdit = view.findViewById(R.id.btEdit)
    mBtGetUnreadCountt = view.findViewById(R.id.btGetUnreadCount)
    mBtNumberOfMessages = view.findViewById(R.id.btNumberOfMessages)
    mBtRefresh = view.findViewById(R.id.btRefresh)
    mSpState = view.findViewById(R.id.spState)
    mTvUnreadCount = view.findViewById(R.id.tvUnreadCount)
  }

  private fun fetchNotificationInboxMessageList() {
    ProgressUtils.showProgress(activity, "Fetching Inbox message")
    val state = InboxMessageViewModel.State.values()[mSpState?.selectedItemPosition ?: 0]
    mInboxMessageViewModel?.getInboxMessageList(state)
  }

  private fun handleMessageListObtained(list: List<InboxMessage>) {
    mAdapter?.updateMessageList(list)
  }

  override fun onClick(v: View) {
    val id = v.id
    when (id) {
      R.id.btEdit -> handleEditButtonClick()
      R.id.btRefresh -> fetchNotificationInboxMessageList()
      R.id.btGetUnreadCount -> {
        val unreadCount = mInboxMessageViewModel!!.getUnreadCount()
        showAlert("Unread count", unreadCount)
      }
      R.id.btNumberOfMessages -> {
        val numberOfMessage =
          mInboxMessageViewModel!!.getNumberOfMessages(InboxMessageViewModel.State.values()[mSpState!!.selectedItemPosition])
        showAlert("Number of Messages", numberOfMessage)
      }
    }
  }

  private fun showAlert(title: String, value: Int) {
    val a = activity ?: return
    val builder = AlertDialog.Builder(a)
    builder.setTitle(title)
    builder.setMessage("Count : $value")
    builder.setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
    builder.show()
  }

  private fun handleEditButtonClick() {
    val list = mInboxMessageViewModel?.list()
    if (list == null || list.isEmpty()) {
      GenericUtils.displayToast(context, "No item to be edited")
      return
    }

    if (activity is MainActivity) {
      val frg = NotificationInboxEditFragment()
      (activity as MainActivity?)!!.addFragments(frg, true, true)
    } else if (activity is LoginActivity) {
      val frg = NotificationInboxEditFragment()
      (activity as LoginActivity?)!!.addFragments(frg, true, true)
    }
  }

  override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
    when (parent.id) {
      R.id.spState -> {
        if (++mStateCheck > 1) {
          fetchNotificationInboxMessageList()
        }
      }
    }
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {}
}