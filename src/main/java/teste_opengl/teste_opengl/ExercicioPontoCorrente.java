package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class ExercicioPontoCorrente {
    
    private static class Point {
        float x, y;
        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private static List<Point> points = new ArrayList<>();
    private static Point pontoCorrente = null; 

    private static void selecionarPontoMaisProximo(float x, float y) {
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

        if (closestPoint != null) {
            pontoCorrente = closestPoint;
        }
    }

    public static void main(String[] args) {
        int windowWidth = 500;
        int windowHeight = 500;

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar o GLFW");
        }

        long window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Ponto Corrente OpenGL", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Falha ao criar a janela");
        }

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallbackI() {
            @Override
            public void invoke(long win, int button, int action, int mods) {
                if (action == GLFW.GLFW_PRESS) {
                    double[] xPos = new double[1];
                    double[] yPos = new double[1];
                    GLFW.glfwGetCursorPos(win, xPos, yPos);
                    
                    float glX = (float) xPos[0];
                    float glY = (float) (windowHeight - yPos[0]);

                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        Point novoPonto = new Point(glX, glY);
                        points.add(novoPonto);
                        pontoCorrente = novoPonto;
                    } 
                    else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        selecionarPontoMaisProximo(glX, glY);
                    }
                }
            }
        });

        // Callback de Teclado
        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallbackI() {
            @Override
            public void invoke(long win, int key, int scancode, int action, int mods) {
                if ((action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) && pontoCorrente != null) {
                    float velocidade = 5.0f;
                    
                    switch (key) {
                        case GLFW.GLFW_KEY_UP:
                            pontoCorrente.y += velocidade;
                            break;
                        case GLFW.GLFW_KEY_DOWN:
                            pontoCorrente.y -= velocidade;
                            break;
                        case GLFW.GLFW_KEY_LEFT:
                            pontoCorrente.x -= velocidade;
                            break;
                        case GLFW.GLFW_KEY_RIGHT:
                            pontoCorrente.x += velocidade;
                            break;
                    }
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

        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f); // Fundo branco

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            
            GL11.glPointSize(5.0f);
            GL11.glBegin(GL11.GL_POINTS);
                
                for (Point p : points) {
                    if (p == pontoCorrente) {
                        GL11.glColor3f(1.0f, 0.0f, 0.0f); 
                    } else {
                        GL11.glColor3f(0.0f, 0.0f, 0.0f); 
                    }
                    GL11.glVertex2f(p.x, p.y);
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