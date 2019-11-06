package inc.ahmedmourad.sherlock.widget.adapter

import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.bumptech.glide.Glide
import dagger.Lazy
import inc.ahmedmourad.sherlock.R
import inc.ahmedmourad.sherlock.dagger.SherlockComponent
import inc.ahmedmourad.sherlock.domain.platform.DateManager
import inc.ahmedmourad.sherlock.formatter.Formatter
import inc.ahmedmourad.sherlock.model.AppSimpleRetrievedChild
import splitties.init.appCtx
import timber.log.Timber
import javax.inject.Inject

internal class ResultsRemoteViewsFactory(private val context: Context, private val results: List<Pair<AppSimpleRetrievedChild, Int>>) : RemoteViewsService.RemoteViewsFactory {

    @Inject
    lateinit var formatter: Lazy<Formatter>

    @Inject
    lateinit var dateManager: Lazy<DateManager>

    override fun onCreate() {
        SherlockComponent.Widget.resultsRemoteViewsFactoryComponent.get().inject(this)
    }

    override fun onDataSetChanged() {

    }

    override fun getCount() = results.size

    override fun getViewAt(position: Int): RemoteViews? {

        if (position >= count)
            return null

        val result = results[position]

        val views = RemoteViews(context.packageName, R.layout.item_widget_result)

        //TODO: needs to change over time
        views.setTextViewText(
                R.id.widget_result_date,
                dateManager.get().getRelativeDateTimeString(result.first.publicationDate)
        )

        views.setTextViewText(
                R.id.widget_result_notes,
                result.first.notes
        )

        views.setTextViewText(
                R.id.widget_result_location,
                formatter.get().formatLocation(
                        result.first.locationName,
                        result.first.locationAddress
                )
        )

        setPicture(views, result.first.pictureUrl)

        return views
    }

    private fun setPicture(views: RemoteViews, pictureUrl: String) {

        var bitmap: Bitmap?

        try {
            bitmap = Glide.with(appCtx)
                    .asBitmap()
                    .load(pictureUrl)
                    .submit()
                    .get()
        } catch (e: Exception) {
            bitmap = null
            Timber.e(e)
        }

        if (bitmap != null)
            views.setImageViewBitmap(R.id.widget_result_picture, bitmap)
        else
            views.setImageViewResource(R.id.widget_result_picture, R.drawable.placeholder)
    }

    override fun getLoadingView() = null

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = false

    override fun onDestroy() {
        SherlockComponent.Widget.resultsRemoteViewsFactoryComponent.release()
    }
}
