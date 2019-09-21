package tech.takenoko.screenmirror

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tech.takenoko.screenmirror.view.FragmentPage1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().run {
            add(R.id.fragment_frame, FragmentPage1())
            commit()
        }
    }
}
