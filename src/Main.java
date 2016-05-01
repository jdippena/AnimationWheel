import base.Mat;
import base.Matrix;
import base.Triangle;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lights.AbstractLight;
import lights.AmbientLight;
import lights.PointLight;
import shapes.AbstractShape;
import shapes.Axes;
import shapes.Cube;
import shapes.Tetrahedron;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Main extends Application implements EventHandler<KeyEvent> {
    private int width = 800;
    private int height = 600;
    private long last;

    Camera camera = new Camera();
    ArrayList<AbstractShape> shapes = new ArrayList<>();
    ArrayList<AbstractLight> lights = new ArrayList<>();
    Matrix.Builder builder = new Matrix.Builder();
    PriorityQueue<Triangle> drawTriangles = new PriorityQueue<>(100);
    private float angle = 0;

    public static void main(String[] args) throws InterruptedException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("W/S to move Z\nA/D to move X\nR/F to move Y\nQ/E to rotate around Y axis\nC/X to rotate around X axis\nZ to reset facing towards origin");

        primaryStage.setTitle("AnimationWheel");
        Group root = new Group();
        Scene scene = new Scene(root);
        scene.setOnKeyPressed(this);
        primaryStage.setScene(scene);

        Canvas canvas = new Canvas(width, height);
        GraphicsContext context = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        Tetrahedron t = new Tetrahedron();
        shapes.add(t);

        Cube c=new Cube();
        shapes.add(c);

        Axes a = new Axes();
        shapes.add(a);


        AmbientLight ambientLight = new AmbientLight(0.3f, 0xffffff);
        lights.add(ambientLight);

        Cube lightCube1 = new Cube();
        lightCube1.setEmissive(true);
        float[] lightPos1 = new float[] {0,200,0,1};
        lightCube1.setTransform(new Matrix.Builder().scale(10).translate(lightPos1).build());
        shapes.add(lightCube1);
        PointLight pointLight1 = new PointLight(1, 0xffffff, lightPos1, 500);
        lights.add(pointLight1);

        Cube lightCube2 = new Cube();
        lightCube2.setEmissive(true);
        float[] lightPos2 = new float[] {-20, 5, 0, 1};
        lightCube2.setTransform(new Matrix.Builder().translate(lightPos2).build());
        shapes.add(lightCube2);
        PointLight pointLight2 = new PointLight(1, 0xffffff, lightPos2, 200);
        lights.add(pointLight2);

        camera.setPos(new float[] {0, 0, 100, 1});
        camera.faceTowardsOrigin();


        last = System.nanoTime();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                step(context, now);
            }
        }.start();

        primaryStage.show();
    }

    private void step(GraphicsContext context, long time) {
        // clear canvas to draw again
        context.clearRect(0, 0, width, height);

        angle = (angle + 1f*(time-last)/(16.66666f*1000000)) % 360;
        float[][] tetrahedronMatrix = builder
                .reset()
                .scale(20)
                .translate(0,0,0)
                .rotateY(60)
                .build();
        shapes.get(0).setTransform(tetrahedronMatrix);

        float[][] cubeMatrix = builder
                .reset()
                .scale(20)
                .translate(0,0,-400)
                .rotateY(angle)
                .build();
        shapes.get(1).setTransform(cubeMatrix);

        for (AbstractShape s : shapes) {
            for (Triangle t : s.mesh) {
                // This applies the movements we've made to the shapes
                float[][] points=Mat.matrixPointMult(s.transform, t.points);

                // Cull tris facing away (which solves depth-checking for simple shapes)
                // This needs to be done before applying perspective
                float[] edge1= Mat.subtract(points[0], points[1]);
                float[] edge2= Mat.subtract(points[0], points[2]);
                float[] norm = Mat.cross(edge1, edge2);

                // With perspective instead of isometric, we use this instead of the raw facing
                float[] toTri = Mat.subtract(camera.pos,points[1]);

                if(Mat.dot(norm, toTri)<0) {
                    drawTriangles.add(new Triangle(points, t.color, t.emissive, Mat.magnitude(toTri)));
                }
            }
        }
        for (int i = 0; i < drawTriangles.size(); i++) {
            Triangle t = drawTriangles.poll();
            t = camera.look(t, lights);
            if (t.points != null) {
                double[][] coords = pointsToDisplayPoints(t.points);
                context.setFill(Color.rgb((t.color & 0xff0000) >> 16, (t.color & 0x00ff00) >> 8, t.color & 0x0000ff));
                context.fillPolygon(coords[0], coords[1], 3);
            }
        }
        last = time;
    }

    @Override
    public void handle(KeyEvent event) {
        switch (event.getCode()) {
            case W:
                camera.moveForward(5);
                break;
            case S:
                camera.moveForward(-5);
                break;
            case D:
                camera.moveRight(5);
                break;
            case A:
                camera.moveRight(-5);
                break;
            case E:
                camera.rotateYBy(5);
                break;
            case Q:
                camera.rotateYBy(-5);
                break;
            case R:
                camera.moveUp(5);
                break;
            case F:
                camera.moveUp(-5);
                break;
            case X:
                camera.rotateXBy(5);
                break;
            case C:
                camera.rotateXBy(-5);
                break;
            case Z:
                camera.faceTowardsOrigin();
                break;
        }
    }

    /**
     * @param points Points of a triangle in [-1,1] ready to be painted
     * @return A worldView with two rows, one for x values, the other for y values
     */
    private double[][] pointsToDisplayPoints(float [][] points) {
        int midHeight = height/2;
        int midWidth = width/2;
        double[][] newPoints = new double[2][points.length];
        for (int i = 0; i < points.length; i++) {
            newPoints[0][i] = (points[i][0]*width) + midWidth; // move and set x
            newPoints[1][i] = midHeight - (points[i][1]*height); // move and set y
        }
        return newPoints;
    }

}
