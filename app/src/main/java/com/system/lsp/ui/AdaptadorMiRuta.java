package com.system.lsp.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.system.lsp.R;
import com.system.lsp.modelo.DatosCliente;
import com.system.lsp.provider.Contract;

import java.util.ArrayList;

public class AdaptadorMiRuta extends RecyclerView.Adapter<AdaptadorMiRuta.Datos>{

    public ArrayList<DatosCliente> mItems;
    public Context mCtx;
    private AdaptadorMiRuta.OnItemClickListener escucha;

    public AdaptadorMiRuta(ArrayList<DatosCliente> mItems,Context mCtx,AdaptadorMiRuta.OnItemClickListener escucha){
        this.mItems=mItems;
        this.mCtx=mCtx;
        this.escucha= escucha;
    }

    public interface OnItemClickListener {
        public void onClick(String idprestamo);
    }

    @Override
    public Datos onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_ruta_item, parent, false);
        return new Datos(v);
    }

    @Override
    public void onBindViewHolder(Datos holder, int position) {
        final DatosCliente d = mItems.get(position);

        holder.mCodigoPrestamo.setText(d.getPRESTAMO());
        holder.mNombre.setText(d.getCLIENTE());
        holder.mDireccion.setText(d.getDIRECCION());
        holder.mPlazo.setText(d.getPLAZO());
        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escucha.onClick(d.getPRESTAMO());
            }
        });
        holder.mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escucha.onClick(d.getPRESTAMO());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mItems==null)?0:mItems.size();
    }

    public class Datos extends RecyclerView.ViewHolder{
        CardView mLayout;
        TextView mNombre,mDireccion,mPlazo,mCodigoPrestamo;
        ImageView mNext;
        public Datos(View itemView) {
            super(itemView);
            mLayout = (CardView)itemView.findViewById(R.id.Layout);
            mNombre =(TextView)itemView.findViewById(R.id.Nombre);
            mDireccion =(TextView)itemView.findViewById(R.id.Direccion);
            mPlazo = (TextView)itemView.findViewById(R.id.Plazo);
            mCodigoPrestamo = (TextView)itemView.findViewById(R.id.Codigo);
            mNext=(ImageView)itemView.findViewById(R.id.Next);

        }
    }
}
