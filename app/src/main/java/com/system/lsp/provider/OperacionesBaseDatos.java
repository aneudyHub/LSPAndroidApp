package com.system.lsp.provider;

import android.app.ExpandableListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.system.lsp.modelo.CuotaPaga;
import com.system.lsp.modelo.CuotaPendiente;
import com.system.lsp.ui.Pagos.CuotasAdapter;
import com.system.lsp.utilidades.UPreferencias;
import com.system.lsp.utilidades.UTiempo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suarez on 20/01/2018.
 */

public class OperacionesBaseDatos {

    private static DatabaseHandler baseDatos;
    private static OperacionesBaseDatos instancia = new OperacionesBaseDatos();
    static Context mCtx;

    public OperacionesBaseDatos() {
    }

    public static OperacionesBaseDatos obtenerInstancia(Context context){
        if (baseDatos == null) {
            baseDatos = new DatabaseHandler(context);
        }
        mCtx=context;
        return instancia;
    }

    private static final String CABECERA_PRESTAMO_JOIN_CLIENTE_Y_DATALLEPRESTAMO = "clientes " +
            "INNER JOIN prestamos " +
            "ON clientes.id = prestamos.clientes_id " +
            "INNER JOIN prestamos_detalle " +
            "ON prestamos_detalle.prestamos_id = prestamos.id";

    private static final String CABECERA_PRESTAMO_JOIN_DATALLEPRESTAMO = "prestamos " +
            "INNER JOIN prestamos_detalle " +
            "ON prestamos.id = prestamos_detalle.prestamos_id ";


    private static final String CABECERA_CUOTAS_PAGAS = "cuota_paga ";

    public Double obtenerTotalAPagar(String prestamo){
        double t=0;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Contract.PRESTAMOS_DETALLES);
        Cursor c=null;

