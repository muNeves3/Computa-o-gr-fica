package teste_opengl.teste_opengl;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;


public class AtividadeDino {
    private record Point(float x, float y) {
    }

    private static class Nucleo {
        int nucleo;
        List<Point> pontos = new ArrayList<>();

        public Nucleo(int nucleo) {
            this.nucleo = nucleo;
        }

        public void addPonto(Point ponto) {
            pontos.add(ponto);
        }
    }

    public static int qtdNucleos;
    public static int qtdLinhas;
    private final static List<Nucleo> nucleos = new ArrayList<>();

    public static void LerArquivoDino() {
        String arquivo = "dino.dat";

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(arquivo))) {
            String linha;
            int counter = 0;
            int qtdLinhasNucleoAtual = 0;
            Nucleo nucleoAtual = null;
            while ((linha = br.readLine()) != null) {
                switch (counter) {
                    case 0 -> qtdNucleos = Integer.parseInt(linha.trim());
                    default -> {
                        String[] coordenadas = linha.trim().split(" ");
                        if (coordenadas.length == 1) {
                            int linhasNucleo = Integer.parseInt(coordenadas[0]);
                            nucleos.add(new Nucleo(linhasNucleo));
                            nucleoAtual = nucleos.get(nucleos.size() - 1);
                        }
                        if (coordenadas.length == 2) {
                            float x = Float.parseFloat(coordenadas[0]);
                            float y = Float.parseFloat(coordenadas[1]);
                            // pontos.add(new Point(x, y));
                            if (nucleoAtual != null) {
                                nucleoAtual.addPonto(new Point(x, y));
                                if(qtdLinhasNucleoAtual < nucleos.get(nucleos.size() - 1).nucleo - 1){
                                    qtdLinhasNucleoAtual++;
                                } else {
                                    qtdLinhasNucleoAtual = 0;
                                    nucleoAtual = null;
                                }
                            }
                        }
                    }
                }
                counter++;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        int windowWidth = 500;
        int windowHeight = 500;

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Falha ao inicializar o GLFW");
        }

        long window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Super dinossauro maneiro", MemoryUtil.NULL, MemoryUtil.NULL);
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

        LerArquivoDino();

        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            
            GL11.glLineWidth(5.0f); 

            for (Nucleo nucleo : nucleos) {
                GL11.glBegin(GL11.GL_LINE_STRIP); 
                    GL11.glColor3f(0.0f, 0.0f, 0.0f); 
                    for (Point ponto : nucleo.pontos) {
                        GL11.glVertex2f(ponto.x, ponto.y); 
                    }
                GL11.glEnd(); 
            }
            
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }

        org.lwjgl.glfw.Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}