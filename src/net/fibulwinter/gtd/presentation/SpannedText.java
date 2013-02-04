package net.fibulwinter.gtd.presentation;

import android.text.Spannable;
import android.text.Spanned;
import android.widget.TextView;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class SpannedText {
    private static class Style {
        private Object style;
        private int start;
        private int end;

        private Style(Object style, int start, int end) {
            this.style = style;
            this.start = start;
            this.end = end;
        }
    }

    private String string;
    private List<Style> styles = newArrayList();


    public SpannedText(String string, Object... styles) {
        this.string = string;
        if (!isEmpty()) {
            for (Object style : styles) {
                this.styles.add(new Style(style, 0, string.length()));
            }
        }
    }

    public SpannedText(String string) {
        this.string = string;
    }

    public SpannedText space() {
        return join(" ");
    }

    public SpannedText join(String string) {
        return join(new SpannedText(string));
    }

    public SpannedText join(SpannedText anotherText) {
        SpannedText joined = new SpannedText(this.string + anotherText.string);
        joined.styles.addAll(this.styles);
        for (Style style : anotherText.styles) {
            joined.styles.add(new Style(style.style, style.start + this.string.length(), style.end + this.string.length()));
        }
        return joined;
    }

    public SpannedText style(Object style) {
        SpannedText modified = new SpannedText(this.string, style);
        modified.styles.addAll(this.styles);
        return modified;
    }

    public void apply(TextView textView) {
        textView.setText(string, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable) textView.getText();
        for (Style style : styles) {
            str.setSpan(style.style, style.start, style.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public boolean isEmpty() {
        return string.isEmpty();
    }

}
