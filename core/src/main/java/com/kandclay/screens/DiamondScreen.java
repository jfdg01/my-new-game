package com.kandclay.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.Slot;
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
        viewport = new FitViewport(800, 800);
        stage = new Stage(viewport);

        skeletonRenderer = new SkeletonRenderer();
        skeletonRenderer.setPremultipliedAlpha(true);

        shapeRenderer = new ShapeRenderer();

        initializeSkeleton();

        stage.addListener(new InputListener() {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                // Implement hover behavior if needed
                return false;
            }
        });

        addInputProcessor(stage);
    }

    private void initializeSkeleton() {
        String atlasPath = Constants.DiamondScreen.ATLAS; // Replace with actual constant
        String skeletonPath = Constants.DiamondScreen.JSON; // Replace with actual constant

        skeleton = game.getSpineAnimationHandler().createSkeleton(atlasPath, skeletonPath);
        state = game.getSpineAnimationHandler().createAnimationState(skeleton);

        state.setAnimation(0, "rotating-animation", true);
    }

    @Override
    public void render(float delta) {
        clearScreen(0.5f, 0.5f, 0.5f, 1);

        state.update(delta);
        state.apply(skeleton);
        skeleton.updateWorldTransform();

        viewport.apply();
        game.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        game.getBatch().begin();
        skeletonRenderer.draw(game.getBatch(), skeleton);
        game.getBatch().end();

        // Optionally, draw debug shapes or additional graphics here
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Example: draw a circle at the skeleton's root bone position
        Vector2 skeletonPos = new Vector2(skeleton.getX(), skeleton.getY());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(skeletonPos.x, skeletonPos.y, 10);

        shapeRenderer.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        setSkeletonScale(skeleton, 0.8f, 0.8f, viewport);
        // setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2);
        setSkeletonPosition(skeleton, viewport.getWorldWidth() / 2, 20);
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
