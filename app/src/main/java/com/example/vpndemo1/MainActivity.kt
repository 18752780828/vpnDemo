package com.example.vpndemo1


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.vpndemo1.vpn.sever.LocalVPNService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST_CODE = 0x0F

    private var waitingForVPNStart = false

    private val vpnStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocalVPNService.BROADCAST_VPN_STATE == intent.action) {
                if (intent.getBooleanExtra("running", false)) waitingForVPNStart = false
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        waitingForVPNStart = false
        LocalBroadcastManager.getInstance(this).registerReceiver(
            vpnStateReceiver,
            IntentFilter(LocalVPNService.BROADCAST_VPN_STATE)
        )

//        sw_connect.setOnCheckedChangeListener{_,checked->
//            if(checked)
//                startVPN()
//            else
//                shutdownVPN()
//        }

        btn_connect.setOnClickListener {
            if(!waitingForVPNStart){
                startVPN()
            }
        }
    }

    private fun startVPN() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null)
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
    }

    private fun swSynStatus(sta:Boolean) {
        if(sta)
            sw_connect.setText("Connected")
        else
            sw_connect.setText("Disconnected")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            waitingForVPNStart = true
            startService(Intent(this, LocalVPNService::class.java))
            enableButton(false)
        }
    }

    override fun onResume() {
        super.onResume()
        enableButton(!waitingForVPNStart && !LocalVPNService.isRunning())
    }

    private fun enableButton(enable: Boolean) {
        val vpnButton = findViewById<Button>(R.id.btn_connect)
        if (enable) {
            vpnButton.isEnabled = true
            vpnButton.setText(R.string.connected)
        } else {
            vpnButton.isEnabled = false
            vpnButton.setText(R.string.disconnected)
        }
    }

//    private fun shutdownVPN() {
//        if (VhostsService.isRunning())
//            startService(
//                Intent(this, VhostsService::class.java).
//            setAction(VhostsService.ACTION_DISCONNECT))
//        swSynStatus(false)
//    }


}