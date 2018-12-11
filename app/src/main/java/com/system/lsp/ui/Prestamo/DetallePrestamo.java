package com.system.lsp.ui.Prestamo;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.system.lsp.R;
import com.system.lsp.fragmentos.FragmentPrestamoPagado;

import com.system.lsp.fragmentos.FragmentPrestamoPediente;
import com.system.lsp.fragmentos.FragmentTodasLasCuotas;
import com.system.lsp.provider.Contract;
import com.system.lsp.provider.OperacionesBaseDatos;
import com.system.lsp.sync.SyncAdapter;
import com.system.lsp.ui.Pagos.PagoCapital;
import com.system.lsp.ui.Pagos.PagoInteres;
import com.system.lsp.ui.Pagos.Pagos;
import com.system.lsp.utilidades.Resolve;
import com.system.lsp.utilidades.UPreferencias;

import java.text.DateFormat;
import java.util.Date;

public class DetallePrestamo extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private OperacionesBaseDatos datos;
    //private Cursor cursor;
    private Context ctx;

    private int REQ_DET=200;

    public ProgressDialog progress;
    private String idPrestamos;
    private String tipoPrestamo;
    private Double monto;
    private Double totalCuota;
    private float totlInteres;

    private  TextView capital;
    private  TextView interes;
    private  TextView mora;
    private  TextView balance;
    private  TextView fecha;
    private  TextView cuotas;
    private  TextView pagas;
    private  TextView pendiente;
    private  TextView nombre;
    private  TextView documento;
    private  TextView iPrestamo;
    private  Button adelantarPago;
    public OperacionesBaseDatos operacionesBaseDatos;
    private Double montoTotal,montoCapitalizable;
    private ImageView mFoto,mLlamar;

    private BroadcastReceiver receptorSync;




    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filtroSync = new IntentFilter(Resolve.ACTION_DETALLE_PRESTAMOS);
        LocalBroadcastManager.getInstance(this).registerReceiver(receptorSync, filtroSync);
        Log.e("ESTOY EN RESUME","");
        //startTime(getContext());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_prestamo);
        
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        balance = (TextView)findViewById(R.id.balance);


        idPrestamos = (String) getIntent().getExtras().get(Contract.Prestamo.ID);
        tipoPrestamo = (String) getIntent().getExtras().get("ESTADO");

        UPreferencias.guardarIdPrestamos(this,idPrestamos);

        Log.e("Este es el valor",idPrestamos);
        Log.e("Este es el TipoPrestamo",tipoPrestamo);
        getSupportActionBar().setTitle("Detalle Prestamo");

        capital = (TextView) findViewById(R.id.capital);
        interes = (TextView) findViewById(R.id.interes);
        mora = (TextView) findViewById(R.id.mora);
        balance = (TextView) findViewById(R.id.balance);
        fecha = (TextView) findViewById(R.id.fecha);
        cuotas = (TextView) findViewById(R.id.cuotas);
        pagas = (TextView) findViewById(R.id.pagada);
        pendiente = (TextView) findViewById(R.id.pendiente);
        nombre = (TextView) findViewById(R.id.nombre_cliente);
        documento = (TextView) findViewById(R.id.documento);
        iPrestamo = (TextView) findViewById(R.id.idprestamo);
        mFoto = (ImageView)findViewById(R.id.Foto);
        mFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog pDialog;


                final AlertDialog.Builder builder = new AlertDialog.Builder(DetallePrestamo.this);
                builder.setTitle("Foto");
                View view = getLayoutInflater().inflate(R.layout.dialog_foto_layout,null);
                builder.setView(view);
                ImageView foto = (ImageView) view.findViewById(R.id.DialogFoto_Foto);

                RequestOptions options = new RequestOptions()
                        .fitCenter()
                        .placeholder(getResources().getDrawable(R.drawable.index))
                        .error(getResources().getDrawable(R.drawable.ic_error_info));

                String Url = UPreferencias.obtenerUrlFoto(DetallePrestamo.this)+documento.getText().toString()+".jpg";
                Glide.with(DetallePrestamo.this).load(Url).apply(options).into(foto);


                pDialog = builder.create();
                pDialog.show();
            }
        });
        mLlamar=(ImageView)findViewById(R.id.Llamar);
        mLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo agregar a la consulta de datos los numero de telefono y celular para poder llamar
            }
        });




        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        datos = OperacionesBaseDatos
                .obtenerInstancia(getApplicationContext());

        adelantarPago = (Button)findViewById(R.id.adelantarPago);
        adelantarPago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(DetallePrestamo.this, Pagos.class);

                if (tipoPrestamo.equals("5")){
                    if (datos.isCuotasPrestamoPendienteExists(idPrestamos)){
                        intent = new Intent(DetallePrestamo.this, PagoInteres.class);
                    }else {
                        intent = new Intent(DetallePrestamo.this, PagoCapital.class);
                    }

                }

                Uri uri;

                uri = Contract.PrestamoDetalle.crearUriPrestamoDetalle(idPrestamos);
                Log.e("URI-PRE",uri.toString());
                Log.e("ID-PRESTAMO",Contract.Prestamo.obtenerIdPrestamo(uri));
                Log.e("URI-PRE",nombre.getText().toString());

                if (null != uri) {
                    intent.putExtra(Contract.PRESTAMOS, uri.toString());
                    intent.putExtra(Contract.Cobrador.TOTAL,monto);
                    Log.e("MONTO EN CUOTA",""+montoCapitalizable);
                    Log.e("MONTO EN INTERES",""+montoCapitalizable);
                    Log.e("TIPO-PRESTAMO",""+montoCapitalizable);
                    intent.putExtra("TotalCuota",totalCuota);
                    intent.putExtra("TotalCapital",totalCuota);
                    intent.putExtra("TotalInteres",totlInteres);
                    intent.putExtra("TipoPrestamo",tipoPrestamo);
                    intent.putExtra("MontoCapitalizable",montoCapitalizable);
                    intent.putExtra(Contract.Prestamo.ID, Contract.Prestamo.obtenerIdPrestamo(uri));
                    intent.putExtra(Contract.Cobrador.CLIENTE,nombre.getText());

                }
                startActivityForResult(intent,REQ_DET);
            }
        });

        new TareaPruebaDatos().execute();

        receptorSync = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Resolve.ACTION_DETALLE_PRESTAMOS)){
                    //mostrarProgreso(false);
                    Log.e("broadcast","llego0000000000 detalles");
                    String mensaje = intent.getStringExtra(Resolve.EXTRA_MENSAJE_DETALLE_PRESTAMOS);

                    android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(DetallePrestamo.this);

                    alertDialogBuilder.setTitle("Info");

                    // set dialog message
                    alertDialogBuilder
                            .setMessage(mensaje)
                            .setCancelable(false)
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();
                    progress.dismiss();
                    new TareaPruebaDatos().execute();
                }
            }
        };

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prestamo_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();

        if(i==R.id.refresh){
            new TareaPruebaDatos().execute();
        }


        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_DET){
            if(resultCode==RESULT_OK){
                new AsyncTask<Void,Void,Void>(){
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        runOnUiThread (new Thread(new Runnable() {
                            public void run() {
                                showProgress("CARGANDO DATOS");

                            }
                        }));
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                    }

                    @Override
                    protected Void doInBackground(Void... voids) {
                        // Verificación para evitar iniciar más de una sync a la vez
//                        Resolve.sincronizarData(DetallePrestamo.this);
                        SyncAdapter syncAdapter = null;
                        Object lock = new Object();
                        synchronized (lock) {
                            if (syncAdapter == null) {
                                syncAdapter = new SyncAdapter(DetallePrestamo.this, true,2);
                            }
                        }

                        return null;
                    }
                }.execute();
            }
        }

    }

    public void showProgress(String title){
        progress = new ProgressDialog(this);
        progress.setTitle(title);
        progress.setCancelable(false);
        progress.show();
    }

    private void consultar(String id)
    {
        //
        // Consultamos el centro por el identificador
        //

        Cursor cursor = null;
        try{
            cursor = datos.ObtenerDatosPrestamoPorId(id);
            operacionesBaseDatos = OperacionesBaseDatos
                    .obtenerInstancia(this);

            int totalPendiente = operacionesBaseDatos.getCuotaPendiete(UPreferencias.obtenerIdPrestamos(this),"0").size();
            int totalPagada = operacionesBaseDatos.getCuotaPendiete(UPreferencias.obtenerIdPrestamos(this),"1").size();
            Log.e("TAOTAL-PENDIENTE",String.valueOf(totalPendiente));

            capital.setText("RD$ "+cursor.getString(cursor.getColumnIndex(Contract.Prestamo.CAPITAL)));
            interes.setText("RD$ "+cursor.getString(cursor.getColumnIndex(Contract.PrestamoDetalle.INTERES)));
            mora.setText("RD$ "+cursor.getString(cursor.getColumnIndex(Contract.PrestamoDetalle.MORA)));
            balance.setText("RD$ "+cursor.getString(cursor.getColumnIndex("Balance")));
            //double b =Double.parseDouble(cursor.getString(cursor.getColumnIndex(Contract.PrestamoDetalle.CAPITAL)));
            montoCapitalizable = cursor.getDouble(cursor.getColumnIndex(Contract.Prestamo.CAPITAL_AMORTIZABLE));
            Log.e("MONTOCAPITALIZABLE",String.valueOf(montoCapitalizable));
            monto = datos.obtenerTotalAPagar(idPrestamos);
            totalCuota = datos.obtenerTotalCuota(idPrestamos);
            totlInteres = datos.obtenerTotalInteres(idPrestamos);



            fecha.setText(cursor.getString(cursor.getColumnIndex(Contract.Prestamo.FECHA_INICIO)));

            cuotas.setText(cursor.getString(cursor.getColumnIndex(Contract.Prestamo.CUOTAS)));
            pagas.setText(String.valueOf(totalPagada));
            pendiente.setText(String.valueOf(totalPendiente));
            nombre.setText(cursor.getString(cursor.getColumnIndex(Contract.Cliente.NOMBRE)));
            documento.setText(cursor.getString(cursor.getColumnIndex(Contract.Cliente.DOCUMENTO)));
            iPrestamo.setText("# "+idPrestamos);
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }


    public class TareaPruebaDatos extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // [QUERIES]
           Log.d("Clientes","Clientes");
            //DatabaseUtils.dumpCursor(datos.ObtenerDatosPrestamoPorId(idPrestamos));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    consultar(idPrestamos);
                }
            });


            return null;
        }
    }






    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receptorSync);
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
            View rootView = inflater.inflate(R.layout.fragment_detalle_prestamo, container, false);
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
            //return PlaceholderFragment.newInstance(position + 1);


            switch (position){
                case 0:
                    FragmentTodasLasCuotas todosLosPrestamo = new FragmentTodasLasCuotas();
                    return todosLosPrestamo;
                case 1:
                    FragmentPrestamoPediente fragmentPrestamoPediente = new FragmentPrestamoPediente();
                    return fragmentPrestamoPediente;
                case 2:
                    FragmentPrestamoPagado fragmentPrestamoPagado = new FragmentPrestamoPagado();
                    return fragmentPrestamoPagado;

            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }


    }
}
