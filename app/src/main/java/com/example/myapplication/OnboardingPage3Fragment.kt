package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class OnboardingPage3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.onboarding_page_3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val finishButton = view.findViewById<Button>(R.id.finish_button)
        val nameInputLayout = view.findViewById<TextInputLayout>(R.id.name_input_layout)
        val nameEditText = view.findViewById<TextInputEditText>(R.id.name_edit_text)

        finishButton.setOnClickListener {
            // 1. Pega o nome digitado e remove espaços em branco extras
            val userName = nameEditText.text.toString().trim()

            // 2. Valida se o nome não está vazio
            if (userName.isEmpty()) {
                // Mostra uma mensagem de erro no campo de texto
                nameInputLayout.error = "Por favor, digite seu nome."
                return@setOnClickListener
            }


            nameInputLayout.error = null


            val sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("onboarding_completed", true)
                putString("user_name", userName)
                apply()
            }


            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)


            requireActivity().finish()
        }
    }
}