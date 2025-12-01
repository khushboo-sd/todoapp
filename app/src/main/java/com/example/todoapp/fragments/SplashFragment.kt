package com.example.todoapp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todoapp.R
import com.google.firebase.auth.FirebaseAuth


class SplashFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        val isLogin = auth.currentUser != null

        Handler(Looper.getMainLooper()).postDelayed({

            // If fragment is detached, do nothing (avoid crash)
            if (!isAdded) return@postDelayed

            if (isLogin) {
                Toast.makeText(context, "Welcome Back!", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.action_splashFragment_to_homeFragment)
            } else {
                navController.navigate(R.id.action_splashFragment_to_signInFragment)
            }

        }, 2000)
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
    }
}
