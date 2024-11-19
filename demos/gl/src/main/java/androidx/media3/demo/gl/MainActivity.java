/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package androidx.media3.demo.gl;

import static androidx.media3.common.util.Assertions.checkNotNull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Assertions;
import androidx.media3.common.util.GlUtil;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.decoder.vp9.LibvpxVideoRenderer;
import androidx.media3.exoplayer.DecoderCounters;
import androidx.media3.exoplayer.DecoderReuseEvaluation;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.Renderer;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback;
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.util.EventLogger;
import androidx.media3.exoplayer.video.VideoRendererEventListener;
import androidx.media3.ui.PlayerView;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;


public class MainActivity extends Activity {

  private MainGLSurfaceView mainGLSurfaceView;
  private PlayerView playerView;
  private ExoPlayer player;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initialize the custom GLSurfaceView
    mainGLSurfaceView = new MainGLSurfaceView(this);

    // Set the content view from the layout resource
    setContentView(R.layout.main_activity);

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
          public void onVideoInputFormatChanged(
              Format format, @Nullable DecoderReuseEvaluation decoderReuseEvaluation) {
            Log.d("TAG", "Input format changed: " + format.bitrate);
          }
        },
            /* maxDroppedFramesToNotify= */ 50
        ),
        new MediaCodecAudioRenderer(this, MediaCodecSelector.DEFAULT)
    };

    // Build ExoPlayer with custom renderer
    player = new ExoPlayer.Builder(this).setRenderersFactory((eventHandler, videoRendererEventListener,
            audioRendererEventListener, textRendererOutput, metadataRendererOutput) -> renderers)
        .build();

    player.setRepeatMode(Player.REPEAT_MODE_ALL);
    // Create a MediaItem from a raw resource
    MediaItem mediaItem = MediaItem.fromUri(
        RawResourceDataSource.buildRawResourceUri(R.raw.sample)
    );


    // Set the MediaItem to the player
    player.setMediaItem(mediaItem);

    // Find the PlayerView in the layout
    playerView = findViewById(R.id.player_view);

    // Attach the player to the PlayerView
    playerView.setPlayer(player);

    // Prepare and play the video
    player.prepare();
    player.play();
  }



}
