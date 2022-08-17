package opnet.cyberdevs.whychat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnEnter;
    private TextView mTxtAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);

        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mBtnEnter = findViewById(R.id.btn_enter);
        mTxtAccount = findViewById(R.id.txt_account);

        mBtnEnter.setOnClickListener(v -> {
            String email = mEditEmail.getText().toString();
            String password = mEditPassword.getText().toString();

            Log.i("Teste", email);
            Log.i("Teste", password);

            if (email.isEmpty() || password == null || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Nome, senha e email devem ser preenchidos", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        Log.i("Teste", Objects.requireNonNull(task.getResult().getUser()).getUid());

                        Intent intent = new Intent(LoginActivity.this, MessagesActivity.class);

                        intent.setFlags(intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Log.i("Teste", e.getMessage()));
        });

        mTxtAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
