package project.com.isly;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.v1tor.nuno.projectoiii.base.NFCActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import project.com.isly.R;
import project.com.isly.adapter.RecyclerAdapter;
import project.com.isly.adapter.RecyclerItemClickListener;
import project.com.isly.helpers.DBH;
import project.com.isly.helpers.NFCFragment;
import project.com.isly.models.Lists;
import project.com.isly.models.Student;

/**
 * Created by juan on 13/10/15.
 * Activity which is able to show the easiest way to take list
 */
public class Take extends NFCActivity  {
    RecyclerView rv;
    List<Student> items;
    private static final int REQUEST_ENABLE_BT = 1;
    RecyclerAdapter adapter;
    Switch entrySw;
    TextView text;
    String isExit = "N";
    private Toolbar toolbar;
    private com.github.clans.fab.FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take);

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(llm);
        items= new ArrayList<>();
        adapter=new RecyclerAdapter(items);
        rv.setAdapter(adapter);

        entrySw =(Switch)findViewById(R.id.EntrySwitch);
        text = (TextView)findViewById(R.id.EntrySwitchTxt);
        entrySw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    text.setText(R.string.exit);
                    isExit = "Y";
                }else{
                    text.setText(R.string.entry);
                    isExit = "N";
                }
            }
        });

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Take");
        toolbar.setTitleTextColor(Color.WHITE);
        final AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);

        rv.addOnItemTouchListener(new RecyclerItemClickListener(this, rv, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(View view, final int position) {
                final Student student = items.get(position);
                LayoutInflater layoutInflater= LayoutInflater.from(getApplicationContext());
                View prompt= layoutInflater.inflate(R.layout.dialog_change, null);

                alertDialog.setView(prompt);

                final EditText etKeyList=(EditText) prompt.findViewById(R.id.etKeyList);
                final TextView tvErrorList=(TextView) prompt.findViewById(R.id.tvErrorList);

                alertDialog
                        .setTitle(R.string.newList)
                        .setIcon(R.drawable.add_list)
                        .setCancelable(true)
                        .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    if(etKeyList.getText().toString().trim().equals("")){
                                        tvErrorList.setText(R.string.new_list_error);
                                    }else{
                                        items.get(position).setName(etKeyList.getText().toString());
                                        adapter.notifyItemChanged(position);
                                    }
                                }catch (Exception ex){
                                    Toast.makeText(getApplicationContext(),"System Error",Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alert=alertDialog.create();
                alert.show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                items.remove(position);
                adapter.notifyItemRemoved(position);
            }
        }));

        fab=(com.github.clans.fab.FloatingActionButton)findViewById(R.id.fabAdd0);

        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               for(Student student : items){
                   DBH.addNewStudent(getApplicationContext(),new Student(student.getName(),student.getName(), student.getMac()), isExit);

               }
            }
        });
    }


  /*  @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {}
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(), name;
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                if(device.getName()==null) name= "No available";
                else name= device.getName();
                items.add(new Student(name,name, device.getAddress()));
                adapter.notifyItemInserted(items.size());

                if(!(DBH.checkIfExists(getActivity().getApplicationContext(),new Student(name,name, device.getAddress())))){
                    DBH.addNewStudent(getActivity().getApplicationContext(),new Student(name,name, device.getAddress()));
                }
            }
        }
    };*/

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onTagDiscovered(Context context, Intent intent) {

            Tag myTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String id = bytesToHex(myTag.getId());
            String name = DBH.getName(getApplicationContext(), id);

            if (name.isEmpty()) name = "No available";
            items.add(new Student(name, name, id));
            adapter.notifyItemInserted(items.size());

    }

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
