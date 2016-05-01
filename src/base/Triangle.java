package base;

/**
 * Created by Jaco on 4/14/16.
 *
 * Smaller class for triangles
 */
public class Triangle implements Comparable<Triangle> {
    public float[][] points; // in column-major order
    public int color;
    public boolean emissive = false;
    public float distToCamera;

    /**
     * @param points The points for the triangle in <b>counterclockwise</b> order and column-major
     * */
    public Triangle(float[][] points, int color) {
        this.points = points;
        this.color = color;
    }

    public Triangle(float[][] points, int color, boolean emissive, float distToCamera) {
        this(points, color);
        this.emissive = emissive;
        this.distToCamera = distToCamera;
    }

    @Override
    public int compareTo(Triangle t) {
        return Float.compare(distToCamera, t.distToCamera);
    }
}
