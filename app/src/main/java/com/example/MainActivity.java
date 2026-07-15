package com.example;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.databinding.ActivityMainBinding;
import com.example.fragments.SplashFragment;
import com.example.viewmodels.HostelViewModel;

/**
 * Host Activity for the Hostel Management Application.
 * Manages fragment transitions and acts as the central state hub.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HostelViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HostelViewModel.class);

        // Load Initial Fragment
        if (savedInstanceState == null) {
            navigateTo(new SplashFragment(), false);
        }
    }

    /**
     * Swaps out current content frame with specified fragment.
     * @param fragment The new view fragment.
     * @param addToBackStack Add state to transaction backstack if true.
     */
    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public HostelViewModel getViewModel() {
        return viewModel;
    }

    public ActivityMainBinding getBinding() {
        return binding;
    }
}
