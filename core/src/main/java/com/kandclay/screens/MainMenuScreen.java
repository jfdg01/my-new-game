package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.*;
import com.esotericsoftware.spine.*;
import com.kandclay.utils.Constants;
import com.kandclay.utils.TrailDot;

import java.util.HashMap;

import static com.kandclay.utils.Shaders.createTransition;

public class MainMenuScreen extends BaseScreen {

    private boolean isInitialAnimationFinished = false;
    private final boolean debugMode = false;
    private boolean minimapIsOn = true;

    private Stage stage;
    private Stage backgroundStage;
    private Stage minimapStage;

    private FrameBuffer fbo;
    private TextureRegion fboRegion;

    private FrameBuffer fboMinimap;
    private TextureRegion fboRegionMinimap;

    private TextureRegion backgroundTexture;
    private TextureRegion minimapTexture;

    private enum AnimationType {
        MENU_1, MENU_2
    }

    public MainMenuScreen() {
        super();
    }

    @Override
    public void show() {
        initializeCommonComponents();
        initializeBackground();
        initializeMainContent();
        initializeMinimap();
        setupInputProcessing();
    }

    private void createFBO(int width, int height) {
        if (fbo != null) {
            fbo.dispose();
        }
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegion = new TextureRegion(fbo.getColorBufferTexture());
        fboRegion.flip(false, true);
    }

    private void createFBOMinimap(int width, int height) {
        if (!minimapIsOn)
            return;
        if (fboMinimap != null) {
            fboMinimap.dispose();
        }
        fboMinimap = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fboRegionMinimap = new TextureRegion(fboMinimap.getColorBufferTexture());
        fboRegionMinimap.flip(false, true);
    }

    private void initializeCommonComponents() {
        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);
        shapeRenderer = new ShapeRenderer();

        hoverStates = new HashMap<String, Boolean>();
        hoverStates.put(Constants.MainMenuScreen.BUTTON_PLAY_NAME, false);
        hoverStates.put(Constants.MainMenuScreen.BUTTON_QUIT_NAME, false);
        hoverStates.put(Constants.MainMenuScreen.BUTTON_STGS_NAME, false);

