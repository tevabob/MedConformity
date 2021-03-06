package net.innit.drugbug.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.DoseArrayAdapter;
import net.innit.drugbug.util.OnChoiceSelectedListener;
import net.innit.drugbug.util.OnListUpdatedListener;
import net.innit.drugbug.util.ReminderArrayAdapter;
import net.innit.drugbug.util.UpdateableListAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_EDIT;
import static net.innit.drugbug.data.Constants.FILTER_DOSE;
import static net.innit.drugbug.data.Constants.FILTER_FUTURE;
import static net.innit.drugbug.data.Constants.FILTER_NONE;
import static net.innit.drugbug.data.Constants.FILTER_TAKEN;
import static net.innit.drugbug.data.Constants.FROM_REMINDER;
import static net.innit.drugbug.data.Constants.INTENT_DOSE_ID;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;
import static net.innit.drugbug.data.Constants.SORT_DATE_ASC;
import static net.innit.drugbug.data.Constants.SORT_DATE_DESC;
import static net.innit.drugbug.data.Constants.SORT_DOSE;
import static net.innit.drugbug.data.Constants.SORT_NAME_ASC;
import static net.innit.drugbug.data.Constants.SORT_NAME_DESC;
import static net.innit.drugbug.data.Constants.TAG_ADD;
import static net.innit.drugbug.data.Constants.TAG_DETAIL;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_FUTURE;
import static net.innit.drugbug.data.Constants.TYPE_MEDICATION;
import static net.innit.drugbug.data.Constants.TYPE_REMINDER;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;
import static net.innit.drugbug.data.Constants.TYPE_TAKEN;

public class DoseListFragment extends ListFragment {
    private static final int CONTEXT_EDIT = 10001;
    private static final int CONTEXT_DELETE = 10002;
    private static final int CONTEXT_REMINDER_SET = 10003;
    private static final int CONTEXT_TAKEN = 10004;
    private static final int CONTEXT_ONLY_THIS_MED = 10005;

    private DatabaseDAO db;
    private UpdateableListAdapter<DoseItem> adapter;

    private String type;
    private String sortOrder;
    private Long medId;
    private int listItemPressedPos;
    private String filter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseDAO(getActivity());

        Bundle bundle = getArguments();
        type = bundle.getString(TYPE, TYPE_FUTURE);
        medId = bundle.getLong(INTENT_MED_ID);
        sortOrder = bundle.getString(SORT_DOSE);
        filter = bundle.getString(FILTER_DOSE);

        // Set the sortOrder if it wasn't passed in
        if (sortOrder == null) {
            switch (type) {
                case TYPE_TAKEN:
                    sortOrder = SORT_DATE_DESC;
                    break;
                default:
                    sortOrder = SORT_DATE_ASC;
            }
        }

        if (filter == null) {
            filter = FILTER_NONE;
        }

