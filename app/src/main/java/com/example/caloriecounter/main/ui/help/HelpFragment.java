package com.example.caloriecounter.main.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.caloriecounter.R;
import com.example.caloriecounter.main.ui.help.HelpViewModel;

public class HelpFragment extends Fragment {

    private HelpViewModel helpViewModel;
    private WebView myWebView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        helpViewModel = new ViewModelProvider(this).get(HelpViewModel.class);
        View root = inflater.inflate(R.layout.fragment_help, container, false);
        myWebView = root.findViewById(R.id.my_site);
        
        myWebView.loadUrl("https://phillipnavarrete.weebly.com");

        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new WebViewClient());

        return root;
    }
}