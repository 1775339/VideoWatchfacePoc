package com.titan.titanvideotrimmingpoc

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.widget.TextView
import androidx.annotation.UiThread

/**
 * @author Jakob Liu
 */
class LoadingDialog(context: Context) : Dialog(context) {

    private val mHandler = Handler()

    private val MIN_SHOW_TIME_MS = 500
    private val MIN_DELAY_MS = 400

    // These fields should only be accessed on the UI thread.
    private var mStartTime: Long = -1
    private var mPostedHide = false
    private var mPostedShow = false
    private var mDismissed = false

    private val mDelayedHide = Runnable {
        mPostedHide = false
        mStartTime = -1
        dismiss()
    }

    private val mDelayedShow = Runnable {
        mPostedShow = false
        if (!mDismissed) {
            mStartTime = System.currentTimeMillis()
            show()
        }
    }

    private lateinit var mMessageView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = this.window
        window!!.requestFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        mMessageView = findViewById(R.id.tvMessage)
        setCanceledOnTouchOutside(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks()
    }

    private fun removeCallbacks() {
        mHandler.removeCallbacks(mDelayedHide)
        mHandler.removeCallbacks(mDelayedShow)
    }

    /**
     * Hide the progress view if it is visible. The progress view will not be
     * hidden until it has been shown for at least a minimum show time. If the
     * progress view was not yet visible, cancels showing the progress view.
     *
     *
     * This method may be called off the UI thread.
     */
    fun hideDialog() {
        // This method used to be synchronized, presumably so that it could be safely called off
        // the UI thread; however, the referenced fields were still accessed both on and off the
        // UI thread, e.g. not thread-safe. Now we hand-off everything to the UI thread.
        mHandler.post { hideOnUiThread() }
    }

    @UiThread
    private fun hideOnUiThread() {
        mDismissed = true
        mHandler.removeCallbacks(mDelayedShow)
        mPostedShow = false
        val diff = System.currentTimeMillis() - mStartTime
        if (diff >= MIN_SHOW_TIME_MS || mStartTime == -1L) {
            // The progress spinner has been shown long enough
            // OR was not shown yet. If it wasn't shown yet,
            // it will just never be shown.
            dismiss()
        } else {
            // The progress spinner is shown, but not long enough,
            // so put a delayed message in to hide it when its been
            // shown long enough.
            if (!mPostedHide) {
                mHandler.postDelayed(mDelayedHide, MIN_SHOW_TIME_MS - diff)
                mPostedHide = true
            }
        }
    }

    /**
     * Show the progress view after waiting for a minimum delay. If
     * during that time, hide() is called, the view is never made visible.
     *
     *
     * This method may be called off the UI thread.
     */
    fun showDialog() {
        // This method used to be synchronized, presumably so that it could be safely called off
        // the UI thread; however, the referenced fields were still accessed both on and off the
        // UI thread, e.g. not thread-safe. Now we hand-off everything to the UI thread.
        mHandler.post { showOnUiThread() }
    }

    @UiThread
    private fun showOnUiThread() {
        // Reset the start time.
        mStartTime = -1
        mDismissed = false
        mHandler.removeCallbacks(mDelayedHide)
        mPostedHide = false
        if (!mPostedShow) {
            mHandler.postDelayed(mDelayedShow, MIN_DELAY_MS.toLong())
            mPostedShow = true
        }
    }

}