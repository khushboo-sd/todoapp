package com.example.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth




class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navControl: NavController
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerEvents()
    }

    private fun init(view: View) {
        navControl = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private fun registerEvents() {

        // Go to Sign In screen
        binding.authTextView.setOnClickListener {
            navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
        }

        // Sign-Up Logic
        binding.nextBtn.setOnClickListener {

            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passEt.text.toString().trim()
            val verifypass = binding.verifyPassEt.text.toString().trim()

            // Check if fields are empty
            if (email.isEmpty() || pass.isEmpty() || verifypass.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (pass != verifypass) {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create account
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener {

                    if (it.isSuccessful) {
                        Toast.makeText(context, "Registered Successfully", Toast.LENGTH_SHORT).show()

                        navControl.navigate(R.id.action_signUpFragment_to_homeFragment)
                    } else {
                        Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
