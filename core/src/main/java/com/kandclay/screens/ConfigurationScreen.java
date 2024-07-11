package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kandclay.handlers.SpineAnimationHandler;
import com.kandclay.utils.Constants;
import com.kandclay.utils.ScreenType;
import com.kandclay.utils.TrailDot;

import javax.swing.text.View;

public class ConfigurationScreen extends BaseScreen {
    private Slider volumeSlider;
    private TextButton backButton;
    private TextButton hairColorButton;
    private TextButton coinColorButton;
    private boolean isYellowCoin;
    private SpriteBatch batch;
    private Camera camera;
    private Viewport viewport;
    private Stage stage;

    public ConfigurationScreen() {
        super();
    }

    @Override
    public void show() {

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        stage = new Stage(viewport);
        batch = new SpriteBatch();

        Skin skin = game.getAssetManager().get(Constants.Skin.JSON, Skin.class);
        float savedVolume = game.getConfigManager().getPreference("volume", Constants.Audio.DEFAULT_VOLUME);
        isYellowCoin = game.getConfigManager().getPreference("coinColor", true);


        volumeSlider = new Slider(0, 1, 0.01f, false, skin);
        volumeSlider.setValue(savedVolume);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = volumeSlider.getValue();
                game.getAudioManager().setVolume(volume);
                game.getConfigManager().setPreference("volume", volume);
            }
        });

        backButton = new TextButton("Back", skin, Constants.Font.BUTTON);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // screenManager.setScreen(ScreenType.MENU);
            }
        });

        coinColorButton = new TextButton("Coin Color: " + (isYellowCoin ? "Yellow" : "Red"), skin, Constants.Font.BUTTON);
        coinColorButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isYellowCoin = !isYellowCoin;
                coinColorButton.setText("Coin Color: " + (isYellowCoin ? "Yellow" : "Red"));
                game.getConfigManager().setPreference("coinColor", isYellowCoin);
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(new Label("Options", skin, Constants.Font.LABEL)).padBottom(Constants.UIButtons.PADDING).row();
        table.add(new Label("Volume", skin, Constants.Font.LABEL)).padBottom(Constants.UIButtons.PADDING).row();
        table.add(volumeSlider).width(Constants.UIButtons.SLIDER_WIDTH).padBottom(Constants.UIButtons.PADDING).row();
        table.add(hairColorButton).width(Constants.UIButtons.CONTROL_BUTTON_WIDTH).height(Constants.UIButtons.CONTROL_BUTTON_HEIGHT).padBottom(Constants.UIButtons.PADDING).row();
        table.add(coinColorButton).width(Constants.UIButtons.CONTROL_BUTTON_WIDTH).height(Constants.UIButtons.CONTROL_BUTTON_HEIGHT).padBottom(Constants.UIButtons.PADDING).row();
        table.add(backButton).width(Constants.UIButtons.BACK_BUTTON_WIDTH).height(Constants.UIButtons.CONTROL_BUTTON_HEIGHT).padTop(Constants.UIButtons.PADDING);

        stage.addActor(table);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        clearScreen();

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        stage.act(delta);
        stage.draw();

        TrailDot.renderTrail(batch, viewport);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

