package ai.lava.demoapp.android.inbox

import ai.lava.demoapp.android.R
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lava.lavasdk.InboxMessage

class NotificationInboxEditAdapter(
  private var mMessageList: List<InboxMessage>?,
  private val mCheckedChangeListener: CompoundButton.OnCheckedChangeListener
) : RecyclerView.Adapter<NotificationInboxEditAdapter.InboxMessageViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxMessageViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_notification_inbox_edit_item, parent, false)
    return InboxMessageViewHolder(view, mCheckedChangeListener)
  }

  override fun onBindViewHolder(holder: InboxMessageViewHolder, position: Int) {
    val nimb = mMessageList?.get(position)
    if (nimb != null) {
      holder.cbMessage.isChecked = false
      val title = nimb.title
      if (!TextUtils.isEmpty(title)) {
        holder.tvNotificationMessageTitle.text = "Title : $title"
      } else {
        holder.tvNotificationMessageTitle.visibility = View.GONE
      }
      val id = nimb.messageId
      if (!TextUtils.isEmpty(id)) {
        holder.tvNotificationMessageId.text = "Id : $id"
      } else {
        holder.tvNotificationMessageId.visibility = View.GONE
      }
      holder.cbMessage.tag = nimb.messageId
      holder.tvNotificationMessageCreatedDate.text = "Created at " + nimb.createdAt.toString()
      holder.tvNotificationMessageExpirationDate.text = "Expires at " + nimb.expiresAt.toString()
      holder.tvNotificationMessageState.text = "State : " + if (nimb.read) "READ" else "UNREAD"
    }
  }

  override fun getItemCount(): Int {
    return if (mMessageList != null) mMessageList!!.size else 0
  }

  fun updateMessageList(list: List<InboxMessage>?) {
    if (list != null) {
      mMessageList = list
    }
    notifyDataSetChanged()
  }

  inner class InboxMessageViewHolder(itemView: View, mCheckedChangeListener: CompoundButton.OnCheckedChangeListener?) :
    RecyclerView.ViewHolder(itemView) {
    val tvNotificationMessageId: TextView = itemView.findViewById(R.id.tvMessageId)
    val tvNotificationMessageTitle: TextView = itemView.findViewById(R.id.tvMessageTitle)
    val tvNotificationMessageCreatedDate: TextView = itemView.findViewById(R.id.tvMessageCreatedDate)
    val tvNotificationMessageExpirationDate: TextView = itemView.findViewById(R.id.tvMessageExpirationDate)
    val tvNotificationMessageState: TextView = itemView.findViewById(R.id.tvMessageStatus)
    val cbMessage: CheckBox = itemView.findViewById(R.id.cbMessage)

    init {
      cbMessage.setOnCheckedChangeListener(mCheckedChangeListener)
    }
  }
}