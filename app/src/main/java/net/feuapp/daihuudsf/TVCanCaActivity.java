package net.feuapp.daihuudsf;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.feuapp.daihuudsf.crudmanager.crudLocal;
import net.feuapp.daihuudsf.customadapters.CustomAdapterTVCanCa;
import net.feuapp.daihuudsf.models.TVCanCa;
import net.feuapp.daihuudsf.models.WantDeleteFromServer;
import net.feuapp.daihuudsf.utils.MyContextWrapper;
import net.feuapp.daihuudsf.utils.NumberTextWatcher;
import net.feuapp.daihuudsf.utils.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.function.Predicate;

import static net.feuapp.daihuudsf.crudmanager.SyncCheck.LOGIN_NAME;
import static net.feuapp.daihuudsf.crudmanager.SyncCheck.WHO_START;
import static net.feuapp.daihuudsf.utils.utils.comPare;
import static net.feuapp.daihuudsf.utils.utils.doubleGet;
import static net.feuapp.daihuudsf.utils.utils.getCurrentDate;
import static net.feuapp.daihuudsf.utils.utils.getCurrentTimeMiliS;
import static net.feuapp.daihuudsf.utils.utils.getStringLeft;
import static net.feuapp.daihuudsf.utils.utils.hideSoftKeyboard;
import static net.feuapp.daihuudsf.utils.utils.intGet;
import static net.feuapp.daihuudsf.utils.utils.isBad;
import static net.feuapp.daihuudsf.utils.utils.longGet;

public class TVCanCaActivity extends AppCompatActivity {

    private GestureDetector mGestureDetector;
    private static final int SWIPE_MIN_DISTANCE = 250;
    private static final int SWIPE_MAX_OFF_PATH = 500;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;

    final private String TAG = getClass().getSimpleName();

    private int mShowMenuSave=0;
    private boolean needRefresh=false,daChia=false;
    private Calendar cal;
    private AutoCompleteTextView aedtThuyenVien, aedtTenHS;
    private EditText edtNgayps, edtNotes, edtSoLuong, edtDonGia, edtGiamGia;
    private TextView tvId, tvRkey, tvServerKey, tvRkeyThuyenVien, tvUserName;
    private ListView lvTVCanCa;
    private CheckBox chkAutoTV;
    private crudLocal crudLocaldb=crudLocal.getInstance(this);
    private ArrayList<TVCanCa> customadapterData=new ArrayList<>();
    private ArrayList<TVCanCa> arrTVCanCa = new ArrayList<>();
    private CustomAdapterTVCanCa customAdapter;
    private ArrayAdapter<String> adapterCB, adapterTV, adapterHS;
    private String[] ChuyenBien_listChuyenBien;
    private String[] DSTV_listThuyenVien;
    private ArrayList<String> DMHaiSan_listHaiSan=new ArrayList<>();

    private  int lvPossition=-1;
    private long intentRkeyChuyenBien, intentRkeyThuyenVien;
    private String edit_mode="VIEW";
    private String autoNgayps, autoNotes, autoTenTV, workingTenThuyenVien;
    private double autoGiamGia;


    //lam viec voi menu
    @Override
    public boolean onCreateOptionsMenu(Menu manu) {
        getMenuInflater().inflate(R.menu.record_menu, manu);
        MenuItem mSave = manu.findItem(R.id.save);
        if (daChia && !WHO_START.equals("admin")){
            for (int i = 0; i < manu.size(); i++){
                manu.getItem(i).setVisible(false);
            }
            return true;
        }
        if (mShowMenuSave==1){
            mSave.setVisible(true);
        }else{
            mSave.setVisible(false);
        }
        // return true so that the menu pop up is opened
        return true;
    }