        initializeAnimations();
    }

    private void initializeBackground() {
        Texture texture = game.getAssetManager().get(Constants.Background.PATH_1, Texture.class);
        backgroundTexture = new TextureRegion(texture);
        Viewport backgroundViewport = new ExtendViewport(Constants.General.EMBED_WIDTH, Constants.General.EMBED_HEIGHT);
        backgroundStage = new Stage(backgroundViewport);
    }

    private void initializeMainContent() {
        Viewport viewport = new ExtendViewport(Constants.General.EMBED_WIDTH, Constants.General.EMBED_HEIGHT);
        stage = new Stage(viewport);
        stage.addListener(createStageInputListener(AnimationType.MENU_1.ordinal()));
    }

    private void initializeMinimap() {
        if (!minimapIsOn)
            return;
        Texture texture = game.getAssetManager().get(Constants.Minimap.PATH, Texture.class);
        minimapTexture = new TextureRegion(texture);
        Viewport minimapViewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        minimapStage = new Stage(minimapViewport);
        minimapStage.addListener(createStageInputListener(AnimationType.MENU_2.ordinal()));

    }

    private void setupInputProcessing() {
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                Vector2 coords = stage.getViewport().unproject(new Vector2(screenX, screenY));
                TrailDot.createTrailDot(coords.x, coords.y, stage.getViewport());

                if (minimapIsOn) {
                    coords = minimapStage.getViewport().unproject(new Vector2(screenX, screenY));
                    TrailDot.createTrailDot(coords.x, coords.y, minimapStage.getViewport());
                }

                return false;
            }
        });
        if (minimapIsOn)
            multiplexer.addProcessor(minimapStage);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private InputListener createStageInputListener(final int skeletonIndex) {
        return new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                if (isInitialAnimationFinished) {
                    handleHover(x, y, skeletonIndex);
                }
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                handleClick(x, y, skeletonIndex);
                return true;
            }
        };
    }

    private void handleHover(float x, float y, int skeletonIndex) {
        updateHoverState(x, y, Constants.MainMenuScreen.BUTTON_PLAY_NAME, skeletonIndex, 1, "Buttons/PlayHoverIn", "Buttons/PlayHoverOut");
        updateHoverState(x, y, Constants.MainMenuScreen.BUTTON_QUIT_NAME, skeletonIndex, 2, "Buttons/QuitHoverIn", "Buttons/QuitHoverOut");
        updateHoverState(x, y, Constants.MainMenuScreen.BUTTON_STGS_NAME, skeletonIndex, 3, "Buttons/SettingsHoverIn", "Buttons/SettingsHoverOut");
    }

    private void handleClick(float x, float y, int skeletonIndex) {
        if (isHoveringButton(x, y, Constants.MainMenuScreen.BUTTON_PLAY_NAME, skeletonIndex)) {
            playButtonPressAnimation("Buttons/PlayPress", skeletonIndex);
        } else if (isHoveringButton(x, y, Constants.MainMenuScreen.BUTTON_QUIT_NAME, skeletonIndex)) {
            playButtonPressAnimation("Buttons/QuitPress", skeletonIndex);
        } else if (isHoveringButton(x, y, Constants.MainMenuScreen.BUTTON_STGS_NAME, skeletonIndex)) {
            playButtonPressAnimation("Buttons/SettingsPress", skeletonIndex);
        }
    }

    private void initializeAnimations() {
        initializeMenuSkeleton(AnimationType.MENU_1.ordinal());
        initializeMenuSkeleton(AnimationType.MENU_2.ordinal());
    }

    private void initializeMenuSkeleton(int skeletonIndex) {

        String atlasPath = Constants.MainMenuScreen.ATLAS;
        String skeletonPath = Constants.MainMenuScreen.JSON;

        skeletons.insert(skeletonIndex, game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath));
        states.insert(skeletonIndex, game.getSpineAnimationHandler().createAnimationState(skeletons.get(skeletonIndex)));

        states.get(skeletonIndex).setAnimation(0, "animation", false);

        states.get(skeletonIndex).addListener(new AnimationState.AnimationStateListener() {
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
                if (entry.getAnimation().getName().equals("animation")) {
                    isInitialAnimationFinished = true;
                }
            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
            }
        });
    }

    private void renderBackground() {
        backgroundStage.getViewport().apply();
        game.getBatch().setProjectionMatrix(backgroundStage.getViewport().getCamera().combined);
        game.getBatch().begin();
        game.getBatch().draw(backgroundTexture, 0, 0, backgroundStage.getViewport().getWorldWidth(), backgroundStage.getViewport().getWorldHeight());
        game.getBatch().end();
    }

    private void renderMainContent() {
        renderMenu(game.getBatch(), stage.getViewport(), AnimationType.MENU_1.ordinal());
        renderDebug(stage.getViewport(), Color.RED, AnimationType.MENU_1.ordinal());
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        TrailDot.renderTrail(game.getBatch(), stage.getViewport());
    }

    private void renderMinimap() {
        if (!minimapIsOn)
            return;
        minimapStage.getViewport().apply();
        game.getBatch().setProjectionMatrix(minimapStage.getViewport().getCamera().combined);
        game.getBatch().begin();
        game.getBatch().draw(minimapTexture, 0, 0, minimapStage.getViewport().getWorldWidth(), minimapStage.getViewport().getWorldHeight());
        game.getBatch().end();

        renderMenu(game.getBatch(), minimapStage.getViewport(), AnimationType.MENU_2.ordinal());
        renderDebug(minimapStage.getViewport(), Color.GREEN, AnimationType.MENU_2.ordinal());
        TrailDot.renderTrail(game.getBatch(), minimapStage.getViewport());
    }

    private void renderMenu(SpriteBatch batch, Viewport viewport, int skeletonIndex) {
        Skeleton skeleton = skeletons.get(skeletonIndex);
        AnimationState state = states.get(skeletonIndex);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        state.update(Gdx.graphics.getDeltaTime());
        state.apply(skeleton);
        skeleton.updateWorldTransform();
        updateSkeletonScaleAndPosition(viewport, skeletonIndex);

        skeletonRenderer.draw(batch, skeleton);

        batch.end();
    }

    private void playButtonPressAnimation(final String animationName, int skeletonIndex) {
        Gdx.app.log("MainMenuScreen", "Playing button press animation: " + animationName);
        states.get(skeletonIndex).setAnimation(4, animationName, false).setListener(new AnimationState.AnimationStateListener() {
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
                Gdx.app.log("MainMenuScreen", "Animation complete: " + animationName);
                if (animationName.equals("Buttons/PlayPress")) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Gdx.app.log("MainMenuScreen", "Changing screen to: ZeldaScreen");
                            game.getScreenManager().pushScreen(new ZeldaScreen(), createTransition());
                        }
                    });
                } else if (animationName.equals("Buttons/SettingsPress")) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Gdx.app.log("MainMenuScreen", "Changing screen to: ConfigScreen");
                            game.getScreenManager().pushScreen(new ConfigurationScreen(), createTransition());
                        }
                    });
                } else if (animationName.equals("Buttons/QuitPress")) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Gdx.net.openURI("https://www.google.com");
                        }
                    });
                }

            }

            @Override
            public void event(AnimationState.TrackEntry entry, Event event) {
            }
        });
    }

    private void renderWithFBO() {
        fbo.begin();

        clearScreen();
        renderBackground();
        renderMainContent();

        fbo.end();

        fboMinimap.begin();

        clearScreen();
        renderMinimap();

        fboMinimap.end();

        stage.getViewport().apply();
        game.getBatch().setProjectionMatrix(stage.getViewport().getCamera().combined);
        game.getBatch().begin();
        game.getBatch().draw(fboRegion, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        // maintain at stage viewport because the size is handled in renderMinimap()
        game.getBatch().draw(fboRegionMinimap, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        game.getBatch().end();
    }

    @Override
    public void render(float delta) {
        clearScreen();
        renderBackground();
        renderMainContent();
        renderMinimap();

        // renderWithFBO();
    }

    private void renderDebug(Viewport viewport, Color color, int skeletonIndex) {
        if (debugMode) {
            shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(color);
            drawDebugBounds(Constants.MainMenuScreen.BUTTON_PLAY_NAME, skeletonIndex);
            drawDebugBounds(Constants.MainMenuScreen.BUTTON_QUIT_NAME, skeletonIndex);
            drawDebugBounds(Constants.MainMenuScreen.BUTTON_STGS_NAME, skeletonIndex);
            shapeRenderer.end();
        }
    }

    private void drawDebugBounds(String buttonName, int skeletonIndex) {
        Rectangle bounds = getButtonBounds(buttonName, skeletonIndex);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public void resize(int width, int height) {
        createFBO(width, height);
        createFBOMinimap(width, height);
        stage.getViewport().update(width, height, true);
        backgroundStage.getViewport().update(width, height, true);
        updateMinimapViewport(width, height);
    }

    private void updateMinimapViewport(int width, int height) {
        if (!minimapIsOn)
            return;
        minimapStage.getViewport().update(width, height, true);
        minimapStage.getViewport().setScreenBounds(0, 0, 200, 200);
    }

    private void updateSkeletonScaleAndPosition(Viewport viewport, int skeletonIndex) {
        setSkeletonScale(skeletons.get(skeletonIndex), Constants.MainMenuScreen.SKEL_WIDTH_PERCENTAGE, Constants.MainMenuScreen.SKEL_HEIGHT_PERCENTAGE, viewport);
        setSkeletonPosition(skeletons.get(skeletonIndex), viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stage != null)
            stage.dispose();
        if (minimapStage != null)
            minimapStage.dispose();
        if (backgroundStage != null)
            backgroundStage.dispose();
        if (game.getBatch() != null) {
            game.getBatch().dispose();
        }
    }
}