        if (bundle.getBoolean(FROM_REMINDER, false) && (bundle.getLong(INTENT_DOSE_ID) > 0)) {
            showDetailFragment(bundle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_dose, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDisplay();
    }

    /**
     * Redisplay the list of doses
     */
    private void refreshDisplay() {
        switch (type) {
            case TYPE_REMINDER:
                adapter = new ReminderArrayAdapter(getActivity(), getDoses());
                break;
            case TYPE_SINGLE:
                adapter = new DoseArrayAdapter(getActivity(), getDoses());
                break;
            default:
                adapter = new DoseArrayAdapter(getActivity(), getDoses());
        }

        setListAdapter((ListAdapter) adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DoseItem doseItem = getDoses().get(position);
                Bundle bundle = new Bundle();
                bundle.putString(TYPE, type);
                bundle.putLong(INTENT_DOSE_ID, doseItem.getId());
                bundle.putString(SORT_DOSE, sortOrder);

                showDetailFragment(bundle);
            }
        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View v, int pos, long id) {
                listItemPressedPos = pos;
                return false;
            }
        });

        registerForContextMenu(getListView());

    }

    private void showDetailFragment(Bundle bundle) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(bundle);
        fragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                adapter.updateList(getDoses());
            }
        });
        transaction.addToBackStack(null).add(fragment, TAG_DETAIL).commit();
    }

    /**
     * Returns the list of sorted doses
     *
     * @return sorted list of doses
     */
    private List<DoseItem> getDoses() {
        db.open();

        List<DoseItem> doses;
        switch (type) {
            case TYPE_TAKEN:
                doses = db.getAllTaken(getActivity());
                break;
            case TYPE_REMINDER:
                doses = db.getAllFutureWithReminder(getActivity());
                break;
            case TYPE_SINGLE:
                MedicationItem medication = db.getMedication(medId);
                switch (filter) {
                    case FILTER_TAKEN:
                        doses = medication.getAllTaken(getActivity());
                        break;
                    case FILTER_FUTURE:
                        doses = medication.getAllFuture(getActivity());
                        break;
                    default:
                        doses = medication.getAllDoses(getActivity());
                }
                break;
            default:
                doses = db.getAllFuture(getActivity());

        }
        db.close();

        switch (sortOrder) {
            case SORT_DATE_ASC:
                Collections.sort(doses);
                break;
            case SORT_DATE_DESC:
                Collections.sort(doses, new DoseItem.ReverseDateComparator());
                break;
            case SORT_NAME_ASC:
                Collections.sort(doses, new DoseItem.NameComparator());
                break;
            case SORT_NAME_DESC:
                Collections.sort(doses, new DoseItem.ReverseNameComparator());
                break;
        }

        return doses;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        DoseItem doseItem = getDoses().get(listItemPressedPos);

        switch (type) {
            case TYPE_FUTURE:
            case TYPE_MEDICATION:
            case TYPE_REMINDER:
            case TYPE_SINGLE:
                if (doseItem.isTaken()) break;      // no context options for taken items as yet
                menu.add(0, CONTEXT_TAKEN, 0, getString(R.string.context_menu_taken));
                menu.add(0, CONTEXT_EDIT, 1, getString(R.string.dose_list_context_edit));
                menu.add(0, CONTEXT_DELETE, 2, getString(R.string.dose_list_context_delete));
                Date now = new Date();
                if (now.before(doseItem.getDate()))
                    menu.add(0, CONTEXT_REMINDER_SET, 3, getString((doseItem.isReminderSet()) ? R.string.context_menu_reminder_unset : R.string.context_menu_reminder_set));
            case TYPE_TAKEN:
                if (!type.equals(TYPE_SINGLE))
                    menu.add(0, CONTEXT_ONLY_THIS_MED, 4, getString(R.string.context_menu_only_med));
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DoseItem doseItem = getDoses().get(info.position);

        switch (item.getItemId()) {
            case CONTEXT_EDIT:
                Bundle b = new Bundle();
                b.putLong(INTENT_DOSE_ID, doseItem.getId());
                b.putString(ACTION, ACTION_EDIT);
                b.putString(TYPE, type);
                b.putString(SORT_DOSE, sortOrder);
                b.putLong(INTENT_MED_ID, medId);

                Fragment fragment = new AddDoseFragment();
                fragment.setArguments(b);
                getFragmentManager().beginTransaction().add(fragment, TAG_ADD).commit();
                return true;
            case CONTEXT_DELETE:
                doseItem.confirmDelete(getActivity(), new OnListUpdatedListener() {
                    @Override
                    public void onListUpdated() {
                        adapter.updateList(getDoses());
                    }
                });

                return true;
            case CONTEXT_REMINDER_SET:
//                doseItem.toggleReminder();
//                db.open();
//                if (db.updateDose(doseItem)) {
//                    String message = ((doseItem.isReminderSet()) ? getString(R.string.dose_list_toast_reminder_set) : getString(R.string.dose_list_toast_reminder_unset));
//                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getActivity(), R.string.dose_list_toast_db_error_update, Toast.LENGTH_SHORT).show();
//                }
//                db.close();
                doseItem.reminderAllOrOne(getActivity(), new OnChoiceSelectedListener() {
                    @Override
                    public void onChoiceSelected(String choice) {
                        adapter.updateList(getDoses());
                    }
                });
//                refreshDisplay();
                return true;
            case CONTEXT_TAKEN:
                Intent intent = new Intent(getActivity().getApplicationContext(), DoseListActivity.class);
                intent.putExtra(TYPE, TYPE_TAKEN);
                intent.putExtra(SORT_DOSE, sortOrder);
                doseItem.confirmTaken(this, intent);
                return true;
            case CONTEXT_ONLY_THIS_MED:
                intent = new Intent(getActivity().getApplicationContext(), DoseListActivity.class);
                intent.putExtra(TYPE, TYPE_SINGLE);
                intent.putExtra(INTENT_MED_ID, doseItem.getMedication().getId());
                intent.putExtra(SORT_DOSE, sortOrder);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }


}
