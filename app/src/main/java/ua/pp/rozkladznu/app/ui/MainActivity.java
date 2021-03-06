package ua.pp.rozkladznu.app.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import ua.pp.rozkladznu.app.R;
import ua.pp.rozkladznu.app.provider.ScheduleContract;
import ua.pp.rozkladznu.app.util.MetricaUtils;
import ua.pp.rozkladznu.app.util.UiUtils;

import static java.lang.String.format;
import static ua.pp.rozkladznu.app.provider.ScheduleContract.Group.buildGroupUri;


public class MainActivity extends BaseActivity
        implements ScheduleFragment.OnScheduleItemClickListener,
        ScheduleOfWeekFragment.OnPeriodicityChangeListener,
        LecturersFragment.OnLecturerClickListener, MaterialDialog.ListCallback {

    public static interface EXTRA_KEY {
        String SELECTED_NAV_DRAWER_ITEM_ID = "SELECTED_NAV_DRAWER_ITEM_ID";
    }

    private static final int NAV_DRAWER_ITEM_SCHEDULE = 0;
    private static final int NAV_DRAWER_ITEM_SUBJECTS = 1;
    private static final int NAV_DRAWER_ITEM_LECTURERS = 2;
    private static final int NAV_DRAWER_ITEM_SETTINGS = 3;
    private static final int NAV_DRAWER_ITEM_ABOUT = 4;
    private static final int NAV_DRAWER_ITEM_SEPARATOR = -1;

    private static final int[] NAV_DRAWER_TITLE_RES_ID = {
            R.string.nav_drawer_item_schedule,
            R.string.nav_drawer_item_subjects,
            R.string.nav_drawer_item_lecturers,
            R.string.nav_drawer_item_settings,
            R.string.nav_drawer_item_about
    };

    private static final int[] NAV_DRAWER_ICON_RES_ID = {
            R.drawable.ic_query_builder_white_24dp,
            R.drawable.ic_class_white_24dp,
            R.drawable.ic_people_white_24dp,
            R.drawable.ic_settings_white_24dp,
            R.drawable.ic_help_white_24dp
    };

    private static final int[] PERIODICITY_SUBTITLE_RES_ID = {
            0,
            R.string.numerator,
            R.string.denominator
    };

    private int selectedNavDrawerItemId;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayList<Integer> navDrawerItems = new ArrayList<>();
    private View appBarShadow;
    private DrawerLayout drawerLayout;
    private TextView groupName;
    private TextView departmentName;
    private View[] navDrawerItemViews = null;
    private View.OnClickListener changeGroupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getAccount() != null) {
                new MaterialDialog.Builder(MainActivity.this)
                        .content(R.string.change_group_question)
                        .positiveText(R.string.change_group)
                        .negativeText(R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                changeGroup();
                            }
                        }).show();
            }
        }
    };

    private Toolbar appBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(appBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        appBarShadow = findViewById(R.id.app_bar_shadow);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackground(
                UiUtils.getThemeAttribute(this, R.attr.colorPrimaryDark).resourceId
        );

        groupName = (TextView) drawerLayout.findViewById(R.id.group_name_text);
        departmentName = (TextView) drawerLayout.findViewById(R.id.department_name_text);

        selectedNavDrawerItemId = NAV_DRAWER_ITEM_SCHEDULE;

        if (savedInstanceState != null) {
            selectedNavDrawerItemId = savedInstanceState
                    .getInt(EXTRA_KEY.SELECTED_NAV_DRAWER_ITEM_ID, NAV_DRAWER_ITEM_SCHEDULE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.animator.activity_open_alpha,
                R.animator.activity_close_translate_down);
    }

    @Override
    protected void onAccountChanged() {
        setUpNavDrawer();
    }

    @Override
    protected void onAccountAuthenticated() {
        setUpNavDrawer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_KEY.SELECTED_NAV_DRAWER_ITEM_ID, selectedNavDrawerItemId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (selectedNavDrawerItemId) {
            case NAV_DRAWER_ITEM_SCHEDULE:
                getMenuInflater().inflate(R.menu.schedule_menu, menu);
                break;
        }
        getMenuInflater().inflate(R.menu.default_menu, menu);

        if (getAccount() != null && getAccount().getSubgroupCount() > 0) {
            menu.findItem(R.id.action_change_subgroup).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                Fragment scheduleOfWeek = getFragmentManager().findFragmentById(R.id.main_content);
                if (scheduleOfWeek != null && scheduleOfWeek instanceof ScheduleOfWeekFragment) {
                    ((ScheduleOfWeekFragment) scheduleOfWeek).setCurrentDayPage();
                    return true;
                }
                return false;
            case R.id.action_sync_schedule: {
                Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
                if (fragment != null && (fragment instanceof ScheduleOfWeekFragment)) {
                    ((ScheduleOfWeekFragment) fragment).performSync();
                }
            }
            return true;
            case R.id.action_report_mistake:
                UiUtils.reportScheduleMistake(this, getAccount().getGroupId());
                return true;
            case R.id.action_change_subgroup:
                changeSubgroup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeGroup() {
        drawerLayout.closeDrawer(Gravity.START);
        AccountManager mAccountManager = AccountManager.get(this);
        if (getAccount() != null) {
            MetricaUtils.reportGroupChange(getAccount());
            mAccountManager.removeAccount(getAccount().getBaseAccount(), null, new Handler());
            ContentValues values = new ContentValues();
            values.put(ScheduleContract.Group.UPDATED, 0);
            getContentResolver()
                    .update(buildGroupUri(getAccount().getGroupId()), values, null, null);
            requestLogin();
        }
    }

    private void changeSubgroup() {
        String[] subgroups = new String[getAccount().getSubgroupCount()];
        for (int i = 0; i < getAccount().getSubgroupCount(); i++) {
            subgroups[i] = String.format(getString(R.string.subgroup_format_1), i + 1);
        }

        new MaterialDialog.Builder(this)
                .title(R.string.choose_subgroup_hint)
                .items(subgroups)
                .itemsCallbackSingleChoice(getAccount().getSubgroup() - 1, this)
                .negativeText(R.string.cancel)
                .show();
    }

    @Override
    public void onSelection(MaterialDialog dialog, View v, int position, CharSequence cs) {
        int subgroup = position + 1;
        if (subgroup != getAccount().getSubgroup()) {
            getAccount().setSubgroup(subgroup);
            getGroupAuthenticatorHelper().setSubgroup(getAccount());
            setUpNavDrawerAccountInfo();
            reloadContentFragment();
        }
    }

    private void reloadContentFragment() {
        switch (selectedNavDrawerItemId) {
            case NAV_DRAWER_ITEM_SCHEDULE:
                reloadSchedule();
                break;
            case NAV_DRAWER_ITEM_SUBJECTS:
                reloadSubjects();
                break;
            case NAV_DRAWER_ITEM_LECTURERS:
                reloadLecturers();
                break;
        }
        invalidateOptionsMenu();
    }

    private void setUpNavDrawer() {
        drawerLayout.findViewById(R.id.change_group_box_indicator)
                .setOnClickListener(changeGroupClickListener);

        setUpNavDrawerAccountInfo();

        drawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, appBar,
                R.string.nav_drawer_open,
                R.string.nav_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        navDrawerItems.clear();
        navDrawerItems.add(NAV_DRAWER_ITEM_SCHEDULE);
        navDrawerItems.add(NAV_DRAWER_ITEM_SUBJECTS);
        navDrawerItems.add(NAV_DRAWER_ITEM_LECTURERS);
        navDrawerItems.add(NAV_DRAWER_ITEM_SEPARATOR);
        navDrawerItems.add(NAV_DRAWER_ITEM_SETTINGS);
        navDrawerItems.add(NAV_DRAWER_ITEM_ABOUT);
        createNavDrawerItems();

        onNavDrawerItemClicked(selectedNavDrawerItemId);
    }

    private void setUpNavDrawerAccountInfo() {
        String groupNameText = getString(R.string.group) + " " + getAccount().getGroupName();
        if (getAccount().getSubgroupCount() > 0) {
            groupNameText +=
                    format(getString(R.string.subgroup_format_2), getAccount().getSubgroup());
        }

        groupName.setText(groupNameText);
        departmentName.setText(getAccount().getDepartmentName());
    }

    private void createNavDrawerItems() {
        ViewGroup navDrawerItemsListContainer =
                (ViewGroup) drawerLayout.findViewById(R.id.nav_drawer_items_list);
        if (navDrawerItemsListContainer == null) {
            return;
        }

        navDrawerItemViews = new View[navDrawerItems.size()];
        navDrawerItemsListContainer.removeAllViews();
        int itemId;
        for (int i = 0; i < navDrawerItems.size(); i++) {
            itemId = navDrawerItems.get(i);
            navDrawerItemViews[i] =
                    makeNavDrawerItem(itemId, navDrawerItemsListContainer);
            formatNavDrawerItem(navDrawerItemViews[i], itemId, itemId == selectedNavDrawerItemId);
            navDrawerItemsListContainer.addView(navDrawerItemViews[i]);
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        int layoutResId;

        if (itemId == NAV_DRAWER_ITEM_SEPARATOR) {
            layoutResId = R.layout.nav_drawer_separetor;
        } else {
            layoutResId = R.layout.nav_drawer_item;
        }

        View view = getLayoutInflater().inflate(layoutResId, container, false);

        if (itemId == NAV_DRAWER_ITEM_SEPARATOR) {
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        int iconResId = itemId >= 0 && itemId < NAV_DRAWER_ICON_RES_ID.length ?
                NAV_DRAWER_ICON_RES_ID[itemId] : 0;
        int titleResId = itemId >= 0 && itemId < NAV_DRAWER_TITLE_RES_ID.length ?
                NAV_DRAWER_TITLE_RES_ID[itemId] : 0;

        if (iconResId > 0) {
            iconView.setImageResource(iconResId);
        } else {
            iconView.setVisibility(View.GONE);
        }

        titleView.setText(getString(titleResId));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (itemId == NAV_DRAWER_ITEM_SEPARATOR) {
            return;
        }
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        view.setSelected(selected);

        titleView.setTextColor(selected ?
                UiUtils.getThemeAttribute(this, R.attr.colorPrimary).data :
                getResources().getColor(R.color.nav_drawer_text_color));
        iconView.setColorFilter(selected ?
                UiUtils.getThemeAttribute(this, R.attr.colorPrimary).data :
                getResources().getColor(R.color.nav_drawer_icon_tint));
    }

    private void onNavDrawerItemClicked(int itemId) {
        switch (itemId) {
            case NAV_DRAWER_ITEM_SETTINGS: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.animator.activity_open_translate_right,
                        R.animator.activity_close_alpha);
            }
            return;
            case NAV_DRAWER_ITEM_ABOUT: {
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                overridePendingTransition(R.animator.activity_open_translate_right,
                        R.animator.activity_close_alpha);
            }
            return;
            case NAV_DRAWER_ITEM_SCHEDULE:
                onScheduleSelected();
                break;
            case NAV_DRAWER_ITEM_SUBJECTS:
                onSubjectsSelected();
                break;
            case NAV_DRAWER_ITEM_LECTURERS:
                onLecturersSelected();
                break;
        }
        setSelectedNavDrawerItem(itemId);
        if (drawerLayout.isShown()) {
            drawerLayout.closeDrawer(Gravity.START);
        }
        invalidateOptionsMenu();
    }

    private void setSelectedNavDrawerItem(int itemId) {
        selectedNavDrawerItemId = itemId;
        if (navDrawerItemViews != null) {
            for (int i = 0; i < navDrawerItemViews.length; i++) {
                if (i < navDrawerItems.size()) {
                    int thisItemId = navDrawerItems.get(i);
                    formatNavDrawerItem(navDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    private void onScheduleSelected() {
        getSupportActionBar().setTitle(R.string.schedule);
        hideAppBarShadow();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment == null || !(fragment instanceof ScheduleOfWeekFragment)) {
            getSupportActionBar().setSubtitle(0);
            replaceMainContent(ScheduleOfWeekFragment.newInstance(getAccount()));
        } else {
            reloadSchedule();
        }
    }

    private void onSubjectsSelected() {
        getSupportActionBar().setTitle(R.string.nav_drawer_item_subjects);
        getSupportActionBar().setSubtitle(0);
        showAppBarShadow();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment == null || !(fragment instanceof SubjectsFragment)) {
            replaceMainContent(SubjectsFragment.newInstance(getAccount()));
        } else {
            reloadSubjects();
        }
    }

    private void onLecturersSelected() {
        getSupportActionBar().setTitle(R.string.nav_drawer_item_lecturers);
        getSupportActionBar().setSubtitle(0);
        showAppBarShadow();

        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment == null || !(fragment instanceof LecturersFragment)) {
            replaceMainContent(LecturersFragment.newInstance(getAccount()));
        } else {
            reloadLecturers();
        }
    }

    private void reloadSchedule() {
        Fragment scheduleOfWeek = getFragmentManager().findFragmentById(R.id.main_content);
        if (scheduleOfWeek != null && scheduleOfWeek instanceof ScheduleOfWeekFragment) {
            ((ScheduleOfWeekFragment) scheduleOfWeek).reload(getAccount());
        }
    }

    private void reloadSubjects() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && fragment instanceof SubjectsFragment) {
            ((SubjectsFragment) fragment).reload(getAccount());
        }
    }

    private void reloadLecturers() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment != null && fragment instanceof LecturersFragment) {
            ((LecturersFragment) fragment).reload(getAccount());
        }
    }

    @Override
    public void onScheduleItemClicked(long scheduleItemId) {
        Intent intent = new Intent(this, ScheduleItemActivity.class);
        intent.putExtra(ScheduleItemActivity.ARG_SCHEDULE_ITEM_ID, scheduleItemId);
        startActivity(intent);
        overridePendingTransition(R.animator.activity_open_translate_right,
                R.animator.activity_close_alpha);
    }

    @Override
    public void onLecturerClicked(long lecturerId) {
        Intent intent = new Intent(this, LecturerScheduleActivity.class);
        intent.putExtra(LecturerScheduleActivity.ARG_LECTURER_ID, lecturerId);
        startActivity(intent);
        overridePendingTransition(R.animator.activity_open_translate_right,
                R.animator.activity_close_alpha);
    }

    @Override
    public void onPeriodicityChanged(int periodicity) {
        getSupportActionBar().setSubtitle(PERIODICITY_SUBTITLE_RES_ID[periodicity]);
    }

    private void replaceMainContent(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment).commit();
    }

    private void showAppBarShadow() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            appBarShadow.setVisibility(View.INVISIBLE);
            getSupportActionBar()
                    .setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        } else {
            if (appBarShadow.getVisibility() == View.INVISIBLE) {
                appBarShadow.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideAppBarShadow() {
        if (appBarShadow.getVisibility() == View.VISIBLE) {
            appBarShadow.setVisibility(View.INVISIBLE);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            getSupportActionBar().setElevation(0);
        }
    }

    public static void startClearTask(Activity clientActivity) {
        Intent intent = new Intent(clientActivity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        clientActivity.startActivity(intent);
    }
}
