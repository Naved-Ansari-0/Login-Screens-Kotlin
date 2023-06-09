package com.example.login.signIn

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.example.login.R
import com.example.login.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUpScreen : AppCompatActivity() {

    private lateinit var nameSignUp : TextView
    private lateinit var emailSignUp : TextView
    private lateinit var passwordSignUp : TextView
    private lateinit var forgotPwdButton : Button
    private lateinit var signUpButton : Button
    private lateinit var signInButton : Button
    private lateinit var agreeCheckBox : CheckBox

    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    @SuppressLint("MissingInflatedId", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_screen)

        nameSignUp = findViewById(R.id.nameSignUp)
        emailSignUp = findViewById(R.id.emailSignUp)
        passwordSignUp = findViewById(R.id.passwordSignUp)
        forgotPwdButton = findViewById(R.id.forgotPwdButton)
        signInButton = findViewById(R.id.signInButton)
        signUpButton = findViewById(R.id.signUpButton)
        agreeCheckBox = findViewById(R.id.agreeCheckBox)

        auth = Firebase.auth

        signInButton.setOnClickListener{
            SignInSignUpUtils.navigateToSignInScreen(this, this)
        }

        forgotPwdButton.setOnClickListener{
            SignInSignUpUtils.navigateToResetPwdScreen(this, this)
        }

        signUpButton.setOnClickListener{

            val name = nameSignUp.text.toString().trim()
            val email = emailSignUp.text.toString().trim()
            val password = passwordSignUp.text.toString().trim()

            if( !SignInSignUpUtils.checkName(this, name) ||
                !SignInSignUpUtils.checkEmail(this, email) ||
                    !SignInSignUpUtils.checkPassword(this, password)
            )
                return@setOnClickListener

            if(!agreeCheckBox.isChecked){
                Toast.makeText(this,"Click on checkbox to agree Privacy Policy and Terms of Service.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(!SignInSignUpUtils.isInternetAvailable(this)){
                SignInSignUpUtils.noInternetToast(this)
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser

//                        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
//                        user?.updateProfile(profileUpdates)
//                            ?.addOnCompleteListener { task ->
//                                if (task.isSuccessful) {
//                                    // Name added successfully
//                                } else {
//
//                                    val errorCode = (task.exception as FirebaseAuthException).errorCode
//                                    SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
//
//                                }
//                            }

                        val userData = User(name,
                                            email,
                                            "",
                                            "",
                                            ArrayList(),
                                            ArrayList(),
                                            ArrayList(),
                                            Timestamp.now()
                                        )

                        val id = FirebaseAuth.getInstance().currentUser!!.uid
                        db = FirebaseFirestore.getInstance()

                        db.collection("users").document(id).set(userData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener{
                                val errorCode = (task.exception as FirebaseAuthException).errorCode
                                SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
                            }

                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    Toast.makeText(this, "Email verification link sent to $email", Toast.LENGTH_LONG).show()
                                    SignInSignUpUtils.navigateToSignInScreen(this, this)
                                }else{
                                    val errorCode = (task.exception as FirebaseAuthException).errorCode
                                    SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
                                }
                            }

                    } else {
                        val errorCode = (task.exception as FirebaseAuthException).errorCode
                        SignInSignUpUtils.firebaseExceptionToast(this, errorCode)
                    }
                }

        }



    }

    override fun onBackPressed() {
        SignInSignUpUtils.navigateToSignInScreen(this, this)
    }
}