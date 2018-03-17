package com.elite.mychat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by evk29 on 28-01-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Messages> messages_list;
    private FirebaseUser current_user;
    private String sender_name, sender_thumb_image, receiver_name, receiver_thumb_image;
    private DatabaseReference users_db_ref;
    private String current_user_id, from_user_id, uid, type;
    private Context context;
    private ViewGroup group;

    public MessageAdapter(List<Messages> messages_list, final String uid) {
        this.messages_list = messages_list;
        this.uid = uid;

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        current_user_id = current_user.getUid();

        users_db_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        users_db_ref.keepSynced(true);

        users_db_ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        sender_name = dataSnapshot.child(current_user_id).child("name").getValue().toString();
                        sender_thumb_image = dataSnapshot.child(current_user_id).child("thumb_image").getValue().toString();
                        receiver_name = dataSnapshot.child(uid).child("name").getValue().toString();
                        receiver_thumb_image = dataSnapshot.child(uid).child("thumb_image").getValue().toString();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    public int getItemViewType(int position) {
        if (messages_list.get(position).getFrom().equals(current_user_id))
            return 1;
        else
            return 2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.message_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            group = parent;
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.message_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tv_message;
        ImageView iv_message;
        RelativeLayout sender;

        public SenderViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            sender = itemView.findViewById(R.id.sender);

            tv_message = itemView.findViewById(R.id.tv_message);
            iv_message = itemView.findViewById(R.id.iv_message);
        }
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        View view;
        public TextView tv_message_aliter_text, tv_message_aliter_time;
        public ImageView iv_message_aliter, iv_message_receiver;
        RelativeLayout receiver;

        public ReceiverViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            receiver = itemView.findViewById(R.id.receiver);

            tv_message_aliter_text = itemView.findViewById(R.id.tv_message_aliter_text);
            iv_message_aliter = itemView.findViewById(R.id.iv_message_aliter);
            iv_message_receiver = itemView.findViewById(R.id.iv_message_receiver);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Messages message = messages_list.get(position);
        from_user_id = message.getFrom();
        type = message.getType();


        if (current_user_id.equals(from_user_id)) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((SenderViewHolder) holder).sender.getLayoutParams();
            context = ((SenderViewHolder) holder).view.getContext();
            if (type.equals("image")) {
                ((SenderViewHolder) holder).tv_message.setVisibility(View.GONE);
                ((SenderViewHolder) holder).iv_message.setVisibility(View.VISIBLE);
                params.height = (int) (140.625 * context.getResources().getDisplayMetrics().density);//250 * 140.625 is 16:9 ratio
                ((SenderViewHolder) holder).sender.setLayoutParams(params);
                Glide.with(context).load(message.getMessage())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_user)
                        .into(((SenderViewHolder) holder).iv_message);
            } else {
                ((SenderViewHolder) holder).tv_message.setVisibility(View.VISIBLE);
                ((SenderViewHolder) holder).iv_message.setVisibility(View.GONE);
                params.height = params.WRAP_CONTENT;
                ((SenderViewHolder) holder).sender.setLayoutParams(params);
                ((SenderViewHolder) holder).tv_message.setText(message.getMessage());
            }

        } else {

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ((ReceiverViewHolder) holder).receiver.getLayoutParams();
            context = ((ReceiverViewHolder) holder).view.getContext();

            if (context != null && group.getContext() != null)//Profile pic
                Glide.with(context).load(receiver_thumb_image)
                        .thumbnail(0.5f)
                        .crossFade()
                        .bitmapTransform(new GlideCircleTransformation(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_user)
                        .into(((ReceiverViewHolder) holder).iv_message_aliter);

            if (type.equals("image")) {
                ((ReceiverViewHolder) holder).tv_message_aliter_text.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).iv_message_receiver.setVisibility(View.VISIBLE);

                params.height = (int) (140.625 * context.getResources().getDisplayMetrics().density);//250 * 140.625 is 16:9 ratio
                ((ReceiverViewHolder) holder).receiver.setLayoutParams(params);

                Glide.with(context).load(message.getMessage())
                        .thumbnail(0.5f)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_user)
                        .into(((ReceiverViewHolder) holder).iv_message_receiver);

            } else {

                ((ReceiverViewHolder) holder).tv_message_aliter_text.setVisibility(View.VISIBLE);
                ((ReceiverViewHolder) holder).iv_message_receiver.setVisibility(View.GONE);

                params.height = params.WRAP_CONTENT;
                ((ReceiverViewHolder) holder).receiver.setLayoutParams(params);

                ((ReceiverViewHolder) holder).tv_message_aliter_text.setText(message.getMessage());

            }
        }

    }

    @Override
    public int getItemCount() {
        return messages_list.size();
    }

}
