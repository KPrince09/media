package androidx.media3.demo.gl;

import android.opengl.GLES30;
import android.opengl.GLES11Ext;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VideoPlane {
  private static final String VERTEX_SHADER =
      "uniform mat4 uMVPMatrix;\n" +
          "uniform mat4 uSTMatrix;\n" +
          "attribute vec4 aPosition;\n" +
          "attribute vec4 aTextureCoord;\n" +
          "varying vec2 vTextureCoord;\n" +
          "void main() {\n" +
          "    gl_Position = uMVPMatrix * aPosition;\n" +
          "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
          "}\n";

  private static final String FRAGMENT_SHADER =
      "#extension GL_OES_EGL_image_external : require\n" +
          "precision mediump float;\n" +
          "varying vec2 vTextureCoord;\n" +
          "uniform samplerExternalOES sTexture;\n" +
          "void main() {\n" +
          "    vec4 color = texture2D(sTexture, vTextureCoord);\n" +
          "    float alpha = color.a;\n" +
          "    vec3 rgb = color.rgb;\n" +
          "    gl_FragColor = vec4(rgb, alpha);\n" +
          "}\n";

  private final int[] textureID = new int[1];
  private final FloatBuffer vertexBuffer;
  private final FloatBuffer textureBuffer;
  private final int program;
  private final int positionHandle;
  private final int textureHandle;
  private final int mvpMatrixHandle;
  private final int stMatrixHandle;
  private final float[] mvpMatrix = new float[16];
  private final float[] stMatrix = new float[16];

  private static final float[] VERTEX_DATA = {
      // X, Y, Z, W
      -1.0f, -0.3f, 0.0f, 1.0f,
      1.0f, -0.3f, 0.0f, 1.0f,
      -1.0f,  0.3f, 0.0f, 1.0f,
      1.0f,  0.3f, 0.0f, 1.0f,
  };

  private static final float[] TEXTURE_DATA = {
      0.0f, 1.0f,
      1.0f, 1.0f,
      0.0f, 0.0f,
      1.0f, 0.0f,
  };

  public VideoPlane() {
    vertexBuffer = createFloatBuffer(VERTEX_DATA);
    textureBuffer = createFloatBuffer(TEXTURE_DATA);

    // Create texture
    GLES30.glGenTextures(1, textureID, 0);
    GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID[0]);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
    GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

    // Create program
    program = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);

    // Get handles
    positionHandle = GLES30.glGetAttribLocation(program, "aPosition");
    textureHandle = GLES30.glGetAttribLocation(program, "aTextureCoord");
    mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
    stMatrixHandle = GLES30.glGetUniformLocation(program, "uSTMatrix");

    Matrix.setIdentityM(mvpMatrix, 0);
    Matrix.setIdentityM(stMatrix, 0);
  }


  public void draw() {
    GLES30.glUseProgram(program);

    // Enable blending for alpha
    GLES30.glEnable(GLES30.GL_BLEND);
    GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);

    GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
    GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID[0]);

    // Set the vertex and texture coordinates
    GLES30.glVertexAttribPointer(positionHandle, 4, GLES30.GL_FLOAT, false, 0, vertexBuffer);
    GLES30.glVertexAttribPointer(textureHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);

    GLES30.glEnableVertexAttribArray(positionHandle);
    GLES30.glEnableVertexAttribArray(textureHandle);

    // Set the transformation matrices
    GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
    GLES30.glUniformMatrix4fv(stMatrixHandle, 1, false, stMatrix, 0);

    // Draw the plane
    GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

    // Clean up
    GLES30.glDisableVertexAttribArray(positionHandle);
    GLES30.glDisableVertexAttribArray(textureHandle);
    GLES30.glDisable(GLES30.GL_BLEND);
  }

  public int getTextureId() {
    return textureID[0];
  }

  private FloatBuffer createFloatBuffer(float[] data) {
    ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);
    bb.order(ByteOrder.nativeOrder());
    FloatBuffer buffer = bb.asFloatBuffer();
    buffer.put(data);
    buffer.position(0);
    return buffer;
  }

  private int createProgram(String vertexSource, String fragmentSource) {
    int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
    int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);

    int program = GLES30.glCreateProgram();
    GLES30.glAttachShader(program, vertexShader);
    GLES30.glAttachShader(program, fragmentShader);
    GLES30.glLinkProgram(program);

    return program;
  }

  private int loadShader(int type, String shaderCode) {
    int shader = GLES30.glCreateShader(type);
    GLES30.glShaderSource(shader, shaderCode);
    GLES30.glCompileShader(shader);
    return shader;
  }
}