        try{
            String[] proyeccion ={
                    "(SUM("+Contract.PrestamoDetalle.CAPITAL+") + SUM("+Contract.PrestamoDetalle.INTERES+") + SUM("+Contract.PrestamoDetalle.MORA+")) - (SUM("+Contract.PrestamoDetalle.MONTO_PAGADO+")+SUM("+Contract.PrestamoDetalle.ABONO_MORA+")) as total"
            };
            c = builder.query(db, proyeccion, Contract.PrestamoDetalle.PRESTAMO+"=? and "+Contract.PrestamoDetalle.PAGADO+"=? and date("+Contract.PrestamoDetalle.FECHA+") <= ?", new String[]{prestamo,"0",UTiempo.obtenerFecha()}, null, null, null);

            if(c!=null){
                c.moveToFirst();
                t = c.getDouble(c.getColumnIndex("total"));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;

        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return t;
    }




    public Double obtenerTotalAPagarInteresSimple(String prestamo){
        double t=0;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(CABECERA_PRESTAMO_JOIN_DATALLEPRESTAMO );
        Cursor c=null;

        try{

            String[] proyeccion ={

                            Contract.PRESTAMOS + "." + Contract.Prestamo.CAPITAL+" + " +
                            "(SUM("+Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.INTERES+") + " +
                            "SUM("+Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.MORA+")) - " +
                            "(SUM("+Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.MONTO_PAGADO+")+" +
                            "SUM("+Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.ABONO_MORA+")) as total"
            };
            c = builder.query(db, proyeccion, Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.PRESTAMO+"=? and "+Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.PAGADO+"=? ", new String[]{prestamo,"0"}, null, null, null);

            if(c!=null){
                c.moveToFirst();
                t = c.getDouble(c.getColumnIndex("total"));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;

        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return t;
    }


    public Double obtenerTotalCuota(String prestamo){
        double t=0;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Contract.PRESTAMOS_DETALLES);
        Cursor c = null;
        try{
            String[] proyeccion ={
                    "(SUM("+Contract.PrestamoDetalle.CAPITAL+") + SUM("+Contract.PrestamoDetalle.INTERES+")) - SUM("+Contract.PrestamoDetalle.MONTO_PAGADO+") as totalCuota"
            };
            c = builder.query(db, proyeccion, Contract.PrestamoDetalle.PRESTAMO+"=? and "+Contract.PrestamoDetalle.PAGADO+"=?", new String[]{prestamo,"0"}, null, null, null);

            if(c!=null){
                c.moveToFirst();
                t = c.getDouble(c.getColumnIndex("totalCuota"));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }

            if(db!=null){
                db.close();
            }
        }
        return t;
    }

    public float obtenerTotalInteres(String prestamo){
        float t=0;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Contract.PRESTAMOS_DETALLES);
        Cursor c = null;
        try{
            String[] proyeccion ={
                    "( SUM("+Contract.PrestamoDetalle.INTERES+")) - SUM("+Contract.PrestamoDetalle.MONTO_PAGADO+") as totalInteres"
            };
            c = builder.query(db, proyeccion, Contract.PrestamoDetalle.PRESTAMO+"=? and "+Contract.PrestamoDetalle.PAGADO+"=? and date("+Contract.PrestamoDetalle.FECHA+") <= ?", new String[]{prestamo,"0",UTiempo.obtenerFecha()}, null, null, null);

            if(c!=null){
                c.moveToFirst();
                t = c.getFloat(c.getColumnIndex("totalInteres"));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }

            if(db!=null){
                db.close();
            }
        }
        return t;
    }




    public Double obtenerTotalMora(String idPrestamo){
        double t=0;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(Contract.PRESTAMOS_DETALLES);
        Cursor c=null;
        try{
            String[] proyeccion ={
                    "SUM("+Contract.PrestamoDetalle.MORA+") - SUM("+Contract.PrestamoDetalle.ABONO_MORA+")  as totalMora"
            };
            c = builder.query(db, proyeccion, Contract.PrestamoDetalle.PRESTAMO+"=? and "+Contract.PrestamoDetalle.PAGADO+"=? and date("+Contract.PrestamoDetalle.FECHA+") <= ?", new String[]{idPrestamo,"0",UTiempo.obtenerFecha()}, null, null, null);

            if(c!=null){
                c.moveToFirst();
                t = c.getDouble(c.getColumnIndex("totalMora"));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return t;
    }



    public boolean actualizarCuotas(String idPrestamoDetalle,double monto,String MC) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        int resultado =0;
        try{
            ContentValues valores = new ContentValues();
            if(MC.equals("1")){
                valores.put(Contract.PrestamoDetalle.ABONO_MORA,monto);
            }else {
                valores.put(Contract.PrestamoDetalle.MONTO_PAGADO,monto);

            }

            String whereClause = String.format("%s=?", Contract.PrestamoDetalle.ID);
            String[] whereArgs = {idPrestamoDetalle};

             resultado = db.update(Contract.PRESTAMOS_DETALLES, valores, whereClause, whereArgs);
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return resultado > 0;
    }


    public Cursor ObtenerDatosPrestamoPorId(String id){
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c=null;

        try{
            String selection = String.format("%s=?", Contract.PRESTAMOS + "." +Contract.Prestamo.ID,"%s=?");
            String[] selectionArgs = {id};


            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(CABECERA_PRESTAMO_JOIN_CLIENTE_Y_DATALLEPRESTAMO);
            String[] proyeccion = {
                    Contract.CLIENTES + "." + Contract.Cliente.NOMBRE,
                    Contract.CLIENTES + "." + Contract.Cliente.DOCUMENTO,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.CAPITAL,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.TIPO_NOMBRE,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.CAPITAL_AMORTIZABLE,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.CUOTAS,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.FECHA_INICIO,
                    "(       SUM( " + Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.CAPITAL + " ) + " +
                            "SUM(" + Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.INTERES + " ) + " +
                            "SUM(" + Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.MORA + " ) - "+
                            "(SUM(" +Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.MONTO_PAGADO +") + "+
                            "SUM(" +Contract.PRESTAMOS_DETALLES+ "." + Contract.PrestamoDetalle.ABONO_MORA+"))) as Balance",
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.MONTO_PAGADO,
                    "SUM("+Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.INTERES+") AS "+Contract.PrestamoDetalle.INTERES,
                    "SUM("+Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.MORA+") AS "+Contract.PrestamoDetalle.MORA,
            };

            c = builder.query(db, proyeccion, selection, selectionArgs, null, null, null);

            //Nos movemos al primer registro de la consulta
            if (c != null) {
                c.moveToFirst();
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
            if(db!=null){
                db.close();
            }
        }
        return c;

    }

    public Cursor ObtenerInfoPrestamoPorId(String id){
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c=null;
        try{
            String selection;
            selection = String.format("%s=?", Contract.PRESTAMOS + "." +Contract.Prestamo.ID);
            String[] selectionArgs = {id};


            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(CABECERA_PRESTAMO_JOIN_DATALLEPRESTAMO);
            String[] proyeccion = {
                    Contract.PRESTAMOS + "." + Contract.Prestamo.CAPITAL,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.PORCIENTO_MORA,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.CAPITAL,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.INTERES,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.PLAZO,
                    Contract.PRESTAMOS + "." + Contract.Prestamo.CUOTAS,
                    "SUM("+Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.PAGADO+") AS CuotaPagada",

            };

            c = builder.query(db, proyeccion, selection, selectionArgs, null, null, null);

            //Nos movemos al primer registro de la consulta
            if (c != null) {
                c.moveToFirst();
            }

        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if (c != null) {
//                c.close();
//            }
            if (db != null) {
                db.close();
            }
        }
        return c;

    }


    public Cursor ObtenerInfoPrestamoDiasAtrasadoAndMora(String id,String pendiente){
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c = null;
        try{
            String selection;
            if (pendiente.equals("1")){
                selection = String.format("%s=?", Contract.PRESTAMOS + "." +Contract.Prestamo.ID) +
                        " AND "+ Contract.PRESTAMOS + "." +Contract.Prestamo.SALDADO + " = 0" +
                        " AND "+ Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.DIAS_ATRASADOS + " > 0 IS NOT NULL";

            }else {
                selection = String.format("%s=?", Contract.PRESTAMOS + "." +Contract.Prestamo.ID) +
                        " AND "+ Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.PAGADO + " = 0" +
                        " AND "+ Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.DIAS_ATRASADOS + " > 0 IS NOT NULL";

            }String[] selectionArgs = {id};





            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(CABECERA_PRESTAMO_JOIN_DATALLEPRESTAMO);
            String[] proyeccion = {
                    "COUNT("+Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.CUOTA+")  AS CuotasAtrasadas ",
                    "SUM("+Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.DIAS_ATRASADOS+") AS DiasAtrasados ",
                    "(SUM(" + Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.MORA + " ) - " +
                            "SUM(" + Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.ABONO_MORA + " ))  as ValorMora",
                    "SUM(" + Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.ABONO_MORA + " )  as AbonoMora",
            };

            c = builder.query(db, proyeccion, selection, selectionArgs, null, null, null);

            //Nos movemos al primer registro de la consulta
            if (c != null) {
                c.moveToFirst();
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
            if(db!=null){
                db.close();
            }
        }
        return c;

    }




    public Cursor ObtenerCuotasPagasPorCobrador(String id,String fecha){
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c=null;
        try{
            String selection = String.format("%s=?", Contract.CUOTA_PAGADA + "." +Contract.CuotaPaga.COBRADOR_ID) +
                    " AND "+ String.format("%s=?", Contract.CUOTA_PAGADA + "." +Contract.CuotaPaga.FECHA_CONSULTA);;
            String[] selectionArgs = {id,fecha};


            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(CABECERA_CUOTAS_PAGAS);
            String[] proyeccion = {
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.FECHA,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.PRESTAMO,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.NOMBRE_CLIENTE,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.CADENA_STRING,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.MONTO,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.TOTALMORA,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.NOMBRE_COBRADOR,
            };

            c = builder.query(db, proyeccion, selection, selectionArgs, null, null, Contract.CuotaPaga.FECHA+" ASC");

            //Nos movemos al primer registro de la consulta
        /*if (c != null) {
            c.moveToFirst();
        }*/
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
//            if(db!=null){
//                db.close();
//            }
        }
        return c;

    }


    public Cursor cuadreCobrador(String cobradorId){
        Log.e("valorFecha","entre");
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c=null;

        try {
            String selection = String.format("%s=?", Contract.CUOTA_PAGADA + "." +Contract.CuotaPaga.COBRADOR_ID);
            String[] selectionArgs = {cobradorId};


            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(CABECERA_CUOTAS_PAGAS);
            String[] proyeccion = {

                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.NOMBRE_COBRADOR,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.FECHA_CONSULTA,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.PRESTAMO,
                    Contract.CUOTA_PAGADA + "." + Contract.CuotaPaga.MONTO,

            };

            c = builder.query(db, proyeccion, selection, selectionArgs, null, null, null);

            //Nos movemos al primer registro de la consulta
        /*if (c != null) {
            c.moveToFirst();
        }*/
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
//            if(db!=null){
//                db.close();
//            }
        }
        return c;

    }

    public Cursor ObtenerCuotasPendientesOPagadas(String id,String pagado){
        DatabaseHandler handler = new DatabaseHandler(mCtx);

        SQLiteDatabase db =handler.getReadableDatabase();
        Cursor c=null;

        try{
            String selection = String.format("%s=?", Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.PRESTAMO) +
                    " AND "+ String.format("%s=?", Contract.PRESTAMOS_DETALLES + "." +Contract.PrestamoDetalle.PAGADO);
            String[] selectionArgs = {id,pagado};


            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(Contract.PRESTAMOS_DETALLES);
            String[] proyeccion = {

                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.CUOTA,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.CAPITAL,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.INTERES,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.MORA,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.FECHA,
                    Contract.PRESTAMOS_DETALLES + "." + Contract.PrestamoDetalle.PAGADO,

            };

            c = builder.query(db, proyeccion, selection, selectionArgs, null, null, null);

            //Nos movemos al primer registro de la consulta
       /* if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast();c.moveToNext()){
                c.moveToFirst();
            }
        }*/
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
//            if(db!=null)
//            {
//                db.close();
//            }
        }
        return c;

    }



    public boolean actualizarSyncTime(String idCobrador,String fecha) {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        int resultado=0;
        try{
            ContentValues valores = new ContentValues();
            valores.put(Contract.Cobrador.SYNC_TIME, fecha);

            String whereClause = String.format("%s=?", Contract.Cobrador.COBRADOR_ID);
            String[] whereArgs = {idCobrador};

            resultado= db.update(Contract.COBRADOR, valores, whereClause, whereArgs);
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(db!=null){
                db.close();
            }
        }
        return resultado > 0;
    }

    // [OPERACIONES_FORMA_PAGO]
//    public Cursor obtenerSyncTime(String idCobrador) {
//        SQLiteDatabase db = baseDatos.getReadableDatabase();
//        Cursor c=null;
//        try{
//            String sql = String.format("SELECT sync_time FROM %s ", Contract.COBRADOR +" WHERE id = "+idCobrador);
//            c = db.rawQuery(sql, null);
//        }catch (Exception e){
//            FirebaseCrash.report(e);
//        }finally {
//            if(c!=null){
//                c.close();
//            }
//            if(db!=null){
//                db.close();
//            }
//        }
//        return c;
//    }

    // [OPERACIONES_FORMA_PAGO]
    public String obtenerSyncTime(String idCobrador) {
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String fechaSync="2000-01-01";
        Cursor c=null;
        try{
            String sql = "SELECT sync_time FROM "+ Contract.COBRADOR +" WHERE id = "+idCobrador;
            c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                fechaSync = c.getString(c.getColumnIndex(Contract.Cobrador.SYNC_TIME));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return fechaSync==null?"2000-01-01":fechaSync;
    }

    // [OPERACIONES_FORMA_PAGO]
    public Cursor obtenerDetallePrestamo(String idPrestamos) {
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        Cursor c=null;
        try {
            String sql = String.format("SELECT * FROM %s ", Contract.PRESTAMOS_DETALLES +" WHERE prestamos_id = "+idPrestamos);
            c = db.rawQuery(sql, null);
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return c;
    }



    // [OPERACIONES_FORMA_PAGO]
    public String obtenerTipoPrestamo(String idPrestamos) {
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        String tipo="";
        Cursor c=null;
        try {
            String sql = String.format("SELECT estado  FROM %s ", Contract.PRESTAMOS +" WHERE id = "+idPrestamos);
            c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                tipo = c.getString(c.getColumnIndex(Contract.Prestamo.ESTADO));
            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return tipo;
    }


    public List<CuotaPendiente> getCuotaPendiete(String ipPrestamos,String pagado){

        List<CuotaPendiente> list = new ArrayList<>();
        Cursor c= null;
       try{
            c = ObtenerCuotasPendientesOPagadas(ipPrestamos,pagado);
            while (c.moveToNext()) {
                CuotaPendiente cuotaPendiente = new CuotaPendiente();

                cuotaPendiente.setCuota(c.getInt(0));
                cuotaPendiente.setCapital(c.getDouble(1));
                cuotaPendiente.setInteres(c.getDouble(2));
                cuotaPendiente.setMora(c.getDouble(3));
                cuotaPendiente.setFecha(c.getString(4));
                cuotaPendiente.setPagado(c.getInt(5));

                list.add(cuotaPendiente);

            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if (c != null) {
                c.close();
            }
        }
        return  list;

    }



    public List<CuotaPaga> getCutaPagas(String idCobrador,String fecha){

        List<CuotaPaga> list = new ArrayList<>();
        Cursor c = null;
        try{
            c = ObtenerCuotasPagasPorCobrador(idCobrador,fecha);
            while (c.moveToNext()) {
                CuotaPaga cuotaPaga = new CuotaPaga();
                cuotaPaga.setFecha(c.getString(0));
                cuotaPaga.setPrestamoId(c.getInt(1));
                cuotaPaga.setNombreCliente(c.getString(2));
                cuotaPaga.setCadenaString(c.getString(3));
                cuotaPaga.setMonto(c.getDouble(4));
                cuotaPaga.setTotalMora(c.getDouble(5));
                cuotaPaga.setNombreCobrador(c.getString(6));;
                list.add(cuotaPaga);

            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
        }
        return  list;

    }

    public ArrayList<CuotaPaga> getImprimirCuadre(String cobradorId){

        ArrayList<CuotaPaga> list = new ArrayList<>();
        Cursor c =null;
        try{
            c = cuadreCobrador(cobradorId);
            while (c.moveToNext()) {
                CuotaPaga cuotaPaga = new CuotaPaga();
                cuotaPaga.setNombreCobrador(c.getString(0));
                cuotaPaga.setFechaConsulta(c.getString(1));
                cuotaPaga.setPrestamoId(c.getInt(2));
                cuotaPaga.setMonto(c.getDouble(3));

                list.add(cuotaPaga);

            }
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
        }
        return  list;

    }



    // [OPERACIONES_CLIENTE]
    public Cursor obtenerCuotasPagas() {
        SQLiteDatabase db = baseDatos.getReadableDatabase();
        Cursor c=null;
        try{
            String sql = String.format("SELECT * FROM %s", Contract.CUOTA_PAGADA);
            c = db.rawQuery(sql, null);
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }
        return c;
    }

    public Cursor pagosPendiente() {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c = null;
        try{
            String intertado = "1";
            String sql = String.format("SELECT * FROM %s", Contract.CUOTA_PAGADA);
            c = db.rawQuery(sql, null);
        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(c!=null){
                c.close();
            }
            if(db!=null){
                db.close();
            }
        }

       /* String sql = String.format("SELECT * FROM %s", Contract.CUOTA_PAGADA+
                                    " WHERE insertado ="+intertado);*/


        return c;

        //return resultado > 0;
    }

    public Cursor datosCompania() {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c=null;
        try{
            String sql = String.format("SELECT * FROM %s", Contract.COBRADOR);

            c = db.rawQuery(sql, null);
            if (c != null) {
                c.moveToFirst();
            }

        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
//            if(db!=null){
//                db.close();
//            }
        }
        return c;

        //return resultado > 0;
    }



    public Cursor tipoPrestamo() {
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        Cursor c=null;
        try{
            String sql = String.format("SELECT * FROM %s", Contract.PRESTAMOS);

            c = db.rawQuery(sql, null);
            if (c != null) {
                c.moveToFirst();
            }

        }catch (Exception e){
            FirebaseCrash.report(e);
            throw e;
        }finally {
//            if(c!=null){
//                c.close();
//            }
//            if(db!=null){
//                db.close();
//            }
        }
        return c;

        //return resultado > 0;
    }



    public boolean isCuotasPrestamoPendienteExists(String idPrestamo) {
        Cursor cursor = null;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        String pagado = "0";
        boolean result = false;
        try {
            String[] args = { "" + pagado , "" + idPrestamo };
            StringBuffer sbQuery = new StringBuffer("SELECT * from ").append(
                    Contract.PRESTAMOS_DETALLES).append(" where pagado =?").append(" AND prestamos_id =? ");
            cursor = db.rawQuery(sbQuery.toString(), args);
            if (cursor != null && cursor.moveToFirst()) {

                result = true;
            }else {
                result = false;
            }

        } catch (Exception e) {
//            Log.e("Requestdbhelper", e.toString());
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
            if(db !=null){
                db.close();
            }
        }
        return result;
    }

    public boolean isCuotasPagasExists() {
        Cursor cursor = null;
        SQLiteDatabase db = baseDatos.getWritableDatabase();
        String insertado = "1";
        boolean result = false;
        try {
            String[] args = { "" + insertado };
            StringBuffer sbQuery = new StringBuffer("SELECT * from ").append(
                    Contract.CUOTA_PAGADA).append(" where insertado =?");
            cursor = db.rawQuery(sbQuery.toString(), args);
            if (cursor != null && cursor.moveToFirst()) {
                result = true;
            }else {
                result = false;
            }

        } catch (Exception e) {
//            Log.e("Requestdbhelper", e.toString());
            FirebaseCrash.report(e);
            throw e;
        }finally {
            if(cursor!=null){
                cursor.close();
            }
            if(db !=null){
                db.close();
            }
        }
        return result;
    }





}
