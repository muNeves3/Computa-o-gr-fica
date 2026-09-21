package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class ExercicioTeardrop {
    public static void printTeardrop() {
        // linhas do teardrop
        // ponto + a esquerda: (50, 100), ponto + a direita: (150, 100), ponto + em
        // cima: (100, 250)
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor3f(0.0f, 0.0f, 0.0f);
        GL11.glVertex2f(100f, 250f);
        GL11.glVertex2f(50f, 100f);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor3f(0.0f, 0.0f, 0.0f);
        GL11.glVertex2f(100f, 250f);
        GL11.glVertex2f(150f, 100f);
        GL11.glEnd();

        // semicirculo na parte de baixo do teardrop
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glColor3f(0.0f, 0.0f, 0.0f);
        for (int i = 0; i <= 180; i++) {
            double angle = Math.toRadians(i);
            float x = (float) (100 + 50 * Math.cos(angle));
            float y = (float) (100 - 50 * Math.sin(angle));
            GL11.glVertex2f(x, y);
        }
        GL11.glEnd();
    }

    public static void main(String[] args) {
        int windowWidth = 500;
        int windowHeight = 500;

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar o GLFW");
        }

        long window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Super teardrop maneiro", MemoryUtil.NULL,
                MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Falha ao criar a janela");
        }

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

            GL11.glLineWidth(2.0f);

            printTeardrop();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
