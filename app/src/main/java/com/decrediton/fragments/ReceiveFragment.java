package com.decrediton.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.decrediton.R;
import com.decrediton.Util.AccountResponse;
import com.decrediton.Util.DcrResponse;
import com.decrediton.Util.EncodeQrCode;
import com.decrediton.Util.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dcrwallet.Dcrwallet;

/**
 * Created by Macsleven on 28/11/2017.
 */

public class ReceiveFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener{
    ImageView imageView;
    private TextView address;
    ProgressDialog pd;
    ArrayAdapter dataAdapter;
    List<String> categories;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        return inflater.inflate(R.layout.content_receive, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageView = view.findViewById(R.id.bitm);
        address = view.findViewById(R.id.barcode_address);
        Button buttonGenerate = view.findViewById(R.id.btn_gen_new_addr);
        Spinner accountSpinner = view.findViewById(R.id.recieve_dropdown);
        accountSpinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        categories = new ArrayList<>();

        dataAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        accountSpinner.setAdapter(dataAdapter);

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyToClipboard(address.getText().toString());
            }
        });
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Receive");
        prepareAccounts();
    }

    public void copyToClipboard(String copyText) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(copyText);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("Your address", copyText);
            clipboard.setPrimaryClip(clip);
        }
        Toast toast = Toast.makeText(getContext(),
                "Your address is copied", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 50, 50);
        toast.show();
    }

    private void prepareAccounts(){
        pd = Utils.getProgressDialog(ReceiveFragment.this.getContext(), false,false,"Getting Accounts...");
        pd.show();
        new Thread(){
            public void run(){
                try{
                    final AccountResponse response = AccountResponse.parse(Dcrwallet.getAccounts());
                    if(response.errorOccurred){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(pd.isShowing()){
                                    pd.dismiss();
                                }
                                Toast.makeText(ReceiveFragment.this.getContext(),response.errorMessage,Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    categories.clear();
                    for(int i = 0; i < response.items.size(); i++){
                        if(response.items.get(i).name.trim().equals("imported")){
                            continue;
                        }
                        categories.add(i, response.items.get(i).name);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(pd.isShowing()){
                                pd.dismiss();
                            }
                            dataAdapter.notifyDataSetChanged();
                            //Default Account
                            //getAddress(0);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void getAddress(final int accountNumber){
        pd = Utils.getProgressDialog(ReceiveFragment.this.getContext(), false,false,"Getting Accounts...");
        pd.show();
        new Thread(){
            public void run(){
                try {
                    final DcrResponse response = DcrResponse.parse(Dcrwallet.nextAddress((long) accountNumber));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(response.errorOccurred){
                                Toast.makeText(ReceiveFragment.this.getContext(),"Error occurred while trying to get address for account: "+accountNumber,Toast.LENGTH_SHORT).show();
                            }else{
                                String newAddress = response.content;
                                address.setText(newAddress);
                                imageView.setImageBitmap(EncodeQrCode.encodeToQrCode(newAddress,200,200));
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String item = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}