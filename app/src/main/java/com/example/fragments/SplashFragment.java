package com.example.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.MainActivity;
import com.example.R;
import com.example.databinding.FragmentSplashBinding;

/**
 * Fragment displaying the welcome screen and role selector.
 */
public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide toolbar if present on splash
        if (getActivity() instanceof MainActivity) {
            MainActivity main = (MainActivity) getActivity();
            if (main.getBinding().appBarLayout != null) {
                main.getBinding().appBarLayout.setVisibility(View.GONE);
            }
            if (main.getBinding().bottomNavigation != null) {
                main.getBinding().bottomNavigation.setVisibility(View.GONE);
            }
        }

        // Action on Admin selection
        binding.btnAdminPortal.setOnClickListener(v -> {
            LoginFragment login = LoginFragment.newInstance(true);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(login, true);
            }
        });

        // Action on Student selection
        binding.btnStudentPortal.setOnClickListener(v -> {
            LoginFragment login = LoginFragment.newInstance(false);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(login, true);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
