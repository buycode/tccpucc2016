package com.example.tcc.buycode.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcc.buycode.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ProductSearchActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String Facebook_ID;
    String productID;
    ImageView btProduto;
    ImageView imgProduto;
    ImageView imgPerfil;
    TextView namePerfil;
    TextView resultadoID;
    TextView opcao1;
    TextView op1Text;
    String imgURL_Perfil;
    String apelido_Perfil;
    String receivedType;


    FrameLayout frameLayout;
    LayoutInflater layoutInflater;
    View activityView;

    Context ctx = this;
    ProgressDialog progress;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    final FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(ctx);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent config = getIntent();
        receivedType = config.getStringExtra("TYPE").toString();

        if(receivedType.equals("2")){
            SQLiteDatabase db = mDbHelper.getReadableDatabase();
            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            Cursor result = db.rawQuery("SELECT apelido, foto_url FROM user WHERE status='online'", null);
            result.moveToFirst();
            if(result.getString(result.getColumnIndex("apelido")).toString().isEmpty()){
                apelido_Perfil = "Convidado";
            }else{
                apelido_Perfil = result.getString(result.getColumnIndex("apelido")).toString();
            }
            if (result.getString(result.getColumnIndex("foto_url")).toString().isEmpty()) {
                imgURL_Perfil = "http://www.buycodeapp.esy.es/images/default_avatar.png";
            }else{
                imgURL_Perfil = result.getString(result.getColumnIndex("foto_url")).toString();
            }
            result.close();
            db.close();
        }else{
            apelido_Perfil = ">Administrador<";
            imgURL_Perfil = "http://www.buycodeapp.esy.es/images/default_avatar.png";
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = getLayoutInflater().inflate(R.layout.nav_header_navigation, null);
        imgPerfil = (ImageView) header.findViewById(R.id.ivFoto);
        namePerfil = (TextView) header.findViewById(R.id.tvName);
        namePerfil.setText(apelido_Perfil);
        Picasso.with(this).load(imgURL_Perfil).into(imgPerfil);
        //imgPerfil.setImageURI(Uri.parse(config.getStringExtra("FB_FOTO")));

        navigationView.addHeaderView(header);

        //EXTENDER ACTIVITY COMO NAVIGATIONACTIVITY - COLOCAR ISSO SEMPRE
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_content);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View activityView = layoutInflater.inflate(R.layout.activity_product_search, null, false);
        frameLayout.addView(activityView);


//        Picasso.with(this)
//                .load(config.getStringExtra("FB_FOTO"))
//                .into(imgPerfil);
        /////////////////////////////////////


        opcao1 = (TextView) findViewById(R.id.opNutricional);
        op1Text = (TextView) findViewById(R.id.opNutText);
        resultadoID = (TextView) findViewById(R.id.txtResult);
        btProduto = (ImageView) findViewById(R.id.btScanner);
        imgProduto = (ImageView) findViewById(R.id.product_image);

        opcao1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (op1Text.getVisibility() == View.GONE) {
                    op1Text.setVisibility(View.VISIBLE);
                } else {
                    op1Text.setVisibility(View.GONE);
                }

            }
        });
        btProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(ProductSearchActivity.this);
                integrator.setCaptureActivity(CaptureActivity.class);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES);
                integrator.setPrompt("Scan something");
                integrator.setOrientationLocked(true);
                integrator.setBarcodeImageEnabled(true);
                integrator.setBeepEnabled(true);
                integrator.initiateScan();

            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        getMenuInflater().inflate(R.menu.activity_navigation_drawer, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent newScreen = new Intent(this, ProductSearchActivity.class);
            newScreen.putExtra("TYPE", receivedType);
            startActivity(newScreen);
            finish();
        } else if (id == R.id.nav_produtos) {
            Intent newScreen = new Intent(this, ProductSearchActivity.class);
            newScreen.putExtra("TYPE", receivedType);
            startActivity(newScreen);
            finish();
        } else if (id == R.id.nav_listas) {

        } else if (id == R.id.nav_receitas) {

        } else if (id == R.id.nav_conta) {

        } else if (id == R.id.nav_exit) {
            Log.d("Andamento: ","Atualizou para offline e voltou!");
            SQLiteDatabase online = mDbHelper.getWritableDatabase();
            Cursor rs2 = online.rawQuery("UPDATE user SET status='offline' WHERE status='online'", null);
            rs2.moveToFirst();
            Log.d("Andamento: ","Entrou no if do result.moveToFirst()");
            rs2.close();
            online.close();
            Log.d("Andamento: ","Passou dos close()");
            Toast.makeText(ctx, "Volte sempre!", Toast.LENGTH_SHORT).show();
            Intent back = new Intent(ctx, LoginFragment.class);
            back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(back);
            finish();




        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("SCAN", "Entrou no onActivityResult do Fragment!");
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //Log.d("SCAN", "IntentIntegrator Result = " + result.getContents().toString());
        if (result != null) {
            if (result.getContents() != null) {
                productID = result.getContents();
                searchProduct sp = new searchProduct();
                sp.execute(productID.toString());
            }
        }


    }


    public void showProgressBar(String msg) {
        progress = new ProgressDialog(ctx);
        progress.setMessage(msg.toString());
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        final int totalProgressTime = 50;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while (jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (progress.getWindow().isActive() || progress.isShowing()) {
                    progress.cancel();
                }

            }
        };
        t.start();
    }



    class searchProduct extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar("Carregando..");
        }

        @Override
        protected String doInBackground(String... params) {
            String codigo = params[0];
            String data = "";
            int tmp;

            try {
                URL url = new URL("http://buycodeapp.esy.es/webservice/searchproduct.php?codigo=" + codigo.toString());
                Log.d("URL = ", url.toString());


                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");


                StringBuilder sb = new StringBuilder();
                int HttpResult = httpURLConnection.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(httpURLConnection.getInputStream()));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                } else {
                    Log.d("Mensagem de resposta: ", httpURLConnection.getResponseMessage());
                }
                httpURLConnection.disconnect();


                return sb.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            String err = null;
            Bitmap mIcon11 = null;

            try {
                Log.d("URL RESULT = ", s.toString());
                JSONObject root = new JSONObject(s);
                if (root.getString("barcode").toString().equals(productID.toString())) {

                    Picasso.with(ctx)
                            .load("http://buycodeapp.esy.es/images/" + root.getString("imagem").toString())
                            .resize(320, 300)
                            .into(imgProduto);


                    resultadoID.setText(root.getString("nome").toString());
                    op1Text.setText("Categoria: " + root.getString("categoria").toString()
                            + "\nDescrição: " + root.getString("desc").toString()
                            + "\nPreço: " + root.getString("preco").toString());

                } else {
                    Toast.makeText(ctx, "Produto inválido!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                err = "Exception: " + e.getMessage();

            }

        }
    }
}
