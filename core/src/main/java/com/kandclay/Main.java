package com.kandclay;

import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.kandclay.handlers.SpineAnimationHandler;
import com.kandclay.managers.AudioManager;
import com.kandclay.managers.ConfigurationManager;
import com.kandclay.managers.MyAssetManager;
import com.kandclay.screens.DiamondScreen;
import com.kandclay.screens.PenetrationTestScreen;
import com.kandclay.utils.Constants;
import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;

import static com.kandclay.utils.Shaders.createTransition;

public class Main extends ManagedGame<ManagedScreen, ScreenTransition> {

    private MyAssetManager assetManager;
    private AudioManager audioManager;
    private ConfigurationManager configManager;
    private SpineAnimationHandler spineAnimationHandler;
    private PolygonSpriteBatch batch;

    @Override
    public void create() {
        super.create();

        batch = new PolygonSpriteBatch();
        configManager = ConfigurationManager.getInstance();
        assetManager = MyAssetManager.getInstance();
        audioManager = AudioManager.getInstance();
        spineAnimationHandler = new SpineAnimationHandler();

        loadInitialAssets();
        // this.screenManager.pushScreen(new MainMenuScreen(), createTransition());
        this.screenManager.pushScreen(new DiamondScreen(), createTransition());
    }

    private void loadInitialAssets() {
        assetManager.load(Constants.Skin.JSON, Skin.class);
        assetManager.load(Constants.MainMenuScreen.ATLAS, TextureAtlas.class);
        assetManager.load(Constants.TrailDot.ATLAS, TextureAtlas.class);
        assetManager.load(Constants.MainAnimationScreen.YellowCoin.ATLAS, TextureAtlas.class);
        assetManager.load(Constants.MainAnimationScreen.RedCoin.ATLAS, TextureAtlas.class);
        assetManager.load(Constants.Background.PATH_1, Texture.class);
        assetManager.load(Constants.MainAnimationScreen.ATLAS, TextureAtlas.class);
        assetManager.load(Constants.Minimap.PATH, Texture.class);
        assetManager.load(Constants.TestScreen.ATLAS, TextureAtlas.class);
        assetManager.load(Constants.Font.FONT_FNT, BitmapFont.class);
        assetManager.load(Constants.DiamondScreen.ATLAS, TextureAtlas.class);

        assetManager.finishLoading();
        addFontsToSkin();
    }

    private void addFontsToSkin() {
        Skin skin = assetManager.get(Constants.Skin.JSON, Skin.class);
        BitmapFont customFont = assetManager.get(Constants.Font.FONT_FNT, BitmapFont.class);
        skin.add(Constants.Font.FONT, customFont, BitmapFont.class);

        Label.LabelStyle customLabelStyle = new Label.LabelStyle();
        customLabelStyle.font = customFont;
        skin.add(Constants.Font.LABEL, customLabelStyle);

        // Add ButtonStyle
        TextButton.TextButtonStyle customButtonStyle = new TextButton.TextButtonStyle();
        customButtonStyle.font = customFont;
        customButtonStyle.up = skin.getDrawable("default-rect");
        customButtonStyle.down = skin.getDrawable("default-rect-down");
        customButtonStyle.checked = skin.getDrawable("default-rect");
        skin.add(Constants.Font.BUTTON, customButtonStyle);
    }

    @Override
    public void render() {
        screenManager.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0)
            screenManager.resize(width, height);
    }

    @Override
    public void pause() {
        screenManager.pause();
    }

    @Override
    public void resume() {
        screenManager.resume();
    }

    @Override
    public void dispose() {
        if (screenManager != null) {
            screenManager.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose();
        }
        if (audioManager != null) {
            audioManager.dispose();
        }
    }

    public PolygonSpriteBatch getBatch() {
        return batch;
    }

    public MyAssetManager getAssetManager() {
        return assetManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public SpineAnimationHandler getSpineAnimationHandler() {
        if (spineAnimationHandler != null)
            return spineAnimationHandler;
        return null;
    }
}

