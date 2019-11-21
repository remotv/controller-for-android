package tv.remo.android.controller.sdk.utils;

public class ValueUtil {
    public static float map(float input, float inMin, float inMax, float outMin, float outMax, float multiplier) {
        input = range(input, inMin, inMax);
        input *= multiplier;
        inMin *= multiplier;
        inMax *= multiplier;
        float output = (input - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
        return range(output, outMin, outMax);
    }

    public static float range(float input, float min, float max) {
        float output = input;
        if (input < min) {
            output = min;
        } else if (input > max) {
            output = max;
        }
        return output;
    }
}