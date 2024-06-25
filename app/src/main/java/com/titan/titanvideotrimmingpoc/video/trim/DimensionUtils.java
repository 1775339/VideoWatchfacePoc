package com.titan.titanvideotrimmingpoc.video.trim;





import static com.titan.titanvideotrimmingpoc.video.trim.ApplicationHolder.*;

import android.content.Context;
import android.util.TypedValue;

public class DimensionUtils {

    public static float applyDimension(float value, Unit unit) {
        return TypedValue.applyDimension(unit.value, value, sApplication.getResources().getDisplayMetrics());
    }

    public enum Unit {
        PX(TypedValue.COMPLEX_UNIT_PX),
        DIP(TypedValue.COMPLEX_UNIT_DIP),
        SP(TypedValue.COMPLEX_UNIT_SP),
        PT(TypedValue.COMPLEX_UNIT_PT),
        IN(TypedValue.COMPLEX_UNIT_IN),
        MM(TypedValue.COMPLEX_UNIT_MM);
        private int value;

        Unit(int value) {
            this.value = value;
        }
    }

}