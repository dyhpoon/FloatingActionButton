package com.dyhpoon.fab.sample;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dyhpoon.fab.FloatingActionButton;
import com.dyhpoon.fab.FloatingActionsMenu;
import com.dyhpoon.fab.ObservableScrollView;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    @SuppressWarnings("deprecation")
    private void initActionBar() {
        if (getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.addTab(actionBar.newTab()
                .setText("ListView")
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        fragmentTransaction.replace(android.R.id.content, new ListViewFragment());
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    }
                }));
            actionBar.addTab(actionBar.newTab()
                .setText("RecyclerView")
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        fragmentTransaction.replace(android.R.id.content, new RecyclerViewFragment());
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    }
                }));
            actionBar.addTab(actionBar.newTab()
                .setText("ScrollView")
                .setTabListener(new ActionBar.TabListener() {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                        fragmentTransaction.replace(android.R.id.content, new ScrollViewFragment());
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    }

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    }
                }));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            TextView content = (TextView) getLayoutInflater().inflate(R.layout.about_view, null);
            content.setMovementMethod(LinkMovementMethod.getInstance());
            content.setText(Html.fromHtml(getString(R.string.about_body)));
            new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setView(content)
                .setInverseBackgroundForced(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ListViewFragment extends Fragment {

        @SuppressLint("InflateParams")
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_listview, container, false);

            ListView list = (ListView) root.findViewById(android.R.id.list);
            ListViewAdapter listAdapter = new ListViewAdapter(getActivity(),
                getResources().getStringArray(R.array.countries));
            list.setAdapter(listAdapter);

            FloatingActionsMenu menu = (FloatingActionsMenu) root.findViewById(R.id.floating_menu);
            menu.attachToListView(list);

            return root;
        }
    }

    public static class RecyclerViewFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));

            RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(), getResources()
                .getStringArray(R.array.countries));
            recyclerView.setAdapter(adapter);

            FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
            fab.attachToRecyclerView(recyclerView);

            return root;
        }
    }

    public static class ScrollViewFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_scrollview, container, false);

            ObservableScrollView scrollView = (ObservableScrollView) root.findViewById(R.id.scroll_view);
            LinearLayout list = (LinearLayout) root.findViewById(R.id.list);

            String[] countries = getResources().getStringArray(R.array.countries);
            for (String country : countries) {
                TextView textView = (TextView) inflater.inflate(R.layout.list_item, container, false);
                String[] values = country.split(",");
                String countryName = values[0];
                int flagResId = getResources().getIdentifier(values[1], "drawable", getActivity().getPackageName());
                textView.setText(countryName);
                textView.setCompoundDrawablesWithIntrinsicBounds(flagResId, 0, 0, 0);

                list.addView(textView);
            }

            FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
            fab.attachToScrollView(scrollView);

            return root;
        }
    }
}