package com.polarbearr.todo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import static com.polarbearr.todo.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.MainActivity.TODO_KEY;
import static com.polarbearr.todo.MainActivity.TODO_WRITE_REQUEST_CODE;
import static com.polarbearr.todo.WriteActivity.DATABASE_FLAG_KEY;

public class ListFragment extends Fragment {
    static final String TITLE_KEY = "titlekey";
    static final String CONTENT_KEY = "contentkey";
    static final String ID_KEY = "idkey";
    static final String ITEM_COUNT_KEY = "itemkey";

    private Bundle loadedData;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_list, container, false);

        // 리사이클러뷰 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);

        // 플로팅버튼 설정
        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WriteActivity.class);
                startActivityForResult(intent, TODO_WRITE_REQUEST_CODE);
            }
        });
        DisplayMetrics metrics = getMetrics(getContext());
        setButtonPosition(metrics, fab);

        // 프래그먼트 구분해서 데이터 설정
        Bundle bundle = getArguments();
        if(bundle != null) processBundle(bundle);

        // 리사이클러뷰에 어댑터 설정
        setTodoAdapter();

        return rootView;
    }

    public void processBundle(final Bundle bundle){
        int fragmentType = bundle.getInt(TODO_KEY);

        switch(fragmentType){
            case 0:
                // 할 일 데이터베이스에서 불러오기
                loadedData = DatabaseHelper.selectData(DatabaseHelper.TODO_TABLE);
                break;
            case 1:
                loadedData = DatabaseHelper.selectData(DatabaseHelper.COMPLETED_TABLE);
                fab.setVisibility(View.INVISIBLE);
                // 완료한 일 데이터베이스에서 불러오기
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//                            loadedData = DatabaseHelper.selectData(DatabaseHelper.COMPLETED_TABLE);
//                        } catch(Exception e){}
//                    }
//                }).start();
                break;
        }
    }

    public void setTodoAdapter(){
        final TodoAdapter adapter = new TodoAdapter(getContext());
        Bundle itemBundle;
        TodoItem item;
        if(loadedData != null) {
            for (int i = 0; i < loadedData.size(); i++) {
                itemBundle = loadedData.getBundle(TODO_ITEM + i);

                String title = itemBundle.getString(TITLE_KEY);
                String content = itemBundle.getString(CONTENT_KEY);
                int id = itemBundle.getInt(ID_KEY);

                item = new TodoItem(title, content, id);
                adapter.addItem(item);
            }

            adapter.setOnItemClickListener(new TodoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(TodoAdapter.ViewHolder holder, View view, int position) {
                    TodoItem item = adapter.getItem(position);
                    String title = item.getTitle();
                    String content = item.getContent();
                    int id = item.getId();
                    int count = adapter.getItemCount();

                    Intent intent = new Intent(getContext().getApplicationContext(), WriteActivity.class);
                    intent.putExtra(TITLE_KEY, title);
                    intent.putExtra(CONTENT_KEY, content);
                    intent.putExtra(ID_KEY, id);
//                    intent.putExtra(ITEM_COUNT_KEY, count);  저장 후 writeactivity 종료안할거면 전달

                    startActivityForResult(intent, TODO_WRITE_REQUEST_CODE);
                }
            });

            recyclerView.setAdapter(adapter);
        }
    }

    // 화면 크기 얻기
    public DisplayMetrics getMetrics(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    // 버튼 위치 설정
    public static void setButtonPosition(DisplayMetrics metrics, View view){
        view.setX(metrics.widthPixels * 8 / 10);
        view.setY(metrics.heightPixels * 8 / 10);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 리사이클러뷰 목록 업데이트
        if(data != null) {
            boolean databaseChangeFlag = data.getBooleanExtra(DATABASE_FLAG_KEY, false);
            if(databaseChangeFlag == true) {
                loadedData = DatabaseHelper.selectData(DatabaseHelper.TODO_TABLE);
                setTodoAdapter();
//                Toast.makeText(getContext(), "리사이클러뷰 업데이트함", Toast.LENGTH_SHORT).show();
            } else ;
//                Toast.makeText(getContext(), "리사이클러뷰 업데이트 안함", Toast.LENGTH_SHORT).show();
        }
    }
}
