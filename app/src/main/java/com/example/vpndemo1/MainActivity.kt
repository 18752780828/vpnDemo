package com.example.vpndemo1


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.vpndemo1.vpn.sever.LocalVPNService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST_CODE = 0x0F
    private var virtualIP4Str = "192.168.43.1";
    private var waitingForVPNStart = false

    private val vpnStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (LocalVPNService.BROADCAST_VPN_STATE == intent.action) {
                if (intent.getBooleanExtra("running", false)) {
                    waitingForVPNStart = false
                    synEdtIpSta(true, true)
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        edt_vir_ip.setText(virtualIP4Str)
        waitingForVPNStart = false
        LocalBroadcastManager.getInstance(this).registerReceiver(
            vpnStateReceiver,
            IntentFilter(LocalVPNService.BROADCAST_VPN_STATE)
        )

        btn_connect.setOnClickListener {
            if(!waitingForVPNStart){
                if(LocalVPNService.isRunning())
                    shutdownVPN()
                else
                    startVPN()
            }
        }
    }

    private fun startVPN() {
        virtualIP4Str = edt_vir_ip.text.toString()
        if(!LocalVPNService.isCorrectIp(virtualIP4Str)){
            Toast.makeText(this,"IP不合法!", Toast.LENGTH_SHORT).show()
            return
        }
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            vpnIntent.putExtra(LocalVPNService.ACTION_ADDRESS, edt_vir_ip.text.toString())
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE)
            synEdtIpSta(false,false)
        }
        else
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            waitingForVPNStart = true
            val intent = Intent(this, LocalVPNService::class.java)
            intent.putExtra(LocalVPNService.ACTION_ADDRESS, edt_vir_ip.text.toString())
            startService(intent)
            synEdtIpSta(false,false)

        }
    }

    override fun onResume() {
        super.onResume()
        synEdtIpSta(!waitingForVPNStart, LocalVPNService.isRunning())
    }

    private fun synEdtIpSta(enSta: Boolean, conSta: Boolean) {

        btn_connect.isEnabled = enSta
        if (conSta) btn_connect.setText(R.string.connected)
        else btn_connect.setText(R.string.disconnected)
        edt_vir_ip.isEnabled = (enSta && !conSta)
    }

    private fun shutdownVPN() {
            startService(
                Intent(this, LocalVPNService::class.java).
            setAction(LocalVPNService.ACTION_DISCONNECT))
        synEdtIpSta(true, false)
    }


}