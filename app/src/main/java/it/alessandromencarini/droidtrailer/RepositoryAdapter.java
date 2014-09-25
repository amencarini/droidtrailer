package it.alessandromencarini.droidtrailer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alessandromencarini on 18/09/2014.
 */
public class RepositoryAdapter extends ArrayAdapter<Repository> {
    private final Context mContext;
    private ArrayList<Repository> mRepositories;

    public RepositoryAdapter(Context context, ArrayList<Repository> repositories) {
        super(context, R.layout.list_item_repository, repositories);
        mContext = context;
        mRepositories = repositories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_repository, parent, false);

        final Repository repository = mRepositories.get(position);

        TextView fullNameTextView = (TextView)rowView.findViewById(R.id.list_item_repository_fullNameTextView);
        fullNameTextView.setText(repository.getFullName());

        CheckBox selectedCheckBox = (CheckBox)rowView.findViewById(R.id.list_item_repository_selectedCheckBox);
        selectedCheckBox.setChecked(repository.getSelected());

        selectedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                repository.setSelected(b);
                RepositoryActivity activity = (RepositoryActivity)mContext;
                activity.updateRepository(repository);
            }
        });

        return rowView;
    }
}
