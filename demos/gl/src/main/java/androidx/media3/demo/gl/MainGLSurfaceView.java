package androidx.media3.demo.gl;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.decoder.vp9.LibvpxVideoRenderer;
import androidx.media3.exoplayer.DecoderCounters;
import androidx.media3.exoplayer.DecoderReuseEvaluation;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.video.VideoRendererEventListener;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.jetbrains.annotations.Nullable;

public class MainGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    Context context;
    SurfaceTexture videoSurfaceTexture;
    VideoPlane videoPlane;
    LibvpxVideoRenderer libvpxVideoRenderer;
    ExoPlayer player;

    public MainGLSurfaceView(Context context) {
        super(context);
        this.context = context;
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setWillNotDraw(false);
    }



    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        videoPlane = new VideoPlane();
        this.videoSurfaceTexture = new SurfaceTexture(videoPlane.getTextureId());
        initializePlayer();
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(1f, 1f, 1f, 1.0f);
        videoSurfaceTexture.updateTexImage();
        videoPlane.draw();
    }

    @Override
    public void onPause() {
        super.onPause();


    }
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private void initializePlayer() {
        ((Activity)this.context).runOnUiThread(() -> {


        androidx.media3.exoplayer.Renderer[] renderers = new androidx.media3.exoplayer.Renderer[] {
            new LibvpxVideoRenderer(
                /* allowedJoiningTimeMs= */ 5000,
                /* eventHandler= */ new Handler(Looper.getMainLooper()),
                /* eventListener= */ new VideoRendererEventListener() {
                @Override
                public void onVideoEnabled(DecoderCounters counters) {
                    Log.d("TAG", "Video enabled");
                }

                @Override
                public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                    Log.d("TAG", "Decoder initialized: " + decoderName);
                }

                @Override
                public void onVideoInputFormatChanged(Format format, @Nullable DecoderReuseEvaluation decoderReuseEvaluation) {
                    Log.d("TAG", "Input format changed: " + format.sampleMimeType);
                }
            },
                /* maxDroppedFramesToNotify= */ 50
            ),
            new MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT)
        };

        // Build ExoPlayer with custom renderer
        player = new ExoPlayer.Builder(context)
            .setRenderersFactory((eventHandler, videoRendererEventListener,
                audioRendererEventListener, textRendererOutput, metadataRendererOutput) -> renderers)
            .build();

        player.setRepeatMode(Player.REPEAT_MODE_ALL);

        // Create media source for VP9 WebM
//        MediaSource mediaSource = new ProgressiveMediaSource.Factory(
//            new DefaultDataSource.Factory(context))
//            .createMediaSource(MediaItem.fromUri(
//                Uri.parse("https://rotato.netlify.app/alpha-demo/movie-webm.webm")));
//        player.setMediaSource(mediaSource);
            MediaItem mediaItem = MediaItem.fromUri(
                RawResourceDataSource.buildRawResourceUri(R.raw.sample)
            );
            player.setMediaItem(mediaItem);

        // Create surface and set it
        Surface surface = new Surface(videoSurfaceTexture);
        player.setVideoSurface(surface);

        // Prepare and start playback
        player.prepare();
        player.setPlayWhenReady(true);
        });
    }
}