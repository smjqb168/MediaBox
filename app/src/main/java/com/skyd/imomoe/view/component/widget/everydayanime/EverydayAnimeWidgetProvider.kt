package com.skyd.imomoe.view.component.widget.everydayanime

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.Toast
import com.google.gson.Gson
import com.skyd.imomoe.App
import com.skyd.imomoe.R
import com.skyd.imomoe.bean.AnimeCoverBean
import com.skyd.imomoe.model.DataSourceManager
import com.skyd.imomoe.model.impls.RouterProcessor
import com.skyd.imomoe.util.Util.getWeekday
import com.skyd.imomoe.util.Util.showToast
import java.util.*


class EverydayAnimeWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        "桌面小组件为测试性功能，若造成系统卡顿请移除".showToast(Toast.LENGTH_LONG)
//
//        val firstTime: Long = SystemClock.elapsedRealtime() + 5000
//        val receiver = Intent().setAction(REFRESH_ACTION)
//        val pendingIntent =
//            PendingIntent.getBroadcast(context, 2006, receiver,
//                PendingIntent.FLAG_UPDATE_CURRENT)
//
//        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        am.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime, 60000, pendingIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == VIEW_CLICK_ACTION) {
            val item = Gson().fromJson(intent.getStringExtra(ITEM), AnimeCoverBean::class.java)
            if (item.episodeClickable?.actionUrl.equals(item.actionUrl))
                startPlayActivity(context, item.episodeClickable?.actionUrl)
            else startPlayActivity(context, item.episodeClickable?.actionUrl + item.actionUrl)
        } else if (intent.action == REFRESH_ACTION) {
            val mgr: AppWidgetManager = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, EverydayAnimeWidgetProvider::class.java)
            val rv = RemoteViews(context.packageName, R.layout.widget_everyday_anime)
            rv.setTextViewText(
                R.id.tv_widget_everyday_anime_title, getWeekday(
                    Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK)
                )
            )
            mgr.updateAppWidget(mgr.getAppWidgetIds(cn), rv)
            mgr.notifyAppWidgetViewDataChanged(
                mgr.getAppWidgetIds(cn),
                R.id.lv_widget_everyday_anime
            )
            App.context.getString(R.string.update_widget).showToast()
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // update each of the widgets with the remote adapter
        for (i in appWidgetIds.indices) {
            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            val intent = Intent(context, EverydayAnimeService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val rv = RemoteViews(context.packageName, R.layout.widget_everyday_anime)
            rv.setTextViewText(
                R.id.tv_widget_everyday_anime_title, getWeekday(
                    Calendar.getInstance(Locale.getDefault()).get(Calendar.DAY_OF_WEEK)
                )
            )
            rv.setRemoteAdapter(R.id.lv_widget_everyday_anime, intent)
            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
//            rv.setEmptyView(R.id.stack_view, R.id.empty_view)
            Intent(context, EverydayAnimeWidgetProvider::class.java).apply {
                action = REFRESH_ACTION
                rv.setOnClickPendingIntent(
                    R.id.iv_widget_everyday_anime_refresh,
                    PendingIntent.getBroadcast(
                        context, 0, this,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            val viewClickIntent = Intent(context, EverydayAnimeWidgetProvider::class.java)
            viewClickIntent.action = VIEW_CLICK_ACTION
            viewClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
            val toastPendingIntent = PendingIntent.getBroadcast(
                context, 0, viewClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            rv.setPendingIntentTemplate(R.id.lv_widget_everyday_anime, toastPendingIntent)
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv)
            appWidgetManager.notifyAppWidgetViewDataChanged(
                appWidgetIds,
                R.id.lv_widget_everyday_anime
            )
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun startPlayActivity(context: Context, actionUrl: String?) {
        (DataSourceManager.getRouterProcessor() ?: RouterProcessor()).process(context, actionUrl)
    }

    companion object {
        const val VIEW_CLICK_ACTION = "com.skyd.imomoe.widget.VIEW_CLICK_ACTION"
        const val REFRESH_ACTION = "com.skyd.imomoe.widget.REFRESH_ACTION"
        const val ITEM = "com.skyd.imomoe.widget.ITEM"
    }
}