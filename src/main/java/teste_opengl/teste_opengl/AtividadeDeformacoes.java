package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class AtividadeDeformacoes {
    private static class Point {
        private float x;
        private float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    private static List<Point> pontosCurva = new ArrayList<>();
    private static List<Point> pontosTorig = new ArrayList<>();
    private static List<Point> pontosTfinal = new ArrayList<>();
    private static boolean isTransformacao = false;
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

    private static void aplicarDeformacao() {
        if (pontosTorig.size() < 3 || pontosTfinal.size() < 3) {
            return;
        }

        float x1 = pontosTorig.get(0).x();
        float y1 = pontosTorig.get(0).y();
        float x2 = pontosTorig.get(1).x();
        float y2 = pontosTorig.get(1).y();
        float x3 = pontosTorig.get(2).x();
        float y3 = pontosTorig.get(2).y();

        float u1 = pontosTfinal.get(0).x();
        float q1 = pontosTfinal.get(0).y();
        float u2 = pontosTfinal.get(1).x();
        float q2 = pontosTfinal.get(1).y();
        float u3 = pontosTfinal.get(2).x();
        float q3 = pontosTfinal.get(2).y();

        float detM = x1 * (y2 - y3) - y1 * (x2 - x3) + (x2 * y3 - y2 * x3);

        if (Math.abs(detM) < 0.0001f) {
            System.out.println("Erro: Os pontos originais são colineares. Não é possível deformar.");
            pontosTorig.clear();
            pontosTfinal.clear();
            return;
        }

        float detMa = u1 * (y2 - y3) - y1 * (u2 - u3) + (u2 * y3 - y2 * u3);
        float detMb = x1 * (u2 - u3) - u1 * (x2 - x3) + (x2 * u3 - u2 * x3);
        float detMc = x1 * (y2 * u3 - u2 * y3) - y1 * (x2 * u3 - u2 * x3) + u1 * (x2 * y3 - y2 * x3);

        float a = detMa / detM;
        float b = detMb / detM;
        float c = detMc / detM;

        float detMd = q1 * (y2 - y3) - y1 * (q2 - q3) + (q2 * y3 - y2 * q3);
        float detMe = x1 * (q2 - q3) - q1 * (x2 - x3) + (x2 * q3 - q2 * x3);
        float detMf = x1 * (y2 * q3 - q2 * y3) - y1 * (x2 * q3 - q2 * x3) + q1 * (x2 * y3 - y2 * x3);

        float d = detMd / detM;
        float e = detMe / detM;
        float f = detMf / detM;

        for (Point p : pontosCurva) {
            float originalX = p.x();
            float originalY = p.y();

            float novoX = (a * originalX) + (b * originalY) + c;
            float novoY = (d * originalX) + (e * originalY) + f;

            p.setX(novoX);
            p.setY(novoY);
        }

        pontosTorig.clear();
        pontosTfinal.clear();
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

                        if (isTransformacao == true) {
                            if (pontosTorig.size() < 3) {
                                pontosTorig.add(new Point(glX, glY));
                                return;
                            } else {
                                pontosTfinal.add(new Point(glX, glY));
                                if (pontosTfinal.size() == 3) {
                                    isTransformacao = false;
                                    aplicarDeformacao();
                                }
                                return;
                            }
                        }

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

        GLFW.glfwSetKeyCallback(window, (long windowHandle, int key, int scancode, int action, int mods) -> {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                if (key == 86) {
                    isTransformacao = true;
                }
                System.out.println("Tecla pressionada. Código: " + key);
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

            GL11.glPointSize(8.0f);
            GL11.glColor3f(1.0f, 1.0f, 0.0f);
            GL11.glBegin(GL11.GL_POINTS);

            for (Point p : pontosTorig) {
                GL11.glVertex2f(p.x(), p.y());
            }
            GL11.glEnd();

            GL11.glPointSize(8.0f);
            GL11.glColor3f(1.0f, 0.0f, 1.0f);
            GL11.glBegin(GL11.GL_POINTS);

            for (Point p : pontosTfinal) {
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
