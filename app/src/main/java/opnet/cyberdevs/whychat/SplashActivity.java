package opnet.cyberdevs.whychat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

import opnet.cyberdevs.whychat.java.LoginActivity;
import opnet.cyberdevs.whychat.java.MessagesActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_splash);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mostrarMainActivity();
            }
        }, 5000);
    }

    private void verifyAuthentication() {
        if (FirebaseAuth.getInstance().getUid() == null) {
            Intent intent = new Intent(SplashActivity.this, MessagesActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
    }

    private void mostrarMainActivity() {
        Intent intent = new Intent(
                SplashActivity.this,MessagesActivity.class
        );
        startActivity(intent);
        finish();
    }
}