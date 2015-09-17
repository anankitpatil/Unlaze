package com.unlazeapp.unlaze;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gc.materialdesign.views.CheckBox;
import com.unlazeapp.R;
import com.unlazeapp.utils.ApiService;
import com.unlazeapp.utils.ApiServiceListener;
import com.unlazeapp.utils.GlobalVars;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by AP on 15/07/15.
 */
public class DeleteFragment extends Fragment {
    private static final String KEY_TITLE = "title";

    public DeleteFragment() {
        // Required empty public constructor
    }

    public static DeleteFragment newInstance(String title) {
        DeleteFragment f = new DeleteFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        f.setArguments(args);
        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_delete, container, false);

        // check
        final CheckBox agree = (CheckBox) v.findViewById(R.id.checkbox);
        agree.setOncheckListener(new CheckBox.OnCheckListener() {
            @Override
            public void onCheck(CheckBox checkBox, boolean b) {

                // if checked button
                final Button delete = (Button) v.findViewById(R.id.button_delete);
                if (b) {
                    delete.setBackgroundResource(R.drawable.button_blue);
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // delete request
                            ApiService call = ApiService.getInstance();
                            try {
                                call.deleteUser(GlobalVars.getInstance().userDetail.getString("id"), new ApiServiceListener() {
                                    @Override
                                    public void onSuccess(JSONObject result) {

                                        // user deleted -- finish activity
                                        getActivity().finish();
                                    }

                                    @Override
                                    public void onFailure() {

                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    delete.setBackgroundResource(R.color.u_lgrey);
                }
            }
        });
        return v;
    }
}
