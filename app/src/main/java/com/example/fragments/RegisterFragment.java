package com.example.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.MainActivity;
import com.example.R;
import com.example.databinding.FragmentRegisterBinding;
import com.example.models.Student;
import com.example.viewmodels.HostelViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Fragment governing student self-registration forms and validations.
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private HostelViewModel viewModel;
    private final Calendar calendar = Calendar.getInstance();
    private String selectedPhotoUri = "android.resource://com.example/drawable/ic_student";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof MainActivity) {
            viewModel = ((MainActivity) getActivity()).getViewModel();
        }

        // Back action trigger
        binding.btnRegBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // DOB selector setup
        binding.etDob.setOnClickListener(v -> showDatePicker());

        // Select Photo simulated setup
        binding.btnUploadPhoto.setOnClickListener(v -> {
            selectedPhotoUri = "android.resource://com.example/drawable/ic_student_attached";
            binding.imgStudentPhoto.setImageResource(R.drawable.ic_student);
            binding.imgStudentPhoto.setColorFilter(getResources().getColor(R.color.primary));
            Toast.makeText(requireContext(), "Profile Photo Attached Successfully", Toast.LENGTH_SHORT).show();
        });

        // Submit registration action
        binding.btnRegisterSubmit.setOnClickListener(v -> {
            processRegistration();
        });
    }

    private void showDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            binding.etDob.setText(sdf.format(calendar.getTime()));
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void processRegistration() {
        // Retrieve values
        String fullName = binding.etFullName.getText().toString().trim();
        String fatherName = binding.etFatherName.getText().toString().trim();
        String motherName = binding.etMotherName.getText().toString().trim();
        String gender = binding.etGender.getText().toString().trim();
        String blood = binding.etBloodGroup.getText().toString().trim();
        String dob = binding.etDob.getText().toString().trim();

        String cnic = binding.etCnic.getText().toString().trim();
        String passport = binding.etPassport.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String emergency = binding.etEmergency.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();

        String regNum = binding.etRegNumber.getText().toString().trim();
        String rollNum = binding.etRollNumber.getText().toString().trim();
        String dept = binding.etDepartment.getText().toString().trim();
        String program = binding.etProgram.getText().toString().trim();
        String semester = binding.etSemester.getText().toString().trim();

        String gName = binding.etGuardianName.getText().toString().trim();
        String gPhone = binding.etGuardianContact.getText().toString().trim();

        String pass = binding.etRegPassword.getText().toString().trim();
        String confPass = binding.etConfirmPassword.getText().toString().trim();
        String sQuest = binding.etSecurityQuestion.getText().toString().trim();
        String sAns = binding.etSecurityAnswer.getText().toString().trim();

        // VALIDATIONS
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError("Full Name is required");
            binding.etFullName.requestFocus();
            return;
        } else {
            binding.tilFullName.setError(null);
        }

        if (TextUtils.isEmpty(cnic)) {
            binding.tilCnic.setError("CNIC Number is required");
            binding.etCnic.requestFocus();
            return;
        } else {
            binding.tilCnic.setError(null);
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            binding.etEmail.requestFocus();
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError("Phone number is required");
            binding.etPhone.requestFocus();
            return;
        } else {
            binding.tilPhone.setError(null);
        }

        if (TextUtils.isEmpty(emergency)) {
            binding.tilEmergency.setError("Emergency phone number is required");
            binding.etEmergency.requestFocus();
            return;
        } else {
            binding.tilEmergency.setError(null);
        }

        if (TextUtils.isEmpty(regNum)) {
            binding.tilRegNumber.setError("Registration Number is required");
            binding.etRegNumber.requestFocus();
            return;
        } else {
            binding.tilRegNumber.setError(null);
        }

        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            binding.tilRegPassword.setError("Password must be at least 6 characters");
            binding.etRegPassword.requestFocus();
            return;
        } else {
            binding.tilRegPassword.setError(null);
        }

        if (!pass.equals(confPass)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            binding.etConfirmPassword.requestFocus();
            return;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        if (TextUtils.isEmpty(sAns)) {
            binding.tilSecurityAnswer.setError("Security answer is required");
            binding.etSecurityAnswer.requestFocus();
            return;
        } else {
            binding.tilSecurityAnswer.setError(null);
        }

        // CREATE STUDENT POJO
        Student s = new Student();
        s.setPhotoUri(selectedPhotoUri);
        s.setFullName(fullName);
        s.setFatherName(fatherName);
        s.setMotherName(motherName);
        s.setGender(gender);
        s.setBloodGroup(blood);
        s.setDob(dob);
        s.setCnic(cnic);
        s.setPassport(passport);
        s.setEmail(email);
        s.setPhone(phone);
        s.setEmergencyContact(emergency);
        s.setAddress(address);
        s.setCity(city);
        s.setRegNumber(regNum);
        s.setRollNumber(rollNum);
        s.setDepartment(dept);
        s.setProgram(program);
        s.setSemester(semester);
        s.setGuardianName(gName);
        s.setGuardianContact(gPhone);
        s.setPassword(pass);
        s.setSecurityQuestion(sQuest);
        s.setSecurityAnswer(sAns);
        s.setStatus("PENDING_ALLOCATION");

        // Save into repository via ViewModel
        if (viewModel != null) {
            boolean success = viewModel.registerStudent(s);
            if (success) {
                Toast.makeText(requireContext(), "Registration Form Submitted Successfully!", Toast.LENGTH_LONG).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            } else {
                Snackbar.make(binding.getRoot(), "Registration Failed. Registration Number may already exist.", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getResources().getColor(R.color.error))
                        .show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
