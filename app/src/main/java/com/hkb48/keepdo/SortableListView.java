package com.hkb48.keepdo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.hkb48.keepdo.util.CompatUtil;

public class SortableListView extends ListView {
    private final int mItemHeight;
    private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private int mDragPos; // which item is being dragged
    private int mFirstDragPos; // where was the dragged item originally
    private int mDragPoint; // at what offset inside the item did the user grab
    // it
    private int mCoordOffset; // the difference between screen coordinates and
    // coordinates in this view
    private DragAndDropListener mDragAndDropListener;
    private int mUpperBound;
    private int mLowerBound;
    private int mHeight;
    private Bitmap mDragBitmap;

    public SortableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SortableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mItemHeight = context.getResources().getDimensionPixelSize(
                R.dimen.tasksort_item_height);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int position = pointToPosition(x, y);
            if (position == AdapterView.INVALID_POSITION) {
                return super.onInterceptTouchEvent(ev);
            }

            SortableListItem listItemView = (SortableListItem) getChildAt(position
                    - getFirstVisiblePosition());

            View grabberView = listItemView.getGrabberView();
            if (grabberView != null) {
                mDragPoint = y - listItemView.getTop();
                mCoordOffset = ((int) ev.getRawY()) - y;
                Rect rect = new Rect();

                rect.left = grabberView.getLeft();
                rect.right = grabberView.getRight();
                rect.top = grabberView.getTop();
                rect.bottom = grabberView.getBottom();

                if ((rect.left < x) && (x < rect.right)) {
                    listItemView.setDrawingCacheEnabled(true);
                    // Create a copy of the drawing cache so that it does
                    // not get recycled
                    // by the framework when the list tries to clean up
                    // memory
                    Bitmap bitmap = Bitmap.createBitmap(listItemView
                            .getDrawingCache());
                    listItemView.setDrawingCacheEnabled(false);

                    Rect listBounds = new Rect();

                    getGlobalVisibleRect(listBounds, null);

                    startDrag(bitmap, listBounds.left, y);
                    mDragPos = position;
                    mFirstDragPos = mDragPos;
                    mHeight = getHeight();
                    final Context context = getContext();
                    final int touchSlop = ViewConfiguration.get(context)
                            .getScaledTouchSlop();
                    mUpperBound = Math.min(y - touchSlop, mHeight / 3);
                    mLowerBound = Math.max(y + touchSlop, mHeight * 2 / 3);
                    return false;
                }

                mDragView = null;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void adjustScrollBounds(int y) {
        if (y >= mHeight / 3) {
            mUpperBound = mHeight / 3;
        }
        if (y <= mHeight * 2 / 3) {
            mLowerBound = mHeight * 2 / 3;
        }
    }

    /*
     * Restore visibility for all items in list
     */
    private void unExpandViews() {
        for (int visibleItemIndex = 0; ; visibleItemIndex++) {
            SortableListItem itemView = (SortableListItem) getChildAt(visibleItemIndex);
            if (itemView == null) {
                break;
            }

            if (itemView.getGrabberView() != null) {
                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                params.height = mItemHeight;
                itemView.setLayoutParams(params);
                itemView.setVisibility(View.VISIBLE);
            }
        }
    }

    /*
     * Adjust visibility to make it appear as though an item is being dragged
     * around and other items are making room for it.
     */
    private void doExpansion() {
        final View first = getChildAt(mFirstDragPos - getFirstVisiblePosition());

        for (int visibleItemIndex = 0; ; visibleItemIndex++) {
            SortableListItem itemView = (SortableListItem) getChildAt(visibleItemIndex);
            if (itemView == null) {
                break;
            }
            int visibility = View.VISIBLE;
            if (itemView.equals(first)) {
                // processing the item that is being dragged
                if (mDragPos == mFirstDragPos) {
                    // hovering over the original location
                    visibility = View.INVISIBLE;
                }
            }
            if (visibleItemIndex == (mDragPos - getFirstVisiblePosition())) {
                visibility = View.INVISIBLE;
            }

            if (itemView.getGrabberView() != null) {
                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                params.height = mItemHeight;
                itemView.setLayoutParams(params);
                itemView.setVisibility(visibility);
            }
        }

        layoutChildren();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mDragView != null) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    Rect rect = new Rect();
                    mDragView.getDrawingRect(rect);
                    stopDrag();

                    if (mDragAndDropListener != null && mDragPos >= 0
                            && mDragPos < getCount()) {
                        mDragAndDropListener.onDrop(mFirstDragPos, mDragPos);
                    }
                    unExpandViews();
                    break;

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    final int y = (int) ev.getY();
                    dragView(y);
                    final int position = pointToPosition(0, y);
                    if (position >= 0) {
                        if (action == MotionEvent.ACTION_DOWN
                                || position != mDragPos) {
                            if (mDragAndDropListener != null) {
                                mDragAndDropListener.onDrag(mDragPos, position);
                            }
                            mDragPos = position;
                            doExpansion();
                        }
                        int speed = 0;
                        adjustScrollBounds(y);
                        if (y > mLowerBound) {
                            // scroll the list up a bit
                            speed = y > (mHeight + mLowerBound) / 2 ? 16 : 4;
                        } else if (y < mUpperBound) {
                            // scroll the list down a bit
                            speed = y < mUpperBound / 2 ? -16 : -4;
                        }
                        if (speed != 0) {
                            int ref = pointToPosition(0, mHeight / 2);
                            if (ref == AdapterView.INVALID_POSITION) {
                                // we hit a divider or an invisible view, check
                                // somewhere else
                                ref = pointToPosition(0, mHeight / 2
                                        + getDividerHeight() + mItemHeight);
                            }
                            View v = getChildAt(ref - getFirstVisiblePosition());
                            if (v != null) {
                                int pos = v.getTop();
                                setSelectionFromTop(ref, pos - speed);
                            }
                        }
                    }
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void startDrag(Bitmap bm, int x, int y) {
        stopDrag();

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.START;
        mWindowParams.x = x;
        mWindowParams.y = y - mDragPoint + mCoordOffset;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        final Context context = getContext();
        final ImageView v = new ImageView(context);
        final int backGroundColor = CompatUtil.getColor(context,
                R.color.tasksort_dragdrop_view_bg);
        v.setBackgroundColor(backGroundColor);
        v.setImageBitmap(bm);
        mDragBitmap = bm;

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mWindowManager != null) {
            mWindowManager.addView(v, mWindowParams);
        }
        mDragView = v;
    }

    private void dragView(int y) {
        mWindowParams.y = y - mDragPoint + mCoordOffset;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);
    }

    private void stopDrag() {
        if (mDragView != null) {
            mDragView.setVisibility(View.INVISIBLE);
            mWindowManager.removeView(mDragView);
            mDragView.setImageDrawable(null);
            mDragView = null;
        }
        if (mDragBitmap != null) {
            mDragBitmap.recycle();
            mDragBitmap = null;
        }
    }

    public void setDragAndDropListener(DragAndDropListener listener) {
        mDragAndDropListener = listener;
    }

    public interface DragAndDropListener {
        void onDrag(int from, int to);

        void onDrop(int from, int to);
    }
}
