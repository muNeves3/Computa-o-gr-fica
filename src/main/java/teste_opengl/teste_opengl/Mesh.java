package teste_opengl.teste_opengl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2d;

class Vertice {
    double x, y, z;

    public Vertice(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

class Face {
    List<Integer> vertexIndices;
    List<Integer> normalIndices;

    public Face(List<Integer> vertexIndices, List<Integer> normalIndices) {
        this.vertexIndices = vertexIndices;
        this.normalIndices = normalIndices;
    }
}

public class Mesh {
    private Vertice pivo = new Vertice(0.0, 0.0, 0.0);
    private List<Vertice> vertices = new ArrayList<>();
    private List<Vertice> normais = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();

    public void setPivo() {
        if (vertices.isEmpty()) {
            this.pivo = new Vertice(0.0, 0.0, 0.0);
            return;
        }

        double minX = vertices.get(0).x, maxX = vertices.get(0).x;
        double minY = vertices.get(0).y, maxY = vertices.get(0).y;
        double minZ = vertices.get(0).z, maxZ = vertices.get(0).z;

        for (Vertice v : vertices) {
            if (v.x < minX)
                minX = v.x;
            if (v.x > maxX)
                maxX = v.x;

            if (v.y < minY)
                minY = v.y;
            if (v.y > maxY)
                maxY = v.y;

            if (v.z < minZ)
                minZ = v.z;
            if (v.z > maxZ)
                maxZ = v.z;
        }

        double cmx = (maxX + minX) / 2.0;
        double cmy = (maxY + minY) / 2.0;
        double cmz = (maxZ + minZ) / 2.0;

        this.pivo = new Vertice(cmx, cmy, cmz);
    }

    public void LeituraObj(String nomeArquivo) {
        vertices.clear();
        normais.clear();
        faces.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty())
                    continue;

                String[] linhaSplit = linha.split("\\s+");
                switch (linhaSplit[0]) {
                    case "v":
                        this.vertices.add(new Vertice(
                                Double.parseDouble(linhaSplit[1]),
                                Double.parseDouble(linhaSplit[2]),
                                Double.parseDouble(linhaSplit[3])));
                        break;
                    case "vn":
                        this.normais.add(new Vertice(
                                Double.parseDouble(linhaSplit[1]),
                                Double.parseDouble(linhaSplit[2]),
                                Double.parseDouble(linhaSplit[3])));
                        break;
                    case "f":
                        List<Integer> indicesVerticesFace = new ArrayList<>();
                        List<Integer> indicesNormaisFace = new ArrayList<>();

                        for (int i = 1; i < linhaSplit.length; i++) {
                            String[] partes = linhaSplit[i].split("/");
                            indicesVerticesFace.add(Integer.parseInt(partes[0]) - 1);

                            if (partes.length == 3 && !partes[2].isEmpty()) {
                                indicesNormaisFace.add(Integer.parseInt(partes[2]) - 1);
                            }
                        }
                        this.faces.add(new Face(indicesVerticesFace, indicesNormaisFace));
                        break;
                    default:
                        break;
                }
            }
            this.setPivo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double[][] multiplicarMatrizes(double[][] A, double[][] B) {
        double[][] C = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;

        // GL11.glMultMatrix(Rx);
        // GL11.glMultMatrix(Ry);
        // GL11.glMultMatrix(Rz);
    }

    private double[][] matrizTranslacao(double dx, double dy, double dz) {
        return new double[][] {
            {1, 0, 0, dx},
            {0, 1, 0, dy},
            {0, 0, 1, dz},
            {0, 0, 0, 1}
        };
    }

    private double[][] matrizEscala(double ex, double ey, double ez) {
        return new double[][] {
            {ex, 0,  0,  0},
            {0,  ey, 0,  0},
            {0,  0,  ez, 0},
            {0,  0,  0,  1}
        };
    }

    private double[][] matrizRotacaoX(double anguloGraus) {
        double rad = Math.toRadians(anguloGraus);
        double c = Math.cos(rad), s = Math.sin(rad);
        return new double[][] {
            {1, 0,  0, 0},
            {0, c, -s, 0},
            {0, s,  c, 0},
            {0, 0,  0, 1}
        };
    }

    private double[][] matrizRotacaoY(double anguloGraus) {
        double rad = Math.toRadians(anguloGraus);
        double c = Math.cos(rad), s = Math.sin(rad);
        return new double[][] {
            { c, 0, s, 0},
            { 0, 1, 0, 0},
            {-s, 0, c, 0},
            { 0, 0, 0, 1}
        };
    }

    private double[][] matrizRotacaoZ(double anguloGraus) {
        double rad = Math.toRadians(anguloGraus);
        double c = Math.cos(rad), s = Math.sin(rad);
        return new double[][] {
            {c, -s, 0, 0},
            {s,  c, 0, 0},
            {0,  0, 1, 0},
            {0,  0, 0, 1}
        };
    }

    public void aplicarTransformacaoComposta(
        double dx, double dy, double dz,
        double angX, double angY, double angZ,
        double ex, double ey, double ez) {

        double[][] pivo_origem = matrizTranslacao(-pivo.x, -pivo.y, -pivo.z);

        double[][] escala = matrizEscala(ex, ey, ez);

        double[][] rotacao = multiplicarMatrizes(matrizRotacaoZ(angZ),
                        multiplicarMatrizes(matrizRotacaoY(angY), matrizRotacaoX(angX)));

        double[][] volta_pivo = matrizTranslacao(pivo.x + dx, pivo.y + dy, pivo.z + dz);

        double[][] G = multiplicarMatrizes(rotacao, escala);
        double[][] M = multiplicarMatrizes(volta_pivo, multiplicarMatrizes(G, pivo_origem));    

        for (Vertice v : vertices) {
            double x = v.x, y = v.y, z = v.z;
            v.x = M[0][0] * x + M[0][1] * y + M[0][2] * z + M[0][3];
            v.y = M[1][0] * x + M[1][1] * y + M[1][2] * z + M[1][3];
            v.z = M[2][0] * x + M[2][1] * y + M[2][2] * z + M[2][3];
        }

        for (Vertice n : normais) {
            double nx = n.x, ny = n.y, nz = n.z;
            n.x = rotacao[0][0] * nx + rotacao[0][1] * ny + rotacao[0][2] * nz;
            n.y = rotacao[1][0] * nx + rotacao[1][1] * ny + rotacao[1][2] * nz;
            n.z = rotacao[2][0] * nx + rotacao[2][1] * ny + rotacao[2][2] * nz;
        }

        pivo.x += dx;
        pivo.y += dy;
        pivo.z += dz;
    }

    public void desenharWireframe() {
        for (Face face : faces) {
            glBegin(GL_LINE_LOOP);
            for (int idx : face.vertexIndices) {
                Vertice v = vertices.get(idx);
                glVertex2d(v.x, v.y);
            }
            glEnd();
        }
    }

    public Mesh() {
    }

    public List<Vertice> getVertices() {
        return vertices;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public Vertice getPivo() {
        return pivo;
    }
}
