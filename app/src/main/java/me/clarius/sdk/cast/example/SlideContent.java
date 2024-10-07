package me.clarius.sdk.cast.example;

public class SlideContent {
    private final int slideNumber; // New field to represent the slide number
    private final String title;
    private final String textLeft;
    private final String textRight;
    private final int imageLeftResId; // Image resource ID for left image
    private final int imageRightResId; // Image resource ID for right image

    public SlideContent(int slideNumber, String title, String textLeft, String textRight, int imageLeftResId, int imageRightResId) {
        this.slideNumber = slideNumber;
        this.title = title;
        this.textLeft = textLeft;
        this.textRight = textRight;
        this.imageLeftResId = imageLeftResId;
        this.imageRightResId = imageRightResId;
    }

    public int getSlideNumber() {
        return slideNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getTextLeft() {
        return textLeft;
    }

    public String getTextRight() {
        return textRight;
    }

    public int getImageLeftResId() {
        return imageLeftResId;
    }

    public int getImageRightResId() {
        return imageRightResId;
    }
}