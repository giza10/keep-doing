package com.hkb48.keepdo.ui.sort

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import com.hkb48.keepdo.R
import com.hkb48.keepdo.util.CompatUtil

class SortableListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyle: Int = 0
) : ListView(context, attrs, defStyle) {
    private val mItemHeight: Int = context.resources.getDimensionPixelSize(
        R.dimen.tasksort_item_height
    )
    private var mDragView: ImageView? = null
    private var mWindowManager: WindowManager? = null
    private var mWindowParams: WindowManager.LayoutParams? = null
    private var mDragPos // which item is being dragged
            = 0
    private var mFirstDragPos // where was the dragged item originally
            = 0
    private var mDragPoint // at what offset inside the item did the user grab
            = 0

    // it
    private var mCoordOffset // the difference between screen coordinates and
            = 0

    // coordinates in this view
    private var mDragAndDropListener: DragAndDropListener? = null
    private var mUpperBound = 0
    private var mLowerBound = 0
    private var mHeight = 0
    private var mDragBitmap: Bitmap? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            val position = pointToPosition(x, y)
            if (position == INVALID_POSITION) {
                return super.onInterceptTouchEvent(ev)
            }
            val listItemView = getChildAt(
                position
                        - firstVisiblePosition
            ) as SortableListItem
            val grabberView = listItemView.grabberView
            mDragPoint = y - listItemView.top
            mCoordOffset = ev.rawY.toInt() - y
            val rect = Rect().apply {
                left = grabberView.left
                right = grabberView.right
                top = grabberView.top
                bottom = grabberView.bottom
            }
            if (rect.left < x && x < rect.right) {
                // Create a copy of the drawing cache so that it does not get recycled
                // by the framework when the list tries to clean up memory
                listItemView.isDrawingCacheEnabled = true
                val bitmap = Bitmap.createBitmap(listItemView.drawingCache)
                listItemView.isDrawingCacheEnabled = false
                val listBounds = Rect()
                getGlobalVisibleRect(listBounds, null)
                startDrag(bitmap, listBounds.left, y)
                mDragPos = position
                mFirstDragPos = mDragPos
                mHeight = height
                val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
                mUpperBound = (y - touchSlop).coerceAtMost(mHeight / 3)
                mLowerBound = (y + touchSlop).coerceAtLeast(mHeight * 2 / 3)
                return false
            }
            mDragView = null
        }
        return super.onInterceptTouchEvent(ev)
    }

    private fun getBitmapFromView(view: View, activity: Activity, callback: (Bitmap) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.window?.let { window ->
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val locationOfViewInWindow = IntArray(2)
                view.getLocationInWindow(locationOfViewInWindow)
                try {
                    PixelCopy.request(
                        window,
                        Rect(
                            locationOfViewInWindow[0],
                            locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width,
                            locationOfViewInWindow[1] + view.height
                        ),
                        bitmap,
                        { copyResult ->
                            if (copyResult == PixelCopy.SUCCESS) {
                                callback(bitmap)
                            }
                            // possible to handle other result codes ...
                        },
                        Handler(Looper.getMainLooper())
                    )
                } catch (e: IllegalArgumentException) {
                    // PixelCopy may throw IllegalArgumentException, make sure to handle it
                    e.printStackTrace()
                }
            }
        } else {
            @Suppress("DEPRECATION")
            view.isDrawingCacheEnabled = true
            @Suppress("DEPRECATION")
            val bitmap = Bitmap.createBitmap(view.drawingCache)
            @Suppress("DEPRECATION")
            view.isDrawingCacheEnabled = false
            callback(bitmap)
        }
    }

    private fun adjustScrollBounds(y: Int) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3
        }
    }

    /*
     * Restore visibility for all items in list
     */
    private fun unExpandViews() {
        var visibleItemIndex = 0
        while (true) {
            val itemView = getChildAt(visibleItemIndex) as SortableListItem? ?: break
            val params = itemView.layoutParams
            params.height = mItemHeight
            itemView.layoutParams = params
            itemView.visibility = VISIBLE
            visibleItemIndex++
        }
    }

    /*
     * Adjust visibility to make it appear as though an item is being dragged
     * around and other items are making room for it.
     */
    private fun doExpansion() {
        val first = getChildAt(mFirstDragPos - firstVisiblePosition)
        var visibleItemIndex = 0
        while (true) {
            val itemView = getChildAt(visibleItemIndex) as SortableListItem? ?: break
            var visibility = VISIBLE
            if (itemView == first) {
                // processing the item that is being dragged
                if (mDragPos == mFirstDragPos) {
                    // hovering over the original location
                    visibility = INVISIBLE
                }
            }
            if (visibleItemIndex == mDragPos - firstVisiblePosition) {
                visibility = INVISIBLE
            }
            val params = itemView.layoutParams
            params.height = mItemHeight
            itemView.layoutParams = params
            itemView.visibility = visibility
            visibleItemIndex++
        }
        layoutChildren()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (mDragView != null) {
            when (val action = ev.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val rect = Rect()
                    mDragView?.getDrawingRect(rect)
                    stopDrag()
                    if (mDragPos in 0 until count) {
                        mDragAndDropListener?.onDrop(mFirstDragPos, mDragPos)
                    }
                    unExpandViews()
                }
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val y = ev.y.toInt()
                    dragView(y)
                    val position = pointToPosition(0, y)
                    if (position >= 0) {
                        if (action == MotionEvent.ACTION_DOWN
                            || position != mDragPos
                        ) {
                            mDragAndDropListener?.onDrag(mDragPos, position)
                            mDragPos = position
                            doExpansion()
                        }
                        var speed = 0
                        adjustScrollBounds(y)
                        if (y > mLowerBound) {
                            // scroll the list up a bit
                            speed = if (y > (mHeight + mLowerBound) / 2) 16 else 4
                        } else if (y < mUpperBound) {
                            // scroll the list down a bit
                            speed = if (y < mUpperBound / 2) -16 else -4
                        }
                        if (speed != 0) {
                            var ref = pointToPosition(0, mHeight / 2)
                            if (ref == INVALID_POSITION) {
                                // we hit a divider or an invisible view, check
                                // somewhere else
                                ref = pointToPosition(0, mHeight / 2 + dividerHeight + mItemHeight)
                            }
                            getChildAt(ref - firstVisiblePosition)?.let {
                                val pos = it.top
                                setSelectionFromTop(ref, pos - speed)
                            }
                        }
                    }
                }
            }
            return true
        }
        return super.onTouchEvent(ev)
    }

    private fun startDrag(bitmap: Bitmap, x: Int, y: Int) {
        stopDrag()
        val windowParams = WindowManager.LayoutParams().apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y - mDragPoint + mCoordOffset
            height = WindowManager.LayoutParams.WRAP_CONTENT
            width = WindowManager.LayoutParams.WRAP_CONTENT
            flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            format = PixelFormat.TRANSLUCENT
            windowAnimations = 0
        }
        val v = ImageView(context)
        val backGroundColor = CompatUtil.getColor(
            context,
            R.color.tasksort_dragdrop_view_bg
        )
        v.setBackgroundColor(backGroundColor)
        v.setImageBitmap(bitmap)
        mDragBitmap = bitmap
        mWindowParams = windowParams
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        mWindowManager?.addView(v, mWindowParams)
        mDragView = v
    }

    private fun dragView(y: Int) {
        mWindowParams?.y = y - mDragPoint + mCoordOffset
        mWindowManager?.updateViewLayout(mDragView, mWindowParams)
    }

    private fun stopDrag() {
        mDragView?.let {
            it.visibility = INVISIBLE
            mWindowManager?.removeView(mDragView)
            it.setImageDrawable(null)
            mDragView = null
        }
        mDragBitmap?.recycle()
        mDragBitmap = null
    }

    fun setDragAndDropListener(listener: DragAndDropListener?) {
        mDragAndDropListener = listener
    }

    interface DragAndDropListener {
        fun onDrag(from: Int, to: Int)
        fun onDrop(from: Int, to: Int)
    }

}