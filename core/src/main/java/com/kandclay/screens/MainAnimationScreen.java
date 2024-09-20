package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.*;

import com.kandclay.utils.Constants;
import com.kandclay.utils.TrailDot;

import java.util.HashMap;

import static com.kandclay.utils.Shaders.createTransition;

public class MainAnimationScreen extends BaseScreen {

    private boolean isYellowCoin;
    private boolean isLooping = true;
    private float speedMultiplier = 1f;
    private float lastSliderValue = 0f;

    private Stage stage;

    private TextureRegion backgroundTexture;
    private Viewport backgroundViewport;

    private Viewport uiViewport;

    private enum AnimationType {
        COIN, BUTTON
    }

    public MainAnimationScreen() {
        super();
    }

    @Override
    public void show() {

        Viewport viewport = new ExtendViewport(Constants.General.EMBED_WIDTH, Constants.General.EMBED_HEIGHT);
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);
        Texture texture = game.getAssetManager().get(Constants.Background.PATH_2, Texture.class);
        backgroundTexture = new TextureRegion(texture);
        backgroundViewport = new ExtendViewport(Constants.General.EMBED_WIDTH, Constants.General.EMBED_HEIGHT);

        uiViewport = new ExtendViewport(Constants.General.EMBED_WIDTH, Constants.General.EMBED_HEIGHT);

        shapeRenderer = new ShapeRenderer();

        isYellowCoin = game.getConfigManager().getPreference("coinColor", true);
        initializeCoinAnimations();
        initializeButtonAnimations();

        Skin skin = game.getAssetManager().get(Constants.Skin.JSON, Skin.class);

