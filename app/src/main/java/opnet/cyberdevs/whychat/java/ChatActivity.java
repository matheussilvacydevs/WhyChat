package opnet.cyberdevs.whychat.java;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.List;

import opnet.cyberdevs.whychat.R;
import opnet.cyberdevs.whychat.java.fragments.Contact;
import opnet.cyberdevs.whychat.java.fragments.Message;
import opnet.cyberdevs.whychat.java.fragments.User;

public class ChatActivity extends AppCompatActivity {

    private GroupAdapter adapter;
    private User user;
    private User me;

    private EditText editChat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        user = getIntent().getExtras().getParcelable("user");
        getSupportActionBar().setTitle(user.getUsername());

        RecyclerView rv = findViewById(R.id.recycler_chat);
        editChat = findViewById(R.id.edit_chat);
        Button btnChat = findViewById(R.id.btn_chat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        adapter = new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("/users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                        fetchMessage();
                    }
                });

    }

    private void fetchMessage() {
        if (me != null) {

            String fromId = me.getUuid();
            String toId = user.getUuid();

            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(fromId)
                    .collection(toId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                            if (documentChanges != null) {
                                for (DocumentChange doc: documentChanges) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                        Message message = doc.getDocument().toObject(Message.class);
                                        adapter.add(new MessageItem(message));

                                    }
                                }

                            }
                        }
                    });
        }
    }

    private void sendMessage() {
        String text = editChat.getText().toString();

        editChat.setText(null);

        String fromId = FirebaseAuth.getInstance().getUid();
        String toId = user.getUuid();
        long timestamp = System.currentTimeMillis();

        Message message = new Message();
        message.setFromId(fromId);
        message.setToId(toId);
        message.setTimestamp(timestamp);
        message.setText(text);

        if (!message.getText().isEmpty()) {
            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(fromId)
                    .collection(toId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());

                            Contact contact = new Contact();
                            contact.setUuid(toId);
                            contact.setUsername(user.getUsername());
                            contact.setPhotoUrl(user.getProfileUrl());
                            contact.setTimestamp(message.getTimestamp());
                            contact.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(fromId)
                                    .collection("contacts")
                                    .document(toId)
                                    .set(contact);
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });

            FirebaseFirestore.getInstance().collection("/conversations")
                    .document(toId)
                    .collection(fromId)
                    .add(message)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("Teste", documentReference.getId());

                            Contact contact = new Contact();
                            contact.setUuid(toId);
                            contact.setUsername(user.getUsername());
                            contact.setPhotoUrl(user.getProfileUrl());
                            contact.setTimestamp(message.getTimestamp());
                            contact.setLastMessage(message.getText());

                            FirebaseFirestore.getInstance().collection("/last-messages")
                                    .document(toId)
                                    .collection("contacts")
                                    .document(fromId)
                                    .set(contact);
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Teste", e.getMessage(), e);
                        }
                    });

        }
    }

    private class MessageItem extends Item<GroupieViewHolder> {

        private final Message message;

        private MessageItem(Message message) {this.message = message;
        }


        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView txtMsg = viewHolder.itemView.findViewById(R.id.txt_msg);
            ImageView imgMessage = viewHolder.itemView.findViewById(R.id.img_message_user);

            txtMsg.setText(message.getText());
            Picasso.get()
                    .load(user.getProfileUrl())
                    .into(imgMessage);
        }

        @Override
        public int getLayout() {
            return message.getFromId().equals (FirebaseAuth.getInstance().getUid())
                    ? R.layout.item_to_message
                    : R.layout.item_from_message;
        }
    }
}