package com.example.criminalintent.fragment;

import java.util.List;

import com.example.criminalfragment.R;
import com.example.criminalintent.CrimeActivity;
import com.example.criminalintent.CrimePageActivity;
import com.example.criminalintent.entity.Crime;
import com.example.criminalintent.entity.CrimeLab;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListfragment extends ListFragment {

	private List<Crime> mCrimes;	
	private String tag = "CrimeListFragment";
	private ListView mListView;
	private TextView mEmptyTextView;
	private boolean mSubtitleVisible;
	
	private CrimeAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		getActivity().setTitle(R.string.crimes_title);
		
		mCrimes = CrimeLab.getInstance(getActivity()).getCrimes();
		
//		ArrayAdapter<Crime> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mCrimes);
		mAdapter = new CrimeAdapter(mCrimes);
		setListAdapter(mAdapter);
		setRetainInstance(true);
		mSubtitleVisible = false;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(container!=null) {
			container.removeAllViews();
		}
		View v = inflater.inflate(R.layout.fragment_crime_list, container);
		if(mAdapter==null || mAdapter.getCount()==0) {
			mListView = (ListView)v.findViewById(R.id.crime_list);
			mEmptyTextView = new TextView(getActivity());  
			mEmptyTextView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			mEmptyTextView.setText(R.string.crime_list_empty);  
			mEmptyTextView.setVisibility(View.GONE);
			((ViewGroup)mListView.getParent()).addView(mEmptyTextView);
			mListView.setEmptyView(mEmptyTextView);
		}
		View view = super.onCreateView(inflater, container, savedInstanceState);
		ListView listView = (ListView)view.findViewById(android.R.id.list);
		if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
			if(mSubtitleVisible) {
				getActivity().getActionBar().setSubtitle(R.string.subtitle);
			}
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
			listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					return false;
				}
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
				}
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					return true;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					switch(item.getItemId()) {
					case R.id.menu_item_delete_crime:
						CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
						CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
						for(int i=adapter.getCount()-1; i>=0; i--) {
							if(getListView().isItemChecked(i)) {
								crimeLab.delete(adapter.getItem(i));
							}
						}
						mode.finish();
						adapter.notifyDataSetChanged();
						return true;
					default:
						return false;
					}
				}
				
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				}
			});
		} else {
			registerForContextMenu(listView);
		}
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Crime c = ((CrimeAdapter)getListAdapter()).getItem(position);
		Intent intent = new Intent(getActivity(), CrimePageActivity.class);
		intent.putExtra(CrimeFragmentConst.EXTRA_CRIME_ID, c.getId());
		intent.putExtra(CrimeFragmentConst.EXTRA_CRIME_OPERATOR, CrimeFragmentConst.CRIM_OPERATOR_SHOW);
		startActivity(intent);
		Log.d(tag, c.getTitle()+" was clicked");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);
		MenuItem showSubtitl = menu.findItem(R.id.menu_item_show_subtitle);
		if(mSubtitleVisible && showSubtitl!=null) {
			showSubtitl.setTitle(R.string.hide_subtitle);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_item_new_crime:
				Intent intent = new Intent(getActivity(), CrimeActivity.class);
				intent.putExtra(CrimeFragmentConst.EXTRA_CRIME_OPERATOR, CrimeFragmentConst.CRIME_OPERATOR_ADD);
				startActivityForResult(intent, 0);
				return true;
			case R.id.menu_item_show_subtitle:
				if(getActivity().getActionBar().getSubtitle() == null) {
					getActivity().getActionBar().setSubtitle(R.string.subtitle);
					mSubtitleVisible = true;
					item.setTitle(R.string.hide_subtitle);
				} else {
					getActivity().getActionBar().setSubtitle(null);
					mSubtitleVisible = false;
					item.setTitle(R.string.show_subtitle);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.crime_list_item_context, menu);
	}
	
	

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int position = info.position;
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		Crime crime = adapter.getItem(position);
		
		switch(item.getItemId()) {
			case R.id.menu_item_delete_crime:
				CrimeLab.getInstance(getActivity()).delete(crime);
				adapter.notifyDataSetChanged();
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private class CrimeAdapter extends ArrayAdapter<Crime> {

		public CrimeAdapter(List<Crime> crimes) {
			super(getActivity(), 0, crimes);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_crime, null);
			}
			
			Crime c = getItem(position);
			TextView titleTextView = (TextView)convertView.findViewById(R.id.crime_list_item_titleTextView);
			TextView dateTextView = (TextView)convertView.findViewById(R.id.crime_list_item_dateTextView);
			CheckBox solvedCheckbox = (CheckBox)convertView.findViewById(R.id.crime_list_item_solvedCheckbox);
			
			titleTextView.setText(c.getTitle());
			dateTextView.setText(DateFormat.format("yyyy/MM/dd HH:mm:ss", c.getDate()));
			solvedCheckbox.setChecked(c.isSolved());
			
			return convertView;
		}
		
		
	}
	
	
}
