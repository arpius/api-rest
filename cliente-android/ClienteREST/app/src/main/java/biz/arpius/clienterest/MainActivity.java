package biz.arpius.clienterest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button btnInsertar, btnActualizar, btnBorrar, btnObtener, btnListar;
    private EditText edtId, edtNombre, edtEmail, edtAlta;
    private TextView txtResultado;
    private ListView listaUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnInsertar = (Button) findViewById(R.id.btn_insertar);
        btnActualizar = (Button) findViewById(R.id.btn_modificar);
        btnBorrar = (Button) findViewById(R.id.btn_borrar);
        btnObtener = (Button) findViewById(R.id.btn_obtener);
        btnListar = (Button) findViewById(R.id.btn_listar);

        edtId = (EditText) findViewById(R.id.edt_id);
        edtNombre = (EditText) findViewById(R.id.edt_nombre);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtAlta = (EditText) findViewById(R.id.edt_alta);

        txtResultado = (TextView) findViewById(R.id.txt_resultado);

        listaUsuarios = (ListView) findViewById(R.id.lv_usuarios);

        btnInsertar.setOnClickListener(this);
        btnActualizar.setOnClickListener(this);
        btnBorrar.setOnClickListener(this);
        btnObtener.setOnClickListener(this);
        btnListar.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_insertar:
                listaUsuarios.setAdapter(null); //vaciamos el listview
                TareaInsertar tareaInsertar = new TareaInsertar();
                tareaInsertar.execute(
                        edtNombre.getText().toString(),
                        edtEmail.getText().toString());
                limpiarCampos();
                break;

            case R.id.btn_modificar:
                listaUsuarios.setAdapter(null);
                TareaActualizar tareaActualizar = new TareaActualizar();
                tareaActualizar.execute(
                        edtId.getText().toString(),
                        edtNombre.getText().toString(),
                        edtEmail.getText().toString(),
                        edtAlta.getText().toString());
                limpiarCampos();
                break;

            case R.id.btn_borrar:
                listaUsuarios.setAdapter(null);
                TareaEliminar tareaEliminar = new TareaEliminar();
                tareaEliminar.execute(edtId.getText().toString());
                limpiarCampos();
                break;

            case R.id.btn_obtener:
                listaUsuarios.setAdapter(null);
                TareaObtener tareaObtener = new TareaObtener();
                tareaObtener.execute(edtId.getText().toString());
                limpiarCampos();
                break;

            case R.id.btn_listar:
                txtResultado.setText("");
                TareaListar tareaListar = new TareaListar();
                tareaListar.execute();
                limpiarCampos();
                break;
        }
    }

    private void limpiarCampos() {
        edtId.setText("");
        edtNombre.setText("");
        edtEmail.setText("");
        edtAlta.setText("");
    }

    private class TareaListar extends AsyncTask<String, Integer, Boolean> {

        private String[] usuarios;

        @Override
        protected Boolean doInBackground(String... params) {
            boolean resultado = true;
            HttpClient httpClient = new DefaultHttpClient();

            HttpGet get = new HttpGet("http://192.168.1.100/api-rest/v1/usuarios");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());

                JSONArray respJSON = new JSONArray(respStr);
                usuarios = new String[respJSON.length()];

                for(int i=0; i<respJSON.length(); i++) {
                    JSONObject obj = respJSON.getJSONObject(i);

                    int idUsuario = obj.getInt("id");
                    String nombUsuario = obj.getString("nombre");
                    String emailUsuario = obj.getString("email");
                    String altaUsuario = obj.getString("alta");

                    usuarios[i] = idUsuario+ ": " +nombUsuario+ " (" +emailUsuario+ ") <" +altaUsuario+ ">";
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                resultado = false;
            }

            return resultado;
        }

        @Override
        protected void onPostExecute(Boolean resultado) {
            if(resultado) {
                ArrayAdapter<String> adaptador = new ArrayAdapter<String>(
                        MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        usuarios);

                listaUsuarios.setAdapter(adaptador);
            }
        }
    }

    private class TareaObtener extends AsyncTask<String, Integer, Boolean> {

        private int idUsuario;
        private String nombUsuario;
        private String emailUsuario;
        private String altaUsuario;

        @Override
        protected Boolean doInBackground(String... params) {
            boolean resultado = true;
            HttpClient httpClient = new DefaultHttpClient();
            String id = params[0];

            HttpGet get = new HttpGet("http://192.168.1.100/api-rest/v1/usuarios/" +id);
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());

                JSONArray respJSON = new JSONArray(respStr);
                JSONObject obj = respJSON.getJSONObject(0);

                idUsuario = obj.getInt("id");
                nombUsuario = obj.getString("nombre");
                emailUsuario = obj.getString("email");
                altaUsuario = obj.getString("alta");
            }
            catch (Exception ex) {
                ex.printStackTrace();
                resultado = false;
            }

            return resultado;
        }

        @Override
        protected void onPostExecute(Boolean resultado) {
            if(resultado) {
                txtResultado.setText(idUsuario+ ": " +nombUsuario+ " (" +emailUsuario+ ") <" +altaUsuario+ ">");
            }
            else {
                txtResultado.setText(R.string.comprobar_id);
            }
        }
    }

    private class TareaEliminar extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean resultado = true;
            HttpClient httpClient = new DefaultHttpClient();
            String id = params[0];

            HttpDelete del = new HttpDelete("http://192.168.1.100/api-rest/v1/usuarios/" +id);
            del.setHeader("content-type", "application/json");

            try {
                HttpResponse resp = httpClient.execute(del);
                String respStr = EntityUtils.toString(resp.getEntity());

                if(!respStr.equals("true"))
                    resultado = false;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                resultado = false;
            }

            return resultado;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            txtResultado.setText(R.string.usuario_borrado);
        }
    }

    private class TareaActualizar extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean resultado = true;
            HttpClient httpClient = new DefaultHttpClient();
            String id = params[0];
            HttpPut put = new HttpPut("http://192.168.1.100/api-rest/v1/usuarios/" +id);

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("id", params[0]));
                nameValuePairs.add(new BasicNameValuePair("nombre", params[1]));
                nameValuePairs.add(new BasicNameValuePair("email", params[2]));
                nameValuePairs.add(new BasicNameValuePair("alta", params[3]));

                put.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse resp = httpClient.execute(put);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                resultado = false;
            }

            return resultado;
        }

        @Override
        protected void onPostExecute(Boolean resultado) {
            if(resultado) {
                txtResultado.setText(R.string.usuario_modificado);
            }
        }
    }

    private class TareaInsertar extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean resultado = true;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://192.168.1.100/api-rest/v1/usuarios");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("nombre", params[0]));
                nameValuePairs.add(new BasicNameValuePair("email", params[1]));

                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse resp = httpClient.execute(post);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                resultado = false;
            }
            return resultado;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            txtResultado.setText(R.string.usuario_creado);
        }
    }
}
