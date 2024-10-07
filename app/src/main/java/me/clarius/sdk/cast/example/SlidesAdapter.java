package me.clarius.sdk.cast.example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SlidesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SlideContent> slides;

    public SlidesAdapter(List<SlideContent> slides) {
        this.slides = slides;
    }

    @Override
    public int getItemViewType(int position) {
        return slides.get(position).getSlideNumber();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_layout_1, parent, false);
                return new SlideViewHolder1(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_layout_2, parent, false);
                return new SlideViewHolder2(view);
            case 3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_layout_3, parent, false);
                return new SlideViewHolder3(view);
            case 4:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_layout_4, parent, false);
                return new SlideViewHolder4(view);
            case 5:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_layout_5, parent, false);
                return new SlideViewHolder5(view);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SlideContent slideContent = slides.get(position);
        switch (holder.getItemViewType()) {
            case 1:
                ((SlideViewHolder1) holder).bind(slideContent);
                break;
            case 2:
                ((SlideViewHolder2) holder).bind(slideContent);
                break;
            case 3:
                ((SlideViewHolder3) holder).bind(slideContent);
                break;
            case 4:
                ((SlideViewHolder4) holder).bind(slideContent);
                break;
            case 5:
                ((SlideViewHolder5) holder).bind(slideContent);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return slides.size();
    }

    static class SlideViewHolder1 extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView textLeftTextView;
        ImageView imageRightImageView;

        SlideViewHolder1(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_slide);
            textLeftTextView = itemView.findViewById(R.id.text_left);
            imageRightImageView = itemView.findViewById(R.id.image_right);
        }

        void bind(SlideContent slideContent) {
            titleTextView.setText(slideContent.getTitle());
            textLeftTextView.setText(slideContent.getTextLeft());
            imageRightImageView.setImageResource(slideContent.getImageRightResId());
        }
    }

    static class SlideViewHolder2 extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView textLeftTextView;
        TextView textRightTextView;

        SlideViewHolder2(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_slide);
            textLeftTextView = itemView.findViewById(R.id.text_left);
            textRightTextView = itemView.findViewById(R.id.text_right);
        }

        void bind(SlideContent slideContent) {
            titleTextView.setText(slideContent.getTitle());
            textLeftTextView.setText(slideContent.getTextLeft());
            textRightTextView.setText(slideContent.getTextRight());
        }
    }

    static class SlideViewHolder3 extends RecyclerView.ViewHolder {
        // Similar to SlideViewHolder1
        TextView titleTextView;
        TextView textLeftTextView;
        ImageView imageRightImageView;

        SlideViewHolder3(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_slide);
            textLeftTextView = itemView.findViewById(R.id.text_left);
            imageRightImageView = itemView.findViewById(R.id.image_right);
        }

        void bind(SlideContent slideContent) {
            titleTextView.setText(slideContent.getTitle());
            textLeftTextView.setText(slideContent.getTextLeft());
            imageRightImageView.setImageResource(slideContent.getImageRightResId());
        }
    }

    static class SlideViewHolder4 extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView imageLeftImageView;
        TextView textTopRightTextView;
        ImageView imageBottomRightImageView;

        SlideViewHolder4(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_slide);
            imageLeftImageView = itemView.findViewById(R.id.image_left);
            textTopRightTextView = itemView.findViewById(R.id.text_top_right);
            imageBottomRightImageView = itemView.findViewById(R.id.image_bottom_right);
        }

        void bind(SlideContent slideContent) {
            titleTextView.setText(slideContent.getTitle());
            imageLeftImageView.setImageResource(slideContent.getImageLeftResId());
            textTopRightTextView.setText(slideContent.getTextLeft());
            imageBottomRightImageView.setImageResource(slideContent.getImageRightResId());
        }
    }

    static class SlideViewHolder5 extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView imageLeftImageView;
        TextView textBottomTextView;

        SlideViewHolder5(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_slide);
            imageLeftImageView = itemView.findViewById(R.id.image_left);
            textBottomTextView = itemView.findViewById(R.id.text_bottom);
        }

        void bind(SlideContent slideContent) {
            titleTextView.setText(slideContent.getTitle());
            imageLeftImageView.setImageResource(slideContent.getImageLeftResId());
            textBottomTextView.setText(slideContent.getTextLeft());
        }
    }
}