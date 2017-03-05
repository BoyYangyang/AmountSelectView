package houyi.mime.com.amountselectedview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import houyi.mime.com.amountselected.ScaleView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScaleView scaleView = (ScaleView) findViewById(R.id.scaleview);
        scaleView.setAmount(0,10000,10000);
    }
}
