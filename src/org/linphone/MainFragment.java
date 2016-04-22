package org.linphone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MainFragment extends Fragment {
	private static MainFragment instance;
	private static boolean isCallTransferOngoing = false;
	
	public boolean mVisible;
	private ImageView mHistory, mContact, mDialer;
	private OnClickListener addContactListener, cancelListener, transferListener;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		instance = this;
        View view = inflater.inflate(R.layout.main_menu, container, false);
        mHistory = (ImageView) view.findViewById(R.id.main_history_ic);
        mHistory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LinphoneActivity.instance().displayHistory();
			}
        });
        mDialer = (ImageView) view.findViewById(R.id.main_dialer_ic);
        mDialer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LinphoneActivity.instance().goToDialer();
			}
        });
        mContact = (ImageView) view.findViewById(R.id.main_contact_ic);
        mContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LinphoneActivity.instance().displayContacts(false);
			}
        });
		
		return view;
    }

	/**
	 * @return null if not ready yet
	 */
	public static MainFragment instance() { 
		return instance;
	}
	
}
