package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class ExercicioBSpline {
    private static record Point(float x, float y) {
    }

    private static List<Point> pontosCurva = new ArrayList<>();
    private static final int k = 3;
    private static int draggedPointIndex = -1;

    /*
     * Resolvido em forma de matriz para facilitar :)
     * S(t) = (Pi Pi+1 Pi+2) * (1/2) * |1 -2 1| * |t^2|
     * |-2 2 1| | t |
     * |1 0 0| | 1 |
     * 
     * Dps de calcular fica:
     * S(t) = 1/2*[Pi(t^2 - 2t + 1) + Pi+1(-2t^2+2t+1) + Pi+2(t^2)]
     */

    private static List<Point> SplineCalculado() {
        List<Point> bsCalculada = new ArrayList<>();

        // funciona c/ sliding window de 3 pontos
        for (int i = 0; i < pontosCurva.size() - k + 1; i++) {
            Point pi = pontosCurva.get(i);
            Point pi1 = pontosCurva.get(i + 1);
            Point pi2 = pontosCurva.get(i + 2);

            // qtd pontos proprocional às distâncias entre eles
            float dist1 = (float) Math.hypot(pi1.x() - pi.x(), pi1.y() - pi.y());
            float dist2 = (float) Math.hypot(pi2.x() - pi1.x(), pi2.y() - pi1.y());
            float distanciaTotal = dist1 + dist2;

            int numVertices = (int) (distanciaTotal / 5.0f);
            numVertices = Math.max(5, Math.min(numVertices, 200));
            float step = 1.0f / numVertices;

            for (float t = 0.0f; t <= 1.0f; t += step) {
                float xcalc = (1 / 2.0f)
                        * ((pi.x() * ((t * t) - (2 * t) + 1)) + (pi1.x() * (-2 * (t * t) + (2 * t) + 1))
                                + (pi2.x() * t * t));
                float ycalc = (1 / 2.0f)
                        * ((pi.y() * ((t * t) - (2 * t) + 1)) + (pi1.y() * (-2 * (t * t) + (2 * t) + 1))
                                + (pi2.y() * t * t));

                bsCalculada.add(new Point(xcalc, ycalc));
            }
        }

        return bsCalculada;
    }

    private static Point pontoMaisProximo(float x, float y) {
        if (pontosCurva.isEmpty()) {
            return null;
        }

        Point closestPoint = null;
        float closestDistance = Float.MAX_VALUE;

        for (Point point : pontosCurva) {
            float distance = (float) Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
            if (distance < closestDistance) {
                closestDistance = distance;
                closestPoint = point;
            }
        }

        return closestPoint;
    }

    private static void removePontoMaisProximoClique(float x, float y) {
        Point closestPoint = pontoMaisProximo(x, y);

        if (closestPoint != null) {
            pontosCurva.remove(closestPoint);
        }
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

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallbackI() {
            @Override
            public void invoke(long win, int button, int action, int mods) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    double[] xPos = new double[1];
                    double[] yPos = new double[1];
                    GLFW.glfwGetCursorPos(win, xPos, yPos);
                    float glX = (float) xPos[0];
                    float glY = (float) (windowHeight - yPos[0]);

                    if (action == GLFW.GLFW_PRESS) {
                        Point closesPoint = pontoMaisProximo(glX, glY);
                        int closestIndex = pontosCurva.indexOf(closesPoint);
                        final float DRAG_THRESHOLD = 15.0f;

                        if (closestIndex != -1 &&
                                Math.hypot(pontosCurva.get(closestIndex).x() - glX,
                                        pontosCurva.get(closestIndex).y() - glY) <= DRAG_THRESHOLD) {
                            draggedPointIndex = closestIndex;
                        } else {
                            pontosCurva.add(new Point(glX, glY));
                            draggedPointIndex = pontosCurva.size() - 1;
                        }
                    } else if (action == GLFW.GLFW_RELEASE) {
                        draggedPointIndex = -1;
                    }
                }

                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_PRESS) {
                    double[] xPos = new double[1];
                    double[] yPos = new double[1];
                    GLFW.glfwGetCursorPos(win, xPos, yPos);
                    float glX = (float) xPos[0];
                    float glY = (float) (windowHeight - yPos[0]);

                    removePontoMaisProximoClique(glX, glY);
                }
            }
        });

        GLFW.glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (draggedPointIndex != -1) {
                float glX = (float) xpos;
                float glY = (float) (windowHeight - ypos);

                pontosCurva.set(draggedPointIndex, new Point(glX, glY));
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

            GL11.glPointSize(8.0f);
            GL11.glColor3f(1.0f, 0.0f, 0.0f);
            GL11.glBegin(GL11.GL_POINTS);

            for (Point p : pontosCurva) {
                GL11.glVertex2f(p.x(), p.y());
            }
            GL11.glEnd();

            List<Point> BSpline = SplineCalculado();

            GL11.glColor3f(0.0f, 0.0f, 0.0f);

            GL11.glBegin(GL11.GL_LINE_STRIP);

            for (Point p : BSpline) {
                GL11.glVertex2f(p.x(), p.y());
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
