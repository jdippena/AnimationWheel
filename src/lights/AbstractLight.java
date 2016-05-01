package lights;

import base.Triangle;
import shapes.AbstractShape;

/**
 * @author Jaco
 */
public abstract class AbstractLight {
    float intensity;
    int color;

    public AbstractLight(float intensity, int color) {
        this.intensity = intensity;
        this.color = color;
    }

    public abstract int light(float[][] points, float[] norm, int color);
}
