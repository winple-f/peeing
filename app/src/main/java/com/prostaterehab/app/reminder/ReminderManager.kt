package com.prostaterehab.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * 随访提醒管理器
 * 负责设置和取消四个时间点的提醒：基线、1个月、3个月、6个月
 */
object ReminderManager {

    private const val REQUEST_CODE_BASELINE = 1001
    private const val REQUEST_CODE_1MONTH = 1002
    private const val REQUEST_CODE_3MONTH = 1003
    private const val REQUEST_CODE_6MONTH = 1004

    /**
     * 设置所有随访提醒
     * @param context 上下文
     * @param baselineDate 基线日期（时间戳），默认使用当前时间
     */
    fun setAllReminders(context: Context, baselineDate: Long = System.currentTimeMillis()) {
        val calendar = Calendar.getInstance()

        // 基线提醒（注册后第2天提醒）
        calendar.timeInMillis = baselineDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        setReminder(context, REQUEST_CODE_BASELINE, calendar.timeInMillis, "请完成基线评估")

        // 1个月后
        calendar.timeInMillis = baselineDate
        calendar.add(Calendar.MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        setReminder(context, REQUEST_CODE_1MONTH, calendar.timeInMillis, "1个月随访时间到了，请填写随访问卷")

        // 3个月后
        calendar.timeInMillis = baselineDate
        calendar.add(Calendar.MONTH, 3)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        setReminder(context, REQUEST_CODE_3MONTH, calendar.timeInMillis, "3个月随访时间到了，请填写随访问卷")

        // 6个月后
        calendar.timeInMillis = baselineDate
        calendar.add(Calendar.MONTH, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        setReminder(context, REQUEST_CODE_6MONTH, calendar.timeInMillis, "6个月随访时间到了，请填写随访问卷")
    }

    /**
     * 设置单个提醒
     */
    private fun setReminder(context: Context, requestCode: Int, triggerTime: Long, message: String) {
        // 如果时间已经过去了，就不设置了
        if (triggerTime <= System.currentTimeMillis()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("message", message)
            putExtra("request_code", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // 没有精确闹钟权限时，使用不精确的
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * 取消所有提醒
     */
    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)

        val requestCodes = intArrayOf(
            REQUEST_CODE_BASELINE,
            REQUEST_CODE_1MONTH,
            REQUEST_CODE_3MONTH,
            REQUEST_CODE_6MONTH
        )

        for (code in requestCodes) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
