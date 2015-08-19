package com.github.kmenager.materialanimatedswitch;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import com.github.kmenager.materialanimatedswitch.observer.BallFinishObservable;
import com.github.kmenager.materialanimatedswitch.observer.BallMoveObservable;
import com.github.kmenager.materialanimatedswitch.painter.BallPainter;
import com.github.kmenager.materialanimatedswitch.painter.BasePainter;
import com.github.kmenager.materialanimatedswitch.painter.IconPressPainter;
import com.github.kmenager.materialanimatedswitch.painter.IconReleasePainter;

import java.util.Observable;
import java.util.Observer;


public class MaterialAnimatedSwitch extends View {

    private int margin;
    private BasePainter basePainter;
    private BallPainter ballPainter;
    private MaterialAnimatedSwitchState actualState;
    private IconPressPainter iconPressPainter;
    private IconReleasePainter iconReleasePainter;
    private int baseColorRelease = Color.parseColor("#3061BE");
    private int baseColorPress = Color.parseColor("#D7E7FF");
    private int ballColorRelease = Color.parseColor("#5992FB");
    private int ballColorPress = Color.parseColor("#FFFFFF");
    private Bitmap releaseIcon;
    private Bitmap pressIcon;
    private int ballRadius;
    private int iconSize;
    private int switchWidth;
    private int switchHeight;
    private BallFinishObservable ballFinishObservable;
    private BallMoveObservable ballMoveObservable;
    private boolean isClickable = true;
    private OnCheckedChangeListener onCheckedChangeListener;

    public MaterialAnimatedSwitch(Context context) {
        super(context);
        init();
    }

    public MaterialAnimatedSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialAnimatedSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init() {
        initObservables();
        initPainters();
        actualState = MaterialAnimatedSwitchState.INIT;
        setState(actualState);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void initPainters() {
        basePainter = new BasePainter(baseColorRelease, baseColorPress, margin, ballMoveObservable);
        ballPainter = new BallPainter(ballColorRelease, ballColorPress, margin, ballRadius, ballFinishObservable,
                ballMoveObservable, getContext());

        iconPressPainter =
                new IconPressPainter(getContext(), pressIcon, ballRadius, iconSize,
                        ballFinishObservable, ballMoveObservable, margin);
        iconReleasePainter =
                new IconReleasePainter(getContext(), releaseIcon, iconSize, ballFinishObservable, margin);
    }

    private void init(AttributeSet attrs) {
        TypedArray attributes =
                getContext().obtainStyledAttributes(attrs, R.styleable.materialAnimatedSwitch);
        initAttributes(attributes);
        init();
    }

    private void initAttributes(TypedArray attributes) {
        baseColorRelease = attributes.getColor(R.styleable.materialAnimatedSwitch_base_release_color,
                baseColorRelease);
        baseColorPress =
                attributes.getColor(R.styleable.materialAnimatedSwitch_base_press_color, baseColorPress);
        ballColorRelease = attributes.getColor(R.styleable.materialAnimatedSwitch_ball_release_color,
                ballColorRelease);
        ballColorPress =
                attributes.getColor(R.styleable.materialAnimatedSwitch_ball_press_color, ballColorPress);
        pressIcon = BitmapFactory.decodeResource(getResources(),
                attributes.getResourceId(R.styleable.materialAnimatedSwitch_icon_press,
                        R.drawable.tack_save_button_32_blue));
        releaseIcon = BitmapFactory.decodeResource(getResources(),
                attributes.getResourceId(R.styleable.materialAnimatedSwitch_icon_release,
                        R.drawable.tack_save_button_32_white));
        ballRadius = (int) attributes.getDimension(R.styleable.materialAnimatedSwitch_ball_radius,
                getResources().getDimension(R.dimen.ball_radius));

        switchWidth = (int) attributes.getDimension(R.styleable.materialAnimatedSwitch_switch_width,
                getResources().getInteger(R.integer.default_switch_width));
        switchHeight = (int) attributes.getDimension(R.styleable.materialAnimatedSwitch_switch_height,
                getResources().getInteger(R.integer.default_switch_height));

        iconSize = (int) attributes.getDimension(R.styleable.materialAnimatedSwitch_icon_size,
                getResources().getDimension(R.dimen.icon_size));


        margin = (int) attributes.getDimension(R.styleable.materialAnimatedSwitch_margin,
                getResources().getDimension(R.dimen.margin));

    }

    private void initObservables() {
        ballFinishObservable = new BallFinishObservable();
        ballMoveObservable = new BallMoveObservable();
        ballFinishObservable.addObserver(new BallStateObserver());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;
        if (getResources().getInteger(R.integer.default_switch_width) == switchWidth) {
            width = Utils.dpToPx(switchWidth, getResources());
        } else {
            width = switchWidth;
        }

        if (getResources().getInteger(R.integer.default_switch_height) == switchHeight) {
            height = Utils.dpToPx(switchHeight, getResources());
        } else {
            height = switchHeight;
        }
        setMeasuredDimension(width, height);
        basePainter.onSizeChanged(height, width);
        ballPainter.onSizeChanged(height, width);
        iconPressPainter.onSizeChanged(height, width);
        iconReleasePainter.onSizeChanged(height, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        basePainter.draw(canvas);
        ballPainter.draw(canvas);
        iconPressPainter.draw(canvas);
        iconReleasePainter.draw(canvas);
        invalidate();
    }

    private void setState(MaterialAnimatedSwitchState materialAnimatedSwitchState) {
        basePainter.setState(materialAnimatedSwitchState);
        ballPainter.setState(materialAnimatedSwitchState);
        iconPressPainter.setState(materialAnimatedSwitchState);
        iconReleasePainter.setState(materialAnimatedSwitchState);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (isClickable) {
                    doActionDown();
                }
                return true;
            default:
                return false;
        }
    }

    private void doActionDown() {
        if (actualState.equals(MaterialAnimatedSwitchState.RELEASE) || actualState.equals(
                MaterialAnimatedSwitchState.INIT) || actualState == null) {
            actualState = MaterialAnimatedSwitchState.PRESS;
            setState(actualState);
        } else {
            actualState = MaterialAnimatedSwitchState.RELEASE;
            setState(actualState);
        }
        playSoundEffect(SoundEffectConstants.CLICK);
    }

    public boolean isChecked() {
        return actualState.equals(MaterialAnimatedSwitchState.PRESS);
    }

    public void toggle() {
        if (isClickable) {
            doActionDown();
        }
    }

    /**
     * Avoid click when ball is still in movement
     * Call listener when state is updated
     */
    private class BallStateObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            BallFinishObservable ballFinishObservable = (BallFinishObservable) observable;
            isClickable = !ballFinishObservable.getState().equals(BallFinishObservable.BallState.MOVE);

            if (ballFinishObservable.getState().equals(BallFinishObservable.BallState.PRESS)) {
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(true);
                }
            } else if (ballFinishObservable.getState().equals(BallFinishObservable.BallState.RELEASE)) {
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.onCheckedChanged(false);
                }
            }
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {

        void onCheckedChanged(boolean isChecked);
    }
}