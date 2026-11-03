package teste_opengl.teste_opengl;

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

        long window = GLFW.glfwCreateWindow(1000, 1000, "Agora eu sou uma imagem 3D!", MemoryUtil.NULL, MemoryUtil.NULL);
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

        mesh.LeituraObj(nomeArquivo);

        double anguloRotacao = 5.0;
        double fatorTranslacao = 5.0;
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            GL11.glPointSize(8.0f);
            GL11.glColor3f(1.0f, 0.0f, 0.0f);

            double angX = 0.0, angY = 0.0, angZ = 0.0;
            double fatorEscala = 1;
            boolean transformou = false;

            // Eixo X 
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
                angX -= anguloRotacao;
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
                angX += anguloRotacao;
                transformou = true;
            }

            // Eixo Y 
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
                angY -= anguloRotacao;
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
                angY += anguloRotacao;
                transformou = true;
            }

            // Eixo Z 
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Q) == GLFW.GLFW_PRESS) {
                angZ += anguloRotacao;
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_E) == GLFW.GLFW_PRESS) {
                angZ -= anguloRotacao;
                transformou = true;
            }

            // Escala 
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_I) == GLFW.GLFW_PRESS) {
                fatorEscala = 1.02;
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_O) == GLFW.GLFW_PRESS) {
                fatorEscala = 0.98;
                transformou = true;
            }

            // Translação 
            double dx = 0.0, dy = 0.0, dz = 0.0;
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
                dy += fatorTranslacao;
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
                dy -= fatorTranslacao;
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
                dx -= fatorTranslacao;  
                transformou = true;
            }
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
                dx += 5.0;
                transformou = true;
            }

            if (transformou) {
                mesh.aplicarTransformacaoComposta(
                    dx, dy, dz,
                    angX, angY, angZ,
                    fatorEscala, fatorEscala, fatorEscala
                );
            }

            mesh.desenharWireframe();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
