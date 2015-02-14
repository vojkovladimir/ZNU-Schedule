package ua.zp.rozklad.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.zp.rozklad.app.R;
import ua.zp.rozklad.app.adapter.CursorRecyclerViewAdapter;
import ua.zp.rozklad.app.provider.ScheduleContract.FullLecturer;
import ua.zp.rozklad.app.provider.ScheduleContract.FullSchedule.Summary;

import static ua.zp.rozklad.app.provider.ScheduleContract.combineArgs;
import static ua.zp.rozklad.app.provider.ScheduleContract.combineSelection;

public class LecturersFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String ARG_GROUP_ID = "groupId";
    private static final String ARG_SUBGROUP = "subgroup";

    private OnLecturerClickListener mListener;

    private int groupId;
    private int subgroup;

    private RecyclerView mRecyclerView;
    private LecturersAdapter mAdapter;

    public static LecturersFragment newInstance(int groupId, int subgroup) {
        LecturersFragment fragment = new LecturersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_GROUP_ID, groupId);
        args.putInt(ARG_SUBGROUP, subgroup);
        fragment.setArguments(args);
        return fragment;
    }

    public LecturersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getInt(ARG_GROUP_ID);
            subgroup = getArguments().getInt(ARG_SUBGROUP);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLecturerClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLecturerClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subjects, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new LecturersAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(getActivity());

        loader.setUri(FullLecturer.CONTENT_URI);
        loader.setProjection(FullLecturer.PROJECTION);
        loader.setSelection(combineSelection(Summary.Selection.GROUP, Summary.Selection.SUBGROUP));
        loader.setSelectionArgs(combineArgs(groupId, subgroup));
        loader.setSortOrder(FullLecturer.DEFAULT_SORT_ORDER);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public void reload(int groupId, int subgroup) {
        this.groupId = groupId;
        this.subgroup = subgroup;
        if (getArguments() != null) {
            getArguments().putInt(ARG_GROUP_ID, groupId);
            getArguments().putInt(ARG_SUBGROUP, subgroup);
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    public void onLecturerClicked(long lecturerId) {
        if (mListener != null) {
            mListener.onLecturerClicked(lecturerId);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a lecturer is clicked.
     */
    public interface OnLecturerClickListener {
        /**
         * @param lecturerId id of the row of the lecturer table in the database.
         */
        public void onLecturerClicked(long lecturerId);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        public void update(String text) {
            mTextView.setText(text);
        }
    }

    private class LecturersAdapter extends CursorRecyclerViewAdapter<ViewHolder>
            implements View.OnClickListener {

        public LecturersAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
            viewHolder.update(cursor.getString(FullLecturer.Column.LECTURER_NAME));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(getActivity()).inflate(R.layout.single_line_item, parent, false);
            view.setOnClickListener(this);
            return new ViewHolder(view);
        }

        @Override
        public void onClick(View v) {
            onLecturerClicked(getItemId(mRecyclerView.getChildPosition(v)));
        }
    }
}