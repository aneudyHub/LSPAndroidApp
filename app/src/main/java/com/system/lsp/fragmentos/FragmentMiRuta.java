package com.system.lsp.fragmentos;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.system.lsp.R;
import com.system.lsp.modelo.DatosCliente;
import com.system.lsp.provider.Contract;
import com.system.lsp.ui.AdaptadorMiRuta;
import com.system.lsp.ui.Pagos.Pagos;
import com.system.lsp.ui.Prestamo.DetallePrestamo;

import java.util.ArrayList;

public class FragmentMiRuta extends Fragment implements AdaptadorMiRuta.OnItemClickListener{

    public ArrayList<Dias> mDias;
    public Spinner mSpinnerDias;
    ArrayAdapter<Dias> arrayAdapter;
    public ArrayList<DatosCliente> mDatos;
    public AdaptadorMiRuta mAdapter;
    public RecyclerView mList;
    public TextView mCantidad;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_mi_ruta, container, false);
        crearDias();
        prepareView(view);
        return view;
    }

    public void prepareView(View view){
        mList = (RecyclerView)view.findViewById(R.id.ListaClientes);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        mList.setLayoutManager(manager);

        mCantidad =(TextView)view.findViewById(R.id.Cantidad);

        mSpinnerDias =(Spinner)view.findViewById(R.id.Dias);
        arrayAdapter = new ArrayAdapter<Dias>(getContext(),  android.R.layout.simple_spinner_item,mDias);
        arrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        mSpinnerDias.setAdapter(arrayAdapter);
        mSpinnerDias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Dias d =(Dias) arrayAdapter.getItem(position);
                getData(d.num);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    public void crearDias(){
        mDias = new ArrayList<>();
        mDias.add(new Dias(1,"LUNES"));
        mDias.add(new Dias(2,"MARTES"));
        mDias.add(new Dias(3,"MIERCOLES"));
        mDias.add(new Dias(4,"JUEVES"));
        mDias.add(new Dias(5,"VIERNES"));
        mDias.add(new Dias(6,"SABADO"));
        mDias.add(new Dias(0,"DOMINGO"));
    }

    @Override
    public void onClick(String idprestamo) {
        Uri u = Contract.PrestamoDetalle.crearUriPrestamoDetalle(idprestamo);

        Intent intent = new Intent(getActivity(), DetallePrestamo.class);
        intent.putExtra(Contract.Prestamo.ID, Contract.Prestamo.obtenerIdPrestamo(u));
        startActivityForResult(intent,500);
    }


    public class Dias {
        int num;
        String nombre;

        public Dias(int num,String nombre){
            this.num=num;
            this.nombre=nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }

    public void getData(int day){
        mDatos= new ArrayList<>();
        Uri uri = Uri.withAppendedPath(Contract.Cobrador.URI_CONTENIDO,"RUTA/"+String.valueOf(day));
        Cursor c = getActivity().getContentResolver().query(uri,null,null,null,null);
        for (c.moveToFirst(); !c.isAfterLast();c.moveToNext()){
            mDatos.add(new DatosCliente(c.getString(c.getColumnIndex(Contract.Prestamo.ID)),c.getString(c.getColumnIndex(Contract.Prestamo.PLAZO)),c.getString(c.getColumnIndex(Contract.Cliente.DIRECCION)),c.getString(c.getColumnIndex(Contract.Cliente.NOMBRE))));
        }
        mAdapter = new AdaptadorMiRuta(mDatos,getActivity(),this);
        mList.setAdapter(mAdapter);
        mCantidad.setText(String.valueOf(mDatos.size()));
    }


}
