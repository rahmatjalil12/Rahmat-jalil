package com.example.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.MainActivity;
import com.example.R;
import com.example.databinding.FragmentLoginBinding;
import com.example.viewmodels.HostelViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * Fragment managing secure account authentication for both Admins and Students.
 */
public class LoginFragment extends Fragment {

    private static final String ARG_IS_ADMIN = "is_admin_mode";
    private FragmentLoginBinding binding;
    private HostelViewModel viewModel;
    private boolean isAdminMode = false;

    public static LoginFragment newInstance(boolean isAdmin) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdminMode = getArguments().getBoolean(ARG_IS_ADMIN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        // Setup Back Button
        binding.btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Setup default view based on creation arguments
        updateUiMode(isAdminMode);

        // Switch to Student UI Mode on toggle
        binding.btnToggleStudent.setOnClickListener(v -> {
            updateUiMode(false);
        });

        // Switch to Admin UI Mode on toggle
        binding.btnToggleAdmin.setOnClickListener(v -> {
            updateUiMode(true);
        });

        // Registration Link trigger
        binding.tvRegisterLink.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateTo(new RegisterFragment(), true);
            }
        });

        // Submit button authentication trigger
        binding.btnLoginSubmit.setOnClickListener(v -> {
            performLogin();
        });

        // Forgot password workflow
        binding.tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });
    }

    private void updateUiMode(boolean admin) {
        this.isAdminMode = admin;
        if (admin) {
            binding.tilIdentifier.setHint("Administrator Username");
            binding.tilIdentifier.setStartIconDrawable(requireContext().getDrawable(R.drawable.ic_settings));
            binding.layoutRegisterPrompt.setVisibility(View.GONE);
            binding.tvForgotPassword.setVisibility(View.GONE);

            // Style buttons to show selected state
            binding.btnToggleAdmin.setBackgroundColor(getResources().getColor(R.color.primary));
            binding.btnToggleAdmin.setTextColor(getResources().getColor(R.color.white));
            binding.btnToggleStudent.setBackgroundColor(getResources().getColor(R.color.grey_100));
            binding.btnToggleStudent.setTextColor(getResources().getColor(R.color.primary));
        } else {
            binding.tilIdentifier.setHint("Student Registration Number");
            binding.tilIdentifier.setStartIconDrawable(requireContext().getDrawable(R.drawable.ic_student));
            binding.layoutRegisterPrompt.setVisibility(View.VISIBLE);
            binding.tvForgotPassword.setVisibility(View.VISIBLE);

            // Style buttons to show selected state
            binding.btnToggleStudent.setBackgroundColor(getResources().getColor(R.color.primary));
            binding.btnToggleStudent.setTextColor(getResources().getColor(R.color.white));
            binding.btnToggleAdmin.setBackgroundColor(getResources().getColor(R.color.grey_100));
            binding.btnToggleAdmin.setTextColor(getResources().getColor(R.color.primary));
        }
    }

    private void performLogin() {
        String identifier = binding.etIdentifier.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(identifier)) {
            binding.tilIdentifier.setError("Field cannot be empty");
            return;
        } else {
            binding.tilIdentifier.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password cannot be empty");
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        if (viewModel == null) return;

        if (isAdminMode) {
            boolean success = viewModel.adminLogin(identifier, password);
            if (success) {
                Snackbar.make(binding.getRoot(), "Admin Authentication Successful", Snackbar.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateTo(new AdminDashboardFragment(), false);
                }
            } else {
                Snackbar.make(binding.getRoot(), "Invalid administrator credentials", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.error))
                        .show();
            }
        } else {
            boolean success = viewModel.studentLogin(identifier, password);
            if (success) {
                Snackbar.make(binding.getRoot(), "Resident Authentication Successful", Snackbar.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateTo(new StudentDashboardFragment(), false);
                }
            } else {
                Snackbar.make(binding.getRoot(), "Invalid registration number or password", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.error))
                        .show();
            }
        }
    }

    private void showForgotPasswordDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etReg = new EditText(requireContext());
        etReg.setHint("Registration Number");
        etReg.setPadding(20, 30, 20, 30);
        layout.addView(etReg);

        TextView tvQ = new TextView(requireContext());
        tvQ.setText("Security Question: What is your primary school name?");
        tvQ.setPadding(20, 20, 20, 10);
        layout.addView(tvQ);

        final EditText etAns = new EditText(requireContext());
        etAns.setHint("Security Answer");
        etAns.setPadding(20, 30, 20, 30);
        layout.addView(etAns);

        final EditText etNewPass = new EditText(requireContext());
        etNewPass.setHint("Enter New Password");
        etNewPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        etNewPass.setPadding(20, 30, 20, 30);
        layout.addView(etNewPass);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Account Password")
                .setView(layout)
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    String reg = etReg.getText().toString().trim();
                    String ans = etAns.getText().toString().trim();
                    String newPass = etNewPass.getText().toString().trim();

                    if (TextUtils.isEmpty(reg) || TextUtils.isEmpty(ans) || TextUtils.isEmpty(newPass)) {
                        Toast.makeText(requireContext(), "All fields are required to reset", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean ok = viewModel.resetStudentPassword(reg, "What is your primary school name?", ans, newPass);
                    if (ok) {
                        Toast.makeText(requireContext(), "Password reset successfully! Log in now.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Security answers matched incorrectly.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
