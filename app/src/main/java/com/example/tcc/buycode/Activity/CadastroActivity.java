package com.example.tcc.buycode.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tcc.buycode.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CadastroActivity extends AppCompatActivity {

    ImageView fotoPerfil;
    TextView apelidoPerfil;
    TextView emailPerfil;
    TextView senhaPerfil;
    Button btSalvar;
    Button btCancelar;

    String apelido, email, senha;
    ProgressDialog progress;

    private static final int PICK_IMAGE_ID = 234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        fotoPerfil = (ImageView) findViewById(R.id.ivFotoUser);
        apelidoPerfil = (TextView) findViewById(R.id.etNickname);
        emailPerfil = (TextView) findViewById(R.id.etEmail);
        senhaPerfil = (TextView) findViewById(R.id.etPassword);
        btSalvar = (Button) findViewById(R.id.btSave);
        btCancelar = (Button) findViewById(R.id.btCancel);

        fotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);//one can be replaced with any action code

            }
        });

        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login;
                login = new Intent(getApplicationContext(), LoginFragment.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            }
        });

        btSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apelido = apelidoPerfil.getText().toString();
                email = emailPerfil.getText().toString();
                senha = senhaPerfil.getText().toString();
                cadastrar sp = new cadastrar();
                sp.execute(apelido, email, senha);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    Picasso.with(this).load(selectedImage).into(fotoPerfil);
                }
        }

    }



    class cadastrar extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String nome = params[0];
            String email = params[1];
            String senha = params[2];
            String data = "";
            int tmp;

            try {
                URL url = new URL("http://buycodeapp.esy.es/webservice/cadastro.php?tipo=1&email="+email.toString()+"&apelido="+nome.toString()+"&senha="+senha.toString());
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
                if (root.getString("status").toString().equals("1")) {
                    Toast.makeText(getApplicationContext(), "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    Intent ok;
                    ok = new Intent(getApplicationContext(), LoginFragment.class);
                    ok.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(ok);
                    finish();
                } else if (root.getString("status").toString().equals("2")) {
                    Toast.makeText(getApplicationContext(), "Erro: Usuario ja esta cadastrado!", Toast.LENGTH_SHORT).show();
                } else if (root.getString("status").toString().equals("3")) {
                    Toast.makeText(getApplicationContext(), "Ocorreu um erro, tente novamente mais tarde!", Toast.LENGTH_SHORT).show();
                }




            } catch (JSONException e) {
                e.printStackTrace();
                err = "Exception: " + e.getMessage();

            }

        }
    }
}
