package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class LeituraObj {
    public static void main(String[] args) {
        int windowWidth = 500;
        int windowHeight = 500;
        String nomeArquivo = args[0];
        Mesh mesh = new Mesh();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar o GLFW");
        }

        long window = GLFW.glfwCreateWindow(1000, 1000, "Teste LWJGL 3", MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Falha ao criar a janela");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);

        GL.createCapabilities();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(windowWidth / 2.0, -windowWidth / 2.0, -windowHeight / 2.0, windowHeight / 2.0, -1000.0, 1000.0);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        mesh.LeituraObj(nomeArquivo);

        GLFW.glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                switch (key) {
                    case GLFW.GLFW_KEY_1 -> mesh.rotacaoX(5.0);
                    case GLFW.GLFW_KEY_2 -> mesh.rotacaoX(-5.0);
                    case GLFW.GLFW_KEY_3 -> mesh.rotateY(5.0);
                    case GLFW.GLFW_KEY_4 -> mesh.rotateY(-5.0);
                    case GLFW.GLFW_KEY_5 -> mesh.rotateZ(5.0);
                    case GLFW.GLFW_KEY_6 -> mesh.rotateZ(-5.0);
                    case GLFW.GLFW_KEY_E -> mesh.escala(1.2, 1.2, 1.2);
                    case GLFW.GLFW_KEY_Q -> mesh.escala(0.8, 0.8, 0.8);
                    case GLFW.GLFW_KEY_UP -> mesh.translacao(0, 5, 0);
                    case GLFW.GLFW_KEY_DOWN -> mesh.translacao(0, -5, 0);
                    case GLFW.GLFW_KEY_LEFT -> mesh.translacao(5, 0, 0);
                    case GLFW.GLFW_KEY_RIGHT -> mesh.translacao(-5, 0, 0);
                    case GLFW.GLFW_KEY_ESCAPE -> GLFW.glfwSetWindowShouldClose(win, true);
                }
            }
        });

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            GL11.glPointSize(8.0f);
            GL11.glColor3f(1.0f, 0.0f, 0.0f);

            mesh.desenharWireframe();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
