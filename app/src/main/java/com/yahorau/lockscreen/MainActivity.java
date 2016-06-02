package com.yahorau.lockscreen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class MainActivity extends Activity{

    public static final String PASSWORD = "Hello";
    private EditText editText;
    public static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setScreenMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);

        startService(new Intent(this, LockService.class));

        passwordCheck();

        setPhoneStateListener();

    }

    private void setPhoneStateListener() {
        StateListener phoneStateListener = new StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.dialog_text))
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Обработка кнопки Menu.
     * В версии Marshmallow кнопка не отображается.
     * В версии KitKat при нажатии на Menu вызывается диалог.
     * Это позволяет избежать сворачивания окна приложения.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_MENU)) {
            showDialog();
            return false;
        }
        return false;
    }

    /**
     * Метод срабатывает при нажатии кнопки Home.
     * В версии Marshmallow при нажатии на Home окно сворачивается,
     * после чего разворачивается и отображается диалог.
     * В манефесте:
     * <category android:name="android.intent.category.HOME"/>
     * <category android:name="android.intent.category.DEFAULT"/>
     * Это позволяет сразу разворачивать окно приложения.
     *
     *В версии KitKat приложение не разворачивается.
     */
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        showDialog();
    }

    /**
     * Установки типа экрана, полноэкранного режима, флагов,
     * позволяющих отображеть окно до KEYGUARD и включающие IMMERSIVE_STICKY мод.
     */
    public void setScreenMode() {
       Window window = this.getWindow();
       window.setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Считывается ввод поля editText. При совпадении с константой PASSWORD
     * устройство разблокируется.
     */
    private void passwordCheck() {
        View.OnFocusChangeListener focusChangeListener = new MyFocusChangeListener();
        editText.setOnFocusChangeListener(focusChangeListener);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().equals(PASSWORD)){
                        unlockScreen();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    public void unlockScreen() {
            this.finish();
    }

    /**
     * Прячет soft keyboard при отсутствии фокуса в поле ввода пароля
     */
    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if(v.getId() == R.id.editText && !hasFocus) {

                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }
    }

    /**
     * Обработка работы телефона в режиме вызова
     */
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    unlockScreen();
                    finish();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }
}