    // Method này sử lý sự kiện khi MenuItem được chọn.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId)  {
            //ten cua cac id khi thiet ke cac resource menu
            case R.id.id_new :
                //wrtite ơn logic
                chkAutoTV.setChecked(false);
                CheckAddNew();
                return true;
            case R.id.id_edit :
                if (arrTVCanCa.size()==0){
                    return true;
                }
                if (!comPare(LOGIN_NAME,tvUserName.getText()+"") && !LOGIN_NAME.substring(0,5).equals("admin")){
                    Toast.makeText(TVCanCaActivity.this, "Dữ liệu được bảo vệ...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                CheckEdit();
                return true;
            case R.id.id_delete :
                //wrtite ơn logic
                if (arrTVCanCa.size()==0){
                    return true;
                }
                if (!TextUtils.equals(LOGIN_NAME,tvUserName.getText()+"") && !LOGIN_NAME.substring(0,5).equals("admin")){
                    Toast.makeText(TVCanCaActivity.this, "Dữ liệu được bảo vệ...", Toast.LENGTH_SHORT).show();
                    return true;
                }
                //lay ra index hien tai cua listview
                if (lvPossition==-1){
                    Toast.makeText(this, "Chưa chọn đúng dữ liệu cần xóa", Toast.LENGTH_SHORT).show();
                }else {
                    final TVCanCa tvcanca =arrTVCanCa.get(lvPossition);
                    // Hỏi trước khi xóa.
                    new AlertDialog.Builder(this)
                            .setTitle("Dai Huu DSF")
                            .setMessage(tvcanca.getTenhs()+ " | " + tvcanca.getSoluong() + "kg" + "\n\n" + "Có chắc xóa?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    CheckDelete(tvcanca);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
                return true;
            case R.id.save:
                SaveRecord();
                hideSoftKeyboard(TVCanCaActivity.this);
                return true;
            case R.id.id_exit:
                Intent ExitIntent=new Intent(getApplicationContext(),NavDrawerActivity.class);
                ExitIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ExitIntent.putExtra("EXIT", true);
                startActivity(ExitIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class CustomGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap");
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int x = (int) e.getX();
            int y = (int) e.getY();
            lvPossition=lvTVCanCa.pointToPosition( (int) x, (int) y );
            if (lvPossition<0){
                return true;
            }
            gotoRec(lvPossition);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress");
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll");
            return false;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            lvPossition=lvTVCanCa.pointToPosition( (int) e1.getX(), (int) e1.getY() );
            gotoRec(lvPossition);

            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "Right to Left swipe performed");
            }
            // left to right swipe
            else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                finish();
                Log.d(TAG, "Left to Right swipe performed");
            }

            // Down to Up swipe performed
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                Log.d(TAG, "Down to Up swipe performed");
            }
            // Up to Down swipe performed
            else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                
                Log.d(TAG, "Up to Down swipe performed");
            }

