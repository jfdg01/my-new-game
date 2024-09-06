package com.kandclay.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.*;
import com.esotericsoftware.spine.*;
import com.kandclay.utils.Constants;

public class DiamondScreen extends BaseScreen {
    private Stage stage;
    private Skeleton skeleton;
    private AnimationState state;
    private SkeletonRenderer skeletonRenderer;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    public DiamondScreen() {
        super();
    }

    @Override
    public void show() {
        viewport = new ScreenViewport(); //
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);

        shapeRenderer = new ShapeRenderer();

        initializeSkeleton();
        updateSkeletonBounds();

        setupSkeletonDragging(stage);
        addInputProcessor(stage);

        setSkeletonScale(skeleton, 0.8f, 0.8f, viewport);
        setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, 0);
    }

    private void initializeSkeleton() {
        String atlasPath = Constants.DiamondScreen.ATLAS;
        String skeletonPath = Constants.DiamondScreen.JSON;
        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);
        state.setAnimation(0, "rotating-animation", true);
        skeletons.add(skeleton);  // Add the skeleton to the array
    }

    public void update(float delta) {
        state.update(delta);
        state.apply(skeleton);
        skeleton.updateWorldTransform();
        updateSkeletonBounds();
        stage.act(delta);
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen(0.1f, 0.1f, 0.1f, 1);

        viewport.apply();
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        game.getBatch().begin();
        skeletonRenderer.draw(game.getBatch(), skeleton);
        game.getBatch().end();

        // Draw debug shapes
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw circle at skeleton's root position
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(skeleton.getX(), skeleton.getY(), 10);

        shapeRenderer.end();

        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
        updateSkeletonBounds();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (stage != null) {
            stage.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
