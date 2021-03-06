package net.innit.drugbug.util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.innit.drugbug.R;
import net.innit.drugbug.model.DoseItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static net.innit.drugbug.data.Constants.IMAGE_HEIGHT_LIST;
import static net.innit.drugbug.data.Constants.IMAGE_WIDTH_LIST;

/**
 * ListView array adapter for taken and future doses
 */
public class DoseArrayAdapter extends ArrayAdapter<DoseItem> implements UpdateableListAdapter<DoseItem> {
    private final Context context;
    private List<DoseItem> data;
    private int defaultColor;

    public DoseArrayAdapter(Context context, List<DoseItem> doseItems) {
        super(context, R.layout.list_item_dose, doseItems);

        this.context = context;
        data = doseItems;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;

        DoseItem doseItem = data.get(position);

        if (convertView == null) {
            mViewHolder = new ViewHolder();

            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item_dose, parent, false);

            mViewHolder.name = (TextView) convertView.findViewById(R.id.tvListItemName);
            mViewHolder.dateLabel = (TextView) convertView.findViewById(R.id.tvListItemDateLabel);
            mViewHolder.date = (TextView) convertView.findViewById(R.id.tvListItemDate);
            mViewHolder.reminderImg = (ImageView) convertView.findViewById(R.id.ivListItemReminder);
            mViewHolder.image = (ImageView) convertView.findViewById(R.id.ivDoseListImage);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String display = doseItem.getMedication().getName() + " (" + doseItem.getDosage() + ")";
        mViewHolder.name.setText(display);

        mViewHolder.dateLabel.setText((doseItem.isTaken()) ? context.getString(R.string.dose_adapter_date_label_taken) : context.getString(R.string.dose_adapter_date_label_due));

        boolean missedDose = false;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault());
        Date date = doseItem.getDate();
        // Change the date to red if the dose is untaken and late
        Date now = new Date();

        if (mViewHolder.date.getTextColors().getDefaultColor() != Color.RED)
            defaultColor = mViewHolder.date.getTextColors().getDefaultColor();

        if ((!doseItem.isTaken()) && (now.after(date))) {
            mViewHolder.date.setTextColor(Color.RED);
            missedDose = true;
        } else {
            mViewHolder.date.setTextColor(defaultColor);
        }

        display = simpleDateFormat.format(doseItem.getDate());
        mViewHolder.date.setText(display);

        // Don't display the reminder set TextView if dose is taken
        // If dose is future, don't display the reminder set TextView if dose is missed
        // If future dose hasn't been missed, don't display the reminder set TextView if dose reminder isn't set
        if (doseItem.isTaken() || missedDose) {
            mViewHolder.reminderImg.setVisibility(View.INVISIBLE);
        } else {
            mViewHolder.reminderImg.setVisibility(View.VISIBLE);
            if (!doseItem.isReminderSet()) {
                mViewHolder.reminderImg.setImageResource(R.drawable.ic_content_alarm_off);
            } else {
                mViewHolder.reminderImg.setImageResource(R.drawable.ic_content_alarm_on);
            }
        }

        // Replace placeholder image thumbnail with medication's image
        if (doseItem.getMedication().hasImage()) {
            doseItem.getMedication().getBitmap(context, mViewHolder.image, IMAGE_WIDTH_LIST, IMAGE_HEIGHT_LIST);
        } else {
            mViewHolder.image.setImageResource(R.drawable.default_image);
        }

        return convertView;
    }

    @Override
    public void updateList(List<DoseItem> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    /**
     * Holder, used for recycling view
     */
    static class ViewHolder {
        private TextView name;
        private TextView dateLabel;
        private TextView date;
        private ImageView reminderImg;
        private ImageView image;
    }
}
