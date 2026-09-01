package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;


public class ExercicioLinhaClique {
    private static record Point(float x, float y) {}
    private static LinkedList<Point> points = new LinkedList<>();
    private static List<List<Point>> lines = new ArrayList<>();

    private static void removeClosestPointAndLine(float x, float y) {
        if (points.isEmpty()) {
            return;
        }

        Point closestPoint = null;
        float closestDistance = Float.MAX_VALUE;

        for (Point point : points) {
            float distance = (float) Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }

        final Point pointToRemove = closestPoint;

        if (closestPoint != null) {
            points.remove(closestPoint);
            lines.removeIf(line -> line.contains(pointToRemove));
        }
    }

    private static List<Point> drawLineBetweenPointAndLastCreated(float x, float y) {
        List<Point> linePoints = new ArrayList<>();
        if (points.isEmpty()) {
            return linePoints;
        }

        Point lastPoint = points.getLast();

        // Calculate steps based on the largest pixel difference (DDA algorithm)
        int steps = (int) Math.max(Math.abs(x - lastPoint.x), Math.abs(y - lastPoint.y));
        int diffX = (int) (x - lastPoint.x);
        int diffY = (int) (y - lastPoint.y);

        // Prevent division by zero if the user double-clicks the exact same pixel
        if (steps == 0) {
            return linePoints;
        }

        for (int i = 0; i <= steps; i++) {
            // Progress goes from 0.0 to 1.0
            float progress = (float) i / steps; 
            
            // Formula: Start + (Progress * Difference)
            float xStep = lastPoint.x + (progress * diffX);
            float yStep = lastPoint.y + (progress * diffY);

            linePoints.add(new Point(xStep, yStep));
        }
        return linePoints;
    }

    public static void main(String[] args) {
        int windowWidth = 500;
        int windowHeight = 500;

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar o GLFW");
        }

        long window = GLFW.glfwCreateWindow(500, 500, "Teste LWJGL 3", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Falha ao criar a janela");
        }

        // clique esquerdo do mouse para adicionar ponto e desenhar linha,
         // clique direito do mouse para remover ponto mais próximo
        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallbackI() {
            @Override
            public void invoke(long win, int button, int action, int mods) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
                    double[] xPos = new double[1];
                    double[] yPos = new double[1];
                    GLFW.glfwGetCursorPos(win, xPos, yPos);
                    float glX = (float) xPos[0];
                    float glY = (float) (windowHeight - yPos[0]);
                    
                    lines.add(drawLineBetweenPointAndLastCreated(glX, glY));
                    points.add(new Point(glX, glY));
                }

                 if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_PRESS)
                {
                    double[] xPos = new double[1];
                    double[] yPos = new double[1];
                    GLFW.glfwGetCursorPos(win, xPos, yPos);
                    float glX = (float) xPos[0];
                    float glY = (float) (windowHeight - yPos[0]);
                    removeClosestPointAndLine(glX, glY);
                }
            }
        });

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        
        GL.createCapabilities();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, windowWidth, 0.0, windowHeight, -1.0, 1.0);
        

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            GL11.glPointSize(5.0f);

            GL11.glBegin(GL11.GL_POINTS);
                GL11.glColor3f(0.0f, 0.0f, 0.0f); 

                for (Point p : ExercicioLinhaClique.points) {
                        GL11.glVertex2f(p.x(), p.y());
                }

                for (List<Point> line : ExercicioLinhaClique.lines) {
                    for (Point p : line) {
                        GL11.glVertex2f(p.x(), p.y());
                    }
                }


            GL11.glEnd();
            
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}