package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.kandclay.utils.Constants;

public class ZeldaScreen extends BaseScreen {
    private Stage stage;
    private Skeleton skeleton;
    private AnimationState state;
    private SkeletonRenderer skeletonRenderer;
    private Viewport viewport;

    public ZeldaScreen() {
        super();
    }

    @Override
    public void show() {

        viewport = new ExtendViewport(Constants.General.EMBED_WIDTH, Constants.General.EMBED_HEIGHT);
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);

        initializeZeldaSkeleton();

        Gdx.input.setInputProcessor(stage);
    }

    private void initializeZeldaSkeleton() {

        String atlasPath = Constants.Zelda.ATLAS;
        String skeletonPath = Constants.Zelda.JSON;

        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);

        // Set initial animation - adjust "animation" to match your skeleton's animation name
        state.setAnimation(0, "animation", true);

        setSkeletonScale(skeleton, 0.5f, 0.5f, viewport);  // Adjust scale as needed
        setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
    }

    @Override
    public void render(float delta) {
        clearScreen(255, 255, 255, 0);

        state.update(delta);
        state.apply(skeleton);
        skeleton.updateWorldTransform();

        viewport.apply();
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        game.getBatch().begin();
        skeletonRenderer.draw(game.getBatch(), skeleton);
        game.getBatch().end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setSkeletonScale(skeleton, 0.5f, 0.5f, viewport);  // Adjust scale as needed
        setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stage != null) {
            stage.dispose();
        }
    }
}