            return false;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvcanca);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CustomGestureDetector customGestureDetector = new CustomGestureDetector();
        // Create a GestureDetector
        mGestureDetector = new GestureDetector(this, customGestureDetector);
        // Attach listeners that'll be called for double-tap and related gestures
        mGestureDetector.setOnDoubleTapListener(customGestureDetector);

        addControls();
        addEvents();
        initialization();
    }


    private void addControls() {
        tvId=findViewById(R.id.tv_TVCanCaId);
        tvRkey=findViewById(R.id.tv_TVCanCaRkey);
        tvServerKey=findViewById(R.id.tv_TVCanCaServerKey);
        tvRkeyThuyenVien=findViewById(R.id.tv_TVCanCaRkeyTV);
        tvUserName=findViewById(R.id.tv_TVCanCaUserName);
        aedtThuyenVien=findViewById(R.id.aedt_TVCanCaTenTV);
        aedtTenHS=findViewById(R.id.aedt_TVCanCaTenHs);
        edtNotes=findViewById(R.id.edt_TVCanCaNotes);
        edtNgayps=findViewById(R.id.edt_TVCanCaNgayPS);
        edtSoLuong=findViewById(R.id.aedt_TVCanCaSoLuong);
        lvTVCanCa=findViewById(R.id.lv_TVCanCa);
        chkAutoTV=findViewById(R.id.chk_TVCanCaAutoTV);
        edtDonGia=findViewById(R.id.edt_TVCanCaDonGia);
        edtGiamGia=findViewById(R.id.aedt_TVCanCaGiamGia);

    }

    private void addEvents() {

        chkAutoTV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chkAutoTV.isChecked()){
                    if (edtNotes.getText()+""=="" || edtNgayps.getText()+""=="" || aedtThuyenVien.getText()+""=="" ){
                        chkAutoTV.setChecked(false);
                        return;
                    }
                    autoNotes=edtNotes.getText()+"";
                    autoNgayps=edtNgayps.getText()+"";
                    autoTenTV=aedtThuyenVien.getText()+"";
                    aedtThuyenVien.setFocusable(false);
                    autoGiamGia=doubleGet(edtGiamGia.getText()+"");
                    edtNotes.setFocusable(false);
                    edtGiamGia.setFocusable(false);
                    edtDonGia.setFocusable(false);
                }else{
                    aedtThuyenVien.setFocusable(true);
                    aedtThuyenVien.setFocusableInTouchMode(true);
                    edtNotes.setFocusable(true);
                    edtNotes.setFocusableInTouchMode(true);
                    edtDonGia.setFocusable(true);
                    edtDonGia.setFocusableInTouchMode(true);
                    edtGiamGia.setFocusable(true);
                    edtGiamGia.setFocusableInTouchMode(true);
                }
            }
        });

        edtNgayps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(edtNgayps);
            }
        });
        edtNgayps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                edtNgayps.setText("");
                return true;
            }
        });

        lvTVCanCa.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mGestureDetector.onTouchEvent(event);
                return mGestureDetector.onTouchEvent(event);
                // return true;
            }
        });

        aedtThuyenVien.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    // on focus off
                    String str = aedtThuyenVien.getText().toString();

                    ListAdapter listAdapter = aedtThuyenVien.getAdapter();
                    for(int i = 0; i < listAdapter.getCount(); i++) {
                        String temp = listAdapter.getItem(i).toString();
                        if(str.compareToIgnoreCase(temp) == 0) {
                            aedtThuyenVien.setText(temp);
                            intentRkeyThuyenVien=crudLocaldb.DSTV_getRkeyThuyenVien(temp,intentRkeyChuyenBien);
                            updateListTVCanCa();
                            setTitle("Chi tiết cân cá | "+temp);
                            return;
                        }
                    }

                    aedtThuyenVien.setText("");

                }else {
                    if (aedtThuyenVien.getText()+""==""){
                        customadapterData.clear();
                        customAdapter.notifyDataSetChanged();
                        String tenTau=getStringLeft(crudLocaldb.ChuyenBien_getTenChuyenBien(intentRkeyChuyenBien),"@");
                        setTitle("Chi tiết cân cá | "+workingTenThuyenVien);
                    }
                }
            }
        });
        edtDonGia.addTextChangedListener(new NumberTextWatcher(edtDonGia));
        aedtTenHS.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    // on focus off
                    String str = aedtTenHS.getText().toString();

                    ListAdapter listAdapter = aedtTenHS.getAdapter();
                    for(int i = 0; i < listAdapter.getCount(); i++) {
                        String temp = listAdapter.getItem(i).toString();
                        if(str.compareToIgnoreCase(temp) == 0) {
                            aedtTenHS.setText(temp);
                            edtDonGia.setText(crudLocaldb.DMHaiSan_getDgiaHaiSanByTen(temp));
                            return;
                        }
                    }

                    aedtTenHS.setText("");

                }

            }
        });

        edtSoLuong.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                   if (edtSoLuong.getText()+""!=""){
                       edtSoLuong.setSelectAllOnFocus(true);
                   }
                }

            }
        });
        edtGiamGia.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    if (edtGiamGia.getText()+""!=""){
                        edtGiamGia.setSelectAllOnFocus(true);
                    }
                }

            }
        });

        edtSoLuong.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (edit_mode!="VIEW"){
                        SaveRecord();
                        if (comPare(edit_mode,"NEW")){
                            CheckAddNew();
                        }
                    }
                }
                return false;
            }
        });

    }

    private void initialization() {

        Intent intent=getIntent();

        intentRkeyChuyenBien = intent.getLongExtra("rkeyChuyenBien",0);
        intentRkeyThuyenVien=intent.getLongExtra("rkeyThuyenVien",0);
        daChia=intent.getBooleanExtra("daChia",false);
        arrTVCanCa=crudLocaldb.TVCanCa_getTVCanCaByThuyenVien(intentRkeyThuyenVien);

        //set Adapter
        DSTV_listThuyenVien=crudLocaldb.DSTV_listThuyenVien(intentRkeyChuyenBien);
        adapterTV = new ArrayAdapter<String>(TVCanCaActivity.this, android.R.layout.simple_list_item_1, DSTV_listThuyenVien);
        aedtThuyenVien.setAdapter(adapterTV);

        DMHaiSan_listHaiSan=crudLocaldb.DMHaiSan_listHaiSan();
        adapterHS = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, DMHaiSan_listHaiSan);
        aedtTenHS.setAdapter(adapterHS);

        setAdapter();

        if ( arrTVCanCa.size()==0){
            //re quest make new record
            CheckAddNew();
            String tenTau=getStringLeft(crudLocaldb.ChuyenBien_getTenChuyenBien(intentRkeyChuyenBien),"@");
            setTitle("Chi tiết cân cá | "+tenTau);
        }else{
            gotoRec(arrTVCanCa.size()-1);
        }

    }

    private  void gotoRec(int lvPosition){
        if (lvPosition==-1){
            return;
        }
        if (arrTVCanCa.size()>0) { //phong truong hop null k co record nao
            TVCanCa tvcanca =arrTVCanCa.get(lvPosition);
            tvRkey.setText(tvcanca.getRkey()+"");
            tvId.setText(String.valueOf(tvcanca.getId()));
            tvServerKey.setText(tvcanca.getServerkey()+"");
            tvRkeyThuyenVien.setText(tvcanca.getRkeythuyenvien()+"");
            aedtThuyenVien.setText(crudLocaldb.DSTV_getTenThuyenVien(intentRkeyThuyenVien));
            aedtTenHS.setText(tvcanca.getTenhs());
            edtSoLuong.setText(tvcanca.getSoluong());
            edtNgayps.setText(tvcanca.getNgayps());
            edtNotes.setText(tvcanca.getNotes());
            edtDonGia.setText(tvcanca.getDongia());
            edtGiamGia.setText(tvcanca.getGiamgia());
            tvUserName.setText(tvcanca.getUsername()+"");
            workingTenThuyenVien=aedtThuyenVien.getText()+"";
            intentRkeyThuyenVien=crudLocaldb.DSTV_getRkeyThuyenVien(aedtThuyenVien.getText()+"",intentRkeyChuyenBien);
            setTitle("Chi tiết cân cá | "+workingTenThuyenVien);
            setEditMod(false);
        }
    }

    private void CheckAddNew(){
        if (edtNotes.getText()+""!=""){
            autoNotes=edtNotes.getText()+"";
        }
        tvRkey.setText("");
        tvId.setText("");
        tvServerKey.setText("");
        tvRkeyThuyenVien.setText("");
        aedtThuyenVien.setText("");
        edtDonGia.setText("");
        edtNgayps.setText(getCurrentDate());
        edtNotes.setText(autoNotes);
        aedtTenHS.setText("");
        edtSoLuong.setText("");
        tvUserName.setText(LOGIN_NAME);
        edit_mode="NEW";
        setEditMod(true);
        if (chkAutoTV.isChecked()) {
            aedtThuyenVien.setText(autoTenTV);
            edtNgayps.setText(autoNgayps);
            edtGiamGia.setText(autoGiamGia + "");
            aedtTenHS.requestFocus();
        }else{
            if (edtNotes.getText()+""==""){
                edtNotes.requestFocus();
            }else{
                aedtThuyenVien.requestFocus();
            }
        }
    }

    private void CheckEdit(){
        edit_mode="EDIT";
        chkAutoTV.setChecked(false);
        setEditMod(true);
    }

    private void CheckDelete(final TVCanCa tvcanca) {
        if (tvcanca.getServerkey()!=0){
            WantDeleteFromServer wdfs=new WantDeleteFromServer();
            wdfs.setmServerkey(tvcanca.getServerkey());
            wdfs.setmTablename("tvcanca");
            crudLocaldb.WDFS_addWDFS(wdfs);
        }

        crudLocaldb.TVCanCa_deleteTVCanCa(tvcanca.getRkey());
        //remove from array list
        Predicate<TVCanCa> personPredicate = p-> p.getId() == tvcanca.getId();
        arrTVCanCa.removeIf(personPredicate);
        //refesh screen
        tvRkey.setText("");
        tvId.setText("");
        tvServerKey.setText("");
        tvRkeyThuyenVien.setText("");
        aedtThuyenVien.setText("");
        edtSoLuong.setText("");
        edtDonGia.setText("");
        edtGiamGia.setText("");
        aedtTenHS.setText("");
        edtNgayps.setText("");
        edtNotes.setText("");
        tvUserName.setText(LOGIN_NAME);
        // Refresh ListView.
        this.needRefresh=true;
        if (arrTVCanCa.size()==0){
            String tenTau=getStringLeft(crudLocaldb.ChuyenBien_getTenChuyenBien(intentRkeyChuyenBien),"@");
            setTitle("Chi tiết cân cá | "+workingTenThuyenVien);
        }
        CapNhatHauCanh();
        updateListTVCanCa();

    }

    private void SaveRecord(){
        if (daChia){
            Toast.makeText(this, "Chuyến biển củ, dữ liệu chỉ được view", Toast.LENGTH_SHORT).show();
            return;
        }
        TVCanCa tvcanca=new TVCanCa();
        if(aedtThuyenVien.getText()+""=="" || doubleGet(edtSoLuong.getText()+"")==0 || aedtTenHS.getText()+""=="") {
            Toast.makeText(getApplicationContext(), "Cần nhập vào đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }
        workingTenThuyenVien=aedtThuyenVien.getText()+"";
        intentRkeyThuyenVien=crudLocaldb.DSTV_getRkeyThuyenVien(aedtThuyenVien.getText()+"",intentRkeyChuyenBien);

        if (intentRkeyChuyenBien==0 || intentRkeyThuyenVien==0){
            Toast.makeText(this, "Cần chính xác tên Chuyến biển và tên Thuyền viên.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(edtNgayps.getText()+""==""){
            edtNgayps.setText(getCurrentDate());
        }

        long dongia=longGet(edtDonGia.getText()+"");
        double soluong=doubleGet(edtSoLuong.getText()+"");
        double giamgia=doubleGet(edtGiamGia.getText()+"");
        long laygia=dongia-longGet(String.valueOf((dongia*giamgia)/100));
        double thanhtien=longGet(laygia+"")*doubleGet(soluong+"");

        tvcanca.setRkeythuyenvien(intentRkeyThuyenVien);
        tvcanca.setNgayps(edtNgayps.getText()+"");
        tvcanca.setNotes(edtNotes.getText()+"");
        tvcanca.setTenhs(aedtTenHS.getText()+"");
        tvcanca.setSoluong(String.valueOf(soluong));
        tvcanca.setDongia(String.valueOf(dongia));
        tvcanca.setGiamgia(String.valueOf(giamgia));
        tvcanca.setThanhtien(String.valueOf(thanhtien));
        tvcanca.setUpdatetime(getCurrentTimeMiliS());
        tvcanca.setUsername(LOGIN_NAME);
        if (edit_mode=="NEW" && longGet(tvRkey.getText()+"")==0) {
            //chuyenbien.set;);= new ChuyenBien(tenchuyenbien,tentau,ngaykhoihanh);
            tvcanca.setServerkey(0);
            tvcanca.setRkey(longGet(getCurrentTimeMiliS()));
            if (tvcanca != null) {
                long i = crudLocaldb.TVCanCa_addTVCanCa(tvcanca);
                if (i != -1) {
                    this.needRefresh=true;
                    setEditMod(false);
                    edit_mode="NEW";
                    CapNhatHauCanh();
                    updateListTVCanCa();
                }
            }
        }else{
            if (edit_mode=="EDIT") {
                tvcanca.setId(intGet(tvId.getText()+""));
                tvcanca.setRkey(longGet(tvRkey.getText()+""));
                tvcanca.setServerkey(intGet(tvServerKey.getText()+""));
                int result = crudLocaldb.TVCanCa_updateTVCanCa(tvcanca);
                if (result > 0) {
                    this.needRefresh=true;
                    setEditMod(false);
                    CapNhatHauCanh();
                    updateListTVCanCa();
                }
            }
        }
    }
    private void CapNhatHauCanh(){
        //Cap nhat DSTV
        String tiencanca=crudLocaldb.TVCanCa_SumThanhTienByRkeyTV(intentRkeyThuyenVien);
        crudLocaldb.DSTV_CapNhatTien(intentRkeyChuyenBien,workingTenThuyenVien,"0","0",tiencanca,getCurrentTimeMiliS(), LOGIN_NAME);
    }

    private void setAdapter() {
        if (customAdapter == null) {
            customadapterData.addAll(arrTVCanCa);
            // gan data source cho adapter
            customAdapter = new CustomAdapterTVCanCa(TVCanCaActivity.this, R.layout.customlist_tvcanca,customadapterData);
            //gan adapter cho spinner
            lvTVCanCa.setAdapter(customAdapter);
        }else{
            updateListTVCanCa();
            lvTVCanCa.setSelection(customAdapter.getCount()-1);
        }
    }
    //gett all to list
    public void updateListTVCanCa(){
        arrTVCanCa=crudLocaldb.TVCanCa_getTVCanCaByThuyenVien(intentRkeyThuyenVien);
        customadapterData.clear();
        customadapterData.addAll(arrTVCanCa);
        customAdapter.notifyDataSetChanged();
    }

    public void showDatePickerDialog(final EditText edtViewDate) {
        hideSoftKeyboard(TVCanCaActivity.this);
        DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year,
                                  int monthOfYear,
                                  int dayOfMonth) {
                //Khi nguoi dung da chon thi callback chay set ngay da chon vao edt
                String ngay, thang;
                if (String.valueOf(dayOfMonth).length()<2){
                    ngay="0"+String.valueOf(dayOfMonth);
                } else {
                    ngay=String.valueOf(dayOfMonth);
                }
                if (String.valueOf(monthOfYear + 1).length()<2){
                    thang="0"+String.valueOf(monthOfYear + 1);
                }else {
                    thang=String.valueOf(monthOfYear + 1);
                }
                edtViewDate.setText(ngay + "/" + thang + "/" + year);
            }
        };
        //Duoi day la hien len giao dien de chon ngay
        String s;
        if (!utils.isDate(utils.getEditText(this,edtViewDate))){
            //Dinh dang lai kieu ngay hien tai
            cal=Calendar.getInstance();
            SimpleDateFormat dft=new SimpleDateFormat("dd/MM/yyyy");
            //gan ngay thang hien tai da dc dinh dang cho s
            s=dft.format(cal.getTime());
        }else {
            s=utils.getEditText(this,edtViewDate);
        }
        String strArrtmp[] = s.split("/");
        int ngay = Integer.parseInt(strArrtmp[0]);
        int thang = Integer.parseInt(strArrtmp[1]) - 1;
        int nam = Integer.parseInt(strArrtmp[2]);
        DatePickerDialog pic = new DatePickerDialog(
                this,
                callback, nam, thang, ngay);
        pic.setTitle("Chọn ngày");
        pic.show();

    }

    private  void setEditMod(boolean chohaykhong){
        if (chohaykhong==true){
            mShowMenuSave=1;
            aedtThuyenVien.setThreshold(1);
            aedtTenHS.setThreshold(1);
        }else {
            mShowMenuSave=0;
            aedtThuyenVien.setThreshold(100);
            aedtTenHS.setThreshold(100);
            edtNgayps.setFocusable(false);
            edit_mode="VIEW";
        }
        invalidateOptionsMenu(); // now onCreateOptionsMenu(...) is called again
    }

    @Override
    public void finish() {
        // Chuẩn bị dữ liệu Intent.
        Intent data = new Intent();
        // Yêu cầu MainActivity refresh lại ListView hoặc không.
        data.putExtra("needRefresh", this.needRefresh);
        // Activity đã hoàn thành OK, trả về dữ liệu.
        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            super.attachBaseContext(MyContextWrapper.wrap(newBase, "en"));
        }
        else {
            super.attachBaseContext(newBase);
        }
    }
}
