package com.example.fairr.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecurringExpenseReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var scheduler: RecurringExpenseScheduler
    
    companion object {
        private const val TAG = "RecurringExpenseReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        
        when (intent.action) {
            "com.example.fairr.CHECK_UPCOMING" -> {
                scheduler.processDailyCheck()
            }
            "com.example.fairr.GENERATE_INSTANCES" -> {
                val groupId = intent.getStringExtra("group_id")
                val expenseId = intent.getStringExtra("expense_id")
                
                if (groupId != null && expenseId != null) {
                    scheduler.generateInstancesForExpense(groupId, expenseId)
                } else {
                    Log.e(TAG, "Missing group_id or expense_id in intent")
                }
            }
            else -> {
                Log.w(TAG, "Unknown action: ${intent.action}")
            }
        }
    }
} 