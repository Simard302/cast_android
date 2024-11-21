package me.clarius.sdk.cast.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SlidesAdapter(private val slides: List<SlideContent>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return slides[position].slideNumber
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        when (viewType) {
            1 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.slide_layout_1, parent, false)
                return SlideViewHolder1(view)
            }

            2 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.slide_layout_2, parent, false)
                return SlideViewHolder2(view)
            }

            3 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.slide_layout_3, parent, false)
                return SlideViewHolder3(view)
            }

            4 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.slide_layout_4, parent, false)
                return SlideViewHolder4(view)
            }

            5 -> {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.slide_layout_5, parent, false)
                return SlideViewHolder5(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val slideContent = slides[position]
        when (holder.itemViewType) {
            1 -> (holder as SlideViewHolder1).bind(slideContent)
            2 -> (holder as SlideViewHolder2).bind(slideContent)
            3 -> (holder as SlideViewHolder3).bind(slideContent)
            4 -> (holder as SlideViewHolder4).bind(slideContent)
            5 -> (holder as SlideViewHolder5).bind(slideContent)
        }
    }

    override fun getItemCount(): Int {
        return slides.size
    }

    internal class SlideViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView = itemView.findViewById(R.id.title_slide)
        var textLeftTextView: TextView = itemView.findViewById(R.id.text_left)
        var imageRightImageView: ImageView =
            itemView.findViewById(R.id.image_right)

        fun bind(slideContent: SlideContent) {
            titleTextView.text = slideContent.title
            textLeftTextView.text = slideContent.textLeft
            imageRightImageView.setImageResource(slideContent.imageRightResId)
        }
    }

    internal class SlideViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView = itemView.findViewById(R.id.title_slide)
        var textLeftTextView: TextView = itemView.findViewById(R.id.text_left)
        var textRightTextView: TextView = itemView.findViewById(R.id.text_right)

        fun bind(slideContent: SlideContent) {
            titleTextView.text = slideContent.title
            textLeftTextView.text = slideContent.textLeft
            textRightTextView.text = slideContent.textRight
        }
    }

    internal class SlideViewHolder3(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Similar to SlideViewHolder1
        var titleTextView: TextView = itemView.findViewById(R.id.title_slide)
        var textLeftTextView: TextView = itemView.findViewById(R.id.text_left)
        var imageRightImageView: ImageView =
            itemView.findViewById(R.id.image_right)

        fun bind(slideContent: SlideContent) {
            titleTextView.text = slideContent.title
            textLeftTextView.text = slideContent.textLeft
            imageRightImageView.setImageResource(slideContent.imageRightResId)
        }
    }

    internal class SlideViewHolder4(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView = itemView.findViewById(R.id.title_slide)
        var imageLeftImageView: ImageView =
            itemView.findViewById(R.id.image_left)
        var textTopRightTextView: TextView = itemView.findViewById(R.id.text_top_right)
        var imageBottomRightImageView: ImageView =
            itemView.findViewById(R.id.image_bottom_right)

        fun bind(slideContent: SlideContent) {
            titleTextView.text = slideContent.title
            imageLeftImageView.setImageResource(slideContent.imageLeftResId)
            textTopRightTextView.text = slideContent.textLeft
            imageBottomRightImageView.setImageResource(slideContent.imageRightResId)
        }
    }

    internal class SlideViewHolder5(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTextView: TextView = itemView.findViewById(R.id.title_slide)
        var imageLeftImageView: ImageView =
            itemView.findViewById(R.id.image_left)
        var textBottomTextView: TextView = itemView.findViewById(R.id.text_bottom)

        fun bind(slideContent: SlideContent) {
            titleTextView.text = slideContent.title
            imageLeftImageView.setImageResource(slideContent.imageLeftResId)
            textBottomTextView.text = slideContent.textLeft
        }
    }
}