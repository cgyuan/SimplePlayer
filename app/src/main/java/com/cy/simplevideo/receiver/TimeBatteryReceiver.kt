package com.cy.simplevideo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager


class TimeBatteryReceiver : BroadcastReceiver() {

    companion object {

        fun register(context: Context): TimeBatteryReceiver {
            val receiver = TimeBatteryReceiver()
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK)
            filter.addAction(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(receiver, filter)
            return receiver
        }

    }

    private var onTimeUpdate: (() -> Unit)? = null
    private var onBatteryChanged: ((Int) -> Unit)? = null

    fun setOnTimeUpdateListener(listener: () -> Unit) {
        onTimeUpdate = listener
    }

    fun setOnBatteryChangedListener(listener: (Int) -> Unit) {
        onBatteryChanged = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_TIME_TICK -> {
                onTimeUpdate?.invoke()
            }
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                onBatteryChanged?.invoke(level)
            }
        }
    }

}