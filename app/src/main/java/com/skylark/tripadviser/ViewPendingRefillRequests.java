package com.skylark.tripadviser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewPendingRefillRequests extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private ProgressDialog progressDialog;
    private JSONArray pendingList;
    private JSONArray clearedList;
    private JSONArray rejectedList;
    private JSONArray list;
    private static CustomAdapter adapter;
    private static RecyclerView recyclerView;
    private int tab;
    private static TextView tvMsg;
    private TextView totalAmount;
    private String totalPendingAmount = "₹ 0.00";
    private String totalConfirmedAmount = "₹ 0.00";
    private String totalRejectedAmount = "₹ 0.00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pending_money_request);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(ViewPendingRefillRequests.this, R.style.Theme_AppCompat_Light_Dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        totalAmount = findViewById(R.id.totalAmount);

        list = new JSONArray();
        pendingList = new JSONArray();
        clearedList = new JSONArray();
        rejectedList = new JSONArray();

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0) {
                    list = pendingList;
                    totalAmount.setText("₹ " + totalPendingAmount);
                }
                else if(position==1){
                    list = clearedList;
                    totalAmount.setText("₹ " + totalConfirmedAmount);
                }
                else {
                    list = rejectedList;
                    totalAmount.setText("₹ " + totalRejectedAmount);
                }

                if(list.length()<=0) {
                    recyclerView.setVisibility(View.GONE);
                    tvMsg.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvMsg.setVisibility(View.GONE);
                }

                tab = position;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        adapter = new CustomAdapter();
        SharedPreferences pref = getSharedPreferences("com.skylark.fueltrades", MODE_PRIVATE);
        String username = pref.getString("currentUsername", "vinsu");
        if(!isConnected()) {
            Toast.makeText(getApplicationContext(), "Error: No internet connection.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ViewPendingRefillRequests.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            new getPendingRequests().execute(username);
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_view_pending_money_request, container, false);
            recyclerView = rootView.findViewById(R.id.PendingList);
            tvMsg = rootView.findViewById(R.id.tvMsg);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public class getPendingRequests extends AsyncTask<String, Void, Boolean> {

        JSONObject object;
        String msg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            String result = HTTPRequest.sendHTTPRequest("https://fueltrades.prateekmathur.in/php/app/getPendingRefillDetails.php", "username=" + strings[0]);

            try {
                if(result != null) {
                    object = new JSONObject(result);
                    if (object.getString("status").equals("OK")) {
                        pendingList = object.getJSONArray("pending");
                        clearedList = object.getJSONArray("confirmed");
                        rejectedList = object.getJSONArray("rejected");
                        msg = object.getString("message");
                        return true;
                    }
                    else {
                        msg = object.getString("message");
                    }
                }
                else {
                    msg = "Error: Server Not Available, Try again later.";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean status) {
            super.onPostExecute(status);
            progressDialog.dismiss();

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            if(!status) {
                Intent intent = new Intent(ViewPendingRefillRequests.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                list = pendingList;
                try {
                    totalPendingAmount = object.getString("totalPendingAmount");
                    totalConfirmedAmount = object.getString("totalConfirmedAmount");
                    totalRejectedAmount = object.getString("totalRejectedAmount");
                    totalAmount.setText("₹ " + totalPendingAmount);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custom_card_details_layout, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, int position) {
            try {
                JSONObject object = list.getJSONObject(position);
                holder.tvAccount.setText(object.getString("cardNumber"));
                holder.tvTimeStamp.setText(object.getString("timeStamp"));
                holder.tvAmount.setText("₹ " + object.getString("amount"));

                if(tab == 0) {
                    holder.img.setImageDrawable(getDrawable(R.drawable.ic_request));
                }
                else if(tab == 1) {
                    holder.img.setImageDrawable(getDrawable(R.drawable.ic_check_circle));
                }
                else {
                    holder.img.setImageDrawable(getDrawable(R.drawable.ic_add_circle_black));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @Override
        public int getItemCount() {
            if(list != null) {
                return list.length();
            }
            return 0;
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView img;
            private TextView tvAccount;
            private TextView tvTimeStamp;
            private TextView tvAmount;

            public CustomViewHolder(View view) {
                super(view);
                img = view.findViewById(R.id.icon_card);
                tvAccount = view.findViewById(R.id.tvCardNumber);
                tvTimeStamp = view.findViewById(R.id.tvCardCompany);
                tvAmount = view.findViewById(R.id.tvCardBalance);
                img.setImageDrawable(getDrawable(R.drawable.ic_request));

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ViewPendingRefillRequests.this, DetailsActivity.class);
                        try {
                            JSONObject object = list.getJSONObject(recyclerView.getChildLayoutPosition(view));
                            intent.putExtra("Activity", "CardRefillRequests");
                            intent.putExtra("paymentId", object.getString("paymentId"));
                            intent.putExtra("timeStamp", object.getString("timeStamp"));
                            intent.putExtra("amount", object.getString("amount"));
                            intent.putExtra("cardNumber", object.getString("cardNumber"));
                            intent.putExtra("comments", object.getString("comments"));
                            intent.putExtra("status", object.getString("status"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        startActivity(intent);
                    }
                });

            }

        }
    }

}
