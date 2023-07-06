package ai.lava.demoapp.android.inbox

import ai.lava.demoapp.android.R
import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lava.lavasdk.InboxMessage

class NotificationInboxAdapter(
  private var mMessageList: List<InboxMessage>?,
  private val mOnClickListener: View.OnClickListener
) : RecyclerView.Adapter<NotificationInboxAdapter.InboxMessageViewHolder>() {

  fun updateMessageList(list: List<InboxMessage>?) {
    mMessageList = list
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxMessageViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_notification_inbox_item, parent, false)
    return InboxMessageViewHolder(view, mOnClickListener)
  }

  @SuppressLint("SetTextI18n")
  override fun onBindViewHolder(holder: InboxMessageViewHolder, position: Int) {
    val nimb = mMessageList?.get(position)
    if (nimb != null) {
      if (!nimb.read) {
        holder.vDot.visibility = View.VISIBLE
      } else {
        holder.vDot.visibility = View.INVISIBLE
      }

      val title = nimb.title
      if (!TextUtils.isEmpty(title)) {
        holder.tvNotificationMessageTitle.text = title
      } else {
        holder.tvNotificationMessageTitle.visibility = View.GONE
      }

      val id = nimb.messageId
      if (!TextUtils.isEmpty(id)) {
        holder.tvNotificationMessageId.text = "ID: $id"
      } else {
        holder.tvNotificationMessageId.visibility = View.GONE
      }
      holder.tvNotificationMessageCreatedDate.text = "Created at " + nimb.createdAt.toString()
      holder.tvNotificationMessageExpirationDate.text = "Expires at " + nimb.expiresAt.toString()
      holder.tvNotificationMessageState.text = if (nimb.read) "READ" else "UNREAD"
      holder.rlCameraContainer.tag = position
    }
  }

  override fun getItemCount(): Int {
    return if (mMessageList != null) mMessageList!!.size else 0
  }

  inner class InboxMessageViewHolder(itemView: View, onClickListener: View.OnClickListener?) :
    RecyclerView.ViewHolder(itemView) {
    val tvNotificationMessageId: TextView = itemView.findViewById(R.id.tvMessageId)
    val tvNotificationMessageTitle: TextView = itemView.findViewById(R.id.tvMessageTitle)
    val tvNotificationMessageCreatedDate: TextView = itemView.findViewById(R.id.tvMessageCreatedDate)
    val tvNotificationMessageExpirationDate: TextView = itemView.findViewById(R.id.tvMessageExpirationDate)
    val tvNotificationMessageState: TextView = itemView.findViewById(R.id.tvMessageStatus)
    val rlCameraContainer: RelativeLayout = itemView.findViewById(R.id.rlCameraContainer)
    val vDot: View = itemView.findViewById(R.id.vDotView)

    init {
      itemView.setOnClickListener(onClickListener)
    }
  }
}