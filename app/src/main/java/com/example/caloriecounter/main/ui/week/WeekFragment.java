package com.example.caloriecounter.main.ui.week;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.caloriecounter.R;

public class WeekFragment extends Fragment {

    private WeekViewModel weekViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        weekViewModel =
                new ViewModelProvider(this).get(WeekViewModel.class);
        View root = inflater.inflate(R.layout.fragment_week, container, false);
        final TextView textView = root.findViewById(R.id.text_slideshow);
        weekViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}