package babu.com.lockscreentest;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Puntu on 5/29/15.
 */
public class TextView_CustomFont extends TextView {
    public TextView_CustomFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createFont();
    }

    public TextView_CustomFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        createFont();
    }

    public TextView_CustomFont(Context context) {
        super(context);
        createFont();
    }

    public void createFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/lato-regular.ttf");
        setTypeface(font);
    }
}
