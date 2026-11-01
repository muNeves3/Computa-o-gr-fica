package teste_opengl.teste_opengl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.lwjgl.opengl.GL11.*; // Caso desenhe direto via LWJGL/OpenGL

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
                if (linha.isEmpty() || linha.startsWith("#"))
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

    public void translacao(double dx, double dy, double dz) {
        for (Vertice v : vertices) {
            v.x += dx;
            v.y += dy;
            v.z += dz;
        }

        pivo.x += dx;
        pivo.y += dy;
        pivo.z += dz;
    }

    public void escala(double ex, double ey, double ez) {
        for (Vertice v : vertices) {
            v.x = (v.x - pivo.x) * ex + pivo.x;
            v.y = (v.y - pivo.y) * ey + pivo.y;
            v.z = (v.z - pivo.z) * ez + pivo.z;
        }
    }

    public void rotacaoX(double angulo) {
        double rad = Math.toRadians(angulo);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (Vertice v : vertices) {
            double y0 = v.y - pivo.y;
            double z0 = v.z - pivo.z;

            v.y = (y0 * cos - z0 * sin) + pivo.y;
            v.z = (y0 * sin + z0 * cos) + pivo.z;
        }

        for (Vertice n : normais) {
            double ny = n.y * cos - n.z * sin;
            double nz = n.y * sin + n.z * cos;
            n.y = ny;
            n.z = nz;
        }
    }

    public void rotateY(double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (Vertice v : vertices) {
            double x0 = v.x - pivo.x;
            double z0 = v.z - pivo.z;

            v.x = (z0 * sin + x0 * cos) + pivo.x;
            v.z = (z0 * cos - x0 * sin) + pivo.z;
        }

        for (Vertice n : normais) {
            double nx = n.z * sin + n.x * cos;
            double nz = n.z * cos - n.x * sin;
            n.x = nx;
            n.z = nz;
        }
    }

    public void rotateZ(double angleDegrees) {
        double rad = Math.toRadians(angleDegrees);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (Vertice v : vertices) {
            double x0 = v.x - pivo.x;
            double y0 = v.y - pivo.y;

            v.x = (x0 * cos - y0 * sin) + pivo.x;
            v.y = (x0 * sin + y0 * cos) + pivo.y;
        }

        for (Vertice n : normais) {
            double nx = n.x * cos - n.y * sin;
            double ny = n.x * sin + n.y * cos;
            n.x = nx;
            n.y = ny;
        }
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