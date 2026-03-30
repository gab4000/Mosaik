package fr.gab400.mosaik.grid;

import dev.xernas.microscope.helper.PathHelper;
import dev.xernas.photon.api.shader.Shader;
import dev.xernas.photon.exceptions.PhotonException;
import dev.xernas.photon.utils.ShaderResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class Utils {

    public static @Nullable Grid.Cell.Border getBorderAt(Grid.Cell cell, float x, float y) {
        Grid.Cell.Border border;

        if (checkTrianglePos(new float[]{x, y}, new float[]{-0.5f, 0.5f}, new float[]{0.5f, 0.5f}, new float[]{0, 0})) border = cell.getDivision()[0];
        else if (checkTrianglePos(new float[]{x, y}, new float[]{-0.5f, -0.5f}, new float[]{-0.5f, 0.5f}, new float[]{0, 0})) border = cell.getDivision()[1];
        else if (checkTrianglePos(new float[]{x, y}, new float[]{0.5f, -0.5f}, new float[]{-0.5f, -0.5f}, new float[]{0, 0})) border = cell.getDivision()[2];
        else if (checkTrianglePos(new float[]{x, y}, new float[]{0.5f, 0.5f}, new float[]{0.5f, -0.5f}, new float[]{0, 0})) border = cell.getDivision()[3];
        else border = null;

        return border;
    }

    public static boolean checkTrianglePos(float[] point, float[] a, float[] b, float[] c) {
        return (checkPointPos(point, a, b) && checkPointPos(point, c, a) && checkPointPos(point, b , c))
                || (!checkPointPos(point, a, b) && !checkPointPos(point, c, a) && !checkPointPos(point, b , c));
    }

    public static boolean checkPointPos(float[] point, float[] a, float[] b) {
        return (a[0] - point[0]) * (b[1] - point[1]) >= (a[1] - point[1]) * (b[0] - point[0]);
    }

    public static Shader getShaderFromResources() throws PhotonException {
        try {
            Path vertexShaderPath = PathHelper.getResourcePath("shaders/game.vert");
            Path fragmentShaderPath = PathHelper.getResourcePath("shaders/game.frag");
            String vertexSource = PathHelper.getStringOf(vertexShaderPath);
            String fragmentSource = PathHelper.getStringOf(fragmentShaderPath);
            ShaderResource vertexResource = new ShaderResource(vertexShaderPath.getFileName().toString(), vertexSource);
            ShaderResource fragmentResource = new ShaderResource(fragmentShaderPath.getFileName().toString(), fragmentSource);
            return new Shader(vertexResource, fragmentResource);
        } catch (IOException | URISyntaxException e) {
            throw new PhotonException("Failed to load shader files from resources", e);
        }
    }
}
