package org.linphone;

import java.util.List;

import org.linphone.ContactsFragment.ContactsListAdapter;
import org.linphone.compatibility.Compatibility;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PresenceActivityType;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author gyf
 *
 */
@SuppressLint("DefaultLocale")
public class TVContactsFragment extends Fragment implements OnClickListener, OnItemClickListener, OnItemSelectedListener {
	private LayoutInflater mInflater;
	private GridView contactsList;
	private TextView noContact, newContact;
	private int lastKnownPosition;
	private String sipAddressToAdd;
	private boolean editOnClick = false, editConsumed = false, onlyDisplayChatAddress = false;
	
	private static TVContactsFragment instance;
	
	static final boolean isInstanciated() {
		return instance != null;
	}

	public static final TVContactsFragment instance() {
		return instance;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		mInflater = inflater;
        View view = inflater.inflate(R.layout.contacts_list_tv, container, false);
        
        if (getArguments() != null) {
	        editOnClick = getArguments().getBoolean("EditOnClick");
	        sipAddressToAdd = getArguments().getString("SipAddress");
	        
	        onlyDisplayChatAddress = getArguments().getBoolean("ChatAddressOnly");
        }
        
        noContact = (TextView) view.findViewById(R.id.noContact);
        
        contactsList = (GridView) view.findViewById(R.id.contactsList);
        contactsList.setOnItemClickListener(this);
        contactsList.setOnItemSelectedListener(this);
        
        newContact = (TextView) view.findViewById(R.id.newContact);
        newContact.setOnClickListener(this);
        newContact.setEnabled(LinphoneManager.getLc().getCallsNb() == 0);
        
        return view;
	}
	
	private void changeContactsAdapter() {
		Cursor allContactsCursor = ContactsManager.getInstance().getAllContactsCursor();

		noContact.setVisibility(View.GONE);
		contactsList.setVisibility(View.VISIBLE);
		
		if (allContactsCursor != null && allContactsCursor.getCount() == 0) {
			noContact.setVisibility(View.VISIBLE);
			contactsList.setVisibility(View.GONE);
		} else {
			contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getAllContacts(), allContactsCursor));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		Contact contact = (Contact) adapter.getItemAtPosition(position);
		String address = null;
		for (String numberOrAddress : contact.getNumbersOrAddresses()) {
			address = numberOrAddress;
		}
		if (address != null && LinphoneActivity.isInstanciated()) {
			LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
			address += "@linphone.org";
			if (lc != null) {
				LinphoneProxyConfig lpc = lc.getDefaultProxyConfig();
				String to;
				if (lpc != null) {
					if (!address.contains("@")) {
						to = lpc.normalizePhoneNumber(address);
					} else {
						to = address;
					}
				} else {
					to = address;
				}
				Toast.makeText(getActivity(), "To:"+to, Toast.LENGTH_SHORT).show();
				LinphoneActivity.instance().setAddresGoToDialerAndCall(to, contact.getName(), contact.getPhotoUri());
			}
		}
	}


	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int position,
			long id) {
		//Toast.makeText(getActivity(), "Select:"+position, Toast.LENGTH_SHORT).show();
		try{
			Contact contact = (Contact) adapter.getItemAtPosition(position);
			lastKnownPosition = contactsList.getFirstVisiblePosition();
			LinphoneActivity.instance().displayContact(contact, onlyDisplayChatAddress);
		}
		catch(Exception e) {}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.newContact) {
			editConsumed = true;
			LinphoneActivity.instance().addContact(null, sipAddressToAdd);
		} 
	}
	
	@Override
	public void onResume() {
		instance = this;
		super.onResume();
		
		if (editConsumed) {
			editOnClick = false;
			sipAddressToAdd = null;
		}
		
		if (LinphoneActivity.isInstanciated()) {
			LinphoneActivity.instance().selectMenu(FragmentsAvailable.CONTACTS);
			
			if (getResources().getBoolean(R.bool.show_statusbar_only_on_dialer)) {
				LinphoneActivity.instance().hideStatusBar();
			}
		}
		
		invalidate();
	}
	
	@Override
	public void onPause() {
		instance = null;
		super.onPause();
	}
	
	public void invalidate() {
		changeContactsAdapter();
		//contactsList.setSelectionFromTop(lastKnownPosition, 0);
	}

	
	class ContactsListAdapter extends BaseAdapter {
		private int margin;
		private Bitmap bitmapUnknown;
		private List<Contact> contacts;
		private Cursor cursor;
		
		ContactsListAdapter(List<Contact> contactsList, Cursor c) {
			contacts = contactsList;
			cursor = c;

			margin = LinphoneUtils.pixelsToDpi(LinphoneActivity.instance().getResources(), 10);
			bitmapUnknown = BitmapFactory.decodeResource(LinphoneActivity.instance().getResources(), R.drawable.unknown_small);
		}
		
		public int getCount() {
			return cursor.getCount();
		}

		public Object getItem(int position) {
			if (contacts == null || position >= contacts.size()) {
				return Compatibility.getContact(getActivity().getContentResolver(), cursor, position);
			} else {
				return contacts.get(position);
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			Contact contact = null;
			do {
				contact = (Contact) getItem(position);
			} while (contact == null);
			
			if (convertView != null) {
				view = convertView;
			} else {
				view = mInflater.inflate(R.layout.contact_cell_tv, parent, false);
			}
			
			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(contact.getName());
			
			ImageView icon = (ImageView) view.findViewById(R.id.icon);
			if (contact.getPhoto() != null) {
				icon.setImageBitmap(contact.getPhoto());
			} else if (contact.getPhotoUri() != null) {
				icon.setImageURI(contact.getPhotoUri());
			} else {
				icon.setImageBitmap(bitmapUnknown);
			}
			
			return view;
		}
	}

}