        TextButton backButton = new TextButton("Back to Menu", skin, Constants.Font.BUTTON);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getScreenManager().pushScreen(new MainMenuScreen(), createTransition());
            }
        });

        final Slider slider = new Slider(0, 1, 0.01f, false, skin);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!isLooping) {
                    float progress = slider.getValue();
                    float animationDuration = states.get(AnimationType.COIN.ordinal()).getCurrent(0).getAnimation().getDuration();
                    states.get(AnimationType.COIN.ordinal()).getCurrent(0).setTrackTime(progress * animationDuration);
                    if (progress != lastSliderValue) {
                        lastSliderValue = progress;
                        System.out.println("Slider changed: " + slider.getValue() + " Mode: Manual");
                    }
                }
            }
        });

        final TextButton modeButton = new TextButton("Switch to Manual Mode", skin, Constants.Font.BUTTON);
        modeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isLooping = !isLooping;
                System.out.println("Mode changed to: " + (isLooping ? "Automatic" : "Manual"));
                if (isLooping) {
                    modeButton.setText("Switch to Manual Mode");
                    states.get(AnimationType.COIN.ordinal()).setAnimation(0, "animation", true);
                } else {
                    modeButton.setText("Switch to Automatic Mode");
                }
            }
        });

        TextButton changeCoinColorButton = new TextButton("Change Coin Color", skin, Constants.Font.BUTTON);
        changeCoinColorButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                swapCoinColor();
            }
        });

        // New button to swap skins
        TextButton swapSkinsButton = new TextButton("Swap Skins", skin, Constants.Font.BUTTON);
        swapSkinsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                swapSkins();
            }
        });

        Table bottomTable = new Table();
        bottomTable.setFillParent(true);
        bottomTable.bottom();
        bottomTable.add(slider).width(Constants.UIButtons.SLIDER_WIDTH).padBottom(Constants.UIButtons.PADDING);
        bottomTable.row();
        bottomTable.add(modeButton).padBottom(Constants.UIButtons.PADDING);

        Table backButtonTable = new Table();
        backButtonTable.setFillParent(true);
        backButtonTable.bottom().left();
        backButtonTable.add(backButton).width(Constants.UIButtons.BACK_BUTTON_WIDTH).height(Constants.UIButtons.CONTROL_BUTTON_HEIGHT).pad(Constants.UIButtons.PADDING);

        Table topTable = new Table();
        topTable.setFillParent(true);
        topTable.top();
        topTable.add(changeCoinColorButton).pad(Constants.UIButtons.PADDING);
        topTable.row();
        topTable.add(swapSkinsButton).pad(Constants.UIButtons.PADDING);  // Add the new button to the layout

        stage.addActor(bottomTable);
        stage.addActor(backButtonTable);
        stage.addActor(topTable);

        hoverStates = new HashMap<String, Boolean>();
        hoverStates.put(Constants.MainAnimationScreen.BUTTON_1X_NAME, false);
        hoverStates.put(Constants.MainAnimationScreen.BUTTON_2X_NAME, false);
        hoverStates.put(Constants.MainAnimationScreen.BUTTON_3X__NAME, false);

        stage.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                handleHover(x, y);
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                handleClick(x, y);
                return true;
            }
        });

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                Vector2 coords = stage.getViewport().unproject(new Vector2(screenX, screenY));
                TrailDot.createTrailDot(coords.x, coords.y, stage.getViewport());
                return false;
            }
        });

        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void swapCoinColor() {
        float currentStateTime = states.get(AnimationType.COIN.ordinal()).getCurrent(0).getTrackTime();
        isYellowCoin = !isYellowCoin;
        game.getConfigManager().setPreference("coinColor", isYellowCoin);
        swapCoinAnimation(currentStateTime);
    }

    private void configureCoinAnimation(float stateTime, boolean isAddOperation) {
        String atlasPath = isYellowCoin ? Constants.MainAnimationScreen.YellowCoin.ATLAS : Constants.MainAnimationScreen.RedCoin.ATLAS;
        String skeletonPath = isYellowCoin ? Constants.MainAnimationScreen.YellowCoin.JSON : Constants.MainAnimationScreen.RedCoin.JSON;

        if (isAddOperation) {
            skeletons.insert(AnimationType.COIN.ordinal(), game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath));
            states.insert(AnimationType.COIN.ordinal(), game.getSpineAnimationHandler().createAnimationState(skeletons.get(AnimationType.COIN.ordinal())));
        } else {
            skeletons.set(AnimationType.COIN.ordinal(), game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath));
            states.set(AnimationType.COIN.ordinal(), game.getSpineAnimationHandler().createAnimationState(skeletons.get(AnimationType.COIN.ordinal())));
        }

        setSkeletonScale(skeletons.get(AnimationType.COIN.ordinal()), stage.getViewport(), Constants.MainAnimationScreen.COIN_WIDTH_PERCENTAGE, Constants.MainAnimationScreen.COIN_HEIGHT_PERCENTAGE);
        setSkeletonPosition(skeletons.get(AnimationType.COIN.ordinal()), stage.getViewport().getWorldWidth() / 2, stage.getViewport().getWorldHeight() / 2);
        states.get(AnimationType.COIN.ordinal()).setAnimation(0, "animation", true);
        states.get(AnimationType.COIN.ordinal()).getCurrent(0).setTrackTime(stateTime);
        states.get(AnimationType.COIN.ordinal()).addListener(new AnimationState.AnimationStateAdapter() {
            @Override
            public void complete(AnimationState.TrackEntry entry) {
                System.out.println("Animation completed");
            }
        });
    }

    private void initializeCoinAnimations() {
        configureCoinAnimation((float) 0.0, true);
    }

    private void swapCoinAnimation(float stateTime) {
        configureCoinAnimation(stateTime, false);
    }

    private void initializeButtonAnimations() {
        String atlasPath = Constants.MainAnimationScreen.ATLAS;
        String skeletonPath = Constants.MainAnimationScreen.JSON;

        skeletons.insert(AnimationType.BUTTON.ordinal(), game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath));
        states.insert(AnimationType.BUTTON.ordinal(), game.getSpineAnimationHandler().createAnimationState(skeletons.get(AnimationType.BUTTON.ordinal())));

        setSkeletonScale(skeletons.get(AnimationType.BUTTON.ordinal()), stage.getViewport(), Constants.MainAnimationScreen.BUTTONS_WIDTH_PERCENTAGE, Constants.MainAnimationScreen.BUTTONS_HEIGHT_PERCENTAGE);
        setSkeletonPosition(skeletons.get(AnimationType.BUTTON.ordinal()), 0, stage.getViewport().getWorldHeight());

        playButtonPressAnimation("1x/pressed", 1f);
    }

    private void handleHover(float x, float y) {
        updateHoverState(x, y, Constants.MainAnimationScreen.BUTTON_1X_NAME, AnimationType.BUTTON.ordinal(), 1, "1x/hoverIn", "1x/hoverOut");
        updateHoverState(x, y, Constants.MainAnimationScreen.BUTTON_2X_NAME, AnimationType.BUTTON.ordinal(), 2, "2x/hoverIn", "2x/hoverOut");
        updateHoverState(x, y, Constants.MainAnimationScreen.BUTTON_3X__NAME, AnimationType.BUTTON.ordinal(), 3, "3x/hoverIn", "3x/hoverOut");
    }

    private void swapSkins() {
        String currentSkin = skeletons.get(AnimationType.BUTTON.ordinal()).getSkin().getName();
        String newSkin = currentSkin.equals("Saturated") ? "Accessible" : "Saturated";
        skeletons.get(AnimationType.BUTTON.ordinal()).setSkin(newSkin);
    }

    private void handleClick(float x, float y) {
        if (isHoveringButton(x, y, Constants.MainAnimationScreen.BUTTON_1X_NAME, AnimationType.BUTTON.ordinal())) {
            playButtonPressAnimation("1x/pressed", 1f);
        } else if (isHoveringButton(x, y, Constants.MainAnimationScreen.BUTTON_2X_NAME, AnimationType.BUTTON.ordinal())) {
            playButtonPressAnimation("2x/pressed", 2f);
        } else if (isHoveringButton(x, y, Constants.MainAnimationScreen.BUTTON_3X__NAME, AnimationType.BUTTON.ordinal())) {
            playButtonPressAnimation("3x/pressed", 3f);
        }
    }

    private void playButtonPressAnimation(final String animationName, final float speed) {
        Gdx.app.log("MainAnimationScreen", "Playing button press animation: " + animationName);
        states.get(AnimationType.BUTTON.ordinal()).setAnimation(4, animationName, false).setListener(new AnimationState.AnimationStateListener() {
            @Override
            public void start(AnimationState.TrackEntry entry) {
            }

            @Override
            public void interrupt(AnimationState.TrackEntry entry) {
            }

            @Override
            public void end(AnimationState.TrackEntry entry) {
            }

            @Override
            public void dispose(AnimationState.TrackEntry entry) {
            }

            @Override
            public void complete(AnimationState.TrackEntry entry) {
                Gdx.app.log("MainAnimationScreen", "Animation complete: " + animationName);
                speedMultiplier = speed;
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
            }
        });
    }

    @Override
    protected Rectangle getButtonBounds(String buttonName, int pos) {
        String bgSlotName = "bg-" + buttonName;
        return getRectangle(buttonName, bgSlotName, skeletons.get(pos));
    }

    @Override
    public void render(float delta) {
        clearScreen();

        if (isLooping) {
            states.get(AnimationType.COIN.ordinal()).update(delta * speedMultiplier);
        }

        states.get(AnimationType.COIN.ordinal()).apply(skeletons.get(AnimationType.COIN.ordinal()));
        skeletons.get(AnimationType.COIN.ordinal()).updateWorldTransform(Skeleton.Physics.update);

        states.get(AnimationType.BUTTON.ordinal()).update(delta);
        states.get(AnimationType.BUTTON.ordinal()).apply(skeletons.get(AnimationType.BUTTON.ordinal()));
        skeletons.get(AnimationType.BUTTON.ordinal()).updateWorldTransform(Skeleton.Physics.update);

        // Render background
        backgroundViewport.apply();
        game.getBatch().setProjectionMatrix(backgroundViewport.getCamera().combined);
        game.getBatch().begin();
        game.getBatch().draw(backgroundTexture, 0, 0, backgroundViewport.getWorldWidth(), backgroundViewport.getWorldHeight());
        game.getBatch().end();

        // Render content
        stage.getViewport().apply();
        game.getBatch().setProjectionMatrix(stage.getCamera().combined);
        game.getBatch().begin();
        skeletonRenderer.draw(game.getBatch(), skeletons.get(AnimationType.COIN.ordinal()));
        skeletonRenderer.draw(game.getBatch(), skeletons.get(AnimationType.BUTTON.ordinal()));
        game.getBatch().end();

        uiViewport.apply();
        game.getBatch().setProjectionMatrix(uiViewport.getCamera().combined);
        game.getBatch().begin();
        stage.act(delta);
        stage.draw();
        game.getBatch().end();

        TrailDot.renderTrail(game.getBatch(), stage.getViewport());

        // Render debug bounds
        // renderDebug();
    }

    private void renderDebug() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        drawDebugBounds(Constants.MainAnimationScreen.BUTTON_1X_NAME);
        drawDebugBounds(Constants.MainAnimationScreen.BUTTON_2X_NAME);
        drawDebugBounds(Constants.MainAnimationScreen.BUTTON_3X__NAME);
        shapeRenderer.end();
    }

    private void drawDebugBounds(String buttonName) {
        Rectangle bounds = getButtonBounds(buttonName, AnimationType.BUTTON.ordinal());
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        backgroundViewport.update(width, height, true);
        uiViewport.update(width, height, true);

        setSkeletonScale(skeletons.get(AnimationType.COIN.ordinal()), stage.getViewport(), Constants.MainAnimationScreen.COIN_WIDTH_PERCENTAGE, Constants.MainAnimationScreen.COIN_HEIGHT_PERCENTAGE);  // Adjust the percentages as needed
        setSkeletonPosition(skeletons.get(AnimationType.COIN.ordinal()), stage.getViewport().getWorldWidth() / 2, stage.getViewport().getWorldHeight() / 2);

        setSkeletonScale(skeletons.get(AnimationType.BUTTON.ordinal()), stage.getViewport(), Constants.MainAnimationScreen.BUTTONS_WIDTH_PERCENTAGE, Constants.MainAnimationScreen.BUTTONS_HEIGHT_PERCENTAGE);  // Adjust the percentages as needed
        setSkeletonPosition(skeletons.get(AnimationType.BUTTON.ordinal()), 0, stage.getViewport().getWorldHeight());
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

