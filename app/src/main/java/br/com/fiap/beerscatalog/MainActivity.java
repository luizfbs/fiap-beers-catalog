package br.com.fiap.beerscatalog;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

import br.com.fiap.beerscatalog.adapters.BeerAdapter;
import br.com.fiap.beerscatalog.listeners.RecyclerItemClickListener;
import br.com.fiap.beerscatalog.models.Beer;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "BeersCatalog";
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    private TextView userName;
    private SearchView searchView;

    private RecyclerView beersView;
    private BeerAdapter adapter;

    private TextView noBeers;
    private List<Beer> beers;

    private int totalBeers = 0;
    private boolean loadingBeers = false;

    private GridLayoutManager gridLayoutManager;
    private StickyRecyclerHeadersDecoration headersDecor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FacebookSdk.sdkInitialize(getApplicationContext());

        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        String loggedUser = preferences.getString(LoginActivity.PREF_LOGGEDUSER_NAME, null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StartFormActivity();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userName);
        userName.setText(loggedUser);

        beersView = (RecyclerView) findViewById(R.id.beersView);
        noBeers = (TextView) findViewById(R.id.noBeers);

        totalBeers = Beer.count();
        beers = Beer.retrieve();

        adapter = new BeerAdapter(MainActivity.this, beers);

        beersView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_read_permission);

                builder.setMessage(R.string.message_read_permission);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requestReadExternalStoragePermission();

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {

                requestReadExternalStoragePermission();

            }
        } else {

            beers = Beer.retrieve();
            RefreshData();

        }

        gridLayoutManager = new GridLayoutManager(this, 1);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if(isTablet){
            gridLayoutManager.setSpanCount(3);
        }else{
            headersDecor = new StickyRecyclerHeadersDecoration(adapter);
            beersView.addItemDecoration(headersDecor);
        }
        beersView.setLayoutManager(gridLayoutManager);

        beersView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        StartFormActivity(beers.get(position));
                    }
                })
        );

        beersView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!loadingBeers && !recyclerView.canScrollVertically(1) && totalBeers > beers.size()) {
                    loadingBeers = true;
                    android.util.Log.e(TAG, "Loading beers...");

                    int page = (int) Math.floor(beers.size() / Beer.DEFAULT_PAGE_SIZE);
                    List<Beer> loadedBeers = Beer.retrieve(page);

                    for(int i = 0; i < loadedBeers.size(); i++){
                        if(!beers.contains(loadedBeers.get(i))){
                            beers.add(loadedBeers.get(i));
                        }
                    }
                    RefreshData();

                    android.util.Log.e(TAG, "Beers loaded!");
                    loadingBeers = false;
                }
            }
        });

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                beers = (newText.length() > 0) ? Beer.find(newText) : Beer.retrieve();
                RefreshData();
                return false;
            }
        });
    }

    private void requestReadExternalStoragePermission(){

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    beers = Beer.retrieve();
                    RefreshData();

                } else {

                    Toast.makeText(this, R.string.message_read_permission_denied,
                            Toast.LENGTH_LONG).show();
                    finish();

                }
                return;
            }
        }
    }

    private void StartFormActivity(){
        StartFormActivity(null);
    }

    private void StartFormActivity(Beer editBeer){
        Intent intent = new Intent(MainActivity.this, FormActivity.class);

        if(editBeer != null){
            intent.putExtra(FormActivity.BUNDLE_EXTRA_BEERID_KEY, editBeer.getId());
        }

        startActivityForResult(intent, FormActivity.REQUEST_CODE_SUBMIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FormActivity.REQUEST_CODE_SUBMIT &&
                resultCode == RESULT_OK) {

            totalBeers = Beer.count();
            beers = Beer.retrieve();
            RefreshData();

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            LogoutUser();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void LogoutUser() {
        SharedPreferences preferences = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(LoginActivity.PREF_LOGGEDUSER_NAME);
        editor.commit();

        LoginManager.getInstance().logOut();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void RefreshData(){

        if(beers.size() == 0){

            noBeers.setVisibility(View.VISIBLE);

        }else {

            noBeers.setVisibility(View.GONE);

        }

        adapter.swap(beers);

    }
}
