package com.example.tcc.buycode.Activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcc.buycode.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;


public class LoginFragment extends AppCompatActivity {

    private String email;
    private String senha;
    EditText txt1;
    EditText txt2;
    Button btGo;
    ProgressDialog progress;
    String Facebook_ID;
    String Facebook_Name;
    Uri Facebook_Foto;
    TextView cadastro;

    //FACEBOOK LOGIN
    private CallbackManager callbackManager;

    //GOOGLE LOGIN
    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    //CONTEXTO
    private Class nextPage = new ProductSearchActivity().getClass();
    LoginFragment ctx = this;

    private static final int LOGIN_TYPE_NORMAL = 1;
    private static final int LOGIN_TYPE_FACEBOOK = 2;
    private static final int LOGIN_TYPE_GOOGLE = 3;
    private static final String URL_BUYCODE = "http://www.buycodeapp.esy.es";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //BANCO DE DADOS
        final FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(ctx);

        // VERIFICAR SE JÁ ESTA LOGADO NO APP
        SQLiteDatabase consulta = mDbHelper.getReadableDatabase();
        Cursor result = consulta.rawQuery("SELECT * FROM user WHERE status='online'", null);
        result.moveToFirst();
        if(result.getCount() > 0){
            doLoginProcess(result.getInt(result.getColumnIndex("tipo_id")));
            Log.d("Andamento: ", "Ja estava logado, entrou!");
        }
        result.close();
        consulta.close();



        //FACEBOOK SIGN IN
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (loginResult.getAccessToken() != null) {
                    Profile profile = Profile.getCurrentProfile();
                    Facebook_ID = profile.getId();
                    Facebook_Name = profile.getName();
                    Facebook_Foto = profile.getProfilePictureUri(250,250);

                    SQLiteDatabase sql1 = mDbHelper.getReadableDatabase();
                    Cursor rs1 = sql1.rawQuery("SELECT * FROM user WHERE id="+ Facebook_ID, null);
                    rs1.moveToFirst();
                    if(rs1.getCount() > 0){
                        SQLiteDatabase sql2 = mDbHelper.getWritableDatabase();
                        Cursor rs2 = sql2.rawQuery("UPDATE user SET status='online' WHERE status='offline' AND id="+ Facebook_ID, null);
                        rs2.moveToFirst();
                        Log.d("Andamento: ", "Ja existia a conta, atualizou e entrou!");
                        rs2.close();
                        sql2.close();
                        doLoginProcess(LOGIN_TYPE_FACEBOOK);
                        rs1.close();
                        sql1.close();
                    }else {
                        Log.d("Andamento: ", "Não tinha conta cadastrada, cadastrou e entrou!");
                        ContentValues values = new ContentValues();
                        values.put("id", Facebook_ID);
                        values.put("tipo_id", LOGIN_TYPE_FACEBOOK);
                        values.put("status", "online");
                        values.put("apelido", Facebook_Name);
                        values.put("foto_url", Facebook_Foto.toString());
                        SQLiteDatabase db = mDbHelper.getWritableDatabase();
                        long newRowId;
                        newRowId = db.insert(
                                "user",
                                null,
                                values);
                        db.close();
                        doLoginProcess(LOGIN_TYPE_FACEBOOK);
                    }
                    Log.d("ImageURI: ", Facebook_Foto.toString());
                }
                }


            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ctx, "Erro: " + error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });


        cadastro = (TextView) findViewById(R.id.tvCadastro);
        cadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cad_Page;
                cad_Page = new Intent(ctx, CadastroActivity.class);
                cad_Page.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cad_Page);
                finish();
            }
        });


       // GOOGLE SIGN IN
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        //FACEBOOK SIGN IN BUTTON CONFIGURATION
        Button faceLoginButton = (Button) findViewById(R.id.face_login_button);
        TextView faceLoginText = (TextView) findViewById(R.id.face_login_text);
        faceLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(ctx, Arrays.asList("public_profile", "user_friends", "email"));
            }
        });
        faceLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(ctx, Arrays.asList("public_profile", "user_friends", "email"));
            }
        });


        //GOOGLE SIGN IN BUTTON CONFIGURATION
        TextView googleLoginText = (TextView) findViewById(R.id.sign_in_text);
        Button googleLoginButton = (Button) findViewById(R.id.sign_in_button);
        googleLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });
        /////////////////////////////////

        //NORMAL LOGIN WITH FIELDS
        txt1 = (EditText) findViewById(R.id.editText);
        txt2 = (EditText) findViewById(R.id.editText2);
        txt1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    email = txt1.getText().toString();
                    if ((!email.contains("@") || !email.contains(".com")) && !email.isEmpty()) {
                        txt1.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                        txt1.setError("E-mail inválido!");
                    } else {
                        txt1.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);

                    }
                }
            }
        });
        txt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = txt1.getText().toString();
                if (email.isEmpty()) {
                    txt1.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btGo = (Button) findViewById(R.id.btgologin);
        btGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Enviando Json para o webservice
                email = txt1.getText().toString();
                senha = txt2.getText().toString();
                if(!email.isEmpty() && !senha.isEmpty()){
                    solicita b = new solicita();
                    b.execute(email, senha);
                }else{
                    Toast.makeText(ctx,"Preencha os dados de login!",Toast.LENGTH_SHORT).show();
                }


            }
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                doLoginProcess(LOGIN_TYPE_GOOGLE);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    public void doLoginProcess(int tipo){
        Intent goScreen;
        switch (tipo){
            case LOGIN_TYPE_NORMAL:
                goScreen = new Intent(this, nextPage);
                goScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goScreen.putExtra("TYPE", "1");
                startActivity(goScreen);
                finish();
                break;

            case LOGIN_TYPE_FACEBOOK:
                goScreen = new Intent(this, nextPage);
                goScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goScreen.putExtra("TYPE", "2");
                startActivity(goScreen);
                finish();
                break;

            case LOGIN_TYPE_GOOGLE:
                goScreen = new Intent(this, nextPage);
                goScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goScreen.putExtra("TYPE", "3");
                startActivity(goScreen);
                finish();
                break;

        }

    }

    public void googleLogin(){
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    class solicita extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar("Carregando..");
        }

        @Override
        protected String doInBackground(String... params) {
            String nome = params[0];
            String pass = params[1];
            String data = "";
            int tmp;

            try {
                URL url = new URL("http://www.buycodeapp.esy.es/webservice/acesso.php?nome=" + nome + "&senha="+pass);
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
            String err=null;
            try {
                Log.d("URL RESULT = ", s.toString());
                JSONObject root = new JSONObject(s);
                if(root.getString("status").toString().equals("ok")) {
                    doLoginProcess(LOGIN_TYPE_NORMAL);
                }else {
                    Toast.makeText(ctx,"Usuário inválido!",Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                err = "Exception: "+e.getMessage();
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

        final int totalProgressTime = 60;
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
}


