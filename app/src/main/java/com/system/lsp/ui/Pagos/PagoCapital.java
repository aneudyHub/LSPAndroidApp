package com.system.lsp.ui.Pagos;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.system.lsp.R;
import com.system.lsp.fragmentos.FragmentDialogInformacionPrestamo;
import com.system.lsp.provider.Contract;
import com.system.lsp.provider.OperacionesBaseDatos;
import com.system.lsp.utilidades.Progress;
import com.system.lsp.utilidades.UPreferencias;
import com.system.lsp.utilidades.UTiempo;
import com.system.lsp.utilidades.ZebraPrint;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PagoCapital extends AppCompatActivity implements Progress,LoaderManager.LoaderCallbacks<Cursor>{

    private String mPrestamoURI;
    private Cursor cursor;
    public static String  idPrestamos;
    private RecyclerView mList;
    public static EditText mMonto;
    private OperacionesBaseDatos datosBD;
    private TextView mPendiente,mPagar,totalMora,totalCuota;
    private InteresAdapter mAdapter;
    private double montoAPagar;
    private double montoDigitado;
    private double TotalCapital;
    private double totalPagado;
    private String nombreCliente;
    private String tipoPrestamo;
    private String detallePago;
    private String totalPendiente;
    private Context context;
    private ImageView infoPrestamo;
    private TextView nombreClienteDia;
    private ArrayList<ContentProviderOperation> ops  = new ArrayList<>();
    private ProgressDialog server_prog;
    public ProgressDialog mPrinterProgress;
    public ProgressDialog progress;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagos_interes);

        mPrinterProgress = new ProgressDialog(this);
        mPrinterProgress.setTitle("Printing...");
        mPrinterProgress.setCancelable(false);
        mPrinterProgress.setIndeterminate(true);

        datosBD = OperacionesBaseDatos
                .obtenerInstancia(this);

        if(getIntent().getExtras()!=null){
            mPrestamoURI = (String) getIntent().getExtras().get(Contract.PRESTAMOS);
            montoAPagar = (Double) getIntent().getExtras().get(Contract.Cobrador.TOTAL);
            TotalCapital = (Double) getIntent().getExtras().get("MontoCapitalizable");
            //totalAPagar = datosBD.obtenerTotalAPagar(idPrestamos);
            idPrestamos = (String) getIntent().getExtras().get(Contract.Prestamo.ID);
            //montoAPagar = datosBD.obtenerTotalMora(idPrestamos)+TotalInteres;
            montoAPagar = TotalCapital;
            nombreCliente  = (String) getIntent().getExtras().get(Contract.Cobrador.CLIENTE);
            tipoPrestamo  = (String) getIntent().getExtras().get("TipoPrestamo");
            Log.e("TIPO PRESTAMO",tipoPrestamo);
            //idCliente = (String) getIntent().getExtras().get(Contract.Cliente.ID);
        }

        infoPrestamo = (ImageView)findViewById(R.id.info_prestamo);
        infoPrestamo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentDialogInformacionPrestamo dialogFrag = new FragmentDialogInformacionPrestamo();
                dialogFrag.setMessage(context,nombreCliente,idPrestamos);
                FragmentManager fm = getFragmentManager();
                dialogFrag.show(fm, "HOLA");
            }
        });
        getSupportLoaderManager().restartLoader(1, null, this);
        prepareView();


    }

    private void prepareView(){
        mList = (RecyclerView)findViewById(R.id.Lista);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);

        mMonto = (EditText)findViewById(R.id.Monto);
        mMonto.setText(String.valueOf(montoAPagar));
        mMonto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("monto",charSequence.toString());
                if(mAdapter!=null){
                    if(!charSequence.toString().equals("")){
                        montoDigitado = Double.parseDouble(charSequence.toString());

                    }
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

       // nombreClienteDia = (TextView)findViewById(R.id.nombre_cliente_dia);
        //nombreClienteDia.setText("Hola amigo");
        totalMora = (TextView)findViewById(R.id.total_mora);
        totalMora.setText(String.valueOf(datosBD.obtenerTotalMora(idPrestamos)));
        totalCuota = (TextView)findViewById(R.id.total_cuota);
        totalCuota.setText(String.valueOf(TotalCapital));

        mPendiente =(TextView)findViewById(R.id.total_pendiente);
        Log.e("TOTALPENDIENTE",String.valueOf(datosBD.obtenerTotalAPagarInteresSimple(idPrestamos)));
        mPendiente.setText(String.valueOf(datosBD.obtenerTotalAPagarInteresSimple(idPrestamos)));
        totalPendiente =String.valueOf(TotalCapital);
        mPagar =(TextView)findViewById(R.id.total_pagar);
        mPagar.setText(String.valueOf(TotalCapital));
        montoDigitado = Double.parseDouble(mMonto.getText().toString());
        Toolbar toolbar= (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("SALDO PRESTAMO");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pagos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.Procesar) {

            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("ESTA SEGURO DE REALIZAR EL PAGO DE: "+ montoDigitado +" ?");

            alertDialogBuilder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                    Log.e("VALOR MONTODIGITADO",""+String.valueOf(montoDigitado));
                    Log.e("VALOR TOTALPENDIENTE",""+String.valueOf(totalPendiente));
                    if(montoDigitado > Double.parseDouble(totalPendiente)){
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PagoCapital.this);
                        // set title
                        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#FF0000'>ERROR</font>"));

                        // set dialog message
                        alertDialogBuilder
                                .setMessage(Html.fromHtml("MONTO DIGITADO.<br/><br/>" +
                                        "<font color='#FF0000'> El monto digitado es mayor que el Total A pagar. Debe digitar un monto igual o menor</font>") )
                                .setCancelable(false)
                                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        // if this button is clicked, close
                                        // current activity
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();
                    }else{
                        try {
                            pagar();
                        } catch (InterruptedException e) {
                            showAlert(e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }

                }
            });

            alertDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.e("Estos son los datos",""+CuotasAdapter.datos);
                }
            });

            android.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(mPrestamoURI);
        return new CursorLoader(this,uri,null,Contract.PrestamoDetalle.PAGADO+"=?",new String[]{"0"},Contract.PrestamoDetalle.FECHA);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter = new InteresAdapter(this,idPrestamos);
        mAdapter.swapCursor(data);
        mList.setAdapter(mAdapter);
        //setTotalPendiente(mAdapter.getTotalPendiente());
        mAdapter.marcarCuotas(montoDigitado);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void setTotalPendiente(double r){
        DecimalFormat precision = new DecimalFormat("0.00");
        Log.e("TOTAL-PENDIENTE", precision.format(r));
        totalPendiente = precision.format(r);
       //mPendiente.setText(precision.format(r));
    }


    public void pagar() throws InterruptedException {
        showProgress("REALIZANDO PAGO...");

        StringBuilder sb= new StringBuilder() ;


        Cursor c = mAdapter.mItems;
        Log.e("VALORINTERESSSSS","empezeos");
        //aplica para cuotas
        totalPagado = montoDigitado;
            double mCapital=0.0;
            double mInteres=0.0;
            double mAbonado =0.0;
            double mRestante =0.0;
            int mNumeroCuota=0;

            if(montoDigitado <=0){
               return;
            }


            mInteres = Double.parseDouble(totalPendiente);
            //mAbonado = c.getDouble(c.getColumnIndex(Contract.PrestamoDetalle.MONTO_PAGADO));
           //mNumeroCuota = c.getInt(c.getColumnIndex(Contract.PrestamoDetalle.CUOTA));
            Log.e("VALORINTERESSSSS",String.valueOf(mInteres));
            mRestante = mInteres;

            Uri mUri= Contract.Prestamo.crearUriPrestamo(idPrestamos);

            //comprobar si es abono o pago
            if(montoDigitado >= mRestante){
                //pago de la cuota

                mCapital = montoDigitado - mRestante;
                sb.append("Prestamo Saldado" + ";" + Math.abs(mRestante) + ";");
                ops.add( ContentProviderOperation.newUpdate(mUri)
                        .withValue(Contract.Prestamo.CAPITAL_AMORTIZABLE,(mCapital))
                        .withValue(Contract.Prestamo.FECHA_SALDO,UTiempo.obtenerFechaHora())
                        .build());
                montoDigitado -= mRestante;


            }else{
                //abono de la cuota
                Log.e("ESTOY ABONANDO","VAMOS VAMOS");
                mCapital = mRestante - montoDigitado;
                sb.append("Abono a Capital(s)N" + ";" + String.valueOf(Math.abs(montoDigitado))+";");

                ops.add( ContentProviderOperation.newUpdate(mUri)
                        .withValue(Contract.Prestamo.CAPITAL_AMORTIZABLE,(mCapital))
                        .build());


                montoDigitado=0;
            }





        try {
            getContentResolver().applyBatch(Contract.AUTORIDAD,ops);
            //c.close();

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }finally {
            if(c!=null){
               c.close();
            }
        }

        ContentValues valores = new ContentValues();
        int idAutoGenerado = Contract.CuotaPaga.generarIdCuotasPaga();

        //valores.put(Contract.CuotaPaga.ID,idAutoGenerado);
        valores.put(Contract.CuotaPaga.FECHA,UTiempo.obtenerTiempo());
        valores.put(Contract.CuotaPaga.COBRADOR_ID,UPreferencias.obtenerIdUsuario(this));
        valores.put(Contract.CuotaPaga.NOMBRE_COBRADOR,UPreferencias.obtenerNombreUsuario(this));
        valores.put(Contract.CuotaPaga.NOMBRE_CLIENTE,nombreCliente);
        valores.put(Contract.CuotaPaga.MONTO,totalPagado);
        valores.put(Contract.CuotaPaga.TOTALMORA,0.0);
        valores.put(Contract.CuotaPaga.PRESTAMO,idPrestamos);
        valores.put(Contract.CuotaPaga.FECHA_CONSULTA,UTiempo.obtenerFecha());
        valores.put(Contract.CuotaPaga.UPDATE_AT,UTiempo.obtenerTiempo());
        valores.put(Contract.CuotaPaga.CADENA_STRING,sb.toString());
        valores.put(Contract.CuotaPaga.INSERTADO,"1");


        getContentResolver().insert(Contract.CuotaPaga.URI_CONTENIDO,valores);

//        sleep(2000);

        detallePago =sb.toString();



        //Resolve.sincronizarData(Pagos.this);
//        setResult(RESULT_OK);
        Log.e("Detalle perstamo",String.valueOf(detallePago));
        Log.e("TOTAL-E-MORA",String.valueOf(CuotasAdapter.totalMora));
        Log.e("VALOR-FATURA",CuotasAdapter.datos);




        ZebraPrint zebraprint = new ZebraPrint(PagoCapital.this,"imprimir",UTiempo.obtenerFechaHora(),idPrestamos,nombreCliente,
                detallePago,totalPagado,0.0, UPreferencias.obtenerNombreUsuario(PagoCapital.this),
                UPreferencias.obtenerTelefonoCobrador(PagoCapital.this),PagoCapital.this);
        zebraprint.probarlo();




        //finish();



    }


    public void showProgress(String title){
        progress = new ProgressDialog(this);
        progress.setTitle(title);
        progress.setCancelable(false);
        progress.show();
    }



    public void showAlert(String mensaje){
        if(server_prog!=null)
            server_prog.dismiss();


        /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(mensaje);

        alertDialogBuilder.setPositiveButton("salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();*/

         /*Snackbar.make(findViewById(R.id.coordinador),
                            "No hay conexion disponible",
                            Snackbar.LENGTH_LONG).show();*/

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PagoCapital.this);
        // set title
        alertDialogBuilder.setTitle(Html.fromHtml("<font color='#FFF'>INFORMACION</font>"));

        // set dialog message
        alertDialogBuilder
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        if(alertDialog !=null)
                            alertDialog.dismiss();

                        setResult(RESULT_OK);
                        finish();
                    }
                });

        // create alert dialog
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }



    @Override
    public void showProgressPrint(Boolean b) {
        /*if(b){
            mPrinterProgress.show();
       /* }else{
            mPrinterProgress.dismiss();
        }*/
    }

    @Override
    public void error(String msj) {
        Log.e("valor",mPrinterProgress.toString());
        if(mPrinterProgress!=null){
            mPrinterProgress.dismiss();
        }
        Log.e("error printer",msj);
        showAlert(msj);
    }

    @Override
    public void finishPrint(String msj) {
        setResult(RESULT_OK);
        showAlert(msj);
    }

    @Override
    protected void onDestroy() {
        if (progress != null && progress.isShowing())
            progress.dismiss();


        if(alertDialog !=null && alertDialog.isShowing())
            alertDialog.dismiss();


        super.onDestroy();

//        progress.onDetachedFromWindow();
    }
}
