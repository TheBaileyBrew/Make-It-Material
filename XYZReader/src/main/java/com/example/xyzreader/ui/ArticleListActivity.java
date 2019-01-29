package com.example.xyzreader.ui;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.adapters.CollapsingToolbarListener;
import com.example.xyzreader.adapters.ImageLoaderHelper;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.objects.DynamicHeightNetworkImageView;
import com.example.xyzreader.utils.ViewTransitionUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ArticleListActivity.class.toString();
    private Toolbar mToolbar;
    private AppBarLayout appBarLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;


    private int lastAnimatedPosition = -1;
    private Interpolator mInterpolator;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_toolbar);
        setupToolbar();

        ViewTransitionUtils.setupEnterExplodeAnimation(this);
        ViewTransitionUtils.setupExitExplodeAnimation(this);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        appBarLayout.setExpanded(true);
        appBarLayout.addOnOffsetChangedListener(new CollapsingToolbarListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case EXPANDED:
                        Log.i(TAG, "onStateChanged: expanded");
                        break;
                    case COLLAPSED:
                        Log.i(TAG, "onStateChanged: collapse");
                        break;
                    case IDLE:
                        Log.i(TAG, "onStateChanged: idle");
                        break;
                }
            }
        });
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Cursor mCursor;

        public Adapter(Cursor cursor) {
            mCursor = cursor;
            mInterpolator = AnimationUtils.loadInterpolator(ArticleListActivity.this,android.R.interpolator.anticipate_overshoot);
        }

        class ViewHolderRight extends RecyclerView.ViewHolder {
            public ImageView thumbnailView;
            public TextView titleView;
            public TextView subtitleView;

            public ViewHolderRight(View view) {
                super(view);

                thumbnailView = view.findViewById(R.id.thumbnail);
                titleView = (TextView) view.findViewById(R.id.article_title);
                subtitleView = (TextView) view.findViewById(R.id.article_subtitle);

            }
        }

        class ViewHolderLeft extends RecyclerView.ViewHolder {
            public ImageView thumbnailView;
            public TextView titleView;
            public TextView subtitleView;

            public ViewHolderLeft(View view) {
                super(view);
                thumbnailView = view.findViewById(R.id.thumbnail);
                titleView = (TextView) view.findViewById(R.id.article_title);
                subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            }
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2 * 2;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
            View view;
            switch(viewType) {
                case 0:
                default:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_article, parent, false);
                    final Adapter.ViewHolderLeft vhl = new Adapter.ViewHolderLeft(view);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    ItemsContract.Items.buildItemUri(getItemId(vhl.getAdapterPosition()))));;
                        }
                    });
                    return vhl;
                case 2:
                    view = getLayoutInflater().inflate(R.layout.list_item_article_alternate, parent, false);
                    final Adapter.ViewHolderRight vhr = new Adapter.ViewHolderRight(view);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    ItemsContract.Items.buildItemUri(getItemId(vhr.getAdapterPosition()))));
                        }
                    });
                    return vhr;
            }
        }

        private Date parsePublishedDate() {
            try {
                String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
                return dateFormat.parse(date);
            } catch (ParseException ex) {
                Log.e(TAG, ex.getMessage());
                Log.i(TAG, "passing today's date");
                return new Date();
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            switch(holder.getItemViewType()) {
                case 0:
                default:
                    Adapter.ViewHolderLeft viewHolderLeft = (Adapter.ViewHolderLeft)holder;
                    viewHolderLeft.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
                    Date publishedDate = parsePublishedDate();
                    if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                        viewHolderLeft.subtitleView.setText(Html.fromHtml(
                                DateUtils.getRelativeTimeSpanString(
                                        publishedDate.getTime(),
                                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_ALL).toString()
                                        + "<br/>" + " by "
                                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));
                    } else {
                        viewHolderLeft.subtitleView.setText(Html.fromHtml(
                                outputFormat.format(publishedDate)
                                        + "<br/>" + " by "
                                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));
                    }
                    Picasso.get()
                            .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                            .into(viewHolderLeft.thumbnailView);
                    setAnimation(viewHolderLeft.itemView, position);
                    break;
                case 2:
                    Adapter.ViewHolderRight viewHolderRight = (Adapter.ViewHolderRight)holder;
                    viewHolderRight.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
                    Date publishedRightDate = parsePublishedDate();
                    if (!publishedRightDate.before(START_OF_EPOCH.getTime())) {
                        viewHolderRight.subtitleView.setText(Html.fromHtml(
                                DateUtils.getRelativeTimeSpanString(
                                        publishedRightDate.getTime(),
                                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_ALL).toString()
                                        + "<br/>" + " by "
                                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));
                    } else {
                        viewHolderRight.subtitleView.setText(Html.fromHtml(
                                outputFormat.format(publishedRightDate)
                                        + "<br/>" + " by "
                                        + mCursor.getString(ArticleLoader.Query.AUTHOR)));
                    }
                    Picasso.get()
                            .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                            .into(viewHolderRight.thumbnailView);
                    setAnimation(viewHolderRight.itemView, position);
                    break;
            }
        }

        private void setAnimation(View viewAnimating, int position) {
            if (position > lastAnimatedPosition) {
                viewAnimating.setTranslationY((position + 1) * 1000);
                viewAnimating.setAlpha(0.85f);
                viewAnimating.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setInterpolator(mInterpolator)
                        .setDuration(1000L)
                        .start();
                lastAnimatedPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

}
