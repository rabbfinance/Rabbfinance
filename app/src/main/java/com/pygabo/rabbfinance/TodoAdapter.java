package com.pygabo.rabbfinance;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pygabo.rabbfinance.utils.ClickHandler;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.MyViewHolder> {

    private List<TodoItem> todoList;

    public ClickHandler clickHandler;


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView id, todo, ready;
        public Button setStatus;
        public ClickHandler clickHandler;

        public MyViewHolder(View view) {
            super(view);
            id = (TextView) view.findViewById(R.id.id);
            todo = (TextView) view.findViewById(R.id.todo);
            ready = (TextView) view.findViewById(R.id.ready);

            setStatus = (Button) view.findViewById(R.id.mark_ready);
            setStatus.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (clickHandler != null) {
                clickHandler.onMyButtonClicked(getAdapterPosition());
            }
        }
    }


    public TodoAdapter(List<TodoItem> todoList, ClickHandler handler) {
        this.todoList = todoList;
        this.clickHandler = handler;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final TodoItem todo = todoList.get(position);
        holder.clickHandler = this.clickHandler;
        holder.todo.setText(todo.getDescription());
        holder.id.setText(todo.getId()+"");

        if(todo.isReady()){
            holder.ready.setText("Ready");
            holder.ready.setTextColor(Color.GREEN);
            holder.setStatus.setVisibility(View.INVISIBLE);
        } else {
            holder.ready.setText("Not ready yet");
            holder.ready.setTextColor(Color.RED);
            holder.setStatus.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void setFilter(List<TodoItem> productoModels) {
        todoList = new ArrayList<>();
        todoList.addAll(productoModels);
        notifyDataSetChanged();
    }

    public TodoItem getItem(int position) {
        return todoList.get(position);
    }
}