package com.shaubert.ui.slidingtab.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import com.shaubert.ui.slidingtab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private SlidingTabLayout tabLayout;
    private ViewAdapter viewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar();

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewAdapter = new ViewAdapter();
        viewAdapter.add();
        viewPager.setAdapter(viewAdapter);
        tabLayout.setViewPager(viewPager);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.ab_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        tabLayout = (SlidingTabLayout) findViewById(R.id.toolbar_tabs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.if_less_then_3_item).setChecked(
                tabLayout.getStretchOption() == SlidingTabLayout.StretchOption.IF_LESS_THAN_3);
        menu.findItem(R.id.if_possible).setChecked(
                tabLayout.getStretchOption() == SlidingTabLayout.StretchOption.IF_POSSIBLE);
        menu.findItem(R.id.always).setChecked(
                tabLayout.getStretchOption() == SlidingTabLayout.StretchOption.ALWAYS);
        menu.findItem(R.id.none).setChecked(
                tabLayout.getStretchOption() == SlidingTabLayout.StretchOption.NONE);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item:
                viewAdapter.add();
                return true;
            case R.id.remove_item:
                viewAdapter.remove();
                return true;

            case R.id.if_less_then_3_item:
                tabLayout.setStretchOption(SlidingTabLayout.StretchOption.IF_LESS_THAN_3);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.if_possible:
                tabLayout.setStretchOption(SlidingTabLayout.StretchOption.IF_POSSIBLE);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.always:
                tabLayout.setStretchOption(SlidingTabLayout.StretchOption.ALWAYS);
                supportInvalidateOptionsMenu();
                return true;
            case R.id.none:
                tabLayout.setStretchOption(SlidingTabLayout.StretchOption.NONE);
                supportInvalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class ViewAdapter extends PagerAdapter {

        private List<String> messages = new ArrayList<>();

        public void add() {
            messages.add("Page " + (messages.size() + 1));
            notifyDataSetChanged();
        }

        public void remove() {
            if (!messages.isEmpty()) {
                messages.remove(messages.size() - 1);
                notifyDataSetChanged();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return messages.get(position);
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String item = messages.get(position);

            TextView textView = new TextView(container.getContext());
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            textView.setGravity(Gravity.CENTER);
            textView.setText(item);
            textView.setTag(item);
            container.addView(textView);

            return textView;
        }

        @Override
        public int getItemPosition(Object object) {
            return messages.indexOf(((View) object).getTag());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
