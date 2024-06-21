package com.example.androidfragmenthashkeyissue

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.fragment.compose.AndroidFragment
import com.example.androidfragmenthashkeyissue.ui.theme.AndroidFragmentHashKeyIssueTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidFragmentHashKeyIssueTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Text(text = "Without fix, Fragment B, C and D are overwritten by A")
                        AndroidFragment<FragmentA>()
                        AndroidFragment<FragmentB>()
                        AndroidFragment<FragmentC>()
                        AndroidFragment<FragmentD>()

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(text = "With fix, we can see all fragment because they do not share container ids")
                        AndroidFragmentFixed<FragmentA>()
                        AndroidFragmentFixed<FragmentB>()
                        AndroidFragmentFixed<FragmentC>()
                        AndroidFragmentFixed<FragmentD>()
                    }
                }
            }
        }
    }
}