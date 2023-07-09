package com.geeksville.mesh.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.geeksville.mesh.NodeInfo
import com.geeksville.mesh.R
import com.geeksville.mesh.android.Logging
import com.geeksville.mesh.databinding.AdapterNodeLayoutBinding
import com.geeksville.mesh.databinding.NodelistFragmentBinding
import com.geeksville.mesh.model.UIViewModel
import com.geeksville.mesh.util.formatAgo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class UsersFragment : ScreenFragment("Users"), Logging {

    private var _binding: NodelistFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private val model: UIViewModel by activityViewModels()

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class ViewHolder(itemView: AdapterNodeLayoutBinding) : RecyclerView.ViewHolder(itemView.root) {
        val chipNode = itemView.chipNode
        val nodeNameView = itemView.nodeNameView
        val distanceView = itemView.distanceView
        val coordsView = itemView.coordsView
        val batteryPctView = itemView.batteryPercentageView
        val lastTime = itemView.lastConnectionView
        val powerIcon = itemView.batteryIcon
        val signalView = itemView.signalView
        val envMetrics = itemView.envMetrics
    }

    private val nodesAdapter = object : RecyclerView.Adapter<ViewHolder>() {

        private var nodes = arrayOf<NodeInfo>()

        private fun popup(view: View, position: Int) {
            if (!model.isConnected()) return
            val node = nodes[position]
            val user = node.user
            val showAdmin = position == 0 || model.adminChannelIndex > 0
            val popup = PopupMenu(requireContext(), view)
            popup.inflate(R.menu.menu_nodes)
            popup.menu.setGroupVisible(R.id.group_remote, position > 0)
            popup.menu.setGroupVisible(R.id.group_admin, showAdmin)
            popup.menu.setGroupEnabled(R.id.group_admin, !model.isManaged)
            popup.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.direct_message -> {
                        if (position > 0 && user != null) {
                            debug("calling MessagesFragment filter: 0${user.id}")
                            setFragmentResult(
                                "requestKey",
                                bundleOf(
                                    "contactKey" to "0${user.id}",
                                    "contactName" to user.longName
                                )
                            )
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.mainActivityLayout, MessagesFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                    }
                    R.id.request_position -> {
                        if (position > 0 && user != null) {
                            debug("requesting position for ${user.longName}")
                            model.requestPosition(node.num)
                        }
                    }
                    R.id.traceroute -> {
                        if (position > 0 && user != null) {
                            debug("requesting traceroute for ${user.longName}")
                            model.requestTraceroute(node.num)
                        }
                    }
                    R.id.remote_admin -> {
                        debug("calling remote admin --> destNum: ${node.num.toUInt()}")
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.mainActivityLayout, DeviceSettingsFragment(node))
                            .addToBackStack(null)
                            .commit()
                    }
                }
                true
            }
            popup.show()
        }

        /**
         * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
         * an item.
         *
         *
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         *
         *
         * The new ViewHolder will be used to display items of the adapter using
         * [.onBindViewHolder]. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary [View.findViewById] calls.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         * an adapter position.
         * @param viewType The view type of the new View.
         *
         * @return A new ViewHolder that holds a View of the given view type.
         * @see .getItemViewType
         * @see .onBindViewHolder
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(requireContext())

            // Inflate the custom layout
            val contactView = AdapterNodeLayoutBinding.inflate(inflater, parent, false)

            // Return a new holder instance
            return ViewHolder(contactView)
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        override fun getItemCount(): Int = nodes.size

        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the [ViewHolder.itemView] to reflect the item at the given
         * position.
         *
         *
         * Note that unlike [android.widget.ListView], RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the `position` parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use [ViewHolder.getAdapterPosition] which will
         * have the updated adapter position.
         *
         * Override [.onBindViewHolder] instead if Adapter can
         * handle efficient partial bind.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the
         * item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val n = nodes[position]
            val user = n.user
            val (textColor, nodeColor) = n.colors
            with(holder.chipNode) {
                text = user?.shortName ?: "UNK"
                chipBackgroundColor = ColorStateList.valueOf(nodeColor)
                setTextColor(textColor)
            }
            val name = user?.longName ?: getString(R.string.unknown_username)
            holder.nodeNameView.text = name

            val pos = n.validPosition
            if (pos != null) {
                val html = "<a href='geo:${pos.latitude},${pos.longitude}?z=17&label=${
                    URLEncoder.encode(name, "utf-8")
                }'>${model.gpsString(pos)}</a>"
                holder.coordsView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
                holder.coordsView.movementMethod = LinkMovementMethod.getInstance()
                holder.coordsView.visibility = View.VISIBLE
            } else {
                holder.coordsView.visibility = View.INVISIBLE
            }

            val ourNodeInfo = model.ourNodeInfo.value
            val distance = ourNodeInfo?.distanceStr(n, model.config.display.units.number)
            if (distance != null) {
                holder.distanceView.text = distance
                holder.distanceView.visibility = View.VISIBLE
            } else {
                holder.distanceView.visibility = View.INVISIBLE
            }
            renderBattery(n.batteryLevel, n.voltage, holder)

            holder.lastTime.text = formatAgo(n.lastHeard)

            if (n.envMetricStr.isNotEmpty()) {
                holder.envMetrics.text = n.envMetricStr
                holder.envMetrics.visibility = View.VISIBLE
            } else {
                holder.envMetrics.visibility = View.GONE
            }

            if (n.num == ourNodeInfo?.num) {
                val text = "ChUtil %.1f%% AirUtilTX %.1f%%".format(
                    n.deviceMetrics?.channelUtilization,
                    n.deviceMetrics?.airUtilTx
                )
                holder.signalView.text = text
                holder.signalView.visibility = View.VISIBLE
            } else {
                if ((n.snr < 100f) && (n.rssi < 0)) {
                    val text = "rssi:%d snr:%.1f".format(n.rssi, n.snr)
                    holder.signalView.text = text
                    holder.signalView.visibility = View.VISIBLE
                } else {
                    holder.signalView.visibility = View.INVISIBLE
                }
            }
            holder.chipNode.setOnClickListener {
                popup(it, position)
            }
            holder.itemView.setOnLongClickListener {
                popup(it, position)
                true
            }
        }

        /// Called when our node DB changes
        fun onNodesChanged(nodesIn: Array<NodeInfo>) {
            if (nodesIn.size > 1)
                nodesIn.sortWith(compareByDescending { it.lastHeard }, 1)
            nodes = nodesIn
            notifyDataSetChanged() // FIXME, this is super expensive and redraws all nodes
        }
    }

    private fun renderBattery(
        battery: Int?,
        voltage: Float?,
        holder: ViewHolder
    ) {

        val (image, text) = when (battery) {
            in 0..100 -> Pair(
                R.drawable.ic_battery_full_24,
                "%d%% %.2fV".format(battery, voltage ?: 0)
            )
            101 -> Pair(R.drawable.ic_power_plug_24, "")
            else -> Pair(R.drawable.ic_battery_full_24, "?")
        }

        holder.batteryPctView.text = text
        holder.powerIcon.setImageDrawable(context?.let {
            ContextCompat.getDrawable(it, image)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NodelistFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nodeListView.adapter = nodesAdapter
        binding.nodeListView.layoutManager = LinearLayoutManager(requireContext())

        // ensure our local node is first (index 0)
        fun Map<String, NodeInfo>.perhapsReindexBy(nodeNum: Int?): Array<NodeInfo> =
            if (size > 1 && nodeNum != null && values.firstOrNull()?.num != nodeNum) {
                values.partition { node -> node.num == nodeNum }.let { it.first + it.second }
            } else {
                values
            }.toTypedArray()

        model.nodeDB.nodes.observe(viewLifecycleOwner) {
            nodesAdapter.onNodesChanged(it.perhapsReindexBy(model.myNodeNum))
        }

        model.packetResponse.asLiveData().observe(viewLifecycleOwner) { meshLog ->
            meshLog?.meshPacket?.let { meshPacket ->
                val routeList = meshLog.routeDiscovery?.routeList ?: return@let
                fun nodeName(num: Int) = model.nodeDB.nodesByNum?.get(num)?.user?.longName

                var routeStr = "${nodeName(meshPacket.to)} --> "
                routeList.forEach { num -> routeStr += "${nodeName(num)} --> " }
                routeStr += "${nodeName(meshPacket.from)}"

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.traceroute)
                    .setMessage(routeStr)
                    .setPositiveButton(R.string.okay) { _, _ -> }
                    .show()

                model.clearPacketResponse()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
