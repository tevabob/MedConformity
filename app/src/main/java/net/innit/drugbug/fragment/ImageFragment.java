package net.innit.drugbug.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.model.MedicationItem;

import static net.innit.drugbug.data.Constants.IMAGE_HEIGHT_FULL;
import static net.innit.drugbug.data.Constants.IMAGE_WIDTH_FULL;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;

/**
 * Displays an image in a clickable, borderless popup
 */
public class ImageFragment extends DialogFragment {
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        long medId = bundle.getLong(INTENT_MED_ID);

        DatabaseDAO db = new DatabaseDAO(context);
        db.open();
        MedicationItem medicationItem = db.getMedication(medId);
        db.close();

        FrameLayout frameLayout = new FrameLayout(context);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        final Bitmap image = medicationItem.getBitmap(context, IMAGE_WIDTH_FULL, IMAGE_HEIGHT_FULL);
        ImageView imageView = new ImageView(context);
        imageView.setClickable(true);
        imageView.setImageBitmap(image);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        frameLayout.addView(imageView);

        return frameLayout;
    }
}
