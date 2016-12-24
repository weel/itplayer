/*
 * This file is part of ITPlayer.
 *
 * Copyright (C) 2016 Iwan Timmer
 *
 * ITPlayer is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ITPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ITPlayer; if not, see <http://www.gnu.org/licenses/>.
 */

package nl.itimmer.itplayer.player;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.VideoFragment;
import android.support.v17.leanback.app.VideoFragmentGlueHost;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.io.IOException;

import nl.itimmer.itplayer.Browser;
import nl.itimmer.itplayer.Config;
import nl.itimmer.itplayer.MediaFile;
import nl.itimmer.itplayer.R;

public class PlayerActivity extends Activity {

    private static final String TAG = "PlayerActivity";

    public static final String MEDIA = "MEDIA";

    private SimpleExoPlayerView videoView;
    private SimpleExoPlayer player;
    private ExoPlayerGlue glueHelper;

    private MediaFile media;
    private Browser browser;

    private VideoFragment videoFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        media = (MediaFile) getIntent().getSerializableExtra(MEDIA);

        videoFragment = (VideoFragment) getFragmentManager().findFragmentById(R.id.playback_fragment);
        VideoFragmentGlueHost glueHost = new VideoFragmentGlueHost(videoFragment);

        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        player = ExoPlayerFactory.newSimpleInstance(getBaseContext(), trackSelector, loadControl);
        glueHelper = new ExoPlayerGlue(player, getBaseContext());
        glueHelper.setHost(glueHost);

        try {
            browser = Browser.getInstance(Config.mountDirectory);
            DataSource.Factory dataSourceFactory = new NfsDataSourceFactory(browser.getContext());
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource = new ExtractorMediaSource(Uri.parse("nfs://" + media.getSize() + "/" + media.getPath()), dataSourceFactory, extractorsFactory, null, null);

            player.prepare(videoSource);
            player.setPlayWhenReady(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
    }

}